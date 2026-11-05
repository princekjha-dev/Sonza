package com.sonza.app.ai.orbit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger

/**
 * Speech Recognition Manager for Orbit Assistant.
 *
 * Requirements:
 * - Thread Safety: Android SpeechRecognizer must ALWAYS be created, started, stopped,
 *   and destroyed strictly on the application's Main/UI thread. All calls are dispatched
 *   through the Main Looper.
 * - Idempotency & Clean Session Management: Prevents multiple simultaneous recognition sessions.
 *   Safely stops/cancels/releases any previous recognizer before starting a new one.
 * - Anti-Looping: Avoids recursive restart loops and does not continuously recreate recognizers on errors.
 * - Sound Silencing: Silences Google Assistant / system sounds (activation bloop, end chime, error beep)
 *   by muting AudioManager.STREAM_SYSTEM during recognition transitions without SecurityException.
 * - Error Hygiene: NEVER surfaces raw exceptions, stack traces, or technical details to the user.
 */
class OrbitVoiceManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "OrbitVoiceMgr"
        const val GENERIC_HEAR_ERROR = "Orbit couldn't hear that. Try again."
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isSystemMuted = false

    private var speechRecognizer: SpeechRecognizer? = null
    private val activeSessionId = AtomicInteger(0)

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private var onPartialResultCallback: ((String) -> Unit)? = null
    private var onResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    private fun runOnMainThread(action: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            action()
        } else {
            mainHandler.post(action)
        }
    }

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun silenceSystemSounds() {
        try {
            if (!isSystemMuted && audioManager != null) {
                // Mute STREAM_SYSTEM only. Never touch STREAM_NOTIFICATION to prevent SecurityException on Android 7+
                audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
                isSystemMuted = true
            }
        } catch (t: Throwable) {
            Log.d(TAG, "silenceSystemSounds: non-fatal volume mute failure: ${t.message}")
        }
    }

    private fun restoreSystemSounds() {
        try {
            if (isSystemMuted && audioManager != null) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
                isSystemMuted = false
            }
        } catch (t: Throwable) {
            Log.d(TAG, "restoreSystemSounds: non-fatal volume restore failure: ${t.message}")
        }
    }

    private fun cleanupRecognizerInternal() {
        try {
            speechRecognizer?.setRecognitionListener(null)
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Throwable) {
            // Ignore cleanup exceptions
        } finally {
            speechRecognizer = null
        }
    }

    /**
     * Starts voice recognition. Fully dispatched on the Main Looper.
     */
    fun startListening(
        onPartialResult: ((String) -> Unit)? = null,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        runOnMainThread {
            if (!hasRecordAudioPermission()) {
                Log.w(TAG, "startListening: microphone permission not granted")
                onError("Microphone permission required")
                return@runOnMainThread
            }

            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                Log.w(TAG, "startListening: SpeechRecognizer not available on device")
                onError("Speech recognition not available on device")
                return@runOnMainThread
            }

            val sessionId = activeSessionId.incrementAndGet()

            // 1. Safely stop and release previous session
            cleanupRecognizerInternal()

            onPartialResultCallback = onPartialResult
            onResultCallback = onResult
            onErrorCallback = onError
            _transcription.value = "Listening…"
            _audioLevel.value = 0f

            try {
                // 2. Mute system sounds before startListening to eliminate Google Assistant start chime
                silenceSystemSounds()

                val recognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
                    SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
                } else {
                    SpeechRecognizer.createSpeechRecognizer(context)
                }

                recognizer.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        if (sessionId != activeSessionId.get()) return
                        Log.d(TAG, "SpeechRecognizer: ready for speech")
                        _isListening.value = true
                        // Restore system volume after the start chime window has elapsed
                        mainHandler.postDelayed({ restoreSystemSounds() }, 300L)
                    }

                    override fun onBeginningOfSpeech() {
                        if (sessionId != activeSessionId.get()) return
                        Log.d(TAG, "SpeechRecognizer: beginning of speech")
                        _transcription.value = "Listening…"
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        if (sessionId != activeSessionId.get()) return
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        if (sessionId != activeSessionId.get()) return
                        Log.d(TAG, "SpeechRecognizer: end of speech")
                        // Mute system sounds to eliminate Google Assistant end chime
                        silenceSystemSounds()
                        _isListening.value = false
                        _audioLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        if (sessionId != activeSessionId.get()) return
                        silenceSystemSounds()
                        _isListening.value = false
                        _audioLevel.value = 0f
                        mainHandler.postDelayed({ restoreSystemSounds() }, 250L)

                        // Safely cleanup the current recognizer so it does not hang
                        cleanupRecognizerInternal()

                        val userMessage = when (error) {
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_NETWORK -> "Network connection needed"
                            SpeechRecognizer.ERROR_NO_MATCH,
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> GENERIC_HEAR_ERROR
                            SpeechRecognizer.ERROR_CLIENT,
                            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> GENERIC_HEAR_ERROR
                            else -> GENERIC_HEAR_ERROR
                        }

                        Log.w(TAG, "SpeechRecognizer error code=$error -> userMessage=\"$userMessage\"")
                        onErrorCallback?.invoke(userMessage)
                    }

                    override fun onResults(results: Bundle?) {
                        if (sessionId != activeSessionId.get()) return
                        _isListening.value = false
                        _audioLevel.value = 0f
                        mainHandler.postDelayed({ restoreSystemSounds() }, 200L)

                        cleanupRecognizerInternal()

                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrBlank()) {
                            Log.d(TAG, "SpeechRecognizer result: \"$text\"")
                            _transcription.value = text
                            onResultCallback?.invoke(text)
                        } else {
                            Log.d(TAG, "SpeechRecognizer: empty result")
                            onErrorCallback?.invoke(GENERIC_HEAR_ERROR)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        if (sessionId != activeSessionId.get()) return
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrBlank()) {
                            _transcription.value = text
                            onPartialResultCallback?.invoke(text)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                speechRecognizer = recognizer

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toLanguageTag())
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
                    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1200L)
                    putExtra("android.speech.extra.DICTATION_MODE", true)
                    putExtra("android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS", 2000L)
                }

                recognizer.startListening(intent)
                _isListening.value = true
                Log.d(TAG, "▶ SpeechRecognizer.startListening() dispatched safely on Main thread (session $sessionId)")
            } catch (t: Throwable) {
                restoreSystemSounds()
                _isListening.value = false
                _audioLevel.value = 0f
                cleanupRecognizerInternal()
                Log.e(TAG, "startListening error", t)
                // NEVER emit raw technical exceptions to UI
                onErrorCallback?.invoke(GENERIC_HEAR_ERROR)
            }
        }
    }

    /**
     * Stops and cancels speech recognition safely on the Main Looper.
     */
    fun stopListening() {
        runOnMainThread {
            activeSessionId.incrementAndGet()
            cleanupRecognizerInternal()
            _isListening.value = false
            _audioLevel.value = 0f
            restoreSystemSounds()
            Log.d(TAG, "⏹ Command listening STOPPED")
        }
    }

    /**
     * Destroys recognizer and clears all active listeners.
     */
    fun destroy() {
        runOnMainThread {
            activeSessionId.incrementAndGet()
            onPartialResultCallback = null
            onResultCallback = null
            onErrorCallback = null
            cleanupRecognizerInternal()
            _isListening.value = false
            _audioLevel.value = 0f
            restoreSystemSounds()
            Log.d(TAG, "Destroyed OrbitVoiceManager")
        }
    }
}
