package com.sonza.app.ui.components

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage

/**
 * Reusable dynamic artwork background for Sonza (Requirement 8).
 *
 * Renders:
 * - Base dark surface derived dynamically from artwork
 * - Blurred artwork influence through hardware RenderEffect / blur
 * - Dynamic radial gradients (primary top-left, secondary mid-right)
 * - Soft ambient glow (accent)
 * - Subtle glass overlay & specular sheen
 *
 * Subtle, fluid atmospheric backdrop that reacts to the currently playing track
 * or avatar fallback without washing out text or UI controls.
 */
@Composable
fun DynamicArtworkBackground(
    artworkModel: Any? = null,
    colors: SonzaDynamicColors = LocalSonzaDynamicColors.current,
    modifier: Modifier = Modifier,
    blurRadius: Float = 60f,
    ambientGlowAlpha: Float = 0.22f,
    showArtworkBlur: Boolean = true,
    content: (@Composable BoxScope.() -> Unit)? = null
) {
    // 600-800ms smooth transitions for ambient background / glow (Requirement 6)
    val animatedBg by animateColorAsState(
        targetValue = colors.background,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "dynamic_bg"
    )
    val animatedPrimary by animateColorAsState(
        targetValue = colors.primary,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "dynamic_bg_primary"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = colors.secondary,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "dynamic_bg_secondary"
    )
    val animatedAccent by animateColorAsState(
        targetValue = colors.accent,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "dynamic_bg_accent"
    )

    val radius = remember(blurRadius) { (blurRadius * 1.4f).coerceAtMost(80f) }
    val fallbackBlurDp = remember(blurRadius) { blurRadius * 0.9f }
    val blurEffect = remember(radius) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            android.graphics.RenderEffect.createBlurEffect(
                radius,
                radius,
                android.graphics.Shader.TileMode.CLAMP
            ).asComposeRenderEffect()
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .background(animatedBg)
    ) {
        // 1. Blurred artwork influence layer
        if (showArtworkBlur && artworkModel != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clipToBounds()
                    .then(
                        if (blurEffect != null) {
                            Modifier.graphicsLayer {
                                renderEffect = blurEffect
                                clip = true
                            }
                        } else {
                            Modifier.blur(fallbackBlurDp.dp)
                        }
                    )
            ) {
                AsyncImage(
                    model = artworkModel,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Scrim to keep content readable and elegant
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                animatedBg.copy(alpha = 0.72f),
                                animatedBg.copy(alpha = 0.88f),
                                animatedBg.copy(alpha = 0.96f)
                            )
                        )
                    )
            )
        }

        // 2. Dynamic radial gradients and soft ambient glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val width = size.width
                    val height = size.height

                    onDrawBehind {
                        // Top-left primary ambient glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    animatedPrimary.copy(alpha = ambientGlowAlpha),
                                    animatedPrimary.copy(alpha = ambientGlowAlpha * 0.4f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.15f, height * 0.18f),
                                radius = width * 0.85f
                            ),
                            radius = width * 0.85f,
                            center = Offset(width * 0.15f, height * 0.18f)
                        )

                        // Mid-right secondary ambient glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    animatedSecondary.copy(alpha = ambientGlowAlpha * 0.85f),
                                    animatedSecondary.copy(alpha = ambientGlowAlpha * 0.35f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.90f, height * 0.52f),
                                radius = width * 0.80f
                            ),
                            radius = width * 0.80f,
                            center = Offset(width * 0.90f, height * 0.52f)
                        )

                        // Lower soft accent glow
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    animatedAccent.copy(alpha = ambientGlowAlpha * 0.6f),
                                    Color.Transparent
                                ),
                                center = Offset(width * 0.40f, height * 0.80f),
                                radius = width * 0.70f
                            ),
                            radius = width * 0.70f,
                            center = Offset(width * 0.40f, height * 0.80f)
                        )

                        // Top subtle specular glass sheen
                        drawRect(
                            brush = Brush.verticalGradient(
                                0.0f to Color.White.copy(alpha = 0.04f),
                                0.20f to Color.Transparent
                            )
                        )
                    }
                }
        )

        content?.invoke(this)
    }
}
