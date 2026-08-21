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
    fun testBluntGreetingsWithoutUserName() {
        // Morning (5..11)
        assertEquals("Good morning ☀️", HomeGreetingHelper.getGreetingText(userName = null, hour = 8))
        assertEquals("Good morning ☀️", HomeGreetingHelper.getGreetingText(userName = "", hour = 8))
        assertEquals("Good morning ☀️", HomeGreetingHelper.getGreetingText(userName = "   ", hour = 8))

        // Afternoon (12..16)
        assertEquals("Good afternoon 🌤️", HomeGreetingHelper.getGreetingText(userName = null, hour = 14))

        // Evening (17..21)
        assertEquals("Good evening 🌆", HomeGreetingHelper.getGreetingText(userName = null, hour = 19))

        // Night (22..4)
        assertEquals("Good night 🌙", HomeGreetingHelper.getGreetingText(userName = null, hour = 23))
        assertEquals("Good night 🌙", HomeGreetingHelper.getGreetingText(userName = null, hour = 2))
    }

    @Test
    fun testBluntGreetingsWithUserName() {
        // Morning (5..11)
        assertEquals("Good morning, Naruto ☀️", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 9))
        assertEquals("Good morning, Naruto ☀️", HomeGreetingHelper.getGreetingText(userName = " Naruto  ", hour = 9))

        // Afternoon (12..16)
        assertEquals("Good afternoon, Naruto 🌤️", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 13))

        // Evening (17..21)
        assertEquals("Good evening, Naruto 🌆", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 18))

        // Night (22..4)
        assertEquals("Good night, Naruto 🌙", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 23))
        assertEquals("Good night, Naruto 🌙", HomeGreetingHelper.getGreetingText(userName = "Naruto", hour = 1))
    }

    @Test
    fun testMoodGreetingsWithUserName() {
        val romanticSong = createSong("1", "Romantic Love Song", "Artist")

        assertEquals(
            "Good morning, Naruto ☀️ Feeling romantic today? ❤️",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = romanticSong, hour = 8)
        )
        assertEquals(
            "Good afternoon, Naruto 🌤️ Love is in the air ❤️",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = romanticSong, hour = 14)
        )
        assertEquals(
            "Good evening, Naruto 🌆 In a romantic mood? ❤️",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = romanticSong, hour = 19)
        )
        assertEquals(
            "Good night, Naruto 🌙 One more love song? ❤️",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = romanticSong, hour = 23)
        )

        val sadSong = createSong("2", "Sad Broken Tears", "Artist")
        assertEquals(
            "Good evening, Naruto 🌆 Feeling a little emotional? 💙",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = sadSong, hour = 20)
        )
        assertEquals(
            "Good night, Naruto 🌙 Some feelings need music 🎧",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = sadSong, hour = 23)
        )

        val energeticSong = createSong("3", "Gym Workout Party Bass", "Artist")
        assertEquals(
            "Good evening, Naruto 🌆 Ready to turn it up? 🔥",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = energeticSong, hour = 18)
        )
        assertEquals(
            "Good night, Naruto 🌙 Still got energy? ⚡",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = energeticSong, hour = 23)
        )

        val chillSong = createSong("4", "Lofi Chill Sunset Rain", "Artist")
        assertEquals(
            "Good evening, Naruto 🌆 Just vibing? 🌙",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = chillSong, hour = 20)
        )
        assertEquals(
            "Good night, Naruto 🌙 Time to relax 🎧",
            HomeGreetingHelper.getGreetingText(userName = "Naruto", currentSong = chillSong, hour = 23)
        )
    }

    @Test
    fun testPositionalCompatibilityOverload() {
        val romanticSong = createSong("1", "Romantic Love Song", "Artist")
        assertEquals(
            "Good morning ☀️ Feeling romantic today? ❤️",
            HomeGreetingHelper.getGreetingText(romanticSong, null, 8)
        )
        assertEquals(
            "Good evening 🌆",
            HomeGreetingHelper.getGreetingText(null, null, 19)
        )
    }
}
