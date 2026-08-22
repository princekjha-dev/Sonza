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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.theme.SonzaBackground

/**
 * A dynamic, subtle ambient gradient background that provides a rich, premium atmosphere
 * without washing out text, cards, or album artwork.
 */
@Composable
fun MeshGradientBackground(
    dominantColors: DominantColors?,
    modifier: Modifier = Modifier,
    speedMultiplier: Float = 1f,
    backgroundColor: Color = SonzaBackground
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val isIdle = dominantColors == null || dominantColors.isIdle

    // When idle, subtly blend the dynamic theme accent rather than creating a flat gray haze
    val colors = if (isIdle) {
        DominantColors(
            primary = dynamicColors.accent.copy(alpha = 0.6f),
            secondary = dynamicColors.surfaceVariant,
            accent = dynamicColors.accent,
            onBackground = dynamicColors.onBackground,
            isIdle = true
        )
    } else {
        dominantColors!!
    }

    // Smooth spring animation for color transitions
    val animatedPrimaryState = animateColorAsState(
        targetValue = colors.primary,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "primary_color_anim"
    )
    val animatedSecondaryState = animateColorAsState(
        targetValue = colors.secondary,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "secondary_color_anim"
    )
    val animatedAccentState = animateColorAsState(
        targetValue = colors.accent,
        animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "accent_color_anim"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "mesh_gradient_motion")
    
    // Slow, subtle ambient movement
    val x1State = infiniteTransition.animateFloat(
        initialValue = 0.15f, targetValue = 0.35f,
        animationSpec = infiniteRepeatable(tween((18000 / speedMultiplier).toInt(), easing = LinearEasing), RepeatMode.Reverse),
        label = "x1"
    )
    val y1State = infiniteTransition.animateFloat(
        initialValue = 0.05f, targetValue = 0.22f,
        animationSpec = infiniteRepeatable(tween((22000 / speedMultiplier).toInt(), easing = LinearEasing), RepeatMode.Reverse),
        label = "y1"
    )

    val x2State = infiniteTransition.animateFloat(
        initialValue = 0.85f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween((24000 / speedMultiplier).toInt(), easing = LinearEasing), RepeatMode.Reverse),
        label = "x2"
    )
    val y2State = infiniteTransition.animateFloat(
        initialValue = 0.08f, targetValue = 0.28f,
        animationSpec = infiniteRepeatable(tween((20000 / speedMultiplier).toInt(), easing = LinearEasing), RepeatMode.Reverse),
        label = "y2"
    )

    val x3State = infiniteTransition.animateFloat(
        initialValue = 0.30f, targetValue = 0.60f,
        animationSpec = infiniteRepeatable(tween((26000 / speedMultiplier).toInt(), easing = LinearEasing), RepeatMode.Reverse),
        label = "x3"
    )
    val y3State = infiniteTransition.animateFloat(
        initialValue = 0.40f, targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween((24000 / speedMultiplier).toInt(), easing = LinearEasing), RepeatMode.Reverse),
        label = "y3"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .graphicsLayer()
            .drawWithCache {
                // Smooth fade down into deep background color to keep lower content crisp
                val scrimBrush = Brush.verticalGradient(
                    0.0f to Color.Transparent,
                    0.45f to backgroundColor.copy(alpha = 0.65f),
                    0.75f to backgroundColor,
                    1.0f to backgroundColor
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

                    // Precise, low-opacity ambient values avoiding milky haze
                    val primaryAlpha = if (isIdle) 0.08f else 0.18f
                    val secondaryAlpha = if (isIdle) 0.05f else 0.14f
                    val accentAlpha = if (isIdle) 0.06f else 0.15f

                    // Blob 1: Top-Left Primary Ambient Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = primaryAlpha), Color.Transparent),
                            center = Offset(width * x1, height * y1),
                            radius = width * 0.85f
                        ),
                        radius = width * 0.85f,
                        center = Offset(width * x1, height * y1)
                    )

                    // Blob 2: Top-Right Secondary Ambient Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(secondary.copy(alpha = secondaryAlpha), Color.Transparent),
                            center = Offset(width * x2, height * y2),
                            radius = width * 0.80f
                        ),
                        radius = width * 0.80f,
                        center = Offset(width * x2, height * y2)
                    )

                    // Blob 3: Mid-Area Accent Glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(accent.copy(alpha = accentAlpha), Color.Transparent),
                            center = Offset(width * x3, height * y3),
                            radius = width * 0.75f
                        ),
                        radius = width * 0.75f,
                        center = Offset(width * x3, height * y3)
                    )

                    // Clean vertical gradient feathering
                    drawRect(brush = scrimBrush)
                }
            }
    )
}

