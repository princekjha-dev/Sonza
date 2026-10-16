package com.sonza.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.core.model.RecentlyPlayed
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.AddToPlaylistSheet
import com.sonza.app.ui.components.CreatePlaylistDialog
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.NowPlayingAnimation
import com.sonza.app.ui.components.SongMenuBottomSheet
import com.sonza.app.ui.screens.viewmodel.RecentsViewModel
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape
import com.sonza.app.ui.viewmodel.PlayerViewModel
import com.sonza.app.ui.viewmodel.PlaylistManagementViewModel
import org.koin.compose.viewmodel.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Rebuilt History (Recents) screen delivering a polished, native music app experience:
 * - Clean Top App Bar with back navigation, prominent title, incognito toggle & clear action
 * - Rounded pill search bar with real-time dynamic filtering
 * - Chronological date grouping (Today, Yesterday, Specific dates) in local timezone
 * - 56dp artwork with Squircle clipping and neutral fallback placeholder
 * - Safe metadata handling for blank titles, filenames and <unknown> artist fallbacks
 * - Trailing vertical 3-dot overflow menu invoking SongMenuBottomSheet
 * - Modern dark rounded confirmation dialog with theme-driven dynamic accent
 * - Minimalist empty history & empty search result states
 * - Generous bottom scroll padding accommodating floating mini-player & bottom nav
 */
