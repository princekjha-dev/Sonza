package com.sonza.app.ui.components.sonza

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade

/**
 * Premium Sonza Artwork component supporting dynamic shapes, loading fallbacks, and subtle borders.
 */
@Composable
fun SonzaArtwork(
    thumbnailUrl: String?,
    modifier: Modifier = Modifier,
    shape: Shape = SonzaDesignTokens.Radii.CardShape,
    contentDescription: String? = "Album Artwork",
    isCircle: Boolean = false,
    contentScale: ContentScale = ContentScale.Crop
) {
    val actualShape = if (isCircle) CircleShape else shape

    Box(
        modifier = modifier
            .clip(actualShape)
            .background(SonzaDesignTokens.Colors.BackgroundSurfaceElevated)
            .border(1.dp, SonzaDesignTokens.Colors.GlassBorderSubtle, actualShape),
        contentAlignment = Alignment.Center
    ) {
        if (!thumbnailUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbnailUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        } else {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = contentDescription,
                tint = SonzaDesignTokens.Colors.TextTertiary,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

/**
 * Ambient Dynamic Glow effect derived from album art color.
 * Gently breathes when playback is active to produce that flagship OLED depth.
 */
@Composable
fun SonzaDynamicGlow(
    glowColor: Color,
    modifier: Modifier = Modifier,
    isPulsing: Boolean = true,
    blurRadius: Dp = 32.dp,
    intensity: Float = 0.45f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dynamicGlow")
    val animatedScale by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = 0.95f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowScale"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1.0f) }
    }

    val animatedAlpha by if (isPulsing) {
        infiniteTransition.animateFloat(
            initialValue = intensity * 0.75f,
            targetValue = intensity * 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(2800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "glowAlpha"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(intensity) }
    }

    Box(
        modifier = modifier
            .scale(animatedScale)
            .blur(blurRadius)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        glowColor.copy(alpha = animatedAlpha.coerceIn(0f, 1f)),
                        glowColor.copy(alpha = (animatedAlpha * 0.4f).coerceIn(0f, 1f)),
                        Color.Transparent
                    )
                )
            )
    )
}
