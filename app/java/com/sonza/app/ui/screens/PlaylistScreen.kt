package com.sonza.app.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SortOrder
import com.sonza.app.core.model.SortType
import com.sonza.app.ui.components.*
import com.sonza.app.ui.theme.*
import com.sonza.app.ui.viewmodel.PlaylistManagementViewModel
import com.sonza.app.ui.viewmodel.PlaylistViewModel
import com.sonza.app.util.ImageUtils
import com.sonza.app.util.SnackbarUtil
import com.sonza.app.util.TimeUtil
import org.koin.compose.viewmodel.koinViewModel

/**
 * Premium, immersive Sonza Playlist Screen.
 * Features:
 * - Dynamic color extraction from artwork (background wash, ambient hero glow, accent highlights)
 * - Immersive hero artwork with rounded squircle geometry and smooth gradient blending
 * - Translucent blurred top bar controls overlaying hero content with collapsing title animation
 * - Prominent Play (high-contrast pill) and circular Shuffle / Save action row
 * - Expandable playlist description with smooth expand/collapse transitions
 * - Clean, high-density track list with animated equalizers for currently playing track
 * - Multi-selection mode, drag-and-drop reordering, sort filter dropdown, and state handling
 */
@Composable
fun PlaylistScreen(
    onBackClick: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlayAll: (List<Song>) -> Unit = {},
    onShufflePlay: (List<Song>) -> Unit = {},
    onAddSongsClick: () -> Unit = {},
    currentSong: Song? = null,
    viewModel: PlaylistViewModel = koinViewModel(),
    playlistMgmtViewModel: PlaylistManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val batchProgress by viewModel.batchProgress.collectAsState()
    val context = LocalContext.current
    val playlist = uiState.playlist

    // Extract dynamic accent colors derived from active playlist artwork
    val dynamicColors = rememberDynamicAccentColors(playlist?.thumbnailUrl)

    // Track scroll position for collapsing header transitions
    val listState = rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 180
        }
    }

    // Scroll Direction Tracking for TopBar visibility
    var isScrollingDown by remember { mutableStateOf(false) }
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow {
            Pair(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset)
        }.collect { (currentIndex, currentOffset) ->
            if (currentIndex > previousIndex) {
                isScrollingDown = true
            } else if (currentIndex < previousIndex) {
                isScrollingDown = false
            } else {
                if (currentOffset > previousScrollOffset + 12) {
                    isScrollingDown = true
                } else if (currentOffset < previousScrollOffset - 12) {
                    isScrollingDown = false
                }
            }
            previousIndex = currentIndex
            previousScrollOffset = currentOffset
        }
    }

    val isTopBarVisible = !isScrolled || !isScrollingDown

    // Dialog & Bottom Sheet States
    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showMediaMenu by remember { mutableStateOf(false) }
    var showSongMenu by remember { mutableStateOf(false) }
    var selectedSong: Song? by remember { mutableStateOf(null) }

    // Selection mode cleanup when leaving screen
    val currentViewModel by rememberUpdatedState(viewModel)
    DisposableEffect(Unit) {
        onDispose {
            currentViewModel.clearSelection()
        }
    }

    BackHandler(enabled = uiState.isSelectionMode) {
        viewModel.clearSelection()
    }

    // Handle messages from ViewModel
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        uiState.successMessage?.let {
            SnackbarUtil.showMessage(it)
            viewModel.clearMessages()
        }
        uiState.errorMessage?.let {
            SnackbarUtil.showError(it)
            viewModel.clearMessages()
        }
    }

    // Handle delete success
    LaunchedEffect(uiState.deleteSuccess) {
        if (uiState.deleteSuccess) {
            onBackClick()
        }
    }

    val sharePlaylist: (Playlist) -> Unit = { playlistToShare ->
        val shareText = "Check out this playlist: ${playlistToShare.title} by ${playlistToShare.author}\n\nhttps://music.youtube.com/playlist?list=${playlistToShare.id}"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Playlist")
        context.startActivity(shareIntent)
    }

    val shareSong: (Song) -> Unit = { song ->
        val shareText = "Check out this song: ${song.title} by ${song.artist}\n\nhttps://music.youtube.com/watch?v=${song.id}"
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Song")
        context.startActivity(shareIntent)
    }

    CompositionLocalProvider(LocalSonzaDynamicColors provides dynamicColors) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SonzaBackground)
        ) {
            // Immersive Ambient Background Blur & Gradient Wash
            if (playlist?.thumbnailUrl != null) {
                val blurredHighRes = remember(playlist.thumbnailUrl) {
                    ImageUtils.getHighResThumbnailUrl(playlist.thumbnailUrl, size = 720) ?: playlist.thumbnailUrl
                }
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(blurredHighRes)
                        .crossfade(true)
                        .size(720)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(90.dp),
                    contentScale = ContentScale.Crop,
                    alpha = 0.38f
                )

                // Deep gradient transition overlay from artwork into AMOLED base
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to dynamicColors.accentMuted.copy(alpha = 0.40f),
                                0.25f to dynamicColors.accentMuted.copy(alpha = 0.15f),
                                0.55f to SonzaBackground.copy(alpha = 0.85f),
                                1.0f to SonzaBackground
                            )
                        )
                )
            } else {
                // Subtle ambient glow fallback when no artwork exists
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to dynamicColors.accentMuted.copy(alpha = 0.25f),
                                0.40f to SonzaBackground.copy(alpha = 0.90f),
                                1.0f to SonzaBackground
                            )
                        )
                )
            }

            // Main Content Area
            if (uiState.isLoading && playlist == null) {
                PlaylistSkeleton(onBackClick = onBackClick)
            } else if (uiState.error != null && (playlist == null || playlist.songs.isEmpty())) {
                SonzaErrorState(
                    title = "Couldn't load this playlist",
                    message = uiState.error,
                    onRetry = { viewModel.refreshPlaylist() },
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (playlist != null) {
                PullToRefreshBox(
                    isRefreshing = uiState.isRefreshing,
                    onRefresh = { viewModel.refreshPlaylist() },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(
                            top = 0.dp,
                            bottom = 128.dp // Space for floating mini player + bottom nav
                        ),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // Immersive Hero Header
                        item(key = "playlist_hero_header") {
                            PlaylistHeroHeader(
                                playlist = playlist,
                                batchProgress = batchProgress,
                                isSaved = uiState.isSaved,
                                sortType = uiState.sortType,
                                sortOrder = uiState.sortOrder,
                                dynamicColors = dynamicColors,
                                onSortChange = viewModel::setSort,
                                onToggleSortOrder = viewModel::toggleSortOrder,
                                onPlayAll = { onPlayAll(playlist.songs) },
                                onShufflePlay = { onShufflePlay(playlist.songs) },
                                onToggleSave = {
                                    viewModel.toggleSaveToLibrary()
                                    val message = if (uiState.isSaved) "Removed from Library" else "Saved to Library"
                                    SnackbarUtil.showMessage(message)
                                },
                                isLoadingMore = uiState.isLoadingMore,
                                totalSongCount = uiState.totalSongCount
                            )
                        }

                        // Empty State or Song List
                        if (playlist.songs.isEmpty()) {
                            item(key = "playlist_empty_state") {
                                SonzaEmptyState(
                                    title = "No songs in this playlist yet",
                                    description = "Add songs to start listening",
                                    icon = Icons.Default.MusicNote,
                                    actionText = "Add Songs",
                                    onActionClick = onAddSongsClick,
                                    modifier = Modifier.padding(vertical = 32.dp)
                                )
                            }
                        } else {
                            // Song List Rows
                            itemsIndexed(
                                items = playlist.songs,
                                key = { _, song -> song.setVideoId ?: song.id }
                            ) { index, song ->
                                val isSelected = uiState.selectedSongIds.contains(song.setVideoId ?: song.id)
                                val isCurrentlyPlaying = song.id == currentSong?.id

                                PlaylistTrackRow(
                                    song = song,
                                    index = index,
                                    totalSongs = playlist.songs.size,
                                    isCurrentlyPlaying = isCurrentlyPlaying,
                                    isSelected = isSelected,
                                    isSelectionMode = uiState.isSelectionMode,
                                    isEditable = uiState.isEditable,
                                    dynamicColors = dynamicColors,
                                    onClick = {
                                        if (uiState.isSelectionMode) {
                                            viewModel.toggleSongSelection(song)
                                        } else {
                                            onSongClick(playlist.songs, index)
                                        }
                                    },
                                    onLongClick = {
                                        if (!uiState.isSelectionMode) {
                                            viewModel.toggleSongSelection(song)
                                        }
                                    },
                                    onMoreClick = {
                                        selectedSong = song
                                        showSongMenu = true
                                    },
                                    onReorder = { from, to ->
                                        viewModel.reorderSong(from, to)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Translucent Floating Top Bar Overlay
            AnimatedVisibility(
                visible = isTopBarVisible,
                enter = slideInVertically(
                    initialOffsetY = { -it },
                    animationSpec = tween(MotionTokens.DurationMedium2, easing = FastOutSlowInEasing)
                ) + fadeIn(),
                exit = slideOutVertically(
                    targetOffsetY = { -it },
                    animationSpec = tween(MotionTokens.DurationMedium2, easing = FastOutSlowInEasing)
                ) + fadeOut(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .zIndex(10f)
            ) {
                if (uiState.isSelectionMode) {
                    SelectionTopBar(
                        selectedCount = uiState.selectedSongIds.size,
                        onCloseClick = { viewModel.clearSelection() },
                        onDeleteClick = { viewModel.removeSelectedSongs() },
                        onPlayNextClick = { viewModel.playNextSelectedSongs() },
                        onAddToQueueClick = { viewModel.addToQueueSelectedSongs() },
                        onAddToPlaylistClick = {
                            val selectedSongs = playlist?.songs?.filter { (it.setVideoId ?: it.id) in uiState.selectedSongIds } ?: emptyList()
                            selectedSong = null
                            playlistMgmtViewModel.showAddToPlaylistSheet(selectedSongs)
                        },
                        onMoveToTopClick = { viewModel.moveSelectedSongs(0) }
                    )
                } else {
                    PlaylistTopBar(
                        title = playlist?.title.orEmpty(),
                        isScrolled = isScrolled,
                        onBackClick = onBackClick,
                        onShareClick = { playlist?.let { sharePlaylist(it) } },
                        onMoreClick = { showMediaMenu = true },
                        showShare = playlist?.id != "CACHED_ALL" && playlist?.id != "DEVICE_SONGS"
                    )
                }
            }

            // Playlist Media Menu Bottom Sheet
            if (showMediaMenu && playlist != null) {
                MediaMenuBottomSheet(
                    isVisible = showMediaMenu,
                    onDismiss = { showMediaMenu = false },
                    title = playlist.title,
                    subtitle = "${playlist.songs.size} songs",
                    thumbnailUrl = playlist.thumbnailUrl,
                    onShuffle = { onShufflePlay(playlist.songs) },
                    onStartRadio = { onShufflePlay(playlist.songs) },
                    onPlayNext = { viewModel.playNext(playlist.songs) },
                    onAddToQueue = { viewModel.addToQueue(playlist.songs) },
                    onAddToPlaylist = {
                        if (playlist.songs.isNotEmpty()) {
                            playlistMgmtViewModel.showAddToPlaylistSheet(playlist.songs)
                        }
                    },
                    onDownload = { viewModel.downloadPlaylist(playlist) },
                    onShare = { sharePlaylist(playlist) },
                    onExport = { showExportDialog = true },
                    onRename = { showRenameDialog = true },
                    onDelete = { showDeleteDialog = true },
                    showShare = playlist.id != "CACHED_ALL" && playlist.id != "DEVICE_SONGS"
                )
            }

            // Single Song Menu Bottom Sheet
            if (showSongMenu && selectedSong != null) {
                val song = selectedSong!!
                SongMenuBottomSheet(
                    isVisible = showSongMenu,
                    onDismiss = { showSongMenu = false },
                    song = song,
                    isCurrentlyPlaying = song.id == currentSong?.id,
                    onPlayNext = { viewModel.playNext(song) },
                    onAddToQueue = { viewModel.addToQueue(song) },
                    onAddToPlaylist = { playlistMgmtViewModel.showAddToPlaylistSheet(song) },
                    onDownload = { viewModel.downloadSong(song) },
                    onShare = { shareSong(song) },
                    onRemoveFromPlaylist = if (uiState.isEditable) {
                        { viewModel.removeSongFromPlaylist(song) }
                    } else null,
                    onMoveUp = if (uiState.isEditable && (playlist?.songs?.indexOf(song) ?: -1) > 0) {
                        {
                            val currentIndex = playlist?.songs?.indexOf(song) ?: -1
                            if (currentIndex > 0) {
                                viewModel.reorderSong(currentIndex, currentIndex - 1)
                            }
                        }
                    } else null,
                    onMoveDown = if (uiState.isEditable && (playlist?.songs?.indexOf(song) ?: -1) != -1 && (playlist?.songs?.indexOf(song) ?: -1) < (playlist?.songs?.size ?: 0) - 1) {
                        {
                            val currentIndex = playlist?.songs?.indexOf(song) ?: -1
                            if (currentIndex != -1 && currentIndex < (playlist?.songs?.size ?: 0) - 1) {
                                viewModel.reorderSong(currentIndex, currentIndex + 1)
                            }
                        }
                    } else null,
                    showShare = playlist?.id != "CACHED_ALL" && playlist?.id != "DEVICE_SONGS"
                )
            }

            // Global Add-to-Playlist Sheet
            val playlistMgmtState by playlistMgmtViewModel.uiState.collectAsState()

            LaunchedEffect(playlistMgmtState.successMessage) {
                if (playlistMgmtState.successMessage != null) {
                    viewModel.refreshPlaylist()
                    playlistMgmtViewModel.clearMessages()
                }
            }

            if (playlistMgmtState.showAddToPlaylistSheet && playlistMgmtState.selectedSongs.isNotEmpty()) {
                AddToPlaylistSheet(
                    songs = playlistMgmtState.selectedSongs,
                    isVisible = playlistMgmtState.showAddToPlaylistSheet,
                    playlists = playlistMgmtState.userPlaylists,
                    isLoading = playlistMgmtState.isLoadingPlaylists,
                    onDismiss = { playlistMgmtViewModel.hideAddToPlaylistSheet() },
                    onAddToPlaylist = { playlistId -> playlistMgmtViewModel.addSongsToPlaylist(playlistId) },
                    onCreateNewPlaylist = { playlistMgmtViewModel.showCreatePlaylistDialog() }
                )
            }

            // Dialogs
            if (showCreateDialog || playlistMgmtState.showCreatePlaylistDialog) {
                CreatePlaylistDialog(
                    isVisible = showCreateDialog || playlistMgmtState.showCreatePlaylistDialog,
                    isCreating = uiState.isCreating || playlistMgmtState.isCreatingPlaylist,
                    onDismiss = {
                        showCreateDialog = false
                        playlistMgmtViewModel.hideCreatePlaylistDialog()
                    },
                    onCreate = { title, desc, isPrivate, syncWithYt ->
                        if (showCreateDialog) {
                            viewModel.createPlaylist(title, desc, isPrivate, syncWithYt)
                            showCreateDialog = false
                        } else {
                            playlistMgmtViewModel.createPlaylist(title, desc, isPrivate, syncWithYt)
                        }
                    },
                    isLoggedIn = uiState.isLoggedIn
                )
            }

            if (showRenameDialog && playlist != null) {
                RenamePlaylistDialog(
                    isVisible = showRenameDialog,
                    currentName = playlist.title,
                    isRenaming = uiState.isRenaming,
                    onDismiss = { showRenameDialog = false },
                    onRename = { newName ->
                        viewModel.renamePlaylist(newName)
                        showRenameDialog = false
                    }
                )
            }

            if (showDeleteDialog && playlist != null) {
                DeletePlaylistDialog(
                    isVisible = showDeleteDialog,
                    playlistTitle = playlist.title,
                    isDeleting = uiState.isDeleting,
                    onDismiss = { showDeleteDialog = false },
                    onDelete = {
                        viewModel.deletePlaylist()
                    }
                )
            }

            if (showExportDialog && playlist != null) {
                ExportPlaylistDialog(
                    isVisible = showExportDialog,
                    onDismiss = { showExportDialog = false },
                    onExportM3U = { viewModel.exportPlaylistToM3U(context) },
                    onExportSonza = { viewModel.exportPlaylistToSonza(context) }
                )
            }
        }
    }
}

/**
 * Translucent Top Navigation Bar overlaying the playlist hero section.
 */
@Composable
private fun PlaylistTopBar(
    title: String,
    isScrolled: Boolean,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onMoreClick: () -> Unit,
    showShare: Boolean
) {
    val barBackground by animateColorAsState(
        targetValue = if (isScrolled) SonzaSurface.copy(alpha = 0.92f) else Color.Transparent,
        animationSpec = tween(MotionTokens.DurationMedium2, easing = FastOutSlowInEasing),
        label = "TopBarBackground"
    )

    val shadowElevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isScrolled) ElevationTokens.Level2 else 0.dp,
        label = "TopBarElevation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(shadowElevation),
        color = barBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Translucent Frosted Back Button
            TranslucentCircularButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick
            )

            // Collapsing Header Title
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = isScrolled,
                    enter = fadeIn(animationSpec = tween(MotionTokens.DurationMedium2)),
                    exit = fadeOut(animationSpec = tween(MotionTokens.DurationMedium2))
                ) {
                    Text(
                        text = title,
                        style = SonzaTypography.SongTitle.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = SonzaOnBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Trailing Actions (Share + More Options)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showShare) {
                    TranslucentCircularButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Share",
                        onClick = onShareClick
                    )
                }

                TranslucentCircularButton(
                    icon = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    onClick = onMoreClick
                )
            }
        }
    }
}

