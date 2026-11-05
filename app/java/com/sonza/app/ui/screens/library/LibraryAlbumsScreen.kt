package com.sonza.app.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.Album
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SonzaLoadingIndicator
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

enum class AlbumSortOption(val title: String) {
    TITLE("Album Name"),
    ARTIST("Artist Name"),
    RECENT("Recently Added")
}

@Composable
fun LibraryAlbumsScreen(
    onBackClick: () -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlayAll: (List<Album>, Boolean) -> Unit = { _, _ -> },
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaColors.BrandAccent
    val coroutineScope = rememberCoroutineScope()
    val gridState = rememberLazyGridState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(AlbumSortOption.TITLE) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Combine local and library remote albums
    val allAlbums = remember(uiState.libraryAlbums, uiState.localAlbums) {
        (uiState.libraryAlbums + uiState.localAlbums).distinctBy { it.id }
    }

    val filteredAlbums = remember(allAlbums, searchQuery, sortOption) {
        val q = searchQuery.trim().lowercase()
        val list = if (q.isEmpty()) allAlbums else allAlbums.filter {
            it.title.lowercase().contains(q) || it.artist.lowercase().contains(q)
        }
        when (sortOption) {
            AlbumSortOption.TITLE -> list.sortedBy { it.title.lowercase() }
            AlbumSortOption.ARTIST -> list.sortedBy { it.artist.lowercase() }
            AlbumSortOption.RECENT -> list
        }
    }

    // Alphabetical headers and A-Z indexing
    val alphabetMap = remember(filteredAlbums) {
        val map = mutableMapOf<Char, Int>()
        filteredAlbums.forEachIndexed { index, album ->
            val firstChar = album.title.firstOrNull()?.uppercaseChar() ?: '#'
            val key = if (firstChar.isLetter()) firstChar else '#'
            if (!map.containsKey(key)) {
                map[key] = index
            }
        }
        map
    }

    val availableLetters = remember(alphabetMap) {
        alphabetMap.keys.sortedWith { a, b ->
            if (a == '#') 1 else if (b == '#') -1 else a.compareTo(b)
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
                    text = "Albums",
                    style = SonzaTypography.Headline.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                    color = SonzaColors.OnBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )

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
                        AlbumSortOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.title,
                                        color = if (sortOption == option) accentColor else SonzaColors.OnSurface,
                                        fontWeight = if (sortOption == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    sortOption = option
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Search Bar (if active)
            AnimatedVisibility(visible = isSearchActive) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search albums...", color = SonzaColors.OnSurfaceVariant) },
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

            // Play & Shuffle Action Buttons
            if (filteredAlbums.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Play Button
                    Surface(
                        onClick = { onPlayAll(filteredAlbums, false) },
                        shape = RoundedCornerShape(24.dp),
                        color = SonzaColors.SurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Play",
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold,
                                style = SonzaTypography.BodyMedium
                            )
                        }
                    }

                    // Shuffle Button
                    Surface(
                        onClick = { onPlayAll(filteredAlbums, true) },
                        shape = RoundedCornerShape(24.dp),
                        color = SonzaColors.SurfaceVariant,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle",
                                tint = accentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Shuffle",
                                color = accentColor,
                                fontWeight = FontWeight.SemiBold,
                                style = SonzaTypography.BodyMedium
                            )
                        }
                    }
                }
            }

            // Content Area
            if (uiState.isLoading && allAlbums.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SonzaLoadingIndicator(modifier = Modifier.size(72.dp))
                }
            } else if (filteredAlbums.isEmpty()) {
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
                            imageVector = Icons.Default.Album,
                            contentDescription = null,
                            tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No albums matching \"$searchQuery\"" else "No albums in library",
                            style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SonzaColors.OnBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Albums you save from search or your local music will appear here.",
                            style = SonzaTypography.BodyMedium,
                            color = SonzaColors.OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    // 2-Column Artwork Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        state = gridState,
                        contentPadding = PaddingValues(start = 16.dp, end = if (availableLetters.size > 5) 8.dp else 16.dp, top = 8.dp, bottom = 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredAlbums, key = { it.id }) { album ->
                            AlbumGridItem(
                                album = album,
                                onClick = { onAlbumClick(album) }
                            )
                        }
                    }

                    // A-Z Fast Scroll Index (if large library)
                    if (availableLetters.size > 5) {
                        Column(
                            modifier = Modifier
                                .padding(end = 4.dp, top = 8.dp, bottom = 140.dp),
                            verticalArrangement = Arrangement.SpaceEvenly,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            availableLetters.forEach { letter ->
                                Text(
                                    text = letter.toString(),
                                    color = accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .clickable {
                                            alphabetMap[letter]?.let { targetIndex ->
                                                coroutineScope.launch {
                                                    gridState.scrollToItem(targetIndex)
                                                }
                                            }
                                        }
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        // Square Album Artwork
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(SonzaColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!album.thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = album.thumbnailUrl,
                    contentDescription = album.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Album,
                    contentDescription = null,
                    tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = album.title,
            style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
            color = SonzaColors.OnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Artist Name
        Text(
            text = album.artist,
            style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
            color = SonzaColors.OnSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
