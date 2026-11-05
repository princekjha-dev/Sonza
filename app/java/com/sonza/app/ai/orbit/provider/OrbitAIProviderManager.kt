package com.sonza.app.ai.orbit.provider

import com.sonza.app.ai.orbit.OrbitAIResponse
import com.sonza.app.ai.orbit.OrbitChatMessage
import com.sonza.app.ai.orbit.OrbitMusicContext
import com.sonza.app.ai.orbit.OrbitProviderType
import com.sonza.app.data.SessionManager
import okhttp3.OkHttpClient

/**
 * Single-point provider manager for Orbit Voice Assistant.
 * Routes requests to the user's selected provider (Local, Groq proxy, OpenAI proxy, or Custom).
 * Automatically falls back to LocalAIProvider when network or cloud proxy is unavailable.
 */
class OrbitAIProviderManager(
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager? = null,
    private val providerTypeProvider: (suspend () -> String)? = null,
    proxyUrlProvider: (suspend () -> String)? = null,
    proxyTokenProvider: (suspend () -> String)? = null
) {
    val localProvider = LocalAIProvider()
    val groqProvider = GroqProxyProvider(okHttpClient, sessionManager, proxyUrlProvider, proxyTokenProvider)
    val openAiProvider = OpenAIProxyProvider(okHttpClient, sessionManager, proxyUrlProvider, proxyTokenProvider)
    val customProvider = CustomEndpointProvider(okHttpClient, sessionManager, proxyUrlProvider, proxyTokenProvider)

    fun getProvider(type: OrbitProviderType): OrbitAIProvider {
        return when (type) {
            OrbitProviderType.LOCAL -> localProvider
            OrbitProviderType.GROQ -> groqProvider
            OrbitProviderType.OPENAI -> openAiProvider
            OrbitProviderType.CUSTOM -> customProvider
        }
    }

    suspend fun getActiveProviderType(): OrbitProviderType {
        val name = providerTypeProvider?.invoke() ?: sessionManager?.getOrbitAiProvider() ?: "GROQ"
        return OrbitProviderType.fromString(name)
    }

    /**
     * Executes chat request with graceful local fallback if cloud service is unavailable.
     */
    suspend fun processChat(
        message: String,
        musicContext: OrbitMusicContext?,
        conversationHistory: List<OrbitChatMessage>
    ): OrbitAIResponse {
        val activeType = getActiveProviderType()
        val provider = getProvider(activeType)

        if (activeType == OrbitProviderType.LOCAL) {
            return provider.processChat(message, musicContext, conversationHistory)
        }

        val response = provider.processChat(message, musicContext, conversationHistory)

        // If cloud provider failed due to network / unconfigured, fall back to local provider
        if (!response.isSuccess) {
            val localResult = localProvider.processChat(message, musicContext, conversationHistory)
            // If local provider could answer or execute tool, return local result
            if (localResult.toolCalls.isNotEmpty() || !localResult.spokenText.contains("I'm running in local mode")) {
                return localResult
            }
        }

        return response
    }
}
