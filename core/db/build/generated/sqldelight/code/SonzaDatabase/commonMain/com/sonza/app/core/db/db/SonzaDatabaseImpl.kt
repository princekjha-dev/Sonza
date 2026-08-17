package com.sonza.app.core.db.db

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.sonza.app.core.db.DislikedItemsQueries
import com.sonza.app.core.db.LibraryItemsQueries
import com.sonza.app.core.db.ListeningHistoryQueries
import com.sonza.app.core.db.SongGenresQueries
import com.sonza.app.core.db.SonzaDatabase
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<SonzaDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = SonzaDatabaseImpl.Schema

internal fun KClass<SonzaDatabase>.newInstance(driver: SqlDriver): SonzaDatabase =
    SonzaDatabaseImpl(driver)

private class SonzaDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver),
    SonzaDatabase {
  override val dislikedItemsQueries: DislikedItemsQueries = DislikedItemsQueries(driver)

  override val libraryItemsQueries: LibraryItemsQueries = LibraryItemsQueries(driver)

  override val listeningHistoryQueries: ListeningHistoryQueries = ListeningHistoryQueries(driver)

  override val songGenresQueries: SongGenresQueries = SongGenresQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE disliked_songs (
          |    songId TEXT NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    artist TEXT NOT NULL,
          |    timestamp INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE disliked_artists (
          |    artistName TEXT NOT NULL PRIMARY KEY,
          |    timestamp INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE library_items (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    subtitle TEXT,
          |    thumbnailUrl TEXT,
          |    type TEXT NOT NULL,
          |    timestamp INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE playlist_songs (
          |    playlistId TEXT NOT NULL,
          |    songId TEXT NOT NULL,
          |    title TEXT NOT NULL,
          |    artist TEXT NOT NULL,
          |    album TEXT,
          |    thumbnailUrl TEXT,
          |    duration INTEGER NOT NULL,
          |    source TEXT NOT NULL,
          |    localUri TEXT,
          |    releaseDate TEXT,
          |    addedAt INTEGER NOT NULL DEFAULT 0,
          |    "order" INTEGER NOT NULL,
          |    PRIMARY KEY (playlistId, songId)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE listening_history (
          |    songId TEXT NOT NULL PRIMARY KEY,
          |    songTitle TEXT NOT NULL,
          |    artist TEXT NOT NULL,
          |    thumbnailUrl TEXT,
          |
          |    album TEXT NOT NULL DEFAULT '',
          |    duration INTEGER NOT NULL DEFAULT 0,
          |    localUri TEXT,
          |
          |    playCount INTEGER NOT NULL DEFAULT 0,
          |    totalDurationMs INTEGER NOT NULL DEFAULT 0,
          |    lastPlayed INTEGER NOT NULL,
          |    firstPlayed INTEGER NOT NULL,
          |
          |    skipCount INTEGER NOT NULL DEFAULT 0,
          |    completionRate REAL NOT NULL DEFAULT 0,
          |    isLiked INTEGER NOT NULL DEFAULT 0,
          |
          |    artistId TEXT,
          |    source TEXT NOT NULL DEFAULT 'YOUTUBE',
          |    releaseDate TEXT
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE song_genres (
          |    songId TEXT NOT NULL PRIMARY KEY,
          |    genreVector TEXT NOT NULL,
          |    timestamp INTEGER NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, "CREATE INDEX playlist_songs_playlistId ON playlist_songs(playlistId)",
          0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
