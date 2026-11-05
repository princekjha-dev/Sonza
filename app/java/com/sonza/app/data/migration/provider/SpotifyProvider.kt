package com.sonza.app.data.migration.provider

import android.net.Uri
import com.sonza.app.data.migration.model.MigrationSource
import com.sonza.app.data.migration.model.SourceTrack
import com.sonza.app.util.SpotifyImportHelper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyProvider @Inject constructor(
    private val spotifyImportHelper: SpotifyImportHelper
) : MusicServiceProvider {

    override val source: MigrationSource = MigrationSource.SPOTIFY

    override fun canHandleUrl(url: String): Boolean {
        return url.contains("spotify.com") || url.contains("spotify.link")
    }

    override suspend fun parseUrl(url: String, onProgress: (Int) -> Unit): ParsedPlaylist? {
        return try {
            val (name, tracks) = spotifyImportHelper.getPlaylistSongs(url, onProgress)
            if (tracks.isEmpty()) return null
            ParsedPlaylist(
                title = name.ifBlank { "Spotify Playlist" },
                description = "Imported from Spotify",
                source = MigrationSource.SPOTIFY,
                tracks = tracks.map {
                    SourceTrack(
                        title = it.title,
                        artist = it.artist,
                        durationMs = it.durationMs
                    )
                }
            )
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun parseFile(uri: Uri): ParsedPlaylist? = null
}
