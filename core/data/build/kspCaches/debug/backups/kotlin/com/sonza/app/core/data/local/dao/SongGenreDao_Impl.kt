package com.sonza.app.core.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.sonza.app.core.`data`.local.entity.SongGenre
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
import kotlin.text.StringBuilder

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SongGenreDao_Impl(
  __db: RoomDatabase,
) : SongGenreDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSongGenre: EntityInsertAdapter<SongGenre>
  init {
    this.__db = __db
    this.__insertAdapterOfSongGenre = object : EntityInsertAdapter<SongGenre>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `song_genres` (`songId`,`genreVector`,`timestamp`) VALUES (?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SongGenre) {
        statement.bindText(1, entity.songId)
        statement.bindText(2, entity.genreVector)
        statement.bindLong(3, entity.timestamp)
      }
    }
  }

  public override suspend fun insertGenre(genre: SongGenre): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSongGenre.insert(_connection, genre)
  }

  public override suspend fun insertGenres(genres: List<SongGenre>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSongGenre.insert(_connection, genres)
  }

  public override suspend fun getGenre(songId: String): SongGenre? {
    val _sql: String = "SELECT * FROM song_genres WHERE songId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, songId)
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfGenreVector: Int = getColumnIndexOrThrow(_stmt, "genreVector")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: SongGenre?
        if (_stmt.step()) {
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpGenreVector: String
          _tmpGenreVector = _stmt.getText(_columnIndexOfGenreVector)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _result = SongGenre(_tmpSongId,_tmpGenreVector,_tmpTimestamp)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getGenres(songIds: List<String>): List<SongGenre> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM song_genres WHERE songId IN (")
    val _inputSize: Int = songIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in songIds) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfGenreVector: Int = getColumnIndexOrThrow(_stmt, "genreVector")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<SongGenre> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: SongGenre
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpGenreVector: String
          _tmpGenreVector = _stmt.getText(_columnIndexOfGenreVector)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item_1 = SongGenre(_tmpSongId,_tmpGenreVector,_tmpTimestamp)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllGenres(): List<SongGenre> {
    val _sql: String = "SELECT * FROM song_genres"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfGenreVector: Int = getColumnIndexOrThrow(_stmt, "genreVector")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<SongGenre> = mutableListOf()
        while (_stmt.step()) {
          val _item: SongGenre
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpGenreVector: String
          _tmpGenreVector = _stmt.getText(_columnIndexOfGenreVector)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = SongGenre(_tmpSongId,_tmpGenreVector,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM song_genres"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM song_genres"
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
