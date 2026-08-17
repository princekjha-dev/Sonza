package com.sonza.music.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import com.sonza.music.audio.engine.SonzaAudioEngine
import com.sonza.music.core.model.Album
import com.sonza.music.core.model.Track
import com.sonza.music.core.theme.SonzaAmberGold
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaHiResBadgeBg
import com.sonza.music.core.theme.SonzaSurface
import com.sonza.music.core.theme.SonzaSurfaceElevated
import com.sonza.music.core.theme.SonzaSurfaceVariant
import com.sonza.music.core.theme.SonzaTextPrimary
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.core.theme.SonzaTextTertiary
import com.sonza.music.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val audioEngine: SonzaAudioEngine
) : ViewModel() {

    val allTracks: StateFlow<List<Track>> = musicRepository.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = musicRepository.getAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            musicRepository.populateDemoCatalog()
            musicRepository.syncLocalMusic()
        }
    }

    fun playTrack(track: Track, allTracksList: List<Track>) {
        val index = allTracksList.indexOfFirst { it.id == track.id }
        if (index != -1) {
            audioEngine.setQueue(allTracksList, startIndex = index, autoPlay = true)
        } else {
            audioEngine.playTrack(track)
        }
    }
}

@Composable
fun HomeScreen(
    onNavigateToEqualizer: () -> Unit,
    onNavigateToListeningRoom: () -> Unit,
    onNavigateToSpotifyImport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onTrackSelected: (Track) -> Unit,
    tracks: List<Track>,
    albums: List<Album>
) {
    val greeting = rememberGreeting()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaDarkBackground),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = SonzaTextPrimary
                    )
                    Text(
                        text = "SONZA Studio Reference",
                        style = MaterialTheme.typography.labelLarge,
                        color = SonzaCyanAccent
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onNavigateToEqualizer,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SonzaSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Equalizer",
                            tint = SonzaCyanAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SonzaSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = SonzaTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Quick Feature Actions (Listen Together, Spotify Import)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SonzaSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToListeningRoom() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = SonzaCyanAccent,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Listen Together",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SonzaTextPrimary
                            )
                            Text(
                                text = "Live sync room",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SonzaTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SonzaSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToSpotifyImport() }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SyncAlt,
                            contentDescription = null,
                            tint = SonzaAmberGold,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Import Playlist",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SonzaTextPrimary
                            )
                            Text(
                                text = "Spotify metadata",
                                style = MaterialTheme.typography.bodyMedium,
                                color = SonzaTextTertiary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }

        // Hero Continue Listening / Master Track
        if (tracks.isNotEmpty()) {
            val heroTrack = tracks.first()
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "Continue Listening",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SonzaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SonzaSurfaceElevated),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTrackSelected(heroTrack) }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(80.dp)) {
                                AsyncImage(
                                    model = heroTrack.artworkUri,
                                    contentDescription = heroTrack.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .padding(4.dp)
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(SonzaCyanAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = SonzaHiResBadgeBg
                                ) {
                                    Text(
                                        text = heroTrack.quality.displayBadge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = SonzaCyanAccent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = heroTrack.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SonzaTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${heroTrack.artist} • ${heroTrack.album}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SonzaTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Recommended Hi-Res Tracks
        item {
            Column {
                Text(
                    text = "Audiophile Master Collection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SonzaTextPrimary,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(14.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(tracks) { track ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SonzaSurface),
                            modifier = Modifier
                                .width(160.dp)
                                .clickable { onTrackSelected(track) }
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                AsyncImage(
                                    model = track.artworkUri,
                                    contentDescription = track.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(140.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SonzaTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = track.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SonzaTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${track.quality.codec} ${track.quality.sampleRateHz / 1000}kHz",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SonzaCyanAccent
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(26.dp))
        }

        // Recommended Albums
        if (albums.isNotEmpty()) {
            item {
                Column {
                    Text(
                        text = "High-Dynamic Range Albums",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SonzaTextPrimary,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(albums) { album ->
                            Column(
                                modifier = Modifier
                                    .width(150.dp)
                                    .clickable { }
                            ) {
                                AsyncImage(
                                    model = album.artworkUri,
                                    contentDescription = album.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(150.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = album.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = SonzaTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = album.artist,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SonzaTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
private fun rememberGreeting(): String {
    val cal = Calendar.getInstance()
    return when (cal.get(Calendar.HOUR_OF_DAY)) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Late Night Listening"
    }
}
