package com.sonza.music.core.model

import kotlinx.serialization.Serializable

@Serializable
data class EqualizerBand(
    val centerFreqHz: Int,
    val label: String,
    val gainDb: Float = 0.0f // Range: -12.0f to +12.0f
)

@Serializable
data class EqualizerPreset(
    val id: String,
    val name: String,
    val bandGains: List<Float>, // 10 values for 31, 62, 125, 250, 500, 1k, 2k, 4k, 8k, 16k Hz
    val preampGainDb: Float = 0.0f,
    val isCustom: Boolean = false
) {
    companion object {
        val FREQUENCY_BANDS = listOf(
            EqualizerBand(31, "31 Hz"),
            EqualizerBand(62, "62 Hz"),
            EqualizerBand(125, "125 Hz"),
            EqualizerBand(250, "250 Hz"),
            EqualizerBand(500, "500 Hz"),
            EqualizerBand(1000, "1 kHz"),
            EqualizerBand(2000, "2 kHz"),
            EqualizerBand(4000, "4 kHz"),
            EqualizerBand(8000, "8 kHz"),
            EqualizerBand(16000, "16 kHz")
        )

        val FLAT = EqualizerPreset("flat", "Flat", listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f))
        val BASS_BOOST = EqualizerPreset("bass_boost", "Bass Boost", listOf(5.5f, 4.5f, 3.0f, 1.5f, 0f, 0f, 0f, 0f, 0f, 0f))
        val TREBLE_BOOST = EqualizerPreset("treble_boost", "Treble Boost", listOf(0f, 0f, 0f, 0f, 0f, 0.5f, 2.0f, 3.5f, 5.0f, 6.0f))
        val VOCAL = EqualizerPreset("vocal", "Vocal & Clarity", listOf(-1.5f, -1.0f, 0f, 1.5f, 3.5f, 4.0f, 3.0f, 1.5f, 0f, -0.5f))
        val ROCK = EqualizerPreset("rock", "Rock", listOf(4.0f, 3.0f, 1.5f, 0f, -1.0f, -0.5f, 1.5f, 3.0f, 4.0f, 4.5f))
        val POP = EqualizerPreset("pop", "Pop", listOf(1.5f, 2.5f, 3.0f, 1.5f, 0f, 0f, 1.0f, 2.5f, 3.5f, 3.0f))
        val CLASSICAL = EqualizerPreset("classical", "Classical & Acoustic", listOf(3.5f, 3.0f, 2.0f, 1.5f, -0.5f, 0f, 1.0f, 2.5f, 3.0f, 3.5f))
        val JAZZ = EqualizerPreset("jazz", "Audiophile Jazz", listOf(2.5f, 2.0f, 1.0f, 1.5f, -1.0f, -0.5f, 0.5f, 2.0f, 3.0f, 3.5f))
        val ELECTRONIC = EqualizerPreset("electronic", "Electronic / EDM", listOf(6.0f, 5.0f, 2.0f, 0f, -1.5f, 1.0f, 1.5f, 3.0f, 4.5f, 5.0f))
        val ACOUSTIC = EqualizerPreset("acoustic", "Acoustic Warmth", listOf(3.0f, 2.5f, 1.5f, 1.0f, 1.0f, 1.5f, 2.0f, 2.5f, 2.0f, 1.5f))

        val FACTORY_PRESETS = listOf(
            FLAT, BASS_BOOST, TREBLE_BOOST, VOCAL, ROCK, POP, CLASSICAL, JAZZ, ELECTRONIC, ACOUSTIC
        )
    }
}

@Serializable
enum class SpatialMode {
    OFF,
    NATURAL,
    WIDE,
    CINEMA,
    IMMERSIVE,
    STUDIO
}

@Serializable
data class SpatialConfig(
    val enabled: Boolean = false,
    val mode: SpatialMode = SpatialMode.STUDIO,
    val intensity: Float = 0.65f, // 0.0 to 1.0
    val stereoWidth: Float = 1.25f // 0.5 (mono) to 2.0 (ultra-wide)
)

@Serializable
enum class VisualizerMode {
    WAVEFORM,
    SPECTRUM,
    BARS,
    CIRCULAR_SPECTRUM,
    MINIMAL_PULSE
}

@Serializable
data class VisualizerConfig(
    val enabled: Boolean = true,
    val mode: VisualizerMode = VisualizerMode.SPECTRUM,
    val targetFps: Int = 60,
    val intensity: Float = 0.8f
)

@Serializable
enum class HapticIntensity {
    OFF,
    SUBTLE,
    NORMAL,
    STRONG
}

@Serializable
enum class VolumeNormalizationMode {
    OFF,
    TRACK_GAIN,
    ALBUM_GAIN
}

@Serializable
enum class ThemeModePreference {
    DYNAMIC_ALBUM_ART,
    DARK_AUDIOPHILE,
    LIGHT_MINIMAL,
    SYSTEM
}

@Serializable
enum class OutputGearPreference {
    WIRED_HEADPHONES,
    BLUETOOTH_LDAC_APT_X,
    PHONE_SPEAKERS,
    CAR_AUDIO,
    EXTERNAL_HI_RES_DAC
}

@Serializable
data class UserPreferences(
    val onboarded: Boolean = false,
    val selectedGenres: List<String> = emptyList(),
    val outputGear: OutputGearPreference = OutputGearPreference.EXTERNAL_HI_RES_DAC,
    val preferredStreamingQuality: String = "Lossless (24-bit/96kHz)",
    val themeMode: ThemeModePreference = ThemeModePreference.DYNAMIC_ALBUM_ART,
    val hapticIntensity: HapticIntensity = HapticIntensity.NORMAL,
    val volumeNormalization: VolumeNormalizationMode = VolumeNormalizationMode.TRACK_GAIN,
    val crossfadeDurationSeconds: Int = 2,
    val gaplessEnabled: Boolean = true,
    val autoQueueSimilar: Boolean = true,
    val highResBadgeEnabled: Boolean = true,
    val reduceMotion: Boolean = false
)
