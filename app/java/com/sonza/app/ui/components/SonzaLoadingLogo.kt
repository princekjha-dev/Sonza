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
    SonzaVideoLoadingIndicator(
        modifier = modifier,
        color = color
    )
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
