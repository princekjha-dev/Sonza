package com.sonza.app.core.db

import kotlin.Long
import kotlin.String

public data class SelectTopArtists(
  public val artist: String,
  public val totalPlays: Long?,
)
