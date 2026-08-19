package com.sonza.app.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaDefaultAccent
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaScrim
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Dynamic per-screen color tokens derived at runtime from active album art (DESIGN_SYSTEM.md Part 1).
 */
data class SonzaDynamicColors(
    val accent: Color = SonzaDefaultAccent,
    val accentMuted: Color = SonzaDefaultAccent.copy(alpha = 0.25f),
    val onAccent: Color = SonzaOnBackground,
    val background: Color = SonzaBackground,
    val surface: Color = SonzaSurface,
    val surfaceVariant: Color = SonzaSurfaceVariant,
    val onBackground: Color = SonzaOnBackground,
    val onSurface: Color = SonzaOnBackground,
    val onSurfaceVariant: Color = SonzaOnSurfaceVariant,
    val outline: Color = SonzaOutline,
    val scrim: Color = SonzaScrim
)

/**
 * Backward-compatible DominantColors representation
 */
data class DominantColors(
    val primary: Color = SonzaSurface,
    val secondary: Color = SonzaSurfaceVariant,
    val accent: Color = SonzaDefaultAccent,
    val onBackground: Color = SonzaOnBackground,
    val accentMuted: Color = SonzaDefaultAccent.copy(alpha = 0.25f),
    val onAccent: Color = SonzaOnBackground
) {
    fun toSonzaDynamicColors(): SonzaDynamicColors = SonzaDynamicColors(
        accent = accent,
        accentMuted = accentMuted,
        onAccent = onAccent,
        background = SonzaBackground,
        surface = SonzaSurface,
        onBackground = onBackground
    )
}

val LocalSonzaDynamicColors = compositionLocalOf { SonzaDynamicColors() }

/**
 * Process-level LRU cache of extracted colors keyed by imageUrl.
 */
private val dynamicColorsCache = android.util.LruCache<String, SonzaDynamicColors>(100)

/**
 * Computes on-accent color ensuring WCAG AA contrast (≥ 4.5:1 for body, ≥ 3:1 for large text).
 */
fun computeOnAccent(accent: Color): Color {
    val accentInt = accent.toArgb()
    val darkText = android.graphics.Color.rgb(11, 11, 13) // SonzaBackground
    val lightText = android.graphics.Color.rgb(245, 245, 247) // SonzaOnBackground

    val darkContrast = ColorUtils.calculateContrast(darkText, accentInt)
    val lightContrast = ColorUtils.calculateContrast(lightText, accentInt)

    return if (darkContrast >= 4.5) {
        Color(darkText)
    } else if (lightContrast >= 4.5) {
        Color(lightText)
    } else if (darkContrast >= lightContrast) {
        Color(darkText)
    } else {
        Color(lightText)
    }
}

/**
 * Extracts and returns animated dynamic accent colors with a 400ms cross-fade transition on track change.
 */
@Composable
fun rememberDynamicAccentColors(
    imageUrl: String?,
    fallbackColor: Color = SonzaDefaultAccent
): SonzaDynamicColors {
    val defaultTokens = remember(fallbackColor) {
        val onAcc = computeOnAccent(fallbackColor)
        SonzaDynamicColors(
            accent = fallbackColor,
            accentMuted = fallbackColor.copy(alpha = 0.25f),
            onAccent = onAcc
        )
    }

    var rawColors by remember(imageUrl) {
        val seeded = imageUrl?.let { dynamicColorsCache.get(it) } ?: defaultTokens
        mutableStateOf(seeded)
    }

    val context = LocalContext.current

    LaunchedEffect(imageUrl) {
        if (imageUrl.isNullOrBlank()) {
            rawColors = defaultTokens
            return@LaunchedEffect
        }

        dynamicColorsCache.get(imageUrl)?.let { cached ->
            rawColors = cached
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                val loader = context.imageLoader
                val request = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .allowHardware(false)
                    .size(100)
                    .build()

                val result = loader.execute(request)
                if (result is SuccessResult) {
                    val bitmap = result.image.toBitmap()
                    val extracted = extractDynamicColorsFromBitmap(bitmap, fallbackColor)
                    dynamicColorsCache.put(imageUrl, extracted)
                    withContext(Dispatchers.Main) {
                        rawColors = extracted
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    rawColors = defaultTokens
                }
            }
        }
    }

    // 400ms cross-fade animation on track change (DESIGN_SYSTEM.md Part 8)
    val animatedAccent by animateColorAsState(
        targetValue = rawColors.accent,
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "dynamic_accent"
    )
    val animatedAccentMuted by animateColorAsState(
        targetValue = rawColors.accentMuted,
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "dynamic_accent_muted"
    )
    val animatedOnAccent by animateColorAsState(
        targetValue = rawColors.onAccent,
        animationSpec = tween(durationMillis = MotionTokens.AccentCrossfadeDuration, easing = FastOutSlowInEasing),
        label = "dynamic_on_accent"
    )

    return remember(animatedAccent, animatedAccentMuted, animatedOnAccent) {
        SonzaDynamicColors(
            accent = animatedAccent,
            accentMuted = animatedAccentMuted,
            onAccent = animatedOnAccent,
            background = SonzaBackground,
            surface = SonzaSurface,
            surfaceVariant = SonzaSurfaceVariant,
            onBackground = SonzaOnBackground,
            onSurface = SonzaOnBackground,
            onSurfaceVariant = SonzaOnSurfaceVariant,
            outline = SonzaOutline,
            scrim = SonzaScrim
        )
    }
}

