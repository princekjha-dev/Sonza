package com.sonza.app.ai.orbit.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.components.SonzaDynamicColors
import com.sonza.app.ui.theme.SonzaBrandAccent

/**
 * Dynamic Music-Reactive Orbit Color Palette.
 *
 * Provides harmonious colors and gradients derived directly from the currently
 * playing track's artwork. Colors continuously and smoothly morph across song transitions
 * (850ms interpolation) with zero abrupt switching.
 */
@Immutable
data class OrbitColorPalette(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val glow: Color,
    val waveformGradient: Brush,
    val avatarRingGradient: Brush,
    val surfaceGradient: Brush,
    val surfaceBackground: Color,
    val borderStroke: Color,
    val onSurfaceText: Color,
    val onSurfaceSubtext: Color
)

/**
 * Dynamic Music-Reactive Color Engine for Orbit Voice Assistant.
 *
 * Guarantees:
 * 1. Real dynamic palette calculated from active track artwork / dominant colors.
 * 2. Continuous, liquid 850ms animated transitions between track changes (no hard jumps).
 * 3. Contrast safeguards: readable text and dark-mode compliance.
 * 4. Zero expensive per-frame image analysis: operates off cached palette tokens.
 */
object OrbitColorEngine {

    private const val TRANSITION_DURATION_MS = 850

    /**
     * Composable hook that observes active song & dynamic tokens, returning
     * smoothly interpolated [OrbitColorPalette] colors.
     */
    @Composable
    fun rememberAnimatedOrbitPalette(
        currentSong: Song? = null,
        dynamicColors: SonzaDynamicColors = LocalSonzaDynamicColors.current
    ): OrbitColorPalette {
        val baseColor = dynamicColors.accent.takeIf { it != Color.Unspecified }
            ?: dynamicColors.primary.takeIf { it != Color.Unspecified }
            ?: SonzaBrandAccent

        // Calculate raw target palette
        val rawPalette = remember(baseColor, currentSong?.id) {
            calculateHarmoniousPalette(baseColor)
        }

        // Animated color states ensuring smooth 850ms transition between songs
        val animatedPrimary = animateColorAsState(
            targetValue = rawPalette.primary,
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            label = "orbit_primary_anim"
        ).value

        val animatedSecondary = animateColorAsState(
            targetValue = rawPalette.secondary,
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            label = "orbit_secondary_anim"
        ).value

        val animatedAccent = animateColorAsState(
            targetValue = rawPalette.accent,
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            label = "orbit_accent_anim"
        ).value

        val animatedGlow = animateColorAsState(
            targetValue = rawPalette.glow,
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            label = "orbit_glow_anim"
        ).value

        val animatedBorder = animateColorAsState(
            targetValue = rawPalette.borderStroke,
            animationSpec = tween(TRANSITION_DURATION_MS, easing = FastOutSlowInEasing),
            label = "orbit_border_anim"
        ).value

        return remember(animatedPrimary, animatedSecondary, animatedAccent, animatedGlow, animatedBorder) {
            OrbitColorPalette(
                primary = animatedPrimary,
                secondary = animatedSecondary,
                accent = animatedAccent,
                glow = animatedGlow,
                waveformGradient = Brush.horizontalGradient(
                    listOf(animatedPrimary, animatedSecondary, animatedAccent)
                ),
                avatarRingGradient = Brush.sweepGradient(
                    listOf(animatedPrimary, animatedSecondary, animatedAccent, animatedPrimary)
                ),
                surfaceGradient = Brush.verticalGradient(
                    listOf(
                        Color(0xFF141722).copy(alpha = 0.88f),
                        Color(0xFF0C0E15).copy(alpha = 0.94f)
                    )
                ),
                surfaceBackground = Color(0xFF11141D).copy(alpha = 0.85f),
                borderStroke = animatedBorder,
                onSurfaceText = Color.White.copy(alpha = 0.96f),
                onSurfaceSubtext = Color.White.copy(alpha = 0.68f)
            )
        }
    }

    /**
     * Derives harmonious, mood-reactive palette stops from a base artwork color.
     */
    private fun calculateHarmoniousPalette(base: Color): RawOrbitPalette {
        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(base.toArgb(), hsl)
        val hue = hsl[0]
        val saturation = hsl[1]
        val lightness = hsl[2]

        // Dark or low-saturation fallback: deep cosmic indigo with celestial cyan
        if (lightness < 0.18f || saturation < 0.12f) {
            val primary = Color(0xFF38538A)
            val secondary = Color(0xFF00B4D8)
            val accent = Color(0xFF90E0EF)
            return RawOrbitPalette(
                primary = primary,
                secondary = secondary,
                accent = accent,
                glow = primary.copy(alpha = 0.45f),
                borderStroke = primary.copy(alpha = 0.40f)
            )
        }

        // Ensure safe visibility bounds for dark mode
        val clampedSat = saturation.coerceIn(0.50f, 0.88f)
        val clampedLight = lightness.coerceIn(0.42f, 0.65f)
        val primary = hslToColor(hue, clampedSat, clampedLight)

        val (secondary, accent) = when {
            // Warm / desert / gold / amber (hue 25..65)
            hue in 25f..65f -> {
                val sec = hslToColor((hue - 18f + 360f) % 360f, (clampedSat * 0.95f).coerceIn(0.5f, 0.9f), 0.52f)
                val acc = hslToColor((hue + 22f) % 360f, 0.95f, 0.64f)
                sec to acc
            }
            // Romantic / pink / red / warm coral (hue 335..360 or 0..25)
            hue >= 335f || hue < 25f -> {
                val sec = hslToColor(16f, 0.92f, 0.58f) // warm orange/coral
                val acc = hslToColor(342f, 0.88f, 0.66f) // pink/rose
                sec to acc
            }
            // Calm / acoustic / blue / cyan (hue 180..240)
            hue in 180f..240f -> {
                val sec = hslToColor(184f, 0.92f, 0.54f) // cyan
                val acc = hslToColor(212f, 0.88f, 0.68f) // electric sky
                sec to acc
            }
            // Energetic / pop / purple / magenta (hue 260..335)
            hue in 260f..335f -> {
                val sec = hslToColor(308f, 0.90f, 0.60f) // magenta
                val acc = hslToColor(276f, 0.92f, 0.70f) // vibrant violet
                sec to acc
            }
            // Lo-fi / chill / green / teal (hue 65..180)
            hue in 65f..180f -> {
                val sec = hslToColor(168f, 0.88f, 0.52f) // teal
                val acc = hslToColor(138f, 0.82f, 0.62f) // mint/emerald
                sec to acc
            }
            // Analogous blend
            else -> {
                val sec = hslToColor((hue + 28f) % 360f, clampedSat, 0.56f)
                val acc = hslToColor((hue + 54f) % 360f, clampedSat.coerceIn(0.6f, 0.95f), 0.66f)
                sec to acc
            }
        }

        return RawOrbitPalette(
            primary = primary,
            secondary = secondary,
            accent = accent,
            glow = primary.copy(alpha = 0.40f),
            borderStroke = primary.copy(alpha = 0.38f)
        )
    }

    private fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
        val argb = ColorUtils.HSLToColor(floatArrayOf(hue, saturation, lightness))
        return Color(argb)
    }

    private data class RawOrbitPalette(
        val primary: Color,
        val secondary: Color,
        val accent: Color,
        val glow: Color,
        val borderStroke: Color
    )
}
