package com.sonza.app.ai.orbit

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Programmatic Sound Design System for Orbit AI Assistant.
 * Generates original, subtle, and modern acoustic cues with zero latency.
 * Does not depend on external audio assets or Google Assistant sounds.
 */
object OrbitSoundManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private const val SAMPLE_RATE = 44100

    private val activationPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(587.33, 880.00), // D5 -> A5
            durationsMs = doubleArrayOf(60.0, 90.0),
            peakVolume = 0.55
        )
    }

    private val listeningPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(523.25), // C5 gentle ping
            durationsMs = doubleArrayOf(80.0),
            peakVolume = 0.45
        )
    }

    private val commandReceivedPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(783.99), // G5 crisp blip
            durationsMs = doubleArrayOf(50.0),
            peakVolume = 0.40
        )
    }

    private val successPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(523.25, 659.25, 783.99), // C5 -> E5 -> G5
            durationsMs = doubleArrayOf(50.0, 50.0, 110.0),
            peakVolume = 0.50
        )
    }

    private val errorPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(440.00, 329.63), // A4 -> E4 gentle descending
            durationsMs = doubleArrayOf(70.0, 90.0),
            peakVolume = 0.40
        )
    }

    private val wakeWordPcm: ByteArray by lazy {
        generateChime(
            frequencies = doubleArrayOf(659.25, 987.77), // E5 -> B5 bright chime
            durationsMs = doubleArrayOf(60.0, 120.0),
            peakVolume = 0.60
        )
    }

    fun playActivation() {
        playPcm(activationPcm)
    }

    fun playListening() {
        playPcm(listeningPcm)
    }

    fun playCommandReceived() {
        playPcm(commandReceivedPcm)
    }

    fun playSuccess() {
        playPcm(successPcm)
    }

    fun playError() {
        playPcm(errorPcm)
    }

    fun playWakeWordDetected() {
        playPcm(wakeWordPcm)
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
                val durationMs = (pcmData.size / 2 * 1000L) / SAMPLE_RATE + 60L
                kotlinx.coroutines.delay(durationMs)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio cue errors if device audio is busy
            }
        }
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
                    peakVolume * exp(-4.2 * progress)
                }

                // Fundamental sine wave + subtle 2nd harmonic for rich bell-like acoustic timbre
                val fundamental = sin(2.0 * PI * freq * t)
                val overtone = 0.22 * sin(2.0 * PI * (freq * 2.0) * t)
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
