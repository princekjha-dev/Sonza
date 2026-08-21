package com.sonza.app.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.app.core.model.Song
import com.sonza.app.data.migration.PlaylistMigrationManager
import com.sonza.app.data.migration.model.DuplicateStrategy
import com.sonza.app.data.migration.model.MatchConfidence
import com.sonza.app.data.migration.model.MigrationRecord
import com.sonza.app.data.migration.model.MigrationSource
import com.sonza.app.data.migration.model.TrackMatchResult
import com.sonza.app.data.migration.provider.ParsedPlaylist
import com.sonza.app.data.repository.YouTubeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MigrationStep {
    LANDING,
    INPUT_URL,
    ANALYZING,
    REVIEW_MATCHES,
    DUPLICATE_PROMPT,
    MIGRATING,
    COMPLETED,
    HISTORY
}

data class PlaylistMigrationUiState(
    val step: MigrationStep = MigrationStep.LANDING,
    val selectedSource: MigrationSource? = null,
    val urlInput: String = "",
    val parsedPlaylist: ParsedPlaylist? = null,
    val matchResults: List<TrackMatchResult> = emptyList(),
    val progressCurrent: Int = 0,
    val progressTotal: Int = 0,
    val currentAnalyzingTitle: String = "",
    val existingPlaylistFound: Boolean = false,
    val migrationRecord: MigrationRecord? = null,
    val targetPlaylistId: String? = null,
    val history: List<MigrationRecord> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val activeReviewFilter: MatchConfidence? = null,
    // Manual search overlay state
    val manualSearchTargetIndex: Int? = null,
    val manualSearchQuery: String = "",
    val manualSearchResults: List<Song> = emptyList(),
    val isSearchingManual: Boolean = false
)

