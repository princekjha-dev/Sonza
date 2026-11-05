package com.sonza.app.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.app.core.model.HomeSection
import com.sonza.app.data.repository.YouTubeRepository
import com.sonza.app.navigation.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExploreUiState(
    val title: String = "",
    val sections: List<HomeSection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ExploreViewModel @Inject constructor(
    private val youTubeRepository: YouTubeRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val browseId: String = checkNotNull(savedStateHandle[Destination.Explore.ARG_BROWSE_ID])
    private val title: String = checkNotNull(savedStateHandle[Destination.Explore.ARG_TITLE])

    private val _uiState = MutableStateFlow(ExploreUiState(title = title))
    val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun retry() {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sections = if (browseId == "FEmusic_podcasts" || title.equals("Podcasts", ignoreCase = true)) {
                    youTubeRepository.getPodcastsSections()
                } else {
                    youTubeRepository.getBrowseSections(browseId)
                }
                _uiState.update { 
                    it.copy(
                        sections = sections,
                        isLoading = false,
                        error = if (sections.isEmpty()) "No content found for $title." else null
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load $title"
                    )
                }
            }
        }
    }
}
