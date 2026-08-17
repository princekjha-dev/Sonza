package com.sonza.music.core.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.sonza.music.core.model.AudioCodec
import com.sonza.music.core.model.AudioQuality
import com.sonza.music.core.model.LoudnessMetadata
import com.sonza.music.core.model.PlaylistType
import com.sonza.music.core.model.Track

@Entity(
    tableName = "tracks",
    indices = [
        Index(value = ["title"]),
        Index(value = ["artist"]),
        Index(value = ["album"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isDownloaded"]),
        Index(value = ["isLocal"])
    ]
)
data class TrackEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val artistId: String,
    val album: String,
    val albumId: String,
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String?,
    val trackNumber: Int,
    val discNumber: Int,
    val year: Int,
    val genre: String,
    // Audiophile Quality Metadata
    val codec: String,
    val bitDepth: Int,
    val sampleRateHz: Int,
    val bitRateKbps: Int,
    val isLossless: Boolean,
    val channelCount: Int,
    // ReplayGain & Loudness
    val trackGainDb: Float,
    val trackPeak: Float,
    val albumGainDb: Float,
    val albumPeak: Float,
    val isFavorite: Boolean,
    val isDownloaded: Boolean,
    val isLocal: Boolean,
    val sourceProvider: String,
    val playCount: Long,
    val dateAdded: Long
) {
    fun toDomain(): Track {
        return Track(
            id = id,
            title = title,
            artist = artist,
            artistId = artistId,
            album = album,
            albumId = albumId,
            durationMs = durationMs,
            mediaUri = mediaUri,
            artworkUri = artworkUri,
            trackNumber = trackNumber,
            discNumber = discNumber,
            year = year,
            genre = genre,
            quality = AudioQuality(
                codec = try { AudioCodec.valueOf(codec) } catch (e: Exception) { AudioCodec.FLAC },
                bitDepth = bitDepth,
                sampleRateHz = sampleRateHz,
                bitRateKbps = bitRateKbps,
                isLossless = isLossless,
                channelCount = channelCount
            ),
            loudness = LoudnessMetadata(
                trackGainDb = trackGainDb,
                trackPeak = trackPeak,
                albumGainDb = albumGainDb,
                albumPeak = albumPeak
            ),
            isFavorite = isFavorite,
            isDownloaded = isDownloaded,
            isLocal = isLocal,
            sourceProvider = sourceProvider,
            playCount = playCount,
            dateAdded = dateAdded
        )
    }

    companion object {
        fun fromDomain(track: Track): TrackEntity {
            return TrackEntity(
                id = track.id,
                title = track.title,
                artist = track.artist,
                artistId = track.artistId,
                album = track.album,
                albumId = track.albumId,
                durationMs = track.durationMs,
                mediaUri = track.mediaUri,
                artworkUri = track.artworkUri,
                trackNumber = track.trackNumber,
                discNumber = track.discNumber,
                year = track.year,
                genre = track.genre,
                codec = track.quality.codec.name,
                bitDepth = track.quality.bitDepth,
                sampleRateHz = track.quality.sampleRateHz,
                bitRateKbps = track.quality.bitRateKbps,
                isLossless = track.quality.isLossless,
                channelCount = track.quality.channelCount,
                trackGainDb = track.loudness.trackGainDb,
                trackPeak = track.loudness.trackPeak,
                albumGainDb = track.loudness.albumGainDb,
                albumPeak = track.loudness.albumPeak,
                isFavorite = track.isFavorite,
                isDownloaded = track.isDownloaded,
                isLocal = track.isLocal,
                sourceProvider = track.sourceProvider,
                playCount = track.playCount,
                dateAdded = track.dateAdded
            )
        }
    }
}

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val artworkUri: String?,
    val type: String,
    val trackCount: Int,
    val durationMs: Long,
    val isCollaborative: Boolean,
    val isDownloaded: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

@Entity(
    tableName = "playlist_tracks",
    primaryKeys = ["playlistId", "trackId"],
    foreignKeys = [
        ForeignKey(
            entity = PlaylistEntity::class,
            parentColumns = ["id"],
            childColumns = ["playlistId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("playlistId"), Index("trackId")]
)
data class PlaylistTrackCrossRef(
    val playlistId: String,
    val trackId: String,
    val sortOrder: Int
)

@Entity(tableName = "playback_history", indices = [Index("timestamp")])
data class PlaybackHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackId: String,
    val trackTitle: String,
    val artistName: String,
    val durationMs: Long,
    val listenedMs: Long,
    val isLossless: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "lyrics_cache")
data class LyricsEntity(
    @PrimaryKey val trackId: String,
    val rawLrcContent: String,
    val isSynced: Boolean,
    val hasWordSync: Boolean,
    val offsetMs: Long,
    val source: String,
    val cachedAt: Long = System.currentTimeMillis()
)
