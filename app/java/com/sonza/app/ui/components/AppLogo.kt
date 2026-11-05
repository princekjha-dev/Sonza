package com.sonza.app.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.sonza.app.R
import com.sonza.app.core.model.LogoVariant
import com.sonza.app.data.SessionManager
import org.koin.compose.koinInject

/**
 * Resolve the drawable resource id for the user's selected [LogoVariant].
 * [LogoVariant.CLASSIC] keeps the original `R.drawable.logo` shipped before
 * the 2026 design refresh; the three new variants are PNG renders of the
 * SVGs the designer delivered.
 */
@DrawableRes
fun LogoVariant.drawableRes(): Int = when (this) {
    LogoVariant.PULSE -> R.drawable.logo_pulse
    LogoVariant.PULSE_APP_ICON -> R.drawable.logo_pulse_app_icon
    LogoVariant.PULSE_MONO -> R.drawable.logo_pulse_mono
    LogoVariant.PULSE_LIGHT -> R.drawable.logo_pulse_light
    LogoVariant.PULSE_TONE -> R.drawable.logo_pulse_tone
    LogoVariant.RESONANCE -> R.drawable.logo_resonance
    LogoVariant.RESONANCE_APP_ICON -> R.drawable.logo_resonance_app_icon
    LogoVariant.RESONANCE_MONO -> R.drawable.logo_resonance_mono
    LogoVariant.RESONANCE_LIGHT -> R.drawable.logo_resonance_light
    LogoVariant.RESONANCE_TONE -> R.drawable.logo_resonance_tone
    LogoVariant.AETHER -> R.drawable.logo_aether
    LogoVariant.AETHER_APP_ICON -> R.drawable.logo_aether_app_icon
    LogoVariant.AETHER_MONO -> R.drawable.logo_aether_mono
    LogoVariant.AETHER_LIGHT -> R.drawable.logo_aether_light
    LogoVariant.AETHER_TONE -> R.drawable.logo_aether_tone
    LogoVariant.CLASSIC -> R.drawable.logo
}

@DrawableRes
fun LogoVariant.launcherIconRes(): Int = when (this) {
    LogoVariant.PULSE -> R.mipmap.ic_launcher_pulse
    LogoVariant.PULSE_APP_ICON -> R.mipmap.ic_launcher_pulse_app_icon
    LogoVariant.PULSE_MONO -> R.mipmap.ic_launcher_pulse_mono
    LogoVariant.PULSE_LIGHT -> R.mipmap.ic_launcher_pulse_light
    LogoVariant.PULSE_TONE -> R.mipmap.ic_launcher_pulse_tone
    LogoVariant.RESONANCE -> R.mipmap.ic_launcher_resonance
    LogoVariant.RESONANCE_APP_ICON -> R.mipmap.ic_launcher_resonance_app_icon
    LogoVariant.RESONANCE_MONO -> R.mipmap.ic_launcher_resonance_mono
    LogoVariant.RESONANCE_LIGHT -> R.mipmap.ic_launcher_resonance_light
    LogoVariant.RESONANCE_TONE -> R.mipmap.ic_launcher_resonance_tone
    LogoVariant.AETHER -> R.mipmap.ic_launcher_aether
    LogoVariant.AETHER_APP_ICON -> R.mipmap.ic_launcher_aether_app_icon
    LogoVariant.AETHER_MONO -> R.mipmap.ic_launcher_aether_mono
    LogoVariant.AETHER_LIGHT -> R.mipmap.ic_launcher_aether_light
    LogoVariant.AETHER_TONE -> R.mipmap.ic_launcher_aether_tone
    LogoVariant.CLASSIC -> R.mipmap.ic_launcher
}

/**
 * Renders the active app logo as an [Image]. Reactively swaps when the
 * user picks a different variant from Appearance settings.
 */
@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    contentDescription: String? = "Sonza",
    contentScale: ContentScale = ContentScale.Fit,
    colorFilter: ColorFilter? = null,
) {
    val sessionManager: SessionManager = koinInject()
    val variant by sessionManager.logoVariantFlow.collectAsState(initial = LogoVariant.DEFAULT)
    Image(
        painter = painterResource(id = variant.drawableRes()),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        colorFilter = colorFilter,
    )
}

/** Convenience overload for callers that just want a square logo at a fixed size. */
@Composable
fun AppLogo(size: Dp, modifier: Modifier = Modifier) {
    AppLogo(modifier = modifier.size(size))
}

/**
 * Renders the actual Sonza application launcher icon resource (e.g. adaptive icon
 * with background and foreground layers rendered faithfully without tinting).
 */
@Composable
fun AppLauncherIcon(
    modifier: Modifier = Modifier,
    contentDescription: String? = "Sonza Launcher Icon",
    contentScale: ContentScale = ContentScale.Fit,
) {
    val context = LocalContext.current
    val sessionManager: SessionManager = koinInject()
    val variant by sessionManager.logoVariantFlow.collectAsState(initial = LogoVariant.DEFAULT)

    val iconBitmap = remember<ImageBitmap?>(variant, context) {
        try {
            val iconRes = variant.launcherIconRes()
            val drawable = try {
                ContextCompat.getDrawable(context, iconRes)
            } catch (e: Exception) {
                null
            } ?: try {
                ContextCompat.getDrawable(context, R.mipmap.ic_launcher)
            } catch (e: Exception) {
                null
            } ?: context.packageManager.getApplicationIcon(context.packageName)

            val size = 512
            val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, size, size)
            drawable.draw(canvas)
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
        )
    } else {
        AppLogo(
            modifier = modifier,
            contentDescription = contentDescription,
            contentScale = contentScale,
        )
    }
}

/** Convenience overload for callers that just want a square launcher icon at a fixed size. */
@Composable
fun AppLauncherIcon(size: Dp, modifier: Modifier = Modifier) {
    AppLauncherIcon(modifier = modifier.size(size))
}


