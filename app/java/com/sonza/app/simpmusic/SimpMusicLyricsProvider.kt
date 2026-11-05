package com.sonza.app.simpmusic

import com.sonza.app.providers.lyrics.LyricsData
import com.sonza.app.providers.lyrics.LyricsProvider
import com.sonza.app.providers.lyrics.SimpMusicApiResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * SimpMusic lyrics provider.
 * Fetches time-synced / richSync lyrics from the SimpMusic community lyrics API.
 */
@Singleton
class SimpMusicLyricsProvider @Inject constructor() : LyricsProvider {

    override val name: String = "SimpMusic"

    private val client by lazy {
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        isLenient = true
                        ignoreUnknownKeys = true
                    }
                )
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 15000
                connectTimeoutMillis = 10000
                socketTimeoutMillis = 15000
            }

            defaultRequest {
                url("https://api-lyrics.simpmusic.org")
            }

            expectSuccess = false
        }
    }

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            // 1. Try querying by video/track ID first if valid
            var responseData: List<LyricsData>? = null
            if (id.isNotBlank()) {
                val resp = client.get("/api/v1/lyrics") {
                    parameter("id", id)
                }
                if (resp.status == HttpStatusCode.OK) {
                    val apiResponse = resp.body<SimpMusicApiResponse>()
                    if (apiResponse.success && apiResponse.data.isNotEmpty()) {
                        responseData = apiResponse.data
                    }
                }
            }

            // 2. If no data by ID, try searching by track title & artist
            if (responseData.isNullOrEmpty()) {
                val resp = client.get("/api/v1/lyrics") {
                    parameter("name", title)
                    parameter("artist", artist)
                    if (duration > 0) {
                        parameter("duration", duration)
                    }
                    if (!album.isNullOrBlank()) {
                        parameter("album", album)
                    }
                }
                if (resp.status == HttpStatusCode.OK) {
                    val apiResponse = resp.body<SimpMusicApiResponse>()
                    if (apiResponse.success && apiResponse.data.isNotEmpty()) {
                        responseData = apiResponse.data
                    }
                }
            }

            val item = responseData?.firstOrNull()
                ?: throw IllegalStateException("No lyrics found on SimpMusic for $title")

            // Prefer richSyncLyrics (word-level timestamps) -> syncedLyrics -> plainLyrics
            val lyrics = item.richSyncLyrics?.takeIf { it.isNotBlank() }
                ?: item.syncedLyrics?.takeIf { it.isNotBlank() }
                ?: item.plainLyrics?.takeIf { it.isNotBlank() }
                ?: throw IllegalStateException("Empty lyrics content from SimpMusic")

            lyrics
        }
    }

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        album: String?,
        callback: (String) -> Unit
    ) {
        getLyrics(id, title, artist, duration, album).onSuccess(callback)
    }
}
