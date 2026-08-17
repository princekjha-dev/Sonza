package com.sonza.music.feature.visualizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sonza.music.core.model.VisualizerMode
import com.sonza.music.core.theme.SonzaAmberGold
import com.sonza.music.core.theme.SonzaCyanAccent
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AudioVisualizerView(
    mode: VisualizerMode,
    waveform: FloatArray,
    fft: FloatArray,
    isBeat: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        when (mode) {
            VisualizerMode.WAVEFORM -> {
                if (waveform.isEmpty()) return@Canvas
                val stepX = width / waveform.size
                val path = Path()
                val midY = height / 2f

                path.moveTo(0f, midY + waveform[0] * midY * 0.9f)
                for (i in 1 until waveform.size) {
                    val x = i * stepX
                    val y = midY + (waveform[i] * midY * 0.9f)
                    path.lineTo(x, y)
                }

                drawPath(
                    path = path,
                    color = if (isBeat) SonzaAmberGold else SonzaCyanAccent,
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            VisualizerMode.SPECTRUM, VisualizerMode.BARS -> {
                if (fft.isEmpty()) return@Canvas
                val barCount = fft.size.coerceAtMost(36)
                val totalSpacing = width * 0.15f
                val barWidth = (width - totalSpacing) / barCount
                val gap = totalSpacing / (barCount + 1)

                for (i in 0 until barCount) {
                    val magnitude = fft[i].coerceIn(0.05f, 1.0f)
                    val barHeight = magnitude * height * 0.85f
                    val left = gap + i * (barWidth + gap)
                    val top = height - barHeight

                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(SonzaCyanAccent, SonzaAmberGold)
                        ),
                        topLeft = Offset(left, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                    )
                }
            }

            VisualizerMode.CIRCULAR_SPECTRUM -> {
                if (fft.isEmpty()) return@Canvas
                val center = Offset(width / 2f, height / 2f)
                val baseRadius = (width.coerceAtMost(height) / 3f)
                val barCount = fft.size.coerceAtMost(48)
                val angleStep = (2 * Math.PI) / barCount

                for (i in 0 until barCount) {
                    val angle = i * angleStep
                    val magnitude = fft[i].coerceIn(0.02f, 1.0f)
                    val barLength = magnitude * baseRadius * 0.8f

                    val startX = center.x + (baseRadius * cos(angle)).toFloat()
                    val startY = center.y + (baseRadius * sin(angle)).toFloat()
                    val endX = center.x + ((baseRadius + barLength) * cos(angle)).toFloat()
                    val endY = center.y + ((baseRadius + barLength) * sin(angle)).toFloat()

                    drawLine(
                        color = if (isBeat) SonzaAmberGold else SonzaCyanAccent,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            VisualizerMode.MINIMAL_PULSE -> {
                val center = Offset(width / 2f, height / 2f)
                val energy = fft.take(10).average().toFloat().coerceIn(0.1f, 1f)
                val radius = (width.coerceAtMost(height) / 4f) * (1.0f + (energy * 0.35f))

                drawCircle(
                    color = (if (isBeat) SonzaAmberGold else SonzaCyanAccent).copy(alpha = 0.25f),
                    radius = radius * 1.25f,
                    center = center
                )
                drawCircle(
                    color = (if (isBeat) SonzaAmberGold else SonzaCyanAccent).copy(alpha = 0.6f),
                    radius = radius,
                    center = center
                )
            }
        }
    }
}
