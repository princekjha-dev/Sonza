package com.sonza.app.ai.orbit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.app.core.model.Song
import com.sonza.app.player.MusicPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface OrbitUiEvent {
    object RequestRecordAudioPermission : OrbitUiEvent
    data class NavigateTo(val target: String) : OrbitUiEvent
    data class ShowToast(val message: String) : OrbitUiEvent
}

class OrbitAssistantViewModel(
    private val voiceManager: OrbitVoiceManager,
    private val executor: OrbitExecutor,
    private val musicPlayer: MusicPlayer
) : ViewModel() {

    private val _orbitState = MutableStateFlow(OrbitState.IDLE)
    val orbitState: StateFlow<OrbitState> = _orbitState.asStateFlow()

    private val _isAssistantVisible = MutableStateFlow(false)
    val isAssistantVisible: StateFlow<Boolean> = _isAssistantVisible.asStateFlow()

    private val _assistantMessage = MutableStateFlow("Tap Orbit to speak...")
    val assistantMessage: StateFlow<String> = _assistantMessage.asStateFlow()

    val audioLevel: StateFlow<Float> = voiceManager.audioLevel

    private val _events = MutableSharedFlow<OrbitUiEvent>()
    val events: SharedFlow<OrbitUiEvent> = _events.asSharedFlow()

    private var autoDismissJob: Job? = null

    /**
     * Primary Interaction: TAP ORBIT = IMMEDIATELY LISTEN
     */
    fun onOrbitTapped() {
        autoDismissJob?.cancel()
        if (_orbitState.value == OrbitState.LISTENING) {
            // Cancel listening and return to IDLE
            voiceManager.stopListening()
            _orbitState.value = OrbitState.IDLE
            _isAssistantVisible.value = false
            return
        }

        // Open assistant and immediately listen
        _isAssistantVisible.value = true
        startListening()
    }

    fun startListening() {
        autoDismissJob?.cancel()
        if (!voiceManager.hasRecordAudioPermission()) {
            viewModelScope.launch {
                _events.emit(OrbitUiEvent.RequestRecordAudioPermission)
            }
            _assistantMessage.value = "Microphone permission required"
            _orbitState.value = OrbitState.ERROR
            return
        }

        _orbitState.value = OrbitState.LISTENING
        _assistantMessage.value = "Listening..."

        voiceManager.startListening(
            onResult = { text ->
                processUtterance(text)
            },
            onError = { errorMsg ->
                _orbitState.value = OrbitState.ERROR
                _assistantMessage.value = errorMsg
                scheduleAutoDismiss(3000)
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
            val intent = OrbitIntentParser.parse(rawInput, currentSong)

            _orbitState.value = OrbitState.WORKING
            val result = executor.execute(intent, currentSong)

            _assistantMessage.value = result.message
            _orbitState.value = result.resultingState

            if (result.navigationTarget != null) {
                _events.emit(OrbitUiEvent.NavigateTo(result.navigationTarget))
            }

            // Keep confirmation visible briefly, then return to IDLE
            scheduleAutoDismiss(3500)
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
        voiceManager.stopListening()
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
    }
}
