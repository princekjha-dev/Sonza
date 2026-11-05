package com.sonza.app.ai.orbit

import android.content.Intent
import android.service.voice.VoiceInteractionService

/**
 * Standard Android VoiceInteractionService implementation.
 * Allows Sonza's Orbit to be chosen as the user's default Digital Assistant in Android Settings.
 * This provides legitimate system-wide voice activation from the home screen, lock screen,
 * or over other apps via home long-press or corner swipe.
 */
class OrbitVoiceInteractionService : VoiceInteractionService() {

    override fun onReady() {
        super.onReady()
    }

    override fun onShutdown() {
        super.onShutdown()
    }
}
