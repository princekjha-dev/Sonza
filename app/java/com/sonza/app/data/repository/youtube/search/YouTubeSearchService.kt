package com.sonza.app.data.repository.youtube.search

import com.sonza.app.cache.OfflineCache
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.Artist
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.SearchResults
import com.sonza.app.core.model.Song
import com.sonza.app.data.SessionManager
import com.sonza.app.data.error.toAppError
import com.sonza.app.data.repository.youtube.internal.YouTubeApiClient
import com.sonza.app.data.repository.youtube.internal.YouTubeConfig
import com.sonza.app.data.repository.youtube.internal.YouTubeContentParser
import com.sonza.app.data.repository.youtube.internal.YouTubeJsonParser
import com.sonza.app.data.repository.youtube.internal.YouTubeLocale
import com.sonza.app.data.repository.youtube.internal.addYouTubeAuthHeaders
import com.sonza.app.telemetry.Telemetry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles all YouTube Music search functionality.
 * Canonical search uses YouTube Music InnerTube endpoint with proper entity type classifications.
 */
@Singleton
class YouTubeSearchService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val sessionManager: SessionManager,
    private val jsonParser: YouTubeJsonParser,
    private val apiClient: YouTubeApiClient,
    private val parser: YouTubeContentParser
) {

    companion object {
        const val FILTER_SONGS = "music_songs"
        const val FILTER_VIDEOS = "music_videos"
        const val FILTER_ALBUMS = "music_albums"
        const val FILTER_PLAYLISTS = "music_playlists"
        const val FILTER_ARTISTS = "music_artists"
        
        // Canonical YouTube Music InnerTube search filter params tokens
        private const val PARAMS_SONGS = "EgWKAQIIAWoQEAMQBBAJEA4QChAFEBEQEBA="
        private const val PARAMS_ALBUMS = "EgWKAQIYAWoQEAMQBBAJEA4QChAFEBEQEBA="
        private const val PARAMS_ARTISTS = "EgWKAQIgAWoQEAMQBBAJEA4QChAFEBEQEBA="
        private const val PARAMS_PLAYLISTS = "EgWKAQIwAWoQEAMQBBAJEA4QChAFEBEQEBA="
    }

    /**
     * Grouped Search for YouTube Music InnerTube (All tab).
     * Returns canonical SearchResults containing songs, albums, artists, and playlists without mixing.
     */
    suspend fun searchAll(query: String): SearchResults = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext SearchResults()

        val cacheKey = "yt:all:${trimmedQuery.lowercase()}"
        try {
            val hl = YouTubeLocale.hl(sessionManager)
            val jsonQuery = JSONObject.quote(trimmedQuery)
            val requestBody = """{ "query": $jsonQuery }"""

            val response = apiClient.fetchPublicApiWithBody("search", requestBody, hl = hl)
            if (response.isNotBlank()) {
                val results = parser.parseSearchResults(response)
                if (results.songs.isNotEmpty() || results.artists.isNotEmpty() || results.albums.isNotEmpty() || results.playlists.isNotEmpty()) {
                    if (results.songs.isNotEmpty()) OfflineCache.putSearch("yt:music_songs:${trimmedQuery.lowercase()}", results.songs)
                    if (results.artists.isNotEmpty()) OfflineCache.putArtists(trimmedQuery, results.artists)
                    if (results.albums.isNotEmpty()) OfflineCache.putAlbums(trimmedQuery, results.albums)
                    if (results.playlists.isNotEmpty()) OfflineCache.putPlaylists(trimmedQuery, results.playlists)
                    return@withContext results
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Telemetry.report("searchAll", "youtube", e.toAppError(), mapOf("qlen" to trimmedQuery.length.toString()))
        }

        // Fallback if InnerTube search fails (e.g. offline): read from cache
        val cachedSongs = OfflineCache.getSearch("yt:music_songs:${trimmedQuery.lowercase()}") ?: emptyList()
        val cachedArtists = OfflineCache.getArtists(trimmedQuery) ?: emptyList()
        val cachedAlbums = OfflineCache.getAlbums(trimmedQuery) ?: emptyList()
        val cachedPlaylists = OfflineCache.getPlaylists(trimmedQuery) ?: emptyList()

        if (cachedSongs.isNotEmpty() || cachedArtists.isNotEmpty() || cachedAlbums.isNotEmpty() || cachedPlaylists.isNotEmpty()) {
            return@withContext SearchResults(
                songs = cachedSongs,
                artists = cachedArtists,
                albums = cachedAlbums,
                playlists = cachedPlaylists
            )
        }

        // If cache empty, query typed searches cleanly
        val songs = search(trimmedQuery, FILTER_SONGS)
        val artists = searchArtists(trimmedQuery)
        val albums = searchAlbums(trimmedQuery)
        val playlists = searchPlaylists(trimmedQuery)

        SearchResults(songs = songs, artists = artists, albums = albums, playlists = playlists)
    }

    /**
     * Search for songs on YouTube Music.
     */
    suspend fun search(query: String, filter: String = FILTER_SONGS): List<Song> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext emptyList()

        val cacheKey = "yt:$filter:${trimmedQuery.lowercase()}"
        try {
            val hl = YouTubeLocale.hl(sessionManager)
            val jsonQuery = JSONObject.quote(trimmedQuery)
            val requestBody = """{ "query": $jsonQuery, "params": "$PARAMS_SONGS" }"""

            val response = apiClient.fetchPublicApiWithBody("search", requestBody, hl = hl)
            if (response.isNotBlank()) {
                val songs = parser.parseSearchSongs(response)
                if (songs.isNotEmpty()) {
                    OfflineCache.putSearch(cacheKey, songs)
                    return@withContext songs
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("YouTubeSearch", "InnerTube search failed: ${e.message}")
        }

        // Fallback to NewPipe if InnerTube fails
        try {
            val ytService = ServiceList.all().find { it.serviceInfo.name == "YouTube" }
                ?: return@withContext (OfflineCache.getSearch(cacheKey) ?: emptyList())

            val searchExtractor = ytService.getSearchExtractor(trimmedQuery, listOf(FILTER_SONGS), "")
            searchExtractor.fetchPage()

            val songs = searchExtractor.initialPage.items.filterIsInstance<StreamInfoItem>().mapNotNull { item ->
                try {
                    val artistId = item.uploaderUrl?.let { url ->
                        when {
                            url.contains("/channel/") -> url.substringAfter("/channel/").substringBefore("/").substringBefore("?")
                            url.contains("/@") -> null
                            else -> null
                        }
                    }

                    Song.fromYouTube(
                        videoId = jsonParser.extractVideoId(item.url),
                        title = item.name ?: "Unknown",
                        artist = item.uploaderName ?: "Unknown Artist",
                        album = "",
                        duration = item.duration * 1000L,
                        thumbnailUrl = item.thumbnails.maxByOrNull { it.width * it.height }?.url,
                        artistId = artistId,
                        isVideo = false,
                        isMembersOnly = false
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (songs.isNotEmpty()) OfflineCache.putSearch(cacheKey, songs)
            songs
        } catch (e: Exception) {
            e.printStackTrace()
            Telemetry.report("search", "youtube", e.toAppError(), mapOf("qlen" to trimmedQuery.length.toString()))
            OfflineCache.getSearch(cacheKey) ?: emptyList()
        }
    }

    /**
     * Search for artists on YouTube Music.
     */
    suspend fun searchArtists(query: String): List<Artist> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext emptyList()

        val cacheKey = "yt:${trimmedQuery.lowercase()}"
        try {
            val hl = YouTubeLocale.hl(sessionManager)
            val jsonQuery = JSONObject.quote(trimmedQuery)
            val requestBody = """{ "query": $jsonQuery, "params": "$PARAMS_ARTISTS" }"""

            val response = apiClient.fetchPublicApiWithBody("search", requestBody, hl = hl)
            if (response.isNotBlank()) {
                val artists = parser.parseSearchArtists(response)
                if (artists.isNotEmpty()) {
                    OfflineCache.putArtists(trimmedQuery, artists)
                    return@withContext artists
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("YouTubeSearch", "InnerTube searchArtists failed: ${e.message}")
        }

        // Fallback to NewPipe channel search if InnerTube fails
        try {
            val ytService = ServiceList.all().find { it.serviceInfo.name == "YouTube" }
                ?: return@withContext (OfflineCache.getArtists(cacheKey) ?: emptyList())

            val searchExtractor = ytService.getSearchExtractor(trimmedQuery, listOf("channels"), "")
            searchExtractor.fetchPage()

            val artists = searchExtractor.initialPage.items.filterIsInstance<org.schabi.newpipe.extractor.channel.ChannelInfoItem>().take(5).mapNotNull { item ->
                try {
                    val channelId = item.url?.substringAfter("/channel/")?.substringBefore("/")?.substringBefore("?")
                    if (channelId.isNullOrBlank()) return@mapNotNull null

                    Artist(
                        id = channelId,
                        name = item.name ?: "Unknown Artist",
                        thumbnailUrl = item.thumbnails.lastOrNull()?.url,
                        subscribers = item.subscriberCount.let {
                            if (it >= 1_000_000) "${it / 1_000_000}M subscribers"
                            else if (it >= 1_000) "${it / 1_000}K subscribers"
                            else "$it subscribers"
                        }
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (artists.isNotEmpty()) OfflineCache.putArtists(cacheKey, artists)
            artists
        } catch (e: Exception) {
            e.printStackTrace()
            Telemetry.report("searchArtists", "youtube", e.toAppError(), mapOf("qlen" to trimmedQuery.length.toString()))
            OfflineCache.getArtists(cacheKey) ?: emptyList()
        }
    }

    /**
     * Search for playlists on YouTube Music.
     */
    suspend fun searchPlaylists(query: String): List<Playlist> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext emptyList()

        val cacheKey = "yt:${trimmedQuery.lowercase()}"
        try {
            val hl = YouTubeLocale.hl(sessionManager)
            val jsonQuery = JSONObject.quote(trimmedQuery)
            val requestBody = """{ "query": $jsonQuery, "params": "$PARAMS_PLAYLISTS" }"""

            val response = apiClient.fetchPublicApiWithBody("search", requestBody, hl = hl)
            if (response.isNotBlank()) {
                val playlists = parser.parseSearchPlaylists(response)
                if (playlists.isNotEmpty()) {
                    OfflineCache.putPlaylists(trimmedQuery, playlists)
                    return@withContext playlists
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("YouTubeSearch", "InnerTube searchPlaylists failed: ${e.message}")
        }

        // Fallback to NewPipe if InnerTube fails
        try {
            val ytService = ServiceList.all().find { it.serviceInfo.name == "YouTube" }
                ?: return@withContext (OfflineCache.getPlaylists(cacheKey) ?: emptyList())

            val searchExtractor = ytService.getSearchExtractor(trimmedQuery, listOf(FILTER_PLAYLISTS), "")
            searchExtractor.fetchPage()

            val playlists = searchExtractor.initialPage.items.filterIsInstance<org.schabi.newpipe.extractor.playlist.PlaylistInfoItem>().take(10).mapNotNull { item ->
                try {
                    val playlistId = item.url?.substringAfter("list=")?.substringBefore("&")
                    if (playlistId.isNullOrBlank()) return@mapNotNull null

                    Playlist(
                        id = playlistId,
                        title = item.name ?: "Unknown Playlist",
                        author = item.uploaderName ?: "",
                        thumbnailUrl = item.thumbnails.lastOrNull()?.url,
                        songs = emptyList()
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (playlists.isNotEmpty()) OfflineCache.putPlaylists(cacheKey, playlists)
            playlists
        } catch (e: Exception) {
            e.printStackTrace()
            Telemetry.report("searchPlaylists", "youtube", e.toAppError(), mapOf("qlen" to trimmedQuery.length.toString()))
            OfflineCache.getPlaylists(cacheKey) ?: emptyList()
        }
    }

    /**
     * Search for albums on YouTube Music.
     */
    suspend fun searchAlbums(query: String): List<Album> = withContext(Dispatchers.IO) {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@withContext emptyList()

        val cacheKey = "yt:${trimmedQuery.lowercase()}"
        try {
            val hl = YouTubeLocale.hl(sessionManager)
            val jsonQuery = JSONObject.quote(trimmedQuery)
            val requestBody = """{ "query": $jsonQuery, "params": "$PARAMS_ALBUMS" }"""

            val response = apiClient.fetchPublicApiWithBody("search", requestBody, hl = hl)
            if (response.isNotBlank()) {
                val albums = parser.parseSearchAlbums(response)
                if (albums.isNotEmpty()) {
                    OfflineCache.putAlbums(trimmedQuery, albums)
                    return@withContext albums
                }
            }
        } catch (e: Exception) {
            android.util.Log.w("YouTubeSearch", "InnerTube searchAlbums failed: ${e.message}")
        }

        // Fallback to NewPipe if InnerTube fails
        try {
            val ytService = ServiceList.all().find { it.serviceInfo.name == "YouTube" }
                ?: return@withContext (OfflineCache.getAlbums(cacheKey) ?: emptyList())

            val searchExtractor = ytService.getSearchExtractor(trimmedQuery, listOf(FILTER_ALBUMS), "")
            searchExtractor.fetchPage()

            val albums = searchExtractor.initialPage.items.filterIsInstance<org.schabi.newpipe.extractor.playlist.PlaylistInfoItem>().mapNotNull { item ->
                try {
                    val albumId = item.url?.substringAfter("list=")?.substringBefore("&")
                    if (albumId.isNullOrBlank()) return@mapNotNull null

                    Album(
                        id = albumId,
                        title = item.name ?: "Unknown Album",
                        artist = item.uploaderName ?: "Unknown Artist",
                        thumbnailUrl = item.thumbnails.lastOrNull()?.url,
                        year = ""
                    )
                } catch (e: Exception) {
                    null
                }
            }
            if (albums.isNotEmpty()) OfflineCache.putAlbums(cacheKey, albums)
            albums
        } catch (e: Exception) {
            e.printStackTrace()
            Telemetry.report("searchAlbums", "youtube", e.toAppError(), mapOf("qlen" to trimmedQuery.length.toString()))
            OfflineCache.getAlbums(cacheKey) ?: emptyList()
        }
    }

    /**
     * Get search suggestions for autocomplete.
     */
    suspend fun getSearchSuggestions(query: String): List<String> = withContext(Dispatchers.IO) {
        if (query.isBlank()) return@withContext emptyList()
        
        try {
            val url = "https://suggestqueries-clients6.youtube.com/complete/search?client=youtube&ds=yt&q=${java.net.URLEncoder.encode(query, "UTF-8")}"
            
            val request = okhttp3.Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .build()
            
            val response = okHttpClient.newCall(request).execute()
            val body = response.body.string()
            
            // Response format: window.google.ac.h(["query",[["suggestion1",0],["suggestion2",0],...]])
            val jsonStart = body.indexOf("[[")
            val jsonEnd = body.lastIndexOf("]]") + 2
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) return@withContext emptyList()
            
            val suggestionsArray = JSONArray(body.substring(jsonStart, jsonEnd))
            val suggestions = mutableListOf<String>()
            
            for (i in 0 until suggestionsArray.length()) {
                val suggestionItem = suggestionsArray.optJSONArray(i)
                if (suggestionItem != null && suggestionItem.length() > 0) {
                    val text = suggestionItem.optString(0)
                    if (text.isNotBlank()) {
                        suggestions.add(text)
                    }
                }
            }
            
            suggestions.take(8) // Limit to 8 suggestions
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    /**
     * Get related songs (Up Next / Radio) for a specific video.
     * Uses YouTube Music's "next" endpoint which provides the official recommendations.
     */
    suspend fun getRelatedSongs(videoId: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val cookies = sessionManager.getCookies()
            val authUser = sessionManager.getAuthUserIndex()

            val jsonBody = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", YouTubeConfig.CLIENT_NAME)
                        put("clientVersion", YouTubeConfig.CLIENT_VERSION)
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("videoId", videoId)
                put("playlistId", "RDAMVM$videoId")
                put("enablePersistentPlaylistPanel", true)
                put("isAudioOnly", true)
            }

            val request = okhttp3.Request.Builder()
                .url("${YouTubeConfig.BASE_URL}/next")
                .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
                .apply {
                    addYouTubeAuthHeaders(cookies, authUser)
                }
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body.string()
            
            parseSongsFromNextResponse(responseBody)
        } catch (e: Exception) {
            e.printStackTrace()
            Telemetry.report("relatedSongs", "youtube", e.toAppError())
            emptyList()
        }
    }

    private fun parseSongsFromNextResponse(json: String): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            val root = JSONObject(json)
            val playlistItems = mutableListOf<JSONObject>()
            jsonParser.findAllObjects(root, "playlistPanelVideoRenderer", playlistItems)
            
            if (playlistItems.isNotEmpty()) {
                for (item in playlistItems) {
                    val videoId = item.optString("videoId")
                    if (videoId.isNullOrBlank()) continue
                    
                    val title = jsonParser.getRunText(item.optJSONObject("title")) ?: "Unknown"
                    
                    val bylineObj = item.optJSONObject("longBylineText") ?: item.optJSONObject("shortBylineText")
                    val fullByline = jsonParser.getRunText(bylineObj) ?: ""
                    
                    val parts = fullByline.split(" • ").map { it.trim() }
                    val artist = parts.firstOrNull() ?: "Unknown Artist"
                    val album = parts.getOrNull(1) ?: ""
                    
                    val lengthText = jsonParser.getRunText(item.optJSONObject("lengthText")) ?: ""
                    val duration = jsonParser.parseDurationText(lengthText)
                    
                    val thumbnail = jsonParser.extractThumbnail(item)
                    val setVideoId = item.optString("setVideoId")

                    Song.fromYouTube(
                        videoId = videoId,
                        title = title,
                        artist = artist,
                        album = album,
                        duration = duration,
                        thumbnailUrl = thumbnail,
                        setVideoId = setVideoId,
                        isMembersOnly = false
                    )?.let { songs.add(it) }
                }
            } else {
                val contents = root.optJSONObject("contents")
                    ?.optJSONObject("singleColumnMusicWatchNextResultsRenderer")
                    ?.optJSONObject("tabbedRenderer")
                    ?.optJSONObject("watchNextTabbedResultsRenderer")
                    ?.optJSONArray("tabs")
                    ?.optJSONObject(0)
                    ?.optJSONObject("tabRenderer")
                    ?.optJSONObject("content")
                    ?.optJSONObject("musicQueueRenderer")
                    ?.optJSONObject("content")
                    ?.optJSONObject("playlistPanelRenderer")
                    ?.optJSONArray("contents")

                if (contents != null) {
                    for (i in 0 until contents.length()) {
                        val item = contents.optJSONObject(i)?.optJSONObject("playlistPanelVideoRenderer")
                        if (item != null) {
                            val videoId = item.optString("videoId")
                            val title = jsonParser.getRunText(item.optJSONObject("title")) ?: "Unknown"
                            val longByline = jsonParser.getRunText(item.optJSONObject("longBylineText")) ?: ""
                            
                            val artist = longByline.split("•").firstOrNull()?.trim() ?: "Unknown Artist"
                            val album = if (longByline.contains("•")) longByline.split("•").lastOrNull()?.trim() ?: "" else ""
                            
                            val lengthText = jsonParser.getRunText(item.optJSONObject("lengthText")) ?: ""
                            val duration = jsonParser.parseDurationText(lengthText)
                            
                            val thumbnail = jsonParser.extractThumbnail(item)
                            val setVideoId = item.optString("setVideoId")

                            Song.fromYouTube(
                                videoId = videoId,
                                title = title,
                                artist = artist,
                                album = album,
                                duration = duration,
                                thumbnailUrl = thumbnail,
                                setVideoId = setVideoId,
                                isMembersOnly = false
                            )?.let { songs.add(it) }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return songs.distinctBy { it.id }
    }
}
