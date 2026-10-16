package com.sonza.app.ai.orbit

import android.os.SystemClock
import com.sonza.app.player.NativeSpatialAudio
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.*

/**
 * Real-time music audio analysis data for Orbit assistant.
 * Drives physical character reactions:
 * - [bass]: slight avatar scale / pulse (low frequencies)
 * - [mid]: headphones / body lateral movement & grooving
 * - [high]: soft glow intensity & top-end waveform responsiveness
 * - [beatPulse]: short controlled bounce on rhythmic beat onset
 * - [waveformBars]: 8-bar equalizer heights around the Orbit character (4 left, 4 right)
 */
data class OrbitMusicAudioData(
    val amplitude: Float = 0f,
    val bass: Float = 0f,
    val mid: Float = 0f,
    val high: Float = 0f,
    val beatPulse: Float = 0f,
    val waveformBars: FloatArray = FloatArray(8) { 0f }
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as OrbitMusicAudioData
        if (amplitude != other.amplitude) return false
        if (bass != other.bass) return false
        if (mid != other.mid) return false
        if (high != other.high) return false
        if (beatPulse != other.beatPulse) return false
        return waveformBars.contentEquals(other.waveformBars)
    }

    override fun hashCode(): Int {
        var result = amplitude.hashCode()
        result = 31 * result + bass.hashCode()
        result = 31 * result + mid.hashCode()
        result = 31 * result + high.hashCode()
        result = 31 * result + beatPulse.hashCode()
        result = 31 * result + waveformBars.contentHashCode()
        return result
    }
}

/**
 * High-performance real-time audio analyzer for driving Orbit's music reactions.
 * Samples native DSP RMS and peak readouts when available, with organic dynamic
 * harmonic synthesis fallback when audio is rendered outside native DSP buffers.
 */
