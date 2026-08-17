package com.sonza.music.data.source

import com.sonza.music.core.model.AudioCodec
import com.sonza.music.core.model.AudioQuality
import com.sonza.music.core.model.LoudnessMetadata
import com.sonza.music.core.model.Lyrics
import com.sonza.music.core.model.LyricsLine
import com.sonza.music.core.model.LyricsWord
import com.sonza.music.core.model.Track

object DemoAudiophileSource {

    val SAMPLE_TRACKS = listOf(
        Track(
            id = "sonza_demo_01",
            title = "Midnight Horizon (Audiophile Master)",
            artist = "Aura Resonance",
            artistId = "artist_aura_res",
            album = "Dimensions in High Fidelity",
            albumId = "album_dim_hi_fi",
            durationMs = 284000L, // 4:44
            mediaUri = "https://storage.googleapis.com/exoplayer-test-media-1/gen-3/screens/dash-vod-single-segment/audio-1080.mp4",
            artworkUri = "https://images.unsplash.com/photo-1614613535308-eb5fbd3d2c17?w=800&auto=format&fit=crop&q=80",
            trackNumber = 1,
            discNumber = 1,
            year = 2026,
            genre = "Electronic / Ambient",
            quality = AudioQuality(
                codec = AudioCodec.FLAC,
                bitDepth = 24,
                sampleRateHz = 96000,
                bitRateKbps = 3200,
                isLossless = true,
                channelCount = 2
            ),
            loudness = LoudnessMetadata(
                trackGainDb = -1.2f,
                trackPeak = 0.98f,
                albumGainDb = -1.5f,
                albumPeak = 0.99f
            ),
            isFavorite = true,
            sourceProvider = "SONZA_HIRES"
        ),
        Track(
            id = "sonza_demo_02",
            title = "Starlight Symphony in D Minor",
            artist = "Vienna Chamber Collective",
            artistId = "artist_vienna_cc",
            album = "Acoustic Spaces & Dynamic Range",
            albumId = "album_acoustics_dr",
            durationMs = 352000L, // 5:52
            mediaUri = "https://storage.googleapis.com/exoplayer-test-media-1/mkv/android-screens-seeds.mkv",
            artworkUri = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800&auto=format&fit=crop&q=80",
            trackNumber = 2,
            discNumber = 1,
            year = 2026,
            genre = "Classical",
            quality = AudioQuality(
                codec = AudioCodec.FLAC,
                bitDepth = 24,
                sampleRateHz = 192000,
                bitRateKbps = 4800,
                isLossless = true,
                channelCount = 2
            ),
            loudness = LoudnessMetadata(
                trackGainDb = -0.5f,
                trackPeak = 0.92f,
                albumGainDb = -0.8f,
                albumPeak = 0.94f
            ),
            isFavorite = false,
            sourceProvider = "SONZA_HIRES"
        ),
        Track(
            id = "sonza_demo_03",
            title = "Velvet Nights",
            artist = "Miles Thorne Quartet",
            artistId = "artist_miles_t",
            album = "Late Night Session Recordings",
            albumId = "album_late_night_rec",
            durationMs = 245000L, // 4:05
            mediaUri = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3",
            artworkUri = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=800&auto=format&fit=crop&q=80",
            trackNumber = 3,
            discNumber = 1,
            year = 2026,
            genre = "Jazz",
            quality = AudioQuality(
                codec = AudioCodec.FLAC,
                bitDepth = 24,
                sampleRateHz = 96000,
                bitRateKbps = 3100,
                isLossless = true,
                channelCount = 2
            ),
            loudness = LoudnessMetadata(
                trackGainDb = -2.1f,
                trackPeak = 0.95f,
                albumGainDb = -1.8f,
                albumPeak = 0.96f
            ),
            isFavorite = true,
            sourceProvider = "SONZA_HIRES"
        ),
        Track(
            id = "sonza_demo_04",
            title = "Quantum Echoes",
            artist = "Hyperion Synthesis",
            artistId = "artist_hyperion",
            album = "Neural Soundscapes",
            albumId = "album_neural_sound",
            durationMs = 218000L, // 3:38
            mediaUri = "https://storage.googleapis.com/exoplayer-test-media-1/mp4/dizzy-with-tx3g.mp4",
            artworkUri = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800&auto=format&fit=crop&q=80",
            trackNumber = 4,
            discNumber = 1,
            year = 2026,
            genre = "Electronic",
            quality = AudioQuality(
                codec = AudioCodec.FLAC,
                bitDepth = 24,
                sampleRateHz = 96000,
                bitRateKbps = 3300,
                isLossless = true,
                channelCount = 2
            ),
            loudness = LoudnessMetadata(
                trackGainDb = -3.0f,
                trackPeak = 0.99f,
                albumGainDb = -2.5f,
                albumPeak = 0.99f
            ),
            isFavorite = false,
            sourceProvider = "SONZA_HIRES"
        )
    )

