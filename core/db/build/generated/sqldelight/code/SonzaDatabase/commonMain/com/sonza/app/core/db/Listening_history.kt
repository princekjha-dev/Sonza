package com.sonza.app.core.db

import kotlin.Double
import kotlin.Long
import kotlin.String

public data class Listening_history(
  public val songId: String,
  public val songTitle: String,
  public val artist: String,
  public val thumbnailUrl: String?,
  public val album: String,
  public val duration: Long,
  public val localUri: String?,
  public val playCount: Long,
  public val totalDurationMs: Long,
  public val lastPlayed: Long,
  public val firstPlayed: Long,
  public val skipCount: Long,
  public val completionRate: Double,
  public val isLiked: Long,
  public val artistId: String?,
  public val source: String,
  public val releaseDate: String?,
)
