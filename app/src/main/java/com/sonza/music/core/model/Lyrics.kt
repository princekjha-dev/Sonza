package com.sonza.music.core.model

import kotlinx.serialization.Serializable

@Serializable
data class LyricsWord(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

@Serializable
data class LyricsLine(
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val words: List<LyricsWord> = emptyList()
) {
    val hasWordSync: Boolean
        get() = words.isNotEmpty()
}

@Serializable
data class Lyrics(
    val trackId: String,
    val lines: List<LyricsLine> = emptyList(),
    val isSynced: Boolean = true,
    val hasWordSync: Boolean = false,
    val offsetMs: Long = 0,
    val source: String = "LRC_EMBEDDED"
) {
    /**
     * Finds active line index for current playback time
     */
    fun getActiveLineIndex(positionMs: Long): Int {
        if (lines.isEmpty()) return -1
        val adjustedPos = positionMs + offsetMs
        for (i in lines.indices) {
            val line = lines[i]
            if (adjustedPos in line.startMs..line.endMs) {
                return i
            }
            if (adjustedPos < line.startMs) {
                return (i - 1).coerceAtLeast(0)
            }
        }
        return lines.lastIndex
    }
}
