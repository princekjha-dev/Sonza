package com.sonza.app.core.`data`.local.dao

import androidx.room.EntityDeleteOrUpdateAdapter
import androidx.room.EntityInsertAdapter
import androidx.room.EntityUpsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.sonza.app.core.`data`.local.entity.ListeningHistory
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Float
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class ListeningHistoryDao_Impl(
  __db: RoomDatabase,
) : ListeningHistoryDao {
  private val __db: RoomDatabase

  private val __upsertAdapterOfListeningHistory: EntityUpsertAdapter<ListeningHistory>
  init {
    this.__db = __db
    this.__upsertAdapterOfListeningHistory = EntityUpsertAdapter<ListeningHistory>(object : EntityInsertAdapter<ListeningHistory>() {
      protected override fun createQuery(): String = "INSERT INTO `listening_history` (`songId`,`songTitle`,`artist`,`thumbnailUrl`,`album`,`duration`,`localUri`,`playCount`,`totalDurationMs`,`lastPlayed`,`firstPlayed`,`skipCount`,`completionRate`,`isLiked`,`artistId`,`source`,`releaseDate`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ListeningHistory) {
        statement.bindText(1, entity.songId)
        statement.bindText(2, entity.songTitle)
        statement.bindText(3, entity.artist)
        val _tmpThumbnailUrl: String? = entity.thumbnailUrl
        if (_tmpThumbnailUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpThumbnailUrl)
        }
        statement.bindText(5, entity.album)
        statement.bindLong(6, entity.duration)
        val _tmpLocalUri: String? = entity.localUri
        if (_tmpLocalUri == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpLocalUri)
        }
        statement.bindLong(8, entity.playCount.toLong())
        statement.bindLong(9, entity.totalDurationMs)
        statement.bindLong(10, entity.lastPlayed)
        statement.bindLong(11, entity.firstPlayed)
        statement.bindLong(12, entity.skipCount.toLong())
        statement.bindDouble(13, entity.completionRate.toDouble())
        val _tmp: Int = if (entity.isLiked) 1 else 0
        statement.bindLong(14, _tmp.toLong())
        val _tmpArtistId: String? = entity.artistId
        if (_tmpArtistId == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpArtistId)
        }
        statement.bindText(16, entity.source)
        val _tmpReleaseDate: String? = entity.releaseDate
        if (_tmpReleaseDate == null) {
          statement.bindNull(17)
        } else {
          statement.bindText(17, _tmpReleaseDate)
        }
      }
    }, object : EntityDeleteOrUpdateAdapter<ListeningHistory>() {
      protected override fun createQuery(): String = "UPDATE `listening_history` SET `songId` = ?,`songTitle` = ?,`artist` = ?,`thumbnailUrl` = ?,`album` = ?,`duration` = ?,`localUri` = ?,`playCount` = ?,`totalDurationMs` = ?,`lastPlayed` = ?,`firstPlayed` = ?,`skipCount` = ?,`completionRate` = ?,`isLiked` = ?,`artistId` = ?,`source` = ?,`releaseDate` = ? WHERE `songId` = ?"

      protected override fun bind(statement: SQLiteStatement, entity: ListeningHistory) {
        statement.bindText(1, entity.songId)
        statement.bindText(2, entity.songTitle)
        statement.bindText(3, entity.artist)
        val _tmpThumbnailUrl: String? = entity.thumbnailUrl
        if (_tmpThumbnailUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpThumbnailUrl)
        }
        statement.bindText(5, entity.album)
        statement.bindLong(6, entity.duration)
        val _tmpLocalUri: String? = entity.localUri
        if (_tmpLocalUri == null) {
          statement.bindNull(7)
        } else {
          statement.bindText(7, _tmpLocalUri)
        }
        statement.bindLong(8, entity.playCount.toLong())
        statement.bindLong(9, entity.totalDurationMs)
        statement.bindLong(10, entity.lastPlayed)
        statement.bindLong(11, entity.firstPlayed)
        statement.bindLong(12, entity.skipCount.toLong())
        statement.bindDouble(13, entity.completionRate.toDouble())
        val _tmp: Int = if (entity.isLiked) 1 else 0
        statement.bindLong(14, _tmp.toLong())
        val _tmpArtistId: String? = entity.artistId
        if (_tmpArtistId == null) {
          statement.bindNull(15)
        } else {
          statement.bindText(15, _tmpArtistId)
        }
        statement.bindText(16, entity.source)
        val _tmpReleaseDate: String? = entity.releaseDate
        if (_tmpReleaseDate == null) {
          statement.bindNull(17)
        } else {
          statement.bindText(17, _tmpReleaseDate)
        }
        statement.bindText(18, entity.songId)
      }
    })
  }

  public override suspend fun upsert(history: ListeningHistory): Unit = performSuspending(__db, false, true) { _connection ->
    __upsertAdapterOfListeningHistory.upsert(_connection, history)
  }

  public override suspend fun getHistoryForSong(songId: String): ListeningHistory? {
    val _sql: String = "SELECT * FROM listening_history WHERE songId = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, songId)
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfSongTitle: Int = getColumnIndexOrThrow(_stmt, "songTitle")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfTotalDurationMs: Int = getColumnIndexOrThrow(_stmt, "totalDurationMs")
        val _columnIndexOfLastPlayed: Int = getColumnIndexOrThrow(_stmt, "lastPlayed")
        val _columnIndexOfFirstPlayed: Int = getColumnIndexOrThrow(_stmt, "firstPlayed")
        val _columnIndexOfSkipCount: Int = getColumnIndexOrThrow(_stmt, "skipCount")
        val _columnIndexOfCompletionRate: Int = getColumnIndexOrThrow(_stmt, "completionRate")
        val _columnIndexOfIsLiked: Int = getColumnIndexOrThrow(_stmt, "isLiked")
        val _columnIndexOfArtistId: Int = getColumnIndexOrThrow(_stmt, "artistId")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _result: ListeningHistory?
        if (_stmt.step()) {
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpSongTitle: String
          _tmpSongTitle = _stmt.getText(_columnIndexOfSongTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpTotalDurationMs: Long
          _tmpTotalDurationMs = _stmt.getLong(_columnIndexOfTotalDurationMs)
          val _tmpLastPlayed: Long
          _tmpLastPlayed = _stmt.getLong(_columnIndexOfLastPlayed)
          val _tmpFirstPlayed: Long
          _tmpFirstPlayed = _stmt.getLong(_columnIndexOfFirstPlayed)
          val _tmpSkipCount: Int
          _tmpSkipCount = _stmt.getLong(_columnIndexOfSkipCount).toInt()
          val _tmpCompletionRate: Float
          _tmpCompletionRate = _stmt.getDouble(_columnIndexOfCompletionRate).toFloat()
          val _tmpIsLiked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsLiked).toInt()
          _tmpIsLiked = _tmp != 0
          val _tmpArtistId: String?
          if (_stmt.isNull(_columnIndexOfArtistId)) {
            _tmpArtistId = null
          } else {
            _tmpArtistId = _stmt.getText(_columnIndexOfArtistId)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          _result = ListeningHistory(_tmpSongId,_tmpSongTitle,_tmpArtist,_tmpThumbnailUrl,_tmpAlbum,_tmpDuration,_tmpLocalUri,_tmpPlayCount,_tmpTotalDurationMs,_tmpLastPlayed,_tmpFirstPlayed,_tmpSkipCount,_tmpCompletionRate,_tmpIsLiked,_tmpArtistId,_tmpSource,_tmpReleaseDate)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllHistory(): List<ListeningHistory> {
    val _sql: String = "SELECT * FROM listening_history"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfSongTitle: Int = getColumnIndexOrThrow(_stmt, "songTitle")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfTotalDurationMs: Int = getColumnIndexOrThrow(_stmt, "totalDurationMs")
        val _columnIndexOfLastPlayed: Int = getColumnIndexOrThrow(_stmt, "lastPlayed")
        val _columnIndexOfFirstPlayed: Int = getColumnIndexOrThrow(_stmt, "firstPlayed")
        val _columnIndexOfSkipCount: Int = getColumnIndexOrThrow(_stmt, "skipCount")
        val _columnIndexOfCompletionRate: Int = getColumnIndexOrThrow(_stmt, "completionRate")
        val _columnIndexOfIsLiked: Int = getColumnIndexOrThrow(_stmt, "isLiked")
        val _columnIndexOfArtistId: Int = getColumnIndexOrThrow(_stmt, "artistId")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _result: MutableList<ListeningHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: ListeningHistory
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpSongTitle: String
          _tmpSongTitle = _stmt.getText(_columnIndexOfSongTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpTotalDurationMs: Long
          _tmpTotalDurationMs = _stmt.getLong(_columnIndexOfTotalDurationMs)
          val _tmpLastPlayed: Long
          _tmpLastPlayed = _stmt.getLong(_columnIndexOfLastPlayed)
          val _tmpFirstPlayed: Long
          _tmpFirstPlayed = _stmt.getLong(_columnIndexOfFirstPlayed)
          val _tmpSkipCount: Int
          _tmpSkipCount = _stmt.getLong(_columnIndexOfSkipCount).toInt()
          val _tmpCompletionRate: Float
          _tmpCompletionRate = _stmt.getDouble(_columnIndexOfCompletionRate).toFloat()
          val _tmpIsLiked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsLiked).toInt()
          _tmpIsLiked = _tmp != 0
          val _tmpArtistId: String?
          if (_stmt.isNull(_columnIndexOfArtistId)) {
            _tmpArtistId = null
          } else {
            _tmpArtistId = _stmt.getText(_columnIndexOfArtistId)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          _item = ListeningHistory(_tmpSongId,_tmpSongTitle,_tmpArtist,_tmpThumbnailUrl,_tmpAlbum,_tmpDuration,_tmpLocalUri,_tmpPlayCount,_tmpTotalDurationMs,_tmpLastPlayed,_tmpFirstPlayed,_tmpSkipCount,_tmpCompletionRate,_tmpIsLiked,_tmpArtistId,_tmpSource,_tmpReleaseDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getTopSongs(limit: Int): Flow<List<ListeningHistory>> {
    val _sql: String = "SELECT * FROM listening_history ORDER BY playCount DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("listening_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfSongTitle: Int = getColumnIndexOrThrow(_stmt, "songTitle")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfTotalDurationMs: Int = getColumnIndexOrThrow(_stmt, "totalDurationMs")
        val _columnIndexOfLastPlayed: Int = getColumnIndexOrThrow(_stmt, "lastPlayed")
        val _columnIndexOfFirstPlayed: Int = getColumnIndexOrThrow(_stmt, "firstPlayed")
        val _columnIndexOfSkipCount: Int = getColumnIndexOrThrow(_stmt, "skipCount")
        val _columnIndexOfCompletionRate: Int = getColumnIndexOrThrow(_stmt, "completionRate")
        val _columnIndexOfIsLiked: Int = getColumnIndexOrThrow(_stmt, "isLiked")
        val _columnIndexOfArtistId: Int = getColumnIndexOrThrow(_stmt, "artistId")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _result: MutableList<ListeningHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: ListeningHistory
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpSongTitle: String
          _tmpSongTitle = _stmt.getText(_columnIndexOfSongTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpTotalDurationMs: Long
          _tmpTotalDurationMs = _stmt.getLong(_columnIndexOfTotalDurationMs)
          val _tmpLastPlayed: Long
          _tmpLastPlayed = _stmt.getLong(_columnIndexOfLastPlayed)
          val _tmpFirstPlayed: Long
          _tmpFirstPlayed = _stmt.getLong(_columnIndexOfFirstPlayed)
          val _tmpSkipCount: Int
          _tmpSkipCount = _stmt.getLong(_columnIndexOfSkipCount).toInt()
          val _tmpCompletionRate: Float
          _tmpCompletionRate = _stmt.getDouble(_columnIndexOfCompletionRate).toFloat()
          val _tmpIsLiked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsLiked).toInt()
          _tmpIsLiked = _tmp != 0
          val _tmpArtistId: String?
          if (_stmt.isNull(_columnIndexOfArtistId)) {
            _tmpArtistId = null
          } else {
            _tmpArtistId = _stmt.getText(_columnIndexOfArtistId)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          _item = ListeningHistory(_tmpSongId,_tmpSongTitle,_tmpArtist,_tmpThumbnailUrl,_tmpAlbum,_tmpDuration,_tmpLocalUri,_tmpPlayCount,_tmpTotalDurationMs,_tmpLastPlayed,_tmpFirstPlayed,_tmpSkipCount,_tmpCompletionRate,_tmpIsLiked,_tmpArtistId,_tmpSource,_tmpReleaseDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getRecentlyPlayed(limit: Int): Flow<List<ListeningHistory>> {
    val _sql: String = "SELECT * FROM listening_history ORDER BY lastPlayed DESC LIMIT ?"
    return createFlow(__db, false, arrayOf("listening_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfSongTitle: Int = getColumnIndexOrThrow(_stmt, "songTitle")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfTotalDurationMs: Int = getColumnIndexOrThrow(_stmt, "totalDurationMs")
        val _columnIndexOfLastPlayed: Int = getColumnIndexOrThrow(_stmt, "lastPlayed")
        val _columnIndexOfFirstPlayed: Int = getColumnIndexOrThrow(_stmt, "firstPlayed")
        val _columnIndexOfSkipCount: Int = getColumnIndexOrThrow(_stmt, "skipCount")
        val _columnIndexOfCompletionRate: Int = getColumnIndexOrThrow(_stmt, "completionRate")
        val _columnIndexOfIsLiked: Int = getColumnIndexOrThrow(_stmt, "isLiked")
        val _columnIndexOfArtistId: Int = getColumnIndexOrThrow(_stmt, "artistId")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _result: MutableList<ListeningHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: ListeningHistory
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpSongTitle: String
          _tmpSongTitle = _stmt.getText(_columnIndexOfSongTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpTotalDurationMs: Long
          _tmpTotalDurationMs = _stmt.getLong(_columnIndexOfTotalDurationMs)
          val _tmpLastPlayed: Long
          _tmpLastPlayed = _stmt.getLong(_columnIndexOfLastPlayed)
          val _tmpFirstPlayed: Long
          _tmpFirstPlayed = _stmt.getLong(_columnIndexOfFirstPlayed)
          val _tmpSkipCount: Int
          _tmpSkipCount = _stmt.getLong(_columnIndexOfSkipCount).toInt()
          val _tmpCompletionRate: Float
          _tmpCompletionRate = _stmt.getDouble(_columnIndexOfCompletionRate).toFloat()
          val _tmpIsLiked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsLiked).toInt()
          _tmpIsLiked = _tmp != 0
          val _tmpArtistId: String?
          if (_stmt.isNull(_columnIndexOfArtistId)) {
            _tmpArtistId = null
          } else {
            _tmpArtistId = _stmt.getText(_columnIndexOfArtistId)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          _item = ListeningHistory(_tmpSongId,_tmpSongTitle,_tmpArtist,_tmpThumbnailUrl,_tmpAlbum,_tmpDuration,_tmpLocalUri,_tmpPlayCount,_tmpTotalDurationMs,_tmpLastPlayed,_tmpFirstPlayed,_tmpSkipCount,_tmpCompletionRate,_tmpIsLiked,_tmpArtistId,_tmpSource,_tmpReleaseDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getHistoryAfter(timestamp: Long): Flow<List<ListeningHistory>> {
    val _sql: String = "SELECT * FROM listening_history WHERE lastPlayed >= ? ORDER BY lastPlayed DESC"
    return createFlow(__db, false, arrayOf("listening_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, timestamp)
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfSongTitle: Int = getColumnIndexOrThrow(_stmt, "songTitle")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfTotalDurationMs: Int = getColumnIndexOrThrow(_stmt, "totalDurationMs")
        val _columnIndexOfLastPlayed: Int = getColumnIndexOrThrow(_stmt, "lastPlayed")
        val _columnIndexOfFirstPlayed: Int = getColumnIndexOrThrow(_stmt, "firstPlayed")
        val _columnIndexOfSkipCount: Int = getColumnIndexOrThrow(_stmt, "skipCount")
        val _columnIndexOfCompletionRate: Int = getColumnIndexOrThrow(_stmt, "completionRate")
        val _columnIndexOfIsLiked: Int = getColumnIndexOrThrow(_stmt, "isLiked")
        val _columnIndexOfArtistId: Int = getColumnIndexOrThrow(_stmt, "artistId")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _result: MutableList<ListeningHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: ListeningHistory
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpSongTitle: String
          _tmpSongTitle = _stmt.getText(_columnIndexOfSongTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpTotalDurationMs: Long
          _tmpTotalDurationMs = _stmt.getLong(_columnIndexOfTotalDurationMs)
          val _tmpLastPlayed: Long
          _tmpLastPlayed = _stmt.getLong(_columnIndexOfLastPlayed)
          val _tmpFirstPlayed: Long
          _tmpFirstPlayed = _stmt.getLong(_columnIndexOfFirstPlayed)
          val _tmpSkipCount: Int
          _tmpSkipCount = _stmt.getLong(_columnIndexOfSkipCount).toInt()
          val _tmpCompletionRate: Float
          _tmpCompletionRate = _stmt.getDouble(_columnIndexOfCompletionRate).toFloat()
          val _tmpIsLiked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsLiked).toInt()
          _tmpIsLiked = _tmp != 0
          val _tmpArtistId: String?
          if (_stmt.isNull(_columnIndexOfArtistId)) {
            _tmpArtistId = null
          } else {
            _tmpArtistId = _stmt.getText(_columnIndexOfArtistId)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          _item = ListeningHistory(_tmpSongId,_tmpSongTitle,_tmpArtist,_tmpThumbnailUrl,_tmpAlbum,_tmpDuration,_tmpLocalUri,_tmpPlayCount,_tmpTotalDurationMs,_tmpLastPlayed,_tmpFirstPlayed,_tmpSkipCount,_tmpCompletionRate,_tmpIsLiked,_tmpArtistId,_tmpSource,_tmpReleaseDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getRecentTopSongs(timestamp: Long): Flow<List<ListeningHistory>> {
    val _sql: String = "SELECT * FROM listening_history WHERE lastPlayed > ? ORDER BY playCount DESC"
    return createFlow(__db, false, arrayOf("listening_history")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, timestamp)
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfSongTitle: Int = getColumnIndexOrThrow(_stmt, "songTitle")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfTotalDurationMs: Int = getColumnIndexOrThrow(_stmt, "totalDurationMs")
        val _columnIndexOfLastPlayed: Int = getColumnIndexOrThrow(_stmt, "lastPlayed")
        val _columnIndexOfFirstPlayed: Int = getColumnIndexOrThrow(_stmt, "firstPlayed")
        val _columnIndexOfSkipCount: Int = getColumnIndexOrThrow(_stmt, "skipCount")
        val _columnIndexOfCompletionRate: Int = getColumnIndexOrThrow(_stmt, "completionRate")
        val _columnIndexOfIsLiked: Int = getColumnIndexOrThrow(_stmt, "isLiked")
        val _columnIndexOfArtistId: Int = getColumnIndexOrThrow(_stmt, "artistId")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _result: MutableList<ListeningHistory> = mutableListOf()
        while (_stmt.step()) {
          val _item: ListeningHistory
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpSongTitle: String
          _tmpSongTitle = _stmt.getText(_columnIndexOfSongTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpTotalDurationMs: Long
          _tmpTotalDurationMs = _stmt.getLong(_columnIndexOfTotalDurationMs)
          val _tmpLastPlayed: Long
          _tmpLastPlayed = _stmt.getLong(_columnIndexOfLastPlayed)
          val _tmpFirstPlayed: Long
          _tmpFirstPlayed = _stmt.getLong(_columnIndexOfFirstPlayed)
          val _tmpSkipCount: Int
          _tmpSkipCount = _stmt.getLong(_columnIndexOfSkipCount).toInt()
          val _tmpCompletionRate: Float
          _tmpCompletionRate = _stmt.getDouble(_columnIndexOfCompletionRate).toFloat()
          val _tmpIsLiked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsLiked).toInt()
          _tmpIsLiked = _tmp != 0
          val _tmpArtistId: String?
          if (_stmt.isNull(_columnIndexOfArtistId)) {
            _tmpArtistId = null
          } else {
            _tmpArtistId = _stmt.getText(_columnIndexOfArtistId)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          _item = ListeningHistory(_tmpSongId,_tmpSongTitle,_tmpArtist,_tmpThumbnailUrl,_tmpAlbum,_tmpDuration,_tmpLocalUri,_tmpPlayCount,_tmpTotalDurationMs,_tmpLastPlayed,_tmpFirstPlayed,_tmpSkipCount,_tmpCompletionRate,_tmpIsLiked,_tmpArtistId,_tmpSource,_tmpReleaseDate)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTotalSongsCount(): Int {
    val _sql: String = "SELECT COUNT(*) FROM listening_history"
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

  public override suspend fun getTotalListeningTime(): Long? {
    val _sql: String = "SELECT SUM(totalDurationMs) FROM listening_history"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Long?
        if (_stmt.step()) {
          val _tmp: Long?
          if (_stmt.isNull(0)) {
            _tmp = null
          } else {
            _tmp = _stmt.getLong(0)
          }
          _result = _tmp
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getTopArtists(limit: Int): List<ArtistStats> {
    val _sql: String = """
        |
        |        SELECT artist, SUM(playCount) as totalPlays 
        |        FROM listening_history 
        |        GROUP BY artist 
        |        ORDER BY totalPlays DESC 
        |        LIMIT ?
        |    
        """.trimMargin()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfArtist: Int = 0
        val _columnIndexOfTotalPlays: Int = 1
        val _result: MutableList<ArtistStats> = mutableListOf()
        while (_stmt.step()) {
          val _item: ArtistStats
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpTotalPlays: Int
          _tmpTotalPlays = _stmt.getLong(_columnIndexOfTotalPlays).toInt()
          _item = ArtistStats(_tmpArtist,_tmpTotalPlays)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getFirstEverTrack(): ListeningHistory? {
    val _sql: String = "SELECT * FROM listening_history ORDER BY firstPlayed ASC LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfSongTitle: Int = getColumnIndexOrThrow(_stmt, "songTitle")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfPlayCount: Int = getColumnIndexOrThrow(_stmt, "playCount")
        val _columnIndexOfTotalDurationMs: Int = getColumnIndexOrThrow(_stmt, "totalDurationMs")
        val _columnIndexOfLastPlayed: Int = getColumnIndexOrThrow(_stmt, "lastPlayed")
        val _columnIndexOfFirstPlayed: Int = getColumnIndexOrThrow(_stmt, "firstPlayed")
        val _columnIndexOfSkipCount: Int = getColumnIndexOrThrow(_stmt, "skipCount")
        val _columnIndexOfCompletionRate: Int = getColumnIndexOrThrow(_stmt, "completionRate")
        val _columnIndexOfIsLiked: Int = getColumnIndexOrThrow(_stmt, "isLiked")
        val _columnIndexOfArtistId: Int = getColumnIndexOrThrow(_stmt, "artistId")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _result: ListeningHistory?
        if (_stmt.step()) {
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpSongTitle: String
          _tmpSongTitle = _stmt.getText(_columnIndexOfSongTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpAlbum: String
          _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpPlayCount: Int
          _tmpPlayCount = _stmt.getLong(_columnIndexOfPlayCount).toInt()
          val _tmpTotalDurationMs: Long
          _tmpTotalDurationMs = _stmt.getLong(_columnIndexOfTotalDurationMs)
          val _tmpLastPlayed: Long
          _tmpLastPlayed = _stmt.getLong(_columnIndexOfLastPlayed)
          val _tmpFirstPlayed: Long
          _tmpFirstPlayed = _stmt.getLong(_columnIndexOfFirstPlayed)
          val _tmpSkipCount: Int
          _tmpSkipCount = _stmt.getLong(_columnIndexOfSkipCount).toInt()
          val _tmpCompletionRate: Float
          _tmpCompletionRate = _stmt.getDouble(_columnIndexOfCompletionRate).toFloat()
          val _tmpIsLiked: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfIsLiked).toInt()
          _tmpIsLiked = _tmp != 0
          val _tmpArtistId: String?
          if (_stmt.isNull(_columnIndexOfArtistId)) {
            _tmpArtistId = null
          } else {
            _tmpArtistId = _stmt.getText(_columnIndexOfArtistId)
          }
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          _result = ListeningHistory(_tmpSongId,_tmpSongTitle,_tmpArtist,_tmpThumbnailUrl,_tmpAlbum,_tmpDuration,_tmpLocalUri,_tmpPlayCount,_tmpTotalDurationMs,_tmpLastPlayed,_tmpFirstPlayed,_tmpSkipCount,_tmpCompletionRate,_tmpIsLiked,_tmpArtistId,_tmpSource,_tmpReleaseDate)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM listening_history"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSong(songId: String) {
    val _sql: String = "DELETE FROM listening_history WHERE songId = ?"
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

  public override suspend fun deleteSongs(songIds: List<String>) {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("DELETE FROM listening_history WHERE songId IN (")
    val _inputSize: Int = songIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(")")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in songIds) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteOldEntries(timestamp: Long) {
    val _sql: String = "DELETE FROM listening_history WHERE lastPlayed < ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, timestamp)
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
