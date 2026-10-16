package com.sonza.app.ai.orbit

import com.sonza.app.core.model.Song

/**
 * Authoritative Orbit Execution and Spoken Response model.
 * Every intent maps to an [OrbitResult] containing both the executable action,
 * the mandatory contextual spoken response, and UI confirmation indicators.
 */
data class OrbitResult(
    val success: Boolean,
    val action: String,
    val spokenResponse: String,
    val visualFeedback: String? = null,
    val navigationTarget: String? = null,
    val requiresFollowUp: Boolean = false,
    val followUpContext: String? = null,
    val disambiguationOptions: List<String> = emptyList(),
    val actionIndicator: String? = null,
    val updatedContext: OrbitDialogueContext? = null
) {
    val message: String get() = visualFeedback ?: spokenResponse
}

/**
 * Dedicated Spoken Response Engine for Orbit.
 * Generates natural, contextual, human-like voice responses for all user commands.
 * Never produces silent completions or hard-coded "Done" generic strings.
 */
object OrbitResponseEngine {

    fun playSong(title: String, artist: String?): OrbitResult {
        val spoken = if (!artist.isNullOrBlank() && artist != "<unknown>") {
            "Playing $title by $artist."
        } else {
            "Playing $title."
        }
        return OrbitResult(
            success = true,
            action = "PLAY_SONG",
            spokenResponse = spoken,
            visualFeedback = "Playing $title",
            actionIndicator = "▶️"
        )
    }

    fun playArtist(artist: String, moodOrGenre: String? = null): OrbitResult {
        val spoken = if (!moodOrGenre.isNullOrBlank()) {
            "Playing a $moodOrGenre mix by $artist."
        } else {
            "Playing top songs by $artist."
        }
        return OrbitResult(
            success = true,
            action = "PLAY_ARTIST",
            spokenResponse = spoken,
            visualFeedback = "Playing $artist",
            actionIndicator = "🧑‍🎤"
        )
    }

