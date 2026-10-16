package com.sonza.app.ai.orbit

/**
 * Sonic Identity System for Sonza's Orbit AI Assistant.
 *
 * Requirements:
 * - Completely removed all Google Assistant-style sounds and generic assistant chimes/beeps.
 * - Orbit uses its own voice as the primary audio identity.
 * - Zero artificial beeps or chimes when activating, listening, succeeding, or erroring.
 */
object OrbitSoundManager {

    /**
     * Orbit relies purely on natural conversational voice feedback.
     * All generic activation, listening, confirmation, and error beeps are completely silenced.
     */
    fun playActivation() {
        // No-op: Orbit activates silently or responds directly with natural voice
    }

    fun playWakeWordDetected() {
        // No-op
    }

    fun playListening() {
        // No-op
    }

    fun playCommandReceived() {
        // No-op
    }

    fun playCommandUnderstood() {
        // No-op
    }

    fun playSuccess() {
        // No-op
    }

    fun playError() {
        // No-op
    }

    fun playClosed() {
        // No-op
    }
}
