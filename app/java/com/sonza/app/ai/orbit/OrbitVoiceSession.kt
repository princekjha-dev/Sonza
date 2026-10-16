package com.sonza.app.ai.orbit

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Microphone ownership states for Orbit voice pipeline.
 */
enum class MicOwner {
    NONE,
    WAKE_WORD_STANDBY,
    COMMAND_RECOGNITION,
    TTS_PLAYBACK
}

/**
 * Single authoritative voice session and microphone ownership manager.
 * Guarantees that AudioRecord (wake word detector) and SpeechRecognizer (command listener)
 * NEVER hold the hardware microphone simultaneously, eliminating Android AudioRecord contention.
 * Features an automatic watchdog recovery timer to guarantee the microphone is never leaked or held stuck.
 */
class OrbitVoiceSession(
    private val voiceManager: OrbitVoiceManager,
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val _micOwner = MutableStateFlow(MicOwner.NONE)
    val micOwner: StateFlow<MicOwner> = _micOwner.asStateFlow()

    private var watchdogJob: Job? = null
    private val isCommandListening = AtomicBoolean(false)

    companion object {
        private const val WATCHDOG_TIMEOUT_MS = 8000L
    }

    fun hasRecordAudioPermission(): Boolean = voiceManager.hasRecordAudioPermission()

    /**
     * Acquires the microphone exclusively for streaming command speech recognition.
     * Pauses the standby wake-word detector, arms the safety watchdog, and begins listening.
     */
    fun acquireMicForCommand(
        onPartialResult: ((String) -> Unit)? = null,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        OrbitLogger.micOwnership("COMMAND_RECOGNITION", "ACQUIRING")
        // 1. Pause wake word detector so it releases AudioRecord
        OrbitWakeWordService.getDetector()?.pauseListening()
        _micOwner.value = MicOwner.COMMAND_RECOGNITION
        isCommandListening.set(true)

        // 2. Arm watchdog timer to prevent indefinite hanging
        watchdogJob?.cancel()
        watchdogJob = scope.launch {
            delay(WATCHDOG_TIMEOUT_MS)
            if (isCommandListening.get()) {
                OrbitLogger.recovery("Watchdog timed out waiting for speech recognition results")
                releaseMic()
                onError("Listening timed out")
            }
        }

        // 3. Start speech recognition
        voiceManager.startListening(
            onPartialResult = { partial ->
                watchdogJob?.cancel() // Speech activity detected, reset or extend watchdog
                watchdogJob = scope.launch {
                    delay(5000L)
                    if (isCommandListening.get()) {
                        OrbitLogger.recovery("Speech pause timeout after partial results")
                        releaseMic()
                        onError("Listening timed out")
                    }
                }
                onPartialResult?.invoke(partial)
            },
            onResult = { result ->
                watchdogJob?.cancel()
                isCommandListening.set(false)
                // Release mic ownership before handing off result
                voiceManager.stopListening()
                _micOwner.value = MicOwner.NONE
                OrbitLogger.micOwnership("COMMAND_RECOGNITION", "RELEASED_ON_RESULT")
                onResult(result)
            },
            onError = { error ->
                watchdogJob?.cancel()
                isCommandListening.set(false)
                voiceManager.stopListening()
                _micOwner.value = MicOwner.NONE
                OrbitLogger.micOwnership("COMMAND_RECOGNITION", "RELEASED_ON_ERROR")
                // Resume standby detector on error
                resumeWakeWordStandby()
                onError(error)
            }
        )
    }

    /**
     * Forcibly stops command recognition and releases the microphone.
     */
    fun releaseMic() {
        watchdogJob?.cancel()
        watchdogJob = null
        if (isCommandListening.getAndSet(false)) {
            voiceManager.stopListening()
            _micOwner.value = MicOwner.NONE
            OrbitLogger.micOwnership("COMMAND_RECOGNITION", "FORCIBLY_RELEASED")
        }
    }

    /**
     * Resumes the standby wake-word detector after command or TTS completes.
     */
    fun resumeWakeWordStandby() {
        releaseMic()
        if (hasRecordAudioPermission()) {
            OrbitLogger.micOwnership("WAKE_WORD_STANDBY", "RESUMING")
            OrbitWakeWordService.getDetector()?.resumeListening()
            _micOwner.value = MicOwner.WAKE_WORD_STANDBY
        }
    }

    /**
     * Pauses the wake word detector (e.g. while Orbit is speaking or system audio ducked).
     */
    fun pauseWakeWordStandby() {
        OrbitLogger.micOwnership("WAKE_WORD_STANDBY", "PAUSING")
        OrbitWakeWordService.getDetector()?.pauseListening()
        if (_micOwner.value == MicOwner.WAKE_WORD_STANDBY) {
            _micOwner.value = MicOwner.NONE
        }
    }
}
