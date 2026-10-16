package com.sonza.app.ai.orbit.provider

import com.sonza.app.ai.orbit.OrbitAIResponse
import com.sonza.app.ai.orbit.OrbitChatMessage
import com.sonza.app.ai.orbit.OrbitMusicContext
import com.sonza.app.ai.orbit.OrbitProviderType

/**
 * Universal abstraction for Orbit AI providers.
 * Decouples Orbit Assistant from any single vendor (Groq, OpenAI, Local AI, Custom).
 */
interface OrbitAIProvider {
    val providerType: OrbitProviderType

    /**
     * Processes a natural language turn from the user.
     *
     * @param message The user's spoken utterance.
     * @param musicContext Structured context of currently playing media (if any).
     * @param conversationHistory Prior turns in the multi-turn session.
     * @return Formatted AI response with spoken text and optional executable tool calls.
     */
    suspend fun processChat(
        message: String,
        musicContext: OrbitMusicContext?,
        conversationHistory: List<OrbitChatMessage>
    ): OrbitAIResponse
}
