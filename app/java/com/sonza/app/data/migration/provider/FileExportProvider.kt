package com.sonza.app.data.migration.provider

import android.content.Context
import android.net.Uri
import com.sonza.app.data.migration.model.MigrationSource
import com.sonza.app.data.migration.model.SourceTrack
import com.sonza.app.util.PlaylistImportHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileExportProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val playlistImportHelper: PlaylistImportHelper
) : MusicServiceProvider {

    override val source: MigrationSource = MigrationSource.FILE_EXPORT

    override fun canHandleUrl(url: String): Boolean = false

    override suspend fun parseUrl(url: String, onProgress: (Int) -> Unit): ParsedPlaylist? = null

    override suspend fun parseFile(uri: Uri): ParsedPlaylist? = withContext(Dispatchers.IO) {
        val fileName = uri.lastPathSegment?.lowercase() ?: ""
        when {
            fileName.endsWith(".m3u") || fileName.endsWith(".m3u8") -> {
                val (name, tracks) = playlistImportHelper.parseM3U(uri)
                if (tracks.isEmpty()) null else ParsedPlaylist(
                    title = name,
                    description = "Imported from M3U",
                    source = MigrationSource.FILE_EXPORT,
                    tracks = tracks.map { SourceTrack(title = it.title, artist = it.artist, durationMs = it.durationMs) }
                )
            }
            fileName.endsWith(".json") -> {
                val (name, tracks) = playlistImportHelper.parseJson(uri)
                if (tracks.isEmpty()) null else ParsedPlaylist(
                    title = name,
                    description = "Imported from JSON",
                    source = MigrationSource.FILE_EXPORT,
                    tracks = tracks.map { SourceTrack(title = it.title, artist = it.artist, durationMs = it.durationMs, sourceId = it.sourceId) }
                )
            }
            fileName.endsWith(".sonza") || fileName.endsWith(".suv") -> {
                val (name, tracks) = playlistImportHelper.parseSonza(uri)
                if (tracks.isEmpty()) null else ParsedPlaylist(
                    title = name,
                    description = "Imported from Sonza format",
                    source = MigrationSource.FILE_EXPORT,
                    tracks = tracks.map { SourceTrack(title = it.title, artist = it.artist, durationMs = it.durationMs, sourceId = it.sourceId) }
                )
            }
            fileName.endsWith(".csv") || fileName.endsWith(".txt") -> {
                parseCsvOrTxt(uri)
            }
            else -> {
                // Fallback attempt: try JSON then M3U
                val jsonRes = playlistImportHelper.parseJson(uri)
                if (jsonRes.second.isNotEmpty()) {
                    ParsedPlaylist(
                        title = jsonRes.first,
                        description = "Imported file",
                        source = MigrationSource.FILE_EXPORT,
                        tracks = jsonRes.second.map { SourceTrack(title = it.title, artist = it.artist, durationMs = it.durationMs) }
                    )
                } else {
                    val m3uRes = playlistImportHelper.parseM3U(uri)
                    if (m3uRes.second.isNotEmpty()) {
                        ParsedPlaylist(
                            title = m3uRes.first,
                            description = "Imported file",
                            source = MigrationSource.FILE_EXPORT,
                            tracks = m3uRes.second.map { SourceTrack(title = it.title, artist = it.artist, durationMs = it.durationMs) }
                        )
                    } else parseCsvOrTxt(uri)
                }
            }
        }
    }

    private fun parseCsvOrTxt(uri: Uri): ParsedPlaylist? {
        val tracks = mutableListOf<SourceTrack>()
        val defaultName = uri.lastPathSegment?.substringBeforeLast(".") ?: "Imported Playlist"

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).useLines { lines ->
                    lines.forEach { rawLine ->
                        val line = rawLine.trim()
                        if (line.isNotBlank() && !line.startsWith("#")) {
                            if (line.contains(",") || line.contains(";") || line.contains("\t")) {
                                val delimiter = if (line.contains("\t")) "\t" else if (line.contains(";")) ";" else ","
                                val parts = line.split(delimiter)
                                if (parts.size >= 2) {
                                    val title = parts[0].trim('"', ' ', '\'')
                                    val artist = parts[1].trim('"', ' ', '\'')
                                    if (title.isNotBlank() && !title.equals("title", true) && !title.equals("track", true)) {
                                        tracks.add(SourceTrack(title = title, artist = artist))
                                    }
                                }
                            } else if (line.contains(" - ")) {
                                val artist = line.substringBefore(" - ").trim()
                                val title = line.substringAfter(" - ").trim()
                                if (title.isNotBlank()) {
                                    tracks.add(SourceTrack(title = title, artist = artist))
                                }
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return if (tracks.isNotEmpty()) {
            ParsedPlaylist(
                title = defaultName,
                description = "Imported from CSV/TXT",
                source = MigrationSource.FILE_EXPORT,
                tracks = tracks
            )
        } else null
    }
}
