package com.sonza.app.ai.orbit.network

import com.google.gson.Gson
import com.sonza.app.ai.orbit.OrbitConstants
import com.sonza.app.ai.orbit.OrbitLogger
import com.sonza.app.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Resilient Retrofit client for the Cloudflare Orbit Proxy.
 *
 * CRITICAL ARCHITECTURAL CONSTRAINTS:
 * 1. Zero AI provider secrets (Groq, OpenAI, Gemini) exist on this client.
 * 2. Communicates ONLY with the configured Cloudflare Orbit Proxy (default: [OrbitConstants.ORBIT_PROXY_URL]).
 * 3. Never crashes on timeout, offline, or malformed responses.
 * 4. Logs complete request lifecycle (URL, method, status code, body, exceptions) without logging secrets.
 */
class OrbitProxyClient(
    private val baseHttpClient: OkHttpClient,
    private val sessionManager: SessionManager? = null,
    private val baseUrlProvider: (suspend () -> String)? = null,
    private val clientTokenProvider: (suspend () -> String)? = null
) {
    private val gson = Gson()

    private val httpClient: OkHttpClient by lazy {
        baseHttpClient.newBuilder()
            .connectTimeout(OrbitConstants.TIMEOUT_CONNECT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(OrbitConstants.TIMEOUT_READ_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(OrbitConstants.TIMEOUT_WRITE_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    @Volatile
    private var cachedBaseUrl: String? = null
    @Volatile
    private var cachedApi: OrbitProxyApi? = null

    suspend fun resolveBaseUrl(): String {
        val configured = baseUrlProvider?.invoke()
            ?: sessionManager?.getOrbitProxyBaseUrl()
            ?: ""
        val trimmed = configured.trim()
        val effective = if (trimmed.isNotBlank()) trimmed else OrbitConstants.ORBIT_PROXY_URL
        // Transparently sanitize any pkiha2028 typo to pkjha2028
        val sanitized = if (effective.contains("pkiha2028.workers.dev")) {
            effective.replace("pkiha2028.workers.dev", "pkjha2028.workers.dev")
        } else {
            effective
        }
        return if (sanitized.endsWith("/")) sanitized else "$sanitized/"
    }

    private suspend fun getApi(): OrbitProxyApi {
        val baseUrl = resolveBaseUrl()
        val currentApi = cachedApi
        if (currentApi != null && cachedBaseUrl == baseUrl) {
            return currentApi
        }
        val api = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(OrbitProxyApi::class.java)

        cachedBaseUrl = baseUrl
        cachedApi = api
        return api
    }

    /**
     * Sends chat prompt and conversation history to POST /v1/chat on Cloudflare Orbit Proxy.
     */
    suspend fun sendChat(
        message: String,
        conversation: List<OrbitConversationMessage> = emptyList(),
        model: String? = null
    ): OrbitChatResult = withContext(Dispatchers.IO) {
        val baseUrl = resolveBaseUrl()
        val endpointUrl = "${baseUrl}v1/chat"

        val api = try {
            getApi()
        } catch (e: Exception) {
            OrbitLogger.http("POST", endpointUrl, exception = e)
            OrbitLogger.error("Failed to initialize OrbitProxyApi: ${e.message}", e)
            return@withContext OrbitChatResult.Error(
                spokenError = "Orbit proxy configuration is invalid.",
                errorCode = "CONFIG_ERROR",
                rawMessage = e.message
            )
        }

        val token = (clientTokenProvider?.invoke()
            ?: sessionManager?.getOrbitProxyAuthToken()
            ?: "").trim()

        val authHeader = if (token.isNotBlank()) "Bearer $token" else null
        val customTokenHeader = if (token.isNotBlank()) token else null

        // Limit conversation to MAX_CONVERSATION_TURNS to adhere strictly to proxy constraints
        val sanitizedConversation = conversation.takeLast(OrbitConstants.MAX_CONVERSATION_TURNS)

        val request = OrbitChatRequest(
            message = message,
            conversation = sanitizedConversation,
            model = model
        )

        OrbitLogger.http("POST", endpointUrl, body = "turns=${sanitizedConversation.size}")

        try {
            val response = api.chat(
                request = request,
                authorization = authHeader,
                clientToken = customTokenHeader
            )

            val code = response.code()

            if (response.isSuccessful) {
                val body = response.body()
                val responseText = body?.response
                OrbitLogger.http("POST", endpointUrl, code = code, body = responseText)

                if (body != null && body.success && !responseText.isNullOrBlank()) {
                    OrbitChatResult.Success(
                        aiResponse = responseText.trim()
                    )
                } else {
                    val errCode = body?.error?.code ?: "UNKNOWN_RESPONSE"
                    val errMsg = body?.error?.message ?: "Received invalid response from Orbit proxy."
                    OrbitLogger.error("Orbit proxy returned error: $errCode - $errMsg")
                    OrbitChatResult.Error(
                        spokenError = "Sorry, I had trouble processing that request.",
                        errorCode = errCode,
                        rawMessage = errMsg
                    )
                }
            } else {
                val errorBodyStr = response.errorBody()?.string().orEmpty()
                val parsedError = parseErrorBody(errorBodyStr)

                OrbitLogger.http("POST", endpointUrl, code = code, body = errorBodyStr)
                OrbitLogger.error("Orbit proxy HTTP $code: $errorBodyStr")

                val spoken = when (code) {
                    400 -> parsedError?.message ?: "I couldn't process that request format."
                    401, 403 -> "Orbit proxy access was rejected. Please check your proxy settings."
                    404 -> "Orbit proxy endpoint not found. Please verify the proxy deployment."
                    429 -> "I'm receiving too many requests right now. Please try again in a moment."
                    504 -> "The AI service took too long to respond. I can still handle your local commands."
                    503 -> {
                        if (parsedError?.code == "MISSING_CONFIGURATION") {
                            "Orbit proxy is not configured on the server."
                        } else {
                            "The AI service is temporarily unavailable. I can still handle your music and device commands."
                        }
                    }
                    500, 502 -> "The AI service is temporarily unavailable. I can still handle your music and device commands."
                    else -> "The AI service is temporarily unavailable."
                }

                OrbitChatResult.Error(
                    spokenError = spoken,
                    errorCode = parsedError?.code ?: "HTTP_$code",
                    rawMessage = "HTTP $code: ${parsedError?.message ?: errorBodyStr}"
                )
            }
        } catch (e: UnknownHostException) {
            OrbitLogger.http("POST", endpointUrl, exception = e)
            OrbitLogger.error("OrbitProxyClient DNS resolution failure / offline: ${e.message}")
            OrbitChatResult.Error(
                spokenError = "I can't reach the AI proxy right now. I can still handle your music and local commands.",
                errorCode = "NETWORK_UNAVAILABLE",
                rawMessage = "UnknownHostException: ${e.message}"
            )
        } catch (e: SocketTimeoutException) {
            OrbitLogger.http("POST", endpointUrl, exception = e)
            OrbitLogger.error("OrbitProxyClient timeout: ${e.message}")
            OrbitChatResult.Error(
                spokenError = "The AI service took too long to respond. I can still handle local commands.",
                errorCode = "TIMEOUT",
                rawMessage = "SocketTimeoutException: ${e.message}"
            )
        } catch (e: ConnectException) {
            OrbitLogger.http("POST", endpointUrl, exception = e)
            OrbitLogger.error("OrbitProxyClient connection failed: ${e.message}")
            OrbitChatResult.Error(
                spokenError = "Unable to connect to Orbit proxy. I can still handle local commands.",
                errorCode = "CONNECT_ERROR",
                rawMessage = "ConnectException: ${e.message}"
            )
        } catch (e: IOException) {
            OrbitLogger.http("POST", endpointUrl, exception = e)
            OrbitLogger.error("OrbitProxyClient IO failure: ${e.message}", e)
            OrbitChatResult.Error(
                spokenError = "I'm having trouble connecting to the network.",
                errorCode = "IO_ERROR",
                rawMessage = "IOException: ${e.message}"
            )
        } catch (e: Exception) {
            OrbitLogger.http("POST", endpointUrl, exception = e)
            OrbitLogger.error("OrbitProxyClient unexpected exception: ${e.message}", e)
            OrbitChatResult.Error(
                spokenError = "Sorry, I had trouble processing that request.",
                errorCode = "UNEXPECTED_ERROR",
                rawMessage = e.message
            )
        }
    }

    /**
     * Checks proxy health via GET /health.
     */
    suspend fun checkHealth(): OrbitHealthResponse? = withContext(Dispatchers.IO) {
        val baseUrl = resolveBaseUrl()
        val healthUrl = "${baseUrl}health"
        try {
            val api = getApi()
            val response = api.health()
            OrbitLogger.http("GET", healthUrl, code = response.code())
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            OrbitLogger.http("GET", healthUrl, exception = e)
            OrbitLogger.error("Orbit health check failed: ${e.message}")
            null
        }
    }

    private fun parseErrorBody(errorBody: String): OrbitChatError? {
        if (errorBody.isBlank()) return null
        return try {
            val envelope = gson.fromJson(errorBody, OrbitChatResponse::class.java)
            envelope?.error
        } catch (_: Exception) {
            null
        }
    }
}

/**
 * Result abstraction for OrbitProxyClient operations.
 */
sealed interface OrbitChatResult {
    data class Success(
        val aiResponse: String
    ) : OrbitChatResult

    data class Error(
        val spokenError: String,
        val errorCode: String,
        val rawMessage: String? = null
    ) : OrbitChatResult
}
