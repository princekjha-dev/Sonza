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
    SINGER,
    WRITER,
    COMPOSER,
    RELEASE_DATE,
    DETAILS,
    VERSIONS,
    POPULARITY,
    STORY,
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
 * Complete sealed hierarchy of all 93 Orbit Intents plus contextual intelligence.
 */
sealed interface OrbitIntent {
    // 0. Conversational Flow, Wake Word & Intelligence
    object WakeWordOnlyIntent : OrbitIntent
    object CancelIntent : OrbitIntent
    data class DisambiguationChoiceIntent(val choice: String) : OrbitIntent
    data class ConfirmationResponseIntent(val confirmed: Boolean) : OrbitIntent
    data class CorrectionIntent(val correctionText: String) : OrbitIntent
    data class FollowUpIntent(val query: String) : OrbitIntent
    data class SmallTalkIntent(val query: String) : OrbitIntent
    data class AIQueryIntent(val query: String, val forcedTool: OrbitToolCall? = null) : OrbitIntent
    data class ToolExecutionIntent(val toolCall: OrbitToolCall) : OrbitIntent

    // 1. Play Music / Discovery
    data class PlayTrackIntent(val trackName: String, val artistName: String? = null) : OrbitIntent
    data class PlayArtistIntent(val artistName: String, val moodOrGenre: String? = null) : OrbitIntent
    data class PlayAlbumIntent(val albumName: String) : OrbitIntent
    data class PlayPlaylistIntent(val playlistName: String) : OrbitIntent
    data class PlayGenreOrMoodIntent(val query: String) : OrbitIntent
    data class PlayActivityIntent(val activity: String) : OrbitIntent
    data class PlayLanguageIntent(val language: String) : OrbitIntent
    data class PlayEraIntent(val era: String) : OrbitIntent
    data class PlayCreatorIntent(val creator: String) : OrbitIntent
    data class PlayVersionIntent(val trackName: String, val version: String) : OrbitIntent
    data class PlayFestivalIntent(val festival: String) : OrbitIntent
    data class PlaySoundtrackIntent(val movieOrShow: String) : OrbitIntent
    object PlayFavoritesIntent : OrbitIntent
    object SurpriseMeIntent : OrbitIntent
    object SimilarMusicIntent : OrbitIntent
    object TrendingIntent : OrbitIntent
    object NewReleasesIntent : OrbitIntent
    object PlayUnheardIntent : OrbitIntent
    object ContinueListeningIntent : OrbitIntent
    object PlayMostPlayedIntent : OrbitIntent
    data class ModifyCurrentMixIntent(val modifier: String) : OrbitIntent

    // 2. Playback Controls
    object PauseIntent : OrbitIntent
    object ResumeIntent : OrbitIntent
    object StopIntent : OrbitIntent
    object NextTrackIntent : OrbitIntent
    object PreviousTrackIntent : OrbitIntent
    object ReplayIntent : OrbitIntent
    data class SeekIntent(val seconds: Int, val isForward: Boolean) : OrbitIntent
    data class SeekAbsoluteIntent(val positionSeconds: Long) : OrbitIntent
    data class SetSpeedIntent(val speed: Float) : OrbitIntent

    // 3. Modes & Volume
    data class ShuffleIntent(val enable: Boolean) : OrbitIntent
    data class RepeatIntent(val mode: RepeatMode) : OrbitIntent
    data class VolumeIntent(val action: VolumeAction, val levelPercent: Int? = null) : OrbitIntent
    data class MuteIntent(val mute: Boolean) : OrbitIntent
    data class AudioQualityIntent(val quality: String) : OrbitIntent

    // 4. Favorites & Playlists & Artists
    data class FavoriteIntent(val isFavorite: Boolean) : OrbitIntent
    object DislikeSongIntent : OrbitIntent
    object SaveToLibraryIntent : OrbitIntent
    data class CreatePlaylistIntent(val name: String, val addCurrentTrack: Boolean = false) : OrbitIntent
    data class AddToPlaylistIntent(val playlistName: String, val trackName: String? = null) : OrbitIntent
    data class RemoveFromPlaylistIntent(val playlistName: String, val trackName: String? = null) : OrbitIntent
    data class DeletePlaylistIntent(val playlistName: String, val confirmed: Boolean = false) : OrbitIntent
    data class PlaylistInfoIntent(val playlistName: String) : OrbitIntent
    object ShowPlaylistsIntent : OrbitIntent
    data class ArtistInfoIntent(val artistName: String) : OrbitIntent
    data class ArtistTopSongsIntent(val artistName: String) : OrbitIntent
    data class ArtistSimilarIntent(val artistName: String) : OrbitIntent
    data class ArtistNewReleaseIntent(val artistName: String) : OrbitIntent
    data class AlbumInfoIntent(val albumName: String) : OrbitIntent

    // 5. Queue Operations
    data class QueueAddIntent(val trackName: String? = null, val playNext: Boolean = false) : OrbitIntent
    data class QueueRemoveIndexIntent(val indexOneBased: Int) : OrbitIntent
    object QueueShowIntent : OrbitIntent
    object QueueClearIntent : OrbitIntent
    object QueueNextInfoIntent : OrbitIntent

    // 6. Metadata, Info & Lyrics
    data class SongInfoIntent(val queryType: SongInfoType) : OrbitIntent
    data class IdentifySongIntent(val lyricsSnippet: String) : OrbitIntent
    data class LyricsIntent(val show: Boolean) : OrbitIntent

    // 7. Downloads & Sleep Timer & Storage
    data class DownloadIntent(val action: DownloadAction) : OrbitIntent
    data class SleepTimerIntent(val minutes: Int?, val cancel: Boolean = false, val stopAfterCurrent: Boolean = false) : OrbitIntent
    data class OfflineModeIntent(val enabled: Boolean) : OrbitIntent
    object StorageInfoIntent : OrbitIntent

    // 8. Navigation & App Features & Stats
    data class NavigationIntent(val screen: String) : OrbitIntent
    data class PlayerUiIntent(val expand: Boolean) : OrbitIntent
    object ListeningStatsIntent : OrbitIntent
    object HelpIntent : OrbitIntent
    data class ShareIntent(val trackName: String? = null) : OrbitIntent

    // 9. Compound Intent
    data class CompoundIntent(val intents: List<OrbitIntent>) : OrbitIntent

    // Conversational Greeting
    data class GreetingIntent(val rawGreeting: String) : OrbitIntent

    // Fallback
    data class UnknownIntent(val rawQuery: String) : OrbitIntent
}
