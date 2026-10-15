package com.sonza.app.ai.orbit

import com.sonza.app.core.model.RepeatMode

/**
 * Volume action types.
 */
enum class VolumeAction {
    UP,
    DOWN,
    SET_LEVEL
}

/**
 * Download action types.
 */
enum class DownloadAction {
    CURRENT_SONG,
    CURRENT_ALBUM,
    CURRENT_PLAYLIST,
    CANCEL,
    SHOW_DOWNLOADS
}

/**
 * Song information query types.
 */
enum class SongInfoType {
    TITLE,
    ARTIST,
    ALBUM,
    YEAR,
    DURATION,
    GENRE,
    ALL
}

/**
 * Conversational dialogue context carried across turns in Orbit's two-way dialogue loop.
 */
sealed interface OrbitDialogueContext {
    object Idle : OrbitDialogueContext

    /**
     * Triggered by saying "Hey Orbit" -> Orbit answers "Yes?" and waits for command.
     */
    data class AwaitingCommand(
        val timestamp: Long = System.currentTimeMillis()
    ) : OrbitDialogueContext

    /**
     * Orbit asked a disambiguation question (e.g. "Which Arijit Singh song would you like?
     * Or what mood — romantic, sad, relaxing, or energetic?").
     */
    data class AwaitingDisambiguation(
        val artist: String? = null,
        val options: List<String> = emptyList(),
        val originalQuery: String = ""
    ) : OrbitDialogueContext

    /**
     * Destructive action confirmation (e.g. "Are you sure you want to delete playlist Chill?").
     */
    data class AwaitingConfirmation(
        val actionType: String,
        val targetName: String
    ) : OrbitDialogueContext
}

/**
 * Sealed hierarchy of all actionable Orbit Intents.
 */
sealed interface OrbitIntent {
    // 0. Conversational Flow & Wake Word
    object WakeWordOnlyIntent : OrbitIntent
    object CancelIntent : OrbitIntent
    data class DisambiguationChoiceIntent(val choice: String) : OrbitIntent
    data class ConfirmationResponseIntent(val confirmed: Boolean) : OrbitIntent

    // 1. Play Music / Search
    data class PlayTrackIntent(val trackName: String, val artistName: String? = null) : OrbitIntent
    data class PlayArtistIntent(val artistName: String, val moodOrGenre: String? = null) : OrbitIntent
    data class PlayAlbumIntent(val albumName: String) : OrbitIntent
    data class PlayPlaylistIntent(val playlistName: String) : OrbitIntent
    data class PlayGenreOrMoodIntent(val query: String) : OrbitIntent
    data class PlayLanguageIntent(val language: String) : OrbitIntent
    data class PlaySoundtrackIntent(val movieOrShow: String) : OrbitIntent
    object PlayFavoritesIntent : OrbitIntent
    object SurpriseMeIntent : OrbitIntent
    object SimilarMusicIntent : OrbitIntent
    object TrendingIntent : OrbitIntent
    object NewReleasesIntent : OrbitIntent

    // 2. Playback Controls
    object PauseIntent : OrbitIntent
    object ResumeIntent : OrbitIntent
    object StopIntent : OrbitIntent
    object NextTrackIntent : OrbitIntent
    object PreviousTrackIntent : OrbitIntent
    object ReplayIntent : OrbitIntent
    data class SeekIntent(val seconds: Int, val isForward: Boolean) : OrbitIntent

    // 3. Modes & Volume
    data class ShuffleIntent(val enable: Boolean) : OrbitIntent
    data class RepeatIntent(val mode: RepeatMode) : OrbitIntent
    data class VolumeIntent(val action: VolumeAction, val levelPercent: Int? = null) : OrbitIntent
    data class MuteIntent(val mute: Boolean) : OrbitIntent

    // 4. Favorites & Playlists
    data class FavoriteIntent(val isFavorite: Boolean) : OrbitIntent
    data class CreatePlaylistIntent(val name: String, val addCurrentTrack: Boolean = false) : OrbitIntent
    data class AddToPlaylistIntent(val playlistName: String, val trackName: String? = null) : OrbitIntent
    data class RemoveFromPlaylistIntent(val playlistName: String, val trackName: String? = null) : OrbitIntent
    data class DeletePlaylistIntent(val playlistName: String, val confirmed: Boolean = false) : OrbitIntent
    object ShowPlaylistsIntent : OrbitIntent

    // 5. Queue Operations
    data class QueueAddIntent(val trackName: String? = null, val playNext: Boolean = false) : OrbitIntent
    data class QueueRemoveIndexIntent(val indexOneBased: Int) : OrbitIntent
    object QueueShowIntent : OrbitIntent
    object QueueClearIntent : OrbitIntent
    object QueueNextInfoIntent : OrbitIntent

    // 6. Metadata & Lyrics
    data class SongInfoIntent(val queryType: SongInfoType) : OrbitIntent
    data class LyricsIntent(val show: Boolean) : OrbitIntent

    // 7. Downloads & Sleep Timer
    data class DownloadIntent(val action: DownloadAction) : OrbitIntent
    data class SleepTimerIntent(val minutes: Int?, val cancel: Boolean = false) : OrbitIntent

    // 8. Navigation & App Features
    data class NavigationIntent(val screen: String) : OrbitIntent
    object HelpIntent : OrbitIntent

    // 9. Compound Intent
    data class CompoundIntent(val intents: List<OrbitIntent>) : OrbitIntent

    // Fallback
    data class UnknownIntent(val rawQuery: String) : OrbitIntent
}
