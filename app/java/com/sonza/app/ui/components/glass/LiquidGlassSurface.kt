package com.sonza.app.ui.components.glass

import android.os.Build
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonza.app.core.model.GlassStyle
import kotlin.random.Random

/**
 * Next-Generation Liquid Glass surface.
 *
 * Physically realistic, multi-layered optical simulation:
 *   1. Atmospheric soft shadow with dynamic elevation
 *   2. Blurred background backdrop (hardware accelerated RenderEffect on API 31+, .blur() fallback)
 *   3. Translucent frosted tint with animated ambient hue interpolation
 *   4. Micro-surface optical grain (deterministic distribution)
 *   5. Smooth specular highlight gradient
 *   6. Polished top-lip sheen reflection
 *   7. Luminous rim edge lighting
 *   8. Interactive content slot
 */
@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LiquidGlassTokens.RadiusCard),
    style: GlassStyle = GlassStyle.NATURAL,
    blurAmount: Float? = null,
    blurRadius: Dp? = null,
    intensity: Float = 1f,
    tint: Color = Color.Unspecified,
    ambientTint: Color = Color.Unspecified,
    isDarkTheme: Boolean = true,
    drawShadow: Boolean = true,
    drawRim: Boolean = true,
    elevationOverride: Dp? = null,
    elevation: Dp? = null,
    content: @Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val params = remember(style) { LiquidGlassTokens.getParams(style) }
    val effectiveBlur = blurRadius?.value ?: blurAmount ?: params.blurRadius
    val i = intensity.coerceIn(0.2f, 1.8f)

    // Animated dynamic tint: when background or album artwork shifts color,
    // the glass lighting interpolates fluidly rather than snapping abruptly.
    val effectiveTint = if (ambientTint != Color.Unspecified) ambientTint else tint
    val targetTint = if (effectiveTint == Color.Unspecified) {
        if (isDarkTheme) Color(0xFF14151E) else Color(0xFFF8F9FA)
    } else {
        effectiveTint
    }
    val animatedTint by animateColorAsState(
        targetValue = targetTint,
        animationSpec = tween(durationMillis = 400),
        label = "glassAmbientTint"
    )

    val baseAlpha = (if (isDarkTheme) params.darkBaseAlpha else params.lightBaseAlpha) * i
    val effectiveBaseColor = if (isDarkTheme) {
        animatedTint.copy(alpha = baseAlpha.coerceIn(0.1f, 0.95f))
    } else {
        Color.White.copy(alpha = 0.35f * i).compositeOver(animatedTint.copy(alpha = (baseAlpha * 0.45f).coerceIn(0.1f, 0.9f)))
    }

    // Specular highlight gradient (top to bottom reflection)
    val specularHighlight = remember(isDarkTheme, style, i) {
        val spec = params.specularIntensity * i
        Brush.verticalGradient(
            colors = if (isDarkTheme) listOf(
                Color.White.copy(alpha = (spec * 0.75f).coerceIn(0f, 1f)),
                Color.White.copy(alpha = (spec * 0.25f).coerceIn(0f, 1f)),
                Color.Transparent,
                Color.Transparent,
                Color.Black.copy(alpha = 0.12f * i)
            ) else listOf(
                Color.White.copy(alpha = (spec * 1.1f).coerceIn(0f, 1f)),
                Color.White.copy(alpha = (spec * 0.35f).coerceIn(0f, 1f)),
                Color.Transparent,
                Color.Transparent,
                Color.Black.copy(alpha = 0.06f * i)
            )
        )
    }

    // Top lip sheen: the polished gleam on the top edge
    val topSheen = remember(isDarkTheme, style, i) {
        val sheen = params.topSheenIntensity * i
        Brush.verticalGradient(
            0.0f to Color.White.copy(alpha = (if (isDarkTheme) sheen else sheen * 1.2f).coerceIn(0f, 1f)),
            0.14f to Color.Transparent
        )
    }

    // Luminous rim border brush
    val borderBrush = remember(isDarkTheme, style, i) {
        val rim = params.topSheenIntensity * i
        if (params.chromaticRim) {
            // Realistic mode: subtle chromatic refraction along the rim
            Brush.verticalGradient(
                colors = if (isDarkTheme) listOf(
                    Color(0xFFE0F2FE).copy(alpha = (rim * 0.9f).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = (rim * 0.35f).coerceIn(0f, 1f)),
                    Color(0xFFF3E8FF).copy(alpha = 0.08f * i),
                    Color(0xFFBAE6FD).copy(alpha = (rim * 0.45f).coerceIn(0f, 1f))
                ) else listOf(
                    Color.White.copy(alpha = (rim * 1.1f).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = (rim * 0.4f).coerceIn(0f, 1f)),
                    Color.Black.copy(alpha = 0.04f * i),
                    Color.Black.copy(alpha = 0.12f * i)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = if (isDarkTheme) listOf(
                    Color.White.copy(alpha = (rim * 0.85f).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = (rim * 0.22f).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = 0.06f * i),
                    Color.White.copy(alpha = (rim * 0.35f).coerceIn(0f, 1f))
                ) else listOf(
                    Color.White.copy(alpha = (rim * 1.0f).coerceIn(0f, 1f)),
                    Color.White.copy(alpha = (rim * 0.35f).coerceIn(0f, 1f)),
                    Color.Black.copy(alpha = 0.04f * i),
                    Color.Black.copy(alpha = 0.10f * i)
                )
            )
        }
    }

    // Deterministic micro-grain points for realistic texture
    val grainFractions = remember {
        val rand = Random(1337)
        FloatArray(600 * 2) { rand.nextFloat() }
    }
    val grainColor = remember(isDarkTheme, style) {
        Color.White.copy(alpha = params.grainAlpha)
    }

    val effectiveElevation = elevation ?: elevationOverride ?: params.elevation

    Box(modifier = modifier) {
        // Layer 1: Ambient soft shadow
        if (drawShadow && effectiveElevation > 0.dp) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .shadow(
                        elevation = effectiveElevation,
                        shape = shape,
                        clip = false,
                        ambientColor = Color.Black.copy(alpha = if (isDarkTheme) 0.45f else 0.14f),
                        spotColor = Color.Black.copy(alpha = if (isDarkTheme) 0.35f else 0.09f)
                    )
            )
        }

        // Layer 2: Blurred backdrop & Layer 3: frosted tint + micro-grain
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && effectiveBlur > 0.5f) {
                        Modifier.graphicsLayer {
                            renderEffect = android.graphics.RenderEffect.createBlurEffect(
                                effectiveBlur,
                                effectiveBlur,
                                android.graphics.Shader.TileMode.DECAL
                            ).asComposeRenderEffect()
                        }
                    } else if (effectiveBlur > 0.5f) {
                        Modifier.blur((effectiveBlur / 1.6f).dp)
                    } else {
                        Modifier
                    }
                )
                .drawWithCache {
                    val grainPoints = ArrayList<Offset>(grainFractions.size / 2)
                    var idx = 0
                    while (idx < grainFractions.size) {
                        grainPoints.add(
                            Offset(
                                grainFractions[idx] * size.width,
                                grainFractions[idx + 1] * size.height
                            )
                        )
                        idx += 2
                    }
                    onDrawWithContent {
                        drawRect(color = effectiveBaseColor)
                        if (params.grainAlpha > 0.005f) {
                            drawPoints(
                                points = grainPoints,
                                pointMode = PointMode.Points,
                                color = grainColor,
                                strokeWidth = 1f
                            )
                        }
                        drawContent()
                    }
                }
        )

        // Layer 4: Specular highlight gradient
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(specularHighlight)
        )

        // Layer 4b: Polished top lip sheen
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(shape)
                .background(topSheen)
        )

        // Layer 5: Luminous rim border
        if (drawRim) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(width = params.rimWidth, brush = borderBrush, shape = shape)
            )
        }

        // Layer 6: Content slot
        content()
    }
}
