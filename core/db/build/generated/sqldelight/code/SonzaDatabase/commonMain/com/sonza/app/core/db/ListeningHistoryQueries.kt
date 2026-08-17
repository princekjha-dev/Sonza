package com.sonza.app.core.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Double
import kotlin.Long
import kotlin.String

public class ListeningHistoryQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectAll(mapper: (
    songId: String,
    songTitle: String,
    artist: String,
    thumbnailUrl: String?,
    album: String,
    duration: Long,
    localUri: String?,
    playCount: Long,
    totalDurationMs: Long,
    lastPlayed: Long,
    firstPlayed: Long,
    skipCount: Long,
    completionRate: Double,
    isLiked: Long,
    artistId: String?,
    source: String,
    releaseDate: String?,
  ) -> T): Query<T> = Query(-858_835_006, arrayOf("listening_history"), driver,
      "ListeningHistory.sq", "selectAll",
      "SELECT listening_history.songId, listening_history.songTitle, listening_history.artist, listening_history.thumbnailUrl, listening_history.album, listening_history.duration, listening_history.localUri, listening_history.playCount, listening_history.totalDurationMs, listening_history.lastPlayed, listening_history.firstPlayed, listening_history.skipCount, listening_history.completionRate, listening_history.isLiked, listening_history.artistId, listening_history.source, listening_history.releaseDate FROM listening_history") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getLong(13)!!,
      cursor.getString(14),
      cursor.getString(15)!!,
      cursor.getString(16)
    )
  }

  public fun selectAll(): Query<Listening_history> = selectAll { songId, songTitle, artist,
      thumbnailUrl, album, duration, localUri, playCount, totalDurationMs, lastPlayed, firstPlayed,
      skipCount, completionRate, isLiked, artistId, source, releaseDate ->
    Listening_history(
      songId,
      songTitle,
      artist,
      thumbnailUrl,
      album,
      duration,
      localUri,
      playCount,
      totalDurationMs,
      lastPlayed,
      firstPlayed,
      skipCount,
      completionRate,
      isLiked,
      artistId,
      source,
      releaseDate
    )
  }

  public fun <T : Any> selectBySongId(songId: String, mapper: (
    songId: String,
    songTitle: String,
    artist: String,
    thumbnailUrl: String?,
    album: String,
    duration: Long,
    localUri: String?,
    playCount: Long,
    totalDurationMs: Long,
    lastPlayed: Long,
    firstPlayed: Long,
    skipCount: Long,
    completionRate: Double,
    isLiked: Long,
    artistId: String?,
    source: String,
    releaseDate: String?,
  ) -> T): Query<T> = SelectBySongIdQuery(songId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getLong(13)!!,
      cursor.getString(14),
      cursor.getString(15)!!,
      cursor.getString(16)
    )
  }

  public fun selectBySongId(songId: String): Query<Listening_history> = selectBySongId(songId) {
      songId_, songTitle, artist, thumbnailUrl, album, duration, localUri, playCount,
      totalDurationMs, lastPlayed, firstPlayed, skipCount, completionRate, isLiked, artistId,
      source, releaseDate ->
    Listening_history(
      songId_,
      songTitle,
      artist,
      thumbnailUrl,
      album,
      duration,
      localUri,
      playCount,
      totalDurationMs,
      lastPlayed,
      firstPlayed,
      skipCount,
      completionRate,
      isLiked,
      artistId,
      source,
      releaseDate
    )
  }

  public fun <T : Any> selectTopByPlayCount(`value`: Long, mapper: (
    songId: String,
    songTitle: String,
    artist: String,
    thumbnailUrl: String?,
    album: String,
    duration: Long,
    localUri: String?,
    playCount: Long,
    totalDurationMs: Long,
    lastPlayed: Long,
    firstPlayed: Long,
    skipCount: Long,
    completionRate: Double,
    isLiked: Long,
    artistId: String?,
    source: String,
    releaseDate: String?,
  ) -> T): Query<T> = SelectTopByPlayCountQuery(value) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getLong(13)!!,
      cursor.getString(14),
      cursor.getString(15)!!,
      cursor.getString(16)
    )
  }

  public fun selectTopByPlayCount(value_: Long): Query<Listening_history> =
      selectTopByPlayCount(value_) { songId, songTitle, artist, thumbnailUrl, album, duration,
      localUri, playCount, totalDurationMs, lastPlayed, firstPlayed, skipCount, completionRate,
      isLiked, artistId, source, releaseDate ->
    Listening_history(
      songId,
      songTitle,
      artist,
      thumbnailUrl,
      album,
      duration,
      localUri,
      playCount,
      totalDurationMs,
      lastPlayed,
      firstPlayed,
      skipCount,
      completionRate,
      isLiked,
      artistId,
      source,
      releaseDate
    )
  }

  public fun <T : Any> selectRecentByLastPlayed(`value`: Long, mapper: (
    songId: String,
    songTitle: String,
    artist: String,
    thumbnailUrl: String?,
    album: String,
    duration: Long,
    localUri: String?,
    playCount: Long,
    totalDurationMs: Long,
    lastPlayed: Long,
    firstPlayed: Long,
    skipCount: Long,
    completionRate: Double,
    isLiked: Long,
    artistId: String?,
    source: String,
    releaseDate: String?,
  ) -> T): Query<T> = SelectRecentByLastPlayedQuery(value) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getLong(13)!!,
      cursor.getString(14),
      cursor.getString(15)!!,
      cursor.getString(16)
    )
  }

  public fun selectRecentByLastPlayed(value_: Long): Query<Listening_history> =
      selectRecentByLastPlayed(value_) { songId, songTitle, artist, thumbnailUrl, album, duration,
      localUri, playCount, totalDurationMs, lastPlayed, firstPlayed, skipCount, completionRate,
      isLiked, artistId, source, releaseDate ->
    Listening_history(
      songId,
      songTitle,
      artist,
      thumbnailUrl,
      album,
      duration,
      localUri,
      playCount,
      totalDurationMs,
      lastPlayed,
      firstPlayed,
      skipCount,
      completionRate,
      isLiked,
      artistId,
      source,
      releaseDate
    )
  }

  public fun <T : Any> selectAfterTimestamp(lastPlayed: Long, mapper: (
    songId: String,
    songTitle: String,
    artist: String,
    thumbnailUrl: String?,
    album: String,
    duration: Long,
    localUri: String?,
    playCount: Long,
    totalDurationMs: Long,
    lastPlayed: Long,
    firstPlayed: Long,
    skipCount: Long,
    completionRate: Double,
    isLiked: Long,
    artistId: String?,
    source: String,
    releaseDate: String?,
  ) -> T): Query<T> = SelectAfterTimestampQuery(lastPlayed) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getLong(13)!!,
      cursor.getString(14),
      cursor.getString(15)!!,
      cursor.getString(16)
    )
  }

  public fun selectAfterTimestamp(lastPlayed: Long): Query<Listening_history> =
      selectAfterTimestamp(lastPlayed) { songId, songTitle, artist, thumbnailUrl, album, duration,
      localUri, playCount, totalDurationMs, lastPlayed_, firstPlayed, skipCount, completionRate,
      isLiked, artistId, source, releaseDate ->
    Listening_history(
      songId,
      songTitle,
      artist,
      thumbnailUrl,
      album,
      duration,
      localUri,
      playCount,
      totalDurationMs,
      lastPlayed_,
      firstPlayed,
      skipCount,
      completionRate,
      isLiked,
      artistId,
      source,
      releaseDate
    )
  }

  public fun <T : Any> selectTopAfterTimestamp(lastPlayed: Long, mapper: (
    songId: String,
    songTitle: String,
    artist: String,
    thumbnailUrl: String?,
    album: String,
    duration: Long,
    localUri: String?,
    playCount: Long,
    totalDurationMs: Long,
    lastPlayed: Long,
    firstPlayed: Long,
    skipCount: Long,
    completionRate: Double,
    isLiked: Long,
    artistId: String?,
    source: String,
    releaseDate: String?,
  ) -> T): Query<T> = SelectTopAfterTimestampQuery(lastPlayed) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getLong(13)!!,
      cursor.getString(14),
      cursor.getString(15)!!,
      cursor.getString(16)
    )
  }

  public fun selectTopAfterTimestamp(lastPlayed: Long): Query<Listening_history> =
      selectTopAfterTimestamp(lastPlayed) { songId, songTitle, artist, thumbnailUrl, album,
      duration, localUri, playCount, totalDurationMs, lastPlayed_, firstPlayed, skipCount,
      completionRate, isLiked, artistId, source, releaseDate ->
    Listening_history(
      songId,
      songTitle,
      artist,
      thumbnailUrl,
      album,
      duration,
      localUri,
      playCount,
      totalDurationMs,
      lastPlayed_,
      firstPlayed,
      skipCount,
      completionRate,
      isLiked,
      artistId,
      source,
      releaseDate
    )
  }

  public fun <T : Any> selectFirstEver(mapper: (
    songId: String,
    songTitle: String,
    artist: String,
    thumbnailUrl: String?,
    album: String,
    duration: Long,
    localUri: String?,
    playCount: Long,
    totalDurationMs: Long,
    lastPlayed: Long,
    firstPlayed: Long,
    skipCount: Long,
    completionRate: Double,
    isLiked: Long,
    artistId: String?,
    source: String,
    releaseDate: String?,
  ) -> T): Query<T> = Query(-123_993_777, arrayOf("listening_history"), driver,
      "ListeningHistory.sq", "selectFirstEver", """
  |SELECT listening_history.songId, listening_history.songTitle, listening_history.artist, listening_history.thumbnailUrl, listening_history.album, listening_history.duration, listening_history.localUri, listening_history.playCount, listening_history.totalDurationMs, listening_history.lastPlayed, listening_history.firstPlayed, listening_history.skipCount, listening_history.completionRate, listening_history.isLiked, listening_history.artistId, listening_history.source, listening_history.releaseDate FROM listening_history
  |ORDER BY firstPlayed ASC
  |LIMIT 1
  """.trimMargin()) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getString(6),
      cursor.getLong(7)!!,
      cursor.getLong(8)!!,
      cursor.getLong(9)!!,
      cursor.getLong(10)!!,
      cursor.getLong(11)!!,
      cursor.getDouble(12)!!,
      cursor.getLong(13)!!,
      cursor.getString(14),
      cursor.getString(15)!!,
      cursor.getString(16)
    )
  }

  public fun selectFirstEver(): Query<Listening_history> = selectFirstEver { songId, songTitle,
      artist, thumbnailUrl, album, duration, localUri, playCount, totalDurationMs, lastPlayed,
      firstPlayed, skipCount, completionRate, isLiked, artistId, source, releaseDate ->
    Listening_history(
      songId,
      songTitle,
      artist,
      thumbnailUrl,
      album,
      duration,
      localUri,
      playCount,
      totalDurationMs,
      lastPlayed,
      firstPlayed,
      skipCount,
      completionRate,
      isLiked,
      artistId,
      source,
      releaseDate
    )
  }

  public fun countAll(): Query<Long> = Query(-1_037_141_579, arrayOf("listening_history"), driver,
      "ListeningHistory.sq", "countAll", "SELECT COUNT(*) FROM listening_history") { cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> sumTotalListeningTime(mapper: (SUM: Long?) -> T): Query<T> =
      Query(-1_296_933_844, arrayOf("listening_history"), driver, "ListeningHistory.sq",
      "sumTotalListeningTime", "SELECT SUM(totalDurationMs) FROM listening_history") { cursor ->
    mapper(
      cursor.getLong(0)
    )
  }

  public fun sumTotalListeningTime(): Query<SumTotalListeningTime> = sumTotalListeningTime { SUM ->
    SumTotalListeningTime(
      SUM
    )
  }

  public fun <T : Any> selectTopArtists(`value`: Long, mapper: (artist: String,
      totalPlays: Long?) -> T): Query<T> = SelectTopArtistsQuery(value) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)
    )
  }

  public fun selectTopArtists(value_: Long): Query<SelectTopArtists> = selectTopArtists(value_) {
      artist, totalPlays ->
    SelectTopArtists(
      artist,
      totalPlays
    )
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertOrReplace(
    songId: String,
    songTitle: String,
    artist: String,
    thumbnailUrl: String?,
    album: String,
    duration: Long,
    localUri: String?,
    playCount: Long,
    totalDurationMs: Long,
    lastPlayed: Long,
    firstPlayed: Long,
    skipCount: Long,
    completionRate: Double,
    isLiked: Long,
    artistId: String?,
    source: String,
    releaseDate: String?,
  ): QueryResult<Long> {
    val result = driver.execute(-833_495_147, """
        |INSERT OR REPLACE INTO listening_history (
        |    songId, songTitle, artist, thumbnailUrl,
        |    album, duration, localUri,
        |    playCount, totalDurationMs, lastPlayed, firstPlayed,
        |    skipCount, completionRate, isLiked,
        |    artistId, source, releaseDate
        |) VALUES (
        |    ?, ?, ?, ?,
        |    ?, ?, ?,
        |    ?, ?, ?, ?,
        |    ?, ?, ?,
        |    ?, ?, ?
        |)
        """.trimMargin(), 17) {
          bindString(0, songId)
          bindString(1, songTitle)
          bindString(2, artist)
          bindString(3, thumbnailUrl)
          bindString(4, album)
          bindLong(5, duration)
          bindString(6, localUri)
          bindLong(7, playCount)
          bindLong(8, totalDurationMs)
          bindLong(9, lastPlayed)
          bindLong(10, firstPlayed)
          bindLong(11, skipCount)
          bindDouble(12, completionRate)
          bindLong(13, isLiked)
          bindString(14, artistId)
          bindString(15, source)
          bindString(16, releaseDate)
        }
    notifyQueries(-833_495_147) { emit ->
      emit("listening_history")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteAll(): QueryResult<Long> {
    val result = driver.execute(498_431_155, """DELETE FROM listening_history""", 0)
    notifyQueries(498_431_155) { emit ->
      emit("listening_history")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteOlderThan(lastPlayed: Long): QueryResult<Long> {
    val result = driver.execute(-1_189_229_977,
        """DELETE FROM listening_history WHERE lastPlayed < ?""", 1) {
          bindLong(0, lastPlayed)
        }
    notifyQueries(-1_189_229_977) { emit ->
      emit("listening_history")
    }
    return result
  }

  private inner class SelectBySongIdQuery<out T : Any>(
    public val songId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("listening_history", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("listening_history", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-178_252_090,
        """SELECT listening_history.songId, listening_history.songTitle, listening_history.artist, listening_history.thumbnailUrl, listening_history.album, listening_history.duration, listening_history.localUri, listening_history.playCount, listening_history.totalDurationMs, listening_history.lastPlayed, listening_history.firstPlayed, listening_history.skipCount, listening_history.completionRate, listening_history.isLiked, listening_history.artistId, listening_history.source, listening_history.releaseDate FROM listening_history WHERE songId = ?""",
        mapper, 1) {
      bindString(0, songId)
    }

    override fun toString(): String = "ListeningHistory.sq:selectBySongId"
  }

  private inner class SelectTopByPlayCountQuery<out T : Any>(
    public val `value`: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("listening_history", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("listening_history", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-127_484_338, """
    |SELECT listening_history.songId, listening_history.songTitle, listening_history.artist, listening_history.thumbnailUrl, listening_history.album, listening_history.duration, listening_history.localUri, listening_history.playCount, listening_history.totalDurationMs, listening_history.lastPlayed, listening_history.firstPlayed, listening_history.skipCount, listening_history.completionRate, listening_history.isLiked, listening_history.artistId, listening_history.source, listening_history.releaseDate FROM listening_history
    |ORDER BY playCount DESC
    |LIMIT ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, value)
    }

    override fun toString(): String = "ListeningHistory.sq:selectTopByPlayCount"
  }

  private inner class SelectRecentByLastPlayedQuery<out T : Any>(
    public val `value`: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("listening_history", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("listening_history", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(577_437_306, """
    |SELECT listening_history.songId, listening_history.songTitle, listening_history.artist, listening_history.thumbnailUrl, listening_history.album, listening_history.duration, listening_history.localUri, listening_history.playCount, listening_history.totalDurationMs, listening_history.lastPlayed, listening_history.firstPlayed, listening_history.skipCount, listening_history.completionRate, listening_history.isLiked, listening_history.artistId, listening_history.source, listening_history.releaseDate FROM listening_history
    |ORDER BY lastPlayed DESC
    |LIMIT ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, value)
    }

    override fun toString(): String = "ListeningHistory.sq:selectRecentByLastPlayed"
  }

  private inner class SelectAfterTimestampQuery<out T : Any>(
    public val lastPlayed: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("listening_history", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("listening_history", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(320_663_001, """
    |SELECT listening_history.songId, listening_history.songTitle, listening_history.artist, listening_history.thumbnailUrl, listening_history.album, listening_history.duration, listening_history.localUri, listening_history.playCount, listening_history.totalDurationMs, listening_history.lastPlayed, listening_history.firstPlayed, listening_history.skipCount, listening_history.completionRate, listening_history.isLiked, listening_history.artistId, listening_history.source, listening_history.releaseDate FROM listening_history
    |WHERE lastPlayed >= ?
    |ORDER BY lastPlayed DESC
    """.trimMargin(), mapper, 1) {
      bindLong(0, lastPlayed)
    }

    override fun toString(): String = "ListeningHistory.sq:selectAfterTimestamp"
  }

  private inner class SelectTopAfterTimestampQuery<out T : Any>(
    public val lastPlayed: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("listening_history", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("listening_history", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-619_307_088, """
    |SELECT listening_history.songId, listening_history.songTitle, listening_history.artist, listening_history.thumbnailUrl, listening_history.album, listening_history.duration, listening_history.localUri, listening_history.playCount, listening_history.totalDurationMs, listening_history.lastPlayed, listening_history.firstPlayed, listening_history.skipCount, listening_history.completionRate, listening_history.isLiked, listening_history.artistId, listening_history.source, listening_history.releaseDate FROM listening_history
    |WHERE lastPlayed > ?
    |ORDER BY playCount DESC
    """.trimMargin(), mapper, 1) {
      bindLong(0, lastPlayed)
    }

    override fun toString(): String = "ListeningHistory.sq:selectTopAfterTimestamp"
  }

  private inner class SelectTopArtistsQuery<out T : Any>(
    public val `value`: Long,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("listening_history", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("listening_history", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-890_244_682, """
    |SELECT artist, SUM(playCount) AS totalPlays
    |FROM listening_history
    |GROUP BY artist
    |ORDER BY totalPlays DESC
    |LIMIT ?
    """.trimMargin(), mapper, 1) {
      bindLong(0, value)
    }

    override fun toString(): String = "ListeningHistory.sq:selectTopArtists"
  }
}
