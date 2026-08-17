package com.sonza.music.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val artworkUri: String? = null,
    val year: Int = 2026,
    val genre: String = "Audiophile",
    val trackCount: Int = 0,
    val durationMs: Long = 0,
    val quality: AudioQuality = AudioQuality(),
    val isHiRes: Boolean = true
)

@Serializable
data class Artist(
    val id: String,
    val name: String,
    val artworkUri: String? = null,
    val bio: String = "",
    val monthlyListeners: Long = 0,
    val trackCount: Int = 0,
    val albumCount: Int = 0
)

@Serializable
enum class PlaylistType {
    USER_CREATED,
    SMART,
    SPOTIFY_IMPORTED,
    FAVORITES,
    RECENTLY_PLAYED,
    MOST_PLAYED,
    HIGH_RES_MIX
}

@Serializable
data class Playlist(
    val id: String,
    val title: String,
    val description: String = "",
    val artworkUri: String? = null,
    val type: PlaylistType = PlaylistType.USER_CREATED,
    val trackCount: Int = 0,
    val durationMs: Long = 0,
    val isCollaborative: Boolean = false,
    val isDownloaded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
