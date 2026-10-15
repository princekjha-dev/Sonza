package com.sonza.app.ai.orbit

import com.sonza.app.core.model.RepeatMode
import com.sonza.app.core.model.Song
import java.util.Locale
import java.util.regex.Pattern

/**
 * Robust Natural Language Intent Parser and Entity Extractor for Orbit AI.
 * Handles contextual references ("this", "it", "current song"), compound commands,
 * and conversational phrasing.
 */
object OrbitIntentParser {

    /**
     * Parses a user utterance into an [OrbitIntent].
     */
    fun parse(rawInput: String, currentSong: Song? = null): OrbitIntent {
        val trimmed = cleanInput(rawInput)
        if (trimmed.isBlank()) return OrbitIntent.HelpIntent

        // Check for compound command (e.g., "play Kesariya and add it to favorites")
        val subCommands = splitCompoundCommand(trimmed)
        if (subCommands.size > 1) {
            val parsedList = subCommands.map { parseSingleIntent(it, currentSong) }
            // Filter out unknown intents if valid intents exist
            val validIntents = parsedList.filter { it !is OrbitIntent.UnknownIntent }
            if (validIntents.isNotEmpty()) {
                return if (validIntents.size == 1) validIntents.first() else OrbitIntent.CompoundIntent(validIntents)
            }
        }

        return parseSingleIntent(trimmed, currentSong)
    }

    private fun cleanInput(input: String): String {
        var s = input.trim().lowercase(Locale.ROOT)
        // Strip wake words and pleasantries
        s = s.removePrefix("hey orbit").removePrefix("ok orbit").removePrefix("orbit")
            .removePrefix("hello orbit").removePrefix("hi orbit").trim()
        s = s.removePrefix("please").removePrefix("can you").removePrefix("could you")
            .removePrefix("would you").trim()
        return s
    }

    private fun splitCompoundCommand(input: String): List<String> {
        val parts = input.split(Regex("\\b(and then|and also|and|then)\\b", RegexOption.IGNORE_CASE))
        return parts.map { it.trim() }.filter { it.length > 2 }
    }

