package com.sonza.music.audio.analyzer

import android.media.audiofx.Visualizer
import com.sonza.music.core.logging.SonzaLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.sqrt

class AudioAnalyzer {

    private var visualizer: Visualizer? = null
    private val _waveformFlow = MutableStateFlow(FloatArray(128) { 0f })
    private val _fftFlow = MutableStateFlow(FloatArray(64) { 0f })
    private val _beatDetectedFlow = MutableStateFlow(false)

    val waveformFlow: StateFlow<FloatArray> = _waveformFlow.asStateFlow()
    val fftFlow: StateFlow<FloatArray> = _fftFlow.asStateFlow()
    val beatDetectedFlow: StateFlow<Boolean> = _beatDetectedFlow.asStateFlow()

    private var energyHistory = FloatArray(43) { 0f }
    private var historyIndex = 0

    fun attachAudioSession(audioSessionId: Int) {
        try {
            release()
            if (audioSessionId != 0) {
                visualizer = Visualizer(audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[0] // e.g. 128/256
                    setDataCaptureListener(
                        object : Visualizer.OnDataCaptureListener {
                            override fun onWaveFormDataCapture(
                                v: Visualizer?,
                                waveform: ByteArray?,
                                samplingRate: Int
                            ) {
                                waveform?.let { processWaveform(it) }
                            }

                            override fun onFftDataCapture(
                                v: Visualizer?,
                                fft: ByteArray?,
                                samplingRate: Int
                            ) {
                                fft?.let { processFft(it) }
                            }
                        },
                        Visualizer.getMaxCaptureRate() / 2,
                        true,
                        true
                    )
                    enabled = true
                }
                SonzaLogger.i("AudioAnalyzer", "Visualizer attached to session: $audioSessionId")
            }
        } catch (e: Exception) {
            SonzaLogger.w("AudioAnalyzer", "Visualizer permission or session error: ${e.message}")
        }
    }

    private fun processWaveform(raw: ByteArray) {
        val count = raw.size.coerceAtMost(128)
        val normalized = FloatArray(count)
        for (i in 0 until count) {
            normalized[i] = ((raw[i].toInt() and 0xFF) - 128) / 128.0f
        }
        _waveformFlow.value = normalized
    }

    private fun processFft(raw: ByteArray) {
        val bands = 64
        val magnitudes = FloatArray(bands)
        var instantEnergy = 0.0f

        for (i in 0 until bands) {
            if (i * 2 + 1 < raw.size) {
                val r = raw[i * 2].toFloat()
                val im = raw[i * 2 + 1].toFloat()
                val mag = sqrt(r * r + im * im) / 128.0f
                magnitudes[i] = mag.coerceIn(0f, 1f)

                // Focus on bass frequencies (bands 0-8 ~ 30Hz - 250Hz) for beat energy
                if (i in 0..8) {
                    instantEnergy += mag * mag
                }
            }
        }
        _fftFlow.value = magnitudes

        // Beat detection: Spectral energy threshold comparison
        var averageEnergy = 0f
        for (e in energyHistory) averageEnergy += e
        averageEnergy /= energyHistory.size

        val variance = calculateVariance(energyHistory, averageEnergy)
        // Sensitivity constant C based on variance
        val c = (-0.0025714f * variance) + 1.5142857f
        val isBeat = instantEnergy > (c * averageEnergy) && instantEnergy > 0.05f

        _beatDetectedFlow.value = isBeat

        energyHistory[historyIndex] = instantEnergy
        historyIndex = (historyIndex + 1) % energyHistory.size
    }

    private fun calculateVariance(history: FloatArray, mean: Float): Float {
        var v = 0f
        for (x in history) {
            val d = x - mean
            v += d * d
        }
        return v / history.size
    }

    fun release() {
        try {
            visualizer?.enabled = false
            visualizer?.release()
            visualizer = null
        } catch (e: Exception) {
            SonzaLogger.w("AudioAnalyzer", "Error releasing Visualizer: ${e.message}")
        }
    }
}
