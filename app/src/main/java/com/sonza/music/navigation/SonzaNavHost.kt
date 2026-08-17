package com.sonza.music.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.sonza.music.core.model.EqualizerPreset
import com.sonza.music.core.model.OutputGearPreference
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaSurface
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.feature.equalizer.EqualizerScreen
import com.sonza.music.feature.equalizer.EqualizerViewModel
import com.sonza.music.feature.home.HomeScreen
import com.sonza.music.feature.home.HomeViewModel
import com.sonza.music.feature.library.LibraryScreen
import com.sonza.music.feature.listeningroom.ListeningRoomScreen
import com.sonza.music.feature.listeningroom.ListeningRoomViewModel
import com.sonza.music.feature.lyrics.LyricsScreen
import com.sonza.music.feature.lyrics.LyricsViewModel
import com.sonza.music.feature.onboarding.OnboardingScreen
import com.sonza.music.feature.onboarding.OnboardingViewModel
import com.sonza.music.feature.player.MiniPlayer
import com.sonza.music.feature.player.NowPlayingScreen
import com.sonza.music.feature.player.PlayerViewModel
import com.sonza.music.feature.settings.SettingsScreen
import com.sonza.music.feature.settings.SettingsViewModel
import com.sonza.music.feature.search.SearchScreen
import com.sonza.music.feature.search.SearchViewModel
import com.sonza.music.feature.spotify.SpotifyImportScreen
import com.sonza.music.feature.spotify.SpotifyImportViewModel
import com.sonza.music.feature.stats.StatsScreen
import com.sonza.music.feature.stats.StatsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "Home", Icons.Default.Home)
    data object Search : Screen("search", "Search", Icons.Default.Search)
    data object Library : Screen("library", "Library", Icons.Default.LibraryMusic)
    data object Onboarding : Screen("onboarding", "Onboarding", Icons.Default.Home)
}

