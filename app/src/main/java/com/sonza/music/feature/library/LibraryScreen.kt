package com.sonza.music.feature.library

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sonza.music.core.common.TimeFormatter
import com.sonza.music.core.model.Album
import com.sonza.music.core.model.Artist
import com.sonza.music.core.model.Playlist
import com.sonza.music.core.model.Track
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaHiResBadgeBg
import com.sonza.music.core.theme.SonzaRose
import com.sonza.music.core.theme.SonzaSurface
import com.sonza.music.core.theme.SonzaTextPrimary
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.core.theme.SonzaTextTertiary

@Composable
fun LibraryScreen(
    tracks: List<Track>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    onTrackSelected: (Track) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isGridView by remember { mutableStateOf(false) }
    val tabs = listOf("Songs", "Albums", "Artists", "Playlists", "Favorites", "Local Music")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaDarkBackground)
            .padding(top = 16.dp)
    ) {
        // Library Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Alignment.CenterVertically
        ) {
            Text(
                text = "Library",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = SonzaTextPrimary
            )

            Row {
                IconButton(onClick = { isGridView = !isGridView }) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Toggle Grid/List",
                        tint = SonzaCyanAccent
                    )
                }
                if (selectedTab == 3) {
                    IconButton(onClick = onCreatePlaylist) {
                        Icon(Icons.Default.Add, contentDescription = "Create Playlist", tint = SonzaCyanAccent)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Scrollable Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = SonzaDarkBackground,
            contentColor = SonzaCyanAccent,
            edgePadding = 20.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = SonzaCyanAccent
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) SonzaCyanAccent else SonzaTextSecondary
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Tab Contents
        when (selectedTab) {
            0 -> {
                // All Songs
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tracks) { track ->
                        TrackRowItem(track = track, onClick = { onTrackSelected(track) })
                    }
                }
            }

            1 -> {
                // Albums Grid
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(albums) { album ->
                        Column(modifier = Modifier.clickable { }) {
                            AsyncImage(
                                model = album.artworkUri,
                                contentDescription = album.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(14.dp))
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = album.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SonzaTextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${album.artist} • ${album.trackCount} tracks",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SonzaTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            2 -> {
                // Artists
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(artists) { artist ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SonzaSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = artist.artworkUri,
                                    contentDescription = artist.name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = artist.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = SonzaTextPrimary
                                    )
                                    Text(
                                        text = "${artist.trackCount} tracks • ${artist.albumCount} albums",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = SonzaTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            3 -> {
                // Playlists
                if (playlists.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Start building your sound with your first playlist.", color = SonzaTextSecondary)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(playlists) { pl ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = SonzaSurface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SonzaCyanAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(pl.title.take(2).uppercase(), fontWeight = FontWeight.Bold, color = SonzaCyanAccent)
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = pl.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = SonzaTextPrimary
                                        )
                                        Text(
                                            text = "${pl.trackCount} songs",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = SonzaTextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            4 -> {
                // Favorites
                val favs = tracks.filter { it.isFavorite }
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(favs) { track ->
                        TrackRowItem(track = track, onClick = { onTrackSelected(track) })
                    }
                }
            }

            5 -> {
                // Local Music
                val localTracks = tracks.filter { it.isLocal }
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(localTracks) { track ->
                        TrackRowItem(track = track, onClick = { onTrackSelected(track) })
                    }
                }
            }
        }
    }
}

@Composable
fun TrackRowItem(track: Track, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SonzaSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Alignment.CenterVertically
        ) {
            AsyncImage(
                model = track.artworkUri,
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SonzaTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${TimeFormatter.formatDuration(track.durationMs)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SonzaTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = SonzaHiResBadgeBg
            ) {
                Text(
                    text = "${track.quality.codec} ${track.quality.bitDepth}b",
                    style = MaterialTheme.typography.labelSmall,
                    color = SonzaCyanAccent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
