package com.sonza.app.data

import java.net.URI

/**
 * Centralized configuration for Sonza Feedback service.
 * Allows runtime and deployment reconfiguration of the feedback endpoint.
 */
object FeedbackConfig {
    /**
     * Default base URL for Sonza Feedback API.
     */
    const val DEFAULT_BASE_URL: String = "https://feedback.naruto.in"

    /**
     * Relative API path for submitting feedback.
     */
    const val FEEDBACK_PATH: String = "/api/feedback"

    /**
     * Current base URL. Configurable at runtime without modifying UI code.
     */
    @Volatile
    var baseUrl: String = DEFAULT_BASE_URL

    /**
     * Validates if a given URL string is a syntactically valid HTTP/HTTPS endpoint.
     */
    fun isValidEndpoint(url: String): Boolean {
        if (url.isBlank()) return false
        return try {
            val uri = URI(url.trim())
            (uri.scheme.equals("https", ignoreCase = true) || uri.scheme.equals("http", ignoreCase = true)) &&
                !uri.host.isNullOrBlank()
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Returns the fully resolved feedback submission URL.
     */
    fun getFeedbackUrl(): String {
        val base = baseUrl.trim().trimEnd('/')
        return "$base$FEEDBACK_PATH"
    }

    /**
     * Resets the base URL back to default.
     */
    fun resetToDefault() {
        baseUrl = DEFAULT_BASE_URL
    }
}
