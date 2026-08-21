package com.sonza.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonza.app.core.model.SeekbarStyle
import com.sonza.app.core.model.SponsorCategory
import com.sonza.app.data.repository.SponsorSegment
import com.sonza.app.ui.sponsorblock.color
import com.sonza.app.util.TimeUtil

/**
 * Clean, straight horizontal seekbar with smooth rounded track,
 * clear played-progress section, and small circular thumb.
 */
@Composable
fun WaveformSeeker(
    progressProvider: () -> Float,
    isPlaying: Boolean,
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    inactiveColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    initialStyle: SeekbarStyle = SeekbarStyle.CLASSIC,
    onStyleChange: ((SeekbarStyle) -> Unit)? = null,
    duration: Long = 0L,
    sponsorSegments: List<SponsorSegment> = emptyList(),
    contentPadding: Dp = 8.dp
) {
    var isDragging by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(progressProvider()) }
    var dragX by remember { mutableFloatStateOf(0f) }

    // Update currentProgress from external progress only when NOT dragging
    val externalProgress = progressProvider()
    LaunchedEffect(externalProgress) {
        if (!isDragging) {
            currentProgress = externalProgress
        }
    }

    val animatedThumbRadius by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 5.5.dp,
        animationSpec = spring(stiffness = 500f),
        label = "thumbRadius"
    )

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val maxWidthPx = with(density) { maxWidth.toPx() }

        // Time Tooltip when dragging
        if (isDragging && duration > 0) {
            val seekTime = (currentProgress * duration).toLong()
            val timeText = TimeUtil.formatPosition(seekTime)

            val tooltipOffset = with(density) {
                (dragX - (maxWidthPx / 2)).toDp()
            }

            Surface(
                modifier = Modifier
                    .offset(x = tooltipOffset, y = (-42).dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 4.dp,
                shadowElevation = 6.dp
            ) {
                Text(
                    text = timeText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = activeColor
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .graphicsLayer { clip = false }
                .padding(horizontal = contentPadding)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            currentProgress = newProgress
                            onSeek(newProgress)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragX = offset.x
                            val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                            currentProgress = newProgress
                        },
                        onDragEnd = {
                            if (duration > 0) onSeek(currentProgress)
                            isDragging = false
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onHorizontalDrag = { change, _ ->
                            val newProgress = (change.position.x / size.width).coerceIn(0f, 1f)
                            currentProgress = newProgress
                            dragX = change.position.x
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val width = size.width
                val height = size.height
                if (width <= 0f || height <= 0f) return@Canvas

                val centerY = height / 2f
                val trackHeight = 4.5.dp.toPx()
                val cornerRadius = CornerRadius(trackHeight / 2f, trackHeight / 2f)
                val clampedProgress = currentProgress.coerceIn(0f, 1f)
                val progressX = clampedProgress * width
                val thumbRadiusPx = animatedThumbRadius.toPx()

                // 1. Inactive Background Track (smooth rounded straight line)
                drawRoundRect(
                    color = inactiveColor.copy(alpha = 0.35f),
                    topLeft = Offset(0f, centerY - trackHeight / 2f),
                    size = Size(width, trackHeight),
                    cornerRadius = cornerRadius
                )

                // 2. Active Played-Progress Section (smooth rounded straight line)
                if (progressX > 0f) {
                    drawRoundRect(
                        color = activeColor,
                        topLeft = Offset(0f, centerY - trackHeight / 2f),
                        size = Size(progressX, trackHeight),
                        cornerRadius = cornerRadius
                    )
                }

                // 3. Sponsor segments overlay (if present)
                if (duration > 0 && sponsorSegments.isNotEmpty()) {
                    val durationSec = duration / 1000f
                    sponsorSegments.forEach { segment ->
                        val startFraction = (segment.start / durationSec).coerceIn(0f, 1f)
                        val endFraction = (segment.end / durationSec).coerceIn(0f, 1f)

                        val startX = startFraction * width
                        val endX = endFraction * width
                        val segWidth = endX - startX

                        if (segWidth > 0f) {
                            val categoryColor = SponsorCategory.fromKey(segment.category)?.color ?: Color.Yellow

                            drawRoundRect(
                                color = categoryColor,
                                topLeft = Offset(startX, centerY - trackHeight / 2f),
                                size = Size(segWidth, trackHeight),
                                cornerRadius = cornerRadius,
                                blendMode = BlendMode.SrcAtop
                            )
                        }
                    }
                }

                // 4. Small Circular Thumb at Current Position
                drawCircle(
                    color = activeColor,
                    radius = thumbRadiusPx,
                    center = Offset(progressX, centerY)
                )
            }
        }
    }
}