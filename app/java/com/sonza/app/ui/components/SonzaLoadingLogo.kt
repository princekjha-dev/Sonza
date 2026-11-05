package com.sonza.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Native Android Sonza loading indicator (loding.gif).
 */
@Composable
fun SonzaLoadingLogo(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    barCount: Int = 8,
    durationMillis: Int = 1000,
    inactiveAlpha: Float = 0.22f
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
fun SonzaLoadingLogo(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    durationMillis: Int = 1000
) {
    SonzaLoadingIndicator(
        size = size,
        modifier = modifier,
        color = color
    )
}
