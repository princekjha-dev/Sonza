package com.sonza.app.data.repository

import com.sonza.app.core.model.Album
import com.sonza.app.core.model.Artist
import com.sonza.app.core.model.BrowseCategory
import com.sonza.app.core.model.HomeSection
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SongSource
import com.sonza.app.core.model.VideoQuality
import com.sonza.app.data.SessionManager
import com.sonza.app.data.SessionManager.StoredAccount
import com.sonza.app.data.repository.youtube.account.YouTubeAccountService
import com.sonza.app.data.repository.youtube.browse.YouTubeBrowseService
import com.sonza.app.data.repository.youtube.catalog.YouTubeCatalogService
import com.sonza.app.data.repository.youtube.internal.YouTubeLocale
import com.sonza.app.data.repository.youtube.library.YouTubeLibraryActionService
import com.sonza.app.data.repository.youtube.lyrics.YouTubeLyricsService
import com.sonza.app.data.repository.youtube.playlist.AddToPlaylistResult
import com.sonza.app.data.repository.youtube.playlist.YouTubePlaylistService
import com.sonza.app.data.repository.youtube.search.YouTubeSearchService
import com.sonza.app.data.repository.youtube.streaming.YouTubeStreamingService
import com.sonza.app.di.ApplicationScope
import com.sonza.app.newpipe.NewPipeDownloaderImpl
import com.sonza.app.providers.lyrics.Lyrics
import com.sonza.app.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.Localization
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Entry point for everything the app reads from YouTube Music.
 *
 * The work itself lives in focused services under `data/repository/youtube/` — search,
 * streaming, playlists, browse, catalog, library actions, lyrics and accounts. This class
 * is the seam they're reached through: it owns the one-time NewPipe bootstrap, applies the
 * "don't bother when offline" guard, and forwards each call. Keeping the facade means the
 * ~40 call sites across the app didn't have to learn the new layout.
 */
