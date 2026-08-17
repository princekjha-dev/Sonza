package com.sonza.app.core.db

import kotlin.Long
import kotlin.String

public data class Playlist_songs(
  public val playlistId: String,
  public val songId: String,
  public val title: String,
  public val artist: String,
  public val album: String?,
  public val thumbnailUrl: String?,
  public val duration: Long,
  public val source: String,
  public val localUri: String?,
  public val releaseDate: String?,
  public val addedAt: Long,
  public val order: Long,
)
