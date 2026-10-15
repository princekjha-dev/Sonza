package com.sonza.app.ui.components.sonza

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.utils.rememberHaptics
import kotlin.math.roundToInt

/**
 * Premium Sonza Slider for Now Playing progress scrubbing.
 * Features reactive thumb expansion during drag, elapsed/remaining time labels, and haptics.
 */
@Composable
fun SonzaSlider(
    currentPositionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = SonzaDesignTokens.Colors.BrandAccent,
    trackColor: Color = Color.White.copy(alpha = 0.15f),
    showTimestamps: Boolean = true
) {
    val haptics = rememberHaptics()
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var trackWidthPx by remember { mutableFloatStateOf(1f) }

    val safeDuration = durationMs.coerceAtLeast(1L)
    val actualProgress = if (isDragging) {
        dragProgress
    } else {
        (currentPositionMs.toFloat() / safeDuration.toFloat()).coerceIn(0f, 1f)
    }

    val thumbSize by animateDpAsState(
        targetValue = if (isDragging) 16.dp else 10.dp,
        animationSpec = spring(stiffness = 500f),
        label = "thumbSize"
    )

    val trackHeight by animateDpAsState(
        targetValue = if (isDragging) 6.dp else 4.dp,
        animationSpec = spring(stiffness = 500f),
        label = "trackHeight"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(28.dp)
                .onSizeChanged { trackWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(safeDuration) {
                    detectTapGestures { offset ->
                        val newProgress = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                        haptics.tick()
                        onSeekTo((newProgress * safeDuration).toLong())
                    }
                }
                .pointerInput(safeDuration) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / trackWidthPx).coerceIn(0f, 1f)
                            haptics.tick()
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeekTo((dragProgress * safeDuration).toLong())
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            dragProgress = (change.position.x / trackWidthPx).coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(trackColor)
            )

            // Active progress fill
            Box(
                modifier = Modifier
                    .fillMaxWidth(actualProgress)
                    .height(trackHeight)
                    .clip(CircleShape)
                    .background(accentColor)
            )

            // Interactive thumb
            Box(
                modifier = Modifier
                    .offset {
                        val thumbX = (actualProgress * trackWidthPx) - (thumbSize.toPx() / 2f)
                        IntOffset(thumbX.roundToInt().coerceAtLeast(0), 0)
                    }
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }

        if (showTimestamps) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val displayPositionMs = if (isDragging) (dragProgress * safeDuration).toLong() else currentPositionMs
                Text(
                    text = formatDuration(displayPositionMs),
                    style = SonzaTypography.Metadata.copy(fontSize = 11.sp),
                    color = SonzaDesignTokens.Colors.TextSecondary
                )
                Text(
                    text = formatDuration(durationMs),
                    style = SonzaTypography.Metadata.copy(fontSize = 11.sp),
                    color = SonzaDesignTokens.Colors.TextSecondary
                )
            }
        }
    }
}

private fun formatDuration(millis: Long): String {
    if (millis <= 0) return "0:00"
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        String.format("%d:%02d:%02d", hours, remainingMinutes, seconds)
    } else {
        String.format("%d:%02d", minutes, seconds)
    }
}
