package com.sonza.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.sonza.app.data.SessionManager
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.Artist
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.PlayerState
import com.sonza.app.ui.utils.DeviceFormFactor
import com.sonza.app.ui.utils.LocalDeviceFormFactor
import com.sonza.app.ui.screens.AboutScreen
import com.sonza.app.ui.screens.HowItWorksScreen
import com.sonza.app.ui.screens.AppearanceSettingsScreen
import com.sonza.app.ui.screens.ArtworkShapeScreen
import com.sonza.app.ui.screens.ArtworkSizeScreen
import com.sonza.app.ui.screens.CustomizationScreen
import com.sonza.app.ui.screens.HomeScreen
import com.sonza.app.ui.screens.LibraryScreen
import com.sonza.app.ui.screens.player.PlayerScreen
import com.sonza.app.ui.screens.PlaybackSettingsScreen
import com.sonza.app.ui.screens.PlaylistScreen
import com.sonza.app.ui.screens.RecentsScreen
import com.sonza.app.ui.screens.SearchScreen
import com.sonza.app.ui.screens.SeekbarStyleScreen
import com.sonza.app.ui.screens.SettingsScreen
import com.sonza.app.ui.screens.StorageScreen
import com.sonza.app.ui.screens.SupportScreen
import com.sonza.app.ui.screens.YouTubeLoginScreen
import com.sonza.app.ui.screens.MiscScreen
import com.sonza.app.ui.screens.LyricsProvidersScreen
import com.sonza.app.ui.screens.ChangelogScreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharedFlow
import androidx.media3.common.Player
import com.sonza.app.ui.screens.SponsorBlockSettingsScreen
import com.sonza.app.ui.screens.ListenTogetherScreen
import org.koin.compose.viewmodel.koinViewModel

import androidx.navigation.toRoute

