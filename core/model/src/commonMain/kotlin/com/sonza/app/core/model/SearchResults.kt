package com.sonza.app.core.model

/**
 * Encapsulates search results canonically grouped by entity type.
 */
data class SearchResults(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)
