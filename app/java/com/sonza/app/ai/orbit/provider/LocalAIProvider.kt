package com.sonza.app.ai.orbit.provider

import com.sonza.app.ai.orbit.OrbitAIResponse
import com.sonza.app.ai.orbit.OrbitChatMessage
import com.sonza.app.ai.orbit.OrbitMusicContext
import com.sonza.app.ai.orbit.OrbitProviderType
import com.sonza.app.ai.orbit.OrbitToolCall
import java.util.Locale

/**
 * On-device Local AI Provider for Sonza Orbit.
 *
 * Operates 100% offline with zero external network dependencies and zero API keys.
 * Handles music-aware reasoning, local command routing, multi-turn follow-ups,
 * and device controls.
 */
class LocalAIProvider : OrbitAIProvider {

    override val providerType: OrbitProviderType = OrbitProviderType.LOCAL

    override suspend fun processChat(
        message: String,
        musicContext: OrbitMusicContext?,
        conversationHistory: List<OrbitChatMessage>
    ): OrbitAIResponse {
        val s = message.trim().lowercase(Locale.ROOT)
            .replace(Regex("[?,!.'\"]"), "")

        // 1. Music Contextual Questions ("who sings this", "what song is this", "what does this mean")
        val currentTitle = musicContext?.title
        val currentArtist = musicContext?.artist

        if (s.contains("who sang") || s.contains("who sings") || s.contains("who is singing") || s.contains("who made this") || s.contains("whos the artist") || s.contains("who is the artist")) {
            return if (!currentTitle.isNullOrBlank() && !currentArtist.isNullOrBlank()) {
                OrbitAIResponse(
                    spokenText = "$currentTitle is performed by $currentArtist.",
                    providerUsed = providerType
                )
            } else if (!currentArtist.isNullOrBlank()) {
                OrbitAIResponse(
                    spokenText = "This track is by $currentArtist.",
                    providerUsed = providerType
                )
            } else {
                OrbitAIResponse(
                    spokenText = "I don't have song details right now. Play a track first, or name a specific song.",
                    providerUsed = providerType
                )
            }
        }

        if (s.contains("what song is this") || s.contains("whats playing") || s.contains("what is playing") || s.contains("what am i listening to")) {
            return if (!currentTitle.isNullOrBlank()) {
                val artistPart = if (!currentArtist.isNullOrBlank()) " by $currentArtist" else ""
                OrbitAIResponse(
                    spokenText = "This is $currentTitle$artistPart.",
                    providerUsed = providerType
                )
            } else {
                OrbitAIResponse(
                    spokenText = "No music is currently playing on Sonza.",
                    providerUsed = providerType
                )
            }
        }

        if (s.contains("what does it mean") || s.contains("what does this mean") || s.contains("meaning of this song")) {
            return if (!currentTitle.isNullOrBlank()) {
                val artistPart = if (!currentArtist.isNullOrBlank()) " by $currentArtist" else ""
                OrbitAIResponse(
                    spokenText = "$currentTitle$artistPart explores expressive emotional themes and storytelling through melody and rhythm.",
                    providerUsed = providerType
                )
            } else {
                OrbitAIResponse(
                    spokenText = "Play a song first, and I can tell you more about it.",
                    providerUsed = providerType
                )
            }
        }

        // 2. Music Recommendations & Similar Songs
        if (s.contains("similar songs") || s.contains("similar music") || s.contains("more like this") || s.contains("play something similar")) {
            val seedArtist = currentArtist ?: extractLastMentionedArtist(conversationHistory)
            val seedTitle = currentTitle ?: ""
            return OrbitAIResponse(
                spokenText = if (!seedArtist.isNullOrBlank()) "Finding tracks similar to $seedArtist." else "Playing a curated similar mix for you.",
                toolCalls = listOf(
                    OrbitToolCall(
                        tool = "play_music",
                        arguments = mapOf("query" to (seedArtist ?: "popular music"), "isSimilar" to true)
                    )
                ),
                providerUsed = providerType
            )
        }

        // 3. Playback Controls via Tool Call
        if (s in listOf("pause", "pause music", "stop music", "hold on")) {
            return OrbitAIResponse(
                spokenText = "Paused.",
                toolCalls = listOf(OrbitToolCall(tool = "playback_control", arguments = mapOf("action" to "pause"))),
                providerUsed = providerType
            )
        }

        if (s in listOf("resume", "play", "unpause", "keep playing")) {
            return OrbitAIResponse(
                spokenText = "Resuming playback.",
                toolCalls = listOf(OrbitToolCall(tool = "playback_control", arguments = mapOf("action" to "resume"))),
                providerUsed = providerType
            )
        }

        if (s in listOf("skip", "next song", "next track", "skip this")) {
            return OrbitAIResponse(
                spokenText = "Skipping to the next song.",
                toolCalls = listOf(OrbitToolCall(tool = "playback_control", arguments = mapOf("action" to "next"))),
                providerUsed = providerType
            )
        }

        if (s in listOf("replay", "play again", "restart this song", "start over")) {
            return OrbitAIResponse(
                spokenText = "Replaying ${currentTitle ?: "the current song"}.",
                toolCalls = listOf(OrbitToolCall(tool = "playback_control", arguments = mapOf("action" to "replay"))),
                providerUsed = providerType
            )
        }

        // 4. Play Direct Track Intent
        if (s.startsWith("play ")) {
            val query = s.removePrefix("play ").trim()
            val byParts = query.split(Regex("\\bby\\b"))
            val title = byParts[0].trim()
            val artist = if (byParts.size > 1) byParts[1].trim() else null

            return OrbitAIResponse(
                spokenText = if (artist != null) "Playing $title by $artist." else "Playing $title.",
                toolCalls = listOf(
                    OrbitToolCall(
                        tool = "play_music",
                        arguments = mapOf("title" to title, "artist" to (artist ?: ""))
                    )
                ),
                providerUsed = providerType
            )
        }

        // 5. Volume Control
        if (s.contains("louder") || s.contains("volume up") || s.contains("increase volume")) {
            return OrbitAIResponse(
                spokenText = "Volume turned up.",
                toolCalls = listOf(OrbitToolCall(tool = "set_volume", arguments = mapOf("action" to "up"))),
                providerUsed = providerType
            )
        }

        if (s.contains("softer") || s.contains("quieter") || s.contains("volume down") || s.contains("lower volume")) {
            return OrbitAIResponse(
                spokenText = "Volume turned down.",
                toolCalls = listOf(OrbitToolCall(tool = "set_volume", arguments = mapOf("action" to "down"))),
                providerUsed = providerType
            )
        }

        // 6. Device Tools: App Launch, Phone Call, Timers
        if (s.startsWith("open ") || s.startsWith("launch ")) {
            val appName = s.removePrefix("open ").removePrefix("launch ").trim()
            return OrbitAIResponse(
                spokenText = "Opening $appName.",
                toolCalls = listOf(OrbitToolCall(tool = "open_app", arguments = mapOf("appName" to appName))),
                providerUsed = providerType
            )
        }

        if (s.startsWith("call ") || s.startsWith("dial ")) {
            val contact = s.removePrefix("call ").removePrefix("dial ").trim()
            return OrbitAIResponse(
                spokenText = "Calling $contact.",
                toolCalls = listOf(OrbitToolCall(tool = "call_phone", arguments = mapOf("contact" to contact))),
                providerUsed = providerType
            )
        }

        if (s.contains("timer") || s.contains("sleep timer") || s.contains("remind me in")) {
            val minutesMatch = Regex("(\\d+)\\s*(?:minute|min)").find(s)
            val minutes = minutesMatch?.groupValues?.get(1)?.toIntOrNull() ?: 15
            return OrbitAIResponse(
                spokenText = "Setting a $minutes minute sleep timer.",
                toolCalls = listOf(OrbitToolCall(tool = "set_timer", arguments = mapOf("minutes" to minutes))),
                providerUsed = providerType
            )
        }

        // 7. Web Search Tool
        if (s.startsWith("search ") || s.startsWith("look up ") || s.startsWith("search the web for ")) {
            val query = s.removePrefix("search the web for ").removePrefix("search ").removePrefix("look up ").trim()
            return OrbitAIResponse(
                spokenText = "Searching for $query.",
                toolCalls = listOf(OrbitToolCall(tool = "web_search", arguments = mapOf("query" to query))),
                providerUsed = providerType
            )
        }

        // 8. General conversational queries
        if (s.contains("how are you") || s.contains("whats up")) {
            return OrbitAIResponse(
                spokenText = "I'm doing great, ready to play your favorite music.",
                providerUsed = providerType
            )
        }

        // Fallback for local AI without external web access
        return OrbitAIResponse(
            spokenText = "I'm running in local mode. You can ask me to play any song, control music, set timers, or open apps.",
            providerUsed = providerType
        )
    }

    private fun extractLastMentionedArtist(history: List<OrbitChatMessage>): String? {
        for (msg in history.reversed()) {
            if (msg.role == "assistant" && msg.content.contains("by ", ignoreCase = true)) {
                val parts = msg.content.split(Regex("\\bby\\b", RegexOption.IGNORE_CASE))
                if (parts.size > 1) {
                    return parts[1].replace(Regex("[.,!]"), "").trim()
                }
            }
        }
        return null
    }
}
