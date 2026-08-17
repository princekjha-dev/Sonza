package com.sonza.app.core.db

import kotlin.Long
import kotlin.String

public data class Disliked_songs(
  public val songId: String,
  public val title: String,
  public val artist: String,
  public val timestamp: Long,
)
