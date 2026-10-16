package com.sonza.app.ai.orbit

import android.content.Context
import com.sonza.app.core.model.Song
import com.sonza.app.player.MusicPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * UI Events emitted from the persistent OrbitManager to the hosting Activity/Compose root.
 */
sealed interface OrbitUiEvent {
    object RequestRecordAudioPermission : OrbitUiEvent
    data class NavigateTo(val target: String) : OrbitUiEvent
    data class ShowToast(val message: String) : OrbitUiEvent
}

/**
 * Authoritative, persistent, application-level Orbit Assistant Manager.
 *
 * CRITICAL ARCHITECTURAL SEPARATION:
 * 1. Assistant Lifecycle: Persistent application singleton. Survives navigation, closing players,
 *    switching tabs, backgrounding, sheets, and full-screen covers.
 * 2. Visual UI: Can be hidden by pressing 'X' or timed out without affecting the running assistant,
 *    wake-word detection, context memory, or voice pipeline.
 * 3. Music Player Lifecycle: Independent playback engine; Orbit controls it via authoritative services
 *    without being tied to any player view.
 */
class OrbitManager(
    private val voiceManager: OrbitVoiceManager,
    private val voiceSynthesizer: OrbitVoiceSynthesizer,
    private val executor: OrbitExecutor,
    private val musicPlayer: MusicPlayer,
    private val context: Context
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val voiceSession = OrbitVoiceSession(voiceManager, context)
    val contextMemory = OrbitContextMemory()

    // --- State Machine ---
    private val _orbitState = MutableStateFlow(OrbitState.IDLE)
    val orbitState: StateFlow<OrbitState> = _orbitState.asStateFlow()

    // --- Visual UI Separation (X button only toggles this) ---
    private val _isVisualUiVisible = MutableStateFlow(false)
    val isVisualUiVisible: StateFlow<Boolean> = _isVisualUiVisible.asStateFlow()

    private val _assistantMessage = MutableStateFlow("Tap Orbit to speak…")
    val assistantMessage: StateFlow<String> = _assistantMessage.asStateFlow()

    private val _actionIndicator = MutableStateFlow<String?>(null)
    val actionIndicator: StateFlow<String?> = _actionIndicator.asStateFlow()

    private val _dialogueContext = MutableStateFlow<OrbitDialogueContext>(OrbitDialogueContext.Idle)
    val dialogueContext: StateFlow<OrbitDialogueContext> = _dialogueContext.asStateFlow()

    // Combined audio level for animated avatar (mic RMS while listening, speech waveform while speaking)
    val audioLevel: StateFlow<Float> = combine(
        _orbitState,
        voiceManager.audioLevel,
        voiceSynthesizer.speechLevel
    ) { state, micLevel, ttsLevel ->
        when (state) {
            OrbitState.LISTENING, OrbitState.TRANSCRIBING -> micLevel
            OrbitState.SPEAKING -> ttsLevel
            else -> 0f
        }
    }.stateIn(scope, SharingStarted.Eagerly, 0f)

    private val audioAnalyzer = OrbitMusicAudioAnalyzer(scope = scope)
    val musicAudioData: StateFlow<OrbitMusicAudioData> = audioAnalyzer.audioData

    private val _events = MutableSharedFlow<OrbitUiEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<OrbitUiEvent> = _events.asSharedFlow()

    private var autoDismissJob: Job? = null
    private var followUpJob: Job? = null
    private var lastWakeTimeMs = 0L

    init {
        OrbitLogger.state(OrbitState.IDLE)

        // 1. Start persistent wake-word foreground service if microphone permission granted
        if (voiceSession.hasRecordAudioPermission()) {
            OrbitWakeWordService.start(context)
            voiceSession.resumeWakeWordStandby()
        }

        // 2. React to central music player state transitions for ambient avatar animations
        scope.launch {
            musicPlayer.playerState.collect { playerState ->
                audioAnalyzer.setPlaying(playerState.isPlaying)
                contextMemory.updatePlayback(playerState.currentSong, playerState.queue)

                if (!_orbitState.value.isVoiceActive) {
                    val targetState = if (playerState.isPlaying) OrbitState.MUSIC_REACTIVE else OrbitState.IDLE
                    if (_orbitState.value != targetState) {
                        _orbitState.value = targetState
                        OrbitLogger.state(targetState)
                    }
                }
            }
        }
    }

    /**
     * User tapped the Orbit avatar:
     * - If currently speaking, immediately interrupts speech and opens mic for new command.
     * - If currently listening, cancels listening and returns to standby.
     * - If idle, wakes up and begins listening.
     */
    fun onUserTrigger() {
        autoDismissJob?.cancel()
        followUpJob?.cancel()

        if (_orbitState.value == OrbitState.SPEAKING) {
            OrbitLogger.transcript("User interrupted Orbit speech")
            voiceSynthesizer.stopSpeaking()
            startListening()
            return
        }

        if (_orbitState.value.isVoiceActive) {
            cancelCurrentVoiceSession()
            return
        }

        triggerWakeSequence(null)
    }

    /**
     * Triggered when "Hey Orbit" is spoken by the user anywhere in the app or while in background.
     */
    fun onWakeWordDetected(command: String? = null) {
        val now = System.currentTimeMillis()
        if (now - lastWakeTimeMs < 1000L || _orbitState.value == OrbitState.WAKE_DETECTED || _orbitState.value == OrbitState.ACTIVATING) {
            return
        }
        lastWakeTimeMs = now
        OrbitLogger.wakeDetected(command)
        triggerWakeSequence(command)
    }

    private fun triggerWakeSequence(command: String?) {
        autoDismissJob?.cancel()
        followUpJob?.cancel()
        voiceSynthesizer.stopSpeaking()

        transitionToState(OrbitState.WAKE_DETECTED)
        _isVisualUiVisible.value = true
        OrbitSoundManager.playActivation()

        scope.launch {
            delay(100L)
            transitionToState(OrbitState.ACTIVATING)
            delay(350L) // Settle activation chime before acquiring microphone

            if (!command.isNullOrBlank()) {
                _assistantMessage.value = "“$command”"
                processUtterance(command)
            } else {
                startListening()
            }
        }
    }

    fun startListening() {
        autoDismissJob?.cancel()
        followUpJob?.cancel()
        voiceSynthesizer.stopSpeaking()

        if (!voiceSession.hasRecordAudioPermission()) {
            scope.launch {
                _events.emit(OrbitUiEvent.RequestRecordAudioPermission)
            }
            _assistantMessage.value = "Orbit needs microphone access to hear 'Hey Orbit' and your voice commands."
            transitionToState(OrbitState.ERROR)
            OrbitSoundManager.playError()
            scheduleSafeReturn(3000L)
            return
        }

        // Smoothly duck music while listening so user voice is clear
        musicPlayer.duckVolume(0.15f)
        transitionToState(OrbitState.LISTENING)
        _isVisualUiVisible.value = true

        _assistantMessage.value = when (_dialogueContext.value) {
            is OrbitDialogueContext.AwaitingCommand -> "I'm listening…"
            is OrbitDialogueContext.AwaitingDisambiguation -> "Which one would you like?"
            is OrbitDialogueContext.AwaitingConfirmation -> "Say yes or no…"
            else -> "Listening…"
        }

        voiceSession.acquireMicForCommand(
            onPartialResult = { partialText ->
                if (partialText.isNotBlank()) {
                    transitionToState(OrbitState.TRANSCRIBING)
                    _assistantMessage.value = partialText
                    OrbitLogger.transcribing(partialText)
                }
            },
            onResult = { text ->
                musicPlayer.restoreVolume()
                _assistantMessage.value = text
                OrbitLogger.transcript(text)
                processUtterance(text)
            },
            onError = { errorMsg ->
                musicPlayer.restoreVolume()
                OrbitLogger.error("Recognition error: $errorMsg")

                if (errorMsg.contains("timed out", ignoreCase = true) || errorMsg.contains("No speech", ignoreCase = true)) {
                    returnToIdleState()
                } else {
                    transitionToState(OrbitState.ERROR)
                    OrbitSoundManager.playError()
                    _assistantMessage.value = errorMsg
                    scheduleSafeReturn(3000L)
                }
            }
        )
    }

    /**
     * Natural Language Command Processing & Execution
     */
    fun processUtterance(rawInput: String) {
        autoDismissJob?.cancel()
        followUpJob?.cancel()
        voiceSession.releaseMic()
        musicPlayer.restoreVolume()

        transitionToState(OrbitState.PROCESSING)
        _assistantMessage.value = "Processing…"

        scope.launch {
            val currentSong = musicPlayer.playerState.value.currentSong
            val intent = OrbitIntentParser.parse(
                rawInput = rawInput,
                currentSong = currentSong,
                dialogueContext = _dialogueContext.value,
                contextMemory = contextMemory
            )
            OrbitLogger.intent(intent::class.simpleName ?: "Intent", rawInput)

            // Special: Standalone Wake Word prompt
            if (intent is OrbitIntent.WakeWordOnlyIntent) {
                _dialogueContext.value = OrbitDialogueContext.AwaitingCommand()
                _assistantMessage.value = "I'm listening"
                speakResponse("Yes?", expectsFollowUp = true)
                return@launch
            }

            // Special: Cancel
            if (intent is OrbitIntent.CancelIntent) {
                _dialogueContext.value = OrbitDialogueContext.Idle
                contextMemory.resetContext()
                returnToIdleState()
                return@launch
            }

            transitionToState(OrbitState.EXECUTING)
            OrbitLogger.executing()

            val result = executor.execute(intent, currentSong, _dialogueContext.value, contextMemory)
            OrbitLogger.result(result.success, result.message)

            _assistantMessage.value = result.message
            _actionIndicator.value = result.actionIndicator
            _dialogueContext.value = result.updatedContext ?: OrbitDialogueContext.Idle

            // Context Memory update
            contextMemory.recordTurn(
                userUtterance = rawInput,
                intent = intent,
                spokenResponse = result.spokenResponse,
                action = result.action
            )

            if (result.navigationTarget != null) {
                _events.emit(OrbitUiEvent.NavigateTo(result.navigationTarget))
            }

            // HARD REQUIREMENT: Every command receives a spoken response
            val speechText = result.spokenResponse.ifBlank { result.message }
            speakResponse(speechText, expectsFollowUp = result.requiresFollowUp)
        }
    }

    private fun speakResponse(speechText: String, expectsFollowUp: Boolean) {
        transitionToState(OrbitState.SPEAKING)
        OrbitLogger.tts(speechText)

        voiceSynthesizer.speak(
            text = speechText,
            onStart = {
                transitionToState(OrbitState.SPEAKING)
            },
            onDone = {
                if (expectsFollowUp) {
                    transitionToState(OrbitState.WAITING_FOR_FOLLOWUP)
                    OrbitLogger.state(OrbitState.WAITING_FOR_FOLLOWUP)
                    startListening()
                } else {
                    returnToIdleState()
                }
            }
        )
    }

    /**
     * Critical Requirement 21:
     * The X button ONLY hides the visual UI. It does NOT stop Orbit, does NOT unregister listeners,
     * does NOT destroy context, and does NOT kill the wake-word system.
     */
    fun hideVisualUi() {
        OrbitLogger.transcript("X pressed: hiding visual UI only, Orbit assistant remains active")
        _isVisualUiVisible.value = false
    }

    fun showVisualUi() {
        _isVisualUiVisible.value = true
    }

    fun cancelCurrentVoiceSession() {
        voiceSession.releaseMic()
        voiceSynthesizer.stopSpeaking()
        musicPlayer.restoreVolume()
        _dialogueContext.value = OrbitDialogueContext.Idle
        returnToIdleState()
    }

    private fun returnToIdleState() {
        autoDismissJob?.cancel()
        followUpJob?.cancel()
        voiceSession.releaseMic()
        voiceSynthesizer.stopSpeaking()
        musicPlayer.restoreVolume()

        scope.launch {
            transitionToState(OrbitState.ENDING)
            delay(250L)

            val isNowPlaying = musicPlayer.playerState.value.isPlaying
            val finalState = if (isNowPlaying) OrbitState.MUSIC_REACTIVE else OrbitState.IDLE
            transitionToState(finalState)
            _isVisualUiVisible.value = false
            _dialogueContext.value = OrbitDialogueContext.Idle
            voiceSession.resumeWakeWordStandby()
        }
    }

    private fun scheduleSafeReturn(delayMs: Long) {
        autoDismissJob?.cancel()
        autoDismissJob = scope.launch {
            delay(delayMs)
            returnToIdleState()
        }
    }

    private fun transitionToState(newState: OrbitState) {
        if (_orbitState.value != newState) {
            _orbitState.value = newState
            OrbitLogger.state(newState)
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            OrbitWakeWordService.start(context)
            voiceSession.resumeWakeWordStandby()
            startListening()
        } else {
            _assistantMessage.value = "Orbit needs microphone access to hear 'Hey Orbit' and your voice commands."
            transitionToState(OrbitState.ERROR)
            OrbitSoundManager.playError()
            scheduleSafeReturn(3000L)
        }
    }
}
