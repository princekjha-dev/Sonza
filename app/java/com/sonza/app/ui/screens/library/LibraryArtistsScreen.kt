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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
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
import com.sonza.app.core.model.Artist
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SonzaLoadingIndicator
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.viewmodel.LibraryViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

enum class ArtistSortOption(val title: String) {
    NAME("Name A–Z"),
    RECENT("Recently Added")
}

@Composable
fun LibraryArtistsScreen(
    onBackClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaColors.BrandAccent
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(ArtistSortOption.NAME) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Combine local and remote artists
    val allArtists = remember(uiState.libraryArtists, uiState.localArtists) {
        (uiState.libraryArtists + uiState.localArtists).distinctBy { it.id }
    }

    val filteredArtists = remember(allArtists, searchQuery, sortOption) {
        val q = searchQuery.trim().lowercase()
        val list = if (q.isEmpty()) allArtists else allArtists.filter {
            it.name.lowercase().contains(q)
        }
        when (sortOption) {
            ArtistSortOption.NAME -> list.sortedBy { it.name.lowercase() }
            ArtistSortOption.RECENT -> list
        }
    }

    // A-Z fast scroll indexing
    val alphabetMap = remember(filteredArtists) {
        val map = mutableMapOf<Char, Int>()
        filteredArtists.forEachIndexed { index, artist ->
            val firstChar = artist.name.firstOrNull()?.uppercaseChar() ?: '#'
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
                    text = "Artists",
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
                        ArtistSortOption.entries.forEach { option ->
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
                    placeholder = { Text("Search artists...", color = SonzaColors.OnSurfaceVariant) },
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
            if (uiState.isLoading && allArtists.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SonzaLoadingIndicator(modifier = Modifier.size(72.dp))
                }
            } else if (filteredArtists.isEmpty()) {
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
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No artists matching \"$searchQuery\"" else "No artists in library",
                            style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SonzaColors.OnBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Artists from your saved music or device will appear here.",
                            style = SonzaTypography.BodyMedium,
                            color = SonzaColors.OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(top = 8.dp, bottom = 140.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredArtists, key = { it.id }) { artist ->
                            ArtistListItem(
                                artist = artist,
                                onClick = { onArtistClick(artist.id) }
                            )
                            HorizontalDivider(
                                color = SonzaColors.Outline.copy(alpha = 0.15f),
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(start = 76.dp)
                            )
                        }
                    }

                    // A-Z Fast Scroll Index
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
                                                    listState.scrollToItem(targetIndex)
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
fun ArtistListItem(
    artist: Artist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Circular Artist Photo
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(SonzaColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!artist.thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = artist.thumbnailUrl,
                    contentDescription = artist.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp),
                color = SonzaColors.OnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "Artist",
                style = SonzaTypography.BodySmall.copy(fontSize = 13.sp),
                color = SonzaColors.OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
