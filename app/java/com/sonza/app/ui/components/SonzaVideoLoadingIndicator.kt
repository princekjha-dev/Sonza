package com.sonza.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * Official Sonza Loading Indicator (loding.gif).
 */
@Composable
fun SonzaVideoLoadingIndicator(
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
fun SonzaVideoLoadingIndicator(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    SonzaLoadingIndicator(
        size = size,
        modifier = modifier,
        color = color
    )
}
