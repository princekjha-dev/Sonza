package com.sonza.app.data.migration.provider

import android.net.Uri
import com.sonza.app.data.migration.model.MigrationSource
import com.sonza.app.data.migration.model.SourceTrack
import com.sonza.app.data.repository.YouTubeRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeMusicProvider @Inject constructor(
    private val youTubeRepository: YouTubeRepository
) : MusicServiceProvider {

    override val source: MigrationSource = MigrationSource.YOUTUBE_MUSIC

    override fun canHandleUrl(url: String): Boolean {
        return url.contains("music.youtube.com") || url.contains("youtube.com") || url.contains("youtu.be")
    }

    override suspend fun parseUrl(url: String, onProgress: (Int) -> Unit): ParsedPlaylist? {
        return try {
            val uri = Uri.parse(url)
            var playlistId = uri.getQueryParameter("list") ?: url.substringAfter("list=", "").substringBefore("&")

            if (playlistId.isBlank() && (url.contains("browse/") || url.contains("channel/"))) {
                playlistId = url.substringAfter("browse/").substringAfter("channel/").substringBefore("?").substringBefore("/")
            }

            if (playlistId.isNotBlank()) {
                val playlist = youTubeRepository.getPlaylist(playlistId, autoSave = false)
                onProgress(playlist.songs.size)
                ParsedPlaylist(
                    title = playlist.title.ifBlank { "YouTube Music Playlist" },
                    description = playlist.author.takeIf { it.isNotBlank() }?.let { "By $it • Imported from YouTube" },
                    thumbnailUrl = playlist.thumbnailUrl,
                    source = if (url.contains("music.youtube.com")) MigrationSource.YOUTUBE_MUSIC else MigrationSource.YOUTUBE,
                    tracks = playlist.songs.map {
                        SourceTrack(
                            title = it.title,
                            artist = it.artist,
                            album = it.album,
                            durationMs = it.duration,
                            sourceId = it.id
                        )
                    }
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun parseFile(uri: Uri): ParsedPlaylist? = null
}