    private fun parseSingleIntent(q: String, currentSong: Song?): OrbitIntent {
        // --- 1. Playback Controls ---
        if (q in listOf("pause", "pause music", "pause playback", "hold on", "wait", "freeze")) {
            return OrbitIntent.PauseIntent
        }
        if (q in listOf("resume", "play", "continue", "resume music", "keep playing", "unpause")) {
            return OrbitIntent.ResumeIntent
        }
        if (q in listOf("stop", "stop music", "shut up", "turn off music", "stop playback")) {
            return OrbitIntent.StopIntent
        }
        if (q in listOf("next", "next song", "next track", "skip", "skip song", "play next")) {
            return OrbitIntent.NextTrackIntent
        }
        if (q in listOf("previous", "previous song", "previous track", "back", "go back", "last song")) {
            return OrbitIntent.PreviousTrackIntent
        }
        if (q in listOf("replay", "replay song", "replay this", "restart", "restart song", "start over", "play again")) {
            return OrbitIntent.ReplayIntent
        }

        // --- 2. Seek / Skip Forward / Backward ---
        val seekForwardMatch = Pattern.compile("(?:skip|forward|jump|fast forward)\\s+(\\d+)\\s*(?:s|sec|seconds?)?").matcher(q)
        if (seekForwardMatch.find()) {
            val seconds = seekForwardMatch.group(1)?.toIntOrNull() ?: 15
            return OrbitIntent.SeekIntent(seconds, isForward = true)
        }
        val seekBackwardMatch = Pattern.compile("(?:rewind|go back|back|skip back)\\s+(\\d+)\\s*(?:s|sec|seconds?)?").matcher(q)
        if (seekBackwardMatch.find()) {
            val seconds = seekBackwardMatch.group(1)?.toIntOrNull() ?: 15
            return OrbitIntent.SeekIntent(seconds, isForward = false)
        }

        // --- 3. Shuffle & Repeat ---
        if (q in listOf("shuffle on", "turn on shuffle", "enable shuffle", "shuffle this", "shuffle songs")) {
            return OrbitIntent.ShuffleIntent(enable = true)
        }
        if (q in listOf("shuffle off", "turn off shuffle", "disable shuffle", "no shuffle")) {
            return OrbitIntent.ShuffleIntent(enable = false)
        }
        if (q in listOf("repeat on", "turn on repeat", "repeat playlist", "repeat all")) {
            return OrbitIntent.RepeatIntent(RepeatMode.ALL)
        }
        if (q in listOf("repeat this song", "repeat current song", "repeat one", "loop this", "loop song")) {
            return OrbitIntent.RepeatIntent(RepeatMode.ONE)
        }
        if (q in listOf("repeat off", "turn off repeat", "disable repeat", "no repeat")) {
            return OrbitIntent.RepeatIntent(RepeatMode.OFF)
        }

        // --- 4. Volume Controls ---
        if (q.contains("volume up") || q.contains("louder") || q.contains("turn it up") || q.contains("increase volume")) {
            return OrbitIntent.VolumeIntent(VolumeAction.UP)
        }
        if (q.contains("volume down") || q.contains("quieter") || q.contains("turn it down") || q.contains("decrease volume") || q.contains("lower volume")) {
            return OrbitIntent.VolumeIntent(VolumeAction.DOWN)
        }
        val volumeLevelMatch = Pattern.compile("volume\\s*(?:to|at)?\\s*(\\d+)\\s*%?").matcher(q)
        if (volumeLevelMatch.find()) {
            val percent = volumeLevelMatch.group(1)?.toIntOrNull()
            if (percent != null) return OrbitIntent.VolumeIntent(VolumeAction.SET_LEVEL, percent.coerceIn(0, 100))
        }

        // --- 5. Favorites & Liked Songs ---
        if (q in listOf("play my liked songs", "play liked songs", "play favorites", "play my favorites", "play liked music")) {
            return OrbitIntent.PlayFavoritesIntent
        }
        if (q.contains("add to favorites") || q.contains("like this song") || q.contains("favorite this") || q.contains("like it") || q.contains("i love this")) {
            return OrbitIntent.FavoriteIntent(isFavorite = true)
        }
        if (q.contains("remove from favorites") || q.contains("unlike this") || q.contains("unfavorite") || q.contains("dislike this")) {
            return OrbitIntent.FavoriteIntent(isFavorite = false)
        }

        // --- 6. Sleep Timer ---
        if (q.contains("cancel sleep timer") || q.contains("turn off sleep timer") || q.contains("stop sleep timer")) {
            return OrbitIntent.SleepTimerIntent(minutes = null, cancel = true)
        }
        val sleepTimerMatch = Pattern.compile("(?:sleep timer|turn off|stop)(?:\\s*(?:in|for))?\\s*(\\d+)\\s*(minutes?|mins?|hours?|hrs?)").matcher(q)
        if (sleepTimerMatch.find()) {
            val amount = sleepTimerMatch.group(1)?.toIntOrNull() ?: 30
            val unit = sleepTimerMatch.group(2) ?: "minutes"
            val totalMinutes = if (unit.startsWith("hour") || unit.startsWith("hr")) amount * 60 else amount
            return OrbitIntent.SleepTimerIntent(minutes = totalMinutes, cancel = false)
        }

        // --- 7. Lyrics ---
        if (q.contains("show lyrics") || q.contains("open lyrics") || q.contains("lyrics for this song") || q.contains("what are the lyrics") || q == "lyrics") {
            return OrbitIntent.LyricsIntent(show = true)
        }
        if (q.contains("hide lyrics") || q.contains("close lyrics")) {
            return OrbitIntent.LyricsIntent(show = false)
        }

        // --- 8. Song Information (Contextual) ---
        if (q.contains("who sings this") || q.contains("who is the artist") || q.contains("who is singing")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.ARTIST)
        }
        if (q.contains("what song is this") || q.contains("what's playing") || q.contains("what is playing") || q.contains("song name")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.TITLE)
        }
        if (q.contains("what album") || q.contains("which album")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.ALBUM)
        }
        if (q.contains("what year") || q.contains("release date") || q.contains("when was this released")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.YEAR)
        }
        if (q.contains("how long is this") || q.contains("duration")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.DURATION)
        }

        // --- 9. Queue Management ---
        if (q.contains("play this next") || q.contains("play next")) {
            return OrbitIntent.QueueAddIntent(playNext = true)
        }
        if (q.contains("add to queue") || q.contains("add this to the queue") || q.contains("queue this")) {
            return OrbitIntent.QueueAddIntent(playNext = false)
        }
        if (q.contains("show queue") || q.contains("what's next in queue") || q.contains("open queue")) {
            return OrbitIntent.QueueShowIntent
        }
        if (q.contains("clear queue") || q.contains("empty queue")) {
            return OrbitIntent.QueueClearIntent
        }

        // --- 10. Downloads ---
        if (q.contains("download this song") || q.contains("download song") || q.contains("download this")) {
            return OrbitIntent.DownloadIntent(DownloadAction.CURRENT_SONG)
        }
        if (q.contains("download album") || q.contains("download this album")) {
            return OrbitIntent.DownloadIntent(DownloadAction.CURRENT_ALBUM)
        }
        if (q.contains("show downloads") || q.contains("my downloads") || q.contains("open downloads")) {
            return OrbitIntent.DownloadIntent(DownloadAction.SHOW_DOWNLOADS)
        }

        // --- 11. Discovery & Curation ---
        if (q.contains("play something similar") || q.contains("play more like this") || q.contains("similar songs") || q.contains("songs like this")) {
            return OrbitIntent.SimilarMusicIntent
        }
        if (q.contains("play trending") || q.contains("trending songs") || q.contains("what is trending")) {
            return OrbitIntent.TrendingIntent
        }
        if (q.contains("play new releases") || q.contains("new songs") || q.contains("latest releases")) {
            return OrbitIntent.NewReleasesIntent
        }
        if (q.contains("surprise me") || q.contains("play anything") || q.contains("play random music")) {
            return OrbitIntent.SurpriseMeIntent
        }

        // Moods & Genres
        val moodKeywords = listOf(
            "romantic", "chill", "relaxing", "workout", "gym", "energetic",
            "study", "focus", "party", "sleep", "sad", "happy", "road trip",
            "punjabi", "hindi", "bollywood", "tamil", "telugu", "english", "lofi", "rock", "pop"
        )
        for (mood in moodKeywords) {
            if (q.contains(mood)) {
                return OrbitIntent.PlayGenreOrMoodIntent(mood)
            }
        }

        // --- 12. Playlist Operations ---
        val addToPlaylistMatch = Pattern.compile("(?:add this to|add to)\\s+(?:my\\s+)?playlist\\s+(.+)").matcher(q)
        if (addToPlaylistMatch.find()) {
            val playlistName = addToPlaylistMatch.group(1)?.trim().orEmpty()
            return OrbitIntent.AddToPlaylistIntent(playlistName = playlistName)
        }

        val createPlaylistMatch = Pattern.compile("create\\s+(?:a\\s+)?(?:new\\s+)?playlist\\s+(?:called\\s+|named\\s+)?(.+)").matcher(q)
        if (createPlaylistMatch.find()) {
            val playlistName = createPlaylistMatch.group(1)?.trim().orEmpty()
            return OrbitIntent.CreatePlaylistIntent(name = playlistName, addCurrentTrack = q.contains("add this"))
        }

        if (q.contains("show my playlists") || q.contains("my playlists") || q.contains("open playlists")) {
            return OrbitIntent.ShowPlaylistsIntent
        }

        // --- 13. Play Artist / Track / General Search ---
        val playArtistMatch = Pattern.compile("play\\s+(?:songs\\s+by|music\\s+by|artist)\\s+(.+)").matcher(q)
        if (playArtistMatch.find()) {
            val artist = playArtistMatch.group(1)?.trim().orEmpty()
            return OrbitIntent.PlayArtistIntent(artist)
        }

        if (q.startsWith("play ")) {
            val target = q.removePrefix("play ").trim()
            if (target.isNotBlank()) {
                return OrbitIntent.PlayTrackIntent(trackName = target)
            }
        }

        // --- 14. Navigation ---
        if (q.contains("open search") || q.contains("go to search")) return OrbitIntent.NavigationIntent("search")
        if (q.contains("open library") || q.contains("go to library")) return OrbitIntent.NavigationIntent("library")
        if (q.contains("open home") || q.contains("go to home")) return OrbitIntent.NavigationIntent("home")
        if (q.contains("open settings")) return OrbitIntent.NavigationIntent("settings")

        // Fallback: If utterance starts with "listen" or sounds like a song request
        if (q.isNotBlank()) {
            return OrbitIntent.PlayTrackIntent(trackName = q)
        }

        return OrbitIntent.UnknownIntent(q)
    }
}
