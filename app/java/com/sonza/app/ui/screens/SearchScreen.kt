package com.sonza.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import com.sonza.app.ui.components.BrowseCategoryCard
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
import com.sonza.app.ui.viewmodel.SearchViewModel
import org.koin.compose.viewmodel.koinViewModel

/**
 * Sonza Search Screen — Redesigned Native Search Experience.
 *
 * 1. Initial Landing / Discovery: Headline, resting search bar, recent search history, genre exploration tiles.
 * 2. Active Search Mode: Instant focus, back button, live suggestions & history management with individual deletion.
 * 3. Search Results Mode: Compact scannable music rows, horizontal category pills,
 *    curated All tab sections, loading skeleton, and dynamic bottom insets for floating mini player / IME.
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

    // Dynamic inset calculation to prevent Mini Player, Bottom Nav, and Keyboard (IME) from covering content
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val imePadding = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
    val navBarHeight = 80.dp
    val miniPlayerHeight = if (currentSong != null) 64.dp else 0.dp
    val dynamicBottomInset = maxOf(
        navBarPadding + navBarHeight + miniPlayerHeight + SpacingTokens.SpaceLg,
        imePadding + SpacingTokens.SpaceMd
    )

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

            // Header title shown on Initial Landing Screen when not in active search and query is blank
            if (!isSearchActive && uiState.query.isBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = SpacingTokens.SpaceLg,
                            end = SpacingTokens.SpaceLg,
                            top = SpacingTokens.SpaceMd,
                            bottom = SpacingTokens.SpaceXs
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Search",
                        style = SonzaTypography.PageTitle,
                        color = SonzaOnBackground
                    )
                }
            }

            // Pinned Clean Search Bar Component
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceXs)
                    .height(50.dp),
                shape = RoundedCornerShape(RadiusTokens.Pill),
                color = SonzaSurfaceVariant,
                border = BorderStroke(0.75.dp, SonzaOutline.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Leading Icon: Back arrow when active/query present, or Search glass
                    if (isSearchActive || uiState.query.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                if (uiState.query.isNotEmpty()) {
                                    viewModel.onQueryChange("")
                                }
                                isSearchActive = false
                                focusManager.clearFocus()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = SonzaOnBackground,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = SonzaOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Search Text Input Field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = uiState.query,
                            onValueChange = {
                                viewModel.onQueryChange(it)
                                if (!isSearchActive) isSearchActive = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        isSearchActive = true
                                    }
                                },
                            textStyle = SonzaTypography.BodyLarge.copy(
                                color = SonzaOnBackground
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(accentColor),
                            keyboardOptions = KeyboardOptions(
                                imeAction = ImeAction.Search
                            ),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    viewModel.search()
                                    isSearchActive = false
                                    focusManager.clearFocus()
                                }
                            ),
                            decorationBox = { innerTextField ->
                                if (uiState.query.isEmpty()) {
                                    Text(
                                        text = "Artists, Songs, Lyrics, and more...",
                                        style = SonzaTypography.BodyLarge,
                                        color = SonzaOnSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }

                    // Trailing Icon: Clear query or Voice Search
                    if (uiState.query.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.onQueryChange("") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear query",
                                tint = SonzaOnBackground,
                                modifier = Modifier.size(20.dp)
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
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice search",
                                tint = SonzaOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Divider below search bar
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = SpacingTokens.SpaceXs),
                thickness = 0.5.dp,
                color = SonzaOutline.copy(alpha = 0.20f)
            )

            // Results Category Navigation (Clean horizontally scrollable category pills)
            AnimatedVisibility(
                visible = !isSearchActive && uiState.query.isNotBlank(),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                SearchCategoryTabs(
                    selectedFilter = uiState.resultFilter,
                    onFilterSelected = { viewModel.setResultFilter(it) },
                    accentColor = accentColor,
                    modifier = Modifier.padding(vertical = SpacingTokens.SpaceXs)
                )
            }

            // Content Area: Suggestions View OR Recent Searches / Discovery View OR Results View
            if (isSearchActive && uiState.showSuggestions && uiState.query.isNotBlank() && uiState.suggestions.isNotEmpty()) {
                // Live Query Suggestions View
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = dynamicBottomInset)
                ) {
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
            } else if (uiState.query.isBlank()) {
                // Initial Landing / Recent Searches + Discovery View
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = dynamicBottomInset)
                ) {
                    // Recent Searches Section
                    if (uiState.recentSearches.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = SpacingTokens.SpaceLg,
                                        end = SpacingTokens.SpaceLg,
                                        top = SpacingTokens.SpaceMd,
                                        bottom = SpacingTokens.SpaceSm
                                    ),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Recent searches",
                                    style = SonzaTypography.SectionTitle,
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
                                    when (item) {
                                        is RecentSearchItem.SongItem -> {
                                            viewModel.addToRecentSearches(item.song)
                                            onSongClick(listOf(item.song), 0)
                                        }
                                        is RecentSearchItem.AlbumItem -> {
                                            viewModel.addToRecentSearches(item.album)
                                            onAlbumClick(item.album)
                                        }
                                        is RecentSearchItem.PlaylistItem -> {
                                            viewModel.addToRecentSearches(item.playlist)
                                            onPlaylistClick(item.playlist.id)
                                        }
                                        is RecentSearchItem.QueryItem -> {
                                            viewModel.onRecentSearchClick(item)
                                            isSearchActive = false
                                            focusManager.clearFocus()
                                        }
                                    }
                                },
                                onDelete = {
                                    viewModel.removeRecentSearch(item)
                                }
                            )
                        }
                    } else if (isSearchActive) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SonzaEmptyState(
                                title = "No recent searches",
                                description = "Search for songs, artists, or albums to get started.",
                                modifier = Modifier.padding(SpacingTokens.SpaceLg)
                            )
                        }
                    }

                    // Browse Categories (shown on Discovery screen when not in active search)
                    if (!isSearchActive && uiState.browseCategories.isNotEmpty()) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Text(
                                text = "Browse all",
                                style = SonzaTypography.SectionTitle,
                                color = SonzaOnBackground,
                                modifier = Modifier.padding(
                                    start = SpacingTokens.SpaceLg,
                                    end = SpacingTokens.SpaceLg,
                                    top = SpacingTokens.SpaceLg,
                                    bottom = SpacingTokens.SpaceSm
                                )
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = SpacingTokens.SpaceLg,
                                        end = SpacingTokens.SpaceLg,
                                        top = SpacingTokens.SpaceSm,
                                        bottom = SpacingTokens.SpaceMd
                                    )
                            ) {
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
                                        if (rowItems.size == 1) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Results View (When Query is Entered)
                LazyVerticalGrid(
                    state = gridState,
                    columns = GridCells.Fixed(1),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = dynamicBottomInset)
                ) {
                    // Loading Skeleton State (Q15: Matches result row layout)
                    if (uiState.isLoading) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SearchResultsSkeleton(modifier = Modifier.padding(top = SpacingTokens.SpaceSm))
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
                        // ResultFilter.ALL (Curated Music Grouping per Q20 & Q21)
                        if (uiState.artistResults.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Artists",
                                        style = SonzaTypography.SectionTitle,
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
                                }
                            }
                        }

                        if (uiState.albumResults.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Albums",
                                        style = SonzaTypography.SectionTitle,
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
                                }
                            }
                        }

                        if (uiState.playlistResults.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Playlists",
                                        style = SonzaTypography.SectionTitle,
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
                                }
                            }
                        }

                        if (uiState.results.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "Songs",
                                    style = SonzaTypography.SectionTitle,
                                    color = SonzaOnBackground,
                                    modifier = Modifier.padding(
                                        start = SpacingTokens.SpaceLg,
                                        end = SpacingTokens.SpaceLg,
                                        top = SpacingTokens.SpaceSm,
                                        bottom = SpacingTokens.SpaceXs
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

                    // Empty or Error State for Results (Q16, Q17: Clean user-friendly states)
                    val hasAnyResult = uiState.results.isNotEmpty() ||
                            uiState.artistResults.isNotEmpty() ||
                            uiState.albumResults.isNotEmpty() ||
                            uiState.playlistResults.isNotEmpty()

                    if (!hasAnyResult) {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            if (uiState.error != null) {
                                SonzaErrorState(
                                    title = "Couldn't load results",
                                    message = "Check your connection and try again.",
                                    onRetry = { viewModel.search(saveToHistory = false) },
                                    modifier = Modifier.padding(SpacingTokens.SpaceLg)
                                )
                            } else {
                                SonzaEmptyState(
                                    title = "No results found",
                                    description = "Try a different song, artist, or album keyword.",
                                    icon = Icons.Rounded.SearchOff,
                                    actionText = "Clear search",
                                    onActionClick = { viewModel.onQueryChange("") },
                                    modifier = Modifier.padding(SpacingTokens.SpaceLg)
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        // Bottom Sheets & Dialogs (Q27: Real supported actions only)
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
 * Category navigation pill row per Q3 & Q4.
 * Replaces generic segmented control with horizontally scrollable music-app category pills.
 */
