package com.sonza.app.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.ui.components.CreatePlaylistDialog
import com.sonza.app.ui.components.DeletePlaylistDialog
import com.sonza.app.ui.components.ExportPlaylistDialog
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.MediaMenuBottomSheet
import com.sonza.app.ui.components.RenamePlaylistDialog
import com.sonza.app.ui.components.SonzaLoadingIndicator
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.viewmodel.LibrarySortOption
import com.sonza.app.ui.viewmodel.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryPlaylistsScreen(
    onBackClick: () -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaColors.BrandAccent
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Dialog & menu states
    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var isCreatingPlaylist by remember { mutableStateOf(false) }
    var selectedPlaylist: PlaylistDisplayItem? by remember { mutableStateOf(null) }
    var showPlaylistMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val filteredPlaylists = remember(uiState.playlists, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) uiState.playlists else uiState.playlists.filter {
            it.name.lowercase().contains(q) || it.uploaderName.lowercase().contains(q)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = SonzaColors.OnBackground
                    )
                }

                Text(
                    text = "Playlists",
                    style = SonzaTypography.Headline.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                    color = SonzaColors.OnBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )

                IconButton(onClick = { showCreatePlaylistDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Playlist",
                        tint = accentColor
                    )
                }

                IconButton(onClick = { isSearchActive = !isSearchActive }) {
                    Icon(
                        imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (isSearchActive) accentColor else SonzaColors.OnSurfaceVariant
                    )
                }

                Box {
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sort",
                            tint = SonzaColors.OnSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false },
                        modifier = Modifier.background(SonzaColors.SurfaceVariant)
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Recently Added",
                                    color = if (uiState.sortOption == LibrarySortOption.DATE_ADDED) accentColor else SonzaColors.OnSurface,
                                    fontWeight = if (uiState.sortOption == LibrarySortOption.DATE_ADDED) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.setSortOption(LibrarySortOption.DATE_ADDED)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Name A–Z",
                                    color = if (uiState.sortOption == LibrarySortOption.NAME) accentColor else SonzaColors.OnSurface,
                                    fontWeight = if (uiState.sortOption == LibrarySortOption.NAME) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                viewModel.setSortOption(LibrarySortOption.NAME)
                                showSortMenu = false
                            }
                        )
                    }
                }
            }

            // Search Bar (if active)
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search playlists...", color = SonzaColors.OnSurfaceVariant) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = SonzaColors.Outline,
                        focusedTextColor = SonzaColors.OnBackground,
                        unfocusedTextColor = SonzaColors.OnBackground,
                        cursorColor = accentColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }

            // Content Area
            if (uiState.isLoading && uiState.playlists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SonzaLoadingIndicator(modifier = Modifier.size(72.dp))
                }
            } else if (filteredPlaylists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = null,
                            tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No playlists matching \"$searchQuery\"" else "No playlists yet",
                            style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SonzaColors.OnBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your own playlist or import one from other apps.",
                            style = SonzaTypography.BodyMedium,
                            color = SonzaColors.OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showCreatePlaylistDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("New Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPlaylists, key = { it.id }) { playlist ->
                        PlaylistGridItem(
                            playlist = playlist,
                            onClick = { onPlaylistClick(playlist) },
                            onMoreClick = {
                                selectedPlaylist = playlist
                                showPlaylistMenu = true
                            }
                        )
                    }
                }
            }
        }

        // Dialogs
        CreatePlaylistDialog(
            isVisible = showCreatePlaylistDialog,
            isCreating = isCreatingPlaylist,
            onDismiss = { showCreatePlaylistDialog = false },
            onCreate = { title, description, isPrivate, syncWithYt ->
                isCreatingPlaylist = true
                viewModel.createPlaylist(title, description, isPrivate, syncWithYt) {
                    isCreatingPlaylist = false
                    showCreatePlaylistDialog = false
                }
            },
            isLoggedIn = uiState.isLoggedIn
        )

        selectedPlaylist?.let { playlist ->
            MediaMenuBottomSheet(
                isVisible = showPlaylistMenu,
                onDismiss = { showPlaylistMenu = false },
                title = playlist.name,
                subtitle = "${playlist.songCount} songs",
                thumbnailUrl = playlist.thumbnailUrl,
                onShuffle = {
                    viewModel.shufflePlay(playlist.id)
                    showPlaylistMenu = false
                },
                onPlayNext = {
                    viewModel.playNext(playlist.id)
                    showPlaylistMenu = false
                },
                onAddToQueue = {
                    viewModel.addToQueue(playlist.id)
                    showPlaylistMenu = false
                },
                onAddToPlaylist = {
                    showPlaylistMenu = false
                },
                onDownload = {
                    viewModel.downloadPlaylist(playlist)
                    showPlaylistMenu = false
                },
                onShare = {
                    showPlaylistMenu = false
                },
                onRename = {
                    showPlaylistMenu = false
                    showRenameDialog = true
                },
                onDelete = {
                    showPlaylistMenu = false
                    showDeleteDialog = true
                },
                onExport = {
                    showPlaylistMenu = false
                    showExportDialog = true
                }
            )

            RenamePlaylistDialog(
                isVisible = showRenameDialog,
                currentName = playlist.name,
                isRenaming = false,
                onDismiss = { showRenameDialog = false },
                onRename = { newName ->
                    viewModel.renamePlaylist(playlist.id, newName)
                    showRenameDialog = false
                }
            )

            DeletePlaylistDialog(
                isVisible = showDeleteDialog,
                playlistTitle = playlist.name,
                isDeleting = false,
                onDismiss = { showDeleteDialog = false },
                onDelete = {
                    viewModel.deletePlaylist(playlist.id)
                    showDeleteDialog = false
                }
            )

            ExportPlaylistDialog(
                isVisible = showExportDialog,
                onDismiss = { showExportDialog = false },
                onExportM3U = {
                    viewModel.exportPlaylistToM3U(context, playlist)
                    showExportDialog = false
                },
                onExportSonza = {
                    viewModel.exportPlaylistToSonza(context, playlist)
                    showExportDialog = false
                }
            )
        }
    }
}

@Composable
fun PlaylistGridItem(
    playlist: PlaylistDisplayItem,
    onClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        // Square Artwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SonzaColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!playlist.thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = playlist.thumbnailUrl,
                    contentDescription = playlist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                    color = SonzaColors.OnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${playlist.songCount} songs" + if (playlist.uploaderName.isNotEmpty()) " • ${playlist.uploaderName}" else "",
                    style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
                    color = SonzaColors.OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = SonzaColors.OnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
