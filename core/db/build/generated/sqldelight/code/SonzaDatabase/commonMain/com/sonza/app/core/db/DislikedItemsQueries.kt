package com.sonza.app.core.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class DislikedItemsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun selectAllDislikedSongIds(): Query<String> = Query(202_028_914,
      arrayOf("disliked_songs"), driver, "DislikedItems.sq", "selectAllDislikedSongIds",
      "SELECT songId FROM disliked_songs") { cursor ->
    cursor.getString(0)!!
  }

  public fun <T : Any> selectAllDislikedSongs(mapper: (
    songId: String,
    title: String,
    artist: String,
    timestamp: Long,
  ) -> T): Query<T> = Query(795_740_109, arrayOf("disliked_songs"), driver, "DislikedItems.sq",
      "selectAllDislikedSongs",
      "SELECT disliked_songs.songId, disliked_songs.title, disliked_songs.artist, disliked_songs.timestamp FROM disliked_songs") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getLong(3)!!
    )
  }

  public fun selectAllDislikedSongs(): Query<Disliked_songs> = selectAllDislikedSongs { songId,
      title, artist, timestamp ->
    Disliked_songs(
      songId,
      title,
      artist,
      timestamp
    )
  }

  public fun selectAllDislikedArtistNames(): Query<String> = Query(-2_055_602_480,
      arrayOf("disliked_artists"), driver, "DislikedItems.sq", "selectAllDislikedArtistNames",
      "SELECT artistName FROM disliked_artists") { cursor ->
    cursor.getString(0)!!
  }

  public fun <T : Any> selectAllDislikedArtists(mapper: (artistName: String, timestamp: Long) -> T):
      Query<T> = Query(1_498_360_859, arrayOf("disliked_artists"), driver, "DislikedItems.sq",
      "selectAllDislikedArtists",
      "SELECT disliked_artists.artistName, disliked_artists.timestamp FROM disliked_artists") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!
    )
  }

  public fun selectAllDislikedArtists(): Query<Disliked_artists> = selectAllDislikedArtists {
      artistName, timestamp ->
    Disliked_artists(
      artistName,
      timestamp
    )
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertOrReplaceDislikedSong(
    songId: String,
    title: String,
    artist: String,
    timestamp: Long,
  ): QueryResult<Long> {
    val result = driver.execute(-1_404_357_447, """
        |INSERT OR REPLACE INTO disliked_songs (songId, title, artist, timestamp)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindString(0, songId)
          bindString(1, title)
          bindString(2, artist)
          bindLong(3, timestamp)
        }
    notifyQueries(-1_404_357_447) { emit ->
      emit("disliked_songs")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteDislikedSong(songId: String): QueryResult<Long> {
    val result = driver.execute(-140_073_518, """DELETE FROM disliked_songs WHERE songId = ?""", 1)
        {
          bindString(0, songId)
        }
    notifyQueries(-140_073_518) { emit ->
      emit("disliked_songs")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteAllDislikedSongs(): QueryResult<Long> {
    val result = driver.execute(-1_588_323_716, """DELETE FROM disliked_songs""", 0)
    notifyQueries(-1_588_323_716) { emit ->
      emit("disliked_songs")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun insertOrReplaceDislikedArtist(artistName: String, timestamp: Long): QueryResult<Long> {
    val result = driver.execute(-1_480_145_429, """
        |INSERT OR REPLACE INTO disliked_artists (artistName, timestamp)
        |VALUES (?, ?)
        """.trimMargin(), 2) {
          bindString(0, artistName)
          bindLong(1, timestamp)
        }
    notifyQueries(-1_480_145_429) { emit ->
      emit("disliked_artists")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteDislikedArtist(artistName: String): QueryResult<Long> {
    val result = driver.execute(-1_979_034_428,
        """DELETE FROM disliked_artists WHERE artistName = ?""", 1) {
          bindString(0, artistName)
        }
    notifyQueries(-1_979_034_428) { emit ->
      emit("disliked_artists")
    }
    return result
  }

  /**
   * @return The number of rows updated.
   */
  public fun deleteAllDislikedArtists(): QueryResult<Long> {
    val result = driver.execute(-369_406_198, """DELETE FROM disliked_artists""", 0)
    notifyQueries(-369_406_198) { emit ->
      emit("disliked_artists")
    }
    return result
  }
}
