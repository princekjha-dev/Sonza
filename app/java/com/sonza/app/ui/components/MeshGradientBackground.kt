package com.sonza.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.unit.dp

/**
 * A dynamic, mesh-like gradient background that subtly animates based on dominant colors.
 * Replaces static backgrounds with a fluid, alive feel similar to YouTube Music.
 */
@Composable
fun MeshGradientBackground(
    dominantColors: DominantColors?, // Nullable to handle loading states gracefully
    modifier: Modifier = Modifier,
    speedMultiplier: Float = 1f,
    backgroundColor: Color = MaterialTheme.colorScheme.background
) {
    val isIdle = dominantColors == null || dominantColors.isIdle

    // When idle (no track playing), keep background clean and neutral dark (no blue glowing blobs)
    val colors = if (isIdle) {
        DominantColors(
            primary = com.sonza.app.ui.theme.SonzaSurface,
            secondary = com.sonza.app.ui.theme.SonzaSurfaceVariant,
            accent = com.sonza.app.ui.theme.SonzaSurfaceVariant,
            onBackground = com.sonza.app.ui.theme.SonzaOnBackground,
            isIdle = true
        )
    } else {
        dominantColors!!
    }

    // Animate colors for fast expressive transitions when song changes
    val animatedPrimaryState = animateColorAsState(
        targetValue = colors.primary,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "primary_color_anim"
    )
    val animatedSecondaryState = animateColorAsState(
        targetValue = colors.secondary,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "secondary_color_anim"
    )
    val animatedAccentState = animateColorAsState(
        targetValue = colors.accent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "accent_color_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mesh_gradient_motion")
    
    // Blob movements (animated values) - Use raw state to avoid recomposition
    val x1State = infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(12000, easing = LinearEasing), RepeatMode.Reverse),
        label = "x1"
    )
    val y1State = infiniteTransition.animateFloat(
        initialValue = 0.1f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(15000, easing = LinearEasing), RepeatMode.Reverse),
        label = "y1"
    )

    val x2State = infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(18000, easing = LinearEasing), RepeatMode.Reverse),
        label = "x2"
    )
    val y2State = infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Reverse),
        label = "y2"
    )

    val x3State = infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 0.5f,
        animationSpec = infiniteRepeatable(tween(20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "x3"
    )
    val y3State = infiniteTransition.animateFloat(
        initialValue = 0.8f, targetValue = 0.6f,
        animationSpec = infiniteRepeatable(tween(16000, easing = LinearEasing), RepeatMode.Reverse),
        label = "y3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .graphicsLayer() // Use graphics layer for hardware acceleration
            .drawWithCache {
                // The bottom fade is static (depends only on backgroundColor + size), so
                // build it once per size/color change instead of re-allocating the brush
                // every frame inside the animated draw loop.
                val scrimBrush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        backgroundColor.copy(alpha = 0.5f),
                        backgroundColor
                    ),
                    startY = 0f,
                    endY = size.height
                )
                onDrawBehind {
                    val width = size.width
                    val height = size.height

                    val primary = animatedPrimaryState.value
                    val secondary = animatedSecondaryState.value
                    val accent = animatedAccentState.value

                    val x1 = x1State.value
                    val y1 = y1State.value
                    val x2 = x2State.value
                    val y2 = y2State.value
                    val x3 = x3State.value
                    val y3 = y3State.value

                    val primaryAlpha = if (isIdle) 0.10f else 0.45f
                    val secondaryAlpha = if (isIdle) 0.08f else 0.40f
                    val accentAlpha = if (isIdle) 0.06f else 0.35f

                    // Blob 1: Primary
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = primaryAlpha), Color.Transparent),
                            center = Offset(width * x1, height * y1),
                            radius = width * 1.2f
                        ),
                        radius = width * 1.2f,
                        center = Offset(width * x1, height * y1)
                    )

                    // Blob 2: Secondary
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondary.copy(alpha = secondaryAlpha), Color.Transparent),
                            center = Offset(width * x2, height * y2),
                            radius = width * 1.1f
                        ),
                        radius = width * 1.1f,
                        center = Offset(width * x2, height * y2)
                    )

                    // Blob 3: Accent
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = accentAlpha), Color.Transparent),
                            center = Offset(width * x3, height * y3),
                            radius = width * 1.0f
                        ),
                        radius = width * 1.0f,
                        center = Offset(width * x3, height * y3)
                    )

                    // Static bottom fade (cached brush)
                    drawRect(brush = scrimBrush)
                }
            }
    )
}
