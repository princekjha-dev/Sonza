package com.sonza.app.data.migration.provider

import android.net.Uri
import com.sonza.app.data.migration.model.MigrationSource
import com.sonza.app.data.migration.model.SourceTrack

data class ParsedPlaylist(
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val source: MigrationSource,
    val tracks: List<SourceTrack>
)

interface MusicServiceProvider {
    val source: MigrationSource
    fun canHandleUrl(url: String): Boolean
    suspend fun parseUrl(url: String, onProgress: (Int) -> Unit = {}): ParsedPlaylist?
    suspend fun parseFile(uri: Uri): ParsedPlaylist?
}
