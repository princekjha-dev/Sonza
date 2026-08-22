package com.sonza.app.ui.utils

import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SongSource
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeGreetingHelperTest {

    private fun createSong(id: String, title: String, artist: String): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = "Test Album",
            duration = 180000L,
            thumbnailUrl = null,
            source = SongSource.YOUTUBE
        )
    }

    @Test
    fun testGreetingsWithoutUserName() {
        // Morning (5..11)
        assertEquals("Good morning 🌅", HomeGreetingHelper.getGreetingText(userName = null, hour = 8))
        assertEquals("Good morning 🌅", HomeGreetingHelper.getGreetingText(userName = "", hour = 8))
        assertEquals("Good morning 🌅", HomeGreetingHelper.getGreetingText(userName = "   ", hour = 8))

        // Afternoon (12..16)
        assertEquals("Good afternoon 🌤️", HomeGreetingHelper.getGreetingText(userName = null, hour = 14))

        // Evening (17..21)
        assertEquals("Good evening 🌆", HomeGreetingHelper.getGreetingText(userName = null, hour = 19))

        // Night (22..4)
        assertEquals("Good night 🌙", HomeGreetingHelper.getGreetingText(userName = null, hour = 23))
        assertEquals("Good night 🌙", HomeGreetingHelper.getGreetingText(userName = null, hour = 2))
    }

    @Test
    fun testGreetingsWithUserName() {
        // Morning (5..11)
        assertEquals("Good morning, Naruto 🌅", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 9))
        assertEquals("Good morning, Naruto 🌅", HomeGreetingHelper.getGreetingText(userName = " Naruto  ", hour = 9))

        // Afternoon (12..16)
        assertEquals("Good afternoon, Naruto 🌤️", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 13))

        // Evening (17..21)
        assertEquals("Good evening, Naruto 🌆", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 18))

        // Night (22..4)
        assertEquals("Good night, Naruto 🌙", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 23))
        assertEquals("Good night, Naruto 🌙", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 1))
    }

    @Test
    fun testPositionalCompatibilityOverload() {
        val song = createSong("1", "Romantic Love Song", "Artist")
        assertEquals(
            "Good morning 🌅",
            HomeGreetingHelper.getGreetingText(song, null, 8)
        )
        assertEquals(
            "Good evening 🌆",
            HomeGreetingHelper.getGreetingText(null, null, 19)
        )
    }
}
