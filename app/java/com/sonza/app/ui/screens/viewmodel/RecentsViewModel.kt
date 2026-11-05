package com.sonza.app.ui.screens.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.app.core.data.local.entity.ListeningHistory
import com.sonza.app.core.model.RecentlyPlayed
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SongSource
import com.sonza.app.data.repository.ListeningHistoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecentsViewModel @Inject constructor(
    private val repository: ListeningHistoryRepository,
    private val downloadRepository: com.sonza.app.data.repository.DownloadRepository
) : ViewModel() {

    val incognitoModeEnabled: StateFlow<Boolean> = repository.sessionManager.incognitoModeEnabledFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode = _isEditMode.asStateFlow()

    private val _selectedSongs = MutableStateFlow<Set<String>>(emptySet())
    val selectedSongs = _selectedSongs.asStateFlow()

    fun enterEditMode() {
        _isEditMode.value = true
    }

    fun exitEditMode() {
        _isEditMode.value = false
        _selectedSongs.value = emptySet()
    }

    fun toggleSelection(songId: String) {
        _selectedSongs.update { current ->
            if (current.contains(songId)) current - songId else current + songId
        }
    }

    fun clearSelection() {
        _selectedSongs.value = emptySet()
    }

    fun setIncognitoMode(enabled: Boolean) {
        viewModelScope.launch {
            repository.sessionManager.setIncognitoModeEnabled(enabled)
        }
    }

    fun downloadSong(song: Song) {
        viewModelScope.launch {
            downloadRepository.downloadSong(song)
        }
    }

    // Fetch all persistent history ordered chronologically by last played
    val recentSongs: StateFlow<List<RecentlyPlayed>> = repository.getAllHistory()
        .map { historyList ->
            historyList.map { history ->
                history.toRecentlyPlayed()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearHistory() {
        _selectedSongs.value = emptySet()
        _isEditMode.value = false
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    fun removeFromHistory(songId: String) {
        _selectedSongs.update { it - songId }
        viewModelScope.launch {
            repository.removeFromHistory(songId)
        }
    }

    fun deleteSelectedSongs() {
        val ids = _selectedSongs.value.toList()
        if (ids.isNotEmpty()) {
            _selectedSongs.value = emptySet()
            _isEditMode.value = false
            viewModelScope.launch {
                repository.removeSongsFromHistory(ids)
            }
        }
    }

    private fun ListeningHistory.toRecentlyPlayed(): RecentlyPlayed {
        val song = Song(
            id = this.songId,
            title = this.songTitle,
            artist = this.artist,
            album = this.album,
            duration = this.duration,
            thumbnailUrl = this.thumbnailUrl,
            source = try {
                SongSource.valueOf(this.source)
            } catch (e: Exception) {
                SongSource.YOUTUBE
            },
            localUri = this.localUri,
            artistId = this.artistId
        )
        return RecentlyPlayed(song, this.lastPlayed)
    }
}
