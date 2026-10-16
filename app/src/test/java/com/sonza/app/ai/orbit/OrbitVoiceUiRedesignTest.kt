package com.sonza.app.ai.orbit

import org.junit.Assert.*
import org.junit.Test

/**
 * Unit tests for Orbit Next-Generation Voice UI Redesign:
 * 1. Natural error dialogue handling (zero search engine diagnostics, JSON, or stack traces).
 * 2. Proper assistant state machine (IDLE -> LISTENING -> THINKING -> SPEAKING -> IDLE).
 * 3. Sonic identity (zero Google Assistant sounds).
 * 4. Safe conversational responses.
 */
class OrbitVoiceUiRedesignTest {

    @Test
    fun testDialogueSanitizer_RemovesConflationAndMisspellingDiagnostics() {
        val wikipediaConflation = "The term dopamine appears to be a misspelling or a conflation of dopamine receptor."
        val sanitized = OrbitDialogueSanitizer.sanitize(wikipediaConflation)
        assertEquals("I'm not sure what you mean. Could you say that again?", sanitized)

        val disambiguation = "Dopamine may refer to: Dopamine (neurotransmitter), Dopamine (song)..."
        val sanitizedDisambig = OrbitDialogueSanitizer.sanitize(disambiguation)
        assertEquals("I'm not sure what you mean. Could you say that again?", sanitizedDisambig)
    }

    @Test
    fun testDialogueSanitizer_RemovesRawTechnicalPayloadsAndExceptions() {
        val rawJson = "{\"success\":false,\"error\":{\"code\":\"PROVIDER_ERROR\",\"message\":\"Bad gateway\"}}"
        val sanitizedJson = OrbitDialogueSanitizer.sanitize(rawJson)
        assertEquals("Something went wrong. Try again?", sanitizedJson)

        val exceptionMsg = "java.net.SocketTimeoutException: failed to connect to host within 10000ms"
        val sanitizedTimeout = OrbitDialogueSanitizer.sanitize(exceptionMsg)
        assertEquals("That took a little too long. Try again?", sanitizedTimeout)

        val unknownHost = "java.net.UnknownHostException: Unable to resolve host"
        val sanitizedHost = OrbitDialogueSanitizer.sanitize(unknownHost)
        assertEquals("I'm having trouble connecting to the network right now.", sanitizedHost)
    }

    @Test
    fun testDialogueSanitizer_HumanizesSpeechAndMusicErrors() {
        val speechError = "SpeechRecognizer ERROR_NO_MATCH"
        val sanitizedSpeech = OrbitDialogueSanitizer.sanitize(speechError)
        assertEquals("I didn't catch that. Could you say that again?", sanitizedSpeech)

        val songNotFound = "I couldn't find the song 'NonexistentTrack' in your library"
        val sanitizedSong = OrbitDialogueSanitizer.sanitize(songNotFound)
        assertEquals("I couldn't find that song.", sanitizedSong)
    }

    @Test
    fun testDialogueSanitizer_PreservesCleanConversationalResponses() {
        val normalResponse = "Playing **Dopamine** by BØRNS."
        val sanitized = OrbitDialogueSanitizer.sanitize(normalResponse)
        assertEquals("Playing Dopamine by BØRNS.", sanitized)

        val casualReply = "Sure, playing your workout mix."
        val sanitizedCasual = OrbitDialogueSanitizer.sanitize(casualReply)
        assertEquals("Sure, playing your workout mix.", sanitizedCasual)
    }

    @Test
    fun testOrbitStateMachine_VoiceActiveLifecycle() {
        // IDLE states are NOT voice active
        assertFalse(OrbitState.IDLE.isVoiceActive)
        assertFalse(OrbitState.ORBITING.isVoiceActive)
        assertFalse(OrbitState.MUSIC_REACTIVE.isVoiceActive)

        // Voice interaction states ARE voice active
        assertTrue(OrbitState.WAKE_DETECTED.isVoiceActive)
        assertTrue(OrbitState.LISTENING.isVoiceActive)
        assertTrue(OrbitState.THINKING.isVoiceActive)
        assertTrue(OrbitState.SPEAKING.isVoiceActive)

        // Listening flag
        assertTrue(OrbitState.LISTENING.isListening)
        assertFalse(OrbitState.THINKING.isListening)
        assertFalse(OrbitState.SPEAKING.isListening)
    }

    @Test
    fun testOrbitSoundManager_AllSoundsSilenced() {
        // Must execute cleanly as no-ops without throwing exceptions
        OrbitSoundManager.playActivation()
        OrbitSoundManager.playWakeWordDetected()
        OrbitSoundManager.playListening()
        OrbitSoundManager.playCommandReceived()
        OrbitSoundManager.playCommandUnderstood()
        OrbitSoundManager.playSuccess()
        OrbitSoundManager.playError()
        OrbitSoundManager.playClosed()
    }
}
