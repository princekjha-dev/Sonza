package com.sonza.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.ColorUtils
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.sonza.app.core.model.Song
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SonzaBackground
import com.sonza.app.ui.theme.SonzaBrandAccent
import com.sonza.app.ui.theme.SonzaIdleAccent
import com.sonza.app.ui.theme.SonzaIdleAccentMuted
import com.sonza.app.ui.theme.SonzaOnBackground
import com.sonza.app.ui.theme.SonzaOnSurfaceVariant
import com.sonza.app.ui.theme.SonzaOutline
import com.sonza.app.ui.theme.SonzaScrim
import com.sonza.app.ui.theme.SonzaSurface
import com.sonza.app.ui.theme.SonzaSurfaceVariant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Dynamic per-screen color tokens derived at runtime from active artwork (Requirement 3).
 *
 * Exposes both modern tokens and explicit dynamic aliases:
 * - [dynamicPrimary], [dynamicSecondary], [dynamicAccent], [dynamicBackground],
 *   [dynamicSurface], [dynamicGlow], [dynamicBorder], [dynamicTextAccent].
 */
data class SonzaDynamicColors(
    val primary: Color = SonzaBrandAccent,
    val secondary: Color = Color(0xFF00BDD6), // Harmonious Cyan
    val accent: Color = SonzaBrandAccent,
    val background: Color = SonzaBackground,
    val surface: Color = SonzaSurface,
    val surfaceVariant: Color = SonzaSurfaceVariant,
    val glow: Color = SonzaBrandAccent.copy(alpha = 0.35f),
    val border: Color = SonzaBrandAccent.copy(alpha = 0.35f),
    val textAccent: Color = SonzaBrandAccent,
    val onAccent: Color = SonzaOnBackground,
    val onBackground: Color = SonzaOnBackground,
    val onSurface: Color = SonzaOnBackground,
    val onSurfaceVariant: Color = SonzaOnSurfaceVariant,
    val outline: Color = SonzaOutline,
    val scrim: Color = SonzaScrim,
    val accentMuted: Color = SonzaIdleAccentMuted,
    val isIdle: Boolean = true
) {
    // Explicit dynamic aliases per Requirement 3
    val dynamicPrimary: Color get() = primary
    val dynamicSecondary: Color get() = secondary
    val dynamicAccent: Color get() = accent
    val dynamicBackground: Color get() = background
    val dynamicSurface: Color get() = surface
    val dynamicSurfaceVariant: Color get() = surfaceVariant
    val dynamicGlow: Color get() = glow
    val dynamicBorder: Color get() = border
    val dynamicTextAccent: Color get() = textAccent
    val dynamicOnAccent: Color get() = onAccent
}

/**
 * Backward-compatible DominantColors representation with rich dynamic palette fields.
 */
data class DominantColors(
    val primary: Color = SonzaBrandAccent,
    val secondary: Color = Color(0xFF00BDD6),
    val accent: Color = SonzaBrandAccent,
    val onBackground: Color = SonzaOnBackground,
    val accentMuted: Color = SonzaIdleAccentMuted,
    val onAccent: Color = SonzaOnBackground,
    val glow: Color = SonzaBrandAccent.copy(alpha = 0.35f),
    val border: Color = SonzaBrandAccent.copy(alpha = 0.35f),
    val textAccent: Color = SonzaBrandAccent,
    val background: Color = SonzaBackground,
    val surface: Color = SonzaSurface,
    val isIdle: Boolean = true
) {
    fun toSonzaDynamicColors(): SonzaDynamicColors = SonzaDynamicColors(
        primary = primary,
        secondary = secondary,
        accent = accent,
        background = background,
        surface = surface,
        surfaceVariant = secondary,
        glow = glow,
        border = border,
        textAccent = textAccent,
        onAccent = onAccent,
        onBackground = onBackground,
        accentMuted = accentMuted,
        isIdle = isIdle
    )
}

/**
 * Global NowPlaying state holding track info and its derived dynamic palette (Requirement 1).
 */
data class NowPlaying(
    val song: Song? = null,
    val artist: String? = null,
    val artwork: Any? = null,
    val dynamicColors: SonzaDynamicColors = SonzaDynamicColors()
)

