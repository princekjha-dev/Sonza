package com.sonza.music.audio.effects

import com.sonza.music.core.model.LoudnessMetadata
import com.sonza.music.core.model.VolumeNormalizationMode
import kotlin.math.pow

object ReplayGainNormalizer {

    private const val TARGET_LUFS_DB = -14.0f // Standard streaming reference target
    private const val MAX_GAIN_DB = 10.0f
    private const val MIN_GAIN_DB = -15.0f

    /**
     * Calculates linear volume multiplier based on ReplayGain tags and chosen mode.
     * Includes peak clipping prevention (limiting gain if gain * peak > 1.0)
     */
    fun calculateVolumeScale(
        mode: VolumeNormalizationMode,
        loudness: LoudnessMetadata,
        preampDb: Float = 0.0f
    ): Float {
        if (mode == VolumeNormalizationMode.OFF) {
            return 1.0f
        }

        val rawGainDb = when (mode) {
            VolumeNormalizationMode.TRACK_GAIN -> loudness.trackGainDb
            VolumeNormalizationMode.ALBUM_GAIN -> {
                if (loudness.albumGainDb != 0.0f) loudness.albumGainDb else loudness.trackGainDb
            }
            VolumeNormalizationMode.OFF -> 0.0f
        }

        val peak = when (mode) {
            VolumeNormalizationMode.TRACK_GAIN -> loudness.trackPeak.coerceAtLeast(0.01f)
            VolumeNormalizationMode.ALBUM_GAIN -> loudness.albumPeak.coerceAtLeast(0.01f)
            VolumeNormalizationMode.OFF -> 1.0f
        }

        val targetGainDb = (rawGainDb + preampDb).coerceIn(MIN_GAIN_DB, MAX_GAIN_DB)
        var linearScale = 10.0.pow(targetGainDb / 20.0).toFloat()

        // Anti-clipping prevention
        if (linearScale * peak > 1.0f) {
            linearScale = 1.0f / peak
        }

        return linearScale.coerceIn(0.1f, 2.0f)
    }
}