    fun getSampleLyrics(trackId: String): Lyrics {
        return Lyrics(
            trackId = trackId,
            isSynced = true,
            hasWordSync = true,
            lines = listOf(
                LyricsLine(
                    text = "Close your eyes and let the sound begin",
                    startMs = 2000L,
                    endMs = 6500L,
                    words = listOf(
                        LyricsWord("Close", 2000L, 2600L),
                        LyricsWord("your", 2600L, 2900L),
                        LyricsWord("eyes", 2900L, 3600L),
                        LyricsWord("and", 3600L, 3900L),
                        LyricsWord("let", 3900L, 4300L),
                        LyricsWord("the", 4300L, 4600L),
                        LyricsWord("sound", 4600L, 5400L),
                        LyricsWord("begin", 5400L, 6500L)
                    )
                ),
                LyricsLine(
                    text = "Every frequency vibrating within",
                    startMs = 7000L,
                    endMs = 12000L,
                    words = listOf(
                        LyricsWord("Every", 7000L, 7600L),
                        LyricsWord("frequency", 7600L, 8800L),
                        LyricsWord("vibrating", 8800L, 10200L),
                        LyricsWord("within", 10200L, 12000L)
                    )
                ),
                LyricsLine(
                    text = "Hear music differently in high resolution",
                    startMs = 12500L,
                    endMs = 18000L,
                    words = listOf(
                        LyricsWord("Hear", 12500L, 13100L),
                        LyricsWord("music", 13100L, 13800L),
                        LyricsWord("differently", 13800L, 15400L),
                        LyricsWord("in", 15400L, 15800L),
                        LyricsWord("high", 15800L, 16500L),
                        LyricsWord("resolution", 16500L, 18000L)
                    )
                ),
                LyricsLine(
                    text = "A crystal clear sonic revolution",
                    startMs = 18500L,
                    endMs = 24000L,
                    words = listOf(
                        LyricsWord("A", 18500L, 18800L),
                        LyricsWord("crystal", 18800L, 19800L),
                        LyricsWord("clear", 19800L, 20600L),
                        LyricsWord("sonic", 20600L, 21800L),
                        LyricsWord("revolution", 21800L, 24000L)
                    )
                ),
                LyricsLine(
                    text = "Feel the sub-bass pulse beneath your feet",
                    startMs = 24500L,
                    endMs = 30000L,
                    words = listOf(
                        LyricsWord("Feel", 24500L, 25200L),
                        LyricsWord("the", 25200L, 25500L),
                        LyricsWord("sub-bass", 25500L, 26900L),
                        LyricsWord("pulse", 26900L, 27800L),
                        LyricsWord("beneath", 27800L, 28800L),
                        LyricsWord("your", 28800L, 29200L),
                        LyricsWord("feet", 29200L, 30000L)
                    )
                ),
                LyricsLine(
                    text = "Every transient and beat complete",
                    startMs = 30500L,
                    endMs = 37000L,
                    words = listOf(
                        LyricsWord("Every", 30500L, 31200L),
                        LyricsWord("transient", 31200L, 32800L),
                        LyricsWord("and", 32800L, 33200L),
                        LyricsWord("beat", 33200L, 34200L),
                        LyricsWord("complete", 34200L, 37000L)
                    )
                )
            )
        )
    }
}
