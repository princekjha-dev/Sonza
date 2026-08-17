package com.sonza.app.core.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Boolean
import kotlin.Long
import kotlin.String

public class LibraryItemsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> selectItemById(id: String, mapper: (
    id: String,
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    type: String,
    timestamp: Long,
  ) -> T): Query<T> = SelectItemByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!
    )
  }

  public fun selectItemById(id: String): Query<Library_items> = selectItemById(id) { id_, title,
      subtitle, thumbnailUrl, type, timestamp ->
    Library_items(
      id_,
      title,
      subtitle,
      thumbnailUrl,
      type,
      timestamp
    )
  }

  public fun <T : Any> selectAllItems(mapper: (
    id: String,
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    type: String,
    timestamp: Long,
  ) -> T): Query<T> = Query(-79_814_518, arrayOf("library_items"), driver, "LibraryItems.sq",
      "selectAllItems",
      "SELECT library_items.id, library_items.title, library_items.subtitle, library_items.thumbnailUrl, library_items.type, library_items.timestamp FROM library_items ORDER BY timestamp DESC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!
    )
  }

  public fun selectAllItems(): Query<Library_items> = selectAllItems { id, title, subtitle,
      thumbnailUrl, type, timestamp ->
    Library_items(
      id,
      title,
      subtitle,
      thumbnailUrl,
      type,
      timestamp
    )
  }

  public fun <T : Any> selectItemsByType(type: String, mapper: (
    id: String,
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    type: String,
    timestamp: Long,
  ) -> T): Query<T> = SelectItemsByTypeQuery(type) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!
    )
  }

  public fun selectItemsByType(type: String): Query<Library_items> = selectItemsByType(type) { id,
      title, subtitle, thumbnailUrl, type_, timestamp ->
    Library_items(
      id,
      title,
      subtitle,
      thumbnailUrl,
      type_,
      timestamp
    )
  }

  public fun <T : Any> selectItemsByTypeWithSongCount(type: String, mapper: (
    id: String,
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    type: String,
    timestamp: Long,
    songCount: Long,
  ) -> T): Query<T> = SelectItemsByTypeWithSongCountQuery(type) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getString(3),
      cursor.getString(4)!!,
      cursor.getLong(5)!!,
      cursor.getLong(6)!!
    )
  }

  public fun selectItemsByTypeWithSongCount(type: String): Query<SelectItemsByTypeWithSongCount> =
      selectItemsByTypeWithSongCount(type) { id, title, subtitle, thumbnailUrl, type_, timestamp,
      songCount ->
    SelectItemsByTypeWithSongCount(
      id,
      title,
      subtitle,
      thumbnailUrl,
      type_,
      timestamp,
      songCount
    )
  }

  public fun isItemSaved(id: String): Query<Boolean> = IsItemSavedQuery(id) { cursor ->
    cursor.getBoolean(0)!!
  }

  public fun <T : Any> selectAllPlaylistSongs(mapper: (
    playlistId: String,
    songId: String,
    title: String,
    artist: String,
    album: String?,
    thumbnailUrl: String?,
    duration: Long,
    source: String,
    localUri: String?,
    releaseDate: String?,
    addedAt: Long,
    order: Long,
  ) -> T): Query<T> = Query(-611_754_474, arrayOf("playlist_songs"), driver, "LibraryItems.sq",
      "selectAllPlaylistSongs",
      "SELECT playlist_songs.playlistId, playlist_songs.songId, playlist_songs.title, playlist_songs.artist, playlist_songs.album, playlist_songs.thumbnailUrl, playlist_songs.duration, playlist_songs.source, playlist_songs.localUri, playlist_songs.releaseDate, playlist_songs.addedAt, playlist_songs.\"order\" FROM playlist_songs") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8),
      cursor.getString(9),
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun selectAllPlaylistSongs(): Query<Playlist_songs> = selectAllPlaylistSongs { playlistId,
      songId, title, artist, album, thumbnailUrl, duration, source, localUri, releaseDate, addedAt,
      order ->
    Playlist_songs(
      playlistId,
      songId,
      title,
      artist,
      album,
      thumbnailUrl,
      duration,
      source,
      localUri,
      releaseDate,
      addedAt,
      order
    )
  }

  public fun <T : Any> selectPlaylistSongs(playlistId: String, mapper: (
    playlistId: String,
    songId: String,
    title: String,
    artist: String,
    album: String?,
    thumbnailUrl: String?,
    duration: Long,
    source: String,
    localUri: String?,
    releaseDate: String?,
    addedAt: Long,
    order: Long,
  ) -> T): Query<T> = SelectPlaylistSongsQuery(playlistId) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4),
      cursor.getString(5),
      cursor.getLong(6)!!,
      cursor.getString(7)!!,
      cursor.getString(8),
      cursor.getString(9),
      cursor.getLong(10)!!,
      cursor.getLong(11)!!
    )
  }

  public fun selectPlaylistSongs(playlistId: String): Query<Playlist_songs> =
      selectPlaylistSongs(playlistId) { playlistId_, songId, title, artist, album, thumbnailUrl,
      duration, source, localUri, releaseDate, addedAt, order ->
    Playlist_songs(
      playlistId_,
      songId,
      title,
      artist,
      album,
      thumbnailUrl,
      duration,
      source,
      localUri,
      releaseDate,
      addedAt,
      order
    )
  }

  public fun countPlaylistSongs(playlistId: String): Query<Long> =
      CountPlaylistSongsQuery(playlistId) { cursor ->
    cursor.getLong(0)!!
  }

  public fun isSongInPlaylist(playlistId: String, songId: String): Query<Boolean> =
      IsSongInPlaylistQuery(playlistId, songId) { cursor ->
    cursor.getBoolean(0)!!
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertOrReplaceItem(
    id: String,
    title: String,
    subtitle: String?,
    thumbnailUrl: String?,
    type: String,
    timestamp: Long,
  ): QueryResult<Long> {
    val result = driver.execute(48_162_908, """
        |INSERT OR REPLACE INTO library_items (id, title, subtitle, thumbnailUrl, type, timestamp)
        |VALUES (?, ?, ?, ?, ?, ?)
        """.trimMargin(), 6) {
          bindString(0, id)
          bindString(1, title)
          bindString(2, subtitle)
          bindString(3, thumbnailUrl)
          bindString(4, type)
          bindLong(5, timestamp)
        }
    notifyQueries(48_162_908) { emit ->
      emit("library_items")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateItemThumbnail(thumbnailUrl: String?, id: String): QueryResult<Long> {
    val result = driver.execute(-352_042_655,
        """UPDATE library_items SET thumbnailUrl = ? WHERE id = ? AND type = 'PLAYLIST'""", 2) {
          bindString(0, thumbnailUrl)
          bindString(1, id)
        }
    notifyQueries(-352_042_655) { emit ->
      emit("library_items")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun updateItemName(title: String, id: String): QueryResult<Long> {
    val result = driver.execute(-762_555_146,
        """UPDATE library_items SET title = ? WHERE id = ? AND type = 'PLAYLIST'""", 2) {
          bindString(0, title)
          bindString(1, id)
        }
    notifyQueries(-762_555_146) { emit ->
      emit("library_items")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteItemById(id: String): QueryResult<Long> {
    val result = driver.execute(608_928_031, """DELETE FROM library_items WHERE id = ?""", 1) {
          bindString(0, id)
        }
    notifyQueries(608_928_031) { emit ->
      emit("library_items")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteAllItems(): QueryResult<Long> {
    val result = driver.execute(1_209_881_145, """DELETE FROM library_items""", 0)
    notifyQueries(1_209_881_145) { emit ->
      emit("library_items")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertOrReplacePlaylistSong(
    playlistId: String,
    songId: String,
    title: String,
    artist: String,
    album: String?,
    thumbnailUrl: String?,
    duration: Long,
    source: String,
    localUri: String?,
    releaseDate: String?,
    addedAt: Long,
    order: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-1_828_141_488, """
        |INSERT OR REPLACE INTO playlist_songs (
        |    playlistId, songId, title, artist, album, thumbnailUrl,
        |    duration, source, localUri, releaseDate, addedAt, "order"
        |) VALUES (
        |    ?, ?, ?, ?, ?, ?,
        |    ?, ?, ?, ?, ?, ?
        |)
        """.trimMargin(), 12) {
          bindString(0, playlistId)
          bindString(1, songId)
          bindString(2, title)
          bindString(3, artist)
          bindString(4, album)
          bindString(5, thumbnailUrl)
          bindLong(6, duration)
          bindString(7, source)
          bindString(8, localUri)
          bindString(9, releaseDate)
          bindLong(10, addedAt)
          bindLong(11, order)
        }
    notifyQueries(-1_828_141_488) { emit ->
      emit("playlist_songs")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deletePlaylistSongs(playlistId: String): QueryResult<Long> {
    val result = driver.execute(864_700_402, """DELETE FROM playlist_songs WHERE playlistId = ?""",
        1) {
          bindString(0, playlistId)
        }
    notifyQueries(864_700_402) { emit ->
      emit("playlist_songs")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteSongFromPlaylist(playlistId: String, songId: String): QueryResult<Long> {
    val result = driver.execute(2_069_676_779,
        """DELETE FROM playlist_songs WHERE playlistId = ? AND songId = ?""", 2) {
          bindString(0, playlistId)
          bindString(1, songId)
        }
    notifyQueries(2_069_676_779) { emit ->
      emit("playlist_songs")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteAllPlaylistSongs(): QueryResult<Long> {
    val result = driver.execute(1_299_148_997, """DELETE FROM playlist_songs""", 0)
    notifyQueries(1_299_148_997) { emit ->
      emit("playlist_songs")
    }
    return result
  }

  private inner class SelectItemByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("library_items", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("library_items", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-680_767_632,
        """SELECT library_items.id, library_items.title, library_items.subtitle, library_items.thumbnailUrl, library_items.type, library_items.timestamp FROM library_items WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "LibraryItems.sq:selectItemById"
  }

  private inner class SelectItemsByTypeQuery<out T : Any>(
    public val type: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("library_items", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("library_items", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-905_576_890, """
    |SELECT library_items.id, library_items.title, library_items.subtitle, library_items.thumbnailUrl, library_items.type, library_items.timestamp FROM library_items
    |WHERE type = ?
    |ORDER BY timestamp DESC
    """.trimMargin(), mapper, 1) {
      bindString(0, type)
    }

    override fun toString(): String = "LibraryItems.sq:selectItemsByType"
  }

  private inner class SelectItemsByTypeWithSongCountQuery<out T : Any>(
    public val type: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("library_items", "playlist_songs", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("library_items", "playlist_songs", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_374_793_170, """
    |SELECT
    |    library_items.id, library_items.title, library_items.subtitle, library_items.thumbnailUrl, library_items.type, library_items.timestamp,
    |    (SELECT COUNT(*) FROM playlist_songs WHERE playlistId = library_items.id) AS songCount
    |FROM library_items
    |WHERE type = ?
    |ORDER BY timestamp DESC
    """.trimMargin(), mapper, 1) {
      bindString(0, type)
    }

    override fun toString(): String = "LibraryItems.sq:selectItemsByTypeWithSongCount"
  }

  private inner class IsItemSavedQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("library_items", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("library_items", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_254_398_651,
        """SELECT EXISTS(SELECT 1 FROM library_items WHERE id = ?)""", mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "LibraryItems.sq:isItemSaved"
  }

  private inner class SelectPlaylistSongsQuery<out T : Any>(
    public val playlistId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("playlist_songs", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("playlist_songs", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-2_064_464_831, """
    |SELECT playlist_songs.playlistId, playlist_songs.songId, playlist_songs.title, playlist_songs.artist, playlist_songs.album, playlist_songs.thumbnailUrl, playlist_songs.duration, playlist_songs.source, playlist_songs.localUri, playlist_songs.releaseDate, playlist_songs.addedAt, playlist_songs."order" FROM playlist_songs
    |WHERE playlistId = ?
    |ORDER BY "order" ASC
    """.trimMargin(), mapper, 1) {
      bindString(0, playlistId)
    }

    override fun toString(): String = "LibraryItems.sq:selectPlaylistSongs"
  }

  private inner class CountPlaylistSongsQuery<out T : Any>(
    public val playlistId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("playlist_songs", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("playlist_songs", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_792_460_748,
        """SELECT COUNT(*) FROM playlist_songs WHERE playlistId = ?""", mapper, 1) {
      bindString(0, playlistId)
    }

    override fun toString(): String = "LibraryItems.sq:countPlaylistSongs"
  }

  private inner class IsSongInPlaylistQuery<out T : Any>(
    public val playlistId: String,
    public val songId: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("playlist_songs", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("playlist_songs", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-799_362_587,
        """SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = ? AND songId = ?)""",
        mapper, 2) {
      bindString(0, playlistId)
      bindString(1, songId)
    }

    override fun toString(): String = "LibraryItems.sq:isSongInPlaylist"
  }
}
