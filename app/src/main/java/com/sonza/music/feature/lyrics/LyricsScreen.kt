package com.sonza.music.feature.lyrics

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sonza.music.audio.engine.PlayerState
import com.sonza.music.audio.engine.SonzaAudioEngine
import com.sonza.music.core.model.Lyrics
import com.sonza.music.core.model.LyricsLine
import com.sonza.music.core.theme.SonzaCyanAccent
import com.sonza.music.core.theme.SonzaDarkBackground
import com.sonza.music.core.theme.SonzaSurfaceElevated
import com.sonza.music.core.theme.SonzaTextPrimary
import com.sonza.music.core.theme.SonzaTextSecondary
import com.sonza.music.core.theme.SonzaTextTertiary
import com.sonza.music.data.repository.LyricsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val lyricsRepository: LyricsRepository,
    private val audioEngine: SonzaAudioEngine
) : ViewModel() {

    val playerState: StateFlow<PlayerState> = audioEngine.playerState

    private val _lyrics = MutableStateFlow<Lyrics?>(null)
    val lyrics: StateFlow<Lyrics?> = _lyrics.asStateFlow()

    fun loadLyricsForTrack(trackId: String) {
        viewModelScope.launch {
            _lyrics.value = lyricsRepository.getLyricsForTrack(trackId)
        }
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }
}

@Composable
fun LyricsScreen(
    lyrics: Lyrics?,
    currentPositionMs: Long,
    trackTitle: String,
    artistName: String,
    onSeekTo: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val listState = rememberLazyListState()
    val activeIndex = lyrics?.getActiveLineIndex(currentPositionMs) ?: -1

    // Smooth auto-scroll to keep active line vertically centered
    LaunchedEffect(activeIndex) {
        if (activeIndex >= 0 && lyrics?.lines?.isNotEmpty() == true) {
            val targetScroll = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0E1322),
                        SonzaDarkBackground,
                        SonzaDarkBackground
                    )
                )
            )
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertAlignment
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = SonzaTextPrimary)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = trackTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SonzaTextPrimary
                    )
                    Text(
                        text = artistName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = SonzaTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(SonzaCyanAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Subtitles,
                        contentDescription = null,
                        tint = SonzaCyanAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (lyrics == null || lyrics.lines.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Synchronized lyrics not available for this track.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SonzaTextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = 40.dp, bottom = 160.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    itemsIndexed(lyrics.lines) { index, line ->
                        val isActive = index == activeIndex
                        val isPassed = index < activeIndex

                        LyricsLineView(
                            line = line,
                            isActive = isActive,
                            isPassed = isPassed,
                            currentPositionMs = currentPositionMs,
                            onClick = { onSeekTo(line.startMs) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LyricsLineView(
    line: LyricsLine,
    isActive: Boolean,
    isPassed: Boolean,
    currentPositionMs: Long,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp)
    ) {
        if (isActive && line.hasWordSync) {
            // Word-Level Highlight Sync
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                line.words.forEach { word ->
                    val isWordActive = currentPositionMs >= word.startMs
                    Text(
                        text = "${word.word} ",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = if (isWordActive) SonzaCyanAccent else Color(0x66FFFFFF)
                    )
                }
            }
        } else {
            // Line-Level Sync
            Text(
                text = line.text,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                fontSize = if (isActive) 24.sp else 20.sp,
                color = when {
                    isActive -> SonzaCyanAccent
                    isPassed -> Color(0x99FFFFFF)
                    else -> Color(0x44FFFFFF)
                }
            )
        }
    }
}