@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun RecentsScreen(
    onSongClick: (List<Song>, Int) -> Unit,
    onBack: () -> Unit,
    currentSong: Song? = null,
    isPlaying: Boolean = false,
    onPlayPause: () -> Unit = {},
    viewModel: RecentsViewModel = koinViewModel(),
    playerViewModel: PlayerViewModel = koinViewModel(),
    playlistViewModel: PlaylistManagementViewModel = koinViewModel()
) {
    val recentlyPlayed by viewModel.recentSongs.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val selectedSongIds by viewModel.selectedSongs.collectAsState()
    val incognitoModeEnabled by viewModel.incognitoModeEnabled.collectAsState()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor by animateColorAsState(
        targetValue = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "recentsAccent"
    )
    val destructiveRed = Color(0xFFFF3B30)

    var searchQuery by remember { mutableStateOf("") }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Song Menu State
    var showSongMenu by remember { mutableStateOf(false) }
    var selectedSong: Song? by remember { mutableStateOf(null) }

    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val bottomScrollPadding = if (currentSong != null) {
        navBarPadding + 80.dp // Accommodate floating mini-player gracefully without excess void
    } else {
        navBarPadding + 20.dp
    }

    // Back handling: when in edit/selection mode, exit edit mode and clear selection instead of leaving screen
    BackHandler(enabled = isEditMode) {
        viewModel.exitEditMode()
    }

    val filteredHistory = remember(recentlyPlayed, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            recentlyPlayed
        } else {
            recentlyPlayed.filter { item ->
                item.song.title.lowercase().contains(q) ||
                    item.song.artist.lowercase().contains(q) ||
                    item.song.album.lowercase().contains(q)
            }
        }
    }

    // Confirmation dialog for clearing selected history
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Delete selected songs?",
                    style = SonzaTypography.Headline,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground
                )
            },
            text = {
                Text(
                    text = "This will permanently remove ${selectedSongIds.size} song${if (selectedSongIds.size > 1) "s" else ""} from your listening history.",
                    style = SonzaTypography.BodyMedium,
                    color = SonzaOnSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSelectedSongs()
                        showDeleteConfirmDialog = false
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = destructiveRed,
                        fontWeight = FontWeight.Bold,
                        style = SonzaTypography.BodyMedium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = SonzaOnSurfaceVariant,
                        style = SonzaTypography.BodyMedium
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    // Confirmation dialog for clearing all history
    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            title = {
                Text(
                    text = "Clear all history?",
                    style = SonzaTypography.Headline,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground
                )
            },
            text = {
                Text(
                    text = "This will permanently remove all songs from your listening history.",
                    style = SonzaTypography.BodyMedium,
                    color = SonzaOnSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearAllConfirmDialog = false
                    }
                ) {
                    Text(
                        text = "Clear All",
                        color = destructiveRed,
                        fontWeight = FontWeight.Bold,
                        style = SonzaTypography.BodyMedium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text(
                        text = "Cancel",
                        color = SonzaOnSurfaceVariant,
                        style = SonzaTypography.BodyMedium
                    )
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaBackground)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            if (isEditMode) {
                // Edit / Selection Mode Top Bar: Close Icon, "N Selected", "Delete Selected" button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.exitEditMode() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel Selection",
                            tint = SonzaOnBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (selectedSongIds.isEmpty()) "Select items" else "${selectedSongIds.size} Selected",
                        style = SonzaTypography.Headline.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = SonzaOnBackground,
                        modifier = Modifier.weight(1f)
                    )

                    if (selectedSongIds.isNotEmpty()) {
                        Button(
                            onClick = { showDeleteConfirmDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = destructiveRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Delete Selected",
                                style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            } else {
                // Clean Apple-inspired Native Top Bar: ← History ⋯
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SonzaOnBackground
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "History",
                        style = SonzaTypography.Headline.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = SonzaOnBackground,
                        modifier = Modifier.weight(1f)
                    )

                    // Trailing ⋯ overflow menu
                    Box {
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = SonzaOnBackground
                            )
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            if (recentlyPlayed.isNotEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Select") },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.enterEditMode()
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = {
                                    Text(if (incognitoModeEnabled) "Turn off Incognito" else "Turn on Incognito")
                                },
                                onClick = {
                                    showOverflowMenu = false
                                    viewModel.setIncognitoMode(!incognitoModeEnabled)
                                },
                                leadingIcon = {
                                    Icon(
                                        if (incognitoModeEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                            if (recentlyPlayed.isNotEmpty()) {
                                HorizontalDivider(color = SonzaOutline.copy(alpha = 0.2f))
                                DropdownMenuItem(
                                    text = { Text("Clear All History", color = destructiveRed) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showClearAllConfirmDialog = true
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = destructiveRed, modifier = Modifier.size(18.dp))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Search Field (Shown when history is populated or a search is underway)
            if (recentlyPlayed.isNotEmpty() || searchQuery.isNotEmpty()) {
                HistorySearchField(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClearQuery = { searchQuery = "" },
                    accentColor = accentColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceXs)
                )
            }

            // Incognito Status Banner
            AnimatedVisibility(
                visible = incognitoModeEnabled,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceXs),
                    color = accentColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Incognito Mode is on. Listening history is not being saved.",
                            style = SonzaTypography.BodySmall.copy(fontWeight = FontWeight.Medium),
                            color = SonzaOnBackground
                        )
                    }
                }
            }

            // Main Content Area
            if (recentlyPlayed.isEmpty()) {
                // Empty Listening History State
                EmptyHistoryState(
                    onStartListening = onBack,
                    accentColor = accentColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomScrollPadding)
                )
            } else if (filteredHistory.isEmpty()) {
                // Empty Search Result State
                EmptySearchState(
                    query = searchQuery,
                    onClearSearch = { searchQuery = "" },
                    accentColor = accentColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomScrollPadding)
                )
            } else {
                // Chronologically Grouped History List
                val groupedByDate = remember(filteredHistory) {
                    filteredHistory.groupBy { getDateGroupHeader(it.playedAt) }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = bottomScrollPadding)
                ) {
                    groupedByDate.forEach { (dateGroup, items) ->
                        stickyHeader(key = dateGroup) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = SonzaBackground
                            ) {
                                Text(
                                    text = dateGroup,
                                    style = SonzaTypography.TitleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.3.sp
                                    ),
                                    color = accentColor,
                                    modifier = Modifier.padding(
                                        start = SpacingTokens.SpaceLg,
                                        end = SpacingTokens.SpaceLg,
                                        top = SpacingTokens.SpaceMd,
                                        bottom = SpacingTokens.SpaceXs
                                    )
                                )
                            }
                        }

                        items(
                            items = items,
                            key = { "${it.song.id}_${it.playedAt}" }
                        ) { recent ->
                            val isSelected = selectedSongIds.contains(recent.song.id)
                            val isCurrentTrack = currentSong?.id == recent.song.id
                            val allSongsInHistory = filteredHistory.map { it.song }
                            val indexInAll = allSongsInHistory.indexOf(recent.song)

                            HistorySongItem(
                                recent = recent,
                                isSelected = isSelected,
                                isSelectionMode = isEditMode,
                                isCurrentTrack = isCurrentTrack,
                                isPlaying = isPlaying,
                                accentColor = accentColor,
                                onClick = {
                                    if (isEditMode) {
                                        viewModel.toggleSelection(recent.song.id)
                                    } else if (isCurrentTrack) {
                                        onPlayPause()
                                    } else {
                                        onSongClick(allSongsInHistory, indexInAll)
                                    }
                                },
                                onLongClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (!isEditMode) {
                                        viewModel.enterEditMode()
                                    }
                                    viewModel.toggleSelection(recent.song.id)
                                },
                                onMoreClick = {
                                    selectedSong = recent.song
                                    showSongMenu = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Song Overflow Menu Bottom Sheet
    if (showSongMenu && selectedSong != null) {
        val song = selectedSong!!
        val allSongsInHistory = recentlyPlayed.map { it.song }
        val songIndex = allSongsInHistory.indexOf(song)

        SongMenuBottomSheet(
            isVisible = true,
            onDismiss = { showSongMenu = false },
            song = song,
            onPlay = {
                if (songIndex >= 0) {
                    onSongClick(allSongsInHistory, songIndex)
                } else {
                    onSongClick(listOf(song), 0)
                }
            },
            onPlayNext = { playerViewModel.playNext(song) },
            onAddToQueue = { playerViewModel.addToQueue(song) },
            onAddToPlaylist = { playlistViewModel.showAddToPlaylistSheet(song) },
            onDownload = { viewModel.downloadSong(song) },
            onShare = {
                val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(android.content.Intent.EXTRA_TEXT, "Listen to ${song.title} by ${song.artist} on Sonza")
                }
                context.startActivity(android.content.Intent.createChooser(shareIntent, "Share song"))
            },
            onRemoveFromHistory = {
                viewModel.removeFromHistory(song.id)
            }
        )
    }

    // Add to Playlist Sheet
    val playlistMgmtState by playlistViewModel.uiState.collectAsState()
    if (playlistMgmtState.showAddToPlaylistSheet && playlistMgmtState.selectedSongs.isNotEmpty()) {
        AddToPlaylistSheet(
            songs = playlistMgmtState.selectedSongs,
            isVisible = true,
            playlists = playlistMgmtState.userPlaylists,
            isLoading = playlistMgmtState.isLoadingPlaylists || playlistMgmtState.isAddingSong,
            onDismiss = { playlistViewModel.hideAddToPlaylistSheet() },
            onAddToPlaylist = { playlistId ->
                playlistViewModel.addSongsToPlaylist(playlistId)
                viewModel.clearSelection()
            },
            onCreateNewPlaylist = { playlistViewModel.showCreatePlaylistDialog() }
        )
    }

    if (playlistMgmtState.showCreatePlaylistDialog) {
        CreatePlaylistDialog(
            isVisible = true,
            isCreating = playlistMgmtState.isCreatingPlaylist,
            onDismiss = { playlistViewModel.hideCreatePlaylistDialog() },
            onCreate = { title, desc, private, sync -> playlistViewModel.createPlaylist(title, desc, private, sync) },
            isLoggedIn = true
        )
    }
}

/**
 * Rounded pill search input field with elevated dark surface and dynamic accent focus border.
 */
@Composable
private fun HistorySearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isFocused) accentColor.copy(alpha = 0.65f) else SonzaOutline.copy(alpha = 0.25f),
        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
        label = "historySearchBorder"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, animatedBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = "Search",
                tint = if (isFocused || query.isNotEmpty()) accentColor else SonzaOnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (query.isEmpty()) {
                    Text(
                        text = "Search history...",
                        style = SonzaTypography.BodyMedium,
                        color = SonzaOnSurfaceVariant.copy(alpha = 0.65f)
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    textStyle = SonzaTypography.BodyMedium.copy(color = SonzaOnBackground),
                    singleLine = true,
                    cursorBrush = SolidColor(accentColor)
                )
            }

            if (query.isNotEmpty()) {
                IconButton(
                    onClick = onClearQuery,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear search",
                        tint = SonzaOnSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Individual History Song item row with 56dp artwork, graceful unknown fallback, and aligned 3-dot menu.
 */
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun HistorySongItem(
    recent: RecentlyPlayed,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    isCurrentTrack: Boolean,
    isPlaying: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    val context = LocalContext.current

    // Graceful metadata resolution
    val displayTitle = recent.song.title.takeIf { it.isNotBlank() }
        ?: recent.song.id.takeIf { it.isNotBlank() }
        ?: "Unknown Track"

    val displayArtist = recent.song.artist.takeIf { it.isNotBlank() && it != "<unknown>" } ?: "Unknown Artist"
    val displayTime = getTimeLabel(recent.playedAt)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when {
                    isSelected -> SonzaSurfaceVariant.copy(alpha = 0.5f)
                    isCurrentTrack -> accentColor.copy(alpha = 0.08f)
                    else -> Color.Transparent
                }
            )
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Left circular selection control (visible during selection/edit mode)
        AnimatedVisibility(
            visible = isSelectionMode,
            enter = fadeIn() + expandHorizontally(),
            exit = fadeOut() + shrinkHorizontally()
        ) {
            Box(
                modifier = Modifier.size(26.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    // Filled circular checkmark
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF007AFF),
                        modifier = Modifier.size(22.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                } else {
                    // Empty circle
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .border(
                                width = 1.5.dp,
                                color = SonzaOnSurfaceVariant.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        // Album Artwork (56dp square) with neutral placeholder fallback
        Box(
            modifier = Modifier.size(56.dp),
            contentAlignment = Alignment.Center
        ) {
            if (!recent.song.thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(recent.song.thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = displayTitle,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(SquircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    shape = SquircleShape,
                    color = SonzaSurfaceVariant,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = SonzaOnSurfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Animated playing badge when this song is currently active
            if (isCurrentTrack && !isSelectionMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(SquircleShape),
                    color = Color.Black.copy(alpha = 0.55f)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        NowPlayingAnimation(
                            color = accentColor,
                            isPlaying = isPlaying,
                            barCount = 3,
                            barWidth = 3.5.dp,
                            maxBarHeight = 18.dp,
                            minBarHeight = 4.dp
                        )
                    }
                }
            }
        }

        // Title, Artist, and Time metadata column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = displayTitle,
                style = SonzaTypography.SongTitle.copy(
                    fontWeight = if (isCurrentTrack) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentTrack) accentColor else SonzaOnBackground
            )

            Text(
                text = displayArtist,
                style = SonzaTypography.ArtistSubtitle.copy(
                    fontSize = 13.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SonzaOnSurfaceVariant
            )

            Text(
                text = displayTime,
                style = SonzaTypography.BodySmall.copy(
                    fontSize = 11.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = SonzaOnSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        // Vertically centered 3-dot overflow menu
        if (!isSelectionMode) {
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = SonzaOnSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Centered Empty Listening History State with clear action to return to exploring music.
 */
@Composable
private fun EmptyHistoryState(
    onStartListening: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Surface(
                shape = SquircleShape,
                color = accentColor.copy(alpha = 0.12f),
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.padding(20.dp)
                )
            }

            Text(
                text = "No listening history",
                style = SonzaTypography.Headline,
                fontWeight = FontWeight.Bold,
                color = SonzaOnBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Songs you play will appear here.",
                style = SonzaTypography.BodyMedium,
                color = SonzaOnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onStartListening,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = SonzaBackground
                ),
                shape = RoundedCornerShape(RadiusTokens.Pill),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Start Listening",
                    style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

/**
 * Empty search results state with quick clear-query action.
 */
@Composable
private fun EmptySearchState(
    query: String,
    onClearSearch: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            Surface(
                shape = SquircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.size(72.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SearchOff,
                    contentDescription = null,
                    tint = SonzaOnSurfaceVariant,
                    modifier = Modifier.padding(18.dp)
                )
            }

            Text(
                text = "No results found",
                style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                color = SonzaOnBackground,
                textAlign = TextAlign.Center
            )

            Text(
                text = "No songs match \"$query\"",
                style = SonzaTypography.BodySmall,
                color = SonzaOnSurfaceVariant,
                textAlign = TextAlign.Center
            )

            TextButton(
                onClick = onClearSearch,
                shape = RoundedCornerShape(RadiusTokens.Pill)
            ) {
                Text(
                    text = "Clear search",
                    color = accentColor,
                    style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }
    }
}

/**
 * Group timestamps chronologically using user's local timezone:
 * - "Today"
 * - "Yesterday"
 * - "MMMM d" (e.g. "August 20") for current calendar year
 * - "MMMM d, yyyy" for past calendar years
 */
private fun getDateGroupHeader(timestamp: Long): String {
    val now = Calendar.getInstance()

    val todayStart = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val yesterdayStart = (todayStart.clone() as Calendar).apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }

    return when {
        timestamp >= todayStart.timeInMillis -> "Today"
        timestamp >= yesterdayStart.timeInMillis -> "Yesterday"
        else -> {
            val itemCal = Calendar.getInstance().apply { timeInMillis = timestamp }
            val nowYear = now.get(Calendar.YEAR)
            val itemYear = itemCal.get(Calendar.YEAR)
            if (nowYear == itemYear) {
                SimpleDateFormat("MMMM d", Locale.getDefault()).format(Date(timestamp))
            } else {
                SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
            }
        }
    }
}

/**
 * Formats played timestamp to localized time label (e.g. "8:22 PM").
 */
private fun getTimeLabel(timestamp: Long): String {
    return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
}
