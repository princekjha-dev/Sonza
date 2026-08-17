package com.sonza.music.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class AudioCodec {
    FLAC,
    WAV,
    ALAC,
    OPUS,
    AAC,
    MP3,
    VORBIS,
    UNKNOWN
}

@Serializable
data class AudioQuality(
    val codec: AudioCodec = AudioCodec.FLAC,
    val bitDepth: Int = 24, // 16, 24, 32 bit
    val sampleRateHz: Int = 96000, // 44100, 48000, 96000, 192000 Hz
    val bitRateKbps: Int = 3000,
    val isLossless: Boolean = true,
    val channelCount: Int = 2 // Stereo / Multichannel
) {
    val displayBadge: String
        get() = "${codec.name} ${bitDepth}-bit / ${sampleRateHz / 1000}kHz"

    val isHiRes: Boolean
        get() = (bitDepth >= 24 && sampleRateHz >= 48000) || sampleRateHz >= 96000
}

@Serializable
data class LoudnessMetadata(
    val trackGainDb: Float = 0.0f,
    val trackPeak: Float = 1.0f,
    val albumGainDb: Float = 0.0f,
    val albumPeak: Float = 1.0f
)

@Serializable
data class Track(
    val id: String,
    val title: String,
    val artist: String,
    val artistId: String = "",
    val album: String,
    val albumId: String = "",
    val durationMs: Long,
    val mediaUri: String,
    val artworkUri: String? = null,
    val trackNumber: Int = 1,
    val discNumber: Int = 1,
    val year: Int = 2026,
    val genre: String = "Audiophile",
    val quality: AudioQuality = AudioQuality(),
    val loudness: LoudnessMetadata = LoudnessMetadata(),
    val isFavorite: Boolean = false,
    val isDownloaded: Boolean = false,
    val isLocal: Boolean = false,
    val sourceProvider: String = "SONZA_HIRES",
    val playCount: Long = 0,
    val dateAdded: Long = System.currentTimeMillis()
)
