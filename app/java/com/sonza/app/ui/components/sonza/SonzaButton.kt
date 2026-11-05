package com.sonza.app.ui.components.sonza

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.theme.SonzaTypography
import com.sonza.app.ui.utils.rememberHaptics

enum class SonzaButtonStyle {
    Filled,
    Glass,
    Outlined
}

/**
 * Premium Sonza Button with fluid spring bounce and haptics.
 */
@Composable
fun SonzaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SonzaButtonStyle = SonzaButtonStyle.Filled,
    icon: ImageVector? = null,
    accentColor: Color = SonzaDesignTokens.Colors.BrandAccent,
    enabled: Boolean = true,
    shape: Shape = SonzaDesignTokens.Radii.PillShape
) {
    val haptics = rememberHaptics()

    val bgModifier = when (style) {
        SonzaButtonStyle.Filled -> Modifier.background(
            Brush.horizontalGradient(
                listOf(accentColor, accentColor.copy(alpha = 0.85f))
            )
        )
        SonzaButtonStyle.Glass -> Modifier
            .background(SonzaDesignTokens.Colors.GlassBackground)
            .border(1.dp, SonzaDesignTokens.Colors.GlassBorder, shape)
        SonzaButtonStyle.Outlined -> Modifier
            .border(1.dp, accentColor.copy(alpha = 0.6f), shape)
    }

    val contentColor = when (style) {
        SonzaButtonStyle.Filled -> Color.White
        SonzaButtonStyle.Glass -> SonzaDesignTokens.Colors.TextPrimary
        SonzaButtonStyle.Outlined -> accentColor
    }

    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp, minWidth = 88.dp)
            .clip(shape)
            .then(bgModifier)
            .then(
                if (enabled) {
                    Modifier.bounceClick(scaleDown = 0.94f) {
                        haptics.tick()
                        onClick()
                    }
                } else Modifier
            )
            .padding(horizontal = 20.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = text,
                style = SonzaTypography.BodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Premium Sonza Icon Button with circular glass backdrop and minimum 48dp touch target.
 */
@Composable
fun SonzaIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 22.dp,
    tint: Color = SonzaDesignTokens.Colors.TextPrimary,
    backgroundColor: Color = SonzaDesignTokens.Colors.GlassBackgroundLight,
    borderColor: Color = SonzaDesignTokens.Colors.GlassBorderSubtle,
    enabled: Boolean = true
) {
    val haptics = rememberHaptics()

    Box(
        modifier = modifier
            .size(size)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .then(
                if (enabled) {
                    Modifier.bounceClick(scaleDown = 0.92f) {
                        haptics.tick()
                        onClick()
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(iconSize)
        )
    }
}
