package com.sonza.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.Artist
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.MusicSource
import com.sonza.app.core.model.RecentSearchItem
import com.sonza.app.ui.components.AddToPlaylistSheet
import com.sonza.app.ui.components.CreatePlaylistDialog
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SearchResultsSkeleton
import com.sonza.app.ui.components.SonzaEmptyState
import com.sonza.app.ui.components.SonzaErrorState
import com.sonza.app.ui.components.SongMenuBottomSheet
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.theme.*
import com.sonza.app.ui.viewmodel.PlaylistManagementViewModel
import com.sonza.app.ui.viewmodel.ResultFilter
import com.sonza.app.ui.viewmodel.SearchTab
import com.sonza.app.ui.viewmodel.SearchViewModel
import com.sonza.app.util.dpadFocusable

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
    
    // Scroll tracking for hiding headers
    var isHeaderVisible by remember { mutableStateOf(true) }
    
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta < -20 && isHeaderVisible) {
                    isHeaderVisible = false
                } else if (delta > 20 && !isHeaderVisible) {
                    isHeaderVisible = true
                }
                return Offset.Zero
            }
        }
    }

    // Always show header at the very top
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
        if (listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0) {
            isHeaderVisible = true
        }
    }
    
    // Always show headers when search is expanded/active
    val effectiveHeaderVisibility = isHeaderVisible || isSearchActive
    
    androidx.compose.runtime.LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            focusManager.clearFocus()
        }
    }
    
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent
    
    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is com.sonza.app.ui.viewmodel.SearchEvent.ShowAddToPlaylistSheet -> {
                    playlistViewModel.showAddToPlaylistSheet(event.song)
                }
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val voiceSearchLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == android.app.Activity.RESULT_OK) {
                    val spokenText = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)?.get(0)
                    if (!spokenText.isNullOrBlank()) {
                        viewModel.onQueryChange(spokenText)
                        viewModel.search()
                    }
                }
            }

            AnimatedVisibility(
                visible = effectiveHeaderVisibility,
                enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + 
                        expandVertically(animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow)),
                exit = fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) + 
                       shrinkVertically(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
            ) {
                Column {
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
                                        "Search for songs, artists, or albums",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                leadingIcon = {
                                    if (isSearchActive) {
                                        IconButton(onClick = {
                                            isSearchActive = false
                                            viewModel.onBackPressed()
                                            focusManager.clearFocus()
                                        }) {
                                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                                        }
                                    } else {
                                        Icon(Icons.Default.Search, "Search")
                                    }
                                },
                                trailingIcon = {
                                    if (uiState.query.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.onQueryChange("") }) {
                                            Icon(Icons.Default.Clear, "Clear")
                                        }
                                    } else {
                                        IconButton(onClick = {
                                            val intent = android.content.Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                putExtra(android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL, android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                                putExtra(android.speech.RecognizerIntent.EXTRA_PROMPT, "Speak to search")
                                            }
                                            try { voiceSearchLauncher.launch(intent) }
                                            catch (e: Exception) {
                                                com.sonza.app.util.SnackbarUtil.showWarning("Voice search not supported")
                                            }
                                        }) {
                                            Icon(Icons.Default.Mic, "Voice Search")
                                        }
                                    }
                                }
                            )
                        },
                        expanded = isSearchActive,
                        onExpandedChange = { isSearchActive = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = SearchBarDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ),
                        shape = SquircleShape
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            if (uiState.showSuggestions && uiState.query.isNotBlank() && uiState.suggestions.isNotEmpty()) {
                                items(
                                    items = uiState.suggestions.take(5),
                                    key = { it },
                                    contentType = { "suggestion" }
                                ) { suggestion ->
                                    SuggestionItem(
                                        suggestion = suggestion,
                                        accentColor = accentColor,
                                        onClick = {
                                            viewModel.onSuggestionClick(suggestion)
                                            isSearchActive = false
                                        }
                                    )
                                }
                            }
                            
                            // Zero-state mood row — fills the blank pre-typing screen with
                            // one-tap searches instead of leaving it empty.
                            if (uiState.query.isBlank() && uiState.selectedTab == SearchTab.YOUTUBE_MUSIC) {
                                item {
                                    Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                                        Text(
                                            text = androidx.compose.ui.res.stringResource(com.sonza.app.R.string.label_browse_by_mood),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                                        )
                                        MoodChipsSection(
                                            selectedMood = null,
                                            onMoodSelected = { mood ->
                                                viewModel.onQueryChange("$mood songs")
                                                viewModel.search(saveToHistory = false)
                                            }
                                        )
                                    }
                                }
                            }

                            if (uiState.query.isBlank() && uiState.recentSearches.isNotEmpty()) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Recently Searched",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        
                                        Text(
                                            text = "Clear",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = accentColor,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.clickable { viewModel.clearRecentSearches() }
                                        )
                                    }
                                }
                                
                                items(
                                    items = uiState.recentSearches,
                                    key = { it.id },
                                    contentType = { it.javaClass.simpleName }
                                ) { item ->
                                    RecentSearchItemRow(
                                        item = item,
                                        onSongClick = onSongClick,
                                        onArtistClick = onArtistClick,
                                        onAlbumClick = onAlbumClick,
                                        onPlaylistClick = onPlaylistClick,
                                        onMoreClick = { song ->
                                            selectedSong = song
                                            showSongMenu = true
                                        },
                                        viewModel = viewModel
                                    )
                                }
                            }
                        }
                    }

                    // Tab Selection (YouTube Music / HQ Audio). YouTube Music is the default search source.
                    // Both tabs are always available so the user can always cross-search the other catalogue.
                    val visibleTabs = remember {
                        listOf(SearchTab.YOUTUBE_MUSIC, SearchTab.REMOTE)
                    }
                    val visibleSelectedIdx = visibleTabs.indexOf(uiState.selectedTab).coerceAtLeast(0)
                    TabRow(
                        selectedTabIndex = visibleSelectedIdx,
                        containerColor = Color.Transparent,
                        contentColor = accentColor,
                        divider = {},
                        indicator = { tabPositions ->
                            if (visibleSelectedIdx < tabPositions.size) {
                                TabRowDefaults.SecondaryIndicator(
                                    Modifier.tabIndicatorOffset(tabPositions[visibleSelectedIdx]),
                                    color = accentColor
                                )
                            }
                        },
                        modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg)
                    ) {
                        visibleTabs.forEach { tab ->
                            val label = when (tab) {
                                SearchTab.YOUTUBE_MUSIC -> "YouTube Music"
                                SearchTab.REMOTE -> "HQ Audio"
                            }
                            val isSelected = uiState.selectedTab == tab
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.onTabChange(tab) },
                                text = {
                                    Text(
                                        text = label,
                                        style = SonzaTypography.LabelLarge.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) accentColor else SonzaOnSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                    
                    // Category Chips for YouTube / REMOTE Tab
                    if (uiState.selectedTab == SearchTab.YOUTUBE_MUSIC || uiState.selectedTab == SearchTab.REMOTE) {
                        AnimatedVisibility(
                            visible = uiState.query.isNotBlank(),
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val filters = if (uiState.selectedTab == SearchTab.REMOTE) {
                                listOf(
                                    ResultFilter.ALL to "All",
                                    ResultFilter.SONGS to "Songs",
                                    ResultFilter.ALBUMS to "Albums",
                                    ResultFilter.ARTISTS to "Artists",
                                    ResultFilter.COMMUNITY_PLAYLISTS to "Playlists"
                                )
                            } else {
                                listOf(
                                    ResultFilter.ALL to "All",
                                    ResultFilter.SONGS to "Songs",
                                    ResultFilter.VIDEOS to "Videos",
                                    ResultFilter.ALBUMS to "Albums",
                                    ResultFilter.ARTISTS to "Artists",
                                    ResultFilter.COMMUNITY_PLAYLISTS to "Community",
                                    ResultFilter.FEATURED_PLAYLISTS to "Featured"
                                )
                            }
                            
                            SingleChoiceSegmentedButtonRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = SpacingTokens.SpaceLg, vertical = 6.dp)
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
                    }
                }
            }
            
            // Content Area
            val windowSize = com.sonza.app.ui.utils.rememberWindowSize()
            val gridColumns = when (windowSize) {
                com.sonza.app.ui.utils.WindowSize.Compact -> 1
                else -> 2
            }

            androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                state = androidx.compose.foundation.lazy.grid.rememberLazyGridState(),
                columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection),
                contentPadding = PaddingValues(bottom = 140.dp)
            ) {
                if (uiState.isLoading) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        // Skeleton rows instead of a bare spinner
                        SearchResultsSkeleton(modifier = Modifier.padding(top = 8.dp))
                    }
                }
                
                if (uiState.selectedTab == SearchTab.YOUTUBE_MUSIC) {
                    if (uiState.resultFilter != ResultFilter.ALL && !uiState.isLoading) {
                        when (uiState.resultFilter) {
                            ResultFilter.SONGS, ResultFilter.VIDEOS -> {
                                itemsIndexed(uiState.results, key = { _, song -> "song_${song.id}" }) { index, song ->
                                    SearchResultItem(
                                        song = song,
                                        onClick = { viewModel.addToRecentSearches(song); onSongClick(uiState.results, index) },
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
                                    AlbumSearchListItem(album = album, onClick = { viewModel.addToRecentSearches(album); onAlbumClick(album) })
                                }
                            }
                            ResultFilter.COMMUNITY_PLAYLISTS, ResultFilter.FEATURED_PLAYLISTS -> {
                                items(uiState.playlistResults, key = { it.id }) { playlist ->
                                    PlaylistSearchListItem(playlist = playlist, onClick = { viewModel.addToRecentSearches(playlist); onPlaylistClick(playlist.id) })
                                }
                            }
                            else -> {}
                        }
                    } else if (uiState.resultFilter == ResultFilter.ALL && !uiState.isLoading && uiState.query.isNotBlank()) {
                        if (uiState.artistResults.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Artists",
                                        style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SonzaOnBackground,
                                        modifier = Modifier.padding(start = SpacingTokens.SpaceLg, end = SpacingTokens.SpaceLg, top = SpacingTokens.SpaceSm)
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
                                    HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm))
                                }
                            }
                        }
                        if (uiState.playlistResults.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Playlists",
                                        style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SonzaOnBackground,
                                        modifier = Modifier.padding(start = SpacingTokens.SpaceLg, end = SpacingTokens.SpaceLg, top = SpacingTokens.SpaceSm)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd),
                                        modifier = Modifier.padding(vertical = SpacingTokens.SpaceMd)
                                    ) {
                                        items(uiState.playlistResults, key = { it.id }) { playlist ->
                                            PlaylistSearchCard(playlist = playlist, onClick = { viewModel.addToRecentSearches(playlist); onPlaylistClick(playlist.id) })
                                        }
                                    }
                                    HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm))
                                }
                            }
                        }
                        if (uiState.albumResults.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Column {
                                    Text(
                                        text = "Albums",
                                        style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = SonzaOnBackground,
                                        modifier = Modifier.padding(start = SpacingTokens.SpaceLg, end = SpacingTokens.SpaceLg, top = SpacingTokens.SpaceSm)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg),
                                        horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd),
                                        modifier = Modifier.padding(vertical = SpacingTokens.SpaceMd)
                                    ) {
                                        items(uiState.albumResults, key = { it.id }) { album ->
                                            AlbumSearchCard(album = album, onClick = { viewModel.addToRecentSearches(album); onAlbumClick(album) })
                                        }
                                    }
                                    HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm))
                                }
                            }
                        }
                        if (uiState.results.isNotEmpty()) {
                            item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = "Songs",
                                    style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SonzaOnBackground,
                                    modifier = Modifier.padding(start = SpacingTokens.SpaceLg, end = SpacingTokens.SpaceLg, top = SpacingTokens.SpaceSm, bottom = SpacingTokens.SpaceSm)
                                )
                            }
                            itemsIndexed(uiState.results, key = { _, song -> "main_song_${song.id}" }) { index, song ->
                                SearchResultItem(
                                    song = song,
                                    onClick = { viewModel.addToRecentSearches(song); onSongClick(uiState.results, index) },
                                    onArtistClick = onArtistClick,
                                    onMoreClick = { selectedSong = song; showSongMenu = true }
                                )
                            }
                        }
                    }
                } else if (uiState.selectedTab == SearchTab.REMOTE) {
                    // RemoteAudio results (320 kbps HQ audio)
                    if (!uiState.isLoading && uiState.query.isNotBlank()) {
                        if (uiState.resultFilter != ResultFilter.ALL) {
                            when (uiState.resultFilter) {
                                ResultFilter.SONGS, ResultFilter.VIDEOS -> {
                                    itemsIndexed(uiState.results, key = { _, song -> "remote_song_${song.id}" }) { index, song ->
                                        SearchResultItem(
                                            song = song,
                                            onClick = { viewModel.addToRecentSearches(song); onSongClick(uiState.results, index) },
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
                                        AlbumSearchListItem(album = album, onClick = { viewModel.addToRecentSearches(album); onAlbumClick(album) })
                                    }
                                }
                                ResultFilter.COMMUNITY_PLAYLISTS, ResultFilter.FEATURED_PLAYLISTS -> {
                                    items(uiState.playlistResults, key = { it.id }) { playlist ->
                                        PlaylistSearchListItem(playlist = playlist, onClick = { viewModel.addToRecentSearches(playlist); onPlaylistClick(playlist.id) })
                                    }
                                }
                                else -> {}
                            }

                            val activeResultsEmpty = when (uiState.resultFilter) {
                                ResultFilter.SONGS, ResultFilter.VIDEOS -> uiState.results.isEmpty()
                                ResultFilter.ARTISTS -> uiState.artistResults.isEmpty()
                                ResultFilter.ALBUMS -> uiState.albumResults.isEmpty()
                                ResultFilter.COMMUNITY_PLAYLISTS, ResultFilter.FEATURED_PLAYLISTS -> uiState.playlistResults.isEmpty()
                                else -> false
                            }
                            if (activeResultsEmpty) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    if (uiState.error != null) {
                                        SonzaErrorState(
                                            title = "HQ Search Failed",
                                            message = uiState.error,
                                            onRetry = { viewModel.search(saveToHistory = false) },
                                            modifier = Modifier.padding(SpacingTokens.SpaceLg)
                                        )
                                    } else {
                                        SonzaEmptyState(
                                            title = "No HQ Audio results",
                                            description = "No results found in HQ Audio for \"${uiState.query}\". Switch to YouTube Music for broader catalogue.",
                                            actionText = "Switch to YouTube Music",
                                            onActionClick = { viewModel.onTabChange(SearchTab.YOUTUBE_MUSIC) },
                                            modifier = Modifier.padding(SpacingTokens.SpaceLg)
                                        )
                                    }
                                }
                            }
                        } else {
                            if (uiState.artistResults.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    Column {
                                        Text("Artists", style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold), color = SonzaOnBackground, modifier = Modifier.padding(start = SpacingTokens.SpaceLg, end = SpacingTokens.SpaceLg, top = SpacingTokens.SpaceSm))
                                        LazyRow(contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg), horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceLg), modifier = Modifier.padding(vertical = SpacingTokens.SpaceMd)) {
                                            items(uiState.artistResults, key = { it.id }) { artist -> ArtistSearchCard(artist = artist, onClick = { onArtistClick(artist.id) }) }
                                        }
                                        HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm))
                                    }
                                }
                            }
                            if (uiState.playlistResults.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    Column {
                                        Text("Playlists", style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold), color = SonzaOnBackground, modifier = Modifier.padding(start = SpacingTokens.SpaceLg, end = SpacingTokens.SpaceLg, top = SpacingTokens.SpaceSm))
                                        LazyRow(contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg), horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd), modifier = Modifier.padding(vertical = SpacingTokens.SpaceMd)) {
                                            items(uiState.playlistResults, key = { it.id }) { playlist -> PlaylistSearchCard(playlist = playlist, onClick = { viewModel.addToRecentSearches(playlist); onPlaylistClick(playlist.id) }) }
                                        }
                                        HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm))
                                    }
                                }
                            }
                            if (uiState.albumResults.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    Column {
                                        Text("Albums", style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold), color = SonzaOnBackground, modifier = Modifier.padding(start = SpacingTokens.SpaceLg, end = SpacingTokens.SpaceLg, top = SpacingTokens.SpaceSm))
                                        LazyRow(contentPadding = PaddingValues(horizontal = SpacingTokens.SpaceLg), horizontalArrangement = Arrangement.spacedBy(SpacingTokens.SpaceMd), modifier = Modifier.padding(vertical = SpacingTokens.SpaceMd)) {
                                            items(uiState.albumResults, key = { it.id }) { album -> AlbumSearchCard(album = album, onClick = { viewModel.addToRecentSearches(album); onAlbumClick(album) }) }
                                        }
                                        HorizontalDivider(color = SonzaOutline.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceSm))
                                    }
                                }
                            }
                            if (uiState.results.isNotEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    Text("Songs", style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold), color = SonzaOnBackground, modifier = Modifier.padding(start = SpacingTokens.SpaceLg, end = SpacingTokens.SpaceLg, top = SpacingTokens.SpaceSm, bottom = SpacingTokens.SpaceSm))
                                }
                                itemsIndexed(uiState.results, key = { _, song -> "remote_${song.id}" }) { index, song ->
                                    SearchResultItem(
                                        song = song,
                                        onClick = { viewModel.addToRecentSearches(song); onSongClick(uiState.results, index) },
                                        onArtistClick = onArtistClick,
                                        onMoreClick = { selectedSong = song; showSongMenu = true }
                                    )
                                }
                            }

                            if (uiState.results.isEmpty() && uiState.artistResults.isEmpty() && uiState.albumResults.isEmpty() && uiState.playlistResults.isEmpty()) {
                                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                                    SonzaEmptyState(
                                        title = "No HQ Audio results",
                                        description = "No results found for \"${uiState.query}\" in HQ Audio catalogue.",
                                        actionText = "Switch to YouTube Music",
                                        onActionClick = { viewModel.onTabChange(SearchTab.YOUTUBE_MUSIC) },
                                        modifier = Modifier.padding(SpacingTokens.SpaceLg)
                                    )
                                }
                            }
                        }
                    }
                }

                if (uiState.selectedTab == SearchTab.YOUTUBE_MUSIC && uiState.query.isNotBlank() && uiState.results.isEmpty() && !uiState.isLoading && uiState.artistResults.isEmpty() && uiState.albumResults.isEmpty() && uiState.playlistResults.isEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
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
                
                // "Start browsing" — Spotify-style colored category tiles
                if (uiState.query.isBlank() && uiState.selectedTab == SearchTab.YOUTUBE_MUSIC && uiState.browseCategories.isNotEmpty()) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceLg)) {
                            Text(
                                text = "Start browsing",
                                style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SonzaOnBackground,
                                modifier = Modifier.padding(bottom = SpacingTokens.SpaceMd)
                            )
                            uiState.browseCategories.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = SpacingTokens.SpaceMd),
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

                if (uiState.query.isBlank() && uiState.recentSearches.isEmpty() && uiState.selectedTab == SearchTab.YOUTUBE_MUSIC) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceLg)) {
                            Text(
                                text = "Trending Searches",
                                style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                                color = SonzaOnBackground,
                                modifier = Modifier.padding(bottom = SpacingTokens.SpaceMd)
                            )
                            uiState.trendingSearches.forEach { term ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(RadiusTokens.Md))
                                        .clickable { viewModel.onTrendingSearchClick(term) }
                                        .padding(vertical = SpacingTokens.SpaceMd, horizontal = SpacingTokens.SpaceSm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = SonzaOnSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(SpacingTokens.SpaceLg))
                                    Text(
                                        text = term,
                                        style = SonzaTypography.BodyLarge,
                                        color = SonzaOnBackground
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (showSongMenu && selectedSong != null) {
            val song = selectedSong!!
            SongMenuBottomSheet(
                isVisible = showSongMenu, onDismiss = { showSongMenu = false }, song = song,
                isCurrentlyPlaying = song.id == currentSong?.id,
                onPlayNext = { viewModel.playNext(song) }, onAddToQueue = { viewModel.addToQueue(song) },
                onAddToPlaylist = { viewModel.addToPlaylist(song) }, onDownload = { viewModel.downloadSong(song) },
                onShare = {
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "Check out this song: ${song.title} by ${song.artist}\n\nhttps://music.youtube.com/watch?v=${song.id}")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Song"))
                },
                onViewArtist = song.artistId?.let { id -> { onArtistClick(id) } }, onViewAlbum = null
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
             CreatePlaylistDialog(isVisible = playlistMgmtState.showCreatePlaylistDialog, isCreating = playlistMgmtState.isCreatingPlaylist, onDismiss = { playlistViewModel.hideCreatePlaylistDialog() }, onCreate = { title, description, isPrivate, syncWithYt -> playlistViewModel.createPlaylist(title, description, isPrivate, syncWithYt) }, isLoggedIn = true)
        }
    }
}

