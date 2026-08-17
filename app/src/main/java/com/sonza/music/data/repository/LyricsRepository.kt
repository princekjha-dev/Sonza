package com.sonza.music.data.repository

import com.sonza.music.core.database.LyricsDao
import com.sonza.music.core.database.LyricsEntity
import com.sonza.music.core.model.Lyrics
import com.sonza.music.core.model.LyricsLine
import com.sonza.music.core.model.LyricsWord
import com.sonza.music.data.source.DemoAudiophileSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

interface LyricsRepository {
    suspend fun getLyricsForTrack(trackId: String, rawMediaUri: String? = null): Lyrics
    fun parseLrcContent(trackId: String, lrcText: String): Lyrics
}

class LyricsRepositoryImpl(
    private val lyricsDao: LyricsDao
) : LyricsRepository {

    override suspend fun getLyricsForTrack(trackId: String, rawMediaUri: String?): Lyrics = withContext(Dispatchers.IO) {
        val cached = lyricsDao.getLyricsByTrackId(trackId)
        if (cached != null) {
            return@withContext parseLrcContent(trackId, cached.rawLrcContent)
        }

        // Check if demo source has lyrics
        val demo = DemoAudiophileSource.SAMPLE_TRACKS.find { it.id == trackId }
        if (demo != null) {
            return@withContext DemoAudiophileSource.getSampleLyrics(trackId)
        }

        // Return empty fallback
        Lyrics(trackId = trackId, isSynced = false, lines = emptyList())
    }

    override fun parseLrcContent(trackId: String, lrcText: String): Lyrics {
        val lines = mutableListOf<LyricsLine>()
        val linePattern = Pattern.compile("\\[(\\d{2}):(\\d{2})\\.(\\d{2,3})\\](.*)")
        val wordPattern = Pattern.compile("<(\\d{2}):(\\d{2})\\.(\\d{2,3})>([^<]*)")

        val rawLines = lrcText.lines()
        for (i in rawLines.indices) {
            val rawLine = rawLines[i].trim()
            val matcher = linePattern.matcher(rawLine)
            if (matcher.matches()) {
                val min = matcher.group(1)?.toLongOrNull() ?: 0L
                val sec = matcher.group(2)?.toLongOrNull() ?: 0L
                val msStr = matcher.group(3) ?: "00"
                val ms = if (msStr.length == 2) msStr.toLong() * 10 else msStr.toLong()
                val startTimeMs = (min * 60 * 1000) + (sec * 1000) + ms

                val textContent = matcher.group(4)?.trim() ?: ""

                // Check for word-level sync tags like <00:02.10>Word
                val words = mutableListOf<LyricsWord>()
                val wordMatcher = wordPattern.matcher(textContent)
                var lastWordEnd = startTimeMs

                while (wordMatcher.find()) {
                    val wMin = wordMatcher.group(1)?.toLongOrNull() ?: 0L
                    val wSec = wordMatcher.group(2)?.toLongOrNull() ?: 0L
                    val wMsStr = wordMatcher.group(3) ?: "00"
                    val wMs = if (wMsStr.length == 2) wMsStr.toLong() * 10 else wMsStr.toLong()
                    val wordStart = (wMin * 60 * 1000) + (wSec * 1000) + wMs
                    val wordText = wordMatcher.group(4) ?: ""

                    words.add(LyricsWord(word = wordText, startMs = wordStart, endMs = wordStart + 400L))
                    lastWordEnd = wordStart + 400L
                }

                val cleanText = if (words.isNotEmpty()) {
                    words.joinToString("") { it.word }
                } else {
                    textContent
                }

                // Temporary end time, refined with next line start time
                lines.add(
                    LyricsLine(
                        text = cleanText,
                        startMs = startTimeMs,
                        endMs = startTimeMs + 5000L,
                        words = words
                    )
                )
            }
        }

        // Adjust line end times to meet next line start time
        val refinedLines = lines.mapIndexed { idx, current ->
            val nextStart = if (idx < lines.lastIndex) lines[idx + 1].startMs else current.startMs + 5000L
            current.copy(endMs = nextStart)
        }

        return Lyrics(
            trackId = trackId,
            lines = refinedLines,
            isSynced = refinedLines.isNotEmpty(),
            hasWordSync = refinedLines.any { it.hasWordSync }
        )
    }
}
