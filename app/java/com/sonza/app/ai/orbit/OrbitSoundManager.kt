package com.sonza.app.ai.orbit

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Programmatic Sonic Identity System for Sonza's Orbit AI Assistant.
 * Generates original, futuristic, zero-latency acoustic cues tailored for a premium music assistant.
 *
 * Requirements:
 * - Orbit activation: Short futuristic Sonza chime + soft rising tone + small confirmation pulse (~0.65s).
 * - Distinct subtle sounds for: Orbit activated, Listening started, Command understood, Success, Error, Orbit closed.
 * - Respects current music volume and audio focus.
 */
object OrbitSoundManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private const val SAMPLE_RATE = 44100

    /**
     * Orbit Wake / Activation:
     * 1. Soft acoustic bell base (523.25 Hz, C5, 90ms)
     * 2. Futuristic glissando sweep rising from 659.25 Hz (E5) up to 1046.50 Hz (C6) (220ms)
     * 3. High crystal confirmation pulse (1318.51 Hz, E6) with harmonic shimmer (340ms)
     * Total ~650ms.
     */
    private val activationPcm: ByteArray by lazy {
        generateActivationChime(
            baseFreq = 523.25,
            sweepStart = 659.25,
            sweepEnd = 1046.50,
            confirmFreq = 1318.51,
            peakVolume = 0.52
        )
    }

    /**
     * Listening Started:
     * Subtle dual-tone ping (880 Hz -> 1174.66 Hz, 120ms total) with soft attack and decay.
     */
    private val listeningPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(880.00, 1174.66),
            durationsMs = doubleArrayOf(50.0, 70.0),
            peakVolume = 0.38
        )
    }

    /**
     * Command Understood:
     * Crisp affirmative harmonic pulse (987.77 Hz -> 1318.51 Hz, 140ms).
     */
    private val commandUnderstoodPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(987.77, 1318.51),
            durationsMs = doubleArrayOf(40.0, 100.0),
            peakVolume = 0.42
        )
    }

    /**
     * Success:
     * Bright uplifting 3-note major arpeggio (C5 -> E5 -> G5, 260ms).
     */
    private val successPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(523.25, 659.25, 783.99),
            durationsMs = doubleArrayOf(50.0, 50.0, 160.0),
            peakVolume = 0.45
        )
    }

    /**
     * Error:
     * Soft, low harmonic descending tone (440 Hz -> 349.23 Hz -> 261.63 Hz, 240ms).
     */
    private val errorPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(440.00, 349.23, 261.63),
            durationsMs = doubleArrayOf(70.0, 80.0, 90.0),
            peakVolume = 0.35
        )
    }

    /**
     * Orbit Closed / Standby:
     * Gentle descending fade pulse (783.99 Hz -> 587.33 Hz, 150ms).
     */
    private val closedPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(783.99, 587.33),
            durationsMs = doubleArrayOf(60.0, 90.0),
            peakVolume = 0.32
        )
    }

    fun playActivation() {
        playPcm(activationPcm)
    }

    fun playWakeWordDetected() {
        playPcm(activationPcm)
    }

    fun playListening() {
        playPcm(listeningPcm)
    }

    fun playCommandReceived() {
        playPcm(commandUnderstoodPcm)
    }

    fun playCommandUnderstood() {
        playPcm(commandUnderstoodPcm)
    }

    fun playSuccess() {
        playPcm(successPcm)
    }

    fun playError() {
        playPcm(errorPcm)
    }

    fun playClosed() {
        playPcm(closedPcm)
    }

    private fun playPcm(pcmData: ByteArray) {
        scope.launch {
            try {
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcmData.size)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcmData, 0, pcmData.size)
                audioTrack.play()

                // Clean up after playback finishes
                val durationMs = (pcmData.size / 2 * 1000L) / SAMPLE_RATE + 80L
                delay(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio cue errors if device audio is busy
            }
        }
    }

    /**
     * Futuristic Multi-stage Orbit Activation Chime:
     * - Stage 1: Soft resonant bell base
     * - Stage 2: Glissando upward frequency sweep
     * - Stage 3: Crystal confirmation pulse with octave harmonic
     */
    private fun generateActivationChime(
        baseFreq: Double,
        sweepStart: Double,
        sweepEnd: Double,
        confirmFreq: Double,
        peakVolume: Double
    ): ByteArray {
        val stage1Samples = (SAMPLE_RATE * 0.09).toInt()
        val stage2Samples = (SAMPLE_RATE * 0.22).toInt()
        val stage3Samples = (SAMPLE_RATE * 0.34).toInt()
        val totalSamples = stage1Samples + stage2Samples + stage3Samples

        val pcm = ByteArray(totalSamples * 2)
        var sampleIndex = 0

        // Stage 1: Soft warm bell tone
        for (s in 0 until stage1Samples) {
            val t = s.toDouble() / SAMPLE_RATE
            val progress = s.toDouble() / stage1Samples
            val env = peakVolume * 0.7 * exp(-2.5 * progress)
            val wave = sin(2.0 * PI * baseFreq * t) + 0.18 * sin(2.0 * PI * (baseFreq * 2.0) * t)
            val sampleVal = (wave * env * 32767.0).toInt().coerceIn(-32768, 32767)
            val bytePos = (sampleIndex + s) * 2
            pcm[bytePos] = (sampleVal and 0xFF).toByte()
            pcm[bytePos + 1] = ((sampleVal shr 8) and 0xFF).toByte()
        }
        sampleIndex += stage1Samples

        // Stage 2: Futuristic rising glissando sweep
        var phase = 0.0
        for (s in 0 until stage2Samples) {
            val progress = s.toDouble() / stage2Samples
            // Exponential frequency glide from sweepStart to sweepEnd
            val instantFreq = sweepStart * Math.pow(sweepEnd / sweepStart, progress)
            phase += 2.0 * PI * instantFreq / SAMPLE_RATE
            val env = peakVolume * (0.6 + 0.4 * progress)
            val wave = sin(phase) + 0.15 * sin(phase * 2.0)
            val sampleVal = (wave * env * 32767.0).toInt().coerceIn(-32768, 32767)
            val bytePos = (sampleIndex + s) * 2
            pcm[bytePos] = (sampleVal and 0xFF).toByte()
            pcm[bytePos + 1] = ((sampleVal shr 8) and 0xFF).toByte()
        }
        sampleIndex += stage2Samples

        // Stage 3: Crystal confirmation pulse with pleasant exponential decay
        for (s in 0 until stage3Samples) {
            val t = s.toDouble() / SAMPLE_RATE
            val progress = s.toDouble() / stage3Samples
            val env = peakVolume * exp(-4.5 * progress)
            val wave = sin(2.0 * PI * confirmFreq * t) +
                       0.22 * sin(2.0 * PI * (confirmFreq * 2.0) * t) +
                       0.08 * sin(2.0 * PI * (confirmFreq * 0.5) * t)
            val sampleVal = (wave * env * 32767.0).toInt().coerceIn(-32768, 32767)
            val bytePos = (sampleIndex + s) * 2
            pcm[bytePos] = (sampleVal and 0xFF).toByte()
            pcm[bytePos + 1] = ((sampleVal shr 8) and 0xFF).toByte()
        }

        return pcm
    }

    /**
     * Synthesizes smooth, natural-sounding chimes with harmonic warmth and exponential ADSR envelopes.
     */
    private fun generateChime(
        frequencies: DoubleArray,
        durationsMs: DoubleArray,
        peakVolume: Double
    ): ByteArray {
        var totalSamples = 0
        for (d in durationsMs) {
            totalSamples += (SAMPLE_RATE * (d / 1000.0)).toInt()
        }

        val pcm = ByteArray(totalSamples * 2)
        var sampleIndex = 0

        for (i in frequencies.indices) {
            val freq = frequencies[i]
            val durationMs = durationsMs[i]
            val toneSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()

            for (s in 0 until toneSamples) {
                val t = s.toDouble() / SAMPLE_RATE
                val progress = s.toDouble() / toneSamples

                // Exponential decay envelope with soft 5ms attack
                val attackTime = 0.005
                val envelope = if (t < attackTime) {
                    (t / attackTime) * peakVolume
                } else {
                    peakVolume * exp(-4.0 * progress)
                }

                // Fundamental sine wave + subtle 2nd harmonic for rich bell-like acoustic timbre
                val fundamental = sin(2.0 * PI * freq * t)
                val overtone = 0.20 * sin(2.0 * PI * (freq * 2.0) * t)
                val sampleValue = ((fundamental + overtone) * envelope * 32767.0).toInt().coerceIn(-32768, 32767)

                val bytePos = (sampleIndex + s) * 2
                if (bytePos + 1 < pcm.size) {
                    pcm[bytePos] = (sampleValue and 0xFF).toByte()
                    pcm[bytePos + 1] = ((sampleValue shr 8) and 0xFF).toByte()
                }
            }
            sampleIndex += toneSamples
        }

        return pcm
    }
}
