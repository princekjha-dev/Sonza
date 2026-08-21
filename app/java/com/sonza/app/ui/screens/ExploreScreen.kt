package com.sonza.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import com.sonza.app.core.model.Album
import com.sonza.app.core.model.PlaylistDisplayItem
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.*
import com.sonza.app.ui.viewmodel.ExploreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onBackClick: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    onPlaylistClick: (PlaylistDisplayItem) -> Unit,
    onAlbumClick: (Album) -> Unit,
    viewModel: ExploreViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = uiState.title,
                        style = com.sonza.app.ui.theme.SonzaTypography.SectionTitle
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    HomeLoadingSkeleton()
                }
                uiState.error != null && uiState.sections.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Button(
                            onClick = { viewModel.retry() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry")
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(28.dp)
                    ) {
                        items(uiState.sections, key = { it.title }) { section ->
                            when (section.type) {
                                com.sonza.app.core.model.HomeSectionType.LargeCardWithList -> {
                                    LargeCardWithListSection(
                                        section = section,
                                        onSongClick = onSongClick,
                                        onPlaylistClick = onPlaylistClick,
                                        onAlbumClick = onAlbumClick
                                    )
                                }
                                com.sonza.app.core.model.HomeSectionType.Grid -> {
                                    GridSection(
                                        section = section,
                                        onSongClick = onSongClick,
                                        onPlaylistClick = onPlaylistClick,
                                        onAlbumClick = onAlbumClick
                                    )
                                }
                                com.sonza.app.core.model.HomeSectionType.VerticalList -> {
                                    VerticalListSection(
                                        section = section,
                                        onSongClick = onSongClick,
                                        onPlaylistClick = onPlaylistClick,
                                        onAlbumClick = onAlbumClick
                                    )
                                }
                                else -> {
                                    HorizontalCarouselSection(
                                        section = section,
                                        onSongClick = onSongClick,
                                        onPlaylistClick = onPlaylistClick,
                                        onAlbumClick = onAlbumClick
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
