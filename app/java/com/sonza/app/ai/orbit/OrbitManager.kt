package com.sonza.app.ai.orbit

import android.content.Context
import com.sonza.app.core.model.Song
import com.sonza.app.data.SessionManager
import com.sonza.app.player.MusicPlayer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.Calendar
import java.util.Locale

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
 * Requirements:
 * - Orbit has its own voice identity: zero generic Google Assistant-style sounds or chimes.
 * - Idempotent, robust state machine: idle -> wakeDetected -> listening -> processing -> speaking -> idle.
 *   Prevents duplicate triggers from multiple wake-word callbacks, microphone loops, or view re-renders.
 * - Interruptible and recoverable: Seamlessly ducks music during voice session and restores volume on completion.
 * - Continuous conversation: Allows natural follow-up commands without saying "Hey Orbit" repeatedly,
 *   while respecting privacy timeouts.
 * - Personalized: Orbit greets the user naturally using their saved name from the single-source-of-truth profile.
 */
class OrbitManager(
    private val voiceManager: OrbitVoiceManager,
    private val voiceSynthesizer: OrbitVoiceSynthesizer,
    private val executor: OrbitExecutor,
    private val musicPlayer: MusicPlayer,
    private val sessionManager: SessionManager,
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
                contextMemory.updatePlayback(
                    song = playerState.currentSong,
                    queue = playerState.queue,
                    isPlaying = playerState.isPlaying,
                    durationMs = playerState.duration,
                    currentPositionMs = playerState.currentPosition
                )

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
     * - If idle, wakes up and begins listening directly.
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

        startListening()
    }

    /**
     * Triggered when "Hey Orbit" is spoken by the user anywhere in the app or while in background.
     * Guaranteed single authoritative handler: guards against repeated callbacks, loops, and rapid fire.
     */
    fun onWakeWordDetected(command: String? = null) {
        val now = System.currentTimeMillis()
        if (now - lastWakeTimeMs < 2500L || _orbitState.value.isVoiceActive) {
            return
        }
        lastWakeTimeMs = now
        OrbitLogger.wakeDetected(command)
        triggerWakeSequence(command)
    }

    /**
     * Orbit Wake Sequence:
     * Hey Orbit -> Orbit activates -> Orbit responds naturally with voice (no generic assistant chimes).
     */
    private fun triggerWakeSequence(command: String?) {
        autoDismissJob?.cancel()
        followUpJob?.cancel()
        voiceSynthesizer.stopSpeaking()

        transitionToState(OrbitState.WAKE_DETECTED)
        _isVisualUiVisible.value = true

        scope.launch {
            transitionToState(OrbitState.ACTIVATING)
            delay(120L)

            if (!command.isNullOrBlank()) {
                _assistantMessage.value = "“$command”"
                processUtterance(command)
            } else {
                // Natural conversational voice response for standalone wake word
                val wakeGreeting = getPersonalizedWakeResponse()
                _dialogueContext.value = OrbitDialogueContext.AwaitingCommand()
                _assistantMessage.value = "I'm listening…"
                speakResponse(wakeGreeting, expectsFollowUp = true)
            }
        }
    }

    /**
     * Generates a warm, natural personalized wake response using the saved profile name.
     * Rotates greetings to avoid robotic repetition.
     */
    fun getPersonalizedWakeResponse(): String {
        val name = sessionManager.getCachedUserName()?.trim()
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        if (name.isNullOrBlank()) {
            return "Yes?"
        }

        val options = mutableListOf(
            "Yes, $name?",
            "Hey $name, what can I do for you?",
            "Yes?",
            "I'm here, $name."
        )

        val timeGreeting = when (hour) {
            in 5..11 -> "Good morning, $name."
            in 12..16 -> "Good afternoon, $name."
            in 17..21 -> "Good evening, $name."
            else -> "Hey $name, what would you like to hear?"
        }
        options.add(timeGreeting)

        return options.random()
    }

    fun startListening() {
        if (_orbitState.value == OrbitState.LISTENING || _orbitState.value == OrbitState.TRANSCRIBING) {
            // Already listening, guard against duplicate starts
            return
        }
        autoDismissJob?.cancel()
        voiceSynthesizer.stopSpeaking()

        if (!voiceSession.hasRecordAudioPermission()) {
            scope.launch {
                _events.emit(OrbitUiEvent.RequestRecordAudioPermission)
            }
            _assistantMessage.value = "Orbit needs microphone access to hear 'Hey Orbit' and your voice commands."
            transitionToState(OrbitState.ERROR)
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
            else -> "I'm listening…"
        }

        voiceSession.acquireMicForCommand(
            onPartialResult = { partialText ->
                if (partialText.isNotBlank()) {
                    followUpJob?.cancel()
                    transitionToState(OrbitState.TRANSCRIBING)
                    _assistantMessage.value = partialText
                    OrbitLogger.transcribing(partialText)
                }
            },
            onResult = { text ->
                followUpJob?.cancel()
                _assistantMessage.value = text
                OrbitLogger.transcript(text)
                processUtterance(text)
            },
            onError = { errorMsg ->
                OrbitLogger.error("Recognition error: $errorMsg")

                if (errorMsg.contains("timed out", ignoreCase = true) ||
                    errorMsg.contains("No speech", ignoreCase = true) ||
                    errorMsg.contains("couldn't hear", ignoreCase = true)) {
                    returnToIdleState()
                } else {
                    transitionToState(OrbitState.ERROR)
                    _assistantMessage.value = "Orbit couldn't hear that. Try again."
                    scheduleSafeReturn(2500L)
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

        val s = rawInput.trim().lowercase(Locale.ROOT)

        // Check for conversational dismissals in continuous conversation
        if (s in listOf("thank you", "thanks", "thats all", "that's all", "all done", "goodbye", "bye", "cancel", "close", "never mind", "nevermind", "exit")) {
            val closingResponse = if (s.contains("thank")) "You're welcome!" else "Alright."
            _dialogueContext.value = OrbitDialogueContext.Idle
            contextMemory.resetContext()
            _assistantMessage.value = closingResponse
            speakResponse(closingResponse, expectsFollowUp = false)
            return
        }

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
                _assistantMessage.value = "I'm listening…"
                val wakeGreeting = getPersonalizedWakeResponse()
                speakResponse(wakeGreeting, expectsFollowUp = true)
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

            // Spoken natural response without generic sound effects
            val speechText = result.spokenResponse
            // Only expect follow-up if explicitly required by intent (e.g. disambiguation or multi-turn query)
            val expectsFollowUp = result.requiresFollowUp

            if (speechText.isNotBlank()) {
                speakResponse(speechText, expectsFollowUp = expectsFollowUp)
            } else {
                if (expectsFollowUp) {
                    startContinuousListening(timeoutMs = 4000L)
                } else {
                    scheduleSafeReturn(1500L)
                }
            }
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
                    // Continuous conversation: wait for natural follow-up command with privacy timeout
                    startContinuousListening(timeoutMs = 5000L)
                } else {
                    returnToIdleState()
                }
            }
        )
    }

    /**
     * Continuous Conversation loop:
     * After Orbit answers a command, it stays receptive for follow-up commands (e.g. "make it louder",
     * "skip this one", "play something relaxing") without requiring "Hey Orbit" again.
     * Automatically and gracefully returns to idle/music state if silence timeout expires.
     */
    private fun startContinuousListening(timeoutMs: Long = 5000L) {
        followUpJob?.cancel()
        transitionToState(OrbitState.WAITING_FOR_FOLLOWUP)
        OrbitLogger.state(OrbitState.WAITING_FOR_FOLLOWUP)
        _assistantMessage.value = "I'm listening…"

        startListening()

        followUpJob = scope.launch {
            delay(timeoutMs)
            if (_orbitState.value == OrbitState.WAITING_FOR_FOLLOWUP || _orbitState.value == OrbitState.LISTENING) {
                OrbitLogger.transcript("Continuous conversation window closed after ${timeoutMs}ms silence")
                returnToIdleState()
            }
        }
    }

    /**
     * Requirement 21:
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
        autoDismissJob?.cancel()
        followUpJob?.cancel()
        voiceSession.releaseMic()
        voiceSynthesizer.stopSpeaking()
        musicPlayer.restoreVolume()
        _dialogueContext.value = OrbitDialogueContext.Idle
        transitionToState(OrbitState.CANCELLED)
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
            delay(200L)

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
            scheduleSafeReturn(3000L)
        }
    }
}
