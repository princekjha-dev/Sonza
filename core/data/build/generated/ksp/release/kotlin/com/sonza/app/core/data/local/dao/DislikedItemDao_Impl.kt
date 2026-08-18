package com.sonza.app.core.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.sonza.app.core.`data`.local.entity.DislikedArtist
import com.sonza.app.core.`data`.local.entity.DislikedSong
import javax.`annotation`.processing.Generated
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class DislikedItemDao_Impl(
  __db: RoomDatabase,
) : DislikedItemDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfDislikedSong: EntityInsertAdapter<DislikedSong>

  private val __insertAdapterOfDislikedArtist: EntityInsertAdapter<DislikedArtist>
  init {
    this.__db = __db
    this.__insertAdapterOfDislikedSong = object : EntityInsertAdapter<DislikedSong>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `disliked_songs` (`songId`,`title`,`artist`,`timestamp`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DislikedSong) {
        statement.bindText(1, entity.songId)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.artist)
        statement.bindLong(4, entity.timestamp)
      }
    }
    this.__insertAdapterOfDislikedArtist = object : EntityInsertAdapter<DislikedArtist>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `disliked_artists` (`artistName`,`timestamp`) VALUES (?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DislikedArtist) {
        statement.bindText(1, entity.artistName)
        statement.bindLong(2, entity.timestamp)
      }
    }
  }

  public override suspend fun insertDislikedSong(song: DislikedSong): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDislikedSong.insert(_connection, song)
  }

  public override suspend fun insertDislikedSongs(songs: List<DislikedSong>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDislikedSong.insert(_connection, songs)
  }

  public override suspend fun insertDislikedArtist(artist: DislikedArtist): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDislikedArtist.insert(_connection, artist)
  }

  public override suspend fun insertDislikedArtists(artists: List<DislikedArtist>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDislikedArtist.insert(_connection, artists)
  }

  public override suspend fun getAllDislikedSongIds(): List<String> {
    val _sql: String = "SELECT songId FROM disliked_songs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllDislikedSongs(): List<DislikedSong> {
    val _sql: String = "SELECT * FROM disliked_songs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<DislikedSong> = mutableListOf()
        while (_stmt.step()) {
          val _item: DislikedSong
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = DislikedSong(_tmpSongId,_tmpTitle,_tmpArtist,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllDislikedArtistNames(): List<String> {
    val _sql: String = "SELECT artistName FROM disliked_artists"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: MutableList<String> = mutableListOf()
        while (_stmt.step()) {
          val _item: String
          _item = _stmt.getText(0)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllDislikedArtists(): List<DislikedArtist> {
    val _sql: String = "SELECT * FROM disliked_artists"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfArtistName: Int = getColumnIndexOrThrow(_stmt, "artistName")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<DislikedArtist> = mutableListOf()
        while (_stmt.step()) {
          val _item: DislikedArtist
          val _tmpArtistName: String
          _tmpArtistName = _stmt.getText(_columnIndexOfArtistName)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = DislikedArtist(_tmpArtistName,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removeDislikedSong(songId: String) {
    val _sql: String = "DELETE FROM disliked_songs WHERE songId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, songId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun removeDislikedArtist(artistName: String) {
    val _sql: String = "DELETE FROM disliked_artists WHERE artistName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, artistName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAllDislikedSongs() {
    val _sql: String = "DELETE FROM disliked_songs"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAllDislikedArtists() {
    val _sql: String = "DELETE FROM disliked_artists"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
