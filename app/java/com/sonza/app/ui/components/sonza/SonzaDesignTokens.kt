package com.sonza.app.ui.components.sonza

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sonza.app.ui.theme.RadiusTokens
import com.sonza.app.ui.theme.SpacingTokens

/**
 * Centralized Design System Tokens for Sonza.
 * Guarantees visual harmony across screens and widgets without ad-hoc magic numbers.
 */
object SonzaDesignTokens {

    object Colors {
        // Deep Obsidian / OLED Base
        val BackgroundOled = Color(0xFF000000)
        val BackgroundDark = Color(0xFF0B0B0E)
        val BackgroundSurface = Color(0xFF141418)
        val BackgroundSurfaceElevated = Color(0xFF1B1B22)

        // Glass Materials
        val GlassBackground = Color(0xFF1E1E26).copy(alpha = 0.72f)
        val GlassBackgroundLight = Color(0xFFFFFFFF).copy(alpha = 0.08f)
        val GlassBorder = Color(0xFFFFFFFF).copy(alpha = 0.12f)
        val GlassBorderSubtle = Color(0xFFFFFFFF).copy(alpha = 0.06f)
        val GlassHighlight = Color(0xFFFFFFFF).copy(alpha = 0.18f)

        // Text & Content
        val TextPrimary = Color(0xFFF6F6F9)
        val TextSecondary = Color(0xFFA2A2B0)
        val TextTertiary = Color(0xFF6E6E7E)

        // Accent brand fallback
        val BrandAccent = Color(0xFF4F86F7)
        val BrandAccentMuted = Color(0xFF4F86F7).copy(alpha = 0.22f)
        val BrandAccentGlow = Color(0xFF4F86F7).copy(alpha = 0.40f)

        // Status
        val Success = Color(0xFF38D39F)
        val Warning = Color(0xFFFFB84D)
        val Error = Color(0xFFFF5252)

        // Orbit AI Glow Palette
        val OrbitIdle = Color(0xFF5A87FF)
        val OrbitListening = Color(0xFF00E5FF)
        val OrbitThinking = Color(0xFFB388FF)
        val OrbitWorking = Color(0xFFFF9100)
        val OrbitSpeaking = Color(0xFF00E676)
        val OrbitSuccess = Color(0xFF00E676)
        val OrbitError = Color(0xFFFF5252)
    }

    object Radii {
        val Xs: Dp = 6.dp
        val Sm: Dp = RadiusTokens.Sm      // 8.dp
        val Md: Dp = RadiusTokens.Md      // 12.dp
        val Lg: Dp = RadiusTokens.Lg      // 16.dp
        val Xl: Dp = RadiusTokens.Xl      // 20.dp
        val Xxl: Dp = RadiusTokens.Xxl    // 24.dp
        val Pill: Dp = RadiusTokens.Pill  // 999.dp

        val CardShape = RoundedCornerShape(Xxl)
        val GlassCardShape = RoundedCornerShape(22.dp)
        val MiniPlayerShape = RoundedCornerShape(26.dp)
        val ButtonShape = RoundedCornerShape(Md)
        val PillShape = RoundedCornerShape(Pill)
        val CircleShape = CircleShape
    }

    object Spacing {
        val Space2Xs = SpacingTokens.Space2Xs // 2.dp
        val SpaceXs = SpacingTokens.SpaceXs   // 4.dp
        val SpaceSm = SpacingTokens.SpaceSm   // 8.dp
        val SpaceMd = SpacingTokens.SpaceMd   // 12.dp
        val SpaceLg = SpacingTokens.SpaceLg   // 16.dp
        val SpaceXl = SpacingTokens.SpaceXl   // 24.dp
        val Space2Xl = SpacingTokens.Space2Xl // 32.dp
        val Space3Xl = SpacingTokens.Space3Xl // 40.dp
    }

    object Elevation {
        val None: Dp = 0.dp
        val Low: Dp = 4.dp
        val Medium: Dp = 8.dp
        val High: Dp = 16.dp
        val GlassFloat: Dp = 12.dp
    }

    object Springs {
        val Bouncy = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        val Gentle = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessLow
        )
        val Snappy = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
        val PressScaleDown = 0.94f
    }
}
