package com.sonza.app.core.data.di

import android.content.Context
import androidx.room.Room
import com.sonza.app.core.data.local.AppDatabase
import com.sonza.app.core.data.local.dao.LibraryDao
import com.sonza.app.core.data.local.dao.ListeningHistoryDao
import com.sonza.app.core.data.local.dao.DislikedItemDao
import com.sonza.app.core.data.local.dao.LyricsDao
import com.sonza.app.core.data.local.dao.SongGenreDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "sonza_database"
        )
        .addMigrations(AppDatabase.MIGRATION_11_12)
        // No fallbackToDestructiveMigration(): a schema mismatch must fail loudly
        // (IllegalStateException at open) rather than silently wiping the user's
        // entire library/history/playlists DB. Every schema bump REQUIRES an
        // explicit Migration registered above.
        .build()
    }

    @Provides
    fun provideLibraryDao(database: AppDatabase): LibraryDao {
        return database.libraryDao()
    }
    
    @Provides
    fun provideListeningHistoryDao(database: AppDatabase): ListeningHistoryDao {
        return database.listeningHistoryDao()
    }

    @Provides
    fun provideDislikedItemDao(database: AppDatabase): DislikedItemDao {
        return database.dislikedItemDao()
    }

    @Provides
    fun provideSongGenreDao(database: AppDatabase): SongGenreDao {
        return database.songGenreDao()
    }

    @Provides
    fun provideLyricsDao(database: AppDatabase): LyricsDao {
        return database.lyricsDao()
    }
}
