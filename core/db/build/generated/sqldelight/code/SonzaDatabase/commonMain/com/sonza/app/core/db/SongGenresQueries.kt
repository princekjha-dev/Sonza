package com.sonza.app.core.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String
import kotlin.collections.Collection

public class SongGenresQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectGenre(songId: String, mapper: (
    songId: String,
    genreVector: String,
    timestamp: Long,
  ) -> T): Query<T> = SelectGenreQuery(songId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!
    )
  }

  public fun selectGenre(songId: String): Query<Song_genres> = selectGenre(songId) { songId_,
      genreVector, timestamp ->
    Song_genres(
      songId_,
      genreVector,
      timestamp
    )
  }

  public fun <T : Any> selectGenresByIds(songId: Collection<String>, mapper: (
    songId: String,
    genreVector: String,
    timestamp: Long,
  ) -> T): Query<T> = SelectGenresByIdsQuery(songId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!
    )
  }

  public fun selectGenresByIds(songId: Collection<String>): Query<Song_genres> =
      selectGenresByIds(songId) { songId_, genreVector, timestamp ->
    Song_genres(
      songId_,
      genreVector,
      timestamp
    )
  }

  public fun <T : Any> selectAll(mapper: (
    songId: String,
    genreVector: String,
    timestamp: Long,
  ) -> T): Query<T> = Query(-1_623_801_642, arrayOf("song_genres"), driver, "SongGenres.sq",
      "selectAll",
      "SELECT song_genres.songId, song_genres.genreVector, song_genres.timestamp FROM song_genres") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getLong(2)!!
    )
  }

  public fun selectAll(): Query<Song_genres> = selectAll { songId, genreVector, timestamp ->
    Song_genres(
      songId,
      genreVector,
      timestamp
    )
  }

  public fun countAll(): Query<Long> = Query(-2_031_649_247, arrayOf("song_genres"), driver,
      "SongGenres.sq", "countAll", "SELECT COUNT(*) FROM song_genres") { cursor ->
    cursor.getLong(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertOrReplace(
    songId: String,
    genreVector: String,
    timestamp: Long,
  ): QueryResult<Long> {
    val result = driver.execute(1_328_236_969, """
        |INSERT OR REPLACE INTO song_genres (songId, genreVector, timestamp)
        |VALUES (?, ?, ?)
        """.trimMargin(), 3) {
          bindString(0, songId)
          bindString(1, genreVector)
          bindLong(2, timestamp)
        }
    notifyQueries(1_328_236_969) { emit ->
      emit("song_genres")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteAll(): QueryResult<Long> {
    val result = driver.execute(-266_535_481, """DELETE FROM song_genres""", 0)
    notifyQueries(-266_535_481) { emit ->
      emit("song_genres")
    }
    return result
  }

  private inner class SelectGenreQuery<out T : Any>(
    public val songId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("song_genres", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("song_genres", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_394_911_368,
        """SELECT song_genres.songId, song_genres.genreVector, song_genres.timestamp FROM song_genres WHERE songId = ?""",
        mapper, 1) {
      bindString(0, songId)
    }

    override fun toString(): String = "SongGenres.sq:selectGenre"
  }

  private inner class SelectGenresByIdsQuery<out T : Any>(
    public val songId: Collection<String>,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("song_genres", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("song_genres", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> {
      val songIdIndexes = createArguments(count = songId.size)
      return driver.executeQuery(null,
          """SELECT song_genres.songId, song_genres.genreVector, song_genres.timestamp FROM song_genres WHERE songId IN $songIdIndexes""",
          mapper, songId.size) {
            songId.forEachIndexed { index, songId_ ->
              bindString(index, songId_)
            }
          }
    }

    override fun toString(): String = "SongGenres.sq:selectGenresByIds"
  }
}
