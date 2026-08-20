package com.sonza.app.core.`data`.local.dao

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import com.sonza.app.core.`data`.local.entity.LibraryEntity
import com.sonza.app.core.`data`.local.entity.LibraryItemWithCount
import com.sonza.app.core.`data`.local.entity.PlaylistSongEntity
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
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class LibraryDao_Impl(
  __db: RoomDatabase,
) : LibraryDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfLibraryEntity: EntityInsertAdapter<LibraryEntity>

  private val __insertAdapterOfPlaylistSongEntity: EntityInsertAdapter<PlaylistSongEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfLibraryEntity = object : EntityInsertAdapter<LibraryEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `library_items` (`id`,`title`,`subtitle`,`thumbnailUrl`,`type`,`timestamp`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: LibraryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        val _tmpSubtitle: String? = entity.subtitle
        if (_tmpSubtitle == null) {
          statement.bindNull(3)
        } else {
          statement.bindText(3, _tmpSubtitle)
        }
        val _tmpThumbnailUrl: String? = entity.thumbnailUrl
        if (_tmpThumbnailUrl == null) {
          statement.bindNull(4)
        } else {
          statement.bindText(4, _tmpThumbnailUrl)
        }
        statement.bindText(5, entity.type)
        statement.bindLong(6, entity.timestamp)
      }
    }
    this.__insertAdapterOfPlaylistSongEntity = object : EntityInsertAdapter<PlaylistSongEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `playlist_songs` (`playlistId`,`songId`,`title`,`artist`,`album`,`thumbnailUrl`,`duration`,`source`,`localUri`,`releaseDate`,`addedAt`,`order`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlaylistSongEntity) {
        statement.bindText(1, entity.playlistId)
        statement.bindText(2, entity.songId)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.artist)
        val _tmpAlbum: String? = entity.album
        if (_tmpAlbum == null) {
          statement.bindNull(5)
        } else {
          statement.bindText(5, _tmpAlbum)
        }
        val _tmpThumbnailUrl: String? = entity.thumbnailUrl
        if (_tmpThumbnailUrl == null) {
          statement.bindNull(6)
        } else {
          statement.bindText(6, _tmpThumbnailUrl)
        }
        statement.bindLong(7, entity.duration)
        statement.bindText(8, entity.source)
        val _tmpLocalUri: String? = entity.localUri
        if (_tmpLocalUri == null) {
          statement.bindNull(9)
        } else {
          statement.bindText(9, _tmpLocalUri)
        }
        val _tmpReleaseDate: String? = entity.releaseDate
        if (_tmpReleaseDate == null) {
          statement.bindNull(10)
        } else {
          statement.bindText(10, _tmpReleaseDate)
        }
        statement.bindLong(11, entity.addedAt)
        statement.bindLong(12, entity.order.toLong())
      }
    }
  }

  public override suspend fun insertItem(item: LibraryEntity): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLibraryEntity.insert(_connection, item)
  }

  public override suspend fun insertItems(items: List<LibraryEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfLibraryEntity.insert(_connection, items)
  }

  public override suspend fun insertPlaylistSongs(songs: List<PlaylistSongEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfPlaylistSongEntity.insert(_connection, songs)
  }

  public override suspend fun replacePlaylistSongs(playlistId: String, songs: List<PlaylistSongEntity>): Unit = performInTransactionSuspending(__db) {
    super@LibraryDao_Impl.replacePlaylistSongs(playlistId, songs)
  }

  public override suspend fun getItem(id: String): LibraryEntity? {
    val _sql: String = "SELECT * FROM library_items WHERE id = ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSubtitle: Int = getColumnIndexOrThrow(_stmt, "subtitle")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: LibraryEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSubtitle: String?
          if (_stmt.isNull(_columnIndexOfSubtitle)) {
            _tmpSubtitle = null
          } else {
            _tmpSubtitle = _stmt.getText(_columnIndexOfSubtitle)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _result = LibraryEntity(_tmpId,_tmpTitle,_tmpSubtitle,_tmpThumbnailUrl,_tmpType,_tmpTimestamp)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun isItemSavedFlow(id: String): Flow<Boolean> {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM library_items WHERE id = ?)"
    return createFlow(__db, false, arrayOf("library_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getItemsByType(type: String): Flow<List<LibraryEntity>> {
    val _sql: String = "SELECT * FROM library_items WHERE type = ? ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("library_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSubtitle: Int = getColumnIndexOrThrow(_stmt, "subtitle")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<LibraryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSubtitle: String?
          if (_stmt.isNull(_columnIndexOfSubtitle)) {
            _tmpSubtitle = null
          } else {
            _tmpSubtitle = _stmt.getText(_columnIndexOfSubtitle)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = LibraryEntity(_tmpId,_tmpTitle,_tmpSubtitle,_tmpThumbnailUrl,_tmpType,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getItemsWithTypeAndCount(type: String): Flow<List<LibraryItemWithCount>> {
    val _sql: String = """
        |
        |        SELECT *, (SELECT COUNT(*) FROM playlist_songs WHERE playlistId = library_items.id) as songCount 
        |        FROM library_items 
        |        WHERE type = ? 
        |        ORDER BY timestamp DESC
        |    
        """.trimMargin()
    return createFlow(__db, false, arrayOf("playlist_songs", "library_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, type)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSubtitle: Int = getColumnIndexOrThrow(_stmt, "subtitle")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfSongCount: Int = getColumnIndexOrThrow(_stmt, "songCount")
        val _result: MutableList<LibraryItemWithCount> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryItemWithCount
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSubtitle: String?
          if (_stmt.isNull(_columnIndexOfSubtitle)) {
            _tmpSubtitle = null
          } else {
            _tmpSubtitle = _stmt.getText(_columnIndexOfSubtitle)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpSongCount: Int
          _tmpSongCount = _stmt.getLong(_columnIndexOfSongCount).toInt()
          _item = LibraryItemWithCount(_tmpId,_tmpTitle,_tmpSubtitle,_tmpThumbnailUrl,_tmpType,_tmpTimestamp,_tmpSongCount)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getAllItems(): Flow<List<LibraryEntity>> {
    val _sql: String = "SELECT * FROM library_items ORDER BY timestamp DESC"
    return createFlow(__db, false, arrayOf("library_items")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSubtitle: Int = getColumnIndexOrThrow(_stmt, "subtitle")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<LibraryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSubtitle: String?
          if (_stmt.isNull(_columnIndexOfSubtitle)) {
            _tmpSubtitle = null
          } else {
            _tmpSubtitle = _stmt.getText(_columnIndexOfSubtitle)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = LibraryEntity(_tmpId,_tmpTitle,_tmpSubtitle,_tmpThumbnailUrl,_tmpType,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllItemsSync(): List<LibraryEntity> {
    val _sql: String = "SELECT * FROM library_items"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSubtitle: Int = getColumnIndexOrThrow(_stmt, "subtitle")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _result: MutableList<LibraryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: LibraryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSubtitle: String?
          if (_stmt.isNull(_columnIndexOfSubtitle)) {
            _tmpSubtitle = null
          } else {
            _tmpSubtitle = _stmt.getText(_columnIndexOfSubtitle)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          _item = LibraryEntity(_tmpId,_tmpTitle,_tmpSubtitle,_tmpThumbnailUrl,_tmpType,_tmpTimestamp)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getAllPlaylistSongs(): List<PlaylistSongEntity> {
    val _sql: String = "SELECT * FROM playlist_songs"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfPlaylistId: Int = getColumnIndexOrThrow(_stmt, "playlistId")
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _columnIndexOfOrder: Int = getColumnIndexOrThrow(_stmt, "order")
        val _result: MutableList<PlaylistSongEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaylistSongEntity
          val _tmpPlaylistId: String
          _tmpPlaylistId = _stmt.getText(_columnIndexOfPlaylistId)
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String?
          if (_stmt.isNull(_columnIndexOfAlbum)) {
            _tmpAlbum = null
          } else {
            _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          val _tmpAddedAt: Long
          _tmpAddedAt = _stmt.getLong(_columnIndexOfAddedAt)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          _item = PlaylistSongEntity(_tmpPlaylistId,_tmpSongId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpThumbnailUrl,_tmpDuration,_tmpSource,_tmpLocalUri,_tmpReleaseDate,_tmpAddedAt,_tmpOrder)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun getPlaylistSongs(playlistId: String): List<PlaylistSongEntity> {
    val _sql: String = "SELECT * FROM playlist_songs WHERE playlistId = ? ORDER BY `order` ASC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, playlistId)
        val _columnIndexOfPlaylistId: Int = getColumnIndexOrThrow(_stmt, "playlistId")
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _columnIndexOfOrder: Int = getColumnIndexOrThrow(_stmt, "order")
        val _result: MutableList<PlaylistSongEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaylistSongEntity
          val _tmpPlaylistId: String
          _tmpPlaylistId = _stmt.getText(_columnIndexOfPlaylistId)
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String?
          if (_stmt.isNull(_columnIndexOfAlbum)) {
            _tmpAlbum = null
          } else {
            _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          val _tmpAddedAt: Long
          _tmpAddedAt = _stmt.getLong(_columnIndexOfAddedAt)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          _item = PlaylistSongEntity(_tmpPlaylistId,_tmpSongId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpThumbnailUrl,_tmpDuration,_tmpSource,_tmpLocalUri,_tmpReleaseDate,_tmpAddedAt,_tmpOrder)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPlaylistSongsFlow(playlistId: String): Flow<List<PlaylistSongEntity>> {
    val _sql: String = "SELECT * FROM playlist_songs WHERE playlistId = ? ORDER BY `order` ASC"
    return createFlow(__db, false, arrayOf("playlist_songs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, playlistId)
        val _columnIndexOfPlaylistId: Int = getColumnIndexOrThrow(_stmt, "playlistId")
        val _columnIndexOfSongId: Int = getColumnIndexOrThrow(_stmt, "songId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfArtist: Int = getColumnIndexOrThrow(_stmt, "artist")
        val _columnIndexOfAlbum: Int = getColumnIndexOrThrow(_stmt, "album")
        val _columnIndexOfThumbnailUrl: Int = getColumnIndexOrThrow(_stmt, "thumbnailUrl")
        val _columnIndexOfDuration: Int = getColumnIndexOrThrow(_stmt, "duration")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfLocalUri: Int = getColumnIndexOrThrow(_stmt, "localUri")
        val _columnIndexOfReleaseDate: Int = getColumnIndexOrThrow(_stmt, "releaseDate")
        val _columnIndexOfAddedAt: Int = getColumnIndexOrThrow(_stmt, "addedAt")
        val _columnIndexOfOrder: Int = getColumnIndexOrThrow(_stmt, "order")
        val _result: MutableList<PlaylistSongEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlaylistSongEntity
          val _tmpPlaylistId: String
          _tmpPlaylistId = _stmt.getText(_columnIndexOfPlaylistId)
          val _tmpSongId: String
          _tmpSongId = _stmt.getText(_columnIndexOfSongId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpArtist: String
          _tmpArtist = _stmt.getText(_columnIndexOfArtist)
          val _tmpAlbum: String?
          if (_stmt.isNull(_columnIndexOfAlbum)) {
            _tmpAlbum = null
          } else {
            _tmpAlbum = _stmt.getText(_columnIndexOfAlbum)
          }
          val _tmpThumbnailUrl: String?
          if (_stmt.isNull(_columnIndexOfThumbnailUrl)) {
            _tmpThumbnailUrl = null
          } else {
            _tmpThumbnailUrl = _stmt.getText(_columnIndexOfThumbnailUrl)
          }
          val _tmpDuration: Long
          _tmpDuration = _stmt.getLong(_columnIndexOfDuration)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpLocalUri: String?
          if (_stmt.isNull(_columnIndexOfLocalUri)) {
            _tmpLocalUri = null
          } else {
            _tmpLocalUri = _stmt.getText(_columnIndexOfLocalUri)
          }
          val _tmpReleaseDate: String?
          if (_stmt.isNull(_columnIndexOfReleaseDate)) {
            _tmpReleaseDate = null
          } else {
            _tmpReleaseDate = _stmt.getText(_columnIndexOfReleaseDate)
          }
          val _tmpAddedAt: Long
          _tmpAddedAt = _stmt.getLong(_columnIndexOfAddedAt)
          val _tmpOrder: Int
          _tmpOrder = _stmt.getLong(_columnIndexOfOrder).toInt()
          _item = PlaylistSongEntity(_tmpPlaylistId,_tmpSongId,_tmpTitle,_tmpArtist,_tmpAlbum,_tmpThumbnailUrl,_tmpDuration,_tmpSource,_tmpLocalUri,_tmpReleaseDate,_tmpAddedAt,_tmpOrder)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getPlaylistSongCountFlow(playlistId: String): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM playlist_songs WHERE playlistId = ?"
    return createFlow(__db, false, arrayOf("playlist_songs")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, playlistId)
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

  public override suspend fun isSongInPlaylist(playlistId: String, songId: String): Boolean {
    val _sql: String = "SELECT EXISTS(SELECT 1 FROM playlist_songs WHERE playlistId = ? AND songId = ?)"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, playlistId)
        _argIndex = 2
        _stmt.bindText(_argIndex, songId)
        val _result: Boolean
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp != 0
        } else {
          _result = false
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteItem(id: String) {
    val _sql: String = "DELETE FROM library_items WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAll() {
    val _sql: String = "DELETE FROM library_items"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePlaylistSongs(playlistId: String) {
    val _sql: String = "DELETE FROM playlist_songs WHERE playlistId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, playlistId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearAllPlaylistSongs() {
    val _sql: String = "DELETE FROM playlist_songs"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteSongFromPlaylist(playlistId: String, songId: String) {
    val _sql: String = "DELETE FROM playlist_songs WHERE playlistId = ? AND songId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, playlistId)
        _argIndex = 2
        _stmt.bindText(_argIndex, songId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePlaylistThumbnail(id: String, thumbnailUrl: String?) {
    val _sql: String = "UPDATE library_items SET thumbnailUrl = ? WHERE id = ? AND type = 'PLAYLIST'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        if (thumbnailUrl == null) {
          _stmt.bindNull(_argIndex)
        } else {
          _stmt.bindText(_argIndex, thumbnailUrl)
        }
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updatePlaylistName(id: String, name: String) {
    val _sql: String = "UPDATE library_items SET title = ? WHERE id = ? AND type = 'PLAYLIST'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, name)
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
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
