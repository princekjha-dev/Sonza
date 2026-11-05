package com.sonza.app.data.repository.youtube.lyrics

import com.sonza.app.data.repository.youtube.internal.YouTubeApiClient
import com.sonza.app.data.repository.youtube.internal.YouTubeContentParser
import com.sonza.app.providers.lyrics.Lyrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fetches YouTube Music's own lyrics, preferring time-synced lines and falling back to
 * plain text.
 *
 * It takes two calls: `/next` names a lyrics browse id for the video, and `/browse` on that
 * id returns the lines. Both work signed out, so lyrics don't require an account.
 */
@Singleton
class YouTubeLyricsService @Inject constructor(
    private val apiClient: YouTubeApiClient,
    private val parser: YouTubeContentParser
) {

    suspend fun getLyrics(videoId: String): Lyrics? = withContext(Dispatchers.IO) {
        try {
            val nextResponse = apiClient.fetchPublicApiWithBody("next", """{"videoId": "$videoId"}""")
            if (nextResponse.isBlank()) return@withContext null

            val browseId = parser.extractLyricsBrowseId(JSONObject(nextResponse)) ?: return@withContext null

            val browseResponse = apiClient.fetchPublicApiWithBody("browse", """{"browseId": "$browseId"}""")
            if (browseResponse.isBlank()) return@withContext null

            parser.parseLyrics(JSONObject(browseResponse))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
