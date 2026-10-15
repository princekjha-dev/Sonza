package com.sonza.app.ai.orbit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.sqrt

/**
 * Robust Android Continuous Wake-Word Detection Engine for Sonza's Orbit Assistant.
 *
 * Architecture & Compliance:
 * 1. Low-power Voice Activity Detection (VAD) using AudioRecord with VOICE_RECOGNITION
 *    audio source to activate hardware Acoustic Echo Cancellation (AEC) and Noise Suppression.
 * 2. Adaptive speech-band energy filter to distinguish vocal bursts even while music is playing.
 * 3. Clean microphone handover: AudioRecord is paused before invoking keyword recognition
 *    to eliminate Android hardware resource contention.
 * 4. Recognizes primary wake phrase "Hey Orbit" and natural variations ("Okay Orbit", "Orbit").
 * 5. Compound utterance support: If user says "Hey Orbit, play some romantic songs",
 *    it extracts the wake word and command in one seamless interaction.
 * 6. Explicit lifecycle controls with zero battery/resource leaks.
 */
class OrbitWakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: (command: String?) -> Unit
) {
    enum class State {
        STOPPED,
        STANDBY,
        VOICE_DETECTED,
        KEYWORD_VERIFYING,
        PAUSED
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var listeningJob: Job? = null

    private val _state = MutableStateFlow(State.STOPPED)
    val state: StateFlow<State> = _state.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private val isRecording = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)

    private var speechRecognizer: SpeechRecognizer? = null
    private val isVerifying = AtomicBoolean(false)

    // Moving average for adaptive ambient noise floor
    private var noiseFloorRms = 400.0

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Starts continuous wake word detection in standby mode.
     */
    fun startListening(): Boolean {
        if (!hasPermission()) {
            _state.value = State.STOPPED
            return false
        }

        if (isRecording.get()) return true

        isPaused.set(false)
        _state.value = State.STANDBY
        startVadLoop()
        return true
    }

    /**
     * Temporarily pause wake word detection (e.g. while Orbit assistant is actively listening or speaking).
     */
    fun pauseListening() {
        isPaused.set(true)
        _state.value = State.PAUSED
        try {
            if (isRecording.get()) {
                audioRecord?.stop()
            }
        } catch (_: Exception) {}
        cancelVerification()
    }

    /**
     * Resumes wake word detection after Orbit assistant dismisses.
     */
    fun resumeListening() {
        if (!hasPermission()) return
        isPaused.set(false)
        _state.value = State.STANDBY
        try {
            if (audioRecord?.state == AudioRecord.STATE_INITIALIZED && !isVerifying.get()) {
                audioRecord?.startRecording()
            } else if (!isRecording.get()) {
                startVadLoop()
            }
        } catch (_: Exception) {
            startVadLoop()
        }
    }

    /**
     * Completely stops and releases all microphone resources.
     */
    fun stopListening() {
        isPaused.set(false)
        isRecording.set(false)
        _state.value = State.STOPPED
        listeningJob?.cancel()
        releaseAudioRecord()
        cancelVerification()
    }

    private fun startVadLoop() {
        listeningJob?.cancel()
        listeningJob = scope.launch {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = (minBufSize * 2).coerceAtLeast(4096)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    _state.value = State.STOPPED
                    return@launch
                }

                audioRecord?.startRecording()
                isRecording.set(true)
                _state.value = State.STANDBY

                val buffer = ShortArray(1024)
                while (isActive && isRecording.get()) {
                    if (isPaused.get() || isVerifying.get()) {
                        delay(120L)
                        continue
                    }

                    val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                    if (read > 0) {
                        var sum = 0.0
                        var zeroCrossings = 0
                        for (i in 0 until read) {
                            sum += buffer[i] * buffer[i]
                            if (i > 0 && ((buffer[i] >= 0 && buffer[i - 1] < 0) || (buffer[i] < 0 && buffer[i - 1] >= 0))) {
                                zeroCrossings++
                            }
                        }
                        val rms = sqrt(sum / read)

                        // Smoothly track background noise floor
                        noiseFloorRms = (noiseFloorRms * 0.95) + (rms * 0.05)

                        // Voice activity threshold: significant rise above noise floor + human speech zero crossing rate
                        val speechThreshold = (noiseFloorRms * 2.2).coerceIn(650.0, 3200.0)
                        val isHumanVocalZcr = zeroCrossings in 15..280

                        if (rms > speechThreshold && isHumanVocalZcr && !isVerifying.get() && !isPaused.get()) {
                            _state.value = State.VOICE_DETECTED
                            // Pause AudioRecord immediately so SpeechRecognizer can access the microphone cleanly
                            try {
                                audioRecord?.stop()
                            } catch (_: Exception) {}

                            // Give audio hal 40ms to release hardware mic
                            delay(40L)

                            withContext(Dispatchers.Main) {
                                triggerKeywordVerification()
                            }
                        }
                    }
                    delay(25L)
                }
            } catch (_: Exception) {
                _state.value = State.STOPPED
            } finally {
                releaseAudioRecord()
            }
        }
    }

    private fun triggerKeywordVerification() {
        if (isVerifying.get() || isPaused.get()) return
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            resumeVadAfterVerification()
            return
        }

        isVerifying.set(true)
        _state.value = State.KEYWORD_VERIFYING

        try {
            speechRecognizer?.destroy()
            speechRecognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }.apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val matchedText = matches?.firstOrNull().orEmpty()
                        evaluateUtterance(matchedText)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull().orEmpty()
                        if (checkWakePhrase(partial) != null) {
                            // Instant match from streaming partial results!
                            evaluateUtterance(partial)
                        }
                    }

                    override fun onError(error: Int) {
                        resumeVadAfterVerification()
                    }

                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
            }

            speechRecognizer?.startListening(intent)

            // Failsafe timeout for keyword pass (3.5s max)
            scope.launch {
                delay(3500L)
                if (isVerifying.get()) {
                    withContext(Dispatchers.Main) {
                        resumeVadAfterVerification()
                    }
                }
            }
        } catch (_: Exception) {
            resumeVadAfterVerification()
        }
    }

    private fun checkWakePhrase(text: String): String? {
        val s = text.lowercase(Locale.ROOT).trim()
        val wakePhrases = listOf("hey orbit", "okay orbit", "ok orbit", "orbit")
        for (phrase in wakePhrases) {
            if (s.startsWith(phrase) || s.contains(phrase)) {
                return phrase
            }
        }
        return null
    }

    private fun evaluateUtterance(rawText: String) {
        val s = rawText.lowercase(Locale.ROOT).trim()
        val matchedPhrase = checkWakePhrase(s)

        if (matchedPhrase != null) {
            // Wake word matched!
            isVerifying.set(false)
            pauseListening()

            // Extract command if user spoke "Hey Orbit, <command>" in one breath
            val afterPhrase = s.substringAfter(matchedPhrase).trim()
            val command = afterPhrase.removePrefix(",").removePrefix("and").trim().ifBlank { null }

            onWakeWordDetected(command)
        } else {
            // Not a wake word utterance
            resumeVadAfterVerification()
        }
    }

    private fun resumeVadAfterVerification() {
        if (!isVerifying.getAndSet(false)) return
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null

        if (!isPaused.get() && isRecording.get()) {
            _state.value = State.STANDBY
            scope.launch {
                delay(40L)
                try {
                    if (!isPaused.get() && isRecording.get()) {
                        audioRecord?.startRecording()
                    }
                } catch (_: Exception) {}
            }
        }
    }

    private fun cancelVerification() {
        isVerifying.set(false)
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
    }

    private fun releaseAudioRecord() {
        try {
            if (isRecording.get()) {
                audioRecord?.stop()
            }
            audioRecord?.release()
        } catch (_: Exception) {}
        audioRecord = null
        isRecording.set(false)
    }
}