@Singleton
class YouTubeRepository @Inject constructor(
    private val sessionManager: SessionManager,
    private val streamingService: YouTubeStreamingService,
    private val searchService: YouTubeSearchService,
    private val accountService: YouTubeAccountService,
    private val playlistService: YouTubePlaylistService,
    private val browseService: YouTubeBrowseService,
    private val catalogService: YouTubeCatalogService,
    private val libraryActionService: YouTubeLibraryActionService,
    private val lyricsService: YouTubeLyricsService,
    private val networkMonitor: NetworkMonitor,
    @ApplicationScope private val externalScope: CoroutineScope
) {
    companion object {
        private var isInitialized = false

        // One source of truth: the search service owns the filter tokens, and these aliases
        // keep the long-standing `YouTubeRepository.FILTER_*` call sites working.
        const val FILTER_SONGS = YouTubeSearchService.FILTER_SONGS
        const val FILTER_VIDEOS = YouTubeSearchService.FILTER_VIDEOS
        const val FILTER_ALBUMS = YouTubeSearchService.FILTER_ALBUMS
        const val FILTER_PLAYLISTS = YouTubeSearchService.FILTER_PLAYLISTS
        const val FILTER_ARTISTS = YouTubeSearchService.FILTER_ARTISTS

        /** Map language names to YouTube Music ISO codes (hl). */
        fun getLanguageCode(languageName: String): String = YouTubeLocale.languageCode(languageName)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    init {
        externalScope.launch { initializeNewPipe() }
    }

    private suspend fun initializeNewPipe() = withContext(Dispatchers.IO) {
        if (isInitialized) return@withContext
        try {
            NewPipe.init(
                NewPipeDownloaderImpl(okHttpClient) { sessionManager.getCookies() ?: "" },
                Localization.DEFAULT
            )
        } catch (e: Exception) {
            // A failed init must not be retried on every call — NewPipe-backed paths will
            // fail and fall through to the InnerTube ones.
        }
        isInitialized = true
    }

    fun isOnline(): Boolean = networkMonitor.isCurrentlyConnected()

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    // ============================================================================================
    // Account
    // ============================================================================================

    suspend fun fetchAccountInfo(): StoredAccount? = accountService.fetchAccountInfo()

    suspend fun getAvailableAccounts(): List<StoredAccount> = accountService.getAvailableAccounts()

    suspend fun switchAccount(account: StoredAccount) = accountService.switchAccount(account)

    // ============================================================================================
    // Search & streams
    // ============================================================================================

    suspend fun search(query: String, filter: String = FILTER_SONGS): List<Song> =
        if (isOnline()) searchService.search(query, filter) else emptyList()

    suspend fun searchArtists(query: String): List<Artist> =
        if (isOnline()) searchService.searchArtists(query) else emptyList()

    suspend fun searchPlaylists(query: String): List<Playlist> =
        if (isOnline()) searchService.searchPlaylists(query) else emptyList()

    suspend fun searchAlbums(query: String): List<Album> =
        if (isOnline()) searchService.searchAlbums(query) else emptyList()

    suspend fun getSearchSuggestions(query: String): List<String> =
        if (isOnline()) searchService.getSearchSuggestions(query) else emptyList()

    suspend fun getStreamUrl(videoId: String, forceLow: Boolean = false): String? =
        if (isOnline()) streamingService.getStreamUrl(videoId, forceLow) else null

    suspend fun getVideoStreamUrl(videoId: String, quality: VideoQuality? = null, forceLow: Boolean = false): String? =
        streamingService.getVideoStreamUrl(videoId, quality, forceLow)

    suspend fun getVideoStreamResult(videoId: String, quality: VideoQuality? = null, forceLow: Boolean = false) =
        streamingService.getVideoStreamResult(videoId, quality, forceLow)

    suspend fun getStreamUrlForDownload(videoId: String): Pair<String, String>? =
        streamingService.getStreamUrlForDownload(videoId)

    /** @param maxResolution caps the muxed (video+audio) quality, e.g. 360 / 720 / 1080. */
    suspend fun getMuxedVideoStreamUrlForDownload(videoId: String, maxResolution: Int = 720): String? =
        streamingService.getMuxedVideoStreamUrlForDownload(videoId, maxResolution)

    suspend fun getSongDetails(videoId: String): Song? = streamingService.getSongDetails(videoId)

    /**
     * Related tracks for autoplay.
     *
     * YouTube Music's "next" API is the good source — real music tracks. The vanilla
     * YouTube watch-page sidebar is only a fallback, and even then only its music-looking
     * uploaders, because it otherwise leaks lyric edits, fan covers and vlogs into the queue.
     */
    suspend fun getRelatedSongs(videoId: String): List<Song> {
        val internalResults = try { searchService.getRelatedSongs(videoId) } catch (e: Exception) { emptyList() }

        val candidates = internalResults.ifEmpty {
            try {
                streamingService.getRelatedItems(videoId).filter { song ->
                    val artist = song.artist.lowercase()
                    artist.contains(" - topic") ||
                        artist.contains("vevo") ||
                        artist.contains("records") ||
                        artist.contains(" music")
                }
            } catch (e: Exception) {
                emptyList()
            }
        }

        // Dedupe on id *and* on a title/artist fingerprint — the same track routinely comes
        // back under several video ids.
        val seenIds = mutableSetOf<String>()
        val seenFingerprints = mutableSetOf<String>()
        val fingerprintRegex = Regex("[^a-z0-9]")

        return candidates.filter { song ->
            val title = song.title.lowercase().replace(fingerprintRegex, "")
            val artist = song.artist.lowercase().replace(fingerprintRegex, "")
            val isNewId = seenIds.add(song.id)
            val isNewFingerprint = seenFingerprints.add("$title|$artist")
            isNewId && isNewFingerprint
        }
    }

    /**
     * Finds the official music video for a song, for switching into video mode.
     * For YouTube songs, song.id is already the primary YouTube video ID.
     * For non-YouTube songs, strictly validates search candidates so an unrelated
     * video is never played.
     */
    suspend fun getBestVideoId(song: Song): String = withContext(Dispatchers.IO) {
        if (!isOnline()) return@withContext song.id

        // For YouTube-sourced songs, song.id is already the exact YouTube video ID.
        if (song.source == SongSource.YOUTUBE || song.source == SongSource.YOUTUBE_MUSIC) {
            return@withContext song.id
        }

        try {
            val noise = setOf(
                "official", "video", "audio", "lyrics", "lyric", "full", "song", "songs",
                "hd", "4k", "mv", "feat", "ft", "with", "the", "remastered", "version",
                "original", "soundtrack", "ost", "from", "movie", "visualizer", "music"
            )
            fun normalize(s: String): Set<String> =
                s.lowercase()
                    .replace(Regex("\\(.*?\\)|\\[.*?]"), " ")
                    .replace(Regex("[^a-z0-9\\s]"), " ")
                    .split(Regex("\\s+"))
                    .filter { it.isNotBlank() && it.length > 1 && it !in noise }
                    .toSet()

            val targetTitle = normalize(song.title)
            val targetArtist = normalize(song.artist)
            if (targetTitle.isEmpty()) return@withContext song.id

            val candidates = search("${song.title} ${song.artist} Official Video", FILTER_VIDEOS)
            for (c in candidates) {
                val cTitle = normalize(c.title)
                if (cTitle.isEmpty()) continue

                val inter = targetTitle.intersect(cTitle).size.toDouble()
                val titleRecall = inter / targetTitle.size
                val titlePrecision = inter / cTitle.size
                if (titleRecall < 0.80 || titlePrecision < 0.60) continue

                // Check artist overlap if both known
                val cArtist = normalize(c.artist)
                val artistKnown = targetArtist.isNotEmpty() && cArtist.isNotEmpty()
                val artistOverlap = if (!artistKnown) 0.0
                    else targetArtist.intersect(cArtist).size.toDouble() / targetArtist.size

                // Artist gate: never match an unrelated artist's video
                if (artistKnown && artistOverlap == 0.0) continue

                // Found a confident match
                return@withContext c.id
            }

            // No confident video match found — fallback to song.id
            song.id
        } catch (e: Exception) {
            e.printStackTrace()
            song.id
        }
    }

    // ============================================================================================
    // Browse
    // ============================================================================================

    suspend fun getRecommendations(): List<Song> = browseService.getRecommendations()

    suspend fun getHomeSections(): List<HomeSection> = browseService.getHomeSections()

    suspend fun getHomeSectionsForMood(moodTitle: String): List<HomeSection> =
        browseService.getHomeSectionsForMood(moodTitle)

    suspend fun getBrowseSections(browseId: String): List<HomeSection> =
        browseService.getBrowseSections(browseId)

    suspend fun getPodcastsSections(category: String? = null): List<HomeSection> =
        browseService.getPodcastsSections(category)

    suspend fun getMoodsAndGenres(): List<BrowseCategory> = browseService.getMoodsAndGenres()

    suspend fun getCategoryContent(browseId: String, params: String? = null, title: String? = null): List<Song> =
        browseService.getCategoryContent(browseId, params, title)

    // ============================================================================================
    // Playlists
    // ============================================================================================

    suspend fun getUserPlaylists(autoSave: Boolean = true): List<PlaylistDisplayItem> =
        playlistService.getUserPlaylists(autoSave)

    suspend fun getUserEditablePlaylists(): List<PlaylistDisplayItem> =
        playlistService.getUserEditablePlaylists()

    suspend fun getLikedMusic(fetchAll: Boolean = false): List<Song> = playlistService.getLikedMusic(fetchAll)

    suspend fun syncLikedSongs(fetchAll: Boolean = false): Boolean = playlistService.syncLikedSongs(fetchAll)

    suspend fun removeFromLikedCache(songId: String) = playlistService.removeFromLikedCache(songId)

    suspend fun getCachedPlaylist(playlistId: String): Playlist? = playlistService.getCachedPlaylist(playlistId)

    suspend fun getPlaylist(playlistId: String, autoSave: Boolean = false): Playlist =
        playlistService.getPlaylist(playlistId, autoSave)

    fun getPlaylistFlow(playlistId: String, autoSave: Boolean = false): Flow<Playlist> =
        playlistService.getPlaylistFlow(playlistId, autoSave)

    suspend fun getAutoMixPlaylist(playlistId: String): Playlist = playlistService.getAutoMixPlaylist(playlistId)

    suspend fun createPlaylist(
        title: String,
        description: String = "",
        privacyStatus: String = "PRIVATE"
    ): String? = playlistService.createPlaylist(title, description, privacyStatus)

    suspend fun addSongToPlaylist(playlistId: String, videoId: String): Boolean =
        playlistService.addSongToPlaylist(playlistId, videoId)

    suspend fun addSongsToPlaylist(playlistId: String, videoIds: List<String>): Boolean =
        playlistService.addSongsToPlaylist(playlistId, videoIds)

    /** Adds to a local or YouTube playlist, whichever [playlistId] names, and mirrors the cache. */
    suspend fun addSongsToAnyPlaylist(playlistId: String, songs: List<Song>): AddToPlaylistResult =
        playlistService.addSongsToAnyPlaylist(playlistId, songs)

    suspend fun removeSongFromPlaylist(playlistId: String, setVideoId: String): Boolean =
        playlistService.removeSongFromPlaylist(playlistId, setVideoId)

    suspend fun removeSongsFromPlaylist(playlistId: String, setVideoIds: List<String>): Boolean =
        playlistService.removeSongsFromPlaylist(playlistId, setVideoIds)

    /** Removes from an auto-generated playlist (My Top 50, Discover Mix, …) via feedback tokens. */
    suspend fun removeSongsFromAutoPlaylist(songs: List<Song>): Boolean =
        playlistService.removeSongsFromAutoPlaylist(songs)

    fun isAutoGeneratedPlaylist(playlistId: String): Boolean =
        playlistService.isAutoGeneratedPlaylist(playlistId)

    fun isLocalPlaylist(playlistId: String): Boolean = playlistService.isLocalPlaylist(playlistId)

    suspend fun moveSongInPlaylist(
        playlistId: String,
        setVideoId: String,
        predecessorSetVideoId: String?
    ): Boolean = playlistService.moveSongInPlaylist(playlistId, setVideoId, predecessorSetVideoId)

    suspend fun renamePlaylist(playlistId: String, newTitle: String, newDescription: String? = null): Boolean =
        playlistService.renamePlaylist(playlistId, newTitle, newDescription)

    suspend fun deletePlaylist(playlistId: String): Boolean = playlistService.deletePlaylist(playlistId)

    // ============================================================================================
    // Artists & albums
    // ============================================================================================

    suspend fun getArtist(browseId: String): Artist? = catalogService.getArtist(browseId)

    suspend fun getAlbum(browseId: String): Album? = catalogService.getAlbum(browseId)

    suspend fun getLibraryArtists(): List<Artist> = catalogService.getLibraryArtists()

    suspend fun getLibraryAlbums(): List<Album> = catalogService.getLibraryAlbums()

    suspend fun getArtistRadioId(artistId: String): String? = catalogService.getArtistRadioId(artistId)

    suspend fun getArtistTopSongs(artistName: String, artistId: String): List<Song> =
        catalogService.getArtistTopSongs(artistName, artistId)

    // ============================================================================================
    // Library actions
    // ============================================================================================

    /** @param rating one of LIKE, DISLIKE, INDIFFERENT. */
    suspend fun rateSong(videoId: String, rating: String): Boolean =
        libraryActionService.rateSong(videoId, rating)

    /** @param rating one of LIKE, DISLIKE, INDIFFERENT. */
    suspend fun ratePlaylist(playlistId: String, rating: String): Boolean =
        libraryActionService.ratePlaylist(playlistId, rating)

    suspend fun subscribe(channelId: String, isSubscribe: Boolean): Boolean =
        libraryActionService.subscribe(channelId, isSubscribe)

    suspend fun fetchAndSyncHistory() = libraryActionService.fetchAndSyncHistory()

    suspend fun fetchYouTubeMusicHistory(): List<Song> = libraryActionService.fetchYouTubeMusicHistory()

    suspend fun fetchYouTubeHistory(musicOnly: Boolean = true): List<Song> =
        libraryActionService.fetchYouTubeHistory(musicOnly)

    suspend fun markAsWatched(videoId: String, durationSeconds: Int = 30) =
        libraryActionService.markAsWatched(videoId, durationSeconds)

    // ============================================================================================
    // Lyrics
    // ============================================================================================

    suspend fun getLyrics(videoId: String): Lyrics? = lyricsService.getLyrics(videoId)
}