/**
 * Main navigation graph for the app.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    playbackInfo: PlayerState,
    playerState: PlayerState,
    sessionManager: SessionManager,
    youTubeRepository: com.sonza.app.data.repository.YouTubeRepository,
    onPlaySong: (List<Song>, Int) -> Unit,
    onPlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onDownloadCurrentSong: () -> Unit,
    onLikeCurrentSong: () -> Unit,
    onDislikeCurrentSong: () -> Unit = {},
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    onToggleAutoplay: () -> Unit,
    onToggleVideoMode: () -> Unit = {},
    onDismissVideoError: () -> Unit = {},
    onStartRadio: (Song?, List<Song>?) -> Unit = { _, _ -> },
    onLoadMoreRadioSongs: () -> Unit = {},
    isRadioMode: Boolean = false,
    isLoadingMoreSongs: Boolean = false,
    onSwitchDevice: (com.sonza.app.core.model.OutputDevice) -> Unit = {},
    onRefreshDevices: () -> Unit = {},
    onSetPlaybackParameters: (Float, Float) -> Unit = { _, _ -> },
    player: Player? = null,
    lyrics: com.sonza.app.providers.lyrics.Lyrics?,
    isFetchingLyrics: Boolean,
    isLoggedIn: Boolean = false,
    // Lyrics Provider
    selectedLyricsProvider: com.sonza.app.providers.lyrics.LyricsProviderType = com.sonza.app.providers.lyrics.LyricsProviderType.AUTO,
    enabledLyricsProviders: Map<com.sonza.app.providers.lyrics.LyricsProviderType, Boolean> = emptyMap(),
    onLyricsProviderChange: (com.sonza.app.providers.lyrics.LyricsProviderType) -> Unit = {},
    // Sleep timer
    sleepTimerOption: com.sonza.app.player.SleepTimerOption = com.sonza.app.player.SleepTimerOption.OFF,
    sleepTimerRemainingMs: Long? = null,
    onSetSleepTimer: (com.sonza.app.player.SleepTimerOption, Int?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    volumeKeyEvents: SharedFlow<Unit>? = null,
    downloadRepository: com.sonza.app.data.repository.DownloadRepository? = null,
    startDestination: Any = Destination.Home,
    dominantColors: com.sonza.app.ui.components.DominantColors? = null,
    snackbarHostState: androidx.compose.material3.SnackbarHostState? = null
) {
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val formFactor = LocalDeviceFormFactor.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
             fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                 towards = AnimatedContentTransitionScope.SlideDirection.End,
                 animationSpec = tween(300)
             )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300))
        }
    ) {
        composable<Destination.Home> {
            when {
                formFactor == DeviceFormFactor.TV -> {
                    com.sonza.app.ui.screens.TvHomeScreen(
                        onSongClick = { songs, index -> onPlaySong(songs, index) },
                        onPlaylistClick = { playlist ->
                            navController.navigate(
                                Destination.Playlist(
                                    playlistId = playlist.id,
                                    name = playlist.name,
                                    thumbnailUrl = playlist.thumbnailUrl
                                )
                            )
                        },
                        onAlbumClick = { album ->
                            navController.navigate(
                                Destination.Album(
                                    albumId = album.id,
                                    name = album.title,
                                    thumbnailUrl = album.thumbnailUrl
                                )
                            )
                        }
                    )
                }
                formFactor.isTabletLike -> {
                    com.sonza.app.ui.screens.TabletHomeScreen(
                        onSongClick = { songs, index -> onPlaySong(songs, index) },
                        onPlaylistClick = { playlist ->
                            navController.navigate(
                                Destination.Playlist(
                                    playlistId = playlist.id,
                                    name = playlist.name,
                                    thumbnailUrl = playlist.thumbnailUrl
                                )
                            )
                        },
                        onAlbumClick = { album ->
                            navController.navigate(
                                Destination.Album(
                                    albumId = album.id,
                                    name = album.title,
                                    thumbnailUrl = album.thumbnailUrl
                                )
                            )
                        },
                        onHistoryClick = {
                            navController.navigate(Destination.Recents)
                        },
                        onExploreClick = { browseId, title ->
                            if (browseId == "FEmusic_moods_and_genres") {
                                navController.navigate(Destination.MoodAndGenres)
                            } else {
                                navController.navigate(Destination.Explore(browseId, title))
                            }
                        },
                        onStartRadio = { onStartRadio(null, null) },
                        currentSong = playbackInfo.currentSong
                    )
                }
                else -> {
                    HomeScreen(
                        onSongClick = { songs, index -> onPlaySong(songs, index) },
                        onPlaylistClick = { playlist ->
                            navController.navigate(
                                Destination.Playlist(
                                    playlistId = playlist.id,
                                    name = playlist.name,
                                    thumbnailUrl = playlist.thumbnailUrl
                                )
                            )
                        },
                        onAlbumClick = { album ->
                            navController.navigate(
                                Destination.Album(
                                    albumId = album.id,
                                    name = album.title,
                                    thumbnailUrl = album.thumbnailUrl
                                )
                            )
                        },
                        onHistoryClick = {
                            navController.navigate(Destination.Recents)
                        },
                        onListenTogetherClick = {
                            navController.navigate(Destination.ListenTogether)
                        },
                        onExploreClick = { browseId, title ->
                            if (browseId == "FEmusic_moods_and_genres") {
                                navController.navigate(Destination.MoodAndGenres)
                            } else {
                                navController.navigate(Destination.Explore(browseId, title))
                            }
                        },
                        onStartRadio = { onStartRadio(null, null) },
                        onCreateMixClick = {
                            navController.navigate(Destination.PickMusic)
                        },
                        currentSong = playbackInfo.currentSong
                    )
                }
            }
        }
        
        composable<Destination.ListenTogether> {
            ListenTogetherScreen(
                onDismiss = { navController.popBackStack() },
                dominantColors = dominantColors ?: com.sonza.app.ui.components.DominantColors(
                    primary = MaterialTheme.colorScheme.primary,
                    secondary = MaterialTheme.colorScheme.secondary,
                    accent = MaterialTheme.colorScheme.tertiary,
                    onBackground = MaterialTheme.colorScheme.onBackground
                )
            )
        }
        
        composable<Destination.Explore> {
            com.sonza.app.ui.screens.ExploreScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { songs, index -> onPlaySong(songs, index) },
                onPlaylistClick = { playlist ->
                    navController.navigate(
                        Destination.Playlist(
                            playlistId = playlist.id,
                            name = playlist.name,
                            thumbnailUrl = playlist.thumbnailUrl
                        )
                    )
                },
                onAlbumClick = { album ->
                    navController.navigate(
                        Destination.Album(
                            albumId = album.id,
                            name = album.title,
                            thumbnailUrl = album.thumbnailUrl
                        )
                    )
                }
            )
        }


        composable<Destination.MoodAndGenres> {
            com.sonza.app.ui.screens.MoodAndGenresScreen(
                onCategoryClick = { browseId, params, title ->
                    navController.navigate(
                        Destination.MoodAndGenresDetail(browseId, params, title)
                    )
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Destination.MoodAndGenresDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<Destination.MoodAndGenresDetail>()

            com.sonza.app.ui.screens.MoodAndGenresDetailScreen(
                browseId = route.browseId,
                params = route.params,
                title = route.title,
                onBackClick = { navController.popBackStack() },
                onSongClick = { songs, index -> onPlaySong(songs, index) }
            )
        }
        
        composable<Destination.Search> {
            SearchScreen(
                onSongClick = { songs, index ->
                    // Don't pass search results as queue — fetch recommendations instead
                    onStartRadio(songs[index], null)
                },
                onArtistClick = { artistId ->
                    navController.navigate(Destination.Artist(artistId))
                },
                onPlaylistClick = { playlistId ->
                    navController.navigate(
                        Destination.Playlist(
                            playlistId = playlistId,
                            name = null,
                            thumbnailUrl = null
                        )
                    )
                },
                onAlbumClick = { album ->
                    navController.navigate(
                        Destination.Album(
                            albumId = album.id,
                            name = album.title,
                            thumbnailUrl = album.thumbnailUrl
                        )
                    )
                },
                currentSong = playbackInfo.currentSong
            )
        }        
        composable<Destination.Library> {
            LibraryScreen(
                onSongClick = { songs, index -> 
                    onPlaySong(songs, index)
                },
                onHistoryClick = {
                    navController.navigate(Destination.Recents)
                },
                onPlaylistClick = { playlist ->
                    navController.navigate(
                        Destination.Playlist(
                            playlistId = playlist.id,
                            name = playlist.name,
                            thumbnailUrl = playlist.thumbnailUrl
                        )
                    )
                },
                onArtistClick = { artistId ->
                    navController.navigate(Destination.Artist(artistId))
                },
                onAlbumClick = { album ->
                    navController.navigate(
                        Destination.Album(
                            albumId = album.id,
                            name = album.title,
                            thumbnailUrl = album.thumbnailUrl
                        )
                    )
                },
                onDownloadsClick = {
                    navController.navigate(Destination.Downloads)
                },
                onMigratePlaylistsClick = {
                    navController.navigate(Destination.MigratePlaylists)
                }
            )
        }

        composable<Destination.Downloads> {
            com.sonza.app.ui.screens.DownloadsScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { songs, index -> onPlaySong(songs, index) },
                onPlayAll = { songs -> onPlaySong(songs, 0) },
                onShufflePlay = { songs -> 
                    val shuffledSongs = songs.shuffled()
                    onPlaySong(shuffledSongs, 0)
                }
            )
        }
        
        composable<Destination.Settings> {
            SettingsScreen(
                currentSong = playbackInfo.currentSong,
                onLoginClick = { navController.navigate(Destination.YouTubeLogin) },
                onPlaybackClick = { navController.navigate(Destination.PlaybackSettings) },
                onAppearanceClick = { navController.navigate(Destination.AppearanceSettings) },
                onCustomizationClick = { navController.navigate(Destination.CustomizationSettings) },
                onStorageClick = { navController.navigate(Destination.Storage) },
                onStatsClick = { navController.navigate(Destination.ListeningStats) },
                onSupportClick = { navController.navigate(Destination.Support) },
                onAboutClick = { navController.navigate(Destination.About) },
                onMiscClick = { navController.navigate(Destination.Misc) },
                onLastFmClick = { navController.navigate(Destination.LastFmLogin) },
                onSponsorBlockClick = { navController.navigate(Destination.SponsorBlockSettings) },
                onDiscordClick = { navController.navigate(Destination.DiscordSettings) },
                onAISettingsClick = { navController.navigate(Destination.AISettings) },
                onUpdaterClick = { navController.navigate(Destination.Updater) },
                onMigratePlaylistsClick = { navController.navigate(Destination.MigratePlaylists) }
            )
        }

        composable<Destination.Updater> {
            com.sonza.app.updater.UpdaterScreen(
                currentVersionCode = com.sonza.app.BuildConfig.VERSION_CODE,
                currentVersionName = com.sonza.app.BuildConfig.VERSION_NAME,
                viewModel = koinViewModel(),
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Destination.Changelog> {
            com.sonza.app.ui.screens.ChangelogScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Destination.Storage> {
            val settingsViewModel = koinViewModel<com.sonza.app.ui.viewmodel.SettingsViewModel>()
            downloadRepository?.let { repo ->
                StorageScreen(
                    downloadRepository = repo,
                    settingsViewModel = settingsViewModel,
                    onBackClick = { navController.popBackStack() },
                    onPlayerCacheClick = { navController.navigate(Destination.PlayerCache) }
                )
            }
        }

        composable<Destination.PlayerCache> {
            downloadRepository?.let { repo ->
                val settingsViewModel = koinViewModel<com.sonza.app.ui.viewmodel.SettingsViewModel>()
                com.sonza.app.ui.screens.PlayerCacheScreen(
                    onBackClick = { navController.popBackStack() },
                    settingsViewModel = settingsViewModel,
                    downloadRepository = repo
                )
            }
        }
        
        composable<Destination.PlaybackSettings> {
            PlaybackSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Destination.AppearanceSettings> {
            AppearanceSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Destination.CustomizationSettings> {
            CustomizationScreen(
                onBack = { navController.popBackStack() },
                onSeekbarStyleClick = { navController.navigate(Destination.SeekbarStyleSettings) },
                onArtworkShapeClick = { navController.navigate(Destination.ArtworkShapeSettings) },
                onArtworkSizeClick = { navController.navigate(Destination.ArtworkSizeSettings) }
            )
        }
        
        composable<Destination.ArtworkShapeSettings> {
            ArtworkShapeScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Destination.SeekbarStyleSettings> {
            SeekbarStyleScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Destination.ArtworkSizeSettings> {
            ArtworkSizeScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Destination.Recents> {
            RecentsScreen(
                onSongClick = { songs, index -> onPlaySong(songs, index) },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Destination.About> {
            AboutScreen(
                onBack = { navController.popBackStack() },
                onHowItWorksClick = { navController.navigate(Destination.HowItWorks) }
            )
        }
        
        composable<Destination.HowItWorks> {
            HowItWorksScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        composable<Destination.Support> {
            SupportScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Destination.Misc> {
            MiscScreen(
                onBack = { navController.popBackStack() },
                onLyricsProvidersClick = { navController.navigate(Destination.LyricsProviders) },
                externalSnackbarHostState = snackbarHostState
            )
        }

        composable<Destination.AIEqualizer> {
            val aiService = koinViewModel<com.sonza.app.ui.viewmodel.AIEqualizerViewModel>().aiService
            com.sonza.app.ui.screens.AIEqualizerScreen(
                onBackClick = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Destination.AISettings) },
                aiService = aiService
            )
        }

        composable<Destination.AISettings> {
            com.sonza.app.ui.screens.AISettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Destination.LyricsProviders> {
            LyricsProvidersScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Destination.SponsorBlockSettings> {
            SponsorBlockSettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<Destination.PickMusic> {
            com.sonza.app.ui.screens.PickMusicScreen(
                onBackClick = { navController.popBackStack() },
                onMixCreated = { songs ->
                    if (songs.isNotEmpty()) {
                         onPlaySong(songs, 0)
                    } else {
                         navController.popBackStack()
                    }
                }
            )
        }
        
        composable<Destination.ListeningStats> {
            com.sonza.app.ui.screens.ListeningStatsScreen(
                onBackClick = { navController.popBackStack() },
                onWrappedClick = { navController.navigate(Destination.Wrapped) }
            )
        }

        composable<Destination.Wrapped> {
            com.sonza.app.ui.screens.wrapped.WrappedScreen(
                onBack = { navController.popBackStack() }
            )
        }


        composable<Destination.YouTubeLogin> {
            YouTubeLoginScreen(
                sessionManager = sessionManager,
                onLoginSuccess = {
                    // Show success message
                    com.sonza.app.util.SnackbarUtil.showSuccess("Login Successful")

                    // Mark onboarding as completed
                    scope.launch {
                        sessionManager.setOnboardingCompleted(true)
                        // Fetch and sync history from YouTube to provide better recommendations immediately
                        youTubeRepository.fetchAndSyncHistory()
                    }

                    // Navigate to Home and clear back stack
                    navController.navigate(Destination.Home) {
                        popUpTo<Destination.Home> { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable<Destination.LastFmLogin> {
            com.sonza.app.ui.screens.settings.LastFmSettingsScreen(
                onBack = { navController.popBackStack() },
                onLoginSuccess = { username ->
                    com.sonza.app.util.SnackbarUtil.showSuccess("Connected as $username")
                    navController.popBackStack()
                }
            )
        }

        composable<Destination.DiscordSettings> {
            com.sonza.app.ui.screens.settings.DiscordSettingsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable<Destination.Playlist> {
            PlaylistScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { songs, index -> onPlaySong(songs, index) },
                onPlayAll = { songs ->
                    if (songs.isNotEmpty()) {
                         onPlaySong(songs, 0)
                    }
                },
                onShufflePlay = { songs ->
                     if (songs.isNotEmpty()) {
                         val shuffled = songs.shuffled()
                         onPlaySong(shuffled, 0)
                     }
                },
                onAddSongsClick = { navController.navigate(Destination.Search) },
                currentSong = playbackInfo.currentSong
            )
        }
        composable<Destination.Artist> { backStackEntry ->
            com.sonza.app.ui.screens.ArtistScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { songs, index -> 
                    onPlaySong(songs, index)
                },
                onAlbumClick = { album -> 
                    navController.navigate(
                        Destination.Album(
                            albumId = album.id,
                            name = album.title,
                            thumbnailUrl = album.thumbnailUrl
                        )
                    )
                },
                onSeeAllAlbumsClick = {
                    val route = backStackEntry.toRoute<Destination.Artist>()
                    navController.navigate(
                        Destination.ArtistDiscography(route.artistId, Destination.ArtistDiscography.TYPE_ALBUMS)
                    )
                },
                onSeeAllSinglesClick = {
                    val route = backStackEntry.toRoute<Destination.Artist>()
                    navController.navigate(
                        Destination.ArtistDiscography(route.artistId, Destination.ArtistDiscography.TYPE_SINGLES)
                    )
                },
                onArtistClick = { artist ->
                    navController.navigate(Destination.Artist(artist.id))
                },
                onArtistIdClick = { artistId ->
                    navController.navigate(Destination.Artist(artistId))
                },
                onPlaylistClick = { playlist ->
                    navController.navigate(
                        Destination.Playlist(
                            playlistId = playlist.id,
                            name = playlist.title,
                            thumbnailUrl = playlist.thumbnailUrl
                        )
                    )
                },
                onStartRadio = { songs ->
                    onPlaySong(songs, 0)
                }
            )
        }

        composable<Destination.ArtistDiscography> { backStackEntry ->
            val route = backStackEntry.toRoute<Destination.ArtistDiscography>()
            com.sonza.app.ui.screens.ArtistDiscographyScreen(
                artistId = route.artistId,
                type = route.type,
                onBackClick = { navController.popBackStack() },
                onAlbumClick = { album ->
                    navController.navigate(
                        Destination.Album(
                            albumId = album.id,
                            name = album.title,
                            thumbnailUrl = album.thumbnailUrl
                        )
                    )
                }
            )
        }

        composable<Destination.Album> {
            com.sonza.app.ui.screens.AlbumScreen(
                onBackClick = { navController.popBackStack() },
                onSongClick = { songs, index -> onPlaySong(songs, index) },
                onPlayAll = { songs ->
                    if (songs.isNotEmpty()) {
                        onPlaySong(songs, 0)
                    }
                },
                onShufflePlay = { songs ->
                    if (songs.isNotEmpty()) {
                        val shuffled = songs.shuffled()
                        onPlaySong(shuffled, 0)
                    }
                },
                currentSong = playbackInfo.currentSong
            )
        }

        composable<Destination.MigratePlaylists> {
            val migrationViewModel = koinViewModel<com.sonza.app.ui.viewmodel.PlaylistMigrationViewModel>()
            com.sonza.app.ui.screens.migration.MigratePlaylistsScreen(
                viewModel = migrationViewModel,
                onNavigateBack = { navController.popBackStack() },
                onOpenPlaylist = { playlistId ->
                    navController.navigate(
                        Destination.Playlist(playlistId = playlistId)
                    )
                }
            )
        }
    }
}
