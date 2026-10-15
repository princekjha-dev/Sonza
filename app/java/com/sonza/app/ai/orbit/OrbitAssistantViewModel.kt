package com.sonza.app.ai.orbit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.app.core.model.Song
import com.sonza.app.player.MusicPlayer
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
 * User speaks → Orbit listens → Orbit understands → executes on real playback engine →
 * Orbit speaks response with its own voice → Orbit listens for next command.
 */
class OrbitAssistantViewModel(
    private val voiceManager: OrbitVoiceManager,
    private val voiceSynthesizer: OrbitVoiceSynthesizer,
    private val executor: OrbitExecutor,
    private val musicPlayer: MusicPlayer
) : ViewModel() {

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

    /**
     * Primary Interaction: TAP ORBIT = IMMEDIATELY LISTEN (or interrupt speech)
     */
    fun onOrbitTapped() {
        autoDismissJob?.cancel()

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

        // 3. Otherwise, open assistant and immediately listen
        _isAssistantVisible.value = true
        startListening()
    }

    fun startListening() {
        autoDismissJob?.cancel()
        continuousListeningJob?.cancel()
        voiceSynthesizer.stopSpeaking()

        if (!voiceManager.hasRecordAudioPermission()) {
            viewModelScope.launch {
                _events.emit(OrbitUiEvent.RequestRecordAudioPermission)
            }
            _assistantMessage.value = "Microphone permission required"
            _orbitState.value = OrbitState.ERROR
            return
        }

        _orbitState.value = OrbitState.LISTENING
        _assistantMessage.value = when (_dialogueContext.value) {
            is OrbitDialogueContext.AwaitingCommand -> "Listening for command..."
            is OrbitDialogueContext.AwaitingDisambiguation -> "Listening for choice..."
            is OrbitDialogueContext.AwaitingConfirmation -> "Say yes or no..."
            else -> "Listening..."
        }

        voiceManager.startListening(
            onResult = { text ->
                processUtterance(text)
            },
            onError = { errorMsg ->
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
                    _assistantMessage.value = errorMsg
                    scheduleAutoDismiss(3000)
                }
            }
        )
    }

    fun processUtterance(rawInput: String) {
        autoDismissJob?.cancel()
        voiceManager.stopListening()
        _orbitState.value = OrbitState.THINKING
        _assistantMessage.value = "\"$rawInput\""

        viewModelScope.launch {
            val currentSong = musicPlayer.playerState.value.currentSong
            val intent = OrbitIntentParser.parse(
                rawInput = rawInput,
                currentSong = currentSong,
                dialogueContext = _dialogueContext.value
            )

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

            voiceSynthesizer.speak(
                text = speechText,
                onStart = {
                    _orbitState.value = OrbitState.SPEAKING
                },
                onDone = {
                    // Two-way conversational continuation
                    if (result.expectsFollowUpVoiceInput) {
                        // Immediately listen for the follow-up answer!
                        startListening()
                    } else {
                        // Action completed (e.g. "Playing Kesariya")
                        _orbitState.value = result.resultingState
                        // Keep continuous listening standby for ~6 seconds so user can say next command
                        continuousListeningJob = viewModelScope.launch {
                            delay(6000L)
                            _orbitState.value = OrbitState.IDLE
                            _isAssistantVisible.value = false
                        }
                    }
                }
            )
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            startListening()
        } else {
            _assistantMessage.value = "Microphone access is needed for Orbit"
            _orbitState.value = OrbitState.ERROR
            scheduleAutoDismiss(3000)
        }
    }

    fun dismissAssistant() {
        autoDismissJob?.cancel()
        continuousListeningJob?.cancel()
        voiceManager.stopListening()
        voiceSynthesizer.stopSpeaking()
        _dialogueContext.value = OrbitDialogueContext.Idle
        _orbitState.value = OrbitState.IDLE
        _isAssistantVisible.value = false
    }

    private fun scheduleAutoDismiss(delayMs: Long) {
        autoDismissJob?.cancel()
        autoDismissJob = viewModelScope.launch {
            delay(delayMs)
            _orbitState.value = OrbitState.IDLE
            _isAssistantVisible.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.stopListening()
        voiceSynthesizer.stopSpeaking()
    }
}
