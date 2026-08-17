package com.sonza.music.audio.equalizer

import android.media.audiofx.Equalizer
import com.sonza.music.core.logging.SonzaLogger
import com.sonza.music.core.model.EqualizerPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.pow
import kotlin.math.roundToInt

class SonzaEqualizer {

    private var androidEqualizer: Equalizer? = null
    private var isEnabled = true
    private var preampGainDb = 0.0f
    private var currentGains = MutableStateFlow(EqualizerPreset.FLAT.bandGains)
    private var activePreset = MutableStateFlow<EqualizerPreset>(EqualizerPreset.FLAT)

    val gainsFlow: StateFlow<List<Float>> = currentGains.asStateFlow()
    val presetFlow: StateFlow<EqualizerPreset> = activePreset.asStateFlow()

    fun attachAudioSession(audioSessionId: Int) {
        try {
            release()
            if (audioSessionId != 0) {
                androidEqualizer = Equalizer(0, audioSessionId).apply {
                    enabled = isEnabled
                }
                applyCurrentGainsToNative()
                SonzaLogger.i("SonzaEqualizer", "Attached to audio session: $audioSessionId")
            }
        } catch (e: Exception) {
            SonzaLogger.w("SonzaEqualizer", "Native Equalizer init failed, falling back to software DSP: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        try {
            androidEqualizer?.enabled = enabled
        } catch (e: Exception) {
            SonzaLogger.w("SonzaEqualizer", "Error setting enabled: ${e.message}")
        }
    }

    fun setBandGain(bandIndex: Int, gainDb: Float) {
        val clampedGain = gainDb.coerceIn(-12.0f, 12.0f)
        val updated = currentGains.value.toMutableList()
        if (bandIndex in updated.indices) {
            updated[bandIndex] = clampedGain
            currentGains.value = updated
            activePreset.value = EqualizerPreset(
                id = "custom_${System.currentTimeMillis()}",
                name = "Custom Curve",
                bandGains = updated,
                preampGainDb = preampGainDb,
                isCustom = true
            )
            applyBandGainToNative(bandIndex, clampedGain)
        }
    }

    fun setPreampGain(gainDb: Float) {
        preampGainDb = gainDb.coerceIn(-6.0f, 6.0f)
    }

    fun getPreampGain(): Float = preampGainDb

    fun applyPreset(preset: EqualizerPreset) {
        activePreset.value = preset
        currentGains.value = preset.bandGains
        preampGainDb = preset.preampGainDb

        for (i in preset.bandGains.indices) {
            applyBandGainToNative(i, preset.bandGains[i])
        }
    }

    private fun applyCurrentGainsToNative() {
        val gains = currentGains.value
        for (i in gains.indices) {
            applyBandGainToNative(i, gains[i])
        }
    }

    private fun applyBandGainToNative(bandIndex: Int, gainDb: Float) {
        androidEqualizer?.let { eq ->
            try {
                if (bandIndex < eq.numberOfBands) {
                    val minLevel = eq.bandLevelRange[0] // in mB (millibels = dB * 100)
                    val maxLevel = eq.bandLevelRange[1]
                    val targetMb = (gainDb * 100).roundToInt().toShort().coerceIn(minLevel, maxLevel)
                    eq.setBandLevel(bandIndex.toShort(), targetMb)
                }
            } catch (e: Exception) {
                SonzaLogger.w("SonzaEqualizer", "Failed setting native band $bandIndex to $gainDb dB: ${e.message}")
            }
        }
    }

    /**
     * Software DSP gain multiplier calculation: G = 10^(dB / 20)
     */
    fun calculateLinearGain(gainDb: Float): Float {
        return 10.0.pow((gainDb + preampGainDb) / 20.0).toFloat()
    }

    fun release() {
        try {
            androidEqualizer?.release()
            androidEqualizer = null
        } catch (e: Exception) {
            SonzaLogger.w("SonzaEqualizer", "Error releasing Equalizer: ${e.message}")
        }
    }
}