@Composable
private fun SearchCategoryTabs(
    selectedFilter: ResultFilter,
    onFilterSelected: (ResultFilter) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val filters = listOf(
        ResultFilter.ALL to "All",
        ResultFilter.SONGS to "Songs",
        ResultFilter.VIDEOS to "Videos",
        ResultFilter.ALBUMS to "Albums",
        ResultFilter.ARTISTS to "Artists",
        ResultFilter.COMMUNITY_PLAYLISTS to "Playlists"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = SpacingTokens.SpaceLg),
        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceSm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { (filter, label) ->
            val isSelected = selectedFilter == filter

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) accentColor.copy(alpha = 0.16f) else SonzaSurfaceVariant.copy(alpha = 0.6f),
                animationSpec = tween(durationMillis = MotionTokens.NavSelectionDuration, easing = FastOutSlowInEasing),
                label = "catBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) accentColor else SonzaOnSurfaceVariant,
                animationSpec = tween(durationMillis = MotionTokens.NavSelectionDuration, easing = FastOutSlowInEasing),
                label = "catText"
            )
            val borderColor by animateColorAsState(
                targetValue = if (isSelected) accentColor.copy(alpha = 0.45f) else SonzaOutline.copy(alpha = 0.35f),
                animationSpec = tween(durationMillis = MotionTokens.NavSelectionDuration, easing = FastOutSlowInEasing),
                label = "catBorder"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(RadiusTokens.Pill))
                    .background(backgroundColor)
                    .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(RadiusTokens.Pill))
                    .bounceClick(scaleDown = MotionTokens.CardTapScale) { onFilterSelected(filter) }
                    .padding(horizontal = SpacingTokens.SpaceMd, vertical = SpacingTokens.SpaceSm)
                    .semantics {
                        role = Role.Tab
                        contentDescription = "$label category, ${if (isSelected) "selected" else "not selected"}"
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = SonzaTypography.LabelMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    ),
                    color = textColor
                )
            }
        }
    }
}

