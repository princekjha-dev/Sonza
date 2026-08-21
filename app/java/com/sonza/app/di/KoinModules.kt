package com.sonza.app.di

import androidx.media3.datasource.DataSource
import androidx.media3.datasource.cache.Cache
import com.sonza.app.core.db.DatabaseDriverFactory
import com.sonza.app.core.db.SonzaDatabase
import com.sonza.app.core.db.buildDatabase
import com.sonza.app.core.domain.repository.LibraryRepository
import com.sonza.app.ui.screens.viewmodel.RecentsViewModel
import com.sonza.app.ui.screens.wrapped.WrappedViewModel
import com.sonza.app.ui.viewmodel.AboutViewModel
import com.sonza.app.ui.viewmodel.MainViewModel
import com.sonza.app.ui.viewmodel.PlayerViewModel
import com.sonza.app.ui.viewmodel.AIEqualizerViewModel
import com.sonza.app.ui.viewmodel.AlbumViewModel
import com.sonza.app.ui.viewmodel.ArtistViewModel
import com.sonza.app.ui.viewmodel.BackupViewModel
import com.sonza.app.ui.viewmodel.DownloadsViewModel
import com.sonza.app.ui.viewmodel.ExploreViewModel
import com.sonza.app.ui.viewmodel.HomeViewModel
import com.sonza.app.ui.viewmodel.LibraryViewModel
import com.sonza.app.ui.viewmodel.ListeningStatsViewModel
import com.sonza.app.ui.viewmodel.MoodAndGenresViewModel
import com.sonza.app.ui.viewmodel.PickMusicViewModel
import com.sonza.app.ui.viewmodel.PlaylistManagementViewModel
import com.sonza.app.ui.viewmodel.PlaylistMigrationViewModel
import com.sonza.app.ui.viewmodel.PlaylistViewModel
import com.sonza.app.ui.viewmodel.RingtoneViewModel
import com.sonza.app.ui.viewmodel.SearchViewModel
import com.sonza.app.ui.viewmodel.SettingsViewModel
import com.sonza.app.ui.viewmodel.SongInfoViewModel
import com.sonza.app.updater.UpdateViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

// Stable Koin qualifier names mirroring the existing Hilt @Qualifier annotations.
internal const val Q_PLAYER_DATA_SOURCE = "PlayerDataSource"
internal const val Q_DOWNLOAD_DATA_SOURCE = "DownloadDataSource"
internal const val Q_APPLICATION_SCOPE = "ApplicationScope"

/**
 * Bridge module — every shared singleton is fetched from the live Hilt graph
 * via [HiltKoinBridgeEntryPoint] rather than constructed by Koin. This avoids
 * duplicate instantiation of resource-holding objects (Cache, Room DB,
 * MusicPlayer, etc.) during the parallel-DI period of phase 1.
 *
 * In chunk 1d, when Hilt is removed, this module is replaced with direct
 * `single { ... }` constructors and the bridge is deleted.
 */
private val hiltBridgedModule: Module = module {
    // app/di AppModule equivalents
    single { bridge(androidContext()).sessionManager() }
    single { bridge(androidContext()).okHttpClient() }
    single { bridge(androidContext()).gson() }
    single { bridge(androidContext()).remoteAudioRepository() }
    single { bridge(androidContext()).youTubeRepository() }
    single { bridge(androidContext()).localAudioRepository() }
    single { bridge(androidContext()).musicHapticsManager() }
    single { bridge(androidContext()).musicPlayer() }
    single { bridge(androidContext()).lyricsRepository() }
    single { bridge(androidContext()).workManager() }

    // app/di CacheModule equivalents
    single { bridge(androidContext()).cache() }
    single<DataSource.Factory>(named(Q_PLAYER_DATA_SOURCE)) {
        bridge(androidContext()).playerDataSourceFactory()
    }
    single<DataSource.Factory>(named(Q_DOWNLOAD_DATA_SOURCE)) {
        bridge(androidContext()).downloadDataSourceFactory()
    }

    // Transitive @Inject constructor classes consumed by 1c.1 VMs
    single { bridge(androidContext()).youTubeJsonParser() }
    single { bridge(androidContext()).youTubeApiClient() }
    single { bridge(androidContext()).youTubeStreamingService() }
    single { bridge(androidContext()).youTubeSearchService() }
    single { bridge(androidContext()).networkMonitor() }
    single { bridge(androidContext()).listeningHistoryRepository() }
    single { bridge(androidContext()).ringtoneHelper() }
    single { bridge(androidContext()).downloadRepository() }

    // core/data DatabaseModule equivalents
    single { bridge(androidContext()).appDatabase() }
    single { bridge(androidContext()).libraryDao() }
    single { bridge(androidContext()).listeningHistoryDao() }
    single { bridge(androidContext()).dislikedItemDao() }
    single { bridge(androidContext()).songGenreDao() }

    // core/data RepositoryModule equivalent
    single<LibraryRepository> { bridge(androidContext()).libraryRepository() }

    // scrobbler LastFmModule equivalents
    single { bridge(androidContext()).lastFmConfig() }
    single { bridge(androidContext()).lastFmClient() }

    // updater UpdaterModule equivalent
    single { bridge(androidContext()).updateChecker() }

    // chunk 1c.3 — additional transitive @Inject classes
    single { bridge(androidContext()).spotifyImportHelper() }
    single { bridge(androidContext()).playlistImportHelper() }
    single { bridge(androidContext()).recommendationEngine() }
    single { bridge(androidContext()).lastFmRepository() }
    single { bridge(androidContext()).audioARManager() }
    single { bridge(androidContext()).aiEqualizerService() }
    single { bridge(androidContext()).wrappedGenerator() }
    single { bridge(androidContext()).backupManager() }

    // chunk 1c.4 — PlayerViewModel + UpdateViewModel transitive @Inject classes
    single { bridge(androidContext()).sleepTimerManager() }
    single { bridge(androidContext()).smartQueueManager() }
    single { bridge(androidContext()).sponsorBlockRepository() }
    single { bridge(androidContext()).discordManager() }
    single { bridge(androidContext()).spatialAudioProcessor() }
    single { bridge(androidContext()).updateDownloader() }
    single { bridge(androidContext()).loudnessAnalyzer() }
    single { bridge(androidContext()).playlistMigrationManager() }
    single { bridge(androidContext()).trackMatchingEngine() }
    single { bridge(androidContext()).spotifyProvider() }
    single { bridge(androidContext()).youTubeMusicProvider() }
    single { bridge(androidContext()).fileExportProvider() }

    // MainViewModel uses a deferred Cache accessor (was dagger.Lazy<Cache>) to
    // avoid forcing SimpleCache initialization on app start. Provided as a
    // factory function so MainViewModel constructor stays decoupled from
    // dagger.* — same shape Hilt would have given via dagger.Lazy.
    single<() -> Cache> {
        { bridge(androidContext()).cache() }
    }
}

