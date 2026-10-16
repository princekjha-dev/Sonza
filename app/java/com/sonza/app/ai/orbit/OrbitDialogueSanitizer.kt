package com.sonza.app.ai.orbit

/**
 * Natural Conversational Sanitizer for Orbit Assistant.
 *
 * Prevents raw technical exceptions, Wikipedia disambiguation notices,
 * backend JSON, HTTP errors, stack traces, and internal search diagnostics
 * from ever reaching the user's screen or ears.
 *
 * Transforms any failure into warm, polite, natural assistant speech.
 */
object OrbitDialogueSanitizer {

    private val TECHNICAL_PATTERNS = listOf(
        "misspelling",
        "conflation",
        "disambiguation",
        "may refer to",
        "refer to:",
        "wikipedia",
        "duckduckgo",
        "search result",
        "exception",
        "nullpointer",
        "sockettimeout",
        "connectexception",
        "unknownhost",
        "httpexception",
        "http 4",
        "http 5",
        "status code",
        "stack trace",
        "traceback",
        "provider_error",
        "missing_configuration",
        "internal_error",
        "payload",
        "jsonobject",
        "api key",
        "proxy server",
        "endpoint",
        "looper",
        "speechrecognizer",
        "viewmodel",
        "coroutine",
        "intent:"
    )

    /**
     * Sanitizes raw dialogue or error strings into human-friendly speech.
     */
    fun sanitize(rawMessage: String?): String {
        if (rawMessage.isNullOrBlank()) {
            return "I didn't catch that."
        }

        val trimmed = rawMessage.trim()
        val lower = trimmed.lowercase()

        // 1. Wikipedia / Search engine conflation or disambiguation notices
        if (lower.contains("misspelling") ||
            lower.contains("conflation") ||
            lower.contains("may refer to") ||
            lower.contains("disambiguation") ||
            lower.startsWith("the term") && lower.contains("appears to be")
        ) {
            return "I'm not sure what you mean. Could you say that again?"
        }

        // 2. Raw JSON or technical payloads
        if (trimmed.startsWith("{") || trimmed.startsWith("[") || lower.contains("\"success\":") || lower.contains("\"error\":")) {
            return "Something went wrong. Try again?"
        }

        // 3. Network, DNS, connect, or timeout failures
        if (lower.contains("unknownhost") || lower.contains("connectexception") || lower.contains("no internet") || lower.contains("network")) {
            return "I'm having trouble connecting to the network right now."
        }
        if (lower.contains("timeout") || lower.contains("timed out") || lower.contains("took too long")) {
            return "That took a little too long. Try again?"
        }

        // 4. Missing music / track / artist search failures
        if (lower.contains("couldn't find") && lower.contains("song") || lower.contains("no song found")) {
            return "I couldn't find that song."
        }
        if (lower.contains("couldn't find") && lower.contains("artist")) {
            return "I couldn't find that artist."
        }

        // 5. Speech recognition failures
        if (lower.contains("speechrecognizer") || lower.contains("audio record") || lower.contains("couldn't hear")) {
            return "I didn't catch that. Could you say that again?"
        }

        // 6. Generic technical errors
        for (pattern in TECHNICAL_PATTERNS) {
            if (lower.contains(pattern)) {
                return "Something went wrong. Try again?"
            }
        }

        // 7. Strip markdown bold / code syntax for spoken & visual clarity
        return trimmed
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("`([^`]+)`"), "$1")
            .trim()
    }
}
