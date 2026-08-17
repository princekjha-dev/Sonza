package com.sonza.music.feature.spotify

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.music.core.model.PlaylistType
import com.sonza.music.core.model.Track
import com.sonza.music.core.theme.SonzaAmberGold
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaEmerald
import com.sonza.music.core.theme.SonzaHiResBadgeBg
import com.sonza.music.core.theme.SonzaRose
import com.sonza.music.core.theme.SonzaSurface
import com.sonza.music.core.theme.SonzaSurfaceElevated
import com.sonza.music.core.theme.SonzaTextPrimary
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.data.repository.MusicRepository
import com.sonza.music.data.repository.PlaylistRepository
import com.sonza.music.data.spotify.SpotifyImportReport
import com.sonza.music.data.spotify.SpotifyImportTrack
import com.sonza.music.data.spotify.SpotifyPlaylistImporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpotifyImportViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val playlistRepository: PlaylistRepository
) : ViewModel() {

    private val _report = MutableStateFlow<SpotifyImportReport?>(null)
    val report: StateFlow<SpotifyImportReport?> = _report.asStateFlow()

    fun importDemoSpotifyPlaylist(playlistName: String) {
        viewModelScope.launch {
            val allTracks = musicRepository.getAllTracks().first()

            // Sample simulated Spotify tracks to import
            val importedTracks = listOf(
                SpotifyImportTrack("Midnight Horizon", "Aura Resonance"),
                SpotifyImportTrack("Starlight Symphony in D Minor", "Vienna Chamber Collective"),
                SpotifyImportTrack("Velvet Nights", "Miles Thorne Quartet"),
                SpotifyImportTrack("Quantum Echoes", "Hyperion Synthesis"),
                SpotifyImportTrack("Solaris Flight (Unreleased Ambient)", "Solar Drift"),
                SpotifyImportTrack("Acoustic Horizon Master", "Echo Valley Ensemble")
            )

            val generatedReport = SpotifyPlaylistImporter.matchImportedTracks(
                imported = importedTracks,
                availableTracks = allTracks,
                playlistName = playlistName.ifEmpty { "My Spotify Master Playlist" }
            )

            _report.value = generatedReport
        }
    }

    fun saveImportedPlaylist(report: SpotifyImportReport) {
        viewModelScope.launch {
            val playlistId = playlistRepository.createPlaylist(
                title = report.playlistName,
                description = "Imported from Spotify (${report.matchedCount}/${report.totalTracksCount} matched)",
                type = PlaylistType.SPOTIFY_IMPORTED
            )
            report.results.filter { it.isResolved && it.matchedTrack != null }.forEach { match ->
                playlistRepository.addTrackToPlaylist(playlistId, match.matchedTrack!!.id)
            }
        }
    }
}

@Composable
fun SpotifyImportScreen(
    report: SpotifyImportReport?,
    onImport: (String) -> Unit,
    onSavePlaylist: (SpotifyImportReport) -> Unit,
    onDismiss: () -> Unit
) {
    var playlistUrlOrName by remember { mutableStateOf("https://open.spotify.com/playlist/37i9dQZF1DX4sWSpwq3LiO") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SonzaDarkBackground)
            .padding(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SonzaTextPrimary)
                }

                Text(
                    text = "Spotify Playlist Import",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SonzaTextPrimary
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // URL Input Card
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SonzaSurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Import Spotify Playlist Metadata",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SonzaTextPrimary
                    )
                    Text(
                        text = "Authorized metadata migration without DRM ripping. Matches tracks against your high-resolution library.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonzaTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = playlistUrlOrName,
                        onValueChange = { playlistUrlOrName = it },
                        label = { Text("Spotify Playlist URL or Name") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SonzaCyanAccent,
                            focusedTextColor = SonzaTextPrimary,
                            unfocusedTextColor = SonzaTextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { onImport(playlistUrlOrName) },
                        colors = ButtonDefaults.buttonColors(containerColor = SonzaCyanAccent, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Analyze & Match Tracks", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Report Results
            if (report != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${report.totalTracksCount} tracks: ${report.matchedCount} matched, ${report.unresolvedCount} unresolved",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SonzaCyanAccent
                    )

                    Button(
                        onClick = {
                            onSavePlaylist(report)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SonzaEmerald, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Playlist", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(report.results) { match ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SonzaSurface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (match.isResolved) Icons.Default.CheckCircle else Icons.Default.HelpOutline,
                                    contentDescription = null,
                                    tint = if (match.isResolved) SonzaEmerald else SonzaAmberGold,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = match.spotifyTrack.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = SonzaTextPrimary
                                    )
                                    Text(
                                        text = "${match.spotifyTrack.artist} • ${if (match.isResolved) "Matched to Hi-Res Track" else "Unresolved source"}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (match.isResolved) SonzaTextSecondary else SonzaAmberGold
                                    )
                                }
                                if (match.isResolved) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = SonzaHiResBadgeBg
                                    ) {
                                        Text(
                                            text = "${(match.matchScore * 100).toInt()}% Match",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = SonzaCyanAccent,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
}
