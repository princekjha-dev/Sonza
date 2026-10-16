package com.sonza.app.ai.orbit

import com.sonza.app.ai.orbit.provider.GroqProxyProvider
import com.sonza.app.ai.orbit.provider.LocalAIProvider
import com.sonza.app.ai.orbit.provider.OrbitAIProviderManager
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SongSource
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class OrbitAIProviderTest {

    private lateinit var localProvider: LocalAIProvider
    private lateinit var sampleSong: Song
    private lateinit var musicContext: OrbitMusicContext

    @Before
    fun setUp() {
        localProvider = LocalAIProvider()
        sampleSong = Song(
            id = "test_unholy",
            title = "Unholy",
            artist = "Sam Smith and Kim Petras",
            album = "Gloria",
            duration = 156000L,
            thumbnailUrl = "https://example.com/unholy.jpg",
            source = SongSource.YOUTUBE
        )
        musicContext = OrbitMusicContext(
            title = sampleSong.title,
            artist = sampleSong.artist,
            album = sampleSong.album,
            durationMs = sampleSong.duration,
            currentPositionMs = 45000L,
            isPlaying = true,
            source = "youtube"
        )
    }

    @Test
    fun testLocalAIProvider_MusicContextAwareness() = runBlocking {
        // "Who sings this?"
        val whoSingsResp = localProvider.processChat("Who sings this?", musicContext, emptyList())
        assertTrue(whoSingsResp.isSuccess)
        assertTrue(whoSingsResp.spokenText.contains("Unholy", ignoreCase = true))
        assertTrue(whoSingsResp.spokenText.contains("Sam Smith", ignoreCase = true))

        // "What song is this?"
        val whatSongResp = localProvider.processChat("What song is this?", musicContext, emptyList())
        assertTrue(whatSongResp.isSuccess)
        assertTrue(whatSongResp.spokenText.contains("Unholy", ignoreCase = true))

        // "What does it mean?"
        val meaningResp = localProvider.processChat("What does it mean?", musicContext, emptyList())
        assertTrue(meaningResp.isSuccess)
        assertTrue(meaningResp.spokenText.contains("Unholy", ignoreCase = true))
    }

    @Test
    fun testLocalAIProvider_ToolCallGeneration() = runBlocking {
        // "Give me similar songs"
        val similarResp = localProvider.processChat("Give me similar songs", musicContext, emptyList())
        assertEquals(1, similarResp.toolCalls.size)
        assertEquals("play_music", similarResp.toolCalls[0].tool)
        assertEquals(true, similarResp.toolCalls[0].arguments["isSimilar"])

        // "Pause"
        val pauseResp = localProvider.processChat("pause", musicContext, emptyList())
        assertEquals(1, pauseResp.toolCalls.size)
        assertEquals("playback_control", pauseResp.toolCalls[0].tool)
        assertEquals("pause", pauseResp.toolCalls[0].arguments["action"])

        // "Make it louder"
        val louderResp = localProvider.processChat("volume up", musicContext, emptyList())
        assertEquals(1, louderResp.toolCalls.size)
        assertEquals("set_volume", louderResp.toolCalls[0].tool)
        assertEquals("up", louderResp.toolCalls[0].arguments["action"])

        // "Open Spotify"
        val appResp = localProvider.processChat("open Spotify", musicContext, emptyList())
        assertEquals(1, appResp.toolCalls.size)
        assertEquals("open_app", appResp.toolCalls[0].tool)
        assertEquals("spotify", (appResp.toolCalls[0].arguments["appName"] as String).lowercase())

        // "Call Mom"
        val phoneResp = localProvider.processChat("call Mom", musicContext, emptyList())
        assertEquals(1, phoneResp.toolCalls.size)
        assertEquals("call_phone", phoneResp.toolCalls[0].tool)
        assertEquals("mom", (phoneResp.toolCalls[0].arguments["contact"] as String).lowercase())

        // "Search the web for Grammy winners"
        val searchResp = localProvider.processChat("search the web for Grammy winners", musicContext, emptyList())
        assertEquals(1, searchResp.toolCalls.size)
        assertEquals("web_search", searchResp.toolCalls[0].tool)
        assertEquals("grammy winners", (searchResp.toolCalls[0].arguments["query"] as String).lowercase())
    }

    @Test
    fun testProviderManager_FallbackToLocalWhenProxyUnconfigured() = runBlocking {
        val manager = OrbitAIProviderManager(
            okHttpClient = OkHttpClient(),
            providerTypeProvider = { "GROQ" },
            proxyUrlProvider = { "" },
            proxyTokenProvider = { "" }
        )
        val response = manager.processChat("Who sings this?", musicContext, emptyList())

        // Should gracefully fall back to Local AI and answer successfully!
        assertTrue(response.isSuccess)
        assertTrue(response.spokenText.contains("Sam Smith", ignoreCase = true))
    }

    @Test
    fun testGroqProxyProvider_SafeHandlingWhenUnconfigured() = runBlocking {
        val groqProvider = GroqProxyProvider(
            baseHttpClient = OkHttpClient(),
            proxyUrlProvider = { "" },
            proxyTokenProvider = { "" }
        )
        val resp = groqProvider.processChat("Who sang Unholy?", musicContext, emptyList())

        assertFalse(resp.isSuccess)
        // Never leaks secrets, stack traces, or internal tokens:
        assertTrue(
            resp.spokenText.contains("reach the AI proxy", ignoreCase = true) ||
            resp.spokenText.contains("trouble connecting", ignoreCase = true) ||
            resp.spokenText.contains("temporarily unavailable", ignoreCase = true) ||
            resp.spokenText.contains("not configured", ignoreCase = true)
        )
        assertFalse(resp.spokenText.contains("gsk_"))
        assertFalse(resp.spokenText.contains("401"))
    }
}
