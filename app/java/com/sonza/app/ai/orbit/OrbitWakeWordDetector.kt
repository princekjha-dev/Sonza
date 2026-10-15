package com.sonza.app.ai.orbit

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.content.Intent
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
 * 3. Clean microphone handover: AudioRecord is FULLY RELEASED before invoking keyword recognition
 *    to eliminate Android hardware resource contention. A fresh AudioRecord is created on resume.
 * 4. Recognizes primary wake phrase "Hey Orbit" and natural variations ("Okay Orbit", "Orbit").
 * 5. Compound utterance support: If user says "Hey Orbit, play some romantic songs",
 *    it extracts the wake word and command in one seamless interaction.
 * 6. Explicit lifecycle controls with zero battery/resource leaks.
 * 7. Automatic retry with backoff on transient errors to prevent permanent disconnection.
 */
class OrbitWakeWordDetector(
    private val context: Context,
    private val onWakeWordDetected: (command: String?) -> Unit
) {
    companion object {
        private const val TAG = "OrbitWakeWord"
        private const val MAX_RETRY_COUNT = 5
        private const val RETRY_BASE_DELAY_MS = 500L
        private const val VERIFICATION_TIMEOUT_MS = 4000L
        private const val MIC_RELEASE_SETTLE_MS = 60L
    }

    enum class State {
        STOPPED,
        STANDBY,
        VOICE_DETECTED,
        KEYWORD_VERIFYING,
        PAUSED
    }

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val mainHandler = Handler(Looper.getMainLooper())
    private var listeningJob: Job? = null

    private val _state = MutableStateFlow(State.STOPPED)
    val state: StateFlow<State> = _state.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private val isRunning = AtomicBoolean(false)
    private val isPaused = AtomicBoolean(false)
    private val isVerifying = AtomicBoolean(false)

    // Prevents duplicate evaluateUtterance calls from partial + final results
    private val hasTriggered = AtomicBoolean(false)

    private var speechRecognizer: SpeechRecognizer? = null
    private var verificationTimeoutJob: Job? = null
    private var consecutiveErrors = 0

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
     * Safe to call multiple times — guards against duplicates.
     */
    fun startListening(): Boolean {
        if (!hasPermission()) {
            Log.w(TAG, "startListening: no RECORD_AUDIO permission")
            _state.value = State.STOPPED
            return false
        }

        if (isRunning.get() && !isPaused.get()) {
            Log.d(TAG, "startListening: already running, skipping")
            return true
        }

        Log.d(TAG, "▶ Wake-word listener STARTED")
        isPaused.set(false)
        isRunning.set(true)
        consecutiveErrors = 0
        startVadLoop()
        return true
    }

    /**
     * Temporarily pause wake word detection (e.g. while Orbit assistant is actively listening or speaking).
     */
    fun pauseListening() {
        Log.d(TAG, "⏸ Wake-word listener PAUSED")
        isPaused.set(true)
        _state.value = State.PAUSED
        releaseAudioRecord()
        cancelVerification()
    }

    /**
     * Resumes wake word detection after Orbit assistant dismisses.
     * Always starts a fresh VAD loop to avoid stale AudioRecord state.
     */
    fun resumeListening() {
        if (!hasPermission()) {
            Log.w(TAG, "resumeListening: no permission")
            return
        }
        if (!isRunning.get()) {
            Log.d(TAG, "resumeListening: not running, calling startListening")
            startListening()
            return
        }

        Log.d(TAG, "▶ Wake-word listener RESUMED")
        isPaused.set(false)
        consecutiveErrors = 0
        startVadLoop()
    }

    /**
     * Completely stops and releases all microphone resources.
     */
    fun stopListening() {
        Log.d(TAG, "⏹ Wake-word listener STOPPED")
        isRunning.set(false)
        isPaused.set(false)
        _state.value = State.STOPPED
        listeningJob?.cancel()
        listeningJob = null
        verificationTimeoutJob?.cancel()
        verificationTimeoutJob = null
        releaseAudioRecord()
        cancelVerification()
    }

    /**
     * Starts a fresh VAD (Voice Activity Detection) loop.
     * Always creates a new AudioRecord to avoid stale hardware state.
     */
    private fun startVadLoop() {
        // Cancel any existing loop first
        listeningJob?.cancel()
        // Release any stale AudioRecord
        releaseAudioRecord()

        listeningJob = scope.launch {
            val sampleRate = 16000
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = (minBufSize * 2).coerceAtLeast(4096)

            try {
                val record = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (record.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord failed to initialize")
                    record.release()
                    handleVadError()
                    return@launch
                }

                audioRecord = record
                record.startRecording()
                _state.value = State.STANDBY
                Log.d(TAG, "🎤 Microphone STARTED (VAD loop)")

                val buffer = ShortArray(1024)
                while (isActive && isRunning.get()) {
                    if (isPaused.get()) {
                        // If paused, exit the loop cleanly — resumeListening() will start fresh
                        Log.d(TAG, "VAD loop: paused, exiting loop")
                        break
                    }

                    if (isVerifying.get()) {
                        // Should not happen — we release AudioRecord before verification
                        delay(100L)
                        continue
                    }

                    val read = try {
                        record.read(buffer, 0, buffer.size)
                    } catch (e: Exception) {
                        Log.w(TAG, "AudioRecord.read() failed: ${e.message}")
                        -1
                    }

                    if (read <= 0) {
                        if (read == AudioRecord.ERROR_INVALID_OPERATION || read == AudioRecord.ERROR_BAD_VALUE) {
                            Log.w(TAG, "AudioRecord error: $read, restarting VAD loop")
                            break // Will be restarted by error handler
                        }
                        delay(50L)
                        continue
                    }

                    // Compute RMS energy and zero-crossing rate
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

                    if (rms > speechThreshold && isHumanVocalZcr && !isPaused.get()) {
                        Log.d(TAG, "🗣 Voice activity DETECTED (rms=%.0f, threshold=%.0f, zcr=%d)".format(rms, speechThreshold, zeroCrossings))
                        _state.value = State.VOICE_DETECTED

                        // FULLY release AudioRecord so SpeechRecognizer gets exclusive mic access
                        releaseAudioRecord()

                        // Give audio HAL time to release hardware mic
                        delay(MIC_RELEASE_SETTLE_MS)

                        // Trigger keyword verification on Main thread (SpeechRecognizer requirement)
                        withContext(Dispatchers.Main) {
                            triggerKeywordVerification()
                        }

                        // After verification completes, this coroutine is done.
                        // A new VAD loop will be started by resumeVadAfterVerification().
                        return@launch
                    }

                    delay(25L)
                }
            } catch (e: CancellationException) {
                throw e // Don't suppress coroutine cancellation
            } catch (e: Exception) {
                Log.e(TAG, "VAD loop exception: ${e.message}")
                handleVadError()
            } finally {
                // If we exit the loop without having released (e.g., from isPaused break),
                // make sure to clean up
                releaseAudioRecord()
            }
        }
    }

    /**
     * Handles VAD errors with retry/backoff to prevent permanent disconnection.
     */
    private fun handleVadError() {
        releaseAudioRecord()

        if (!isRunning.get() || isPaused.get()) return

        consecutiveErrors++
        if (consecutiveErrors > MAX_RETRY_COUNT) {
            Log.e(TAG, "Too many consecutive VAD errors ($consecutiveErrors), stopping")
            _state.value = State.STOPPED
            return
        }

        val delayMs = RETRY_BASE_DELAY_MS * consecutiveErrors
        Log.w(TAG, "VAD error, retrying in ${delayMs}ms (attempt $consecutiveErrors/$MAX_RETRY_COUNT)")
        _state.value = State.STANDBY

        scope.launch {
            delay(delayMs)
            if (isRunning.get() && !isPaused.get() && !isVerifying.get()) {
                startVadLoop()
            }
        }
    }

    /**
     * Uses Android SpeechRecognizer to verify whether the detected voice activity
     * contains a wake phrase. Must be called on the Main thread.
     */
    private fun triggerKeywordVerification() {
        if (isVerifying.get() || isPaused.get()) {
            Log.d(TAG, "triggerKeywordVerification: skipped (verifying=${isVerifying.get()}, paused=${isPaused.get()})")
            resumeVadAfterVerification()
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "SpeechRecognizer not available on this device")
            resumeVadAfterVerification()
            return
        }

        isVerifying.set(true)
        hasTriggered.set(false)
        _state.value = State.KEYWORD_VERIFYING
        Log.d(TAG, "🔍 Keyword verification STARTED")

        try {
            // Always destroy previous recognizer
            destroySpeechRecognizer()

            speechRecognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }.apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val matchedText = matches?.firstOrNull().orEmpty()
                        Log.d(TAG, "🔍 Recognition result: \"$matchedText\"")
                        evaluateUtterance(matchedText)
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull().orEmpty()
                        if (partial.isNotBlank() && checkWakePhrase(partial) != null) {
                            Log.d(TAG, "🔍 Partial wake-word match: \"$partial\"")
                            evaluateUtterance(partial)
                        }
                    }

                    override fun onError(error: Int) {
                        val errorName = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "NO_MATCH"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "SPEECH_TIMEOUT"
                            SpeechRecognizer.ERROR_AUDIO -> "ERROR_AUDIO"
                            SpeechRecognizer.ERROR_NETWORK -> "NETWORK"
                            SpeechRecognizer.ERROR_CLIENT -> "CLIENT"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "PERMISSIONS"
                            else -> "UNKNOWN($error)"
                        }
                        Log.d(TAG, "🔍 Recognition error: $errorName (not a wake word)")
                        resumeVadAfterVerification()
                    }

                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "🔍 SpeechRecognizer ready for speech")
                    }
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        Log.d(TAG, "🔍 SpeechRecognizer end of speech")
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            }

            speechRecognizer?.startListening(intent)
            Log.d(TAG, "🔍 SpeechRecognizer.startListening() called")

            // Failsafe timeout — if recognition hangs, force resume
            verificationTimeoutJob?.cancel()
            verificationTimeoutJob = scope.launch {
                delay(VERIFICATION_TIMEOUT_MS)
                if (isVerifying.get()) {
                    Log.w(TAG, "🔍 Keyword verification TIMEOUT — resuming VAD")
                    withContext(Dispatchers.Main) {
                        resumeVadAfterVerification()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "triggerKeywordVerification exception: ${e.message}")
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

    /**
     * Evaluates whether the recognized utterance contains a wake phrase.
     * Uses hasTriggered flag to prevent duplicate callbacks from partial + final results.
     */
    private fun evaluateUtterance(rawText: String) {
        // Guard against duplicate triggers
        if (!hasTriggered.compareAndSet(false, true)) return

        val s = rawText.lowercase(Locale.ROOT).trim()
        val matchedPhrase = checkWakePhrase(s)

        if (matchedPhrase != null) {
            Log.d(TAG, "✅ \"Hey Orbit\" DETECTED in: \"$rawText\"")
            // Cancel timeout since we got a result
            verificationTimeoutJob?.cancel()
            isVerifying.set(false)

            // Clean up recognizer
            destroySpeechRecognizer()

            // Pause detection — the ViewModel will call resumeListening() when done
            isPaused.set(true)
            _state.value = State.PAUSED

            // Extract command if user spoke "Hey Orbit, <command>" in one breath
            val afterPhrase = s.substringAfter(matchedPhrase).trim()
            val command = afterPhrase.removePrefix(",").removePrefix("and").trim().ifBlank { null }

            onWakeWordDetected(command)
        } else {
            Log.d(TAG, "❌ No wake phrase in: \"$rawText\" — resuming VAD")
            // Reset trigger so final results can still match
            hasTriggered.set(false)
            resumeVadAfterVerification()
        }
    }

    /**
     * Cleans up verification state and starts a fresh VAD loop.
     * Always starts from scratch to avoid stale AudioRecord state.
     */
    private fun resumeVadAfterVerification() {
        if (!isVerifying.getAndSet(false) && _state.value != State.VOICE_DETECTED) return

        verificationTimeoutJob?.cancel()
        verificationTimeoutJob = null

        // Destroy the SpeechRecognizer on Main thread
        destroySpeechRecognizer()

        if (!isRunning.get() || isPaused.get()) {
            Log.d(TAG, "resumeVadAfterVerification: not running or paused, skipping")
            return
        }

        Log.d(TAG, "▶ Wake-word listener RESUMED after verification")
        consecutiveErrors = 0
        _state.value = State.STANDBY

        // Start a fresh VAD loop — this avoids all stale AudioRecord problems
        scope.launch {
            delay(MIC_RELEASE_SETTLE_MS) // Brief settle time
            if (isRunning.get() && !isPaused.get()) {
                startVadLoop()
            }
        }
    }

    private fun cancelVerification() {
        verificationTimeoutJob?.cancel()
        verificationTimeoutJob = null
        isVerifying.set(false)
        hasTriggered.set(false)
        destroySpeechRecognizer()
    }

    /**
     * Safely destroys the SpeechRecognizer. Must tolerate being called from any thread
     * by posting to Main if necessary.
     */
    private fun destroySpeechRecognizer() {
        val recognizer = speechRecognizer ?: return
        speechRecognizer = null
        try {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                recognizer.stopListening()
                recognizer.cancel()
                recognizer.destroy()
            } else {
                mainHandler.post {
                    try {
                        recognizer.stopListening()
                        recognizer.cancel()
                        recognizer.destroy()
                    } catch (_: Exception) {}
                }
            }
        } catch (_: Exception) {}
    }

    /**
     * Fully releases the AudioRecord, freeing the hardware microphone.
     * Safe to call multiple times.
     */
    private fun releaseAudioRecord() {
        val record = audioRecord ?: return
        audioRecord = null
        try {
            record.stop()
        } catch (_: Exception) {}
        try {
            record.release()
        } catch (_: Exception) {}
        Log.d(TAG, "🎤 Microphone RELEASED")
    }
}
