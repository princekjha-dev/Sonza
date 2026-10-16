package com.sonza.app.ai.orbit.provider

import com.sonza.app.ai.orbit.OrbitAIResponse
import com.sonza.app.ai.orbit.OrbitChatMessage
import com.sonza.app.ai.orbit.OrbitMusicContext
import com.sonza.app.ai.orbit.OrbitProviderType
import com.sonza.app.ai.orbit.OrbitToolCall
import com.sonza.app.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Groq AI Provider via Secure Cloudflare Workers / Serverless Proxy.
 *
 * CRITICAL ARCHITECTURAL GUARANTEE:
 * The Android client contains ZERO Groq API keys or secrets.
 * All authentication occurs securely on the serverless proxy via environment secrets.
 */
class GroqProxyProvider(
    private val baseHttpClient: OkHttpClient,
    private val sessionManager: SessionManager? = null,
    private val proxyUrlProvider: (suspend () -> String)? = null,
    private val proxyTokenProvider: (suspend () -> String)? = null
) : OrbitAIProvider {

    override val providerType: OrbitProviderType = OrbitProviderType.GROQ

    private val httpClient: OkHttpClient by lazy {
        baseHttpClient.newBuilder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    override suspend fun processChat(
        message: String,
        musicContext: OrbitMusicContext?,
        conversationHistory: List<OrbitChatMessage>
    ): OrbitAIResponse = withContext(Dispatchers.IO) {
        val proxyBaseUrl = (proxyUrlProvider?.invoke() ?: sessionManager?.getOrbitProxyBaseUrl() ?: "").trim().removeSuffix("/")
        val proxyAuthToken = (proxyTokenProvider?.invoke() ?: sessionManager?.getOrbitProxyAuthToken() ?: "").trim()

        if (proxyBaseUrl.isBlank()) {
            return@withContext OrbitAIResponse(
                spokenText = "Orbit cloud proxy is not configured. Please set your proxy URL in Settings, or switch to Local AI.",
                providerUsed = providerType,
                isSuccess = false,
                errorMessage = "Proxy URL unconfigured"
            )
        }

        val endpoint = "$proxyBaseUrl/v1/orbit/chat"

        try {
            val jsonPayload = JSONObject().apply {
                put("message", message)
                put("provider", "groq")

                if (musicContext != null && musicContext.hasActiveSong) {
                    put("musicContext", JSONObject().apply {
                        put("title", musicContext.title ?: "")
                        put("artist", musicContext.artist ?: "")
                        put("album", musicContext.album ?: "")
                        put("isPlaying", musicContext.isPlaying)
                        if (musicContext.durationMs != null) put("durationMs", musicContext.durationMs)
                        if (musicContext.currentPositionMs != null) put("currentPositionMs", musicContext.currentPositionMs)
                        put("source", musicContext.source ?: "youtube")
                    })
                }

                if (conversationHistory.isNotEmpty()) {
                    val historyArray = JSONArray()
                    conversationHistory.takeLast(8).forEach { turn ->
                        historyArray.put(JSONObject().apply {
                            put("role", turn.role)
                            put("content", turn.content)
                        })
                    }
                    put("history", historyArray)
                }
            }

            val requestBuilder = Request.Builder()
                .url(endpoint)
                .post(jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))

            if (proxyAuthToken.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer $proxyAuthToken")
                requestBuilder.header("X-Orbit-Client-Token", proxyAuthToken)
            }

            val response = httpClient.newCall(requestBuilder.build()).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return@withContext OrbitAIResponse(
                    spokenText = if (response.code == 429) {
                        "Too many requests. Please wait a moment."
                    } else {
                        "The AI service is temporarily unavailable."
                    },
                    providerUsed = providerType,
                    isSuccess = false,
                    errorMessage = "HTTP ${response.code}"
                )
            }

            val jsonResponse = JSONObject(responseBody)
            val spokenText = jsonResponse.optString("response", "").ifBlank {
                jsonResponse.optString("message", "I understood your request.")
            }

            val toolCalls = mutableListOf<OrbitToolCall>()
            val toolsJsonArray = jsonResponse.optJSONArray("toolCalls")
            if (toolsJsonArray != null) {
                for (i in 0 until toolsJsonArray.length()) {
                    val toolObj = toolsJsonArray.getJSONObject(i)
                    val toolName = toolObj.optString("tool", "")
                    val argsObj = toolObj.optJSONObject("arguments")
                    val argsMap = mutableMapOf<String, Any?>()
                    if (argsObj != null) {
                        for (key in argsObj.keys()) {
                            argsMap[key] = argsObj.get(key)
                        }
                    }
                    if (toolName.isNotBlank()) {
                        toolCalls.add(OrbitToolCall(toolName, argsMap))
                    }
                }
            }

            OrbitAIResponse(
                spokenText = spokenText,
                toolCalls = toolCalls,
                providerUsed = providerType,
                isSuccess = true
            )
        } catch (_: SocketTimeoutException) {
            OrbitAIResponse(
                spokenText = "The AI service took too long to respond. I can still handle local commands.",
                providerUsed = providerType,
                isSuccess = false,
                errorMessage = "Timeout"
            )
        } catch (_: UnknownHostException) {
            OrbitAIResponse(
                spokenText = "I can't reach the AI service right now. I can still handle local commands.",
                providerUsed = providerType,
                isSuccess = false,
                errorMessage = "Network unavailable"
            )
        } catch (_: IOException) {
            OrbitAIResponse(
                spokenText = "I'm having trouble connecting to the network.",
                providerUsed = providerType,
                isSuccess = false,
                errorMessage = "IO Failure"
            )
        } catch (e: Exception) {
            OrbitAIResponse(
                spokenText = "Sorry, I had trouble processing that request.",
                providerUsed = providerType,
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }
}
