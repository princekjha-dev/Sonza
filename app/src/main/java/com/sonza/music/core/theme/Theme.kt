package com.sonza.music.core.theme

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.palette.graphics.Palette

@Immutable
data class SonzaDynamicPalette(
    val background: Color = SonzaDarkBackground,
    val surface: Color = SonzaSurface,
    val surfaceVariant: Color = SonzaSurfaceVariant,
    val accent: Color = SonzaCyanAccent,
    val secondary: Color = SonzaAmberGold,
    val textPrimary: Color = SonzaTextPrimary,
    val textSecondary: Color = SonzaTextSecondary
)

object DynamicThemeExtractor {

    private val cache = mutableMapOf<String, SonzaDynamicPalette>()

    fun extractPalette(bitmap: Bitmap?, key: String? = null): SonzaDynamicPalette {
        if (key != null && cache.containsKey(key)) {
            return cache[key]!!
        }

        if (bitmap == null) {
            return defaultPalette()
        }

        val palette = Palette.from(bitmap)
            .maximumColorCount(24)
            .generate()

        val dominantSwatch = palette.dominantSwatch
        val vibrantSwatch = palette.vibrantSwatch ?: palette.darkVibrantSwatch ?: dominantSwatch
        val mutedSwatch = palette.mutedSwatch ?: palette.darkMutedSwatch

        val rawAccent = vibrantSwatch?.rgb?.let { Color(it) } ?: SonzaCyanAccent
        val rawBackground = dominantSwatch?.rgb?.let { Color(it) } ?: SonzaDarkBackground
        val rawSurface = mutedSwatch?.rgb?.let { Color(it) } ?: SonzaSurface

        // Deepen background to create audiophile dark stage
        val darkBg = darkenColor(rawBackground, 0.85f)
        val darkSurface = darkenColor(rawSurface, 0.70f)
        val darkSurfaceVariant = brightenColor(darkSurface, 0.15f)

        // Ensure accent color pops with sufficient contrast
        val adjustedAccent = ensureContrast(rawAccent, darkBg, minContrast = 3.5f)
        val secondaryAccent = ensureContrast(
            palette.lightVibrantSwatch?.rgb?.let { Color(it) } ?: SonzaAmberGold,
            darkBg,
            minContrast = 3.0f
        )

        val result = SonzaDynamicPalette(
            background = darkBg,
            surface = darkSurface,
            surfaceVariant = darkSurfaceVariant,
            accent = adjustedAccent,
            secondary = secondaryAccent,
            textPrimary = if (darkBg.luminance() < 0.5f) Color(0xFFF8FAFC) else Color(0xFF0F172A),
            textSecondary = if (darkBg.luminance() < 0.5f) Color(0xFF94A3B8) else Color(0xFF475569)
        )

        if (key != null) {
            if (cache.size > 50) cache.clear()
            cache[key] = result
        }

        return result
    }

    fun defaultPalette(): SonzaDynamicPalette {
        return SonzaDynamicPalette(
            background = SonzaDarkBackground,
            surface = SonzaSurface,
            surfaceVariant = SonzaSurfaceVariant,
            accent = SonzaCyanAccent,
            secondary = SonzaAmberGold,
            textPrimary = SonzaTextPrimary,
            textSecondary = SonzaTextSecondary
        )
    }

    private fun darkenColor(color: Color, factor: Float): Color {
        return Color(
            red = (color.red * (1f - factor)).coerceIn(0.04f, 0.12f),
            green = (color.green * (1f - factor)).coerceIn(0.04f, 0.12f),
            blue = (color.blue * (1f - factor)).coerceIn(0.06f, 0.18f),
            alpha = 1.0f
        )
    }

    private fun brightenColor(color: Color, factor: Float): Color {
        return Color(
            red = (color.red + factor).coerceIn(0.08f, 0.35f),
            green = (color.green + factor).coerceIn(0.08f, 0.35f),
            blue = (color.blue + factor).coerceIn(0.12f, 0.45f),
            alpha = 1.0f
        )
    }

    private fun ensureContrast(foreground: Color, background: Color, minContrast: Float): Color {
        val bgLum = background.luminance()
        val fgLum = foreground.luminance()
        val currentContrast = (Math.max(bgLum, fgLum) + 0.05f) / (Math.min(bgLum, fgLum) + 0.05f)

        return if (currentContrast >= minContrast) {
            foreground
        } else {
            // Brighten for dark backgrounds
            Color(
                red = (foreground.red * 1.4f).coerceIn(0f, 1f),
                green = (foreground.green * 1.4f).coerceIn(0f, 1f),
                blue = (foreground.blue * 1.4f).coerceIn(0f, 1f),
                alpha = 1.0f
            )
        }
    }
}

@Composable
fun SonzaTheme(
    dynamicPalette: SonzaDynamicPalette? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val palette = dynamicPalette ?: DynamicThemeExtractor.defaultPalette()

    // Smooth spring/tween transition between album art theme changes
    val animatedBg = animateColorAsState(targetValue = palette.background, animationSpec = tween(600), label = "bg")
    val animatedSurface = animateColorAsState(targetValue = palette.surface, animationSpec = tween(600), label = "surface")
    val animatedAccent = animateColorAsState(targetValue = palette.accent, animationSpec = tween(600), label = "accent")
    val animatedSecondary = animateColorAsState(targetValue = palette.secondary, animationSpec = tween(600), label = "secondary")

    val colorScheme = darkColorScheme(
        primary = animatedAccent.value,
        onPrimary = Color.Black,
        primaryContainer = animatedAccent.value.copy(alpha = 0.2f),
        onPrimaryContainer = animatedAccent.value,
        secondary = animatedSecondary.value,
        onSecondary = Color.Black,
        background = animatedBg.value,
        onBackground = palette.textPrimary,
        surface = animatedSurface.value,
        onSurface = palette.textPrimary,
        surfaceVariant = palette.surfaceVariant,
        onSurfaceVariant = palette.textSecondary
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SonzaTypography,
        content = content
    )
}
