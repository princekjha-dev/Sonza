package com.sonza.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.HomeItem
import com.sonza.app.core.model.HomeSection
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.*
import com.sonza.app.ui.theme.*
import com.sonza.app.ui.utils.LocalDeviceFormFactor
import com.sonza.app.ui.utils.animateEnter
import com.sonza.app.ui.viewmodel.HomeEvent
import com.sonza.app.ui.viewmodel.HomeViewModel
import com.sonza.app.ui.viewmodel.PlaylistManagementViewModel
import com.sonza.app.util.ImageUtils
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.compose.viewmodel.koinViewModel

/**
 * Completely rebuilt Home Screen around the editorial music-discovery architecture (Part P):
 * - Clean compact Header with Sonza identity.
 * - Dynamic Featured / Spotlight Hero at the top using real data.
 * - Category / Mood chips with zero emojis and Material Symbols.
 * - Compact horizontal content rails with high content density.
 * - Distinct visual patterns: Pattern A (Square cards), Pattern B (Compact rows),
 *   Pattern C (Chart cards), Pattern D (Video cards), Pattern E (Explore).
 * - Responsive card sizing, Manrope typography, and dynamic accent color tokens.
 * - Seamless insets for edge-to-edge, mini-player, and bottom navigation.
 */
@Composable
fun HomeScreen(
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onHistoryClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onExploreClick: (String, String) -> Unit = { _, _ -> },
    onCreateMixClick: () -> Unit = {},
    currentSong: Song? = null,
    viewModel: HomeViewModel = koinViewModel(),
    playlistViewModel: PlaylistManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val sessionManager = remember { com.sonza.app.data.SessionManager(context) }
    val animatedBackgroundEnabled by sessionManager.playerAnimatedBackgroundFlow.collectAsStateWithLifecycle(initialValue = true)
    val isAlbumArtDynamicColorsEnabled by sessionManager.albumArtDynamicColorsEnabledFlow.collectAsStateWithLifecycle(initialValue = true)

    // Song Options Menu State
    var showSongMenu by remember { mutableStateOf(false) }
    var selectedSong: Song? by remember { mutableStateOf(null) }

    val onSongMoreClickHandler = remember {
        { song: Song ->
            selectedSong = song
            showSongMenu = true
        }
    }

    val playlistMgmtState by playlistViewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    // Lifecycle-aware event observation
    val homeLifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(homeLifecycleOwner) {
        homeLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.events.collect { event ->
                when (event) {
                    is HomeEvent.ShowAddToPlaylistSheet -> {
                        playlistViewModel.showAddToPlaylistSheet(event.song)
                    }
                    is HomeEvent.ScrollToTop -> {
                        if (uiState.homeSections.isNotEmpty() || uiState.recommendations.isNotEmpty()) {
                            lazyListState.animateScrollToItem(0)
                        }
                    }
                    is HomeEvent.Refresh -> {}
                }
            }
        }
    }

    // Playlist feedback messages
    LaunchedEffect(playlistMgmtState.successMessage, playlistMgmtState.errorMessage) {
        playlistMgmtState.successMessage?.let {
            com.sonza.app.util.SnackbarUtil.showMessage(it)
            playlistViewModel.clearMessages()
        }
        playlistMgmtState.errorMessage?.let {
            com.sonza.app.util.SnackbarUtil.showError(it)
            playlistViewModel.clearMessages()
        }
    }

    // Dynamic accent & dominant colors: only extract from active playing track
    val actualDominantColors = rememberDominantColors(
        imageUrl = currentSong?.thumbnailUrl
    )

    val dominantColors = if (isAlbumArtDynamicColorsEnabled && currentSong != null) {
        actualDominantColors
    } else {
        DominantColors(
            primary = SonzaSurface,
            secondary = SonzaSurfaceVariant,
            accent = SonzaIdleAccent,
            onBackground = SonzaOnBackground,
            isIdle = true
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fluid background
        if (animatedBackgroundEnabled) {
            MeshGradientBackground(dominantColors = dominantColors)
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SonzaBackground)
            )
        }

        // Home Content State Machine
        when {
            uiState.isLoading && uiState.homeSections.isEmpty() && uiState.recommendations.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    com.sonza.app.ui.components.SonzaLoadingIndicator(modifier = Modifier.size(72.dp))
                }
            }

            uiState.error != null && uiState.homeSections.isEmpty() && uiState.recommendations.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    SonzaErrorState(
                        title = "Couldn't load music",
                        message = uiState.error,
                        onRetry = { viewModel.refresh() }
                    )
                }
            }

            !uiState.isLoading && uiState.homeSections.isEmpty() && uiState.recommendations.isEmpty() && uiState.personalizedSections.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    contentAlignment = Alignment.Center
                ) {
                    SonzaEmptyState(
                        title = "Welcome to Sonza",
                        description = "Start listening to discover personalized recommendations and trending tracks.",
                        icon = Icons.Rounded.Search,
                        actionText = "Explore Music",
                        onActionClick = { onExploreClick("FEmusic_explore", "Explore") }
                    )
                }
            }

            else -> {
                // Infinite scroll detection for smooth discovery feed
                LaunchedEffect(lazyListState, uiState.isLoadingMore) {
                    snapshotFlow {
                        val layoutInfo = lazyListState.layoutInfo
                        val totalItems = layoutInfo.totalItemsCount
                        val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                        lastVisibleIndex >= totalItems - 6 && totalItems > 0 && !uiState.isLoadingMore
                    }
                        .distinctUntilChanged()
                        .filter { it }
                        .collectLatest {
                            viewModel.loadMore()
                        }
                }

                val pullRefreshState = rememberPullToRefreshState()

                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refresh() },
                    state = pullRefreshState,
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    indicator = {
                        if (uiState.isRefreshing) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                com.sonza.app.ui.components.SonzaLoadingIndicator(modifier = Modifier.size(36.dp))
                            }
                        }
                    }
                ) {
                    val formFactor = LocalDeviceFormFactor.current
                    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    val isMiniPlayerVisible = currentSong != null
                    val bottomSystemHeight = if (isMiniPlayerVisible) 132.dp else (if (formFactor.isPhoneLike) 64.dp else 0.dp)
                    val targetBottomPadding = navBarPadding + bottomSystemHeight + 20.dp

                    val animatedBottomPadding by animateDpAsState(
                        targetValue = targetBottomPadding,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "homeBottomContentPadding"
                    )

                    // Hero Content Derived from Real Available Data
                    val heroContent = remember(currentSong, uiState.recommendations, uiState.filteredSections, uiState.personalizedSections) {
                        when {
                            currentSong != null -> {
                                FeaturedHeroData(
                                    title = currentSong.title,
                                    subtitle = currentSong.artist,
                                    thumbnailUrl = currentSong.thumbnailUrl,
                                    tag = "NOW PLAYING",
                                    onClick = { onSongClick(listOf(currentSong), 0) }
                                )
                            }
                            uiState.recommendations.isNotEmpty() -> {
                                val song = uiState.recommendations.first()
                                FeaturedHeroData(
                                    title = song.title,
                                    subtitle = song.artist,
                                    thumbnailUrl = song.thumbnailUrl,
                                    tag = "MADE FOR YOU",
                                    onClick = { onSongClick(uiState.recommendations, 0) }
                                )
                            }
                            uiState.filteredSections.isNotEmpty() -> {
                                val firstItem = uiState.filteredSections.first().items.firstOrNull()
                                when (firstItem) {
                                    is HomeItem.SongItem -> FeaturedHeroData(
                                        title = firstItem.song.title,
                                        subtitle = firstItem.song.artist,
                                        thumbnailUrl = firstItem.song.thumbnailUrl,
                                        tag = "SPOTLIGHT",
                                        onClick = { onSongClick(listOf(firstItem.song), 0) }
                                    )
                                    is HomeItem.PlaylistItem -> FeaturedHeroData(
                                        title = firstItem.playlist.name,
                                        subtitle = firstItem.playlist.uploaderName,
                                        thumbnailUrl = firstItem.playlist.thumbnailUrl,
                                        tag = "FEATURED PLAYLIST",
                                        onClick = { onPlaylistClick(firstItem.playlist) }
                                    )
                                    is HomeItem.AlbumItem -> FeaturedHeroData(
                                        title = firstItem.album.title,
                                        subtitle = firstItem.album.artist,
                                        thumbnailUrl = firstItem.album.thumbnailUrl,
                                        tag = "FEATURED ALBUM",
                                        onClick = { onAlbumClick(firstItem.album) }
                                    )
                                    else -> null
                                }
                            }
                            uiState.personalizedSections.isNotEmpty() -> {
                                val firstItem = uiState.personalizedSections.first().items.firstOrNull()
                                when (firstItem) {
                                    is HomeItem.SongItem -> FeaturedHeroData(
                                        title = firstItem.song.title,
                                        subtitle = firstItem.song.artist,
                                        thumbnailUrl = firstItem.song.thumbnailUrl,
                                        tag = "RECOMMENDED",
                                        onClick = { onSongClick(listOf(firstItem.song), 0) }
                                    )
                                    is HomeItem.PlaylistItem -> FeaturedHeroData(
                                        title = firstItem.playlist.name,
                                        subtitle = firstItem.playlist.uploaderName,
                                        thumbnailUrl = firstItem.playlist.thumbnailUrl,
                                        tag = "RECOMMENDED PLAYLIST",
                                        onClick = { onPlaylistClick(firstItem.playlist) }
                                    )
                                    is HomeItem.AlbumItem -> FeaturedHeroData(
                                        title = firstItem.album.title,
                                        subtitle = firstItem.album.artist,
                                        thumbnailUrl = firstItem.album.thumbnailUrl,
                                        tag = "RECOMMENDED ALBUM",
                                        onClick = { onAlbumClick(firstItem.album) }
                                    )
                                    else -> null
                                }
                            }
                            else -> null
                        }
                    }

                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = animatedBottomPadding),
                        verticalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceXs)
                    ) {
                        // 1. Top Header
                        if (uiState.homeSectionsVisibility.contains("greeting")) {
                            item(key = "home_top_header", contentType = "header") {
                                HomeTopHeader(
                                    avatarUrl = uiState.userAvatarUrl,
                                    userName = uiState.userName,
                                    currentSong = currentSong,
                                    recentlyPlayed = uiState.recentlyPlayed,
                                    onProfileClick = onProfileClick,
                                    modifier = Modifier.animateEnter(index = 0)
                                )
                            }
                        }

                        // 2. Featured / Spotlight Hero Card (P4 & P5)
                        if (heroContent != null) {
                            item(key = "featured_hero", contentType = "hero") {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            horizontal = SpacingTokens.SpaceLg,
                                            vertical = SpacingTokens.SpaceXs
                                        )
                                        .animateEnter(index = 1)
                                ) {
                                    FeaturedHeroCard(
                                        title = heroContent.title,
                                        subtitle = heroContent.subtitle,
                                        thumbnailUrl = heroContent.thumbnailUrl,
                                        tag = heroContent.tag,
                                        onClick = heroContent.onClick
                                    )
                                }
                            }
                        }


                        // 4. Recently Played Dynamic Horizontal Carousel Section
                        if (uiState.recentlyPlayed.isNotEmpty()) {
                            item(key = "recently_played_section", contentType = "recently_played") {
                                val recentSongs = remember(uiState.recentlyPlayed) {
                                    uiState.recentlyPlayed.map { it.song }.distinctBy { it.id }
                                }
                                val configuration = LocalConfiguration.current
                                val screenWidth = configuration.screenWidthDp.dp
                                val cardWidth = remember(screenWidth) {
                                    ((screenWidth - 32.dp - 24.dp) / 2.35f).coerceIn(136.dp, 160.dp)
                                }

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateEnter(index = 3)
                                ) {
                                    HomeSectionHeader(
                                        title = "Recently Played",
                                        onSeeAllClick = onHistoryClick
                                    )

                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
                                    ) {
                                        itemsIndexed(
                                            items = recentSongs,
                                            key = { _, song -> "recent_${song.id}" }
                                        ) { index, song ->
                                            SquareMusicCard(
                                                title = song.title,
                                                subtitle = song.artist,
                                                thumbnailUrl = song.thumbnailUrl,
                                                onClick = { onSongClick(recentSongs, index) },
                                                onMoreClick = { onSongMoreClickHandler(song) },
                                                size = cardWidth
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Best New Songs / Quick Picks (Pattern B: Compact List Rows)
                        if (uiState.recommendations.isNotEmpty() && uiState.homeSectionsVisibility.contains("quick_picks")) {
                            item(key = "quick_picks_section", contentType = "quick_picks") {
                                CompactSongRowsSection(
                                    section = HomeSection(
                                        title = "Quick picks",
                                        items = uiState.recommendations.map { HomeItem.SongItem(it) }
                                    ),
                                    onSongClick = onSongClick,
                                    onPlaylistClick = onPlaylistClick,
                                    onAlbumClick = onAlbumClick,
                                    onSongMoreClick = onSongMoreClickHandler,
                                    modifier = Modifier.animateEnter(index = 4)
                                )
                            }
                        }

                        // 6. Recommended Artists (Last.fm)
                        if (uiState.recommendedArtists.isNotEmpty() && uiState.homeSectionsVisibility.contains("recommendations")) {
                            item(key = "recommended_artists", contentType = "artists") {
                                RecommendedArtistsSection(
                                    artists = uiState.recommendedArtists,
                                    modifier = Modifier.animateEnter(index = 5)
                                )
                            }
                        }

                        // 7. Recommended Tracks (Last.fm)
                        if (uiState.recommendedTracks.isNotEmpty() && uiState.homeSectionsVisibility.contains("recommendations")) {
                            item(key = "recommended_tracks", contentType = "tracks") {
                                RecommendedTracksSection(
                                    tracks = uiState.recommendedTracks,
                                    modifier = Modifier.animateEnter(index = 6)
                                )
                            }
                        }

                        // 8. Primary Content Sections Loop (YouTube / Backend Catalogue)
                        if (uiState.homeSectionsVisibility.contains("youtube_sections")) {
                            itemsIndexed(
                                items = uiState.filteredSections,
                                key = { _, section -> section.title },
                                contentType = { _, section -> section.type }
                            ) { index, section ->
                                RenderHomeSection(
                                    section = section,
                                    onSongClick = onSongClick,
                                    onPlaylistClick = onPlaylistClick,
                                    onAlbumClick = onAlbumClick,
                                    onExploreClick = onExploreClick,
                                    onSongMoreClick = onSongMoreClickHandler,
                                    modifier = Modifier.animateEnter(index = 7 + index)
                                )
                            }
                        }

                        // 9. Personalized Recommendation Sections
                        if (uiState.personalizedSections.isNotEmpty() && uiState.homeSectionsVisibility.contains("personalized")) {
                            itemsIndexed(
                                items = uiState.personalizedSections,
                                key = { _, section -> "personalized_${section.title}" },
                                contentType = { _, section -> "personalized_${section.type}" }
                            ) { index, section ->
                                RenderHomeSection(
                                    section = section,
                                    onSongClick = onSongClick,
                                    onPlaylistClick = onPlaylistClick,
                                    onAlbumClick = onAlbumClick,
                                    onExploreClick = onExploreClick,
                                    onSongMoreClick = onSongMoreClickHandler,
                                    modifier = Modifier.animateEnter(index = 20 + index)
                                )
                            }
                        }

                        // 10. Genre-Based Discovery Sections ("Because you like Pop", etc.)
                        if (uiState.genreSections.isNotEmpty() && uiState.homeSectionsVisibility.contains("genres")) {
                            itemsIndexed(
                                items = uiState.genreSections,
                                key = { _, section -> "genre_${section.title}" },
                                contentType = { _, section -> "genre_${section.type}" }
                            ) { index, section ->
                                HorizontalSquareCardsSection(
                                    section = section,
                                    onSongClick = onSongClick,
                                    onPlaylistClick = onPlaylistClick,
                                    onAlbumClick = onAlbumClick,
                                    onSongMoreClick = onSongMoreClickHandler,
                                    modifier = Modifier.animateEnter(index = 30 + index)
                                )
                            }
                        }

                        // 11. Context-Aware Sections (Time of day / Patterns)
                        if (uiState.contextSections.isNotEmpty() && uiState.homeSectionsVisibility.contains("contextual")) {
                            itemsIndexed(
                                items = uiState.contextSections,
                                key = { _, section -> "context_${section.title}" },
                                contentType = { _, section -> "context_${section.type}" }
                            ) { index, section ->
                                RenderHomeSection(
                                    section = section,
                                    onSongClick = onSongClick,
                                    onPlaylistClick = onPlaylistClick,
                                    onAlbumClick = onAlbumClick,
                                    onExploreClick = onExploreClick,
                                    onSongMoreClick = onSongMoreClickHandler,
                                    modifier = Modifier.animateEnter(index = 40 + index)
                                )
                            }
                        }

                        // 12. Detected Mood Banner
                        uiState.detectedMood?.let { mood ->
                            if (uiState.homeSectionsVisibility.contains("mood_banner")) {
                                item(key = "mood_banner", contentType = "mood_banner") {
                                    DetectedMoodBanner(
                                        mood = mood,
                                        onExplore = { onExploreClick("FEmusic_explore", mood) },
                                        modifier = Modifier
                                            .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)
                                            .animateEnter(index = 50)
                                    )
                                }
                            }
                        }

                        // 13. Create a Mix Action Card
                        if (uiState.homeSectionsVisibility.contains("create_mix")) {
                            item(key = "create_mix", contentType = "create_mix") {
                                Column(modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)) {
                                    CreateMixCard(onClick = onCreateMixClick)
                                }
                            }
                        }

                        // 14. Scroll-Loaded "More to Explore" Feed
                        if (uiState.moreSections.isNotEmpty() && uiState.homeSectionsVisibility.contains("youtube_sections")) {
                            itemsIndexed(
                                items = uiState.moreSections,
                                key = { _, section -> "more_${section.title}" },
                                contentType = { _, section -> "more_${section.type}" }
                            ) { index, section ->
                                RenderHomeSection(
                                    section = section,
                                    onSongClick = onSongClick,
                                    onPlaylistClick = onPlaylistClick,
                                    onAlbumClick = onAlbumClick,
                                    onExploreClick = onExploreClick,
                                    onSongMoreClick = onSongMoreClickHandler,
                                    modifier = Modifier.animateEnter(index = 60 + index)
                                )
                            }
                        }

                        // 15. Loading More Indicator
                        if (uiState.isLoadingMore) {
                            item(key = "loading_more", contentType = "loading_more") {
                                LoadingMoreIndicator(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = SpacingTokens.Space2Xl)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Song Options Menu Bottom Sheet
        selectedSong?.let { song ->
            SongMenuBottomSheet(
                isVisible = showSongMenu,
                onDismiss = { showSongMenu = false },
                song = song,
                isCurrentlyPlaying = song.id == currentSong?.id,
                onPlayNext = {
                    viewModel.playNext(song)
                    showSongMenu = false
                },
                onAddToQueue = {
                    viewModel.addToQueue(song)
                    showSongMenu = false
                },
                onAddToPlaylist = {
                    viewModel.addToPlaylist(song)
                    showSongMenu = false
                },
                onDownload = {
                    viewModel.downloadSong(song)
                    showSongMenu = false
                },
                onShare = {
                    val shareText = "🎵 ${song.title}\n🎤 ${song.artist}\n\nhttps://music.youtube.com/watch?v=${song.id}"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Song"))
                    showSongMenu = false
                }
            )
        }

        // Add to Playlist Sheet
        val playlistUiState by playlistViewModel.uiState.collectAsStateWithLifecycle()
        if (playlistUiState.showAddToPlaylistSheet && playlistUiState.selectedSongs.isNotEmpty()) {
            AddToPlaylistSheet(
                songs = playlistUiState.selectedSongs,
                isVisible = true,
                playlists = playlistUiState.userPlaylists,
                isLoading = playlistUiState.isLoadingPlaylists || playlistUiState.isAddingSong,
                onDismiss = { playlistViewModel.hideAddToPlaylistSheet() },
                onAddToPlaylist = { playlistId ->
                    playlistViewModel.addSongsToPlaylist(playlistId)
                },
                onCreateNewPlaylist = {
                    playlistViewModel.showCreatePlaylistDialog()
                }
            )
        }
    }
}

// -----------------------------------------------------------------------------
// Data Classes & Helper Components
// -----------------------------------------------------------------------------

private data class FeaturedHeroData(
    val title: String,
    val subtitle: String,
    val thumbnailUrl: String?,
    val tag: String,
    val onClick: () -> Unit
)


@Composable
private fun DetectedMoodBanner(
    mood: String,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dynamicColors = LocalSonzaDynamicColors.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onExplore),
        shape = RoundedCornerShape(RadiusTokens.Lg),
        color = SonzaSurfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(dynamicColors.accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                EqualizerGlyph(
                    barColor = dynamicColors.accent,
                    barCount = 4,
                    height = 18.dp,
                    barWidth = 2.5.dp
                )
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Feeling $mood?",
                    style = SonzaTypography.CardTitle,
                    color = SonzaOnBackground
                )
                Text(
                    text = "Tap to explore music for this mood",
                    style = SonzaTypography.CardSubtitle,
                    color = SonzaOnSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(RadiusTokens.Pill),
                color = dynamicColors.accent.copy(alpha = 0.14f)
            ) {
                Text(
                    text = "Explore",
                    style = SonzaTypography.LabelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = dynamicColors.accent,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateMixCard(onClick: () -> Unit) {
    val dynamicColors = LocalSonzaDynamicColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick),
        shape = RoundedCornerShape(RadiusTokens.Lg),
        color = SonzaSurfaceVariant,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = SpacingTokens.SpaceLg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(dynamicColors.accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = dynamicColors.onAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            Column {
                Text(
                    text = "Create your own mix",
                    style = SonzaTypography.CardTitle,
                    color = SonzaOnBackground
                )
                Text(
                    text = "Pick artists and genres to get started",
                    style = SonzaTypography.CardSubtitle,
                    color = SonzaOnSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun LoadingMoreIndicator(modifier: Modifier = Modifier) {
    val dynamicColors = LocalSonzaDynamicColors.current

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            com.sonza.app.ui.components.SonzaLoadingIndicator(
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Loading more for you...",
                style = SonzaTypography.Metadata,
                color = SonzaOnSurfaceVariant
            )
        }
    }
}

@Composable
private fun RecommendedArtistsSection(
    artists: List<com.sonza.app.lastfm.RecommendedArtist>,
    modifier: Modifier = Modifier,
    onArtistClick: (String) -> Unit = {}
) {
    if (artists.isEmpty()) return

    Column(modifier = modifier) {
        HomeSectionHeader(title = "Recommended Artists")

        LazyRow(
            contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            val uniqueArtists = artists.distinctBy { it.name }
            items(uniqueArtists, key = { it.name }) { artist ->
                ArtistCircleCard(artist = artist, onClick = { onArtistClick(artist.name) })
            }
        }
    }
}

@Composable
private fun ArtistCircleCard(
    artist: com.sonza.app.lastfm.RecommendedArtist,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageUrl = artist.image.lastOrNull()?.url ?: ""
    val highResUrl = remember(imageUrl) {
        ImageUtils.getHighResThumbnailUrl(imageUrl, size = 320) ?: imageUrl
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(105.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(96.dp),
            shape = CircleShape,
            color = SonzaSurfaceVariant,
            tonalElevation = 1.dp
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(highResUrl)
                    .crossfade(true)
                    .size(240)
                    .build(),
                contentDescription = artist.name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        Text(
            text = artist.name,
            style = SonzaTypography.CardTitle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            color = SonzaOnBackground
        )
    }
}

@Composable
private fun RecommendedTracksSection(
    tracks: List<com.sonza.app.lastfm.RecommendedTrack>,
    modifier: Modifier = Modifier,
    onTrackClick: (com.sonza.app.lastfm.RecommendedTrack) -> Unit = {}
) {
    if (tracks.isEmpty()) return

    Column(modifier = modifier) {
        HomeSectionHeader(title = "Recommended Tracks")

        LazyRow(
            contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
            horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
        ) {
            val uniqueTracks = tracks.distinctBy { it.name }
            items(uniqueTracks, key = { it.name }) { track ->
                TrackCard(track = track, onClick = { onTrackClick(track) })
            }
        }
    }
}

@Composable
private fun TrackCard(
    track: com.sonza.app.lastfm.RecommendedTrack,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val dynamicColors = LocalSonzaDynamicColors.current
    val imageUrl = track.image.lastOrNull()?.url ?: ""
    val highResUrl = remember(imageUrl) {
        ImageUtils.getHighResThumbnailUrl(imageUrl, size = 256) ?: imageUrl
    }

    Row(
        modifier = Modifier
            .width(260.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(RadiusTokens.Lg))
            .background(SonzaSurfaceVariant)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
            .padding(SpacingTokens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(highResUrl)
                .crossfade(true)
                .size(160)
                .build(),
            contentDescription = track.name,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(RadiusTokens.Sm))
                .background(SonzaSurfaceVariant),
            contentScale = ContentScale.Crop
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.name,
                style = SonzaTypography.SongTitle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SonzaOnBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist.name,
                style = SonzaTypography.ArtistSubtitle,
                color = SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(dynamicColors.accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = dynamicColors.accent,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun EqualizerGlyph(
    barColor: Color,
    barCount: Int,
    height: androidx.compose.ui.unit.Dp,
    barWidth: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    val phases = remember(barCount) {
        when (barCount) {
            3 -> floatArrayOf(0f, 0.33f, 0.66f)
            4 -> floatArrayOf(0f, 0.25f, 0.5f, 0.75f)
            5 -> floatArrayOf(0f, 0.2f, 0.6f, 0.4f, 0.8f)
            else -> FloatArray(barCount) { it.toFloat() / barCount }
        }
    }
    val durations = remember(barCount) {
        when (barCount) {
            3 -> intArrayOf(620, 540, 700)
            4 -> intArrayOf(620, 540, 700, 580)
            5 -> intArrayOf(620, 540, 700, 580, 660)
            else -> IntArray(barCount) { 580 + (it % 3) * 60 }
        }
    }

    Row(
        modifier = modifier.height(height),
        horizontalArrangement = Arrangement.spacedBy(barWidth * 0.7f),
        verticalAlignment = Alignment.Bottom
    ) {
        for (i in 0 until barCount) {
            val anim by transition.animateFloat(
                initialValue = 0.25f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = durations[i],
                        easing = FastOutSlowInEasing,
                        delayMillis = (phases[i] * 400f).toInt()
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar$i"
            )
            Box(
                modifier = Modifier
                    .width(barWidth)
                    .fillMaxHeight(anim)
                    .clip(RoundedCornerShape(50))
                    .background(barColor)
            )
        }
    }
}