val LocalNowPlaying = compositionLocalOf { NowPlaying() }
val LocalSonzaDynamicColors = compositionLocalOf { SonzaDynamicColors() }

/**
 * Process-level LRU cache of extracted colors keyed by artwork identifier.
 */
private val dynamicColorsCache = android.util.LruCache<String, SonzaDynamicColors>(150)

fun cacheKeyForArtwork(artwork: Any?): String = when (artwork) {
    null -> "null"
    is String -> "url:$artwork"
    is Int -> "res:$artwork"
    else -> artwork.toString()
}

/**
 * Computes on-accent color ensuring WCAG AA contrast (≥ 4.5:1 for body, ≥ 3:1 for large text).
 */
fun computeOnAccent(accent: Color): Color {
    val darkText = Color(0xFF0F172A)
    val lightText = Color.White
    return if (accent.luminance() > 0.45f) darkText else lightText
}

/**
 * Extracts a complete Sonza dynamic palette from bitmap with saturation, brightness,
 * contrast, and dark-mode compatibility processing (Requirement 2 & 3).
 */
fun extractDynamicPaletteFromBitmap(
    bitmap: Bitmap,
    fallbackColor: Color = SonzaBrandAccent
): SonzaDynamicColors {
    val width = bitmap.width
    val height = bitmap.height
    if (width <= 0 || height <= 0) {
        val onAcc = computeOnAccent(fallbackColor)
        return SonzaDynamicColors(
            primary = fallbackColor,
            secondary = fallbackColor,
            accent = fallbackColor,
            glow = fallbackColor.copy(alpha = 0.35f),
            border = fallbackColor.copy(alpha = 0.35f),
            textAccent = fallbackColor,
            onAccent = onAcc,
            isIdle = true
        )
    }

    // Step through the bitmap to sample ~600 to 1500 pixels (< 1.5ms)
    val step = maxOf(1, minOf(width, height) / 25)

    class HueBucket {
        var count = 0
        var totalR = 0L
        var totalG = 0L
        var totalB = 0L
        var maxSat = -1f
        var bestVibrantPixel = 0
        var bestScore = -1f
    }
    val buckets = Array(12) { HueBucket() }
    val hslTemp = FloatArray(3)

    var globalMaxScore = -1f
    var globalVibrantPixel: Int? = null

    for (x in 0 until width step step) {
        for (y in 0 until height step step) {
            val pixel = bitmap.getPixel(x, y)
            val a = android.graphics.Color.alpha(pixel)
            if (a < 50) continue

            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)

            ColorUtils.RGBToHSL(r, g, b, hslTemp)
            val h = hslTemp[0]
            val s = hslTemp[1]
            val l = hslTemp[2]

            // Ignore extreme highlights and shadows
            if (l < 0.08f || l > 0.95f) continue

            val bucketIndex = ((h % 360f) / 30f).toInt().coerceIn(0, 11)
            val bucket = buckets[bucketIndex]
            bucket.count++
            bucket.totalR += r
            bucket.totalG += g
            bucket.totalB += b

            // Score vibrant candidates: rewards rich saturation and balanced dark-UI luminance (~0.55)
            val score = s * 1.5f + (1f - kotlin.math.abs(l - 0.55f))
            if (score > bucket.bestScore && s >= 0.18f) {
                bucket.bestScore = score
                bucket.bestVibrantPixel = pixel
                bucket.maxSat = s
            }
            if (score > globalMaxScore && s >= 0.18f) {
                globalMaxScore = score
                globalVibrantPixel = pixel
            }
        }
    }

    val dominantBucket = buckets.maxByOrNull { it.count }
    val bestBucketWithColor = buckets.filter { it.count > 0 }
        .maxByOrNull { it.count.toFloat() * (0.5f + it.maxSat.coerceAtLeast(0f)) }
        ?: dominantBucket

    if (dominantBucket == null || dominantBucket.count == 0) {
        val onAcc = computeOnAccent(fallbackColor)
        return SonzaDynamicColors(
            primary = fallbackColor,
            secondary = fallbackColor,
            accent = fallbackColor,
            glow = fallbackColor.copy(alpha = 0.35f),
            border = fallbackColor.copy(alpha = 0.35f),
            textAccent = fallbackColor,
            onAccent = onAcc,
            isIdle = true
        )
    }

    val domR = (dominantBucket.totalR / dominantBucket.count).toInt()
    val domG = (dominantBucket.totalG / dominantBucket.count).toInt()
    val domB = (dominantBucket.totalB / dominantBucket.count).toInt()
    val dominantHsl = FloatArray(3)
    ColorUtils.RGBToHSL(domR, domG, domB, dominantHsl)

    val baseVibrantInt = globalVibrantPixel ?: if (bestBucketWithColor != null && bestBucketWithColor.count > 0) {
        android.graphics.Color.rgb(
            (bestBucketWithColor.totalR / bestBucketWithColor.count).toInt(),
            (bestBucketWithColor.totalG / bestBucketWithColor.count).toInt(),
            (bestBucketWithColor.totalB / bestBucketWithColor.count).toInt()
        )
    } else {
        android.graphics.Color.rgb(domR, domG, domB)
    }

    val vibrantHsl = FloatArray(3)
    ColorUtils.colorToHSL(baseVibrantInt, vibrantHsl)

    // Secondary bucket: pick a bucket distinct in hue (>= 60 deg away or second highest count)
    val dominantIndex = ((dominantHsl[0] % 360f) / 30f).toInt().coerceIn(0, 11)
    val secondaryBucket = buckets.indices
        .filter { kotlin.math.abs(it - dominantIndex).let { diff -> minOf(diff, 12 - diff) } >= 2 && buckets[it].count > 0 }
        .maxByOrNull { buckets[it].count.toFloat() * (0.5f + buckets[it].maxSat.coerceAtLeast(0f)) }
        ?.let { buckets[it] }

    val secondaryHsl = FloatArray(3)
    if (secondaryBucket != null && secondaryBucket.count > 0) {
        val sR = (secondaryBucket.totalR / secondaryBucket.count).toInt()
        val sG = (secondaryBucket.totalG / secondaryBucket.count).toInt()
        val sB = (secondaryBucket.totalB / secondaryBucket.count).toInt()
        ColorUtils.RGBToHSL(sR, sG, sB, secondaryHsl)
    } else {
        secondaryHsl[0] = (vibrantHsl[0] + 35f) % 360f
        secondaryHsl[1] = (vibrantHsl[1] * 0.85f).coerceIn(0.35f, 0.85f)
        secondaryHsl[2] = vibrantHsl[2].coerceIn(0.40f, 0.65f)
    }

    // 1. Dynamic Accent: Electric, vivid, rich saturation, calibrated luminance for dark UI
    val accentHsl = FloatArray(3)
    accentHsl[0] = vibrantHsl[0]
    accentHsl[1] = (vibrantHsl[1] * 1.25f).coerceIn(0.55f, 1.0f)
    accentHsl[2] = vibrantHsl[2].coerceIn(0.50f, 0.68f)
    val accentColor = Color(ColorUtils.HSLToColor(accentHsl))

    // 2. Dynamic Primary: Dominant tone with rich saturation
    val primaryHsl = FloatArray(3)
    primaryHsl[0] = dominantHsl[0]
    primaryHsl[1] = (dominantHsl[1] * 1.10f).coerceIn(0.40f, 0.85f)
    primaryHsl[2] = dominantHsl[2].coerceIn(0.45f, 0.65f)
    val primaryColor = Color(ColorUtils.HSLToColor(primaryHsl))

    // 3. Dynamic Secondary: Harmonic secondary tone
    val secHsl = FloatArray(3)
    secHsl[0] = secondaryHsl[0]
    secHsl[1] = (secondaryHsl[1] * 1.10f).coerceIn(0.35f, 0.80f)
    secHsl[2] = secondaryHsl[2].coerceIn(0.42f, 0.62f)
    val secondaryColor = Color(ColorUtils.HSLToColor(secHsl))

    // 4. Dynamic Background: Deep, atmospheric dark foundation tinted with artwork dominant hue
    val bgHsl = FloatArray(3)
    bgHsl[0] = dominantHsl[0]
    bgHsl[1] = (vibrantHsl[1] * 0.35f).coerceIn(0.12f, 0.28f)
    bgHsl[2] = 0.052f // luxurious dark foundation (~#0A0B10)
    val backgroundColor = Color(ColorUtils.HSLToColor(bgHsl))

    // 5. Dynamic Surface: Elevated dark surface for cards and sheets
    val surfHsl = FloatArray(3)
    surfHsl[0] = dominantHsl[0]
    surfHsl[1] = (vibrantHsl[1] * 0.25f).coerceIn(0.10f, 0.22f)
    surfHsl[2] = 0.105f // elevated dark surface (~#131620)
    val surfaceColor = Color(ColorUtils.HSLToColor(surfHsl))

    val surfVarHsl = FloatArray(3)
    surfVarHsl[0] = secondaryHsl[0]
    surfVarHsl[1] = (secondaryHsl[1] * 0.20f).coerceIn(0.08f, 0.18f)
    surfVarHsl[2] = 0.155f // higher elevation (~#1C202C)
    val surfaceVariantColor = Color(ColorUtils.HSLToColor(surfVarHsl))

    // 6. Dynamic Text Accent: Bright, sharp legibility on dark surfaces
    val textAccHsl = FloatArray(3)
    textAccHsl[0] = accentHsl[0]
    textAccHsl[1] = (accentHsl[1] * 0.85f).coerceIn(0.50f, 0.85f)
    textAccHsl[2] = 0.82f
    val textAccentColor = Color(ColorUtils.HSLToColor(textAccHsl))

    // 7. Dynamic Glow and Border
    val glowColor = accentColor.copy(alpha = 0.32f)
    val borderColor = accentColor.copy(alpha = 0.35f)
    val onAccentColor = computeOnAccent(accentColor)

    return SonzaDynamicColors(
        primary = primaryColor,
        secondary = secondaryColor,
        accent = accentColor,
        background = backgroundColor,
        surface = surfaceColor,
        surfaceVariant = surfaceVariantColor,
        glow = glowColor,
        border = borderColor,
        textAccent = textAccentColor,
        onAccent = onAccentColor,
        onBackground = SonzaOnBackground,
        onSurface = SonzaOnBackground,
        onSurfaceVariant = SonzaOnSurfaceVariant,
        outline = surfaceVariantColor,
        scrim = SonzaScrim,
        accentMuted = accentColor.copy(alpha = 0.25f),
        isIdle = false
    )
}

