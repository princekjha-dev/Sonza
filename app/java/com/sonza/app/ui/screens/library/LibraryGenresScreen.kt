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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.utils.DiscoveryArtRegistry
import com.sonza.app.ui.viewmodel.LibraryViewModel
import org.koin.compose.viewmodel.koinViewModel

data class LibraryGenreData(
    val title: String,
    val imageUrl: String,
    val tintColor: Color,
    val songCount: Int
)

@Composable
fun LibraryGenresScreen(
    onBackClick: () -> Unit,
    onGenreClick: (String) -> Unit,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val genresList = remember(uiState.downloadedSongs, uiState.localSongs, uiState.likedSongs) {
        val curated = listOf(
            "Pop", "Hits", "Hip-Hop", "Rock", "R&B", "Electronic",
            "Country", "Latin", "Chill", "Classical", "Jazz", "Indie",
            "Ambient", "Metal", "K-Pop", "Soul", "Folk", "Holiday"
        )
        curated.map { genreName ->
            val meta = DiscoveryArtRegistry.get(genreName)
            val songs = viewModel.getSongsForGenre(genreName)
            LibraryGenreData(
                title = genreName,
                imageUrl = meta?.imageUrl ?: "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?q=80&w=800&auto=format&fit=crop",
                tintColor = meta?.let { Color(it.tintColor) } ?: SonzaColors.BrandAccent,
                songCount = songs.size
            )
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
                    text = "Genres",
                    style = SonzaTypography.Headline.copy(fontWeight = FontWeight.Bold, fontSize = 26.sp),
                    color = SonzaColors.OnBackground,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp)
                )
            }

            // 2-Column Curated Genre Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 140.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(genresList, key = { it.title }) { genre ->
                    GenreCardItem(
                        genre = genre,
                        onClick = { onGenreClick(genre.title) }
                    )
                }
            }
        }
    }
}

@Composable
fun GenreCardItem(
    genre: LibraryGenreData,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.5f)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        // Background Artwork
        AsyncImage(
            model = genre.imageUrl,
            contentDescription = genre.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            genre.tintColor.copy(alpha = 0.4f),
                            SonzaColors.Background.copy(alpha = 0.85f)
                        )
                    )
                )
        )

        // Text Info
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = genre.title,
                style = SonzaTypography.TitleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (genre.songCount > 0) {
                Text(
                    text = "${genre.songCount} ${if (genre.songCount == 1) "song" else "songs"}",
                    style = SonzaTypography.BodySmall.copy(fontSize = 12.sp),
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}
