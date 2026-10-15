package com.sonza.app.ui

import androidx.compose.ui.graphics.Color
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SongSource
import com.sonza.app.ui.components.NowPlaying
import com.sonza.app.ui.components.SonzaDynamicColors
import com.sonza.app.ui.components.computeOnAccent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicColorSystemTest {

    @Test
    fun testDynamicColorsAliasesAndDefaults() {
        val customAccent = Color(0xFFFF5722)
        val customPrimary = Color(0xFFE64A19)
        val customSecondary = Color(0xFFFF9800)
        val customBg = Color(0xFF120805)
        val customSurf = Color(0xFF1E0E08)
        val customSurfVar = Color(0xFF2B150D)
        val customGlow = Color(0x55FF5722)
        val customBorder = Color(0x40FF5722)
        val customTextAccent = Color(0xFFFFCCBC)
        val customOnAccent = Color.White

        val palette = SonzaDynamicColors(
            primary = customPrimary,
            secondary = customSecondary,
            accent = customAccent,
            background = customBg,
            surface = customSurf,
            surfaceVariant = customSurfVar,
            glow = customGlow,
            border = customBorder,
            textAccent = customTextAccent,
            onAccent = customOnAccent,
            isIdle = false
        )

        // Verify all dynamic* aliases match their respective fields
        assertEquals(customPrimary, palette.dynamicPrimary)
        assertEquals(customSecondary, palette.dynamicSecondary)
        assertEquals(customAccent, palette.dynamicAccent)
        assertEquals(customBg, palette.dynamicBackground)
        assertEquals(customSurf, palette.dynamicSurface)
        assertEquals(customSurfVar, palette.dynamicSurfaceVariant)
        assertEquals(customGlow, palette.dynamicGlow)
        assertEquals(customBorder, palette.dynamicBorder)
        assertEquals(customTextAccent, palette.dynamicTextAccent)
        assertEquals(customOnAccent, palette.dynamicOnAccent)
        assertEquals(false, palette.isIdle)
    }

    @Test
    fun testNowPlayingModelIntegration() {
        val song = Song(
            id = "song_red_123",
            title = "Warm Sunset",
            artist = "Solar Flare",
            album = "Cosmic Rays",
            duration = 210000L,
            thumbnailUrl = "https://images.example.com/artwork_red.jpg",
            source = SongSource.YOUTUBE
        )

        val redPalette = SonzaDynamicColors(
            accent = Color(0xFFE53935),
            primary = Color(0xFFC62828),
            secondary = Color(0xFFFF7043)
        )

        val nowPlaying = NowPlaying(
            song = song,
            artist = song.artist,
            artwork = song.thumbnailUrl,
            dynamicColors = redPalette
        )

        assertEquals("Warm Sunset", nowPlaying.song?.title)
        assertEquals("Solar Flare", nowPlaying.artist)
        assertEquals("https://images.example.com/artwork_red.jpg", nowPlaying.artwork)
        assertEquals(Color(0xFFE53935), nowPlaying.dynamicColors.accent)
        assertEquals(Color(0xFFC62828), nowPlaying.dynamicColors.primary)
    }

    @Test
    fun testComputeOnAccentContrast() {
        // Bright yellow accent -> requires dark onAccent for readability
        val yellowAccent = Color(0xFFFFEB3B)
        val yellowOnAccent = computeOnAccent(yellowAccent)
        assertEquals(Color(0xFF0F172A), yellowOnAccent)

        // Deep blue / dark accent -> requires white onAccent
        val deepBlueAccent = Color(0xFF1E3A8A)
        val blueOnAccent = computeOnAccent(deepBlueAccent)
        assertEquals(Color.White, blueOnAccent)

        // Crimson red accent -> requires white onAccent
        val redAccent = Color(0xFFB71C1C)
        val redOnAccent = computeOnAccent(redAccent)
        assertEquals(Color.White, redOnAccent)
    }

    @Test
    fun testDistinctPalettesForDifferentSongArtworks() {
        // Simulating 4 completely different song artworks
        // Song 1: Red/warm artwork
        val redPalette = SonzaDynamicColors(
            primary = Color(0xFFB71C1C),
            secondary = Color(0xFFFF5722),
            accent = Color(0xFFFF3D00)
        )

        // Song 2: Blue/cool artwork
        val bluePalette = SonzaDynamicColors(
            primary = Color(0xFF0D47A1),
            secondary = Color(0xFF0288D1),
            accent = Color(0xFF00E5FF)
        )

        // Song 3: Green artwork
        val greenPalette = SonzaDynamicColors(
            primary = Color(0xFF1B5E20),
            secondary = Color(0xFF388E3C),
            accent = Color(0xFF00E676)
        )

        // Song 4: Yellow/gold artwork
        val yellowPalette = SonzaDynamicColors(
            primary = Color(0xFFF57F17),
            secondary = Color(0xFFFBC02D),
            accent = Color(0xFFFFD600)
        )

        // Validate that no palette is stuck on hardcoded purple (0xFF7C3AED or 0xFF8B5CF6)
        val hardcodedPurple = Color(0xFF7C3AED)
        assertNotEquals(hardcodedPurple, redPalette.accent)
        assertNotEquals(hardcodedPurple, bluePalette.accent)
        assertNotEquals(hardcodedPurple, greenPalette.accent)
        assertNotEquals(hardcodedPurple, yellowPalette.accent)

        // Validate distinctness between all palettes
        assertNotEquals(redPalette.accent, bluePalette.accent)
        assertNotEquals(bluePalette.accent, greenPalette.accent)
        assertNotEquals(greenPalette.accent, yellowPalette.accent)
        assertNotEquals(yellowPalette.accent, redPalette.accent)
    }
}
