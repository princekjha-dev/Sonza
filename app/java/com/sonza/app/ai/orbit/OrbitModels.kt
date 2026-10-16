package com.sonza.app.ai.orbit

/**
 * Supported Orbit Assistant AI Providers.
 */
enum class OrbitProviderType(val displayName: String) {
    LOCAL("Local AI"),
    GROQ("Groq (Cloud Proxy)"),
    OPENAI("OpenAI (Cloud Proxy)"),
    CUSTOM("Custom Endpoint");

    companion object {
        fun fromString(value: String): OrbitProviderType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: GROQ
        }
    }
}

/**
 * Structured Music Context exposed to Orbit Assistant during voice turns.
 */
data class OrbitMusicContext(
    val title: String? = null,
    val artist: String? = null,
    val album: String? = null,
    val durationMs: Long? = null,
    val currentPositionMs: Long? = null,
    val isPlaying: Boolean = false,
    val source: String? = null
) {
    val hasActiveSong: Boolean get() = !title.isNullOrBlank()

    fun toSpokenDescription(): String {
        if (!hasActiveSong) return "No music is currently playing."
        val artistPart = if (!artist.isNullOrBlank() && artist != "<unknown>") " by $artist" else ""
        val statePart = if (isPlaying) "Now playing" else "Currently paused"
        return "$statePart: $title$artistPart."
    }
}

/**
 * Conversational message turn in Orbit's context memory.
 */
data class OrbitChatMessage(
    val role: String, // "user" | "assistant" | "system"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Structured tool invocation requested by Orbit AI or intent router.
 */
data class OrbitToolCall(
    val tool: String,
    val arguments: Map<String, Any?> = emptyMap()
)

/**
 * Result returned by an Orbit AI Provider.
 */
data class OrbitAIResponse(
    val spokenText: String,
    val toolCalls: List<OrbitToolCall> = emptyList(),
    val providerUsed: OrbitProviderType,
    val isSuccess: Boolean = true,
    val errorMessage: String? = null
)
