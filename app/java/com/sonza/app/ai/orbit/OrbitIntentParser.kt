package com.sonza.app.ai.orbit

import com.sonza.app.core.model.RepeatMode
import com.sonza.app.core.model.Song
import java.util.Locale
import java.util.regex.Pattern

/**
 * Robust Natural Language Intent Parser and Entity Extractor for Orbit.
 * Handles conversational turns, dialogue context, entity disambiguation, pronouns ("this", "it"),
 * and natural music commands.
 */
object OrbitIntentParser {

    /**
     * Parses a user utterance into an [OrbitIntent] considering dialogue context and current playback.
     */
    fun parse(
        rawInput: String,
        currentSong: Song? = null,
        dialogueContext: OrbitDialogueContext = OrbitDialogueContext.Idle
    ): OrbitIntent {
        val trimmed = cleanInput(rawInput)

        // 1. Check for standalone wake word
        if (isWakeWordOnly(rawInput)) {
            return OrbitIntent.WakeWordOnlyIntent
        }

        if (trimmed.isBlank()) {
            return OrbitIntent.HelpIntent
        }

        // 2. Cancellation and dismissals
        if (isCancellation(trimmed)) {
            return OrbitIntent.CancelIntent
        }

        // 3. Dialogue Context Handling
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
                val artist = dialogueContext.artist
                // Check if user selected a mood (e.g. "romantic", "sad", "chill")
                val moodKeywords = listOf("romantic", "sad", "relaxing", "chill", "energetic", "workout", "happy", "party")
                for (mood in moodKeywords) {
                    if (trimmed.contains(mood)) {
                        return if (artist != null) {
                            OrbitIntent.PlayArtistIntent(artistName = artist, moodOrGenre = mood)
                        } else {
                            OrbitIntent.PlayGenreOrMoodIntent(mood)
                        }
                    }
                }
                // Check ordinal choices: "first", "second", "third", or specific song name
                return OrbitIntent.DisambiguationChoiceIntent(choice = trimmed)
            }
            else -> Unit
        }

        // 4. Check for compound commands (e.g., "pause music and set sleep timer 30 minutes")
        val subCommands = splitCompoundCommand(trimmed)
        if (subCommands.size > 1) {
            val parsedList = subCommands.map { parseSingleIntent(it, currentSong) }
            val validIntents = parsedList.filter { it !is OrbitIntent.UnknownIntent }
            if (validIntents.isNotEmpty()) {
                return if (validIntents.size == 1) validIntents.first() else OrbitIntent.CompoundIntent(validIntents)
            }
        }

        return parseSingleIntent(trimmed, currentSong)
    }

    private fun isWakeWordOnly(input: String): Boolean {
        val s = input.trim().lowercase(Locale.ROOT)
        return s in listOf("hey orbit", "ok orbit", "okay orbit", "orbit", "hello orbit", "hi orbit")
    }

    private fun isCancellation(input: String): Boolean {
        return input in listOf(
            "cancel", "stop", "never mind", "nevermind", "close", "dismiss",
            "goodbye", "bye", "that's all", "thats all", "exit", "shut down"
        )
    }

    private fun cleanInput(input: String): String {
        var s = input.trim().lowercase(Locale.ROOT)
        // Strip wake words
        s = s.removePrefix("hey orbit").removePrefix("ok orbit").removePrefix("okay orbit")
            .removePrefix("hello orbit").removePrefix("hi orbit").removePrefix("orbit").trim()
        // Strip common courtesies
        s = s.removePrefix("please").removePrefix("can you").removePrefix("could you")
            .removePrefix("would you").removePrefix("tell me").removePrefix("i want to").trim()
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
        if (q in listOf("stop", "stop music", "turn off music", "stop playback")) {
            return OrbitIntent.StopIntent
        }
        if (q in listOf("next", "next song", "next track", "skip", "skip song", "play next")) {
            return OrbitIntent.NextTrackIntent
        }
        if (q in listOf("previous", "previous song", "previous track", "back", "go back", "last song", "play previous song", "play the previous song")) {
            return OrbitIntent.PreviousTrackIntent
        }
        if (q in listOf("replay", "replay song", "replay this", "restart", "restart song", "start over", "play again")) {
            return OrbitIntent.ReplayIntent
        }

        // --- 2. Seek / Skip Forward / Backward ---
        val seekForwardMatch = Pattern.compile("(?:skip|forward|jump|fast forward)\\s*(\\d+)?\\s*(?:s|sec|seconds?)?").matcher(q)
        if (seekForwardMatch.find() && (q.contains("skip") || q.contains("forward"))) {
            val seconds = seekForwardMatch.group(1)?.toIntOrNull() ?: 15
            return OrbitIntent.SeekIntent(seconds, isForward = true)
        }
        val seekBackwardMatch = Pattern.compile("(?:rewind|go back|back|skip back)\\s*(\\d+)?\\s*(?:s|sec|seconds?)?").matcher(q)
        if (seekBackwardMatch.find() && (q.contains("rewind") || q.contains("back"))) {
            val seconds = seekBackwardMatch.group(1)?.toIntOrNull() ?: 15
            return OrbitIntent.SeekIntent(seconds, isForward = false)
        }

        // --- 3. Shuffle & Repeat ---
        if (q in listOf("shuffle on", "turn on shuffle", "enable shuffle", "shuffle this", "shuffle songs", "turn shuffle on")) {
            return OrbitIntent.ShuffleIntent(enable = true)
        }
        if (q in listOf("shuffle off", "turn off shuffle", "disable shuffle", "no shuffle", "turn shuffle off")) {
            return OrbitIntent.ShuffleIntent(enable = false)
        }
        if (q in listOf("repeat on", "turn on repeat", "repeat playlist", "repeat all", "turn repeat on")) {
            return OrbitIntent.RepeatIntent(RepeatMode.ALL)
        }
        if (q in listOf("repeat this song", "repeat current song", "repeat one", "loop this", "loop song", "repeat this")) {
            return OrbitIntent.RepeatIntent(RepeatMode.ONE)
        }
        if (q in listOf("repeat off", "turn off repeat", "disable repeat", "no repeat", "turn repeat off")) {
            return OrbitIntent.RepeatIntent(RepeatMode.OFF)
        }

        // --- 4. Volume & Mute ---
        if (q in listOf("mute", "mute music", "silence music")) {
            return OrbitIntent.MuteIntent(mute = true)
        }
        if (q in listOf("unmute", "unmute music")) {
            return OrbitIntent.MuteIntent(mute = false)
        }
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
        if (q.contains("add to favorites") || q.contains("add this song to my favorites") || q.contains("add this to my favorites") ||
            q.contains("like this song") || q.contains("favorite this") || q.contains("like it") || q.contains("i love this")
        ) {
            return OrbitIntent.FavoriteIntent(isFavorite = true)
        }
        if (q.contains("remove from favorites") || q.contains("remove this song from my favorites") ||
            q.contains("unlike this") || q.contains("unfavorite") || q.contains("dislike this")
        ) {
            return OrbitIntent.FavoriteIntent(isFavorite = false)
        }

        // --- 6. Sleep Timer ---
        if (q.contains("cancel sleep timer") || q.contains("turn off sleep timer") || q.contains("stop sleep timer")) {
            return OrbitIntent.SleepTimerIntent(minutes = null, cancel = true)
        }
        val sleepTimerMatch = Pattern.compile("(?:sleep timer|turn off music|stop music)(?:\\s*(?:in|for))?\\s*(\\d+)\\s*(minutes?|mins?|hours?|hrs?)").matcher(q)
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
        if (q.contains("who sings this song") || q.contains("who sings this") || q.contains("who is the artist") || q.contains("who is singing")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.ARTIST)
        }
        if (q.contains("what song is this") || q.contains("what's playing") || q.contains("what is playing") || q.contains("song name") || q.contains("what's currently playing")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.TITLE)
        }
        if (q.contains("what album is this from") || q.contains("what album") || q.contains("which album")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.ALBUM)
        }
        if (q.contains("when was this released") || q.contains("what year") || q.contains("release date")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.YEAR)
        }
        if (q.contains("how long is this") || q.contains("duration")) {
            return OrbitIntent.SongInfoIntent(SongInfoType.DURATION)
        }

        // --- 9. Queue Management ---
        if (q.contains("what's playing next") || q.contains("what is playing next") || q.contains("what's the next song") || q.contains("what is next in queue")) {
            return OrbitIntent.QueueNextInfoIntent
        }
        val removeQueueMatch = Pattern.compile("(?:remove|delete)\\s+(?:the\\s+)?(\\d+|first|second|third|fourth|fifth)\\s+(?:song|track)?\\s*(?:from\\s+(?:the\\s+)?queue)?").matcher(q)
        if (removeQueueMatch.find()) {
            val numStr = removeQueueMatch.group(1) ?: "1"
            val idx = when (numStr) {
                "first", "1st" -> 1
                "second", "2nd" -> 2
                "third", "3rd" -> 3
                "fourth", "4th" -> 4
                "fifth", "5th" -> 5
                else -> numStr.toIntOrNull() ?: 1
            }
            return OrbitIntent.QueueRemoveIndexIntent(idx)
        }
        if (q.contains("play this next") || q.contains("play next")) {
            return OrbitIntent.QueueAddIntent(trackName = currentSong?.title, playNext = true)
        }
        if (q.contains("add this to the queue") || q.contains("add to queue") || q.contains("add these songs to my queue") || q.contains("add to the queue") || q.contains("queue this")) {
            return OrbitIntent.QueueAddIntent(trackName = currentSong?.title, playNext = false)
        }
        if (q.contains("show queue") || q.contains("show my queue") || q.contains("open queue")) {
            return OrbitIntent.QueueShowIntent
        }
        if (q.contains("clear queue") || q.contains("clear my queue") || q.contains("empty queue")) {
            return OrbitIntent.QueueClearIntent
        }

        // --- 10. Downloads ---
        if (q.contains("download this song") || q.contains("download this") || q.contains("download song")) {
            return OrbitIntent.DownloadIntent(DownloadAction.CURRENT_SONG)
        }
        if (q.contains("download album") || q.contains("download this album")) {
            return OrbitIntent.DownloadIntent(DownloadAction.CURRENT_ALBUM)
        }
        if (q.contains("show downloads") || q.contains("my downloads") || q.contains("open downloads")) {
            return OrbitIntent.DownloadIntent(DownloadAction.SHOW_DOWNLOADS)
        }

        // --- 11. Soundtracks / Movies ---
        val movieMatch = Pattern.compile("play\\s+(?:songs\\s+from|soundtrack\\s+from|music\\s+from)\\s+(.+)").matcher(q)
        if (movieMatch.find()) {
            val movie = movieMatch.group(1)?.trim().orEmpty()
            return OrbitIntent.PlaySoundtrackIntent(movie)
        }

        // --- 12. Languages ---
        val languages = listOf("punjabi", "tamil", "telugu", "hindi", "english", "bollywood", "korean", "spanish")
        for (lang in languages) {
            if (q.contains("$lang songs") || q.contains("$lang music") || q == "play $lang") {
                return OrbitIntent.PlayLanguageIntent(lang.capitalize(Locale.ROOT))
            }
        }

        // --- 13. Moods & Discovery ---
        val modifyPattern = Pattern.compile("(?:make it|something)?\\s*more\\s+([a-zA-Z\\-]+)").matcher(q)
        if (modifyPattern.find() || q.contains("old-school") || q.contains("more classic") || q.contains("more upbeat")) {
            val mod = if (q.contains("old-school")) "old-school" else modifyPattern.group(1) ?: "classic"
            return OrbitIntent.ModifyCurrentMixIntent(mod)
        }

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

        val moodKeywords = listOf(
            "romantic", "sad", "relaxing", "workout", "gym", "energetic",
            "chill", "study", "focus", "party", "sleep", "happy", "driving", "road trip", "old bollywood"
        )
        for (mood in moodKeywords) {
            if (q.contains(mood)) {
                return OrbitIntent.PlayGenreOrMoodIntent(mood)
            }
        }

        // --- 14. Playlist Operations ---
        val deletePlaylistMatch = Pattern.compile("(?:delete|remove)\\s+(?:my\\s+)?playlist\\s+(.+)").matcher(q)
        if (deletePlaylistMatch.find()) {
            val pName = deletePlaylistMatch.group(1)?.trim().orEmpty()
            return OrbitIntent.DeletePlaylistIntent(playlistName = pName)
        }

        val addToPlaylistMatch = Pattern.compile("(?:add this to|add this song to|add to)\\s+(?:my\\s+)?(?:playlist\\s+)?(.+)").matcher(q)
        if (addToPlaylistMatch.find()) {
            val pName = addToPlaylistMatch.group(1)?.removePrefix("playlist ")?.trim().orEmpty()
            if (pName.isNotBlank() && pName != "favorites" && pName != "queue") {
                return OrbitIntent.AddToPlaylistIntent(playlistName = pName, trackName = currentSong?.title)
            }
        }

        val createPlaylistMatch = Pattern.compile("create\\s+(?:a\\s+)?(?:new\\s+)?playlist\\s+(?:called\\s+|named\\s+)?(.+)").matcher(q)
        if (createPlaylistMatch.find()) {
            val playlistName = createPlaylistMatch.group(1)?.trim().orEmpty()
            return OrbitIntent.CreatePlaylistIntent(name = playlistName, addCurrentTrack = q.contains("add this"))
        }

        val playPlaylistMatch = Pattern.compile("play\\s+(?:my\\s+)?playlist\\s+(.+)").matcher(q)
        if (playPlaylistMatch.find()) {
            val playlistName = playPlaylistMatch.group(1)?.trim().orEmpty()
            return OrbitIntent.PlayPlaylistIntent(playlistName)
        }

        if (q.contains("show my playlists") || q.contains("my playlists") || q.contains("open playlists")) {
            return OrbitIntent.ShowPlaylistsIntent
        }

        // --- 15. Play Artist / Track / General Search ---
        val giveMeArtistMatch = Pattern.compile("(?:give me some|give me|play some)\\s+(.+?)\\s+songs?").matcher(q)
        if (giveMeArtistMatch.find()) {
            val potentialArtist = giveMeArtistMatch.group(1)?.trim().orEmpty()
            if (potentialArtist.isNotBlank() && !moodKeywords.contains(potentialArtist) && !languages.contains(potentialArtist)) {
                return OrbitIntent.PlayArtistIntent(potentialArtist)
            }
        }

        val artistSongsMatch = Pattern.compile("play\\s+(.+?)\\s+songs?").matcher(q)
        if (artistSongsMatch.find()) {
            val potential = artistSongsMatch.group(1)?.trim().orEmpty()
            if (potential.isNotBlank() && !moodKeywords.contains(potential) && !languages.contains(potential)) {
                return OrbitIntent.PlayArtistIntent(potential)
            }
        }

        // Check for "Play [Artist] [mood] songs" (e.g. "Play Arijit Singh romantic songs")
        for (mood in moodKeywords) {
            val artistMoodPattern = Pattern.compile("(?:play|give me)\\s+(.+?)\\s+$mood\\s+(?:songs?|music)").matcher(q)
            if (artistMoodPattern.find()) {
                val artist = artistMoodPattern.group(1)?.trim().orEmpty()
                if (artist.isNotBlank()) {
                    return OrbitIntent.PlayArtistIntent(artistName = artist, moodOrGenre = mood)
                }
            }
        }

        val playArtistMatch = Pattern.compile("play\\s+(?:songs\\s+by|music\\s+by|artist)\\s+(.+)").matcher(q)
        if (playArtistMatch.find()) {
            val artist = playArtistMatch.group(1)?.trim().orEmpty()
            return OrbitIntent.PlayArtistIntent(artist)
        }

        if (q.startsWith("play that ") || q.startsWith("play the song ") || q.startsWith("play song ")) {
            val track = q.removePrefix("play that ")
                .removePrefix("play the song ")
                .removePrefix("play song ")
                .removeSuffix(" song").trim()
            return OrbitIntent.PlayTrackIntent(trackName = track)
        }

        if (q.startsWith("play ")) {
            val target = q.removePrefix("play ").removeSuffix(" song").trim()
            if (target.isNotBlank()) {
                // If it looks like a known artist name query without song, e.g. "play Arijit Singh"
                val artistKeywords = listOf("arijit singh", "arijit", "shreya ghoshal", "taylor swift", "the weeknd", "ed sheeran", "drake", "badshah", "sidhu moose wala", "diljit dosanjh", "pritam", "ar rahman", "anirudh")
                if (artistKeywords.any { target.equals(it, ignoreCase = true) }) {
                    return OrbitIntent.PlayArtistIntent(artistName = target)
                }
                return OrbitIntent.PlayTrackIntent(trackName = target)
            }
        }

        // --- 16. Navigation ---
        if (q.contains("open search") || q.contains("go to search")) return OrbitIntent.NavigationIntent("search")
        if (q.contains("open library") || q.contains("go to library")) return OrbitIntent.NavigationIntent("library")
        if (q.contains("open home") || q.contains("go to home")) return OrbitIntent.NavigationIntent("home")
        if (q.contains("open settings")) return OrbitIntent.NavigationIntent("settings")

        // Fallback: If utterance is non-empty, treat as a track search request
        if (q.isNotBlank()) {
            return OrbitIntent.PlayTrackIntent(trackName = q)
        }

        return OrbitIntent.UnknownIntent(q)
    }
}
