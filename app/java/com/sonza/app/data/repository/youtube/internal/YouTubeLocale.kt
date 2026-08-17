package com.sonza.app.data.repository.youtube.internal

import com.sonza.app.data.SessionManager

/**
 * Resolves the `hl` (host language) YouTube Music should answer a browse request in.
 *
 * Nearly every browse call needs this, and every call site used to re-derive it the same
 * way — read the preferred languages, map the first one, fall back to English. Keeping the
 * rule and the language table in one place stops those copies from drifting apart.
 */
object YouTubeLocale {

    /** The `hl` for this user, from their first preferred language. */
    suspend fun hl(sessionManager: SessionManager): String {
        val preferred = sessionManager.getPreferredLanguages()
        return preferred.firstOrNull()?.let { languageCode(it) } ?: YouTubeConfig.DEFAULT_HL
    }

    /** Maps a human language name to the ISO code YouTube Music expects. */
    fun languageCode(languageName: String): String = when (languageName.lowercase()) {
        "hindi" -> "hi"
        "bengali" -> "bn"
        "punjabi" -> "pa"
        "marathi" -> "mr"
        "gujarati" -> "gu"
        "tamil" -> "ta"
        "telugu" -> "te"
        "kannada" -> "kn"
        "malayalam" -> "ml"
        "urdu" -> "ur"
        "spanish" -> "es"
        "portuguese" -> "pt"
        "french" -> "fr"
        "german" -> "de"
        "italian" -> "it"
        "korean" -> "ko"
        "japanese" -> "ja"
        "chinese" -> "zh"
        "arabic" -> "ar"
        "russian" -> "ru"
        "turkish" -> "tr"
        "indonesian" -> "id"
        "english" -> "en"
        else -> YouTubeConfig.DEFAULT_HL
    }
}
