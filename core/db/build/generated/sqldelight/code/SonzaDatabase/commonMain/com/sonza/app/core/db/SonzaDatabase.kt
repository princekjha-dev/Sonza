package com.sonza.app.core.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.sonza.app.core.db.db.newInstance
import com.sonza.app.core.db.db.schema
import kotlin.Unit

public interface SonzaDatabase : Transacter {
  public val dislikedItemsQueries: DislikedItemsQueries

  public val libraryItemsQueries: LibraryItemsQueries

  public val listeningHistoryQueries: ListeningHistoryQueries

  public val songGenresQueries: SongGenresQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = SonzaDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): SonzaDatabase =
        SonzaDatabase::class.newInstance(driver)
  }
}
