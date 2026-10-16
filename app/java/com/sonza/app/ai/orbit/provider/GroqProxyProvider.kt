package com.sonza.app.ai.orbit.provider

import com.sonza.app.ai.orbit.OrbitAIResponse
import com.sonza.app.ai.orbit.OrbitChatMessage
import com.sonza.app.ai.orbit.OrbitMusicContext
import com.sonza.app.ai.orbit.OrbitProviderType
import com.sonza.app.ai.orbit.network.OrbitChatResult
import com.sonza.app.ai.orbit.network.OrbitConversationMessage
import com.sonza.app.ai.orbit.network.OrbitProxyClient
import com.sonza.app.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient

/**
 * Groq AI Provider via Secure Cloudflare Workers Orbit Proxy.
 *
 * CRITICAL ARCHITECTURAL GUARANTEE:
 * The Android client contains ZERO Groq API keys or secrets.
 * All authentication occurs securely on the serverless proxy.
 * The Android app communicates ONLY with the Cloudflare Orbit Proxy.
 */
class GroqProxyProvider(
    private val baseHttpClient: OkHttpClient,
    private val sessionManager: SessionManager? = null,
    private val proxyUrlProvider: (suspend () -> String)? = null,
    private val proxyTokenProvider: (suspend () -> String)? = null,
    proxyClient: OrbitProxyClient? = null
) : OrbitAIProvider {

    override val providerType: OrbitProviderType = OrbitProviderType.GROQ

    private val client: OrbitProxyClient by lazy {
        proxyClient ?: OrbitProxyClient(
            baseHttpClient = baseHttpClient,
            sessionManager = sessionManager,
            baseUrlProvider = proxyUrlProvider,
            clientTokenProvider = proxyTokenProvider
        )
    }

    override suspend fun processChat(
        message: String,
        musicContext: OrbitMusicContext?,
        conversationHistory: List<OrbitChatMessage>
    ): OrbitAIResponse = withContext(Dispatchers.IO) {
        val conversationMessages = mutableListOf<OrbitConversationMessage>()

        // Include music context awareness if active
        if (musicContext != null && musicContext.hasActiveSong) {
            conversationMessages.add(
                OrbitConversationMessage(
                    role = "system",
                    content = "Current playback: ${musicContext.toSpokenDescription()}"
                )
            )
        }

        // Add history turns (filtered to valid roles)
        conversationHistory.takeLast(16).forEach { turn ->
            val role = turn.role.lowercase()
            if (role in listOf("user", "assistant", "system")) {
                conversationMessages.add(
                    OrbitConversationMessage(
                        role = role,
                        content = turn.content
                    )
                )
            }
        }

        when (val result = client.sendChat(message = message, conversation = conversationMessages)) {
            is OrbitChatResult.Success -> {
                OrbitAIResponse(
                    spokenText = result.aiResponse,
                    providerUsed = providerType,
                    isSuccess = true
                )
            }
            is OrbitChatResult.Error -> {
                OrbitAIResponse(
                    spokenText = result.spokenError,
                    providerUsed = providerType,
                    isSuccess = false,
                    errorMessage = result.errorCode
                )
            }
        }
    }
}
