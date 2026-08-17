package com.sonza.music.di

import android.content.Context
import androidx.room.Room
import com.sonza.music.audio.analyzer.AudioAnalyzer
import com.sonza.music.audio.engine.SonzaAudioEngine
import com.sonza.music.audio.equalizer.SonzaEqualizer
import com.sonza.music.audio.haptics.HapticScheduler
import com.sonza.music.audio.spatial.SpatialAudioProcessor
import com.sonza.music.core.common.Constants
import com.sonza.music.core.database.HistoryDao
import com.sonza.music.core.database.LyricsDao
import com.sonza.music.core.database.PlaylistDao
import com.sonza.music.core.database.SonzaDatabase
import com.sonza.music.core.database.TrackDao
import com.sonza.music.data.local.LocalMusicScanner
import com.sonza.music.data.repository.ListenTogetherRepository
import com.sonza.music.data.repository.ListenTogetherRepositoryImpl
import com.sonza.music.data.repository.LyricsRepository
import com.sonza.music.data.repository.LyricsRepositoryImpl
import com.sonza.music.data.repository.MusicRepository
import com.sonza.music.data.repository.MusicRepositoryImpl
import com.sonza.music.data.repository.PlaylistRepository
import com.sonza.music.data.repository.PlaylistRepositoryImpl
import com.sonza.music.data.repository.SettingsRepository
import com.sonza.music.data.repository.SettingsRepositoryImpl
import com.sonza.music.data.repository.StatsRepository
import com.sonza.music.data.repository.StatsRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SonzaDatabase {
        return Room.databaseBuilder(
            context,
            SonzaDatabase::class.java,
            Constants.DATABASE_NAME
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideTrackDao(db: SonzaDatabase): TrackDao = db.trackDao()

    @Provides
    fun providePlaylistDao(db: SonzaDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideHistoryDao(db: SonzaDatabase): HistoryDao = db.historyDao()

    @Provides
    fun provideLyricsDao(db: SonzaDatabase): LyricsDao = db.lyricsDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideEqualizer(): SonzaEqualizer = SonzaEqualizer()

    @Provides
    @Singleton
    fun provideSpatialProcessor(@ApplicationContext context: Context): SpatialAudioProcessor {
        return SpatialAudioProcessor(context)
    }

    @Provides
    @Singleton
    fun provideAudioAnalyzer(): AudioAnalyzer = AudioAnalyzer()

    @Provides
    @Singleton
    fun provideHapticScheduler(@ApplicationContext context: Context): HapticScheduler {
        return HapticScheduler(context)
    }

    @Provides
    @Singleton
    fun provideLocalScanner(@ApplicationContext context: Context): LocalMusicScanner {
        return LocalMusicScanner(context)
    }

    @Provides
    @Singleton
    fun provideAudioEngine(
        @ApplicationContext context: Context,
        equalizer: SonzaEqualizer,
        spatialProcessor: SpatialAudioProcessor,
        analyzer: AudioAnalyzer
    ): SonzaAudioEngine {
        return SonzaAudioEngine(context, equalizer, spatialProcessor, analyzer)
    }

    @Provides
    @Singleton
    fun provideMusicRepository(
        trackDao: TrackDao,
        localScanner: LocalMusicScanner
    ): MusicRepository {
        return MusicRepositoryImpl(trackDao, localScanner)
    }

    @Provides
    @Singleton
    fun providePlaylistRepository(playlistDao: PlaylistDao): PlaylistRepository {
        return PlaylistRepositoryImpl(playlistDao)
    }

    @Provides
    @Singleton
    fun provideLyricsRepository(lyricsDao: LyricsDao): LyricsRepository {
        return LyricsRepositoryImpl(lyricsDao)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context): SettingsRepository {
        return SettingsRepositoryImpl(context)
    }

    @Provides
    @Singleton
    fun provideStatsRepository(historyDao: HistoryDao): StatsRepository {
        return StatsRepositoryImpl(historyDao)
    }

    @Provides
    @Singleton
    fun provideListenTogetherRepository(client: OkHttpClient): ListenTogetherRepository {
        return ListenTogetherRepositoryImpl(client)
    }
}
