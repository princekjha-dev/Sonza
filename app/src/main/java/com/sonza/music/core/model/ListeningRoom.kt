package com.sonza.music.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoomRole {
    HOST,
    CO_HOST,
    LISTENER
}

@Serializable
data class RoomMember(
    val id: String,
    val displayName: String,
    val avatarUri: String? = null,
    val role: RoomRole = RoomRole.LISTENER,
    val latencyMs: Long = 25,
    val isMuted: Boolean = false,
    val joinedAt: Long = System.currentTimeMillis()
)

@Serializable
enum class RoomPlaybackState {
    PLAYING,
    PAUSED,
    BUFFERING,
    STOPPED
}

@Serializable
data class RoomSyncEvent(
    val roomId: String,
    val trackId: String,
    val playbackPositionMs: Long,
    val state: RoomPlaybackState,
    val serverTimestampMs: Long,
    val senderId: String,
    val sequenceNumber: Long = 0
)

@Serializable
data class ListeningRoom(
    val id: String, // 6-character room code like "SNZ-842"
    val title: String,
    val hostId: String,
    val hostName: String,
    val currentTrack: Track? = null,
    val playbackState: RoomPlaybackState = RoomPlaybackState.PAUSED,
    val playbackPositionMs: Long = 0,
    val members: List<RoomMember> = emptyList(),
    val maxParticipants: Int = 30,
    val isPrivate: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class GenreStats(
    val genre: String,
    val minutesListened: Long,
    val percentage: Float
)

@Serializable
data class ListeningStats(
    val totalMinutesListened: Long = 0,
    val totalTracksPlayed: Long = 0,
    val totalArtistsDiscovered: Int = 0,
    val totalLosslessMinutes: Long = 0,
    val topArtists: List<String> = emptyList(),
    val topAlbums: List<String> = emptyList(),
    val topTracks: List<Track> = emptyList(),
    val genreDistribution: List<GenreStats> = emptyList(),
    val monthlyWrap: MonthlyWrap? = null
)

@Serializable
data class MonthlyWrap(
    val monthName: String, // e.g. "August 2026"
    val minutesListened: Long,
    val uniqueTracksCount: Int,
    val topArtistName: String,
    val topTrackTitle: String,
    val topGenre: String,
    val audiophileScore: Int // e.g. 98% Lossless
)
