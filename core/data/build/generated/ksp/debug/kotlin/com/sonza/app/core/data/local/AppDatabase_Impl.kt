package com.sonza.app.core.`data`.local

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import com.sonza.app.core.`data`.local.dao.DislikedItemDao
import com.sonza.app.core.`data`.local.dao.DislikedItemDao_Impl
import com.sonza.app.core.`data`.local.dao.LibraryDao
import com.sonza.app.core.`data`.local.dao.LibraryDao_Impl
import com.sonza.app.core.`data`.local.dao.ListeningHistoryDao
import com.sonza.app.core.`data`.local.dao.ListeningHistoryDao_Impl
import com.sonza.app.core.`data`.local.dao.LyricsDao
import com.sonza.app.core.`data`.local.dao.LyricsDao_Impl
import com.sonza.app.core.`data`.local.dao.SongGenreDao
import com.sonza.app.core.`data`.local.dao.SongGenreDao_Impl
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AppDatabase_Impl : AppDatabase() {
  private val _listeningHistoryDao: Lazy<ListeningHistoryDao> = lazy {
    ListeningHistoryDao_Impl(this)
  }

  private val _libraryDao: Lazy<LibraryDao> = lazy {
    LibraryDao_Impl(this)
  }

  private val _dislikedItemDao: Lazy<DislikedItemDao> = lazy {
    DislikedItemDao_Impl(this)
  }

  private val _songGenreDao: Lazy<SongGenreDao> = lazy {
    SongGenreDao_Impl(this)
  }

  private val _lyricsDao: Lazy<LyricsDao> = lazy {
    LyricsDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(12, "902c1657cb5e4e42b33dd59cdea57194", "a7a20fd8fb647206e70b2affe54abfa1") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `listening_history` (`songId` TEXT NOT NULL, `songTitle` TEXT NOT NULL, `artist` TEXT NOT NULL, `thumbnailUrl` TEXT, `album` TEXT NOT NULL, `duration` INTEGER NOT NULL, `localUri` TEXT, `playCount` INTEGER NOT NULL, `totalDurationMs` INTEGER NOT NULL, `lastPlayed` INTEGER NOT NULL, `firstPlayed` INTEGER NOT NULL, `skipCount` INTEGER NOT NULL, `completionRate` REAL NOT NULL, `isLiked` INTEGER NOT NULL, `artistId` TEXT, `source` TEXT NOT NULL, `releaseDate` TEXT, PRIMARY KEY(`songId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `library_items` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `subtitle` TEXT, `thumbnailUrl` TEXT, `type` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `playlist_songs` (`playlistId` TEXT NOT NULL, `songId` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `album` TEXT, `thumbnailUrl` TEXT, `duration` INTEGER NOT NULL, `source` TEXT NOT NULL, `localUri` TEXT, `releaseDate` TEXT, `addedAt` INTEGER NOT NULL, `order` INTEGER NOT NULL, PRIMARY KEY(`playlistId`, `songId`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_playlist_songs_playlistId` ON `playlist_songs` (`playlistId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `disliked_songs` (`songId` TEXT NOT NULL, `title` TEXT NOT NULL, `artist` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`songId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `disliked_artists` (`artistName` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`artistName`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `song_genres` (`songId` TEXT NOT NULL, `genreVector` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`songId`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `lyrics_cache` (`songId` TEXT NOT NULL, `providerName` TEXT NOT NULL, `lrcContent` TEXT NOT NULL, `isSynced` INTEGER NOT NULL, `sourceCredit` TEXT, `timestamp` INTEGER NOT NULL, PRIMARY KEY(`songId`, `providerName`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '902c1657cb5e4e42b33dd59cdea57194')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `listening_history`")
        connection.execSQL("DROP TABLE IF EXISTS `library_items`")
        connection.execSQL("DROP TABLE IF EXISTS `playlist_songs`")
        connection.execSQL("DROP TABLE IF EXISTS `disliked_songs`")
        connection.execSQL("DROP TABLE IF EXISTS `disliked_artists`")
        connection.execSQL("DROP TABLE IF EXISTS `song_genres`")
        connection.execSQL("DROP TABLE IF EXISTS `lyrics_cache`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsListeningHistory: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsListeningHistory.put("songId", TableInfo.Column("songId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("songTitle", TableInfo.Column("songTitle", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("artist", TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("thumbnailUrl", TableInfo.Column("thumbnailUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("album", TableInfo.Column("album", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("duration", TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("localUri", TableInfo.Column("localUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("playCount", TableInfo.Column("playCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("totalDurationMs", TableInfo.Column("totalDurationMs", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("lastPlayed", TableInfo.Column("lastPlayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("firstPlayed", TableInfo.Column("firstPlayed", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("skipCount", TableInfo.Column("skipCount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("completionRate", TableInfo.Column("completionRate", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("isLiked", TableInfo.Column("isLiked", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("artistId", TableInfo.Column("artistId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("source", TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsListeningHistory.put("releaseDate", TableInfo.Column("releaseDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysListeningHistory: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesListeningHistory: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoListeningHistory: TableInfo = TableInfo("listening_history", _columnsListeningHistory, _foreignKeysListeningHistory, _indicesListeningHistory)
        val _existingListeningHistory: TableInfo = read(connection, "listening_history")
        if (!_infoListeningHistory.equals(_existingListeningHistory)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |listening_history(com.sonza.app.core.data.local.entity.ListeningHistory).
              | Expected:
              |""".trimMargin() + _infoListeningHistory + """
              |
              | Found:
              |""".trimMargin() + _existingListeningHistory)
        }
        val _columnsLibraryItems: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLibraryItems.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("subtitle", TableInfo.Column("subtitle", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("thumbnailUrl", TableInfo.Column("thumbnailUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("type", TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLibraryItems.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLibraryItems: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLibraryItems: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoLibraryItems: TableInfo = TableInfo("library_items", _columnsLibraryItems, _foreignKeysLibraryItems, _indicesLibraryItems)
        val _existingLibraryItems: TableInfo = read(connection, "library_items")
        if (!_infoLibraryItems.equals(_existingLibraryItems)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |library_items(com.sonza.app.core.data.local.entity.LibraryEntity).
              | Expected:
              |""".trimMargin() + _infoLibraryItems + """
              |
              | Found:
              |""".trimMargin() + _existingLibraryItems)
        }
        val _columnsPlaylistSongs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlaylistSongs.put("playlistId", TableInfo.Column("playlistId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("songId", TableInfo.Column("songId", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("artist", TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("album", TableInfo.Column("album", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("thumbnailUrl", TableInfo.Column("thumbnailUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("duration", TableInfo.Column("duration", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("source", TableInfo.Column("source", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("localUri", TableInfo.Column("localUri", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("releaseDate", TableInfo.Column("releaseDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("addedAt", TableInfo.Column("addedAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsPlaylistSongs.put("order", TableInfo.Column("order", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlaylistSongs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlaylistSongs: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesPlaylistSongs.add(TableInfo.Index("index_playlist_songs_playlistId", false, listOf("playlistId"), listOf("ASC")))
        val _infoPlaylistSongs: TableInfo = TableInfo("playlist_songs", _columnsPlaylistSongs, _foreignKeysPlaylistSongs, _indicesPlaylistSongs)
        val _existingPlaylistSongs: TableInfo = read(connection, "playlist_songs")
        if (!_infoPlaylistSongs.equals(_existingPlaylistSongs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |playlist_songs(com.sonza.app.core.data.local.entity.PlaylistSongEntity).
              | Expected:
              |""".trimMargin() + _infoPlaylistSongs + """
              |
              | Found:
              |""".trimMargin() + _existingPlaylistSongs)
        }
        val _columnsDislikedSongs: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDislikedSongs.put("songId", TableInfo.Column("songId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDislikedSongs.put("title", TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDislikedSongs.put("artist", TableInfo.Column("artist", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDislikedSongs.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDislikedSongs: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDislikedSongs: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDislikedSongs: TableInfo = TableInfo("disliked_songs", _columnsDislikedSongs, _foreignKeysDislikedSongs, _indicesDislikedSongs)
        val _existingDislikedSongs: TableInfo = read(connection, "disliked_songs")
        if (!_infoDislikedSongs.equals(_existingDislikedSongs)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |disliked_songs(com.sonza.app.core.data.local.entity.DislikedSong).
              | Expected:
              |""".trimMargin() + _infoDislikedSongs + """
              |
              | Found:
              |""".trimMargin() + _existingDislikedSongs)
        }
        val _columnsDislikedArtists: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDislikedArtists.put("artistName", TableInfo.Column("artistName", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDislikedArtists.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDislikedArtists: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDislikedArtists: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDislikedArtists: TableInfo = TableInfo("disliked_artists", _columnsDislikedArtists, _foreignKeysDislikedArtists, _indicesDislikedArtists)
        val _existingDislikedArtists: TableInfo = read(connection, "disliked_artists")
        if (!_infoDislikedArtists.equals(_existingDislikedArtists)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |disliked_artists(com.sonza.app.core.data.local.entity.DislikedArtist).
              | Expected:
              |""".trimMargin() + _infoDislikedArtists + """
              |
              | Found:
              |""".trimMargin() + _existingDislikedArtists)
        }
        val _columnsSongGenres: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSongGenres.put("songId", TableInfo.Column("songId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSongGenres.put("genreVector", TableInfo.Column("genreVector", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSongGenres.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSongGenres: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSongGenres: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSongGenres: TableInfo = TableInfo("song_genres", _columnsSongGenres, _foreignKeysSongGenres, _indicesSongGenres)
        val _existingSongGenres: TableInfo = read(connection, "song_genres")
        if (!_infoSongGenres.equals(_existingSongGenres)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |song_genres(com.sonza.app.core.data.local.entity.SongGenre).
              | Expected:
              |""".trimMargin() + _infoSongGenres + """
              |
              | Found:
              |""".trimMargin() + _existingSongGenres)
        }
        val _columnsLyricsCache: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsLyricsCache.put("songId", TableInfo.Column("songId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLyricsCache.put("providerName", TableInfo.Column("providerName", "TEXT", true, 2, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLyricsCache.put("lrcContent", TableInfo.Column("lrcContent", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLyricsCache.put("isSynced", TableInfo.Column("isSynced", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLyricsCache.put("sourceCredit", TableInfo.Column("sourceCredit", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsLyricsCache.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysLyricsCache: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesLyricsCache: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoLyricsCache: TableInfo = TableInfo("lyrics_cache", _columnsLyricsCache, _foreignKeysLyricsCache, _indicesLyricsCache)
        val _existingLyricsCache: TableInfo = read(connection, "lyrics_cache")
        if (!_infoLyricsCache.equals(_existingLyricsCache)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |lyrics_cache(com.sonza.app.core.data.local.entity.LyricsEntity).
              | Expected:
              |""".trimMargin() + _infoLyricsCache + """
              |
              | Found:
              |""".trimMargin() + _existingLyricsCache)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "listening_history", "library_items", "playlist_songs", "disliked_songs", "disliked_artists", "song_genres", "lyrics_cache")
  }

  public override fun clearAllTables() {
    super.performClear(false, "listening_history", "library_items", "playlist_songs", "disliked_songs", "disliked_artists", "song_genres", "lyrics_cache")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(ListeningHistoryDao::class, ListeningHistoryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(LibraryDao::class, LibraryDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(DislikedItemDao::class, DislikedItemDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(SongGenreDao::class, SongGenreDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(LyricsDao::class, LyricsDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun listeningHistoryDao(): ListeningHistoryDao = _listeningHistoryDao.value

  public override fun libraryDao(): LibraryDao = _libraryDao.value

  public override fun dislikedItemDao(): DislikedItemDao = _dislikedItemDao.value

  public override fun songGenreDao(): SongGenreDao = _songGenreDao.value

  public override fun lyricsDao(): LyricsDao = _lyricsDao.value
}
