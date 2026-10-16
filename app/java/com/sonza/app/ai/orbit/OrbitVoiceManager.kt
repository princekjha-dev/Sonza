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

/**
 * Speech Recognition Manager for Orbit Assistant.
 * Implements real-time streaming speech recognition with live RMS audio reactivity for the animated orb,
 * live partial speech transcription, and comprehensive error resilience.
 *
 * Requirements:
 * - Completely silences Google Assistant / system recognition sounds (activation bloops, end chimes, error beeps)
 *   by muting system sonification streams during speech engine start and transition points.
 * - Idempotent start/stop: prevents duplicate SpeechRecognizer instances or listener leaks.
 */
class OrbitVoiceManager(
    private val context: Context
) {
    companion object {
        private const val TAG = "OrbitVoiceMgr"
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isSystemMuted = false

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private var onPartialResultCallback: ((String) -> Unit)? = null
    private var onResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun silenceSystemSounds() {
        try {
            if (!isSystemMuted && audioManager != null) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
                audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
                isSystemMuted = true
            }
        } catch (_: Exception) {}
    }

    private fun restoreSystemSounds() {
        try {
            if (isSystemMuted && audioManager != null) {
                audioManager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0)
                audioManager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0)
                isSystemMuted = false
            }
        } catch (_: Exception) {}
    }

    fun startListening(
        onPartialResult: ((String) -> Unit)? = null,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasRecordAudioPermission()) {
            Log.w(TAG, "startListening: no permission")
            onError("Microphone permission required")
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "startListening: recognition not available")
            onError("Speech recognition not available on device")
            return
        }

        // Clean up previous instance cleanly without duplicate callbacks
        stopListening()

        onPartialResultCallback = onPartialResult
        onResultCallback = onResult
        onErrorCallback = onError
        _transcription.value = "Listening..."
        _audioLevel.value = 0f

        try {
            Log.d(TAG, "▶ Creating SpeechRecognizer for command listening")
            
            // Mute system sounds before startListening to eliminate Google Assistant start bloop/chime
            silenceSystemSounds()

            speechRecognizer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && SpeechRecognizer.isOnDeviceRecognitionAvailable(context)) {
                SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
            } else {
                SpeechRecognizer.createSpeechRecognizer(context)
            }.apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        Log.d(TAG, "SpeechRecognizer: ready for speech")
                        _isListening.value = true
                        // Restore system volume after the start sound window has passed
                        mainHandler.postDelayed({ restoreSystemSounds() }, 300L)
                    }

                    override fun onBeginningOfSpeech() {
                        Log.d(TAG, "SpeechRecognizer: beginning of speech")
                        _transcription.value = "Listening..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB to 0.0 .. 1.0 range for Avatar animation
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        Log.d(TAG, "SpeechRecognizer: end of speech")
                        // Mute system sounds to eliminate Google Assistant end chime
                        silenceSystemSounds()
                        _isListening.value = false
                        _audioLevel.value = 0f
                    }

                    override fun onError(error: Int) {
                        // Mute system sounds to eliminate Google Assistant error beep
                        silenceSystemSounds()
                        _isListening.value = false
                        _audioLevel.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NETWORK -> "Network required for speech"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            SpeechRecognizer.ERROR_CLIENT -> "Speech recognizer busy"
                            else -> "Recognition failed ($error)"
                        }
                        Log.w(TAG, "SpeechRecognizer error: $msg (code=$error)")
                        mainHandler.postDelayed({ restoreSystemSounds() }, 200L)
                        onErrorCallback?.invoke(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioLevel.value = 0f
                        mainHandler.postDelayed({ restoreSystemSounds() }, 200L)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrBlank()) {
                            Log.d(TAG, "SpeechRecognizer result: \"$text\"")
                            _transcription.value = text
                            onResultCallback?.invoke(text)
                        } else {
                            Log.d(TAG, "SpeechRecognizer: empty result")
                            onErrorCallback?.invoke("Couldn't hear clearly")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim()
                        if (!text.isNullOrBlank()) {
                            _transcription.value = text
                            onPartialResultCallback?.invoke(text)
                        }
                    }

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
            _isListening.value = true
            Log.d(TAG, "▶ SpeechRecognizer.startListening() called (silenced system beeps)")
        } catch (e: Exception) {
            restoreSystemSounds()
            _isListening.value = false
            Log.e(TAG, "startListening exception: ${e.message}")
            onErrorCallback?.invoke("Could not start speech recognition: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
            // Ignore clean up errors
        } finally {
            speechRecognizer = null
            _isListening.value = false
            _audioLevel.value = 0f
            restoreSystemSounds()
            Log.d(TAG, "⏹ Command listening STOPPED")
        }
    }
}
