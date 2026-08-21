package com.sonza.app.ui.components

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.request.ImageRequest
import com.sonza.app.R

/**
 * Official unified Sonza GIF Loading Indicator.
 *
 * Renders `loding.gif` using Coil 3 hardware-accelerated animated GIF decoding.
 * Loops automatically and cleans up immediately on composition disposal.
 */
@Composable
fun SonzaLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    size: Dp? = null
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (SDK_INT >= 28) {
                    add(AnimatedImageDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    val request = remember(context) {
        ImageRequest.Builder(context)
            .data(R.raw.loding)
            .build()
    }

    val finalModifier = if (size != null) modifier.size(size) else modifier

    Box(
        modifier = finalModifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = request,
            imageLoader = imageLoader,
            contentDescription = "Loading...",
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Fit
        )
    }
}

/**
 * Convenience overload accepting fixed Dp dimensions.
 */
@Composable
fun SonzaLoadingIndicator(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    SonzaLoadingIndicator(
        modifier = modifier.size(size),
        color = color
    )
}

/**
 * Standard LoadingIndicator — delegates directly to SonzaLoadingIndicator (loding.gif).
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    SonzaLoadingIndicator(
        modifier = modifier,
        color = color
    )
}

/**
 * Convenience overload accepting fixed Dp dimensions.
 */
@Composable
fun LoadingIndicator(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    SonzaLoadingIndicator(
        modifier = modifier.size(size),
        color = color
    )
}

/**
 * Overlay to be placed on top of artwork when loading.
 */
@Composable
fun LoadingArtworkOverlay(
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        SonzaLoadingIndicator(
            modifier = Modifier.size(48.dp),
            color = Color.White
        )
    }
}
