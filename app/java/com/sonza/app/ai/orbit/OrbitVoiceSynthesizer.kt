package com.sonza.app.ai.orbit

import android.content.Context
import com.sonza.app.player.MusicPlayer
import com.sonza.app.util.TTSManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Orbit Voice Synthesizer — gives Orbit its own recognizable assistant voice,
 * manages dynamic audio ducking of music playback during speech, supports
 * immediate voice interruption, and drives the real-time speaking animation for OrbitAvatar.
 */
class OrbitVoiceSynthesizer(
    private val ttsManager: TTSManager,
    private val musicPlayer: MusicPlayer,
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechLevel = MutableStateFlow(0f)
    val speechLevel: StateFlow<Float> = _speechLevel.asStateFlow()

    private var speechLevelJob: Job? = null
    private var duckJob: Job? = null

    // Orbit assistant personality audio signature: natural, calm, confident
    private val orbitPitch = 1.0f
    private val orbitSpeechRate = 1.0f

    /**
     * Speak Orbit's response with natural cadence, ducking music while speaking.
     */
    fun speak(
        text: String,
        onStart: (() -> Unit)? = null,
        onDone: (() -> Unit)? = null
    ) {
        if (text.isBlank()) {
            scope.launch(Dispatchers.Main) {
                onDone?.invoke()
            }
            return
        }

        stopSpeaking()

        val isCompleted = AtomicBoolean(false)

        duckJob = scope.launch {
            // Duck music player volume
            musicPlayer.duckVolume(0.15f)

            _isSpeaking.value = true
            startSpeechLevelModulation()

            ttsManager.speak(
                text = text,
                volume = 1.0f,
                pitch = orbitPitch,
                speechRate = orbitSpeechRate,
                onStart = {
                    scope.launch(Dispatchers.Main) {
                        _isSpeaking.value = true
                        onStart?.invoke()
                    }
                },
                onDone = {
                    if (isCompleted.compareAndSet(false, true)) {
                        scope.launch(Dispatchers.Main) {
                            finishSpeaking()
                            onDone?.invoke()
                        }
                    }
                },
                onError = {
                    if (isCompleted.compareAndSet(false, true)) {
                        scope.launch(Dispatchers.Main) {
                            finishSpeaking()
                            onDone?.invoke()
                        }
                    }
                }
            )
        }
    }

    /**
     * Immediately interrupts Orbit's speech (e.g. user began speaking or tapped avatar).
     */
    fun stopSpeaking() {
        duckJob?.cancel()
        duckJob = null
        ttsManager.stop()
        finishSpeaking()
    }

    private fun finishSpeaking() {
        speechLevelJob?.cancel()
        speechLevelJob = null
        _isSpeaking.value = false
        _speechLevel.value = 0f
        // Restore music player volume smoothly
        musicPlayer.restoreVolume()
    }

    /**
     * Simulates natural syllable waveform amplitude pulses while Orbit is speaking
     * so that OrbitAvatar's face and glow pulsate dynamically with speech.
     */
    private fun startSpeechLevelModulation() {
        speechLevelJob?.cancel()
        speechLevelJob = scope.launch {
            val levels = floatArrayOf(0.35f, 0.65f, 0.85f, 0.45f, 0.75f, 0.9f, 0.4f, 0.2f, 0.6f, 0.8f, 0.5f, 0.25f)
            var index = 0
            while (isActive && _isSpeaking.value) {
                _speechLevel.value = levels[index % levels.size]
                index++
                delay(85L)
            }
            _speechLevel.value = 0f
        }
    }
}