/**
 * Translucent frosted glass circular button for top navigation controls.
 */
@Composable
private fun TranslucentCircularButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    BounceButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        },
        size = 40.dp,
        shape = CircleShape,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f))
                .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = SonzaOnBackground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Hero section displaying artwork, title, creator, metadata, description, and primary actions.
 */
@Composable
private fun PlaylistHeroHeader(
    playlist: Playlist,
    batchProgress: Pair<Int, Int>,
    isSaved: Boolean,
    sortType: SortType,
    sortOrder: SortOrder,
    dynamicColors: SonzaDynamicColors,
    onSortChange: (SortType) -> Unit,
    onToggleSortOrder: () -> Unit,
    onPlayAll: () -> Unit,
    onShufflePlay: () -> Unit,
    onToggleSave: () -> Unit,
    isLoadingMore: Boolean,
    totalSongCount: Int?
) {
    val (current, total) = batchProgress
    val isDownloading = total > 0 && current < total
    var showSortMenu by remember { mutableStateOf(false) }
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 56.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large Centered Artwork with Ambient Glow
        Box(
            modifier = Modifier
                .size(240.dp)
                .shadow(
                    elevation = 28.dp,
                    shape = RoundedCornerShape(RadiusTokens.Lg),
                    spotColor = dynamicColors.accent.copy(alpha = 0.55f),
                    ambientColor = dynamicColors.accent.copy(alpha = 0.25f)
                )
                .clip(RoundedCornerShape(RadiusTokens.Lg))
                .background(SonzaSurfaceVariant)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(RadiusTokens.Lg)),
            contentAlignment = Alignment.Center
        ) {
            if (!playlist.thumbnailUrl.isNullOrBlank()) {
                val coverHighRes = remember(playlist.thumbnailUrl) {
                    ImageUtils.getHighResThumbnailUrl(playlist.thumbnailUrl, size = 720) ?: playlist.thumbnailUrl
                }
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(coverHighRes)
                        .crossfade(true)
                        .size(720)
                        .build(),
                    contentDescription = playlist.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Sleek Fallback Visual
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    dynamicColors.accent.copy(alpha = 0.7f),
                                    SonzaSurfaceVariant
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = dynamicColors.onAccent,
                        modifier = Modifier.size(72.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Playlist Title
        Text(
            text = playlist.title,
            style = SonzaTypography.Headline.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            ),
            color = SonzaOnBackground,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Creator / Artist Subtitle
        if (playlist.author.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = playlist.author,
                style = SonzaTypography.ArtistSubtitle.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = SonzaOnSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Metadata: Type • Songs • Total Duration
        val countAndDuration = remember(playlist.songs) {
            TimeUtil.formatSongCountAndDuration(playlist.songs)
        }
        Text(
            text = "Playlist • $countAndDuration",
            style = SonzaTypography.Metadata.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            ),
            color = SonzaOnSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        // Staged Loading Progress (for large remote playlists loading incrementally)
        if (isLoadingMore) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(PillShape)
                    .background(dynamicColors.accentMuted.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = dynamicColors.accent
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Loading more songs... (${playlist.songs.size}${totalSongCount?.let { " / $it" } ?: ""})",
                    style = SonzaTypography.Metadata.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = dynamicColors.accent
                )
            }
        }

        // Batch Download Progress
        if (isDownloading) {
            Spacer(modifier = Modifier.height(12.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Downloading $current / $total",
                    style = SonzaTypography.Metadata.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = SonzaOnBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { if (total > 0) current.toFloat() / total.toFloat() else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(PillShape),
                    color = dynamicColors.accent,
                    trackColor = SonzaSurfaceVariant
                )
            }
        }

        // Expandable Description
        val description = playlist.description
        if (!description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .animateContentSize(animationSpec = tween(MotionTokens.DurationMedium2)),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = description,
                    style = SonzaTypography.BodyMedium.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = SonzaOnSurfaceVariant.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    maxLines = if (isDescriptionExpanded) Int.MAX_VALUE else 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (description.length > 80) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isDescriptionExpanded) "LESS" else "MORE",
                        style = SonzaTypography.Metadata.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = dynamicColors.accent,
                        modifier = Modifier
                            .clip(PillShape)
                            .clickable { isDescriptionExpanded = !isDescriptionExpanded }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Primary Action Row: [ 🔀 Shuffle ]  [ ▶ Play ]  [ + / ✔ Save ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Secondary Shuffle Button (Circular)
            BounceButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onShufflePlay()
                },
                size = 52.dp,
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = SonzaOnBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Primary Play Button (High-contrast Pill)
            BounceButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onPlayAll()
                },
                shape = PillShape,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(PillShape)
                        .background(Color.White)
                        .shadow(ElevationTokens.Level2, PillShape),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Play",
                            style = SonzaTypography.SongTitle.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Secondary Save/Library Button (Circular)
            if (playlist.id != "LM") {
                BounceButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSave()
                    },
                    size = 52.dp,
                    shape = CircleShape
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(if (isSaved) dynamicColors.accent.copy(alpha = 0.18f) else Color.White.copy(alpha = 0.12f))
                            .border(
                                width = 1.dp,
                                color = if (isSaved) dynamicColors.accent.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.14f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = if (isSaved) "Remove from Library" else "Save to Library",
                            tint = if (isSaved) dynamicColors.accent else SonzaOnBackground,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sort Chip & Ordering Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                Row(
                    modifier = Modifier
                        .clip(SquircleShape)
                        .background(SonzaSurfaceVariant.copy(alpha = 0.6f))
                        .border(1.dp, SonzaOutline.copy(alpha = 0.4f), SquircleShape)
                        .clickable { showSortMenu = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = dynamicColors.accent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = sortType.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                        style = SonzaTypography.Metadata.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = dynamicColors.accent
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false },
                    modifier = Modifier
                        .background(SonzaSurface)
                        .border(1.dp, SonzaOutline, SquircleShape),
                    shape = SquircleShape
                ) {
                    SortType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (sortType == type) FontWeight.Bold else FontWeight.Normal,
                                    color = if (sortType == type) dynamicColors.accent else SonzaOnBackground
                                )
                            },
                            onClick = {
                                onSortChange(type)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                if (sortType == type) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = dynamicColors.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            if (sortType != SortType.CUSTOM) {
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = onToggleSortOrder,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (sortOrder == SortOrder.ASCENDING) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = "Toggle sort order",
                        tint = dynamicColors.accent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * High-density, clean track list row with live playing equalizer animation, reorder support, and touch feedback.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LazyItemScope.PlaylistTrackRow(
    song: Song,
    index: Int,
    totalSongs: Int,
    isCurrentlyPlaying: Boolean,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isEditable: Boolean,
    dynamicColors: SonzaDynamicColors,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit,
    onReorder: (from: Int, to: Int) -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val context = LocalContext.current

    val currentIndexState by rememberUpdatedState(index)
    val onReorderState by rememberUpdatedState(onReorder)

    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isDragging) 1.04f else 1f,
        label = "DragScale"
    )

    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isDragging) ElevationTokens.Level3 else 0.dp,
        label = "DragElevation"
    )

    val rowBackground by animateColorAsState(
        targetValue = when {
            isDragging -> SonzaSurface
            isSelected -> dynamicColors.accentMuted.copy(alpha = 0.30f)
            isCurrentlyPlaying -> dynamicColors.accentMuted.copy(alpha = 0.15f)
            else -> Color.Transparent
        },
        label = "TrackRowBackground"
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .animateItem(
                    placementSpec = if (isDragging) null else spring(
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                )
                .graphicsLayer {
                    this.translationY = offsetY
                    this.scaleX = scale
                    this.scaleY = scale
                    this.shadowElevation = with(density) { elevation.toPx() }
                    this.clip = true
                    this.shape = SquircleShape
                }
                .clip(SquircleShape)
                .background(rowBackground)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongClick()
                    }
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Selection Checkbox / Animated Equalizer / Track Number
            Box(
                modifier = Modifier.width(32.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onClick() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = dynamicColors.accent,
                            checkmarkColor = dynamicColors.onAccent,
                            uncheckedColor = SonzaOnSurfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                } else if (isCurrentlyPlaying) {
                    NowPlayingAnimation(
                        color = dynamicColors.accent,
                        isPlaying = true,
                        barCount = 3,
                        barWidth = 3.5.dp,
                        maxBarHeight = 16.dp,
                        minBarHeight = 3.dp
                    )
                } else {
                    Text(
                        text = "${index + 1}",
                        style = SonzaTypography.Metadata.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = SonzaOnSurfaceVariant.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Thumbnail (Compact 44dp Squircle)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SonzaSurfaceVariant)
            ) {
                if (!song.thumbnailUrl.isNullOrBlank()) {
                    val rowThumbnail = remember(song.thumbnailUrl) {
                        ImageUtils.getHighResThumbnailUrl(song.thumbnailUrl, size = 160) ?: song.thumbnailUrl
                    }
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(rowThumbnail)
                            .crossfade(true)
                            .size(160)
                            .build(),
                        contentDescription = song.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = SonzaOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Song Info (Title & Artist)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = SonzaTypography.SongTitle.copy(
                        fontWeight = if (isCurrentlyPlaying) FontWeight.Bold else FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = if (isCurrentlyPlaying) dynamicColors.accent else SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = song.artist,
                    style = SonzaTypography.ArtistSubtitle.copy(
                        fontSize = 13.sp
                    ),
                    color = SonzaOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Actions: Reorder Drag Handle (if editable) & 3-Dot More Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isEditable && !isSelectionMode) {
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Reorder",
                        tint = SonzaOnSurfaceVariant.copy(alpha = 0.40f),
                        modifier = Modifier
                            .size(36.dp)
                            .padding(8.dp)
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = {
                                        isDragging = true
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        offsetY = 0f
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                        offsetY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offsetY += dragAmount.y
                                        val threshold = with(density) { 56.dp.toPx() }
                                        var workingIndex = currentIndexState
                                        while (offsetY > threshold && workingIndex < totalSongs - 1) {
                                            onReorderState(workingIndex, workingIndex + 1)
                                            workingIndex += 1
                                            offsetY -= threshold
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                        while (offsetY < -threshold && workingIndex > 0) {
                                            onReorderState(workingIndex, workingIndex - 1)
                                            workingIndex -= 1
                                            offsetY += threshold
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        }
                                    }
                                )
                            }
                    )
                }

                if (!isSelectionMode) {
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = SonzaOnSurfaceVariant.copy(alpha = 0.75f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Minimal subtle track separator
        HorizontalDivider(
            modifier = Modifier.padding(start = 58.dp, end = 16.dp),
            thickness = 0.5.dp,
            color = SonzaOutline.copy(alpha = 0.22f)
        )
    }
}

/**
 * Top bar displayed when multi-selection mode is active.
 */
@Composable
fun SelectionTopBar(
    selectedCount: Int,
    onCloseClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPlayNextClick: () -> Unit = {},
    onAddToQueueClick: () -> Unit = {},
    onAddToPlaylistClick: () -> Unit = {},
    onMoveToTopClick: () -> Unit = {},
    contentColor: Color = SonzaOnBackground,
    isDarkTheme: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = SonzaSurface.copy(alpha = 0.96f),
        shadowElevation = ElevationTokens.Level2
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onCloseClick) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close selection",
                    tint = contentColor
                )
            }

            Text(
                text = "$selectedCount",
                style = SonzaTypography.SongTitle.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = contentColor,
                modifier = Modifier.padding(start = 6.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onPlayNextClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                    contentDescription = "Play Next",
                    tint = contentColor
                )
            }

            IconButton(onClick = onAddToQueueClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                    contentDescription = "Add to Queue",
                    tint = contentColor
                )
            }

            IconButton(onClick = onAddToPlaylistClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = "Add to Playlist",
                    tint = contentColor
                )
            }

            IconButton(onClick = onMoveToTopClick) {
                Icon(
                    imageVector = Icons.Default.VerticalAlignTop,
                    contentDescription = "Move to Top",
                    tint = contentColor
                )
            }

            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Selected",
                    tint = SonzaError
                )
            }
        }
    }
}

