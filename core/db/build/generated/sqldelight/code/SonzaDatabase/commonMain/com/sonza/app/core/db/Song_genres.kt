package com.sonza.app.core.db

import kotlin.Long
import kotlin.String

public data class Song_genres(
  public val songId: String,
  public val genreVector: String,
  public val timestamp: Long,
)