    fun playAlbum(album: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "PLAY_ALBUM",
            spokenResponse = "Playing the album $album.",
            visualFeedback = "Album: $album",
            actionIndicator = "💿"
        )
    }

    fun playPlaylist(playlist: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "PLAY_PLAYLIST",
            spokenResponse = "Playing your playlist $playlist.",
            visualFeedback = "Playlist: $playlist",
            actionIndicator = "📁"
        )
    }

    fun playMoodOrGenre(mood: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "PLAY_BY_MOOD",
            spokenResponse = "Playing a $mood mix for you.",
            visualFeedback = "$mood Mix",
            actionIndicator = "✨"
        )
    }

    fun playFavorites(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "PLAY_FAVORITES",
            spokenResponse = "Playing your favorite songs.",
            visualFeedback = "Favorites",
            actionIndicator = "❤️"
        )
    }

    fun surpriseMe(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "SURPRISE_ME",
            spokenResponse = "Here's something I think you'll like.",
            visualFeedback = "Surprise Mix",
            actionIndicator = "🎲"
        )
    }

    fun pause(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "PAUSE",
            spokenResponse = "Paused.",
            visualFeedback = "Paused",
            actionIndicator = "⏸️"
        )
    }

    fun resume(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "RESUME",
            spokenResponse = "Resuming playback.",
            visualFeedback = "Playing",
            actionIndicator = "▶️"
        )
    }

    fun stop(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "STOP",
            spokenResponse = "Stopped.",
            visualFeedback = "Stopped",
            actionIndicator = "⏹️"
        )
    }

    fun next(nextSong: Song? = null): OrbitResult {
        val spoken = if (nextSong != null && nextSong.title.isNotBlank()) {
            "Next up is ${nextSong.title} by ${nextSong.artist}."
        } else {
            "Skipping to the next song."
        }
        return OrbitResult(
            success = true,
            action = "NEXT",
            spokenResponse = spoken,
            visualFeedback = "Skipped",
            actionIndicator = "⏭️"
        )
    }

    fun previous(prevSong: Song? = null): OrbitResult {
        val spoken = if (prevSong != null && prevSong.title.isNotBlank()) {
            "Going back to ${prevSong.title}."
        } else {
            "Going back to the previous song."
        }
        return OrbitResult(
            success = true,
            action = "PREVIOUS",
            spokenResponse = spoken,
            visualFeedback = "Previous track",
            actionIndicator = "⏮️"
        )
    }

    fun replay(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "REPLAY",
            spokenResponse = "Restarting song from the beginning.",
            visualFeedback = "Restarted",
            actionIndicator = "🔄"
        )
    }

    fun seek(seconds: Int, isForward: Boolean): OrbitResult {
        val spoken = if (isForward) {
            "Jumped forward $seconds seconds."
        } else {
            "Rewound $seconds seconds."
        }
        return OrbitResult(
            success = true,
            action = "SEEK",
            spokenResponse = spoken,
            visualFeedback = if (isForward) "+${seconds}s" else "-${seconds}s",
            actionIndicator = "⏩"
        )
    }

    fun volume(percent: Int, isUp: Boolean): OrbitResult {
        val spoken = "Volume is now $percent percent."
        return OrbitResult(
            success = true,
            action = "VOLUME",
            spokenResponse = spoken,
            visualFeedback = "Volume $percent%",
            actionIndicator = if (isUp) "🔊" else "🔉"
        )
    }

    fun mute(isMuted: Boolean): OrbitResult {
        return OrbitResult(
            success = true,
            action = "MUTE",
            spokenResponse = if (isMuted) "Muted." else "Unmuted.",
            visualFeedback = if (isMuted) "Muted" else "Unmuted",
            actionIndicator = if (isMuted) "🔇" else "🔊"
        )
    }

    fun shuffle(enabled: Boolean): OrbitResult {
        return OrbitResult(
            success = true,
            action = "SHUFFLE",
            spokenResponse = if (enabled) "Shuffle is on." else "Shuffle is off.",
            visualFeedback = if (enabled) "Shuffle On" else "Shuffle Off",
            actionIndicator = "🔀"
        )
    }

    fun repeat(mode: String): OrbitResult {
        val spoken = when (mode.uppercase()) {
            "ONE" -> "Repeating this song."
            "ALL" -> "Repeat is on."
            else -> "Repeat is off."
        }
        return OrbitResult(
            success = true,
            action = "REPEAT",
            spokenResponse = spoken,
            visualFeedback = spoken,
            actionIndicator = "🔁"
        )
    }

    fun like(songTitle: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "LIKE",
            spokenResponse = "Added $songTitle to your liked songs.",
            visualFeedback = "Added to Liked Songs",
            actionIndicator = "❤️"
        )
    }

    fun dislike(songTitle: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "DISLIKE",
            spokenResponse = "Got it. I won't recommend this one again.",
            visualFeedback = "Will not recommend again",
            actionIndicator = "💔"
        )
    }

    fun addToPlaylist(songTitle: String, playlistName: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "ADD_TO_PLAYLIST",
            spokenResponse = "Added $songTitle to your playlist $playlistName.",
            visualFeedback = "Added to $playlistName",
            actionIndicator = "➕"
        )
    }

    fun createPlaylist(name: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "CREATE_PLAYLIST",
            spokenResponse = "Created your playlist $name.",
            visualFeedback = "Playlist $name created",
            actionIndicator = "📁"
        )
    }

    fun deletePlaylist(name: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "DELETE_PLAYLIST",
            spokenResponse = "Deleted the playlist $name.",
            visualFeedback = "Deleted $name",
            actionIndicator = "🗑️"
        )
    }

    fun queueAdd(songTitle: String, playNext: Boolean): OrbitResult {
        val spoken = if (playNext) "Playing $songTitle next." else "Added $songTitle to the queue."
        return OrbitResult(
            success = true,
            action = if (playNext) "QUEUE_PLAY_NEXT" else "QUEUE_ADD",
            spokenResponse = spoken,
            visualFeedback = spoken,
            actionIndicator = "📑"
        )
    }

    fun queueClear(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "QUEUE_CLEAR",
            spokenResponse = "Cleared the queue.",
            visualFeedback = "Queue cleared",
            actionIndicator = "🧹"
        )
    }

    fun lyrics(show: Boolean): OrbitResult {
        return OrbitResult(
            success = true,
            action = if (show) "SHOW_LYRICS" else "HIDE_LYRICS",
            spokenResponse = if (show) "Opening lyrics." else "Closing lyrics.",
            navigationTarget = if (show) "lyrics" else null,
            visualFeedback = if (show) "Lyrics opened" else "Lyrics closed",
            actionIndicator = "🎤"
        )
    }

    fun sleepTimer(minutes: Int): OrbitResult {
        return OrbitResult(
            success = true,
            action = "SLEEP_TIMER",
            spokenResponse = "Sleep timer set for $minutes minutes.",
            visualFeedback = "Timer: ${minutes}m",
            actionIndicator = "⏱️"
        )
    }

    fun cancelSleepTimer(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "CANCEL_TIMER",
            spokenResponse = "Sleep timer cancelled.",
            visualFeedback = "Timer cancelled",
            actionIndicator = "⏱️"
        )
    }

    fun navigation(destination: String): OrbitResult {
        val spoken = "Opening $destination."
        return OrbitResult(
            success = true,
            action = "NAVIGATE",
            spokenResponse = spoken,
            navigationTarget = destination.lowercase(),
            visualFeedback = spoken,
            actionIndicator = "🧭"
        )
    }

    fun download(songTitle: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "DOWNLOAD",
            spokenResponse = "Starting download for $songTitle.",
            visualFeedback = "Downloading $songTitle",
            actionIndicator = "⬇️"
        )
    }

    fun infoSinger(song: Song?): OrbitResult {
        val spoken = if (song != null) {
            "This is ${song.title} sung by ${song.artist}."
        } else {
            "No song is currently playing."
        }
        return OrbitResult(
            success = song != null,
            action = "INFO_SINGER",
            spokenResponse = spoken,
            visualFeedback = song?.artist ?: "Unknown Artist",
            actionIndicator = "ℹ️"
        )
    }

    fun infoComposer(composer: String?, song: Song?): OrbitResult {
        val spoken = if (!composer.isNullOrBlank()) {
            "Composed by $composer."
        } else if (song != null) {
            "This track is by ${song.artist}."
        } else {
            "No song information available."
        }
        return OrbitResult(
            success = true,
            action = "INFO_COMPOSER",
            spokenResponse = spoken,
            visualFeedback = spoken,
            actionIndicator = "🎼"
        )
    }

    fun infoWriter(writer: String?, song: Song?): OrbitResult {
        val spoken = if (!writer.isNullOrBlank()) {
            "Lyrics written by $writer."
        } else if (song != null) {
            "Written for ${song.title} by ${song.artist}."
        } else {
            "No songwriter information available."
        }
        return OrbitResult(
            success = true,
            action = "INFO_WRITER",
            spokenResponse = spoken,
            visualFeedback = spoken,
            actionIndicator = "✍️"
        )
    }

    fun infoAlbum(song: Song?): OrbitResult {
        val spoken = if (song != null && !song.album.isNullOrBlank() && song.album != "<unknown>") {
            "This song is from the album ${song.album}."
        } else if (song != null) {
            "${song.title} is a single by ${song.artist}."
        } else {
            "No song is currently playing."
        }
        return OrbitResult(
            success = song != null,
            action = "INFO_ALBUM",
            spokenResponse = spoken,
            visualFeedback = song?.album ?: "No album",
            actionIndicator = "💿"
        )
    }

    fun infoWhatIsPlaying(song: Song?): OrbitResult {
        val spoken = if (song != null) {
            "Currently playing ${song.title} by ${song.artist}."
        } else {
            "Nothing is currently playing. Ask me to play a song or artist!"
        }
        return OrbitResult(
            success = song != null,
            action = "INFO_CURRENT",
            spokenResponse = spoken,
            visualFeedback = song?.title ?: "Nothing playing",
            actionIndicator = "🎵"
        )
    }

    fun infoWhatIsNext(nextSong: Song?): OrbitResult {
        val spoken = if (nextSong != null) {
            "Next up is ${nextSong.title} by ${nextSong.artist}."
        } else {
            "There are no upcoming songs in the queue."
        }
        return OrbitResult(
            success = nextSong != null,
            action = "INFO_NEXT",
            spokenResponse = spoken,
            visualFeedback = nextSong?.title ?: "Queue empty",
            actionIndicator = "⏭️"
        )
    }

    fun disambiguation(query: String, options: List<String>): OrbitResult {
        val optionsStr = options.take(3).joinToString(", or ")
        val spoken = "I found several versions of $query. Do you want $optionsStr?"
        return OrbitResult(
            success = true,
            action = "DISAMBIGUATE",
            spokenResponse = spoken,
            visualFeedback = "Which version would you like?",
            requiresFollowUp = true,
            followUpContext = query,
            disambiguationOptions = options,
            actionIndicator = "❓"
        )
    }

    fun correction(newChoice: String): OrbitResult {
        return OrbitResult(
            success = true,
            action = "CORRECTION",
            spokenResponse = "Got it. Playing the $newChoice version.",
            visualFeedback = "Switching to $newChoice",
            actionIndicator = "🔄"
        )
    }

    fun help(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "HELP",
            spokenResponse = "I can play and control music, search songs and artists, manage playlists and your queue, show lyrics, control volume, set sleep timers, navigate Sonza, and help you discover new music.",
            visualFeedback = "Voice Assistant Capabilities",
            actionIndicator = "💡"
        )
    }

    fun cancel(): OrbitResult {
        return OrbitResult(
            success = true,
            action = "CANCEL",
            spokenResponse = "Alright.",
            visualFeedback = "Cancelled",
            actionIndicator = "⏹️"
        )
    }

    fun unknown(rawQuery: String): OrbitResult {
        return OrbitResult(
            success = false,
            action = "UNKNOWN",
            spokenResponse = "I didn't quite catch that. Try asking me to play a song, skip, pause, search, or manage your playlist.",
            visualFeedback = "Didn't catch that",
            actionIndicator = "❓"
        )
    }

    fun error(message: String? = null): OrbitResult {
        return OrbitResult(
            success = false,
            action = "ERROR",
            spokenResponse = message?.takeIf { it.isNotBlank() } ?: "Sorry, I couldn't complete that.",
            visualFeedback = message ?: "Error completing command",
            actionIndicator = "⚠️"
        )
    }
}