/**
 * Compact music result row per Q5–Q13.
 *
 * Preferred structure:
 * [Artwork 48dp]   Song Title (Manrope TitleMedium 15sp Bold, 1 line)
 *                  Artist (Manrope BodyMedium 13sp Muted)               [⋮ 20dp]
 *
 * Click entire row -> 0.97 scale feedback -> play song.
 * No redundant "Song • " metadata.
 */
@Composable
private fun SearchResultItem(
    song: Song,
    onClick: () -> Unit,
    onArtistClick: (String) -> Unit = {},
    onMoreClick: () -> Unit
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val isVideo = song.isVideo

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 2.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "${song.title}, ${song.artist}, ${if (isVideo) "video" else "song"}"
            },
        shape = RoundedCornerShape(RadiusTokens.Sm),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpacingTokens.SpaceXs, horizontal = SpacingTokens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork (48dp x 48dp, consistent square size, RadiusTokens.Sm)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(SonzaSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!song.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(song.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = SonzaOnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            // Song Info (Title bold hierarchy, Artist subordinate, single-line truncation)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = song.title,
                    style = SonzaTypography.SongTitle,
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val artistId = song.artistId
                    Text(
                        text = if (isVideo) "${song.artist} • Video" else song.artist,
                        style = SonzaTypography.ArtistSubtitle,
                        color = SonzaOnSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = if (artistId != null) {
                            Modifier.clickable(onClick = { onArtistClick(artistId) })
                        } else Modifier
                    )
                }
            }

            // Three-dot action button (Material Symbols MoreVert, visually secondary)
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "More options for ${song.title}" }
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = SonzaOnSurfaceVariant.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Compact Artist search row.
 */
