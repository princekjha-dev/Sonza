package com.sonza.music.data.repository

import com.sonza.music.core.database.PlaylistDao
import com.sonza.music.core.database.PlaylistEntity
import com.sonza.music.core.database.PlaylistTrackCrossRef
import com.sonza.music.core.database.TrackDao
import com.sonza.music.core.database.TrackEntity
import com.sonza.music.core.model.Album
import com.sonza.music.core.model.Artist
import com.sonza.music.core.model.Playlist
import com.sonza.music.core.model.PlaylistType
import com.sonza.music.core.model.Track
import com.sonza.music.data.local.LocalMusicScanner
import com.sonza.music.data.source.DemoAudiophileSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

interface MusicRepository {
    fun getAllTracks(): Flow<List<Track>>
    fun getFavoriteTracks(): Flow<List<Track>>
    fun getDownloadedTracks(): Flow<List<Track>>
    fun getLocalTracks(): Flow<List<Track>>
    fun getAlbums(): Flow<List<Album>>
    fun getArtists(): Flow<List<Artist>>
    fun search(query: String): Flow<List<Track>>
    suspend fun getTrackById(id: String): Track?
    suspend fun toggleFavorite(trackId: String, isFavorite: Boolean)
    suspend fun syncLocalMusic()
    suspend fun populateDemoCatalog()
}

class MusicRepositoryImpl(
    private val trackDao: TrackDao,
    private val localScanner: LocalMusicScanner
) : MusicRepository {

    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracksFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getFavoriteTracks(): Flow<List<Track>> {
        return trackDao.getFavoriteTracksFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getDownloadedTracks(): Flow<List<Track>> {
        return trackDao.getDownloadedTracksFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getLocalTracks(): Flow<List<Track>> {
        return trackDao.getLocalTracksFlow().map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override fun getAlbums(): Flow<List<Album>> {
        return trackDao.getAllTracksFlow().map { list ->
            list.groupBy { it.albumId.ifEmpty { it.album } }.map { (key, group) ->
                val first = group.first()
                Album(
                    id = key,
                    title = first.album,
                    artist = first.artist,
                    artistId = first.artistId,
                    artworkUri = first.artworkUri,
                    year = first.year,
                    genre = first.genre,
                    trackCount = group.size,
                    durationMs = group.sumOf { it.durationMs },
                    quality = first.toDomain().quality,
                    isHiRes = group.any { it.isLossless }
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getArtists(): Flow<List<Artist>> {
        return trackDao.getAllTracksFlow().map { list ->
            list.groupBy { it.artistId.ifEmpty { it.artist } }.map { (key, group) ->
                val first = group.first()
                Artist(
                    id = key,
                    name = first.artist,
                    artworkUri = first.artworkUri,
                    trackCount = group.size,
                    albumCount = group.map { it.album }.distinct().size
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun search(query: String): Flow<List<Track>> {
        return trackDao.searchTracks(query).map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun getTrackById(id: String): Track? = withContext(Dispatchers.IO) {
        trackDao.getTrackById(id)?.toDomain()
    }

    override suspend fun toggleFavorite(trackId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        trackDao.updateFavorite(trackId, isFavorite)
    }

    override suspend fun syncLocalMusic() = withContext(Dispatchers.IO) {
        val scanned = localScanner.scanLocalTracks()
        if (scanned.isNotEmpty()) {
            trackDao.insertTracks(scanned)
        }
    }

    override suspend fun populateDemoCatalog() = withContext(Dispatchers.IO) {
        val entities = DemoAudiophileSource.SAMPLE_TRACKS.map { TrackEntity.fromDomain(it) }
        trackDao.insertTracks(entities)
    }
}

interface PlaylistRepository {
    fun getAllPlaylists(): Flow<List<Playlist>>
    fun getTracksForPlaylist(playlistId: String): Flow<List<Track>>
    suspend fun createPlaylist(title: String, description: String = "", type: PlaylistType = PlaylistType.USER_CREATED): String
    suspend fun addTrackToPlaylist(playlistId: String, trackId: String)
    suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String)
    suspend fun deletePlaylist(playlistId: String)
}

class PlaylistRepositoryImpl(
    private val playlistDao: PlaylistDao
) : PlaylistRepository {

    override fun getAllPlaylists(): Flow<List<Playlist>> {
        return playlistDao.getAllPlaylistsFlow().map { list ->
            list.map { entity ->
                Playlist(
                    id = entity.id,
                    title = entity.title,
                    description = entity.description,
                    artworkUri = entity.artworkUri,
                    type = try { PlaylistType.valueOf(entity.type) } catch (e: Exception) { PlaylistType.USER_CREATED },
                    trackCount = entity.trackCount,
                    durationMs = entity.durationMs,
                    isCollaborative = entity.isCollaborative,
                    isDownloaded = entity.isDownloaded,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }.flowOn(Dispatchers.IO)
    }

    override fun getTracksForPlaylist(playlistId: String): Flow<List<Track>> {
        return playlistDao.getTracksForPlaylistFlow(playlistId).map { list ->
            list.map { it.toDomain() }
        }.flowOn(Dispatchers.IO)
    }

    override suspend fun createPlaylist(title: String, description: String, type: PlaylistType): String = withContext(Dispatchers.IO) {
        val id = "pl_${System.currentTimeMillis()}"
        val entity = PlaylistEntity(
            id = id,
            title = title,
            description = description,
            artworkUri = null,
            type = type.name,
            trackCount = 0,
            durationMs = 0,
            isCollaborative = false,
            isDownloaded = false,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        playlistDao.insertPlaylist(entity)
        id
    }

    override suspend fun addTrackToPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.insertPlaylistTrackCrossRef(
            PlaylistTrackCrossRef(playlistId = playlistId, trackId = trackId, sortOrder = 999)
        )
    }

    override suspend fun removeTrackFromPlaylist(playlistId: String, trackId: String) = withContext(Dispatchers.IO) {
        playlistDao.removeTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: String) = withContext(Dispatchers.IO) {
        playlistDao.deletePlaylist(playlistId)
    }
}
