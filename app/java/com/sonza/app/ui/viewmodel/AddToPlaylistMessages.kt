package com.sonza.app.ui.viewmodel

import com.sonza.app.core.model.Song
import com.sonza.app.data.repository.youtube.playlist.AddToPlaylistResult

/**
 * Turns an [AddToPlaylistResult] into the line shown to the user.
 *
 * Both the player's add-to-playlist sheet and the playlist screen render results through
 * here, so the same outcome can't end up worded two different ways.
 */
fun AddToPlaylistResult.describe(songs: List<Song>): String = when (this) {
    is AddToPlaylistResult.Added ->
        if (count == 1) "Added ${songs.firstOrNull()?.title ?: "song"} to playlist"
        else "Added $count songs to playlist"
    is AddToPlaylistResult.PartiallyAdded -> "Added $count of $total songs to playlist"
    is AddToPlaylistResult.AlreadyPresent -> "$songTitle is already in this playlist"
    is AddToPlaylistResult.Failed -> reason
}

/** True when at least one song made it in. */
val AddToPlaylistResult.isSuccess: Boolean
    get() = this is AddToPlaylistResult.Added || this is AddToPlaylistResult.PartiallyAdded
