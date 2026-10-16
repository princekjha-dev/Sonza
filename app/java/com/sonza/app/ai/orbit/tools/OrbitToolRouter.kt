package com.sonza.app.ai.orbit.tools

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import com.sonza.app.ai.orbit.OrbitResult
import com.sonza.app.ai.orbit.OrbitToolCall
import com.sonza.app.data.repository.YouTubeRepository
import com.sonza.app.player.MusicPlayer
import com.sonza.app.player.SleepTimerManager
import com.sonza.app.player.SleepTimerOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Universal Tool Execution Router for Sonza Orbit Assistant.
 *
 * Implements:
 * - MusicTool: Play, pause, resume, skip, replay, volume, similar music
 * - PhoneTool: Contact dialer / phone call intents
 * - ReminderTool: Sleep timer and reminders
 * - AppTool: Launch installed Android applications
 * - WebSearchTool: Live public knowledge queries
 */
class OrbitToolRouter(
    private val musicPlayer: MusicPlayer,
    private val youTubeRepository: YouTubeRepository,
    private val sleepTimerManager: SleepTimerManager,
    private val webSearchTool: WebSearchTool,
    private val context: Context
) {

    suspend fun executeTool(toolCall: OrbitToolCall): OrbitResult {
        return when (toolCall.tool.lowercase()) {
            "play_music" -> executeMusicTool(toolCall.arguments)
            "playback_control" -> executePlaybackControl(toolCall.arguments)
            "set_volume" -> executeVolume(toolCall.arguments)
            "open_app" -> executeAppTool(toolCall.arguments)
            "call_phone" -> executePhoneTool(toolCall.arguments)
            "set_timer" -> executeTimerTool(toolCall.arguments)
            "web_search" -> executeWebSearch(toolCall.arguments)
            else -> OrbitResult(
                success = false,
                action = "UNKNOWN_TOOL",
                spokenResponse = "I don't know how to handle the tool ${toolCall.tool}."
            )
        }
    }

    private suspend fun executeMusicTool(args: Map<String, Any?>): OrbitResult {
        val query = (args["query"] as? String) ?: (args["title"] as? String) ?: ""
        val artist = (args["artist"] as? String)?.takeIf { it.isNotBlank() }
        val isSimilar = (args["isSimilar"] as? Boolean) ?: false

        if (query.isBlank() && artist == null) {
            return OrbitResult(
                success = false,
                action = "PLAY_MUSIC",
                spokenResponse = "What song would you like me to play?"
            )
        }

        val fullQuery = when {
            isSimilar -> "$query similar mix"
            !artist.isNullOrBlank() -> "$query $artist"
            else -> query
        }

        val results = youTubeRepository.search(fullQuery, YouTubeRepository.FILTER_SONGS)
        if (results.isEmpty()) {
            return OrbitResult(
                success = false,
                action = "PLAY_MUSIC",
                spokenResponse = "I couldn't find \"$fullQuery\" on Sonza."
            )
        }

        val targetSong = results.first()
        withContext(Dispatchers.Main) {
            musicPlayer.playSong(targetSong, results, 0)
        }

        val spoken = if (!targetSong.artist.isNullOrBlank() && targetSong.artist != "<unknown>") {
            "Playing ${targetSong.title} by ${targetSong.artist}."
        } else {
            "Playing ${targetSong.title}."
        }

        return OrbitResult(
            success = true,
            action = "PLAY_MUSIC",
            spokenResponse = spoken,
            visualFeedback = "Playing ${targetSong.title}",
            actionIndicator = "▶️"
        )
    }

    private suspend fun executePlaybackControl(args: Map<String, Any?>): OrbitResult {
        val action = (args["action"] as? String)?.lowercase() ?: "pause"
        return when (action) {
            "pause", "stop" -> {
                withContext(Dispatchers.Main) { musicPlayer.pause() }
                OrbitResult(success = true, action = "PAUSE", spokenResponse = "Paused.", actionIndicator = "⏸️")
            }
            "resume", "play" -> {
                withContext(Dispatchers.Main) { musicPlayer.play() }
                OrbitResult(success = true, action = "RESUME", spokenResponse = "Resuming playback.", actionIndicator = "▶️")
            }
            "next", "skip" -> {
                withContext(Dispatchers.Main) { musicPlayer.seekToNext() }
                OrbitResult(success = true, action = "NEXT", spokenResponse = "Skipping track.", actionIndicator = "⏭️")
            }
            "previous", "back" -> {
                withContext(Dispatchers.Main) { musicPlayer.seekToPrevious() }
                OrbitResult(success = true, action = "PREVIOUS", spokenResponse = "Playing previous track.", actionIndicator = "⏮️")
            }
            "replay", "restart" -> {
                withContext(Dispatchers.Main) { musicPlayer.seekTo(0) }
                OrbitResult(success = true, action = "REPLAY", spokenResponse = "Replaying from start.", actionIndicator = "🔄")
            }
            else -> OrbitResult(success = false, action = "PLAYBACK_CONTROL", spokenResponse = "Unknown playback action.")
        }
    }

    private fun executeVolume(args: Map<String, Any?>): OrbitResult {
        val action = (args["action"] as? String)?.lowercase()
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
        if (audioManager == null) {
            return OrbitResult(success = false, action = "SET_VOLUME", spokenResponse = "Audio manager is unavailable.")
        }

        val direction = if (action == "up") android.media.AudioManager.ADJUST_RAISE else android.media.AudioManager.ADJUST_LOWER
        audioManager.adjustStreamVolume(android.media.AudioManager.STREAM_MUSIC, direction, android.media.AudioManager.FLAG_SHOW_UI)
        val text = if (action == "up") "Volume up." else "Volume down."
        return OrbitResult(success = true, action = "SET_VOLUME", spokenResponse = text, actionIndicator = "🔊")
    }

    private fun executeAppTool(args: Map<String, Any?>): OrbitResult {
        val appName = (args["appName"] as? String)?.trim() ?: ""
        if (appName.isBlank()) {
            return OrbitResult(success = false, action = "OPEN_APP", spokenResponse = "Which app would you like to open?")
        }

        val pm = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        var targetPackage: String? = null
        for (app in packages) {
            val label = pm.getApplicationLabel(app).toString()
            if (label.equals(appName, ignoreCase = true) || label.contains(appName, ignoreCase = true)) {
                targetPackage = app.packageName
                break
            }
        }

        if (targetPackage == null) {
            when (appName.lowercase()) {
                "settings" -> {
                    val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                    return OrbitResult(success = true, action = "OPEN_APP", spokenResponse = "Opening Settings.", actionIndicator = "⚙️")
                }
                "spotify" -> targetPackage = "com.spotify.music"
                "youtube" -> targetPackage = "com.google.android.youtube"
            }
        }

        if (targetPackage != null) {
            val launchIntent = pm.getLaunchIntentForPackage(targetPackage)
            if (launchIntent != null) {
                launchIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(launchIntent)
                return OrbitResult(success = true, action = "OPEN_APP", spokenResponse = "Opening $appName.", actionIndicator = "🚀")
            }
        }

        return OrbitResult(success = false, action = "OPEN_APP", spokenResponse = "I couldn't find an installed app named $appName.")
    }

    private fun executePhoneTool(args: Map<String, Any?>): OrbitResult {
        val contact = (args["contact"] as? String) ?: (args["name"] as? String) ?: ""
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            if (contact.matches(Regex("^[0-9+\\-#* ]+$"))) {
                data = Uri.parse("tel:${contact.replace(" ", "")}")
            }
        }
        return try {
            context.startActivity(dialIntent)
            OrbitResult(
                success = true,
                action = "CALL_PHONE",
                spokenResponse = if (contact.isNotBlank()) "Calling $contact." else "Opening phone dialer.",
                actionIndicator = "📞"
            )
        } catch (_: Exception) {
            OrbitResult(success = false, action = "CALL_PHONE", spokenResponse = "Could not open dialer.")
        }
    }

    private fun executeTimerTool(args: Map<String, Any?>): OrbitResult {
        val minutes = (args["minutes"] as? Number)?.toInt() ?: 15
        sleepTimerManager.startTimer(SleepTimerOption.CUSTOM, customMinutes = minutes)
        return OrbitResult(
            success = true,
            action = "SET_TIMER",
            spokenResponse = "Sleep timer set for $minutes minutes.",
            actionIndicator = "⏱️"
        )
    }

    private suspend fun executeWebSearch(args: Map<String, Any?>): OrbitResult {
        val query = (args["query"] as? String) ?: ""
        if (query.isBlank()) {
            return OrbitResult(success = false, action = "WEB_SEARCH", spokenResponse = "What would you like me to search for?")
        }

        val summary = webSearchTool.search(query)
        return OrbitResult(
            success = true,
            action = "WEB_SEARCH",
            spokenResponse = summary,
            actionIndicator = "🌐"
        )
    }
}
