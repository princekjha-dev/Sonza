package com.sonza.app.ai.orbit

import com.sonza.app.core.model.RepeatMode
import com.sonza.app.core.model.Song
import java.util.Locale

/**
 * Natural Language Intent Parser and Entity Extractor for Sonza Orbit.
 * Comprehensively parses all 93 intents + extra conversational intelligence,
 * resolves pronouns ("this", "it", "him", "her"), handles multi-turn follow-ups,
 * corrections, and disambiguation choices.
 */
object OrbitIntentParser {

    fun parse(
        rawInput: String,
        currentSong: Song? = null,
        dialogueContext: OrbitDialogueContext = OrbitDialogueContext.Idle,
        contextMemory: OrbitContextMemory? = null
    ): OrbitIntent {
        val trimmed = cleanInput(rawInput)

        // 1. Standalone Wake Word
        if (isWakeWordOnly(rawInput)) {
            return OrbitIntent.WakeWordOnlyIntent
        }

        if (trimmed.isBlank()) {
            return OrbitIntent.HelpIntent
        }

        // 2. Cancellation and dismissals (note: "stop" without arguments is handled as playback stop)
        if (isCancellation(trimmed)) {
            return OrbitIntent.CancelIntent
        }

        // 3. Conversational Corrections: "No", "Not that one", "I meant the other song", etc.
        if (isCorrection(trimmed)) {
            return OrbitIntent.CorrectionIntent(trimmed)
        }

        // 4. Dialogue Context Handling
        when (dialogueContext) {
            is OrbitDialogueContext.AwaitingConfirmation -> {
                if (trimmed in listOf("yes", "yeah", "yep", "sure", "confirm", "delete it", "do it", "go ahead")) {
                    return OrbitIntent.ConfirmationResponseIntent(confirmed = true)
                }
                if (trimmed in listOf("no", "nope", "cancel", "don't", "do not", "never mind", "stop")) {
                    return OrbitIntent.ConfirmationResponseIntent(confirmed = false)
                }
            }
            is OrbitDialogueContext.AwaitingDisambiguation -> {
                return OrbitIntent.DisambiguationChoiceIntent(choice = trimmed)
            }
            else -> Unit
        }

        // 5. Compound commands (e.g. "pause music and set sleep timer 30 minutes")
        val subCommands = splitCompoundCommand(trimmed)
        if (subCommands.size > 1) {
            val parsedList = subCommands.map { parseSingleIntent(it, currentSong, contextMemory) }
            val validIntents = parsedList.filter { it !is OrbitIntent.UnknownIntent }
            if (validIntents.isNotEmpty()) {
                return if (validIntents.size == 1) validIntents.first() else OrbitIntent.CompoundIntent(validIntents)
            }
        }

        return parseSingleIntent(trimmed, currentSong, contextMemory)
    }

    private fun isWakeWordOnly(input: String): Boolean {
        val s = input.trim().lowercase(Locale.ROOT)
        return s in listOf("hey orbit", "ok orbit", "okay orbit", "orbit", "hello orbit", "hi orbit", "hey", "aether")
    }

    private fun isCancellation(input: String): Boolean {
        return input in listOf(
            "cancel", "never mind", "nevermind", "forget it", "close", "dismiss",
            "goodbye", "bye", "that's all", "thats all", "exit", "shut down", "cancel that"
        )
    }

    private fun isCorrection(input: String): Boolean {
        val s = input.lowercase(Locale.ROOT)
        return s == "no" || s == "nope" || s.startsWith("no ") || s.startsWith("actually ") ||
                s.contains("not that one") || s.contains("not this one") || s.contains("wrong song") ||
                s.contains("meant the other") || s.contains("meant another") ||
                s.contains("thats not what i meant") || s.contains("that's not what i meant")
    }

    private fun cleanInput(input: String): String {
        var s = input.trim().lowercase(Locale.ROOT)
        s = s.removePrefix("hey orbit").removePrefix("ok orbit").removePrefix("okay orbit")
            .removePrefix("hello orbit").removePrefix("hi orbit").removePrefix("orbit").trim()
        s = s.removePrefix("please").removePrefix("can you").removePrefix("could you")
            .removePrefix("would you").removePrefix("tell me").removePrefix("i want to").trim()
        // Strip punctuation and apostrophes for uniform matching (e.g. "what's" -> "whats")
        s = s.replace(Regex("[?,!.'\"]"), "").trim()
        return s
    }

