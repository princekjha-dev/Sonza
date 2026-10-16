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
import java.util.concurrent.TimeUnit

/**
 * Custom Endpoint Provider for self-hosters running Ollama, LocalAI, vLLM,
 * or custom HTTPS gateways.
 */
class CustomEndpointProvider(
    private val baseHttpClient: OkHttpClient,
    private val sessionManager: SessionManager? = null,
    private val proxyUrlProvider: (suspend () -> String)? = null,
    private val proxyTokenProvider: (suspend () -> String)? = null
) : OrbitAIProvider {

    override val providerType: OrbitProviderType = OrbitProviderType.CUSTOM

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
        val customUrl = (proxyUrlProvider?.invoke() ?: sessionManager?.getOrbitProxyBaseUrl() ?: "").trim().removeSuffix("/")
        val token = (proxyTokenProvider?.invoke() ?: sessionManager?.getOrbitProxyAuthToken() ?: "").trim()

        if (customUrl.isBlank()) {
            return@withContext OrbitAIResponse(
                spokenText = "Custom endpoint URL is not configured. Please check your settings.",
                providerUsed = providerType,
                isSuccess = false,
                errorMessage = "Custom URL empty"
            )
        }

        val endpoint = if (customUrl.endsWith("/chat") || customUrl.endsWith("/chat/completions")) {
            customUrl
        } else {
            "$customUrl/v1/orbit/chat"
        }

        try {
            val jsonPayload = JSONObject().apply {
                put("message", message)
                put("provider", "custom")
                if (musicContext != null && musicContext.hasActiveSong) {
                    put("musicContext", JSONObject().apply {
                        put("title", musicContext.title ?: "")
                        put("artist", musicContext.artist ?: "")
                        put("album", musicContext.album ?: "")
                        put("isPlaying", musicContext.isPlaying)
                    })
                }
                if (conversationHistory.isNotEmpty()) {
                    val historyArray = JSONArray()
                    conversationHistory.takeLast(6).forEach { turn ->
                        historyArray.put(JSONObject().apply {
                            put("role", turn.role)
                            put("content", turn.content)
                        })
                    }
                    put("history", historyArray)
                }
            }

            val reqBuilder = Request.Builder()
                .url(endpoint)
                .post(jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))

            if (token.isNotBlank()) {
                reqBuilder.header("Authorization", "Bearer $token")
            }

            val resp = httpClient.newCall(reqBuilder.build()).execute()
            val respBody = resp.body?.string().orEmpty()

            if (!resp.isSuccessful) {
                return@withContext OrbitAIResponse(
                    spokenText = "The custom AI service returned an error.",
                    providerUsed = providerType,
                    isSuccess = false,
                    errorMessage = "HTTP ${resp.code}"
                )
            }

            val json = JSONObject(respBody)
            val spoken = json.optString("response", "").ifBlank {
                json.optString("message", "Request completed.")
            }

            OrbitAIResponse(
                spokenText = spoken,
                providerUsed = providerType,
                isSuccess = true
            )
        } catch (_: IOException) {
            OrbitAIResponse(
                spokenText = "I couldn't reach your custom AI endpoint.",
                providerUsed = providerType,
                isSuccess = false,
                errorMessage = "Network failure"
            )
        } catch (e: Exception) {
            OrbitAIResponse(
                spokenText = "Sorry, I had trouble processing that request with your custom endpoint.",
                providerUsed = providerType,
                isSuccess = false,
                errorMessage = e.message
            )
        }
    }
}