@Composable
fun ArtistSearchListItem(artist: Artist, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 2.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "${artist.name}, artist"
            },
        shape = RoundedCornerShape(RadiusTokens.Sm),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpacingTokens.SpaceXs, horizontal = SpacingTokens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SonzaSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!artist.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(artist.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = artist.name,
                    style = SonzaTypography.SongTitle,
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = artist.subscribers ?: "Artist",
                    style = SonzaTypography.ArtistSubtitle,
                    color = SonzaOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Compact Album search row.
 */
@Composable
fun AlbumSearchListItem(album: Album, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 2.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "${album.title}, album by ${album.artist}"
            },
        shape = RoundedCornerShape(RadiusTokens.Sm),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpacingTokens.SpaceXs, horizontal = SpacingTokens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(SonzaSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!album.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(album.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = album.title,
                    style = SonzaTypography.SongTitle,
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtitle = (if (album.artist.isNotBlank()) album.artist else "") + (if (album.year != null) " • ${album.year}" else "")
                Text(
                    text = subtitle.ifBlank { "Album" },
                    style = SonzaTypography.ArtistSubtitle,
                    color = SonzaOnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Compact Playlist search row.
 */
@Composable
fun PlaylistSearchListItem(playlist: Playlist, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 2.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = "${playlist.title}, playlist"
            },
        shape = RoundedCornerShape(RadiusTokens.Sm),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = SpacingTokens.SpaceXs, horizontal = SpacingTokens.SpaceXs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(RadiusTokens.Sm))
                    .background(SonzaSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!playlist.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(playlist.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = playlist.title,
                    style = SonzaTypography.SongTitle,
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (playlist.author.isNotBlank()) playlist.author else "Playlist",
                    style = SonzaTypography.ArtistSubtitle,
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
            .width(96.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(artist.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = artist.name,
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(SonzaSurfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        Text(
            text = artist.name,
            style = SonzaTypography.CardTitle,
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = artist.subscribers ?: "Artist",
            style = SonzaTypography.CardSubtitle,
            color = SonzaOnSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun PlaylistSearchCard(playlist: Playlist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(130.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(playlist.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = playlist.title,
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(RadiusTokens.Md))
                .background(SonzaSurfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        Text(
            text = playlist.title,
            style = SonzaTypography.CardTitle,
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (playlist.author.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = playlist.author,
                style = SonzaTypography.CardSubtitle,
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
            .width(130.dp)
            .bounceClick(scaleDown = MotionTokens.CardTapScale, onClick = onClick)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(album.thumbnailUrl)
                .crossfade(true)
                .build(),
            contentDescription = album.title,
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(RadiusTokens.Md))
                .background(SonzaSurfaceVariant),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(SpacingTokens.SpaceSm))
        Text(
            text = album.title,
            style = SonzaTypography.CardTitle,
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        val subtitle = (if (album.artist.isNotBlank()) album.artist else "") + (if (album.year != null) " • ${album.year}" else "")
        if (subtitle.isNotBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = SonzaTypography.CardSubtitle,
                color = SonzaOnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Search history row with compact 56dp height, prominent remove action, and TalkBack accessibility semantics.
 */
@Composable
private fun ActiveSearchHistoryRow(
    item: RecentSearchItem,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val title = item.title
    val subtitle = when (item) {
        is RecentSearchItem.SongItem -> item.song.artist
        is RecentSearchItem.AlbumItem -> if (item.album.artist.isNotBlank()) "Album • ${item.album.artist}" else "Album"
        is RecentSearchItem.PlaylistItem -> if (item.playlist.author.isNotBlank()) "Playlist • ${item.playlist.author}" else "Playlist"
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
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = 2.dp)
            .clip(RoundedCornerShape(RadiusTokens.Sm))
            .clickable(onClick = onClick),
        color = Color.Transparent,
        shape = RoundedCornerShape(RadiusTokens.Sm)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = SpacingTokens.SpaceSm),
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
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(RadiusTokens.Sm),
                    color = SonzaSurfaceVariant
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.History,
                            contentDescription = null,
                            tint = SonzaOnSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(SpacingTokens.SpaceMd))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = SpacingTokens.SpaceSm),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = SonzaTypography.SongTitle,
                    color = SonzaOnBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = SonzaTypography.ArtistSubtitle,
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
                    tint = SonzaOnSurfaceVariant.copy(alpha = 0.70f),
                    modifier = Modifier.size(18.dp)
                )
            }
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
