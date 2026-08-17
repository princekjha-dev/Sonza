package com.sonza.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.sonza.app.core.model.Playlist
import java.io.File

object PlaylistExportHelper {
    
    fun exportPlaylistToM3U(context: Context, playlist: Playlist) {
        val songs = playlist.songs
        if (songs.isEmpty()) return

        try {
            val m3uContent = StringBuilder("#EXTM3U\n")
            for (song in songs) {
                m3uContent.append("#EXTINF:${song.duration / 1000},${song.artist} - ${song.title}\n")
                // For YouTube songs, use the URL. For local songs, use the URI.
                val url = song.localUri ?: "https://www.youtube.com/watch?v=${song.id}"
                m3uContent.append("$url\n")
            }

            val safeTitle = playlist.title.replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "$safeTitle.m3u"
            
            val playlistsDir = File(context.cacheDir, "playlists")
            if (!playlistsDir.exists()) playlistsDir.mkdirs()
            
            val tempFile = File(playlistsDir, fileName)
            tempFile.writeText(m3uContent.toString())
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "audio/x-mpegurl"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(intent, "Export Playlist")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportPlaylistToSonza(context: Context, playlist: Playlist) {
        val songs = playlist.songs
        if (songs.isEmpty()) return

        try {
            val sonzaContent = StringBuilder("#SONZAPLAYLIST\n")
            
            // Add Metadata
            sonzaContent.append("[METADATA]\n")
            sonzaContent.append("title: ${playlist.title}\n")
            sonzaContent.append("author: ${playlist.author}\n")
            playlist.description?.let { sonzaContent.append("description: $it\n") }
            sonzaContent.append("[/METADATA]\n\n")

            for (song in songs) {
                sonzaContent.append("[SONG]\n")
                sonzaContent.append("id: ${song.id}\n")
                sonzaContent.append("title: ${song.title}\n")
                sonzaContent.append("artist: ${song.artist}\n")
                sonzaContent.append("album: ${song.album}\n")
                sonzaContent.append("duration: ${song.duration}\n")
                sonzaContent.append("source: ${song.source}\n")
                sonzaContent.append("[/SONG]\n")
            }

            sonzaContent.append("[SEQUENCE]\n")
            sonzaContent.append(songs.joinToString(",") { it.id })
            sonzaContent.append("\n[/SEQUENCE]")

            val safeTitle = playlist.title.replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "$safeTitle.sonza"
            
            val playlistsDir = File(context.cacheDir, "playlists")
            if (!playlistsDir.exists()) playlistsDir.mkdirs()
            
            val tempFile = File(playlistsDir, fileName)
            tempFile.writeText(sonzaContent.toString())
            
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                tempFile
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooser = Intent.createChooser(intent, "Export Playlist (.sonza)")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun exportPlaylistToSUV(context: Context, playlist: Playlist) = exportPlaylistToSonza(context, playlist)
}
