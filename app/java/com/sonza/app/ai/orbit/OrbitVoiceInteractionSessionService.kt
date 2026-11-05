package com.sonza.app.ai.orbit

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import com.sonza.app.MainActivity

/**
 * Handles the session when Orbit is invoked as the system assistant.
 * Immediately opens the Orbit Voice UI in listening mode.
 */
class OrbitVoiceInteractionSessionService : VoiceInteractionSessionService() {

    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return OrbitVoiceSession(this)
    }

    private class OrbitVoiceSession(context: Context) : VoiceInteractionSession(context) {

        override fun onShow(args: Bundle?, showFlags: Int) {
            super.onShow(args, showFlags)

            // Launch MainActivity with the Orbit voice trigger deep link
            val intent = Intent(context, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = Uri.parse("sonza://orbit_listen")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
            hide()
        }
    }
}
