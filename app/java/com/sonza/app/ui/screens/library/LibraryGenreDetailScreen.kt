package com.sonza.app.ui.screens.library

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.utils.DiscoveryArtRegistry
import com.sonza.app.ui.viewmodel.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryGenreDetailScreen(
    genre: String,
    onBackClick: () -> Unit,
    onSongClick: (List<Song>, Int) -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent.takeIf { it != Color.Unspecified } ?: SonzaColors.BrandAccent

    val meta = remember(genre) { DiscoveryArtRegistry.get(genre) }
    val tintColor = remember(meta) { meta?.let { Color(it.tintColor) } ?: accentColor }

    val genreSongs = remember(uiState.downloadedSongs, uiState.localSongs, uiState.likedSongs, genre) {
        viewModel.getSongsForGenre(genre)
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
                    text = genre,
                    style = SonzaTypography.Headline.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                    color = SonzaColors.OnBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }

            // Genre Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(110.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                if (meta?.imageUrl != null) {
                    AsyncImage(
                        model = meta.imageUrl,
                        contentDescription = genre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    tintColor.copy(alpha = 0.7f),
                                    SonzaColors.Background.copy(alpha = 0.9f)
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = genre,
                        style = SonzaTypography.Headline.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
                        color = Color.White
                    )
                    Text(
                        text = "${genreSongs.size} ${if (genreSongs.size == 1) "track" else "tracks"} in library",
                        style = SonzaTypography.BodyMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Play & Shuffle Action Buttons
            if (genreSongs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        onClick = { onSongClick(genreSongs, 0) },
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

                    Surface(
                        onClick = {
                            val shuffled = genreSongs.shuffled()
                            onSongClick(shuffled, 0)
                        },
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

            // Track List / Empty State
            if (genreSongs.isEmpty()) {
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
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No $genre tracks found",
                            style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = SonzaColors.OnBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Songs in your library matching this genre will appear here.",
                            style = SonzaTypography.BodyMedium,
                            color = SonzaColors.OnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(top = 4.dp, bottom = 140.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(genreSongs, key = { _, song -> song.id }) { index, song ->
                        GenreSongRow(
                            song = song,
                            onClick = { onSongClick(genreSongs, index) }
                        )
                        HorizontalDivider(
                            color = SonzaColors.Outline.copy(alpha = 0.15f),
                            thickness = 0.5.dp,
                            modifier = Modifier.padding(start = 72.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreSongRow(
    song: Song,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Artwork
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(SonzaColors.SurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (!song.thumbnailUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = song.thumbnailUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = SonzaColors.OnSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                style = SonzaTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                color = SonzaColors.OnBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = song.artist,
                style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
                color = SonzaColors.OnSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (song.duration > 0) {
            val minutes = song.duration / 60
            val seconds = song.duration % 60
            Text(
                text = "%d:%02d".format(minutes, seconds),
                style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
                color = SonzaColors.OnSurfaceVariant
            )
        }
    }
}
