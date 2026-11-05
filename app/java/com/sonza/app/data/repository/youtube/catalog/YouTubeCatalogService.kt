package com.sonza.app.data.repository.youtube.catalog

import com.sonza.app.core.domain.repository.LibraryRepository
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.Artist
import com.sonza.app.core.model.Song
import com.sonza.app.data.SessionManager
import com.sonza.app.data.repository.youtube.internal.YouTubeApiClient
import com.sonza.app.data.repository.youtube.internal.YouTubeContentParser
import com.sonza.app.data.repository.youtube.search.YouTubeSearchService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/** Artist and album pages, plus the artist/album grids in the user's library. */
@Singleton
class YouTubeCatalogService @Inject constructor(
    private val sessionManager: SessionManager,
    private val apiClient: YouTubeApiClient,
    private val parser: YouTubeContentParser,
    private val searchService: YouTubeSearchService,
    private val libraryRepository: LibraryRepository
) {

    suspend fun getArtist(browseId: String): Artist? = withContext(Dispatchers.IO) {
        try {
            val artist = parser.parseArtist(apiClient.fetchInternalApi(browseId), browseId)
            // A locally-followed artist stays followed even when YouTube says otherwise
            // (following works offline and without an account).
            val isLocallySubscribed = libraryRepository.isArtistSaved(browseId).first()
            artist.copy(isSubscribed = artist.isSubscribed || isLocallySubscribed)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAlbum(browseId: String): Album? = withContext(Dispatchers.IO) {
        // Album ids come in two shapes: OLAK… from search (needs the VL prefix) and MPRE…
        // from browse (already addressable).
        val effectiveBrowseId = if (browseId.startsWith("OLAK")) "VL$browseId" else browseId
        try {
            parser.parseAlbum(apiClient.fetchInternalApi(effectiveBrowseId), browseId)
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                parser.parseAlbum(apiClient.fetchInternalApi(browseId), browseId)
            } catch (e2: Exception) {
                e2.printStackTrace()
                null
            }
        }
    }

    suspend fun getLibraryArtists(): List<Artist> = withContext(Dispatchers.IO) {
        if (!sessionManager.isLoggedIn()) return@withContext emptyList()
        try {
            parser.parseLibraryArtists(apiClient.fetchInternalApi("FEmusic_library_corpus_track_artists"))
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getLibraryAlbums(): List<Album> = withContext(Dispatchers.IO) {
        if (!sessionManager.isLoggedIn()) return@withContext emptyList()
        try {
            parser.parseLibraryAlbums(apiClient.fetchInternalApi("FEmusic_library_corpus_track_albums"))
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getArtistRadioId(artistId: String): String? = withContext(Dispatchers.IO) {
        try {
            parser.parseArtistRadioId(apiClient.fetchInternalApi(artistId))
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /** The artist's own shelves widened with a targeted search, since neither alone is deep. */
    suspend fun getArtistTopSongs(artistName: String, artistId: String): List<Song> = withContext(Dispatchers.IO) {
        val allSongs = mutableListOf<Song>()
        try {
            getArtist(artistId)?.let { artist ->
                allSongs.addAll(artist.songs)
                allSongs.addAll(artist.videos)
            }

            allSongs.addAll(
                searchService.search(artistName, YouTubeSearchService.FILTER_SONGS)
                    .filter { it.artist.contains(artistName, ignoreCase = true) }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        allSongs.distinctBy { it.id }
    }
}
