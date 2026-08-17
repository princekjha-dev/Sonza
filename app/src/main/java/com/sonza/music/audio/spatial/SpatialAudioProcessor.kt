package com.sonza.music.audio.spatial

import android.content.Context
import android.media.audiofx.Virtualizer
import android.os.Build
import com.sonza.music.core.logging.SonzaLogger
import com.sonza.music.core.model.SpatialConfig
import com.sonza.music.core.model.SpatialMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class SpatialAudioProcessor(private val context: Context) {

    private var virtualizer: Virtualizer? = null
    private val _config = MutableStateFlow(SpatialConfig())
    val config: StateFlow<SpatialConfig> = _config.asStateFlow()

    fun attachAudioSession(audioSessionId: Int) {
        try {
            release()
            if (audioSessionId != 0) {
                virtualizer = Virtualizer(0, audioSessionId).apply {
                    enabled = _config.value.enabled
                    if (strengthSupported) {
                        setStrength((_config.value.intensity * 1000).roundToInt().toShort())
                    }
                }
                SonzaLogger.i("SpatialAudio", "Attached spatial processor to session: $audioSessionId")
            }
        } catch (e: Exception) {
            SonzaLogger.w("SpatialAudio", "Virtualizer attachment failed: ${e.message}")
        }
    }

    fun updateConfig(config: SpatialConfig) {
        _config.value = config
        virtualizer?.let { v ->
            try {
                v.enabled = config.enabled && config.mode != SpatialMode.OFF
                if (v.strengthSupported) {
                    val strength = when (config.mode) {
                        SpatialMode.OFF -> 0
                        SpatialMode.NATURAL -> (config.intensity * 400).roundToInt()
                        SpatialMode.STUDIO -> (config.intensity * 600).roundToInt()
                        SpatialMode.WIDE -> (config.intensity * 800).roundToInt()
                        SpatialMode.CINEMA -> (config.intensity * 950).roundToInt()
                        SpatialMode.IMMERSIVE -> 1000
                    }.coerceIn(0, 1000).toShort()
                    v.setStrength(strength)
                }
            } catch (e: Exception) {
                SonzaLogger.w("SpatialAudio", "Failed updating spatial config: ${e.message}")
            }
        }
    }

    /**
     * Checks if platform supports hardware spatializer on Android 13+ (API 33)
     */
    fun isPlatformSpatializerAvailable(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
            audioManager?.spatializer?.isAvailable == true
        } else {
            false
        }
    }

    fun release() {
        try {
            virtualizer?.release()
            virtualizer = null
        } catch (e: Exception) {
            SonzaLogger.w("SpatialAudio", "Error releasing Virtualizer: ${e.message}")
        }
    }
}