/**
 * Extracts dynamic accent colors from bitmap with saturation & luminance adjustments.
 */
private fun extractDynamicColorsFromBitmap(bitmap: Bitmap, fallbackColor: Color): SonzaDynamicColors {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) {
        val onAcc = computeOnAccent(fallbackColor)
        return SonzaDynamicColors(accent = fallbackColor, accentMuted = fallbackColor.copy(alpha = 0.25f), onAccent = onAcc)
    }

    val step = maxOf(1, minOf(width, height) / 10)
    var totalR = 0L
    var totalG = 0L
    var totalB = 0L
    var count = 0

    // Also look for the most vibrant/saturated pixel
    var maxSaturation = -1f
    var vibrantColorInt: Int? = null
    val hslTemp = FloatArray(3)

    for (x in 0 until width step step) {
        for (y in 0 until height step step) {
            val pixel = bitmap.getPixel(x, y)
            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)

            totalR += r
            totalG += g
            totalB += b
            count++

            ColorUtils.RGBToHSL(r, g, b, hslTemp)
            // Skip overly dark or overly washed out pixels for vibrant detection
            if (hslTemp[1] > maxSaturation && hslTemp[2] in 0.2f..0.85f) {
                maxSaturation = hslTemp[1]
                vibrantColorInt = pixel
            }
        }
    }

    if (count == 0) {
        val onAcc = computeOnAccent(fallbackColor)
        return SonzaDynamicColors(accent = fallbackColor, accentMuted = fallbackColor.copy(alpha = 0.25f), onAccent = onAcc)
    }

    val baseAccentInt = vibrantColorInt ?: android.graphics.Color.rgb(
        (totalR / count).toInt(),
        (totalG / count).toInt(),
        (totalB / count).toInt()
    )

    // Adjust HSL for a rich, vibrant accent
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(baseAccentInt, hsl)
    hsl[1] = (hsl[1] * 1.15f).coerceIn(0.35f, 1.0f) // Keep rich saturation
    hsl[2] = hsl[2].coerceIn(0.45f, 0.70f) // Keep balanced luminance for dark theme

    val accentInt = ColorUtils.HSLToColor(hsl)
    val accent = Color(accentInt)
    val accentMuted = accent.copy(alpha = 0.25f)
    val onAccent = computeOnAccent(accent)

    return SonzaDynamicColors(
        accent = accent,
        accentMuted = accentMuted,
        onAccent = onAccent,
        background = SonzaBackground,
        surface = SonzaSurface,
        surfaceVariant = SonzaSurfaceVariant,
        onBackground = SonzaOnBackground,
        onSurface = SonzaOnBackground,
        onSurfaceVariant = SonzaOnSurfaceVariant,
        outline = SonzaOutline,
        scrim = SonzaScrim
    )
}

/**
 * Backward-compatible helper for existing screens.
 */
@Composable
fun rememberDominantColors(
    imageUrl: String?,
    isDarkTheme: Boolean = true,
    defaultColors: DominantColors? = null
): DominantColors {
    val dynamic = rememberDynamicAccentColors(imageUrl)
    return remember(dynamic) {
        DominantColors(
            primary = dynamic.surface,
            secondary = dynamic.surfaceVariant,
            accent = dynamic.accent,
            onBackground = dynamic.onBackground,
            accentMuted = dynamic.accentMuted,
            onAccent = dynamic.onAccent
        )
    }
}

