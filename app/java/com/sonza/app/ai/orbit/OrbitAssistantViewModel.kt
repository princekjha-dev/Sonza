package com.sonza.app.ai.orbit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.app.core.model.Song
import com.sonza.app.player.MusicPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface OrbitUiEvent {
    object RequestRecordAudioPermission : OrbitUiEvent
    data class NavigateTo(val target: String) : OrbitUiEvent
    data class ShowToast(val message: String) : OrbitUiEvent
}

/**
 * Authoritative Orbit Assistant ViewModel.
 * Orchestrates the full two-way conversational dialogue loop:
 * Standby Wake-Word ("Hey Orbit") → Activation Chime → Listening with live transcription →
 * Intent Processing → Execution on authoritative playback engine → Assistant TTS speech → Standby.
 *
 * MICROPHONE OWNERSHIP:
 * The wake-word detector is owned exclusively by [OrbitWakeWordService].
 * This ViewModel communicates with it via [OrbitWakeWordService.getDetector()] and
 * [OrbitWakeWordService.pauseDetector] / [OrbitWakeWordService.resumeDetector].
 * The [OrbitVoiceManager] (for command listening) and the wake-word detector NEVER
 * hold the microphone simultaneously.
 */
class OrbitAssistantViewModel(
    private val voiceManager: OrbitVoiceManager,
    private val voiceSynthesizer: OrbitVoiceSynthesizer,
    private val executor: OrbitExecutor,
    private val musicPlayer: MusicPlayer,
    private val context: Context
) : ViewModel() {

    companion object {
        private const val TAG = "OrbitVM"
    }

    private val _orbitState = MutableStateFlow(OrbitState.IDLE)
    val orbitState: StateFlow<OrbitState> = _orbitState.asStateFlow()

    private val _isAssistantVisible = MutableStateFlow(false)
    val isAssistantVisible: StateFlow<Boolean> = _isAssistantVisible.asStateFlow()

    private val _assistantMessage = MutableStateFlow("Tap Orbit to speak...")
    val assistantMessage: StateFlow<String> = _assistantMessage.asStateFlow()

    private val _actionIndicator = MutableStateFlow<String?>(null)
    val actionIndicator: StateFlow<String?> = _actionIndicator.asStateFlow()

    private val _dialogueContext = MutableStateFlow<OrbitDialogueContext>(OrbitDialogueContext.Idle)
    val dialogueContext: StateFlow<OrbitDialogueContext> = _dialogueContext.asStateFlow()

    // Combined audio level: mic RMS while listening, or Orbit voice waveform while speaking
    val audioLevel: StateFlow<Float> = combine(
        _orbitState,
        voiceManager.audioLevel,
        voiceSynthesizer.speechLevel
    ) { state, micLevel, ttsLevel ->
        when (state) {
            OrbitState.LISTENING -> micLevel
            OrbitState.SPEAKING -> ttsLevel
            else -> 0f
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    private val _events = MutableSharedFlow<OrbitUiEvent>()
    val events: SharedFlow<OrbitUiEvent> = _events.asSharedFlow()

    private var autoDismissJob: Job? = null
    private var continuousListeningJob: Job? = null

    init {
        // Start the wake-word foreground service which owns the detector
        if (voiceManager.hasRecordAudioPermission()) {
            Log.d(TAG, "init: starting OrbitWakeWordService")
            OrbitWakeWordService.start(context)
        }
    }

    /**
     * Pauses the wake-word detector (owned by the service).
     * Call before acquiring the mic for command listening or TTS.
     */
    private fun pauseWakeWordDetector() {
        Log.d(TAG, "Pausing wake-word detector")
        OrbitWakeWordService.getDetector()?.pauseListening()
    }

    /**
     * Resumes the wake-word detector (owned by the service).
     * Call after releasing the mic from command listening / TTS.
     */
    private fun resumeWakeWordDetector() {
        Log.d(TAG, "Resuming wake-word detector")
        OrbitWakeWordService.getDetector()?.resumeListening()
    }

    /**
     * Primary Interaction: TAP ORBIT = IMMEDIATELY LISTEN (or interrupt speech)
     */
    fun onOrbitTapped() {
        autoDismissJob?.cancel()
        continuousListeningJob?.cancel()

        // 1. If currently speaking, interrupt Orbit immediately and start listening
        if (_orbitState.value == OrbitState.SPEAKING) {
            voiceSynthesizer.stopSpeaking()
            startListening()
            return
        }

        // 2. If currently listening, cancel and close
        if (_orbitState.value == OrbitState.LISTENING) {
            dismissAssistant()
            return
        }

        // 3. Otherwise, play activation sound, open assistant and immediately listen
        OrbitSoundManager.playActivation()
        _isAssistantVisible.value = true
        pauseWakeWordDetector()
        startListening()
    }

    /**
     * Triggered when "Hey Orbit" is spoken by the user.
     * If user said "Hey Orbit, play some romantic songs" in one breath,
     * the command is already present and executed directly.
     */
    fun onWakeWordDetected(command: String? = null) {
        Log.d(TAG, "onWakeWordDetected: command=${command ?: "(none)"}")
        autoDismissJob?.cancel()
        continuousListeningJob?.cancel()
        pauseWakeWordDetector()
        voiceSynthesizer.stopSpeaking()

        OrbitSoundManager.playWakeWordDetected()
        _isAssistantVisible.value = true

        if (!command.isNullOrBlank()) {
            _assistantMessage.value = "\u201C$command\u201D"
            processUtterance(command)
        } else {
            startListening()
        }
    }

    fun startListening() {
        autoDismissJob?.cancel()
        continuousListeningJob?.cancel()
        voiceSynthesizer.stopSpeaking()
        pauseWakeWordDetector()

        if (!voiceManager.hasRecordAudioPermission()) {
            viewModelScope.launch {
                _events.emit(OrbitUiEvent.RequestRecordAudioPermission)
            }
            _assistantMessage.value = "Orbit needs microphone access to hear 'Hey Orbit' and your voice commands."
            _orbitState.value = OrbitState.ERROR
            OrbitSoundManager.playError()
            return
        }

        Log.d(TAG, "▶ Command recognition STARTED")

        // Temporarily duck music so microphone can hear speech clearly
        musicPlayer.duckVolume(0.15f)

        _orbitState.value = OrbitState.LISTENING
        OrbitSoundManager.playListening()
        _assistantMessage.value = when (_dialogueContext.value) {
            is OrbitDialogueContext.AwaitingCommand -> "I'm listening…"
            is OrbitDialogueContext.AwaitingDisambiguation -> "Listening for choice…"
            is OrbitDialogueContext.AwaitingConfirmation -> "Say yes or no…"
            else -> "Listening…"
        }

        voiceManager.startListening(
            onPartialResult = { partialText ->
                if (partialText.isNotBlank()) {
                    _assistantMessage.value = partialText
                }
            },
            onResult = { text ->
                Log.d(TAG, "🎯 Command recognized: \"$text\"")
                _assistantMessage.value = text
                processUtterance(text)
            },
            onError = { errorMsg ->
                Log.w(TAG, "Command recognition error: $errorMsg")
                // Restore volume if error occurs
                musicPlayer.restoreVolume()

                // Don't treat silence timeouts as hard failures during continuous dialogue
                if (errorMsg.contains("timed out", ignoreCase = true) || errorMsg.contains("No speech", ignoreCase = true)) {
                    if (_dialogueContext.value != OrbitDialogueContext.Idle) {
                        _orbitState.value = OrbitState.IDLE
                        scheduleAutoDismiss(2500)
                    } else {
                        scheduleAutoDismiss(2000)
                    }
                } else {
                    _orbitState.value = OrbitState.ERROR
                    OrbitSoundManager.playError()
                    _assistantMessage.value = if (errorMsg.contains("permission", ignoreCase = true)) {
                        "Orbit needs microphone access to hear 'Hey Orbit' and your voice commands."
                    } else {
                        errorMsg
                    }
                    scheduleAutoDismiss(3500)
                }
            }
        )
    }

    fun processUtterance(rawInput: String) {
        autoDismissJob?.cancel()
        voiceManager.stopListening()
        OrbitSoundManager.playCommandReceived()
        _orbitState.value = OrbitState.THINKING
        _assistantMessage.value = "Thinking…"

        viewModelScope.launch {
            val currentSong = musicPlayer.playerState.value.currentSong
            val intent = OrbitIntentParser.parse(
                rawInput = rawInput,
                currentSong = currentSong,
                dialogueContext = _dialogueContext.value
            )

            // Special check: Standalone wake word -> prompt user
            if (intent is OrbitIntent.WakeWordOnlyIntent) {
                _dialogueContext.value = OrbitDialogueContext.AwaitingCommand()
                _assistantMessage.value = "I'm listening"
                _orbitState.value = OrbitState.SPEAKING
                voiceSynthesizer.speak("Yes?", onDone = {
                    startListening()
                })
                return@launch
            }

            // Special check: Cancellation
            if (intent is OrbitIntent.CancelIntent) {
                _dialogueContext.value = OrbitDialogueContext.Idle
                _assistantMessage.value = "Alright"
                _orbitState.value = OrbitState.IDLE
                voiceSynthesizer.speak("Alright", onDone = {
                    dismissAssistant()
                })
                return@launch
            }

            _orbitState.value = OrbitState.WORKING
            val result = executor.execute(intent, currentSong, _dialogueContext.value)

            if (result.success) {
                OrbitSoundManager.playSuccess()
            } else {
                OrbitSoundManager.playError()
            }

            _assistantMessage.value = result.message
            _actionIndicator.value = result.actionIndicator
            _orbitState.value = result.resultingState
            _dialogueContext.value = result.updatedContext ?: OrbitDialogueContext.Idle

            if (result.navigationTarget != null) {
                _events.emit(OrbitUiEvent.NavigateTo(result.navigationTarget))
            }

            // Speak Orbit's response with its assistant voice
            val speechText = result.spokenResponse.ifBlank { result.message }
            _orbitState.value = OrbitState.SPEAKING
            Log.d(TAG, "🗣 Orbit speaking: \"$speechText\"")

            voiceSynthesizer.speak(
                text = speechText,
                onStart = {
                    _orbitState.value = OrbitState.SPEAKING
                },
                onDone = {
                    Log.d(TAG, "🗣 Orbit finished speaking")
                    // Two-way conversational continuation
                    if (result.expectsFollowUpVoiceInput) {
                        startListening()
                    } else {
                        _orbitState.value = result.resultingState
                        // Auto-dismiss smoothly after response
                        continuousListeningJob = viewModelScope.launch {
                            delay(4500L)
                            _orbitState.value = OrbitState.IDLE
                            _isAssistantVisible.value = false
                            Log.d(TAG, "▶ Auto-dismiss → resuming wake-word listener")
                            resumeWakeWordDetector()
                        }
                    }
                }
            )
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            Log.d(TAG, "Microphone permission GRANTED")
            OrbitWakeWordService.start(context)
            startListening()
        } else {
            _assistantMessage.value = "Orbit needs microphone access to hear 'Hey Orbit' and your voice commands."
            _orbitState.value = OrbitState.ERROR
            OrbitSoundManager.playError()
            scheduleAutoDismiss(3500)
        }
    }

    fun dismissAssistant() {
        Log.d(TAG, "Assistant DISMISSED")
        autoDismissJob?.cancel()
        continuousListeningJob?.cancel()
        voiceManager.stopListening()
        voiceSynthesizer.stopSpeaking()
        musicPlayer.restoreVolume()
        OrbitSoundManager.playClosed()
        _dialogueContext.value = OrbitDialogueContext.Idle
        _orbitState.value = OrbitState.IDLE
        _isAssistantVisible.value = false
        resumeWakeWordDetector()
    }

    private fun scheduleAutoDismiss(delayMs: Long) {
        autoDismissJob?.cancel()
        autoDismissJob = viewModelScope.launch {
            delay(delayMs)
            _orbitState.value = OrbitState.IDLE
            _isAssistantVisible.value = false
            resumeWakeWordDetector()
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel CLEARED")
        voiceManager.stopListening()
        voiceSynthesizer.stopSpeaking()
        // Don't stop the service — it should keep running in the background
    }
}