@HiltViewModel
class PlaylistMigrationViewModel @Inject constructor(
    private val migrationManager: PlaylistMigrationManager,
    private val youTubeRepository: YouTubeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlaylistMigrationUiState())
    val uiState: StateFlow<PlaylistMigrationUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    fun loadHistory() {
        viewModelScope.launch {
            val records = migrationManager.getMigrationHistory()
            _uiState.update { it.copy(history = records) }
        }
    }

    fun onSelectSource(source: MigrationSource) {
        if (!source.isSupported) {
            _uiState.update {
                it.copy(errorMessage = "${source.displayName} integration is coming soon! You can also export tracks to CSV/TXT and import as a file.")
            }
            return
        }
        _uiState.update {
            it.copy(
                selectedSource = source,
                step = MigrationStep.INPUT_URL,
                urlInput = "",
                errorMessage = null
            )
        }
    }

    fun onUrlChanged(url: String) {
        _uiState.update { it.copy(urlInput = url, errorMessage = null) }
    }

    fun setStep(step: MigrationStep) {
        _uiState.update { it.copy(step = step, errorMessage = null) }
    }

    fun startUrlImport(url: String = _uiState.value.urlInput) {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid playlist URL") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    step = MigrationStep.ANALYZING,
                    progressCurrent = 0,
                    progressTotal = 0,
                    currentAnalyzingTitle = "Fetching playlist...",
                    errorMessage = null
                )
            }

            try {
                val parsed = migrationManager.parseFromUrl(trimmed) { fetchedCount ->
                    _uiState.update { it.copy(currentAnalyzingTitle = "Fetched $fetchedCount tracks...") }
                }

                if (parsed == null || parsed.tracks.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = MigrationStep.INPUT_URL,
                            errorMessage = "Couldn't extract tracks from this URL. Make sure the playlist is public."
                        )
                    }
                    return@launch
                }

                runMatchingPipeline(parsed)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        step = MigrationStep.INPUT_URL,
                        errorMessage = "Import error: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun startFileImport(uri: Uri) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    step = MigrationStep.ANALYZING,
                    progressCurrent = 0,
                    progressTotal = 0,
                    currentAnalyzingTitle = "Parsing file...",
                    errorMessage = null
                )
            }

            try {
                val parsed = migrationManager.parseFromFile(uri)
                if (parsed == null || parsed.tracks.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            step = MigrationStep.LANDING,
                            errorMessage = "No valid tracks found in this file format."
                        )
                    }
                    return@launch
                }

                runMatchingPipeline(parsed)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        step = MigrationStep.LANDING,
                        errorMessage = "File read error: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    private suspend fun runMatchingPipeline(parsed: ParsedPlaylist) {
        _uiState.update {
            it.copy(
                parsedPlaylist = parsed,
                progressTotal = parsed.tracks.size,
                progressCurrent = 0,
                currentAnalyzingTitle = "Matching tracks with Sonza catalog..."
            )
        }

        val matchResults = migrationManager.matchTracks(parsed.tracks) { completed, total, lastResult ->
            _uiState.update {
                it.copy(
                    progressCurrent = completed,
                    progressTotal = total,
                    currentAnalyzingTitle = "Matching: ${lastResult.sourceTrack.title} (${completed}/$total)"
                )
            }
        }

        val existing = migrationManager.findExistingPlaylist(parsed.title)

        _uiState.update {
            it.copy(
                isLoading = false,
                matchResults = matchResults,
                existingPlaylistFound = existing != null,
                step = MigrationStep.REVIEW_MATCHES
            )
        }
    }

    fun setReviewFilter(filter: MatchConfidence?) {
        _uiState.update { it.copy(activeReviewFilter = filter) }
    }

    fun toggleSkipTrack(index: Int) {
        _uiState.update { state ->
            val updated = state.matchResults.toMutableList()
            if (index in updated.indices) {
                val item = updated[index]
                updated[index] = item.copy(isSkipped = !item.isSkipped)
            }
            state.copy(matchResults = updated)
        }
    }

    fun selectCandidateForTrack(trackIndex: Int, selectedSong: Song) {
        _uiState.update { state ->
            val updated = state.matchResults.toMutableList()
            if (trackIndex in updated.indices) {
                val item = updated[trackIndex]
                updated[trackIndex] = item.copy(
                    matchedSong = selectedSong,
                    confidence = MatchConfidence.PERFECT_MATCH,
                    isManuallySelected = true,
                    isSkipped = false
                )
            }
            state.copy(matchResults = updated, manualSearchTargetIndex = null)
        }
    }

    fun openManualSearch(index: Int) {
        val item = _uiState.value.matchResults.getOrNull(index) ?: return
        val defaultQuery = "${item.sourceTrack.title} ${item.sourceTrack.artist}".trim()
        _uiState.update {
            it.copy(
                manualSearchTargetIndex = index,
                manualSearchQuery = defaultQuery,
                manualSearchResults = emptyList(),
                isSearchingManual = false
            )
        }
        performManualSearch(defaultQuery)
    }

    fun closeManualSearch() {
        _uiState.update { it.copy(manualSearchTargetIndex = null, manualSearchResults = emptyList()) }
    }

    fun onManualSearchQueryChange(query: String) {
        _uiState.update { it.copy(manualSearchQuery = query) }
    }

    fun performManualSearch(query: String = _uiState.value.manualSearchQuery) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearchingManual = true) }
            try {
                val results = youTubeRepository.search(query)
                _uiState.update { it.copy(manualSearchResults = results, isSearchingManual = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSearchingManual = false) }
            }
        }
    }

    fun startMigrationExecution(duplicateStrategy: DuplicateStrategy = DuplicateStrategy.CREATE_NEW_COPY) {
        val parsed = _uiState.value.parsedPlaylist ?: return
        val matches = _uiState.value.matchResults

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    step = MigrationStep.MIGRATING,
                    errorMessage = null
                )
            }

            try {
                val (playlistId, record) = migrationManager.executeMigration(
                    playlistTitle = parsed.title,
                    description = parsed.description,
                    thumbnailUrl = parsed.thumbnailUrl,
                    source = parsed.source,
                    matchedResults = matches,
                    duplicateStrategy = duplicateStrategy
                )

                loadHistory()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        targetPlaylistId = playlistId,
                        migrationRecord = record,
                        step = MigrationStep.COMPLETED
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        step = MigrationStep.REVIEW_MATCHES,
                        errorMessage = "Migration failed: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun proceedFromReview() {
        if (_uiState.value.existingPlaylistFound) {
            _uiState.update { it.copy(step = MigrationStep.DUPLICATE_PROMPT) }
        } else {
            startMigrationExecution(DuplicateStrategy.CREATE_NEW_COPY)
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resetState() {
        _uiState.update {
            PlaylistMigrationUiState(history = it.history)
        }
    }
}
