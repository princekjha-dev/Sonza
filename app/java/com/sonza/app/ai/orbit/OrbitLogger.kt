package com.sonza.app.ai.orbit

import android.util.Log

/**
 * Structured debug logger for Orbit voice assistant operations.
 * Complies with specification: never logs raw audio data, only structured events and lifecycle state transitions.
 */
object OrbitLogger {
    private const val TAG = "ORBIT"

    fun state(state: OrbitState) {
        Log.i(TAG, "[ORBIT] STATE: $state")
    }

    fun wakeDetected(command: String? = null) {
        if (!command.isNullOrBlank()) {
            Log.i(TAG, "[ORBIT] WAKE_DETECTED command=$command")
        } else {
            Log.i(TAG, "[ORBIT] WAKE_DETECTED")
        }
    }

    fun listening() {
        Log.i(TAG, "[ORBIT] LISTENING")
    }

    fun transcribing(partial: String) {
        Log.d(TAG, "[ORBIT] TRANSCRIBING: $partial")
    }

    fun transcript(text: String) {
        Log.i(TAG, "[ORBIT] TRANSCRIPT: $text")
    }

    fun intent(name: String, params: String = "") {
        if (params.isNotBlank()) {
            Log.i(TAG, "[ORBIT] INTENT: $name PARAMS: $params")
        } else {
            Log.i(TAG, "[ORBIT] INTENT: $name")
        }
    }

    fun executing() {
        Log.i(TAG, "[ORBIT] EXECUTING")
    }

    fun result(success: Boolean, message: String) {
        Log.i(TAG, "[ORBIT] RESULT: ${if (success) "SUCCESS" else "ERROR"} - $message")
    }

    fun tts(text: String) {
        Log.i(TAG, "[ORBIT] TTS: $text")
    }

    fun micOwnership(owner: String, action: String) {
        Log.i(TAG, "[ORBIT] MIC: $owner -> $action")
    }

    fun error(msg: String, throwable: Throwable? = null) {
        Log.e(TAG, "[ORBIT] ERROR: $msg", throwable)
    }

    fun recovery(reason: String) {
        Log.w(TAG, "[ORBIT] RECOVERY: $reason")
    }
}
