package com.sonza.music.test

import com.sonza.music.audio.effects.CrossfadeController
import com.sonza.music.audio.effects.ReplayGainNormalizer
import com.sonza.music.audio.equalizer.SonzaEqualizer
import com.sonza.music.core.model.EqualizerPreset
import com.sonza.music.core.model.LoudnessMetadata
import com.sonza.music.core.model.VolumeNormalizationMode
import com.sonza.music.data.repository.LyricsRepositoryImpl
import com.sonza.music.data.spotify.SpotifyImportTrack
import com.sonza.music.data.spotify.SpotifyPlaylistImporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SonzaCoreAlgorithmsTest {

    @Test
    fun testLrcParserWithWordTimestamps() {
        val rawLrc = """
            [00:02.50]Close your eyes and listen
            [00:07.10]<00:07.10>Every <00:08.20>beat <00:09.50>matters
            [00:15.00]Hear music differently
        """.trimIndent()

        val repo = LyricsRepositoryImpl(lyricsDao = null as? Any as com.sonza.music.core.database.LyricsDao)
        val lyrics = repo.parseLrcContent("test_track", rawLrc)

        assertTrue(lyrics.isSynced)
        assertEquals(3, lyrics.lines.size)
        assertEquals(2500L, lyrics.lines[0].startMs)
        assertEquals(7100L, lyrics.lines[0].endMs) // End adjusted to next line start
        assertEquals("Close your eyes and listen", lyrics.lines[0].text)

        // Test word-level sync extraction
        val line2 = lyrics.lines[1]
        assertTrue(line2.hasWordSync)
        assertEquals(3, line2.words.size)
        assertEquals("Every", line2.words[0].word.trim())
        assertEquals(7100L, line2.words[0].startMs)

        // Test active line locator
        assertEquals(0, lyrics.getActiveLineIndex(3000L))
        assertEquals(1, lyrics.getActiveLineIndex(8000L))
        assertEquals(2, lyrics.getActiveLineIndex(16000L))
    }

    @Test
    fun testEqualizerBandsAndLinearGain() {
        val eq = SonzaEqualizer()
        assertEquals(10, EqualizerPreset.FREQUENCY_BANDS.size)
        assertEquals(31, EqualizerPreset.FREQUENCY_BANDS[0].centerFreqHz)
        assertEquals(16000, EqualizerPreset.FREQUENCY_BANDS[9].centerFreqHz)

        // Test 0dB gain -> 1.0 multiplier
        val gain0 = eq.calculateLinearGain(0.0f)
        assertEquals(1.0f, gain0, 0.001f)

        // Test +6dB gain -> ~2.0 multiplier
        val gain6 = eq.calculateLinearGain(6.0f)
        assertTrue(gain6 in 1.98f..2.02f)

        // Test -6dB gain -> ~0.5 multiplier
        val gainMinus6 = eq.calculateLinearGain(-6.0f)
        assertTrue(gainMinus6 in 0.49f..0.51f)
    }

    @Test
    fun testReplayGainNormalizerAndAntiClipping() {
        val metadata = LoudnessMetadata(
            trackGainDb = -3.0f,
            trackPeak = 0.95f,
            albumGainDb = -2.0f,
            albumPeak = 0.90f
        )

        // Track Gain mode
        val scaleTrack = ReplayGainNormalizer.calculateVolumeScale(
            VolumeNormalizationMode.TRACK_GAIN,
            metadata
        )
        assertTrue(scaleTrack in 0.70f..0.72f) // 10^(-3/20) ~ 0.7079

        // Anti-clipping test: positive gain with high peak
        val highPeakMeta = LoudnessMetadata(trackGainDb = +6.0f, trackPeak = 0.90f)
        val scaleClipped = ReplayGainNormalizer.calculateVolumeScale(
            VolumeNormalizationMode.TRACK_GAIN,
            highPeakMeta
        )
        // With +6dB gain (2.0x) * 0.90 peak = 1.8 (>1.0), so limiter scales to 1.0 / 0.90 = 1.111
        assertTrue(scaleClipped in 1.10f..1.12f)
    }

    @Test
    fun testSpotifyPlaylistFuzzyMatcher() {
        val sampleTracks = listOf(
            com.sonza.music.core.model.Track(
                id = "track_1",
                title = "Midnight Horizon",
                artist = "Aura Resonance",
                album = "Dimensions",
                durationMs = 240000L,
                mediaUri = ""
            ),
            com.sonza.music.core.model.Track(
                id = "track_2",
                title = "Velvet Nights",
                artist = "Miles Thorne Quartet",
                album = "Sessions",
                durationMs = 180000L,
                mediaUri = ""
            )
        )

        val imported = listOf(
            SpotifyImportTrack("Midnight Horizon (Original Mix)", "Aura Resonance"),
            SpotifyImportTrack("Velvet Nights", "Miles Thorne Quartet"),
            SpotifyImportTrack("Completely Unmatched Song", "Unknown Artist")
        )

        val report = SpotifyPlaylistImporter.matchImportedTracks(imported, sampleTracks)

        assertEquals(3, report.totalTracksCount)
        assertEquals(2, report.matchedCount)
        assertEquals(1, report.unresolvedCount)
        assertTrue(report.results[0].isResolved)
        assertTrue(report.results[1].isResolved)
        assertFalse(report.results[2].isResolved)
    }

    @Test
    fun testConstantPowerCrossfade() {
        // At start (0.0): Outgoing = 1.0, Incoming = 0.0
        val (out0, in0) = CrossfadeController.calculateVolumes(0.0f)
        assertEquals(1.0f, out0, 0.001f)
        assertEquals(0.0f, in0, 0.001f)

        // At midpoint (0.5): cos(pi/4) = sin(pi/4) ~ 0.7071 (Sum of squares = 1.0, preserving acoustic energy)
        val (outHalf, inHalf) = CrossfadeController.calculateVolumes(0.5f)
        assertEquals(outHalf, inHalf, 0.001f)
        val totalPower = (outHalf * outHalf) + (inHalf * inHalf)
        assertEquals(1.0f, totalPower, 0.001f)

        // At end (1.0): Outgoing = 0.0, Incoming = 1.0
        val (out1, in1) = CrossfadeController.calculateVolumes(1.0f)
        assertEquals(0.0f, out1, 0.001f)
        assertEquals(1.0f, in1, 0.001f)
    }
}
