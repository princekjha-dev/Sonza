package com.sonza.app.ai.orbit

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Lightweight UI bridge ViewModel that exposes the persistent, application-level
 * [OrbitManager] singleton to Jetpack Compose screens.
 *
 * It holds ZERO proprietary audio or recognition state itself, guaranteeing that
 * closing views, sheets, or navigating between tabs will NEVER destroy or reset Orbit.
 */
class OrbitAssistantViewModel(
    val orbitManager: OrbitManager
) : ViewModel() {

    val orbitState: StateFlow<OrbitState> = orbitManager.orbitState
    val isAssistantVisible: StateFlow<Boolean> = orbitManager.isVisualUiVisible
    val assistantMessage: StateFlow<String> = orbitManager.assistantMessage
    val actionIndicator: StateFlow<String?> = orbitManager.actionIndicator
    val dialogueContext: StateFlow<OrbitDialogueContext> = orbitManager.dialogueContext
    val audioLevel: StateFlow<Float> = orbitManager.audioLevel
    val musicAudioData: StateFlow<OrbitMusicAudioData> = orbitManager.musicAudioData
    val events: SharedFlow<OrbitUiEvent> = orbitManager.events

    fun onOrbitTapped() {
        orbitManager.onUserTrigger()
    }

    fun onWakeWordDetected(command: String? = null) {
        orbitManager.onWakeWordDetected(command)
    }

    fun startListening() {
        orbitManager.startListening()
    }

    fun processUtterance(rawInput: String) {
        orbitManager.processUtterance(rawInput)
    }

    fun onPermissionResult(isGranted: Boolean) {
        orbitManager.onPermissionResult(isGranted)
    }

    /**
     * Requirement 21: The X button MUST ONLY dismiss the visual Orbit UI.
     * It does NOT terminate speech recognition, destroy OrbitManager, clear context,
     * or unregister listeners.
     */
    fun dismissAssistant() {
        orbitManager.hideVisualUi()
    }

    fun hideVisualUi() {
        orbitManager.hideVisualUi()
    }
}
