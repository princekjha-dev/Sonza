package com.sonza.app.ai.orbit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Speech Recognition Manager for Orbit AI.
 * Implements TAP ORBIT = IMMEDIATELY LISTEN with live RMS audio reactivity for avatar animation.
 */
@Singleton
class OrbitVoiceManager @Inject constructor(
    private val context: Context
) {
    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription: StateFlow<String> = _transcription.asStateFlow()

    private var onResultCallback: ((String) -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startListening(
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasRecordAudioPermission()) {
            onError("Microphone permission required")
            return
        }

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onError("Speech recognition not available on device")
            return
        }

        stopListening()

        onResultCallback = onResult
        onErrorCallback = onError
        _transcription.value = "Listening..."
        _audioLevel.value = 0f

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {
                        _transcription.value = "Listening..."
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS dB to 0.0 .. 1.0 range for Avatar animation
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0f, 1f)
                        _audioLevel.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioLevel.value = 0f
                        _transcription.value = "Understanding..."
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioLevel.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NETWORK -> "Network required for speech"
                            else -> "Recognition failed ($error)"
                        }
                        onErrorCallback?.invoke(msg)
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioLevel.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            _transcription.value = text
                            onResultCallback?.invoke(text)
                        } else {
                            onErrorCallback?.invoke("Couldn't hear clearly")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            _transcription.value = text
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
            }

            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            _isListening.value = false
            onErrorCallback?.invoke("Could not start speech recognition: ${e.message}")
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // Ignore clean up errors
        } finally {
            speechRecognizer = null
            _isListening.value = false
            _audioLevel.value = 0f
        }
    }
}
