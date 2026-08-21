package com.sonza.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.app.core.model.HomeSection
import com.sonza.app.data.repository.YouTubeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PodcastsUiState(
    val title: String = "Podcasts",
    val selectedCategory: String = "All",
    val categories: List<String> = listOf(
        "All",
        "Comedy",
        "News",
        "Technology",
        "True Crime",
        "Music",
        "Education",
        "Society & Culture",
        "Business",
        "Health & Fitness",
        "Science"
    ),
    val sections: List<HomeSection> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class PodcastsViewModel @Inject constructor(
    private val youTubeRepository: YouTubeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PodcastsUiState())
    val uiState: StateFlow<PodcastsUiState> = _uiState.asStateFlow()

    init {
        loadPodcasts("All")
    }

    fun selectCategory(category: String) {
        if (_uiState.value.selectedCategory == category && _uiState.value.sections.isNotEmpty()) return
        _uiState.update { it.copy(selectedCategory = category) }
        loadPodcasts(category)
    }

    fun retry() {
        loadPodcasts(_uiState.value.selectedCategory)
    }

    private fun loadPodcasts(category: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val sections = youTubeRepository.getPodcastsSections(if (category == "All") null else category)
                if (sections.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            sections = emptyList(),
                            error = "Could not load podcasts. Please check your internet connection."
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            sections = sections,
                            isLoading = false,
                            error = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load podcasts"
                    )
                }
            }
        }
    }
}