/**
 * @ApplicationScope CoroutineScope is intentionally NOT bridged — it carries no
 * OS resources, so a Koin-owned scope coexisting with Hilt's is harmless.
 * Cancellation does not propagate across them, which is fine here: nothing in
 * the Koin-resolved code paths relies on cancellation of Hilt's scope.
 */
private val coroutineScopesModule: Module = module {
    single<CoroutineScope>(named(Q_APPLICATION_SCOPE)) {
        CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}

/**
 * SQLDelight database — chunk 3b.3-A bindings. NOT routed through the
 * Hilt bridge because Hilt knows nothing about SonzaDatabase; Koin owns
 * construction directly.
 *
 * Both bindings are lazy — the SQLite file (`sonza.sqldelight.db`) only
 * gets opened the first time something resolves [SonzaDatabase]. The
 * health check in [com.sonza.app.SonzaApplication.onCreate]
 * forces that resolution at startup so we know early if the driver is
 * misconfigured (instead of finding out months later when a real consumer
 * calls in).
 */
private val sqlDelightModule: Module = module {
    single { DatabaseDriverFactory(androidContext()) }
    single<SonzaDatabase> { buildDatabase(get<DatabaseDriverFactory>()) }
}

/**
 * ViewModels migrated to Koin in chunk 1c.1. These ARE Koin-owned (not
 * bridged) — Koin instantiates them with Hilt-bridged singletons as deps.
 * Each VM listed here also has its @HiltViewModel annotation removed and every
 * koinViewModel<X>() call site switched to koinViewModel<X>().
 */
private val viewModelsModule: Module = module {
    // chunk 1c.1
    viewModelOf(::AboutViewModel)
    viewModelOf(::PickMusicViewModel)
    viewModelOf(::SongInfoViewModel)
    viewModelOf(::ExploreViewModel)
    viewModelOf(::ArtistViewModel)
    viewModelOf(::RingtoneViewModel)
    // chunk 1c.2
    viewModelOf(::SearchViewModel)
    viewModelOf(::AlbumViewModel)
    viewModelOf(::DownloadsViewModel)
    viewModelOf(::MoodAndGenresViewModel)
    viewModelOf(::PlaylistManagementViewModel)
    viewModelOf(::ListeningStatsViewModel)
    // chunk 1c.3
    viewModelOf(::HomeViewModel)
    viewModelOf(::LibraryViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::PlaylistViewModel)
    viewModelOf(::AIEqualizerViewModel)
    viewModelOf(::RecentsViewModel)
    viewModelOf(::WrappedViewModel)
    viewModelOf(::BackupViewModel)
    // chunk 1c.4 — last 3 VMs; after this, no @HiltViewModel remains
    viewModelOf(::PlayerViewModel)
    viewModelOf(::MainViewModel)
    viewModelOf(::UpdateViewModel)
    viewModelOf(::PlaylistMigrationViewModel)
}

/**
 * Aggregated Koin module list registered with `startKoin` in
 * [com.sonza.app.SonzaApplication].
 */
val koinAppModules: List<Module> = listOf(
    hiltBridgedModule,
    coroutineScopesModule,
    sqlDelightModule,
    viewModelsModule,
)
