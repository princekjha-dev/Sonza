package com.sonza.app.ui.components.glass

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonza.app.core.model.GlassStyle

/**
 * Parameters for each of the 3 Liquid Glass texture modes.
 */
data class GlassStyleParams(
    val blurRadius: Float,
    val darkBaseAlpha: Float,
    val lightBaseAlpha: Float,
    val specularIntensity: Float,
    val topSheenIntensity: Float,
    val rimWidth: Dp,
    val elevation: Dp,
    val grainAlpha: Float,
    val chromaticRim: Boolean
)

/**
 * Centralized design tokens and physical motion curves for the Sonza Liquid Glass design system.
 */
object LiquidGlassTokens {

    // Style parameter lookup
    fun getParams(style: GlassStyle): GlassStyleParams = when (style) {
        GlassStyle.SOFT -> GlassStyleParams(
            blurRadius = 32f,
            darkBaseAlpha = 0.32f,
            lightBaseAlpha = 0.42f,
            specularIntensity = 0.16f,
            topSheenIntensity = 0.22f,
            rimWidth = 0.6.dp,
            elevation = 10.dp,
            grainAlpha = 0.01f,
            chromaticRim = false
        )
        GlassStyle.NATURAL -> GlassStyleParams(
            blurRadius = 55f,
            darkBaseAlpha = 0.45f,
            lightBaseAlpha = 0.58f,
            specularIntensity = 0.28f,
            topSheenIntensity = 0.38f,
            rimWidth = 0.85.dp,
            elevation = 20.dp,
            grainAlpha = 0.025f,
            chromaticRim = false
        )
        GlassStyle.REALISTIC -> GlassStyleParams(
            blurRadius = 85f,
            darkBaseAlpha = 0.62f,
            lightBaseAlpha = 0.72f,
            specularIntensity = 0.42f,
            topSheenIntensity = 0.55f,
            rimWidth = 1.2.dp,
            elevation = 32.dp,
            grainAlpha = 0.04f,
            chromaticRim = true
        )
    }

    // Standard Radii
    val RadiusPill: Dp = 28.dp
    val RadiusCard: Dp = 22.dp
    val RadiusCardLarge: Dp = 28.dp
    val RadiusSheet: Dp = 32.dp
    val RadiusControl: Dp = 20.dp
    val RadiusChip: Dp = 14.dp
    val RadiusAvatar: Dp = 20.dp

    // Design Tokens: Blur Radii
    object Blur {
        val Soft: Float = 32f
        val Natural: Float = 55f
        val Realistic: Float = 85f
    }

    // Design Tokens: Elevations
    object Elevation {
        val None: Dp = 0.dp
        val Low: Dp = 8.dp
        val Medium: Dp = 16.dp
        val High: Dp = 24.dp
        val Modal: Dp = 32.dp
    }

    // Design Tokens: Icon Sizes
    object IconSizes {
        val Small: Dp = 16.dp
        val Medium: Dp = 20.dp
        val Large: Dp = 24.dp
        val Control: Dp = 32.dp
    }

    // Design Tokens: Animation Durations
    object Durations {
        const val FastMs: Int = 150
        const val NormalMs: Int = 250
        const val FluidMs: Int = 350
        const val ColorMeltMs: Int = 450
    }

    // Fluid Motion Spring Specs
    val FluidEasing: Easing = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)

    object Springs {
        val Interactive: FiniteAnimationSpec<Float> = spring(
            dampingRatio = 0.75f,
            stiffness = 450f
        )
        val Fluid: FiniteAnimationSpec<Float> = spring(
            dampingRatio = 0.82f,
            stiffness = 380f
        )
        val Gentle: FiniteAnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
    }

    fun <T> springInteractive(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.75f,
        stiffness = 450f
    )

    fun <T> springSheet(): FiniteAnimationSpec<T> = spring(
        dampingRatio = 0.82f,
        stiffness = 380f
    )

    fun <T> springGentle(): FiniteAnimationSpec<T> = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )

    fun <T> tweenFluid(durationMs: Int = 300): FiniteAnimationSpec<T> =
        tween(durationMillis = durationMs, easing = FluidEasing)

    // Micro-interaction scales
    const val PressScale: Float = 0.96f
    const val CardHoverScale: Float = 0.985f

    // Accent lighting colors
    val AmbientCyan = Color(0xFF38BDF8)
    val AmbientBlue = Color(0xFF3B82F6)
    val AmbientPurple = Color(0xFFB388FF)
    val AmbientPink = Color(0xFFFF4081)
}
