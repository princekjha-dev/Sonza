package com.sonza.app.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.CreatePlaylistDialog
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SonzaLoadingIndicator
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.utils.horizontalSwipeNavigation
import com.sonza.app.ui.viewmodel.LibraryViewModel
import com.sonza.app.ui.viewmodel.RecentlyAddedItem
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onHistoryClick: () -> Unit,
    onArtistClick: (String) -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    onDownloadsClick: () -> Unit = {},
    onMigratePlaylistsClick: () -> Unit = {},
    onNavigateToPlaylists: () -> Unit = {},
    onNavigateToArtists: () -> Unit = {},
    onNavigateToAlbums: () -> Unit = {},
    onNavigateToGenres: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToProfile: () -> Unit = {},
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaColors.BrandAccent
    val context = LocalContext.current

    // Error toast
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_LONG).show()
        }
    }

    var showCreatePlaylistDialog by remember { mutableStateOf(false) }
    var isCreatingPlaylist by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotation")
    val syncRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(1000)),
        label = "SyncRotationAngle"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaColors.Background)
            .horizontalSwipeNavigation(
                onSwipeLeft = onNavigateToProfile,
                onSwipeRight = onNavigateToSearch
            )
    ) {
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 150.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
            ) {
                // 1. Header
                item(key = "header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Library",
                            style = SonzaTypography.Headline.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 32.sp
                            ),
                            color = SonzaColors.OnBackground
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                                Icon(
                                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (isSearchActive) accentColor else SonzaColors.OnSurfaceVariant
                                )
                            }

                            IconButton(onClick = { viewModel.refresh() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Sync",
                                    tint = if (uiState.isRefreshing) accentColor else SonzaColors.OnSurfaceVariant,
                                    modifier = if (uiState.isRefreshing) Modifier.rotate(syncRotation) else Modifier
                                )
                            }

                            IconButton(onClick = { showCreatePlaylistDialog = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "New Playlist",
                                    tint = accentColor
                                )
                            }
                        }
                    }
                }

                // Search field (if active)
                item(key = "search_field") {
                    AnimatedVisibility(visible = isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Filter library...", color = SonzaColors.OnSurfaceVariant) },
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
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                        )
                    }
                }

                // 2. Main Categories Navigation Rows
                item(key = "categories_navigation") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 12.dp)
                    ) {
                        LibraryCategoryRow(
                            title = "Playlists",
                            icon = Icons.AutoMirrored.Filled.List,
                            accentColor = accentColor,
                            onClick = onNavigateToPlaylists
                        )
                        HorizontalDivider(color = SonzaColors.Outline.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 20.dp))

                        LibraryCategoryRow(
                            title = "Artists",
                            icon = Icons.Default.Person,
                            accentColor = accentColor,
                            onClick = onNavigateToArtists
                        )
                        HorizontalDivider(color = SonzaColors.Outline.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 20.dp))

                        LibraryCategoryRow(
                            title = "Albums",
                            icon = Icons.Default.Album,
                            accentColor = accentColor,
                            onClick = onNavigateToAlbums
                        )
                        HorizontalDivider(color = SonzaColors.Outline.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 20.dp))

                        LibraryCategoryRow(
                            title = "Genres",
                            icon = Icons.Default.Category,
                            accentColor = accentColor,
                            onClick = onNavigateToGenres
                        )
                        HorizontalDivider(color = SonzaColors.Outline.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 20.dp))

                        LibraryCategoryRow(
                            title = "Downloaded Music",
                            icon = Icons.Default.DownloadDone,
                            accentColor = accentColor,
                            onClick = onDownloadsClick
                        )
                        HorizontalDivider(color = SonzaColors.Outline.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 20.dp))

                        LibraryCategoryRow(
                            title = "Migrate Playlists",
                            subtitle = "Import music",
                            icon = Icons.Outlined.FileDownload,
                            accentColor = accentColor,
                            onClick = onMigratePlaylistsClick
                        )
                        HorizontalDivider(color = SonzaColors.Outline.copy(alpha = 0.15f), thickness = 0.5.dp, modifier = Modifier.padding(start = 56.dp, end = 20.dp))
                    }
                }

                // 3. Recently Added Section
                item(key = "recently_added_header") {
                    Text(
                        text = "Recently Added",
                        style = SonzaTypography.TitleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = SonzaColors.OnBackground,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp)
                    )
                }

                val recentItems = uiState.recentlyAdded
                if (recentItems.isEmpty() && uiState.isLoading) {
                    item(key = "recently_added_loading") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            SonzaLoadingIndicator(modifier = Modifier.size(56.dp))
                        }
                    }
                } else if (recentItems.isEmpty()) {
                    item(key = "recently_added_empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 20.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(SonzaColors.SurfaceVariant.copy(alpha = 0.5f))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Your recently added music will appear here",
                                    style = SonzaTypography.BodyMedium,
                                    color = SonzaColors.OnSurfaceVariant
                                )
                            }
                        }
                    }
                } else {
                    // 2-Column Grid for Recently Added
                    val chunkedItems = recentItems.take(8).chunked(2)
                    chunkedItems.forEachIndexed { rowIndex, rowItems ->
                        item(key = "recent_row_$rowIndex") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                rowItems.forEach { item ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        RecentlyAddedCard(
                                            item = item,
                                            onClick = {
                                                when (item) {
                                                    is RecentlyAddedItem.SongItem -> {
                                                        onSongClick(listOf(item.song), 0)
                                                    }
                                                    is RecentlyAddedItem.PlaylistItem -> {
                                                        onPlaylistClick(item.playlist)
                                                    }
                                                    is RecentlyAddedItem.AlbumItem -> {
                                                        onAlbumClick(item.album)
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                                if (rowItems.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // 4. Sonza Collections Section
                item(key = "collections_header") {
                    Text(
                        text = "Collections",
                        style = SonzaTypography.TitleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = SonzaColors.OnBackground,
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
                    )
                }

                item(key = "collections_list") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Liked
                        CollectionCard(
                            title = "Liked",
                            countText = "${uiState.likedSongsCount} songs",
                            icon = Icons.Default.Favorite,
                            iconTint = Color(0xFFE83D84),
                            onClick = {
                                if (uiState.likedSongsCount == 0) {
                                    viewModel.syncLikedSongs()
                                }
                                viewModel.loadLikedSongs()
                                onPlaylistClick(
                                    PlaylistDisplayItem(
                                        id = "LM",
                                        name = "Liked Songs",
                                        url = "",
                                        uploaderName = "",
                                        thumbnailUrl = null,
                                        songCount = uiState.likedSongsCount
                                    )
                                )
                            }
                        )

                        // Downloaded
                        CollectionCard(
                            title = "Downloaded",
                            countText = "${uiState.downloadedSongs.size} songs",
                            icon = Icons.Default.CheckCircle,
                            iconTint = Color(0xFF4ADE80),
                            onClick = onDownloadsClick
                        )

                        // Device Files
                        CollectionCard(
                            title = "Device Files",
                            countText = "${uiState.localSongs.size} songs",
                            icon = Icons.Default.Folder,
                            iconTint = Color(0xFFFBBF24),
                            onClick = {
                                onPlaylistClick(
                                    PlaylistDisplayItem(
                                        id = "DEVICE_SONGS",
                                        name = "Device files",
                                        url = "",
                                        uploaderName = "",
                                        thumbnailUrl = null,
                                        songCount = uiState.localSongs.size
                                    )
                                )
                            }
                        )

                        // Cached
                        CollectionCard(
                            title = "Cached",
                            countText = "${uiState.cachedSongCount} songs",
                            icon = Icons.Default.Cached,
                            iconTint = Color(0xFF60A5FA),
                            onClick = {
                                onPlaylistClick(
                                    PlaylistDisplayItem(
                                        id = "CACHED_ALL",
                                        name = "Cached Songs",
                                        url = "",
                                        uploaderName = "",
                                        thumbnailUrl = null,
                                        songCount = uiState.cachedSongCount
                                    )
                                )
                            }
                        )

                        // My Top 50
                        CollectionCard(
                            title = "My Top 50",
                            countText = "${uiState.top50SongCount} songs",
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            iconTint = Color(0xFFA855F7),
                            onClick = {
                                onPlaylistClick(
                                    PlaylistDisplayItem(
                                        id = "TOP_50",
                                        name = "My Top 50",
                                        url = "",
                                        uploaderName = "",
                                        thumbnailUrl = null,
                                        songCount = uiState.top50SongCount
                                    )
                                )
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
    }
}

@Composable
fun LibraryCategoryRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = SonzaTypography.BodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                ),
                color = SonzaColors.OnBackground
            )

            if (!subtitle.isNullOrEmpty()) {
                Text(
                    text = subtitle,
                    style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
                    color = SonzaColors.OnSurfaceVariant
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(15.dp)
        )
    }
}

@Composable
fun RecentlyAddedCard(
    item: RecentlyAddedItem,
    onClick: () -> Unit
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
            if (!item.thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = item.thumbnailUrl,
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = when (item) {
                        is RecentlyAddedItem.SongItem -> Icons.Default.MusicNote
                        is RecentlyAddedItem.PlaylistItem -> Icons.AutoMirrored.Filled.List
                        is RecentlyAddedItem.AlbumItem -> Icons.Default.Album
                    },
                    contentDescription = null,
                    tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.title,
            style = SonzaTypography.BodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp
            ),
            color = SonzaColors.OnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = item.subtitle,
            style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
            color = SonzaColors.OnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CollectionCard(
    title: String,
    countText: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = SonzaColors.SurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SonzaTypography.BodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = SonzaColors.OnBackground
                )

                Text(
                    text = countText,
                    style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
                    color = SonzaColors.OnSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
