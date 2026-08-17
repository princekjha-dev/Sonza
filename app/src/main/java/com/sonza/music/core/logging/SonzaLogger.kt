package com.sonza.music.core.logging

import android.util.Log

object SonzaLogger {
    private const val TAG_PREFIX = "SONZA_"

    fun d(tag: String, message: String) {
        Log.d("$TAG_PREFIX$tag", sanitize(message))
    }

    fun i(tag: String, message: String) {
        Log.i("$TAG_PREFIX$tag", sanitize(message))
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w("$TAG_PREFIX$tag", sanitize(message), throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$TAG_PREFIX$tag", sanitize(message), throwable)
    }

    /**
     * Sanitizes logs to prevent sensitive credentials, tokens, or PII from being output
     */
    private fun sanitize(message: String): String {
        return message
            .replace(Regex("bearer\\s+[A-Za-z0-9-_.]+", RegexOption.IGNORE_CASE), "Bearer [REDACTED]")
            .replace(Regex("token=[A-Za-z0-9-_.]+", RegexOption.IGNORE_CASE), "token=[REDACTED]")
            .replace(Regex("password=[^&\\s]+", RegexOption.IGNORE_CASE), "password=[REDACTED]")
    }
}