@Composable
fun SonzaNavHost(
    onboardingViewModel: OnboardingViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    searchViewModel: SearchViewModel = hiltViewModel(),
    playerViewModel: PlayerViewModel = hiltViewModel(),
    equalizerViewModel: EqualizerViewModel = hiltViewModel(),
    lyricsViewModel: LyricsViewModel = hiltViewModel(),
    listeningRoomViewModel: ListeningRoomViewModel = hiltViewModel(),
    spotifyImportViewModel: SpotifyImportViewModel = hiltViewModel(),
    statsViewModel: StatsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val preferences by settingsViewModel.preferences.collectAsState()
    val playerState by playerViewModel.playerState.collectAsState()
    val allTracks by homeViewModel.allTracks.collectAsState()
    val albums by homeViewModel.albums.collectAsState()

    var showNowPlaying by remember { mutableStateOf(false) }
    var showEqualizer by remember { mutableStateOf(false) }
    var showLyrics by remember { mutableStateOf(false) }
    var showListeningRoom by remember { mutableStateOf(false) }
    var showSpotifyImport by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val startDestination = if (preferences.onboarded) Screen.Home.route else Screen.Onboarding.route

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isBottomBarVisible = currentRoute in listOf(Screen.Home.route, Screen.Search.route, Screen.Library.route)

    Scaffold(
        bottomBar = {
            if (isBottomBarVisible) {
                Column(modifier = Modifier.navigationBarsPadding()) {
                    // Floating Mini-Player Pill
                    if (playerState.currentTrack != null) {
                        MiniPlayer(
                            playerState = playerState,
                            onExpand = { showNowPlaying = true },
                            onPlayPause = { playerViewModel.togglePlayPause() },
                            onNext = { playerViewModel.next() },
                            onPrevious = { playerViewModel.previous() }
                        )
                    }

                    // Bottom Navigation Bar
                    NavigationBar(
                        containerColor = SonzaSurface,
                        contentColor = SonzaCyanAccent,
                        tonalElevation = 8.dp
                    ) {
                        val items = listOf(Screen.Home, Screen.Search, Screen.Library)
                        items.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title, modifier = Modifier.size(24.dp)) },
                                label = { Text(screen.title) },
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = SonzaCyanAccent,
                                    selectedTextColor = SonzaCyanAccent,
                                    indicatorColor = SonzaCyanAccent.copy(alpha = 0.15f),
                                    unselectedIconColor = SonzaTextSecondary,
                                    unselectedTextColor = SonzaTextSecondary
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(navController = navController, startDestination = startDestination) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        onFinished = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        },
                        onCompleteOnboarding = { genres, gear, quality ->
                            onboardingViewModel.completeOnboarding(genres, gear, quality)
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        tracks = allTracks,
                        albums = albums,
                        onTrackSelected = { track ->
                            homeViewModel.playTrack(track, allTracks)
                            showNowPlaying = true
                        },
                        onNavigateToEqualizer = { showEqualizer = true },
                        onNavigateToListeningRoom = { showListeningRoom = true },
                        onNavigateToSpotifyImport = { showSpotifyImport = true },
                        onNavigateToSettings = { showSettings = true }
                    )
                }

                composable(Screen.Search.route) {
                    val query by searchViewModel.query.collectAsState()
                    val results by searchViewModel.searchResults.collectAsState()

                    SearchScreen(
                        query = query,
                        onQueryChange = { searchViewModel.updateQuery(it) },
                        results = results,
                        onTrackSelected = { track ->
                            homeViewModel.playTrack(track, allTracks)
                            showNowPlaying = true
                        }
                    )
                }

                composable(Screen.Library.route) {
                    LibraryScreen(
                        tracks = allTracks,
                        albums = albums,
                        artists = emptyList(),
                        playlists = emptyList(),
                        onTrackSelected = { track ->
                            homeViewModel.playTrack(track, allTracks)
                            showNowPlaying = true
                        },
                        onCreatePlaylist = {}
                    )
                }
            }

            // Full-screen Overlays with Smooth Slide Animations
            AnimatedVisibility(
                visible = showNowPlaying,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NowPlayingScreen(
                    playerState = playerState,
                    onDismiss = { showNowPlaying = false },
                    onPlayPause = { playerViewModel.togglePlayPause() },
                    onNext = { playerViewModel.next() },
                    onPrevious = { playerViewModel.previous() },
                    onSeek = { playerViewModel.seekTo(it) },
                    onToggleFavorite = { playerViewModel.toggleFavorite(it) },
                    onToggleShuffle = { playerViewModel.toggleShuffle() },
                    onCycleRepeat = { playerViewModel.cycleRepeatMode() },
                    onOpenEqualizer = { showEqualizer = true },
                    onOpenLyrics = {
                        playerState.currentTrack?.let { lyricsViewModel.loadLyricsForTrack(it.id) }
                        showLyrics = true
                    },
                    onSetSleepTimer = { playerViewModel.startSleepTimer(it) }
                )
            }

            AnimatedVisibility(
                visible = showEqualizer,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                val gains by equalizerViewModel.gainsFlow.collectAsState()
                val activePreset by equalizerViewModel.presetFlow.collectAsState()
                val spatialConfig by equalizerViewModel.spatialConfigFlow.collectAsState()

                EqualizerScreen(
                    gains = gains,
                    activePreset = activePreset,
                    spatialConfig = spatialConfig,
                    onBandChange = { band, gain -> equalizerViewModel.setBandGain(band, gain) },
                    onPresetChange = { equalizerViewModel.applyPreset(it) },
                    onReset = { equalizerViewModel.resetEq() },
                    onSpatialChange = { equalizerViewModel.updateSpatial(it) },
                    onDismiss = { showEqualizer = false }
                )
            }

            AnimatedVisibility(
                visible = showLyrics,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                val lyrics by lyricsViewModel.lyrics.collectAsState()
                val track = playerState.currentTrack

                LyricsScreen(
                    lyrics = lyrics,
                    currentPositionMs = playerState.currentPositionMs,
                    trackTitle = track?.title ?: "Track",
                    artistName = track?.artist ?: "Artist",
                    onSeekTo = { lyricsViewModel.seekTo(it) },
                    onDismiss = { showLyrics = false }
                )
            }

            AnimatedVisibility(
                visible = showListeningRoom,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                val activeRoom by listeningRoomViewModel.activeRoom.collectAsState()
                val syncLatency by listeningRoomViewModel.syncLatencyMs.collectAsState()

                ListeningRoomScreen(
                    activeRoom = activeRoom,
                    syncLatencyMs = syncLatency,
                    onCreateRoom = { title, name -> listeningRoomViewModel.createRoom(title, name) },
                    onJoinRoom = { code, name -> listeningRoomViewModel.joinRoom(code, name) },
                    onLeaveRoom = { listeningRoomViewModel.leaveRoom() },
                    onDismiss = { showListeningRoom = false }
                )
            }

            AnimatedVisibility(
                visible = showSpotifyImport,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                val report by spotifyImportViewModel.report.collectAsState()

                SpotifyImportScreen(
                    report = report,
                    onImport = { spotifyImportViewModel.importDemoSpotifyPlaylist(it) },
                    onSavePlaylist = { spotifyImportViewModel.saveImportedPlaylist(it) },
                    onDismiss = { showSpotifyImport = false }
                )
            }

            AnimatedVisibility(
                visible = showSettings,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                SettingsScreen(
                    preferences = preferences,
                    onHapticChange = { settingsViewModel.setHapticIntensity(it) },
                    onVolumeNormChange = { settingsViewModel.setVolumeNormalization(it) },
                    onCrossfadeChange = { settingsViewModel.setCrossfadeDuration(it) },
                    onGaplessChange = { settingsViewModel.setGapless(it) },
                    onReduceMotionChange = { settingsViewModel.setReduceMotion(it) },
                    onThemeModeChange = { settingsViewModel.setThemeMode(it) },
                    onDismiss = { showSettings = false }
                )
            }
        }
    }
}