/**
 * Shimmering skeleton loader for the Playlist Screen matching the hero and list layout.
 */
@Composable
private fun PlaylistSkeleton(
    onBackClick: () -> Unit
) {
    ShimmerContainer {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 16.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SonzaOnBackground
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                ShimmerBox(width = 36.dp, height = 36.dp, shape = CircleShape)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Artwork Skeleton
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .shimmerBackground(RoundedCornerShape(RadiusTokens.Lg))
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Title Skeleton
            ShimmerBox(width = 200.dp, height = 24.dp, shape = RoundedCornerShape(6.dp))

            Spacer(modifier = Modifier.height(8.dp))

            // Creator / Metadata Skeleton
            ShimmerBox(width = 140.dp, height = 14.dp, shape = RoundedCornerShape(4.dp))

            Spacer(modifier = Modifier.height(24.dp))

            // Action Buttons Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(width = 52.dp, height = 52.dp, shape = CircleShape)
                Spacer(modifier = Modifier.width(14.dp))
                ShimmerBox(width = 160.dp, height = 52.dp, shape = PillShape)
                Spacer(modifier = Modifier.width(14.dp))
                ShimmerBox(width = 52.dp, height = 52.dp, shape = CircleShape)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Track Items Skeleton
            repeat(5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ShimmerBox(width = 24.dp, height = 16.dp, shape = RoundedCornerShape(4.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    ShimmerBox(width = 44.dp, height = 44.dp, shape = RoundedCornerShape(8.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        ShimmerBox(width = 160.dp, height = 15.dp, shape = RoundedCornerShape(4.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        ShimmerBox(width = 100.dp, height = 12.dp, shape = RoundedCornerShape(4.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    ShimmerBox(width = 20.dp, height = 20.dp, shape = CircleShape)
                }
            }
        }
    }
}
