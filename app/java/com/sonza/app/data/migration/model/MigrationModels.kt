package com.sonza.app.data.migration.model

import com.sonza.app.core.model.Song
import kotlinx.serialization.Serializable

/**
 * Supported external music services.
 */
enum class MigrationSource(
    val displayName: String,
    val isSupported: Boolean,
    val description: String
) {
    SPOTIFY("Spotify", true, "Import playlists via link or export file"),
    YOUTUBE_MUSIC("YouTube Music", true, "Import playlists via link or account"),
    YOUTUBE("YouTube", true, "Import playlists via link"),
    FILE_EXPORT("File Export", true, "Import from M3U, JSON, CSV, or TXT files"),
    APPLE_MUSIC("Apple Music", false, "Coming soon"),
    AMAZON_MUSIC("Amazon Music", false, "Coming soon"),
    DEEZER("Deezer", false, "Coming soon"),
    TIDAL("Tidal", false, "Coming soon")
}

/**
 * Match confidence level for a source track.
 */
enum class MatchConfidence {
    PERFECT_MATCH,
    POSSIBLE_MATCH,
    UNAVAILABLE
}

/**
 * Source track extracted from external playlist.
 */
data class SourceTrack(
    val title: String,
    val artist: String,
    val album: String? = null,
    val durationMs: Long = 0,
    val isrc: String? = null,
    val sourceId: String? = null
)

/**
 * Result of matching a single track against Sonza's catalog.
 */
data class TrackMatchResult(
    val sourceTrack: SourceTrack,
    val matchedSong: Song? = null,
    val confidence: MatchConfidence = MatchConfidence.UNAVAILABLE,
    val possibleCandidates: List<Song> = emptyList(),
    val isSkipped: Boolean = false,
    val isManuallySelected: Boolean = false
)

/**
 * Duplicate resolution strategies when a playlist already exists.
 */
enum class DuplicateStrategy {
    REPLACE,
    ADD_MISSING,
    CREATE_NEW_COPY,
    CANCEL
}

/**
 * Saved historical record of a migration.
 */
@Serializable
data class MigrationRecord(
    val id: String,
    val playlistTitle: String,
    val sourceName: String,
    val totalTracks: Int,
    val matchedCount: Int,
    val skippedCount: Int,
    val unavailableCount: Int,
    val targetPlaylistId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
