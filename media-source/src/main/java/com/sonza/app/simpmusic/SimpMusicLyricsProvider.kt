package com.sonza.app.simpmusic

import com.sonza.app.providers.lyrics.BetterLyrics
import com.sonza.app.providers.lyrics.LyricsProvider
import javax.inject.Inject

/**
 * SimpMusic-backed lyrics lookup.
 *
 * This provider intentionally reuses the existing BetterLyrics fetcher until a
 * dedicated SimpMusic-backed service is added. Keeping the provider in the
 * registry preserves the Hilt graph and avoids KSP resolution failures.
 */
class SimpMusicLyricsProvider @Inject constructor() : LyricsProvider {
    override val name = "SimpMusic"

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?
    ): Result<String> = BetterLyrics.getLyrics(title, artist, duration, album)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
        callback: (String) -> Unit
    ) {
        BetterLyrics.getAllLyrics(title, artist, duration, album, callback)
    }
}
