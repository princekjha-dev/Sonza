package com.sonza.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.R
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.Artist
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.RecentSearchItem
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.AddToPlaylistSheet
import com.sonza.app.ui.components.CreatePlaylistDialog
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SearchResultsSkeleton
import com.sonza.app.ui.components.SonzaEmptyState
import com.sonza.app.ui.components.SonzaErrorState
import com.sonza.app.ui.components.SongMenuBottomSheet
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.theme.SpacingTokens
import com.sonza.app.ui.theme.SquircleShape
import com.sonza.app.ui.viewmodel.PlaylistManagementViewModel
import com.sonza.app.ui.viewmodel.ResultFilter
import com.sonza.app.ui.viewmodel.SearchEvent
import com.sonza.app.ui.viewmodel.SearchTab
import com.sonza.app.ui.viewmodel.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sonza Search Screen — Part M Redesign.
 *
 * Implements a 3-phase music search flow:
 * 1. Initial Landing / Discovery: "Search" header, source-agnostic search bar, recent searches, browse categories.
 * 2. Active Search Mode: Focusable input, back button / cancel action, recent search history with removal CTA.
 * 3. Search Results: Fast YouTube Music results, filter pills, skeleton loading, and accessible cards.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onSongClick: (List<Song>, Int) -> Unit,
    onArtistClick: (String) -> Unit = {},
    onPlaylistClick: (String) -> Unit = {},
    onAlbumClick: (Album) -> Unit = {},
    currentSong: Song? = null,
    viewModel: SearchViewModel = koinViewModel(),
    playlistViewModel: PlaylistManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    var isSearchActive by remember { mutableStateOf(false) }

    var showSongMenu by remember { mutableStateOf(false) }
    var selectedSong: Song? by remember { mutableStateOf(null) }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent

    // Intercept hardware/gesture back button:
    // First back exits active search mode and clears input focus.
    // Second back leaves the screen naturally.
    BackHandler(enabled = isSearchActive || uiState.query.isNotEmpty()) {
        if (uiState.query.isNotEmpty()) {
            viewModel.onQueryChange("")
        }
        isSearchActive = false
        focusManager.clearFocus()
    }

    LaunchedEffect(listState.isScrollInProgress, gridState.isScrollInProgress) {
        if (listState.isScrollInProgress || gridState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SearchEvent.ShowAddToPlaylistSheet -> {
                    playlistViewModel.showAddToPlaylistSheet(event.song)
                }
            }
        }
    }

    val voiceSearchLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.getOrNull(0)
            if (!spokenText.isNullOrBlank()) {
                viewModel.onQueryChange(spokenText)
                viewModel.search()
                isSearchActive = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaColors.Background)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Header title shown on Initial Landing Screen when not in active search
            if (!isSearchActive && uiState.query.isBlank()) {
                Text(
                    text = "Search",
                    style = SonzaTypography.Headline,
                    fontWeight = FontWeight.Bold,
                    color = SonzaOnBackground,
                    modifier = Modifier.padding(
                        start = SpacingTokens.SpaceLg,
                        top = SpacingTokens.SpaceMd,
                        bottom = SpacingTokens.SpaceXs
                    )
                )
            }

            // Search Bar Component
            SearchBar(
                inputField = {
                    SearchBarDefaults.InputField(
                        query = uiState.query,
                        onQueryChange = { viewModel.onQueryChange(it) },
                        onSearch = {
                            viewModel.search()
                            isSearchActive = false
                            focusManager.clearFocus()
                        },
                        expanded = isSearchActive,
                        onExpandedChange = { isSearchActive = it },
                        placeholder = {
                            Text(
                                text = "Search for songs, artists, or albums",
                                style = SonzaTypography.BodyLarge,
                                color = SonzaOnSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            if (isSearchActive || uiState.query.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        if (uiState.query.isNotEmpty()) {
                                            viewModel.onQueryChange("")
                                        }
                                        isSearchActive = false
                                        focusManager.clearFocus()
                                    },
                                    modifier = Modifier.semantics {
                                        contentDescription = "Back to search discovery"
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = SonzaOnBackground
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.Search,
                                    contentDescription = "Search icon",
                                    tint = SonzaOnSurfaceVariant
                                )
                            }
                        },
                        trailingIcon = {
                            if (uiState.query.isNotEmpty()) {
                                IconButton(
                                    onClick = { viewModel.onQueryChange("") },
                                    modifier = Modifier.semantics { contentDescription = "Clear search query" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = SonzaOnBackground
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                            putExtra(
                                                android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                            )
                                            putExtra(
                                                android.speech.RecognizerIntent.EXTRA_PROMPT,
                                                "Speak to search"
                                            )
                                        }
                                        try {
                                            voiceSearchLauncher.launch(intent)
                                        } catch (e: Exception) {
                                            com.sonza.app.util.SnackbarUtil.showWarning("Voice search not supported")
                                        }
                                    },
                                    modifier = Modifier.semantics { contentDescription = "Voice search" }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = null,
                                        tint = SonzaOnSurfaceVariant
                                    )
                                }
                            }
                        }
                    )
                },
                expanded = isSearchActive,
                onExpandedChange = { isSearchActive = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceXs),
                colors = SearchBarDefaults.colors(
                    containerColor = SonzaSurfaceVariant
                ),
                shape = SquircleShape
            ) {
                // Active Search Dropdown / Overlay Content
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = SpacingTokens.SpaceXl)
                ) {
                    // Live Query Suggestions
                    if (uiState.showSuggestions && uiState.query.isNotBlank() && uiState.suggestions.isNotEmpty()) {
                        items(
                            items = uiState.suggestions.take(8),
                            key = { it }
                        ) { suggestion ->
                            SuggestionItem(
                                suggestion = suggestion,
                                accentColor = accentColor,
                                onClick = {
                                    viewModel.onSuggestionClick(suggestion)
                                    isSearchActive = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }

                    // Recent Searches List (Active Search Mode with blank query)
                    if (uiState.query.isBlank()) {
                        if (uiState.recentSearches.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceMd),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recent searches",
                                        style = SonzaTypography.TitleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SonzaOnBackground
                                    )

                                    Text(
                                        text = "Clear all",
                                        style = SonzaTypography.LabelLarge,
                                        color = accentColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(RadiusTokens.Sm))
                                            .clickable { viewModel.clearRecentSearches() }
                                            .padding(horizontal = SpacingTokens.SpaceSm, vertical = SpacingTokens.SpaceXs)
                                            .semantics { contentDescription = "Clear all recent search history" }
                                    )
                                }
                            }

                            items(
                                items = uiState.recentSearches,
                                key = { it.id }
                            ) { item ->
                                ActiveSearchHistoryRow(
                                    item = item,
                                    onClick = {
                                        viewModel.onRecentSearchClick(item)
                                        isSearchActive = false
                                        focusManager.clearFocus()
                                    },
                                    onDelete = {
                                        viewModel.removeRecentSearch(item)
                                    }
                                )
                            }
                        } else {
                            // Empty History State
                            item {
                                SonzaEmptyState(
                                    title = "No recent searches",
                                    description = "Search for songs, artists, or albums to get started.",
                                    modifier = Modifier.padding(SpacingTokens.SpaceLg)
                                )
                            }
                        }
                    }
                }
            }

            // Results Category Filter Chips (Only shown when query is entered and results are displayed)
            AnimatedVisibility(
                visible = !isSearchActive && uiState.query.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                val filters = listOf(
                    ResultFilter.ALL to "All",
                    ResultFilter.SONGS to "Songs",
                    ResultFilter.VIDEOS to "Videos",
                    ResultFilter.ALBUMS to "Albums",
                    ResultFilter.ARTISTS to "Artists",
                    ResultFilter.COMMUNITY_PLAYLISTS to "Playlists"
                )

                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceXs)
                        .horizontalScroll(rememberScrollState())
                ) {
                    filters.forEachIndexed { index, (filter, label) ->
                        val isSelected = uiState.resultFilter == filter
                        SegmentedButton(
                            selected = isSelected,
                            onClick = { viewModel.setResultFilter(filter) },
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = filters.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = accentColor.copy(alpha = 0.15f),
                                activeContentColor = accentColor,
                                activeBorderColor = accentColor,
                                inactiveContainerColor = SonzaSurfaceVariant,
                                inactiveContentColor = SonzaOnSurfaceVariant,
                                inactiveBorderColor = SonzaOutline.copy(alpha = 0.5f)
                            ),
                            icon = {}
                        ) {
                            Text(
                                text = label,
                                style = SonzaTypography.LabelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // Content Area: Results View OR Initial Discovery Screen
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(1),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                // Loading Skeleton State
                if (uiState.isLoading) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        SearchResultsSkeleton(modifier = Modifier.padding(top = SpacingTokens.SpaceSm))
                    }
                }

                // Initial Discovery Screen (Query is empty & not in active search)
                if (uiState.query.isBlank()) {
                    // Recent Searches Section (Landing Preview)
                    if (uiState.recentSearches.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Recently Searched",
                                        style = SonzaTypography.TitleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SonzaOnBackground
                                    )

                                    Text(
                                        text = "Clear",
                                        style = SonzaTypography.LabelMedium,
                                        color = accentColor,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(RadiusTokens.Sm))
                                            .clickable { viewModel.clearRecentSearches() }
                                            .padding(horizontal = SpacingTokens.SpaceSm, vertical = SpacingTokens.SpaceXs)
                                    )
                                }

                                Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))

                                uiState.recentSearches.take(4).forEach { item ->
                                    ActiveSearchHistoryRow(
                                        item = item,
                                        onClick = {
                                            viewModel.onRecentSearchClick(item)
                                        },
                                        onDelete = {
                                            viewModel.removeRecentSearch(item)
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // "Browse all" Category Tiles
                    if (uiState.browseCategories.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceMd)
                            ) {
                                Text(
                                    text = "Browse all",
                                    style = SonzaTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SonzaOnBackground,
                                    modifier = Modifier.padding(bottom = SpacingTokens.SpaceMd)
                                )

                                uiState.browseCategories.chunked(2).forEach { rowItems ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = SpacingTokens.SpaceMd),
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd)
                                    ) {
                                        rowItems.forEach { category ->
                                            BrowseCategoryCard(
                                                category = category,
                                                onClick = { viewModel.onCategoryClick(category) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        if (rowItems.size == 1) Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                // Results View (When Query is Entered)
                if (uiState.query.isNotBlank() && !uiState.isLoading) {
                    if (uiState.resultFilter != ResultFilter.ALL) {
                        when (uiState.resultFilter) {
                            ResultFilter.SONGS, ResultFilter.VIDEOS -> {
                                itemsIndexed(uiState.results, key = { _, song -> "song_${song.id}" }) { index, song ->
                                    SearchResultItem(
                                        song = song,
                                        onClick = {
                                            viewModel.addToRecentSearches(song)
                                            onSongClick(uiState.results, index)
                                        },
                                        onArtistClick = onArtistClick,
                                        onMoreClick = { selectedSong = song; showSongMenu = true }
                                    )
                                }
                            }
                            ResultFilter.ARTISTS -> {
                                items(uiState.artistResults, key = { it.id }) { artist ->
                                    ArtistSearchListItem(artist = artist, onClick = { onArtistClick(artist.id) })
                                }
                            }
                            ResultFilter.ALBUMS -> {
                                items(uiState.albumResults, key = { it.id }) { album ->
                                    AlbumSearchListItem(
                                        album = album,
                                        onClick = { viewModel.addToRecentSearches(album); onAlbumClick(album) }
                                    )
                                }
                            }
                            ResultFilter.COMMUNITY_PLAYLISTS, ResultFilter.FEATURED_PLAYLISTS -> {
                                items(uiState.playlistResults, key = { it.id }) { playlist ->
                                    PlaylistSearchListItem(
                                        playlist = playlist,
                                        onClick = { viewModel.addToRecentSearches(playlist); onPlaylistClick(playlist.id) }
                                    )
                                }
                            }
                            else -> {}
                        }
                    } else {
                        // ResultFilter.ALL (Categorized Results Layout)
                        if (uiState.artistResults.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Artists",
                                        style = SonzaTypography.TitleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SonzaOnBackground,
                                        modifier = Modifier.padding(
                                            start = SpacingTokens.SpaceLg,
                                            end = SpacingTokens.SpaceLg,
                                            top = SpacingTokens.SpaceSm
                                        )
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceLg),
                                        modifier = Modifier.padding(vertical = SpacingTokens.SpaceMd)
                                    ) {
                                        items(uiState.artistResults, key = { it.id }) { artist ->
                                            ArtistSearchCard(artist = artist, onClick = { onArtistClick(artist.id) })
                                        }
                                    }
                                    HorizontalDivider(
                                        color = SonzaOutline.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)
                                    )
                                }
                            }
                        }

                        if (uiState.playlistResults.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Playlists",
                                        style = SonzaTypography.TitleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SonzaOnBackground,
                                        modifier = Modifier.padding(
                                            start = SpacingTokens.SpaceLg,
                                            end = SpacingTokens.SpaceLg,
                                            top = SpacingTokens.SpaceSm
                                        )
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd),
                                        modifier = Modifier.padding(vertical = SpacingTokens.SpaceMd)
                                    ) {
                                        items(uiState.playlistResults, key = { it.id }) { playlist ->
                                            PlaylistSearchCard(
                                                playlist = playlist,
                                                onClick = { viewModel.addToRecentSearches(playlist); onPlaylistClick(playlist.id) }
                                            )
                                        }
                                    }
                                    HorizontalDivider(
                                        color = SonzaOutline.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)
                                    )
                                }
                            }
                        }

                        if (uiState.albumResults.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Albums",
                                        style = SonzaTypography.TitleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SonzaOnBackground,
                                        modifier = Modifier.padding(
                                            start = SpacingTokens.SpaceLg,
                                            end = SpacingTokens.SpaceLg,
                                            top = SpacingTokens.SpaceSm
                                        )
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd),
                                        modifier = Modifier.padding(vertical = SpacingTokens.SpaceMd)
                                    ) {
                                        items(uiState.albumResults, key = { it.id }) { album ->
                                            AlbumSearchCard(
                                                album = album,
                                                onClick = { viewModel.addToRecentSearches(album); onAlbumClick(album) }
                                            )
                                        }
                                    }
                                    HorizontalDivider(
                                        color = SonzaOutline.copy(alpha = 0.3f),
                                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm)
                                    )
                                }
                            }
                        }

                        if (uiState.results.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "Songs",
                                    style = SonzaTypography.TitleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SonzaOnBackground,
                                    modifier = Modifier.padding(
                                        start = SpacingTokens.SpaceLg,
                                        end = SpacingTokens.SpaceLg,
                                        top = SpacingTokens.SpaceSm,
                                        bottom = SpacingTokens.SpaceSm
                                    )
                                )
                            }
                            itemsIndexed(uiState.results, key = { _, song -> "main_song_${song.id}" }) { index, song ->
                                SearchResultItem(
                                    song = song,
                                    onClick = {
                                        viewModel.addToRecentSearches(song)
                                        onSongClick(uiState.results, index)
                                    },
                                    onArtistClick = onArtistClick,
                                    onMoreClick = { selectedSong = song; showSongMenu = true }
                                )
                            }
                        }
                    }

                    // Empty or Error State for Results
                    val hasAnyResult = uiState.results.isNotEmpty() ||
                            uiState.artistResults.isNotEmpty() ||
                            uiState.albumResults.isNotEmpty() ||
                            uiState.playlistResults.isNotEmpty()

                    if (!hasAnyResult) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            if (uiState.error != null) {
                                SonzaErrorState(
                                    title = "Search failed",
                                    message = uiState.error,
                                    onRetry = { viewModel.search(saveToHistory = false) },
                                    modifier = Modifier.padding(SpacingTokens.SpaceLg)
                                )
                            } else {
                                SonzaEmptyState(
                                    title = "No results found",
                                    description = "Try checking the spelling or use a different keyword for \"${uiState.query}\".",
                                    actionText = "Clear Search",
                                    onActionClick = { viewModel.onQueryChange("") },
                                    modifier = Modifier.padding(SpacingTokens.SpaceLg)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Sheets & Dialogs
        if (showSongMenu && selectedSong != null) {
            val song = selectedSong!!
            SongMenuBottomSheet(
                isVisible = showSongMenu,
                onDismiss = { showSongMenu = false },
                song = song,
                isCurrentlyPlaying = song.id == currentSong?.id,
                onPlayNext = { viewModel.playNext(song) },
                onAddToQueue = { viewModel.addToQueue(song) },
                onAddToPlaylist = { viewModel.addToPlaylist(song) },
                onDownload = { viewModel.downloadSong(song) },
                onShare = {
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            android.content.Intent.EXTRA_TEXT,
                            "Check out this song: ${song.title} by ${song.artist}\n\nhttps://music.youtube.com/watch?v=${song.id}"
                        )
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Song"))
                },
                onViewArtist = song.artistId?.let { id -> { onArtistClick(id) } },
                onViewAlbum = null
            )
        }

        val playlistMgmtState by playlistViewModel.uiState.collectAsState()
        if (playlistMgmtState.showAddToPlaylistSheet && playlistMgmtState.selectedSongs.isNotEmpty()) {
            AddToPlaylistSheet(
                songs = playlistMgmtState.selectedSongs,
                isVisible = true,
                playlists = playlistMgmtState.userPlaylists,
                isLoading = playlistMgmtState.isLoadingPlaylists || playlistMgmtState.isAddingSong,
                onDismiss = { playlistViewModel.hideAddToPlaylistSheet() },
                onAddToPlaylist = { playlistId -> playlistViewModel.addSongsToPlaylist(playlistId) },
                onCreateNewPlaylist = { playlistViewModel.showCreatePlaylistDialog() }
            )
        }
        if (playlistMgmtState.showCreatePlaylistDialog) {
            CreatePlaylistDialog(
                isVisible = playlistMgmtState.showCreatePlaylistDialog,
                isCreating = playlistMgmtState.isCreatingPlaylist,
                onDismiss = { playlistViewModel.hideCreatePlaylistDialog() },
                onCreate = { title, description, isPrivate, syncWithYt ->
                    playlistViewModel.createPlaylist(title, description, isPrivate, syncWithYt)
                },
                isLoggedIn = true
            )
        }
    }
}