/**
 * Spotify-style "Browse all" tile per DESIGN_SYSTEM.md Part 6.1
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
            modifier = Modifier.padding(SpacingTokens.SpaceMd).align(Alignment.TopStart)
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

/**
 * Result Card for Songs per DESIGN_SYSTEM.md Part 6 & Part 8:
 * - Whole row clickable with scale 0.97 tap feedback
 * - SonzaSurface background, radius-md, outline border
 * - Manrope TitleMedium for song title, BodyMedium for artist
 * - Skeleton + crossfade thumbnail
 */
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

@Composable
private fun RecentSearchItemRow(
    item: RecentSearchItem,
    onSongClick: (List<Song>, Int) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (Album) -> Unit,
    onPlaylistClick: (String) -> Unit,
    onMoreClick: (Song) -> Unit,
    viewModel: SearchViewModel
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    
    when (item) {
        is RecentSearchItem.SongItem -> SearchResultItem(
            song = item.song,
            onClick = { viewModel.onRecentSearchClick(item); onSongClick(listOf(item.song), 0) },
            onArtistClick = onArtistClick,
            onMoreClick = { onMoreClick(item.song) }
        )
        is RecentSearchItem.AlbumItem -> AlbumSearchListItem(
            album = item.album,
            onClick = { viewModel.onRecentSearchClick(item); onAlbumClick(item.album) }
        )
        is RecentSearchItem.PlaylistItem -> PlaylistSearchListItem(
            playlist = item.playlist,
            onClick = { viewModel.onRecentSearchClick(item); onPlaylistClick(item.playlist.id) }
        )
        is RecentSearchItem.QueryItem -> QuerySearchItem(
            query = item.query,
            accentColor = dynamicColors.accent,
            onClick = { viewModel.onRecentSearchClick(item) }
        )
    }
}

@Composable
private fun QuerySearchItem(query: String, accentColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = SpacingTokens.SpaceLg, vertical = SpacingTokens.SpaceMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.History,
            contentDescription = null,
            tint = SonzaOnSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(SpacingTokens.SpaceLg))
        Text(
            text = query,
            style = SonzaTypography.BodyLarge,
            color = SonzaOnBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

