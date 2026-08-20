package com.sonza.app.core.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.sonza.app.core.`data`.local.entity.LyricsEntity
import javax.`annotation`.processing.Generated
import kotlin.Boolean
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
public class LyricsDao_Impl(
  __db: RoomDatabase,
) : LyricsDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLyricsEntity: EntityInsertAdapter<LyricsEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfLyricsEntity = object : EntityInsertAdapter<LyricsEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `lyrics_cache` (`songId`,`providerName`,`lrcContent`,`isSynced`,`sourceCredit`,`timestamp`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LyricsEntity) {
        statement.bindText(1, entity.songId)
        statement.bindText(2, entity.providerName)
        statement.bindText(3, entity.lrcContent)
        val _tmp: Int = if (entity.isSynced) 1 else 0
        statement.bindLong(4, _tmp.toLong())
        val _tmpSourceCredit: String? = entity.sourceCredit
        if (_tmpSourceCredit == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpSourceCredit)
        }
        statement.bindLong(6, entity.timestamp)
      }
    }
  }

  public override suspend fun upsert(entity: LyricsEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLyricsEntity.insert(_connection, entity)
  }

  public override suspend fun `get`(songId: String, providerName: String): LyricsEntity? {
    val _sql: String = "SELECT * FROM lyrics_cache WHERE songId = ? AND providerName = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, songId)
        _argIndex = 2
        _stmt.bindText(_argIndex, providerName)
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfProviderName: Int = getColumnIndexOrThrow(_stmt, "providerName")
        val _columnIndexOfLrcContent: Int = getColumnIndexOrThrow(_stmt, "lrcContent")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfSourceCredit: Int = getColumnIndexOrThrow(_stmt, "sourceCredit")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: LyricsEntity?
        if (_stmt.step()) {
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpProviderName: String
          _tmpProviderName = _stmt.getText(_columnIndexOfProviderName)
          val _tmpLrcContent: String
          _tmpLrcContent = _stmt.getText(_columnIndexOfLrcContent)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          val _tmpSourceCredit: String?
          if (_stmt.isNull(_columnIndexOfSourceCredit)) {
            _tmpSourceCredit = null
          } else {
            _tmpSourceCredit = _stmt.getText(_columnIndexOfSourceCredit)
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _result = LyricsEntity(_tmpSongId,_tmpProviderName,_tmpLrcContent,_tmpIsSynced,_tmpSourceCredit,_tmpTimestamp)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllForSong(songId: String): List<LyricsEntity> {
    val _sql: String = "SELECT * FROM lyrics_cache WHERE songId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, songId)
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfProviderName: Int = getColumnIndexOrThrow(_stmt, "providerName")
        val _columnIndexOfLrcContent: Int = getColumnIndexOrThrow(_stmt, "lrcContent")
        val _columnIndexOfIsSynced: Int = getColumnIndexOrThrow(_stmt, "isSynced")
        val _columnIndexOfSourceCredit: Int = getColumnIndexOrThrow(_stmt, "sourceCredit")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<LyricsEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LyricsEntity
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpProviderName: String
          _tmpProviderName = _stmt.getText(_columnIndexOfProviderName)
          val _tmpLrcContent: String
          _tmpLrcContent = _stmt.getText(_columnIndexOfLrcContent)
          val _tmpIsSynced: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsSynced).toInt()
          _tmpIsSynced = _tmp != 0
          val _tmpSourceCredit: String?
          if (_stmt.isNull(_columnIndexOfSourceCredit)) {
            _tmpSourceCredit = null
          } else {
            _tmpSourceCredit = _stmt.getText(_columnIndexOfSourceCredit)
          }
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = LyricsEntity(_tmpSongId,_tmpProviderName,_tmpLrcContent,_tmpIsSynced,_tmpSourceCredit,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM lyrics_cache"
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

  public override suspend fun deleteForSong(songId: String) {
    val _sql: String = "DELETE FROM lyrics_cache WHERE songId = ?"
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

  public override suspend fun delete(songId: String, providerName: String) {
    val _sql: String = "DELETE FROM lyrics_cache WHERE songId = ? AND providerName = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, songId)
        _argIndex = 2
        _stmt.bindText(_argIndex, providerName)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM lyrics_cache"
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