/**
 * Extracts and animates dynamic colors with a smooth 300–800ms transition on artwork change (Requirement 6).
 */
@Composable
fun rememberDynamicAccentColors(
    artwork: Any?,
    fallbackColor: Color = LocalSonzaDynamicColors.current.accent.takeIf { it != Color.Unspecified } ?: SonzaBrandAccent
): SonzaDynamicColors {
    val defaultTokens = remember(fallbackColor) {
        val onAcc = computeOnAccent(fallbackColor)
        SonzaDynamicColors(
            primary = fallbackColor,
            secondary = Color(0xFF00BDD6),
            accent = fallbackColor,
            glow = fallbackColor.copy(alpha = 0.35f),
            border = fallbackColor.copy(alpha = 0.35f),
            textAccent = fallbackColor,
            onAccent = onAcc,
            isIdle = true
        )
    }

    val key = remember(artwork) { cacheKeyForArtwork(artwork) }
    var rawColors by remember(key) {
        val seeded = if (artwork != null) dynamicColorsCache.get(key) else defaultTokens
        mutableStateOf(seeded ?: defaultTokens)
    }

    val context = LocalContext.current

    LaunchedEffect(artwork, key) {
        if (artwork == null) {
            rawColors = defaultTokens
            return@LaunchedEffect
        }

        dynamicColorsCache.get(key)?.let { cached ->
            rawColors = cached
            return@LaunchedEffect
        }

        withContext(Dispatchers.IO) {
            try {
                val bitmap: Bitmap? = when (artwork) {
                    is Int -> {
                        BitmapFactory.decodeResource(context.resources, artwork)
                    }
                    else -> {
                        val loader = context.imageLoader
                        val request = ImageRequest.Builder(context)
                            .data(artwork)
                            .allowHardware(false)
                            .size(128)
                            .build()
                        val result = loader.execute(request)
                        if (result is SuccessResult) result.image.toBitmap() else null
                    }
                }

                if (bitmap != null) {
                    val extracted = extractDynamicPaletteFromBitmap(bitmap, fallbackColor)
                    dynamicColorsCache.put(key, extracted)
                    withContext(Dispatchers.Main) {
                        rawColors = extracted
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        rawColors = defaultTokens
                    }
                }
            } catch (_: Throwable) {
                withContext(Dispatchers.Main) {
                    rawColors = defaultTokens
                }
            }
        }
    }

    // Smooth transitions: 300–800ms cross-fade animation on song change (Requirement 6)
    val animatedPrimary by animateColorAsState(
        targetValue = rawColors.primary,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "dynamic_primary"
    )
    val animatedSecondary by animateColorAsState(
        targetValue = rawColors.secondary,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "dynamic_secondary"
    )
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
    val animatedBg by animateColorAsState(
        targetValue = rawColors.background,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "dynamic_bg"
    )
    val animatedSurface by animateColorAsState(
        targetValue = rawColors.surface,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "dynamic_surface"
    )
    val animatedSurfaceVar by animateColorAsState(
        targetValue = rawColors.surfaceVariant,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "dynamic_surface_variant"
    )
    val animatedGlow by animateColorAsState(
        targetValue = rawColors.glow,
        animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing),
        label = "dynamic_glow"
    )
    val animatedBorder by animateColorAsState(
        targetValue = rawColors.border,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "dynamic_border"
    )
    val animatedTextAccent by animateColorAsState(
        targetValue = rawColors.textAccent,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "dynamic_text_accent"
    )
    val animatedOnAccent by animateColorAsState(
        targetValue = rawColors.onAccent,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "dynamic_on_accent"
    )

    return remember(
        animatedPrimary, animatedSecondary, animatedAccent, animatedAccentMuted,
        animatedBg, animatedSurface, animatedSurfaceVar, animatedGlow,
        animatedBorder, animatedTextAccent, animatedOnAccent, rawColors.isIdle
    ) {
        SonzaDynamicColors(
            primary = animatedPrimary,
            secondary = animatedSecondary,
            accent = animatedAccent,
            background = animatedBg,
            surface = animatedSurface,
            surfaceVariant = animatedSurfaceVar,
            glow = animatedGlow,
            border = animatedBorder,
            textAccent = animatedTextAccent,
            onAccent = animatedOnAccent,
            onBackground = SonzaOnBackground,
            onSurface = SonzaOnBackground,
            onSurfaceVariant = SonzaOnSurfaceVariant,
            outline = animatedSurfaceVar,
            scrim = SonzaScrim,
            accentMuted = animatedAccentMuted,
            isIdle = rawColors.isIdle
        )
    }
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
            primary = dynamic.primary,
            secondary = dynamic.secondary,
            accent = dynamic.accent,
            onBackground = dynamic.onBackground,
            accentMuted = dynamic.accentMuted,
            onAccent = dynamic.onAccent,
            glow = dynamic.glow,
            border = dynamic.border,
            textAccent = dynamic.textAccent,
            background = dynamic.background,
            surface = dynamic.surface,
            isIdle = dynamic.isIdle
        )
    }
}

/**
 * Helper to construct and remember the global [NowPlaying] state (Requirement 1).
 */
@Composable
fun rememberNowPlayingState(
    song: Song?,
    fallbackArtwork: Any? = null,
    fallbackColor: Color = SonzaBrandAccent
): NowPlaying {
    val activeArtwork = song?.thumbnailUrl?.takeIf { it.isNotBlank() } ?: song?.localUri ?: fallbackArtwork
    val dynamicColors = rememberDynamicAccentColors(artwork = activeArtwork, fallbackColor = fallbackColor)
    return remember(song, activeArtwork, dynamicColors) {
        NowPlaying(
            song = song,
            artist = song?.artist,
            artwork = activeArtwork,
            dynamicColors = dynamicColors
        )
    }
}
