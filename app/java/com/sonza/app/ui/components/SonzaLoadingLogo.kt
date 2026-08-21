package com.sonza.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Native Android animated Sonza loading indicator.
 *
 * Recreates the Sonza logo geometry as a lightweight, GPU-accelerated Compose Canvas
 * component with individual rounded capsule bars animated sequentially in a smooth
 * clockwise loading sweep.
 *
 * @param modifier Modifier for layout and sizing.
 * @param color Active primary/accent color resolved at runtime (supports Material You / Dynamic Colors).
 * @param barCount Number of radial capsule bars (default 8).
 * @param durationMillis Duration of one full clockwise loop (default 1000ms).
 * @param inactiveAlpha Lower-emphasis alpha for inactive trailing bars (default 0.22f).
 */
@Composable
fun SonzaLoadingLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 8,
    durationMillis: Int = 1000,
    inactiveAlpha: Float = 0.22f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SonzaLoadingTransition")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = barCount.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SonzaLoadingPhase"
    )

    Canvas(
        modifier = modifier
    ) {
        val diameter = size.minDimension
        if (diameter <= 0f) return@Canvas

        val center = Offset(size.width / 2f, size.height / 2f)
        
        // Exact capsule bar geometry scaled to canvas bounds
        val barWidth = diameter * 0.095f
        val barLength = diameter * 0.23f
        val innerRadius = diameter * 0.22f
        val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

        for (i in 0 until barCount) {
            val angleDegrees = (i * 360f / barCount)
            
            // Calculate distance from active animation head in clockwise direction
            val distance = (phase - i + barCount) % barCount
            val normalizedDistance = distance / barCount
            val intensity = (1f - normalizedDistance).coerceIn(0f, 1f)
            
            // Smooth natural decay curve for trailing bars
            val alpha = (inactiveAlpha + (1f - inactiveAlpha) * (intensity * intensity)).coerceIn(0f, 1f)
            val barColor = color.copy(alpha = alpha)

            withTransform({
                rotate(degrees = angleDegrees, pivot = center)
            }) {
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(center.x - barWidth / 2f, center.y - innerRadius - barLength),
                    size = Size(barWidth, barLength),
                    cornerRadius = cornerRadius
                )
            }
        }
    }
}

/**
 * Convenience overload accepting fixed Dp dimensions.
 */
@Composable
fun SonzaLoadingLogo(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    durationMillis: Int = 1000
) {
    SonzaLoadingLogo(
        modifier = modifier.size(size),
        color = color,
        durationMillis = durationMillis
    )
}
