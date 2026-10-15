package com.sonza.app.glance

import androidx.compose.ui.graphics.Color

/**
 * Cohesive visual design system for all Sonza home screen widgets.
 * Features dark obsidian glass cards, high-contrast typography,
 * refined accent glows, and accessible touch target standards.
 */
object SonzaWidgetTheme {
    val CardBackground = Color(0xFF11121A)
    val CardSurface = Color(0xFF1B1C28)
    val CardSurfaceElevated = Color(0xFF222433)
    val CardBorder = Color(0xFFFFFFFF).copy(alpha = 0.08f)

    // Accents
    val AccentCyan = Color(0xFF38BDF8)
    val AccentBlue = Color(0xFF3B82F6)
    val AccentPink = Color(0xFFFF4081)
    val AccentRed = Color(0xFFFF5252)
    val AccentOrange = Color(0xFFFF9100)
    val AccentPurple = Color(0xFFB388FF)
    val AccentGreen = Color(0xFF10B981)

    // Typography colors
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFFFFFFF).copy(alpha = 0.72f)
    val TextMuted = Color(0xFFFFFFFF).copy(alpha = 0.45f)

    // Controls
    val ControlPill = Color(0xFFFFFFFF).copy(alpha = 0.10f)
    val ControlPillHover = Color(0xFFFFFFFF).copy(alpha = 0.18f)
    val PrimaryButtonFill = Color(0xFFFFFFFF)
    val PrimaryButtonIcon = Color(0xFF0F1017)

    fun computeCardBackground(dominantColor: Int): Color {
        return if (dominantColor != -16777216 && dominantColor != 0) {
            Color(dominantColor).copy(alpha = 0.88f)
        } else {
            CardBackground.copy(alpha = 0.96f)
        }
    }

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "0:00"
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(java.util.Locale.US, "%d:%02d", minutes, seconds)
    }
}