class OrbitMusicAudioAnalyzer(
    private val nativeSpatialAudio: NativeSpatialAudio? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private val _audioData = MutableStateFlow(OrbitMusicAudioData())
    val audioData: StateFlow<OrbitMusicAudioData> = _audioData.asStateFlow()

    @Volatile
    private var isPlaying: Boolean = false

    private var analysisJob: Job? = null

    // Smoothing accumulators
    private var smoothedAmp = 0f
    private var smoothedBass = 0f
    private var smoothedMid = 0f
    private var smoothedHigh = 0f
    private var beatDecay = 0f
    private var runningAvgEnergy = 0.25f
    private var lastBeatTime = 0L

    private val bars = FloatArray(8) { 0f }

    fun setPlaying(playing: Boolean) {
        isPlaying = playing
        if (playing) {
            startAnalysisLoop()
        }
    }

    private fun startAnalysisLoop() {
        if (analysisJob?.isActive == true) return
        analysisJob = scope.launch {
            while (isActive) {
                if (!isPlaying) {
                    // Decay smoothly to zero
                    smoothedAmp *= 0.82f
                    smoothedBass *= 0.82f
                    smoothedMid *= 0.82f
                    smoothedHigh *= 0.82f
                    beatDecay *= 0.75f
                    for (i in bars.indices) {
                        bars[i] *= 0.80f
                    }
                    _audioData.value = OrbitMusicAudioData(
                        amplitude = smoothedAmp,
                        bass = smoothedBass,
                        mid = smoothedMid,
                        high = smoothedHigh,
                        beatPulse = beatDecay,
                        waveformBars = bars.clone()
                    )
                    if (smoothedAmp < 0.005f) {
                        break // Loop can rest until playback resumes
                    }
                } else {
                    val now = SystemClock.uptimeMillis()
                    val nativeRms = nativeSpatialAudio?.getRmsLevel() ?: 0f
                    val nativePeak = nativeSpatialAudio?.getPeakLevel() ?: 0f

                    val rawAmp: Float
                    val rawBass: Float
                    val rawMid: Float
                    val rawHigh: Float

                    if (nativeRms > 0.001f || nativePeak > 0.001f) {
                        // Real native DSP readout
                        rawAmp = (nativeRms * 2.8f).coerceIn(0.05f, 1.0f)
                        rawBass = (nativePeak * 1.5f).coerceIn(0f, 1.0f)
                        rawMid = ((nativeRms + nativePeak) * 0.75f).coerceIn(0f, 1.0f)
                        rawHigh = ((nativePeak - nativeRms * 0.5f).coerceAtLeast(0f) * 1.8f).coerceIn(0f, 1.0f)
                    } else {
                        // Organic dynamic rhythm synthesis based on musical harmonics
                        val t = now / 1000.0
                        // Dual polyrhythm envelopes simulating drums, bassline, and lead
                        val bassOsc = (sin(t * 7.54) * 0.5 + 0.5).toFloat() // ~120 BPM primary pulse
                        val subBassOsc = (sin(t * 3.77) * 0.5 + 0.5).toFloat() // 60 BPM downbeat
                        val midOsc = (cos(t * 11.3) * 0.5 + 0.5).toFloat() // Groove syncopation
                        val highOsc = (sin(t * 18.85) * 0.5 + 0.5).toFloat() // Hi-hats/cymbals

                        rawBass = (bassOsc * 0.7f + subBassOsc * 0.3f).coerceIn(0f, 1f)
                        rawMid = (midOsc * 0.65f + bassOsc * 0.35f).coerceIn(0f, 1f)
                        rawHigh = (highOsc * 0.75f + midOsc * 0.25f).coerceIn(0f, 1f)
                        rawAmp = (rawBass * 0.45f + rawMid * 0.35f + rawHigh * 0.20f).coerceIn(0.12f, 0.95f)
                    }

                    // Smooth values with responsive spring interpolation
                    smoothedAmp = smoothedAmp * 0.65f + rawAmp * 0.35f
                    smoothedBass = smoothedBass * 0.55f + rawBass * 0.45f
                    smoothedMid = smoothedMid * 0.60f + rawMid * 0.40f
                    smoothedHigh = smoothedHigh * 0.65f + rawHigh * 0.35f

                    // Beat onset detection
                    runningAvgEnergy = runningAvgEnergy * 0.94f + smoothedAmp * 0.06f
                    if (smoothedBass > runningAvgEnergy * 1.35f && (now - lastBeatTime) > 380L) {
                        beatDecay = 1.0f
                        lastBeatTime = now
                    } else {
                        beatDecay *= 0.85f
                    }

                    // Generate 8 balanced stereo-mirrored equalizer bars
                    // Left channel: bars 0 (high), 1 (mid-high), 2 (mid), 3 (bass)
                    // Right channel: bars 4 (bass), 5 (mid), 6 (mid-high), 7 (high)
                    val t = now / 1000.0
                    val jitter1 = (sin(t * 14.2) * 0.08f).toFloat()
                    val jitter2 = (cos(t * 17.5) * 0.08f).toFloat()

                    bars[0] = (smoothedHigh * 0.85f + jitter1).coerceIn(0.12f, 1.0f)
                    bars[1] = (smoothedMid * 0.90f + jitter2).coerceIn(0.18f, 1.0f)
                    bars[2] = (smoothedMid * 0.80f + smoothedBass * 0.25f).coerceIn(0.22f, 1.0f)
                    bars[3] = (smoothedBass * 1.05f + beatDecay * 0.25f).coerceIn(0.28f, 1.0f)

                    bars[4] = (smoothedBass * 1.02f + beatDecay * 0.25f).coerceIn(0.28f, 1.0f)
                    bars[5] = (smoothedMid * 0.82f + smoothedBass * 0.22f).coerceIn(0.22f, 1.0f)
                    bars[6] = (smoothedMid * 0.88f - jitter1).coerceIn(0.18f, 1.0f)
                    bars[7] = (smoothedHigh * 0.82f - jitter2).coerceIn(0.12f, 1.0f)

                    _audioData.value = OrbitMusicAudioData(
                        amplitude = smoothedAmp,
                        bass = smoothedBass,
                        mid = smoothedMid,
                        high = smoothedHigh,
                        beatPulse = beatDecay,
                        waveformBars = bars.clone()
                    )
                }
                delay(16L) // ~60 FPS update rate
            }
        }
    }
}
