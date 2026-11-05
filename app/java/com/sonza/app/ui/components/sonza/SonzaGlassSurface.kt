package com.sonza.app.ui.components.sonza

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.components.bounceClick

/**
 * Premium Translucent Glass Surface with layered depth, subtle specular highlight,
 * and optional ambient album-art tint.
 */
@Composable
fun SonzaGlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = SonzaDesignTokens.Radii.GlassCardShape,
    backgroundColor: Color = SonzaDesignTokens.Colors.GlassBackground,
    borderColor: Color = SonzaDesignTokens.Colors.GlassBorder,
    borderWidth: Dp = 1.dp,
    elevation: Dp = SonzaDesignTokens.Elevation.None,
    dynamicAccent: Color? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val dynamicTint = dynamicAccent?.copy(alpha = 0.08f) ?: Color.Transparent
    val specularGradient = Brush.verticalGradient(
        colors = listOf(
            borderColor.copy(alpha = (borderColor.alpha * 1.5f).coerceAtMost(0.35f)),
            borderColor.copy(alpha = (borderColor.alpha * 0.4f))
        )
    )

    Box(
        modifier = modifier
            .then(
                if (elevation > 0.dp) {
                    Modifier.shadow(elevation = elevation, shape = shape, clip = false)
                } else Modifier
            )
            .clip(shape)
            .background(backgroundColor)
            .background(dynamicTint)
            .border(borderWidth, specularGradient, shape),
        content = content
    )
}

/**
 * Premium Interactive Glass Card with spring physics and touch reaction.
 */
@Composable
fun SonzaGlassCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = SonzaDesignTokens.Radii.GlassCardShape,
    backgroundColor: Color = SonzaDesignTokens.Colors.GlassBackground,
    borderColor: Color = SonzaDesignTokens.Colors.GlassBorder,
    dynamicAccent: Color? = null,
    elevation: Dp = SonzaDesignTokens.Elevation.None,
    content: @Composable BoxScope.() -> Unit
) {
    SonzaGlassSurface(
        modifier = modifier.bounceClick(scaleDown = SonzaDesignTokens.Springs.PressScaleDown, onClick = onClick),
        shape = shape,
        backgroundColor = backgroundColor,
        borderColor = borderColor,
        dynamicAccent = dynamicAccent,
        elevation = elevation,
        content = content
    )
}
