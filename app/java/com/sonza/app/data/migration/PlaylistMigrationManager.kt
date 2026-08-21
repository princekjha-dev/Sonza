package com.sonza.app.data.migration

import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sonza.app.core.domain.repository.LibraryRepository
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.Song
import com.sonza.app.data.SessionManager
import com.sonza.app.data.migration.engine.TrackMatchingEngine
import com.sonza.app.data.migration.model.DuplicateStrategy
import com.sonza.app.data.migration.model.MatchConfidence
import com.sonza.app.data.migration.model.MigrationRecord
import com.sonza.app.data.migration.model.MigrationSource
import com.sonza.app.data.migration.model.SourceTrack
import com.sonza.app.data.migration.model.TrackMatchResult
import com.sonza.app.data.migration.provider.FileExportProvider
import com.sonza.app.data.migration.provider.MusicServiceProvider
import com.sonza.app.data.migration.provider.ParsedPlaylist
import com.sonza.app.data.migration.provider.SpotifyProvider
import com.sonza.app.data.migration.provider.YouTubeMusicProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlaylistMigrationManager @Inject constructor(
    private val spotifyProvider: SpotifyProvider,
    private val youTubeMusicProvider: YouTubeMusicProvider,
    private val fileExportProvider: FileExportProvider,
    private val trackMatchingEngine: TrackMatchingEngine,
    private val libraryRepository: LibraryRepository,
    private val sessionManager: SessionManager,
    private val gson: Gson
) {

    val providers: List<MusicServiceProvider> by lazy {
        listOf(spotifyProvider, youTubeMusicProvider, fileExportProvider)
    }

    /**
     * Auto-detect and parse playlist from a URL.
     */
    suspend fun parseFromUrl(
        url: String,
        onProgress: (Int) -> Unit = {}
    ): ParsedPlaylist? = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        val provider = providers.firstOrNull { it.canHandleUrl(trimmed) }
        provider?.parseUrl(trimmed, onProgress)
    }

    /**
     * Parse playlist from a file Uri.
     */
    suspend fun parseFromFile(uri: Uri): ParsedPlaylist? = withContext(Dispatchers.IO) {
        fileExportProvider.parseFile(uri)
    }

    /**
     * Match tracks from a parsed playlist.
     */
    suspend fun matchTracks(
        tracks: List<SourceTrack>,
        onProgress: (completed: Int, total: Int, lastResult: TrackMatchResult) -> Unit = { _, _, _ -> }
    ): List<TrackMatchResult> {
        return trackMatchingEngine.matchTracks(tracks, onProgress)
    }

    /**
     * Check if a playlist with the given name already exists in user's library.
     */
    suspend fun findExistingPlaylist(name: String): com.sonza.app.core.model.PlaylistDisplayItem? = withContext(Dispatchers.IO) {
        try {
            val playlists = libraryRepository.getSavedPlaylists().first()
            playlists.firstOrNull { it.name.trim().equals(name.trim(), ignoreCase = true) }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Execute the migration and persist to Library.
     */
    suspend fun executeMigration(
        playlistTitle: String,
        description: String?,
        thumbnailUrl: String?,
        source: MigrationSource,
        matchedResults: List<TrackMatchResult>,
        duplicateStrategy: DuplicateStrategy = DuplicateStrategy.CREATE_NEW_COPY
    ): Pair<String, MigrationRecord> = withContext(Dispatchers.IO) {
        val validSongs = matchedResults
            .filter { !it.isSkipped && it.matchedSong != null }
            .map { it.matchedSong!! }

        val existing = findExistingPlaylist(playlistTitle)
        val targetPlaylistId: String

        if (existing != null && duplicateStrategy == DuplicateStrategy.REPLACE) {
            targetPlaylistId = existing.id
            libraryRepository.replacePlaylistSongs(targetPlaylistId, validSongs)
        } else if (existing != null && duplicateStrategy == DuplicateStrategy.ADD_MISSING) {
            targetPlaylistId = existing.id
            val currentSongs = libraryRepository.getCachedPlaylistSongs(targetPlaylistId)
            val currentIds = currentSongs.map { it.id }.toSet()
            val newSongs = validSongs.filter { it.id !in currentIds }
            libraryRepository.appendPlaylistSongs(targetPlaylistId, newSongs, currentSongs.size)
        } else {
            // Create New Local Playlist
            targetPlaylistId = "local_migrated_" + UUID.randomUUID().toString().take(8)
            val finalTitle = if (existing != null && duplicateStrategy == DuplicateStrategy.CREATE_NEW_COPY) {
                "$playlistTitle (Imported)"
            } else {
                playlistTitle
            }

            val playlist = Playlist(
                id = targetPlaylistId,
                title = finalTitle,
                author = "Migrated from ${source.displayName}",
                thumbnailUrl = thumbnailUrl ?: validSongs.firstOrNull()?.thumbnailUrl,
                songs = validSongs,
                description = description ?: "Imported via Sonza Playlist Migration"
            )
            libraryRepository.savePlaylist(playlist)
            libraryRepository.savePlaylistSongs(targetPlaylistId, validSongs)
        }

        val total = matchedResults.size
        val matched = validSongs.size
        val skipped = matchedResults.count { it.isSkipped }
        val unavailable = total - matched - skipped

        val record = MigrationRecord(
            id = UUID.randomUUID().toString(),
            playlistTitle = playlistTitle,
            sourceName = source.displayName,
            totalTracks = total,
            matchedCount = matched,
            skippedCount = skipped,
            unavailableCount = unavailable.coerceAtLeast(0),
            targetPlaylistId = targetPlaylistId,
            timestamp = System.currentTimeMillis()
        )

        saveMigrationRecord(record)
        targetPlaylistId to record
    }

    /**
     * History persistence in SessionManager preferences.
     */
    suspend fun getMigrationHistory(): List<MigrationRecord> = withContext(Dispatchers.IO) {
        val json = sessionManager.getMigrationHistoryJson() ?: return@withContext emptyList()
        try {
            val type = object : TypeToken<List<MigrationRecord>>() {}.type
            gson.fromJson<List<MigrationRecord>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun saveMigrationRecord(record: MigrationRecord) = withContext(Dispatchers.IO) {
        val current = getMigrationHistory().toMutableList()
        current.removeAll { it.id == record.id }
        current.add(0, record)
        val trimmed = current.take(50)
        sessionManager.setMigrationHistoryJson(gson.toJson(trimmed))
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        sessionManager.setMigrationHistoryJson(null)
    }
}