/**
 * Search history row with prominent remove action and TalkBack accessibility semantics.
 */
@Composable
private fun ActiveSearchHistoryRow(
    item: RecentSearchItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val title = item.title
    val subtitle = when (item) {
        is RecentSearchItem.SongItem -> "Song • ${item.song.artist}"
        is RecentSearchItem.AlbumItem -> "Album • ${item.album.artist}"
        is RecentSearchItem.PlaylistItem -> "Playlist • ${item.playlist.author}"
        is RecentSearchItem.QueryItem -> "Search"
    }
    val thumbnailUrl = when (item) {
        is RecentSearchItem.SongItem -> item.song.thumbnailUrl
        is RecentSearchItem.AlbumItem -> item.album.thumbnailUrl
        is RecentSearchItem.PlaylistItem -> item.playlist.thumbnailUrl
        is RecentSearchItem.QueryItem -> null
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 3.dp)
            .clip(RoundedCornerShape(RadiusTokens.Md))
            .clickable(onClick = onClick),
        color = SonzaSurface,
        shape = RoundedCornerShape(RadiusTokens.Md),
        border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SpacingTokens.SpaceMd, vertical = SpacingTokens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!thumbnailUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(thumbnailUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(RadiusTokens.Sm))
                        .background(SonzaSurfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(RadiusTokens.Sm))
                        .background(SonzaSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = null,
                        tint = SonzaOnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = SonzaTypography.BodyLarge.copy(fontWeight = FontWeight.Medium),
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = SonzaTypography.LabelSmall,
                    color = SonzaOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(36.dp)
                    .semantics {
                        contentDescription = "Remove $title from search history"
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = SonzaOnSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Category exploration card with dynamic gradient and tap bounce.
 */
@Composable
private fun BrowseCategoryCard(
    category: com.sonza.app.core.model.BrowseCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseColor = category.color?.let { Color(it) } ?: run {
        val hue = (((category.title.hashCode() % 360) + 360) % 360).toFloat()
        Color.hsv(hue, 0.5f, 0.62f)
    }
    Box(
        modifier = modifier
            .height(96.dp)
            .clip(RoundedCornerShape(RadiusTokens.Md))
            .background(baseColor)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        Text(
            text = category.title,
            style = SonzaTypography.TitleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(SpacingTokens.SpaceMd)
                .align(Alignment.TopStart)
        )
        if (!category.thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(category.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(56.dp)
                    .clip(RoundedCornerShape(topStart = RadiusTokens.Sm))
            )
        }
    }
}

@Composable
private fun SuggestionItem(suggestion: String, accentColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, null, tint = accentColor, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(SpacingTokens.SpaceLg))
        Text(
            text = suggestion,
            style = SonzaTypography.BodyLarge,
            color = SonzaOnBackground
        )
    }
}

@Composable
private fun SearchResultItem(
    song: Song,
    onClick: () -> Unit,
    onArtistClick: (String) -> Unit = {},
    onMoreClick: () -> Unit
) {
    val dynamicColors = LocalSonzaDynamicColors.current

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 4.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick),
        shape = RoundedCornerShape(RadiusTokens.Md),
        color = SonzaSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.5f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(song.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = song.title,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(SonzaSurfaceVariant),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    style = SonzaTypography.TitleMedium.copy(fontSize = 15.sp),
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (song.isVideo) "Video" else "Song",
                        style = SonzaTypography.LabelSmall,
                        color = SonzaOnSurfaceVariant
                    )
                    Text(
                        text = " • ",
                        style = SonzaTypography.LabelSmall,
                        color = SonzaOnSurfaceVariant
                    )
                    val artistId = song.artistId
                    Text(
                        text = song.artist,
                        style = SonzaTypography.BodyMedium.copy(fontSize = 13.sp),
                        color = if (artistId != null) dynamicColors.accent else SonzaOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (artistId != null) {
                            Modifier.clickable(onClick = { onArtistClick(artistId) })
                        } else Modifier
                    )
                }
            }

            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = SonzaOnSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ArtistSearchListItem(artist: Artist, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 4.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick),
        shape = RoundedCornerShape(RadiusTokens.Md),
        color = SonzaSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.5f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(artist.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = artist.name,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(SonzaSurfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceLg))
            Column {
                Text(
                    text = artist.name,
                    style = SonzaTypography.TitleMedium.copy(fontSize = 15.sp),
                    color = SonzaOnBackground,
                    fontWeight = FontWeight.Bold
                )
                artist.subscribers?.let {
                    Text(
                        text = it,
                        style = SonzaTypography.BodyMedium.copy(fontSize = 13.sp),
                        color = SonzaOnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun AlbumSearchListItem(album: Album, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 4.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick),
        shape = RoundedCornerShape(RadiusTokens.Md),
        color = SonzaSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.5f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(album.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = album.title,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(SonzaSurfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceLg))
            Column {
                Text(
                    text = album.title,
                    style = SonzaTypography.TitleMedium.copy(fontSize = 15.sp),
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Album • ${album.artist}",
                    style = SonzaTypography.BodyMedium.copy(fontSize = 13.sp),
                    color = SonzaOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PlaylistSearchListItem(playlist: Playlist, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 4.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick),
        shape = RoundedCornerShape(RadiusTokens.Md),
        color = SonzaSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, SonzaOutline.copy(alpha = 0.5f)),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(SpacingTokens.SpaceSm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(playlist.thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = playlist.title,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(SonzaSurfaceVariant),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceLg))
            Column {
                Text(
                    text = playlist.title,
                    style = SonzaTypography.TitleMedium.copy(fontSize = 15.sp),
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Playlist • ${playlist.author}",
                    style = SonzaTypography.BodyMedium.copy(fontSize = 13.sp),
                    color = SonzaOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ArtistSearchCard(artist: Artist, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(artist.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = artist.name,
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(SonzaSurfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        Text(
            text = artist.name,
            style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = artist.subscribers ?: "Artist",
            style = SonzaTypography.LabelSmall,
            color = SonzaOnSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun PlaylistSearchCard(playlist: Playlist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(playlist.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = playlist.title,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(RadiusTokens.Lg))
                .background(SonzaSurfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        Text(
            text = playlist.title,
            style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SonzaOnBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (playlist.author.isNotBlank()) {
            Text(
                text = playlist.author,
                style = SonzaTypography.LabelSmall,
                color = SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AlbumSearchCard(album: Album, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(140.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(album.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = album.title,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(RadiusTokens.Lg))
                .background(SonzaSurfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        Text(
            text = album.title,
            style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.Bold),
            color = SonzaOnBackground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        val subtitle = (if (album.artist.isNotBlank()) album.artist else "") + (if (album.year != null) " • ${album.year}" else "")
        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = SonzaTypography.LabelSmall,
                color = SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
