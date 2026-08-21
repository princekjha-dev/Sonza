package com.sonza.app.di

import android.content.Context
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import androidx.work.WorkManager
import com.google.gson.Gson
import com.sonza.app.core.data.local.AppDatabase
import com.sonza.app.core.data.local.dao.DislikedItemDao
import com.sonza.app.core.data.local.dao.LibraryDao
import com.sonza.app.core.data.local.dao.ListeningHistoryDao
import com.sonza.app.core.data.local.dao.SongGenreDao
import com.sonza.app.core.domain.repository.LibraryRepository
import com.sonza.app.data.SessionManager
import com.sonza.app.data.repository.DownloadRepository
import com.sonza.app.data.repository.RemoteAudioRepository
import com.sonza.app.data.repository.ListeningHistoryRepository
import com.sonza.app.data.repository.LocalAudioRepository
import com.sonza.app.data.repository.LyricsRepository
import com.sonza.app.data.repository.YouTubeRepository
import com.sonza.app.data.repository.youtube.internal.YouTubeApiClient
import com.sonza.app.data.repository.youtube.internal.YouTubeJsonParser
import com.sonza.app.data.repository.youtube.search.YouTubeSearchService
import com.sonza.app.data.repository.youtube.streaming.YouTubeStreamingService
import com.sonza.app.ai.AIEqualizerService
import com.sonza.app.data.BackupManager
import com.sonza.app.lastfm.LastFmClient
import com.sonza.app.lastfm.LastFmConfig
import com.sonza.app.lastfm.LastFmRepository
import com.sonza.app.data.repository.SponsorBlockRepository
import com.sonza.app.discord.DiscordManager
import com.sonza.app.player.AudioARManager
import com.sonza.app.player.MusicPlayer
import com.sonza.app.player.SleepTimerManager
import com.sonza.app.player.SpatialAudioProcessor
import com.sonza.app.recommendation.RecommendationEngine
import com.sonza.app.recommendation.SmartQueueManager
import com.sonza.app.recommendation.WrappedGenerator
import com.sonza.app.updater.UpdateChecker
import com.sonza.app.updater.UpdateDownloader
import com.sonza.app.util.MusicHapticsManager
import com.sonza.app.util.NetworkMonitor
import com.sonza.app.util.PlaylistImportHelper
import com.sonza.app.util.RingtoneHelper
import com.sonza.app.util.SpotifyImportHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient

/**
 * Hilt -> Koin bridge for the parallel-DI period of phase 1.
 *
 * Why this exists:
 * Hilt and Koin would otherwise each construct their own copy of every
 * singleton, and shared OS resources (SimpleCache file lock, Room DB lock,
 * ExoPlayer instance, MediaSession, audio focus owner) crash or misbehave on
 * the second construction. We saw `IllegalStateException: Another SimpleCache
 * instance uses the folder` during chunk 1c.1.
 *
 * The bridge has Koin's `single { ... }` blocks delegate to Hilt's already-
 * constructed instances via this @EntryPoint. Result: single source of truth
 * for every shared object during the migration.
 *
 * Lifecycle:
 * - Added: chunk 1c (now). Required for any Koin consumer that resolves a
 *   shared singleton.
 * - Removed: chunk 1d, when Hilt itself is removed. At that point Koin's
 *   `single { ... }` blocks reclaim direct construction.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HiltKoinBridgeEntryPoint {
    // app/di — AppModule
    fun sessionManager(): SessionManager
    fun youTubeRepository(): YouTubeRepository
    fun localAudioRepository(): LocalAudioRepository
    fun okHttpClient(): OkHttpClient
    fun gson(): Gson
    fun remoteAudioRepository(): RemoteAudioRepository
    fun musicHapticsManager(): MusicHapticsManager
    fun musicPlayer(): MusicPlayer
    fun lyricsRepository(): LyricsRepository
    fun workManager(): WorkManager

    // app/di — CacheModule
    fun cache(): Cache

    @PlayerDataSource
    fun playerDataSourceFactory(): DataSource.Factory

    @DownloadDataSource
    fun downloadDataSourceFactory(): DataSource.Factory

    // app — transitive @Inject constructor classes that 1c.1 VMs reach
    fun youTubeJsonParser(): YouTubeJsonParser
    fun youTubeApiClient(): YouTubeApiClient
    fun youTubeStreamingService(): YouTubeStreamingService
    fun youTubeSearchService(): YouTubeSearchService
    fun networkMonitor(): NetworkMonitor
    fun listeningHistoryRepository(): ListeningHistoryRepository
    fun ringtoneHelper(): RingtoneHelper
    fun downloadRepository(): DownloadRepository

    // core/data
    fun appDatabase(): AppDatabase
    fun libraryDao(): LibraryDao
    fun listeningHistoryDao(): ListeningHistoryDao
    fun dislikedItemDao(): DislikedItemDao
    fun songGenreDao(): SongGenreDao
    fun libraryRepository(): LibraryRepository

    // scrobbler
    fun lastFmConfig(): LastFmConfig
    fun lastFmClient(): LastFmClient

    // updater
    fun updateChecker(): UpdateChecker

    // chunk 1c.3 — additional transitive @Inject constructor classes
    fun spotifyImportHelper(): SpotifyImportHelper
    fun playlistImportHelper(): PlaylistImportHelper
    fun recommendationEngine(): RecommendationEngine
    fun lastFmRepository(): LastFmRepository
    fun audioARManager(): AudioARManager
    fun aiEqualizerService(): AIEqualizerService
    fun wrappedGenerator(): WrappedGenerator
    fun backupManager(): BackupManager

    // chunk 1c.4 — PlayerViewModel + UpdateViewModel transitive @Inject classes
    fun sleepTimerManager(): SleepTimerManager
    fun smartQueueManager(): SmartQueueManager
    fun sponsorBlockRepository(): SponsorBlockRepository
    fun discordManager(): DiscordManager
    fun spatialAudioProcessor(): SpatialAudioProcessor
    fun updateDownloader(): UpdateDownloader
    fun loudnessAnalyzer(): com.sonza.app.player.LoudnessAnalyzer
    fun playlistMigrationManager(): com.sonza.app.data.migration.PlaylistMigrationManager
    fun trackMatchingEngine(): com.sonza.app.data.migration.engine.TrackMatchingEngine
    fun spotifyProvider(): com.sonza.app.data.migration.provider.SpotifyProvider
    fun youTubeMusicProvider(): com.sonza.app.data.migration.provider.YouTubeMusicProvider
    fun fileExportProvider(): com.sonza.app.data.migration.provider.FileExportProvider
}

/** One-call accessor used by Koin module blocks. Resolved against the application Context. */
internal fun bridge(context: Context): HiltKoinBridgeEntryPoint =
    EntryPointAccessors.fromApplication(
        context.applicationContext,
        HiltKoinBridgeEntryPoint::class.java,
    )
