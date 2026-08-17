package com.sonza.app.core.db

import kotlin.Long
import kotlin.String

public data class Library_items(
  public val id: String,
  public val title: String,
  public val subtitle: String?,
  public val thumbnailUrl: String?,
  public val type: String,
  public val timestamp: Long,
)
