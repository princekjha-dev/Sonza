package com.sonza.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.app.data.SessionManager
import com.sonza.app.core.model.Artist
import com.sonza.app.core.model.MusicSource
import com.sonza.app.core.model.Song
import com.sonza.app.data.repository.RemoteAudioRepository
import com.sonza.app.data.repository.LocalAudioRepository
import com.sonza.app.data.repository.YouTubeRepository
import com.sonza.app.navigation.Destination
import com.sonza.app.core.model.ArtistCreditInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ArtistError {
    NETWORK,
    AUTH_REQUIRED,
    UNKNOWN
}

data class ArtistUiState(
    val artist: Artist? = null,
    val isLoading: Boolean = false,
    val error: ArtistError? = null,
    val isSubscribing: Boolean = false,
    val showMultipleArtistsDialog: Boolean = false,
    val currentArtistCredits: List<ArtistCreditInfo> = emptyList()
)

class ArtistViewModel @Inject constructor(
    private val youTubeRepository: YouTubeRepository,
    private val remoteAudioRepository: RemoteAudioRepository,
    private val localAudioRepository: LocalAudioRepository,
    private val sessionManager: SessionManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val artistId: String = checkNotNull(savedStateHandle[Destination.Artist.ARG_ARTIST_ID])

    private val _uiState = MutableStateFlow(ArtistUiState())
    val uiState: StateFlow<ArtistUiState> = _uiState.asStateFlow()

    init {
        loadArtist()
    }

    fun toggleMultipleArtistsDialog(show: Boolean, credits: List<ArtistCreditInfo> = emptyList()) {
        _uiState.update { 
            it.copy(
                showMultipleArtistsDialog = show,
                currentArtistCredits = if (show) credits else emptyList()
            ) 
        }
    }

    fun fetchArtistCreditsAndShow(artistString: String, source: com.sonza.app.core.model.SongSource) {
        viewModelScope.launch {
            val names = parseArtistNames(artistString)
            
            // Show dialog with placeholders immediately if multiple artists
            if (names.size > 1) {
                val placeholders = names.map { name ->
                    ArtistCreditInfo(name, "Vocals", null, null)
                }
                toggleMultipleArtistsDialog(true, placeholders)
                
                // Fetch thumbnails in background
                val updatedCredits = names.map { name ->
                    try {
                        val results = if (source == com.sonza.app.core.model.SongSource.REMOTE) {
                            remoteAudioRepository.searchArtists(name)
                        } else {
                            youTubeRepository.searchArtists(name)
                        }
                        val match = results.firstOrNull { it.name.contains(name, true) || name.contains(it.name, true) } ?: results.firstOrNull()
                        ArtistCreditInfo(name, "Vocals", match?.thumbnailUrl, match?.id)
                    } catch (e: Exception) {
                        ArtistCreditInfo(name, "Vocals", null, null)
                    }
                }
                _uiState.update { it.copy(currentArtistCredits = updatedCredits) }
            } else {
                // Only one artist, handle directly or do nothing if already on that artist's page
                // But usually we don't need a dialog for 1 artist.
            }
        }
    }

    private fun parseArtistNames(artistString: String): List<String> {
        return artistString.split(",", "&", " feat.", " ft.", ";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    fun loadArtist() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                // Heuristic to determine source
                val isYouTubeId = artistId.startsWith("UC") || artistId.startsWith("FE") || artistId.startsWith("VL")
                val isLocalId = artistId.toLongOrNull() != null
                
                val isRemoteSource = sessionManager.getMusicSource() == MusicSource.REMOTE
                
                val artist = if (isLocalId) {
                    val id = artistId.toLong()
                    val artists = localAudioRepository.getAllLocalArtists()
                    val artistBase = artists.find { it.id == artistId }
                    if (artistBase != null) {
                        val songs = localAudioRepository.getSongsByArtist(id)
                        val albums = localAudioRepository.getAlbumsByArtist(id)
                        artistBase.copy(songs = songs, albums = albums)
                    } else null
                } else if (isRemoteSource || !isYouTubeId) {
                    remoteAudioRepository.getArtist(artistId) ?: if (isYouTubeId) {
                        youTubeRepository.getArtist(artistId)
                    } else null
                } else {
                    youTubeRepository.getArtist(artistId)
                }

                if (artist != null) {
                    _uiState.update {
                        it.copy(
                            artist = artist,
                            isLoading = false
                        )
                    }
                } else {
                    // Determine error type based on session state
                    val errorType = if (!sessionManager.isLoggedIn() && isYouTubeId) {
                        ArtistError.AUTH_REQUIRED
                    } else {
                        ArtistError.NETWORK
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = errorType
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = ArtistError.UNKNOWN,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun toggleSubscribe() {
        val currentArtist = _uiState.value.artist ?: return
        if (currentArtist.id.startsWith("UC") || currentArtist.id.startsWith("FE")) {
            viewModelScope.launch {
                _uiState.update { it.copy(isSubscribing = true) }
                val newStatus = !currentArtist.isSubscribed
                val success = youTubeRepository.subscribe(currentArtist.id, newStatus)
                
                if (success) {
                    // Update local state immediately for responsiveness
                    _uiState.update { 
                        it.copy(
                            artist = currentArtist.copy(isSubscribed = newStatus),
                            isSubscribing = false 
                        ) 
                    }
                    // Background refresh to get latest count/status
                    loadArtist()
                } else {
                    _uiState.update { it.copy(isSubscribing = false) }
                }
            }
        }
    }
}