    private fun splitCompoundCommand(input: String): List<String> {
        val parts = input.split(Regex("\\b(and then|and also|and|then)\\b", RegexOption.IGNORE_CASE))
        return parts.map { it.trim() }.filter { it.length > 2 }
    }

    private fun parseSingleIntent(q: String, currentSong: Song?, contextMemory: OrbitContextMemory?): OrbitIntent {
        // --- 1. Playback Controls ---
        if (q in listOf("pause", "pause music", "pause playback", "hold on", "wait", "freeze", "stop the music", "hold it")) {
            return OrbitIntent.PauseIntent
        }
        if (q in listOf("resume", "play", "continue", "resume music", "keep playing", "unpause", "start playing")) {
            return OrbitIntent.ResumeIntent
        }
        if (q in listOf("stop", "stop music", "turn off music", "stop playback")) {
            return OrbitIntent.StopIntent
        }
        if (q in listOf("next", "skip", "skip it", "skip this", "skip song", "next song", "play another", "move on", "change the song")) {
            return OrbitIntent.NextTrackIntent
        }
        if (q in listOf("previous", "go back", "last song", "backtrack", "previous song")) {
            return OrbitIntent.PreviousTrackIntent
        }
        if (q in listOf("replay", "restart", "start over", "play from beginning", "from start")) {
            return OrbitIntent.ReplayIntent
        }

        // Seeking
        val seekFwdMatch = Regex("(?:fast forward|jump forward|skip forward|seek forward)\\s*(\\d+)?\\s*(?:seconds|s)?").find(q)
        if (seekFwdMatch != null) {
            val secs = seekFwdMatch.groupValues.getOrNull(1)?.toIntOrNull() ?: 15
            return OrbitIntent.SeekIntent(seconds = secs, isForward = true)
        }
        val seekBackMatch = Regex("(?:rewind|jump back|skip back|seek back)\\s*(\\d+)?\\s*(?:seconds|s)?").find(q)
        if (seekBackMatch != null) {
            val secs = seekBackMatch.groupValues.getOrNull(1)?.toIntOrNull() ?: 15
            return OrbitIntent.SeekIntent(seconds = secs, isForward = false)
        }

        // Speed
        if (q.contains("speed") || q.contains("play faster") || q.contains("slow down")) {
            val speed = when {
                q.contains("15") || q.contains("faster") -> 1.5f
                q.contains("125") -> 1.25f
                q.contains("2") -> 2.0f
                q.contains("075") || q.contains("slower") -> 0.75f
                else -> 1.0f
            }
            return OrbitIntent.SetSpeedIntent(speed)
        }

        // --- 2. Volume & Audio Quality ---
        if (q in listOf("make it louder", "turn it up", "volume up", "little louder", "louder", "increase volume")) {
            return OrbitIntent.VolumeIntent(VolumeAction.UP)
        }
        if (q in listOf("make it softer", "turn it down", "volume down", "little softer", "quieter", "lower volume", "decrease volume")) {
            return OrbitIntent.VolumeIntent(VolumeAction.DOWN)
        }
        val volSetMatch = Regex("(?:set volume to|volume)\\s*(\\d+)\\s*%?").find(q)
        if (volSetMatch != null) {
            val lvl = volSetMatch.groupValues[1].toIntOrNull()
            if (lvl != null) {
                return OrbitIntent.VolumeIntent(VolumeAction.SET_LEVEL, levelPercent = lvl)
            }
        }
        if (q in listOf("mute", "silence", "mute volume", "turn off sound")) {
            return OrbitIntent.MuteIntent(true)
        }
        if (q in listOf("unmute", "sound on", "turn on sound")) {
            return OrbitIntent.MuteIntent(false)
        }

        // --- 3. Shuffle & Repeat ---
        if (q in listOf("shuffle", "shuffle on", "shuffle playlist", "turn on shuffle", "randomize")) {
            return OrbitIntent.ShuffleIntent(enable = true)
        }
        if (q in listOf("shuffle off", "turn off shuffle", "stop shuffle")) {
            return OrbitIntent.ShuffleIntent(enable = false)
        }
        if (q in listOf("repeat this song", "loop this song", "repeat one", "loop once")) {
            return OrbitIntent.RepeatIntent(RepeatMode.ONE)
        }
        if (q in listOf("repeat", "repeat on", "repeat all", "loop", "loop playlist")) {
            return OrbitIntent.RepeatIntent(RepeatMode.ALL)
        }
        if (q in listOf("repeat off", "turn off repeat", "stop repeat", "stop looping")) {
            return OrbitIntent.RepeatIntent(RepeatMode.OFF)
        }

        // --- 4. Questions & Song Metadata (Section 17) ---
        if (q in listOf("who is singing", "who is singing this", "who sings this", "who sang this", "what artist am i listening to", "singer of this song", "who is the singer")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.SINGER)
        }
        if (q in listOf("who composed this", "who is the composer", "who is the music director", "composer of this song")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.COMPOSER)
        }
        if (q in listOf("who wrote this", "who is the writer", "who is the lyricist", "who wrote the lyrics", "lyricist of this song")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.WRITER)
        }
        if (q in listOf("when was this released", "what year was this", "what year was this released", "release date", "when did this come out")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.RELEASE_DATE)
        }
        if (q in listOf("which album is this from", "what album is this", "album of this song", "which movie is this from")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.ALBUM)
        }
        if (q in listOf("whats playing", "what is playing", "what song is this", "what song is playing", "what am i listening to", "tell me about this song", "song info", "details of this song")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.DETAILS)
        }
        if (q in listOf("whats next", "what is next", "what song is next", "whats the next song", "what is the next song", "whats coming next", "what is coming next", "next song info")) {
            return OrbitIntent.QueueNextInfoIntent
        }
        if (q in listOf("is there an acoustic version", "show versions", "other versions", "what versions exist")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.VERSIONS)
        }

        // --- 5. Favorites & Library ---
        if (q in listOf("like this song", "add to favorites", "add it to my favorites", "add to liked songs", "like this", "i love this song", "i like this")) {
            return OrbitIntent.FavoriteIntent(isFavorite = true)
        }
        if (q in listOf("dislike", "dislike this song", "i hate this", "dont like this", "never play this again", "remove this from my recommendations", "dont recommend this")) {
            return OrbitIntent.DislikeSongIntent
        }
        if (q in listOf("save to library", "save this song", "add to library")) {
            return OrbitIntent.SaveToLibraryIntent
        }
        if (q in listOf("play my favorites", "play favorites", "play liked songs", "play my liked songs")) {
            return OrbitIntent.PlayFavoritesIntent
        }

        // Playlists
        val createPlaylistMatch = Regex("(?:create playlist|make a playlist called|new playlist)\\s*(.+)").find(q)
        if (createPlaylistMatch != null) {
            val name = createPlaylistMatch.groupValues[1].trim()
            return OrbitIntent.CreatePlaylistIntent(name = name)
        }
        val addToPlaylistMatch = Regex("(?:add to playlist|put in playlist|add to my playlist)\\s*(.+)").find(q)
        if (addToPlaylistMatch != null) {
            val pName = addToPlaylistMatch.groupValues[1].trim()
            return OrbitIntent.AddToPlaylistIntent(playlistName = pName)
        }
        val deletePlaylistMatch = Regex("(?:delete playlist|remove playlist)\\s*(.+)").find(q)
        if (deletePlaylistMatch != null) {
            val pName = deletePlaylistMatch.groupValues[1].trim()
            return OrbitIntent.DeletePlaylistIntent(playlistName = pName)
        }
        if (q in listOf("show playlists", "my playlists", "open playlists")) {
            return OrbitIntent.ShowPlaylistsIntent
        }

        // --- 6. Queue ---
        if (q.startsWith("queue next") || q.startsWith("play next") || q.contains("queue this next")) {
            val track = q.removePrefix("queue next").removePrefix("play next").removePrefix("queue this next").trim()
            return OrbitIntent.QueueAddIntent(trackName = track.ifBlank { null }, playNext = true)
        }
        if (q.startsWith("add to queue") || q.startsWith("queue")) {
            val track = q.removePrefix("add to queue").removePrefix("queue").trim()
            return OrbitIntent.QueueAddIntent(trackName = track.ifBlank { null }, playNext = false)
        }
        if (q in listOf("clear the queue", "clear queue", "empty queue")) {
            return OrbitIntent.QueueClearIntent
        }
        if (q in listOf("show queue", "show the queue", "view queue", "whats in the queue", "open queue")) {
            return OrbitIntent.QueueShowIntent
        }

        // --- 7. Lyrics ---
        if (q in listOf("lyrics", "show lyrics", "open lyrics", "what are the lyrics", "view lyrics")) {
            return OrbitIntent.LyricsIntent(show = true)
        }
        if (q in listOf("hide lyrics", "close lyrics", "dismiss lyrics")) {
            return OrbitIntent.LyricsIntent(show = false)
        }

        // --- 8. Timers ---
        val sleepTimerMatch = Regex("(?:set a sleep timer for|sleep timer|stop music after)\\s*(\\d+)\\s*(?:minutes|mins|m)?").find(q)
        if (sleepTimerMatch != null) {
            val mins = sleepTimerMatch.groupValues[1].toIntOrNull() ?: 30
            return OrbitIntent.SleepTimerIntent(minutes = mins)
        }
        if (q.contains("stop music after this song") || q.contains("stop after this song")) {
            return OrbitIntent.SleepTimerIntent(minutes = null, stopAfterCurrent = true)
        }
        if (q in listOf("cancel sleep timer", "turn off sleep timer", "cancel timer", "stop timer")) {
            return OrbitIntent.SleepTimerIntent(minutes = null, cancel = true)
        }

        // --- 9. Extra Intelligence & Discovery (Section 15) ---
        if (q in listOf("play something i havent heard", "discover music", "unheard songs")) {
            return OrbitIntent.PlayUnheardIntent
        }
        if (q in listOf("continue what i was listening to yesterday", "resume yesterday", "continue listening")) {
            return OrbitIntent.ContinueListeningIntent
        }
        if (q in listOf("play my most played songs", "most played songs", "my top songs")) {
            return OrbitIntent.PlayMostPlayedIntent
        }
        if (q in listOf("play something similar to this", "songs like this", "play songs like this", "similar to this", "more like this")) {
            return OrbitIntent.SimilarMusicIntent
        }
        if (q in listOf("surprise me", "play something random", "play random songs")) {
            return OrbitIntent.SurpriseMeIntent
        }
        if (q in listOf("play trending songs", "whats trending", "trending music", "top charts")) {
            return OrbitIntent.TrendingIntent
        }
        if (q in listOf("new releases", "latest songs", "play new music", "new music")) {
            return OrbitIntent.NewReleasesIntent
        }
        if (q.contains("turn on offline mode") || q.contains("play only downloaded music") || q.contains("offline mode")) {
            return OrbitIntent.OfflineModeIntent(enabled = true)
        }
        if (q in listOf("storage info", "how much music is downloaded", "download storage")) {
            return OrbitIntent.StorageInfoIntent
        }
        if (q in listOf("show my listening stats", "show listening stats", "what have i listened to today", "listening stats")) {
            return OrbitIntent.ListeningStatsIntent
        }

        // Activities & Moods
        if (q.contains("study playlist") || q.contains("study music") || q == "study") {
            return OrbitIntent.PlayActivityIntent("study")
        }
        if (q.contains("workout playlist") || q.contains("workout music") || q == "workout") {
            return OrbitIntent.PlayActivityIntent("workout")
        }
        if (q.contains("road trip") || q.contains("driving music")) {
            return OrbitIntent.PlayActivityIntent("road trip")
        }
        val moodKeywords = listOf("romantic", "sad", "chill", "relaxing", "energetic", "party", "happy", "calm", "lofi")
        for (mood in moodKeywords) {
            if (q.contains(mood)) {
                val artist = contextMemory?.currentArtist ?: currentSong?.artist
                return if (artist != null && (q.contains("another") || q == mood || q == "something $mood" || q == "play something $mood")) {
                    OrbitIntent.PlayArtistIntent(artistName = artist, moodOrGenre = mood)
                } else {
                    OrbitIntent.PlayGenreOrMoodIntent(mood)
                }
            }
        }

        // Conversational Follow-up Pronouns ("another one", "play another one by him")
        if (q in listOf("another one", "play another one", "one more", "play next one")) {
            val artist = contextMemory?.currentArtist ?: currentSong?.artist
            return if (artist != null) {
                OrbitIntent.PlayArtistIntent(artistName = artist)
            } else {
                OrbitIntent.NextTrackIntent
            }
        }
        if (q.contains("by him") || q.contains("by her") || q.contains("by them") || q.contains("by this artist")) {
            val artist = contextMemory?.currentArtist ?: currentSong?.artist
            if (artist != null) {
                return OrbitIntent.PlayArtistIntent(artistName = artist)
            }
        }

        // Versions: "play the acoustic version", "play the remix", "play the live version"
        val versionMatch = Regex("(?:play the|play)\\s*(acoustic|remix|live|instrumental|original|slowed|reverb)\\s*(?:version of)?\\s*(.*)").find(q)
        if (versionMatch != null) {
            val ver = versionMatch.groupValues[1]
            var track = versionMatch.groupValues[2].trim()
            if (track.isBlank() || track in listOf("version", "this", "it")) {
                track = contextMemory?.currentSong?.title ?: currentSong?.title ?: ""
            }
            if (track.isNotBlank()) {
                return OrbitIntent.PlayVersionIntent(trackName = track, version = ver)
            }
        }

        // --- 10. Downloads ---
        if (q in listOf("download this song", "download song", "download this")) {
            return OrbitIntent.DownloadIntent(DownloadAction.CURRENT_SONG)
        }
        if (q in listOf("download this album", "download album")) {
            return OrbitIntent.DownloadIntent(DownloadAction.CURRENT_ALBUM)
        }
        if (q in listOf("download this playlist", "download playlist")) {
            return OrbitIntent.DownloadIntent(DownloadAction.CURRENT_PLAYLIST)
        }

        // --- 11. Navigation ---
        val navScreen = when {
            q.contains("library") -> "library"
            q.contains("search") -> "search"
            q.contains("download") -> "downloads"
            q.contains("history") || q.contains("recents") -> "history"
            q.contains("settings") -> "settings"
            q.contains("profile") -> "profile"
            q.contains("home") -> "home"
            else -> null
        }
        if (navScreen != null && (q.startsWith("open") || q.startsWith("go to") || q.startsWith("show") || q.startsWith("navigate to"))) {
            return OrbitIntent.NavigationIntent(navScreen)
        }
        if (q in listOf("show the player", "show player", "open player", "expand player")) {
            return OrbitIntent.PlayerUiIntent(expand = true)
        }
        if (q in listOf("hide the player", "hide player", "close player", "minimize player")) {
            return OrbitIntent.PlayerUiIntent(expand = false)
        }

        // --- 12. Help & Small Talk ---
        if (q in listOf("help", "what can you do", "help me", "commands", "who are you")) {
            return OrbitIntent.HelpIntent
        }
        if (q in listOf("how are you", "tell me a joke", "thank you", "thanks", "good morning", "good evening")) {
            return OrbitIntent.SmallTalkIntent(q)
        }

        // --- 13. General Music Play Matching ---
        val playArtistMatch = Regex("(?:play songs by|play songs of|play artist|songs by)\\s*(.+)").find(q)
        if (playArtistMatch != null) {
            val artist = playArtistMatch.groupValues[1].trim()
            return OrbitIntent.PlayArtistIntent(artistName = artist)
        }

        val playAlbumMatch = Regex("(?:play album|album)\\s*(.+)").find(q)
        if (playAlbumMatch != null) {
            val album = playAlbumMatch.groupValues[1].trim()
            return OrbitIntent.PlayAlbumIntent(albumName = album)
        }

        val playPlaylistMatch = Regex("(?:play playlist|playlist)\\s*(.+)").find(q)
        if (playPlaylistMatch != null) {
            val playlist = playPlaylistMatch.groupValues[1].trim()
            return OrbitIntent.PlayPlaylistIntent(playlistName = playlist)
        }

        // Direct Play Track (e.g. "play Tumhein Mohabbat Hai", "play Kesariya by Arijit Singh")
        if (q.startsWith("play")) {
            val query = q.removePrefix("play").trim()
            if (query.isNotBlank()) {
                val byParts = query.split(Regex("\\bby\\b", RegexOption.IGNORE_CASE))
                if (byParts.size == 2) {
                    return OrbitIntent.PlayTrackIntent(trackName = byParts[0].trim(), artistName = byParts[1].trim())
                }

                // If query matches current artist or known artist context:
                val knownArtist = contextMemory?.currentArtist ?: currentSong?.artist
                if (knownArtist != null && query.equals(knownArtist, ignoreCase = true)) {
                    return OrbitIntent.PlayArtistIntent(artistName = knownArtist)
                }

                return OrbitIntent.PlayTrackIntent(trackName = query)
            }
        }

        val bySplit = q.split(Regex("\\bby\\b", RegexOption.IGNORE_CASE))
        if (bySplit.size == 2) {
            return OrbitIntent.PlayTrackIntent(trackName = bySplit[0].trim(), artistName = bySplit[1].trim())
        }

        if (q.length > 2) {
            return OrbitIntent.PlayTrackIntent(trackName = q)
        }

        return OrbitIntent.UnknownIntent(q)
    }
}
