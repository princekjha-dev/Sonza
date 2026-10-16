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
        return if (effective.endsWith("/")) effective else "$effective/"
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
        val api = try {
            getApi()
        } catch (e: Exception) {
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

        try {
            val response = api.chat(
                request = request,
                authorization = authHeader,
                clientToken = customTokenHeader
            )

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && !body.response.isNullOrBlank()) {
                    OrbitChatResult.Success(
                        aiResponse = body.response.trim()
                    )
                } else {
                    val errCode = body?.error?.code ?: "UNKNOWN_RESPONSE"
                    val errMsg = body?.error?.message ?: "Received invalid response from Orbit proxy."
                    OrbitLogger.error("Orbit proxy returned unsuccessful response: $errCode - $errMsg")
                    OrbitChatResult.Error(
                        spokenError = "Sorry, I had trouble processing that request.",
                        errorCode = errCode,
                        rawMessage = errMsg
                    )
                }
            } else {
                val code = response.code()
                val errorBodyStr = response.errorBody()?.string().orEmpty()
                val parsedError = parseErrorBody(errorBodyStr)

                OrbitLogger.error("Orbit proxy HTTP $code: $errorBodyStr")

                val spoken = when (code) {
                    429 -> "I'm receiving too many requests right now. Please try again in a moment."
                    504 -> "The AI service took too long to respond. I can still handle your local commands."
                    502, 503 -> "The AI service is temporarily unavailable. I can still handle your music and device commands."
                    400 -> parsedError?.message ?: "I couldn't process that request format."
                    else -> "The AI service is temporarily unavailable."
                }

                OrbitChatResult.Error(
                    spokenError = spoken,
                    errorCode = parsedError?.code ?: "HTTP_$code",
                    rawMessage = "HTTP $code: ${parsedError?.message ?: errorBodyStr}"
                )
            }
        } catch (_: SocketTimeoutException) {
            OrbitLogger.error("OrbitProxyClient timeout")
            OrbitChatResult.Error(
                spokenError = "The AI service took too long to respond. I can still handle local commands.",
                errorCode = "TIMEOUT",
                rawMessage = "SocketTimeoutException"
            )
        } catch (_: UnknownHostException) {
            OrbitLogger.error("OrbitProxyClient unknown host / offline")
            OrbitChatResult.Error(
                spokenError = "I can't reach the AI proxy right now. I can still handle your music and local commands.",
                errorCode = "NETWORK_UNAVAILABLE",
                rawMessage = "UnknownHostException"
            )
        } catch (_: ConnectException) {
            OrbitLogger.error("OrbitProxyClient connection failed")
            OrbitChatResult.Error(
                spokenError = "Unable to connect to Orbit proxy. I can still handle local commands.",
                errorCode = "CONNECT_ERROR",
                rawMessage = "ConnectException"
            )
        } catch (e: IOException) {
            OrbitLogger.error("OrbitProxyClient IO failure: ${e.message}", e)
            OrbitChatResult.Error(
                spokenError = "I'm having trouble connecting to the network.",
                errorCode = "IO_ERROR",
                rawMessage = e.message
            )
        } catch (e: Exception) {
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
        try {
            val api = getApi()
            val response = api.health()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
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
