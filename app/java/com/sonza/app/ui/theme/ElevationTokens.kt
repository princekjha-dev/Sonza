package com.sonza.app.ui.theme

import androidx.compose.ui.unit.dp

/**
 * M3 elevation scale — use these instead of raw .dp for surface elevation.
 * Level0  — flush surfaces
 * Level1  — resting cards
 * Level2  — hovered/focused cards
 * Level3  — pressed cards, menus
 * Level4  — navigation bars
 * Level5  — modal sheets, dialogs
 */
object ElevationTokens {
    val Level0 = 0.dp
    val Level1 = 1.dp
    val Level2 = 3.dp
    val Level3 = 6.dp
    val Level4 = 8.dp
    val Level5 = 12.dp

    // Sonza Design System elevation scale (Part 5)
    val Elevation0 = 0.dp  // Base screen background
    val Elevation1 = 1.dp  // Standard cards
    val Elevation2 = 3.dp  // Bottom nav, mini-player (with background blur)
    val Elevation3 = 6.dp  // Modals, expanded Now Playing sheet

    val StandardBlurRadius = 12.dp // 12dp radial blur
}

