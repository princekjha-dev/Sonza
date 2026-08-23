package com.sonza.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.PlayerState
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.navigation.Destination
import com.sonza.app.ui.utils.DeviceFormFactor
import com.sonza.app.ui.utils.LocalDeviceFormFactor
import com.sonza.app.ui.viewmodel.HomeViewModel
import com.sonza.app.ui.viewmodel.LibraryViewModel
import com.sonza.app.ui.viewmodel.PlaylistManagementViewModel
import com.sonza.app.ui.viewmodel.SearchViewModel
import com.sonza.app.ui.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/**
 * Continuous Root Pager Screen coordinating the four primary application pages:
 *
 * Index 0: Home
 * Index 1: Search
 * Index 2: Library
 * Index 3: Profile
 *
 * Features:
 * - Continuous 1:1 horizontal swipe navigation with simultaneous page translation.
 * - [beyondViewportPageCount] = 1 ensures adjacent pages are pre-composed and rendered in layout,
 *   completely eliminating intermediate black frames or flickers.
 * - Seamless BackHandler: returns to Home (page 0) when pressing Back on Search, Library, or Profile.
 * - Dynamic scroll locking: disables horizontal swiping while the soft keyboard is open in Search.
 */
@Composable
fun MainPagerScreen(
    pagerState: PagerState,
    navController: NavHostController,
    playbackInfo: PlayerState,
    onPlaySong: (List<Song>, Int) -> Unit,
    onPlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    homeViewModel: HomeViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(),
    libraryViewModel: LibraryViewModel = koinViewModel(),
    profileViewModel: SettingsViewModel = koinViewModel(),
    playlistViewModel: PlaylistManagementViewModel = koinViewModel()
) {
    val coroutineScope = rememberCoroutineScope()
    val formFactor = LocalDeviceFormFactor.current

    // Back gesture handling: when on Search (page 1), Library (page 2), or Profile (page 3),
    // pressing Back navigates back to Home (page 0).
    BackHandler(enabled = pagerState.currentPage != 0) {
        coroutineScope.launch {
            pagerState.animateScrollToPage(0)
        }
    }

    // Disable horizontal swipe only when typing in the search box to prevent gesture conflict
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val isKeyboardOpen = imePadding > 0.dp
    val isScrollEnabled = !(pagerState.currentPage == 1 && isKeyboardOpen)

    HorizontalPager(
        state = pagerState,
        beyondViewportPageCount = 1,
        userScrollEnabled = isScrollEnabled,
        modifier = modifier.fillMaxSize(),
        key = { it }
    ) { page ->
        when (page) {
            0 -> {
                when {
                    formFactor == DeviceFormFactor.TV -> {
                        TvHomeScreen(
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
                            viewModel = homeViewModel
                        )
                    }
                    formFactor.isTabletLike -> {
                        TabletHomeScreen(
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
                                } else if (browseId == "FEmusic_podcasts" || title.equals("Podcasts", ignoreCase = true)) {
                                    navController.navigate(Destination.Podcasts)
                                } else {
                                    navController.navigate(Destination.Explore(browseId, title))
                                }
                            },
                            currentSong = playbackInfo.currentSong,
                            viewModel = homeViewModel
                        )
                    }
                    else -> {
                        HomeScreen(
                            onSongClick = { songs, index -> onPlaySong(songs, index) },
                            onPlaylistClick = { playlist: PlaylistDisplayItem ->
                                navController.navigate(
                                    Destination.Playlist(
                                        playlistId = playlist.id,
                                        name = playlist.name,
                                        thumbnailUrl = playlist.thumbnailUrl
                                    )
                                )
                            },
                            onAlbumClick = { album: Album ->
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
                            onProfileClick = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(3)
                                }
                            },
                            onNavigateToSearch = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(1)
                                }
                            },
                            onNavigateToLibrary = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(2)
                                }
                            },
                            onNavigateToProfile = {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(3)
                                }
                            },
                            onExploreClick = { browseId, title ->
                                if (browseId == "FEmusic_moods_and_genres") {
                                    navController.navigate(Destination.MoodAndGenres)
                                } else if (browseId == "FEmusic_podcasts" || title.equals("Podcasts", ignoreCase = true)) {
                                    navController.navigate(Destination.Podcasts)
                                } else {
                                    navController.navigate(Destination.Explore(browseId, title))
                                }
                            },
                            onCreateMixClick = {
                                navController.navigate(Destination.PickMusic)
                            },
                            currentSong = playbackInfo.currentSong,
                            isPlaying = playbackInfo.isPlaying,
                            onPlayPause = onPlayPause,
                            viewModel = homeViewModel,
                            playlistViewModel = playlistViewModel
                        )
                    }
                }
            }
            1 -> {
                SearchScreen(
                    onSongClick = { songs, index ->
                        onPlaySong(songs, index)
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
                    onNavigateToHome = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    onNavigateToLibrary = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    currentSong = playbackInfo.currentSong,
                    viewModel = searchViewModel,
                    playlistViewModel = playlistViewModel
                )
            }
            2 -> {
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
                    },
                    onNavigateToPlaylists = {
                        navController.navigate(Destination.LibraryPlaylists)
                    },
                    onNavigateToArtists = {
                        navController.navigate(Destination.LibraryArtists)
                    },
                    onNavigateToAlbums = {
                        navController.navigate(Destination.LibraryAlbums)
                    },
                    onNavigateToGenres = {
                        navController.navigate(Destination.LibraryGenres)
                    },
                    onNavigateToSearch = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    },
                    onNavigateToProfile = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(3)
                        }
                    },
                    viewModel = libraryViewModel
                )
            }
            3 -> {
                UserProfileScreen(
                    onBackClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    onNavigateToHome = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(0)
                        }
                    },
                    onNavigateToLibrary = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(2)
                        }
                    },
                    onLoginClick = { navController.navigate(Destination.YouTubeLogin) },
                    onHistoryClick = { navController.navigate(Destination.Recents) },
                    onDownloadsClick = { navController.navigate(Destination.Downloads) },
                    onStatsClick = { navController.navigate(Destination.ListeningStats) },
                    onFeedbackClick = { navController.navigate(Destination.Support) },
                    onPrivacyPolicyClick = { navController.navigate(Destination.PrivacyPolicy) },
                    onTermsOfServiceClick = { navController.navigate(Destination.TermsOfService) },
                    onAboutSonzaClick = { navController.navigate(Destination.About) },
                    onAboutDeveloperClick = { navController.navigate(Destination.AboutDeveloper) },
                    onCheckForUpdatesClick = { navController.navigate(Destination.Updater) },
                    onOpenSourceLicensesClick = { navController.navigate(Destination.OpenSourceLicenses) },
                    currentSong = playbackInfo.currentSong,
                    viewModel = profileViewModel
                )
            }
        }
    }
}
