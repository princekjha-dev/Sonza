package com.sonza.app.recommendation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SeasonalSectionHelperTest {

    @Test
    fun testMonthToSeasonMappingForAllTwelveMonths() {
        // Monsoon: June–September (Calendar.JUNE = 5, JULY = 6, AUGUST = 7, SEPTEMBER = 8)
        assertEquals(Season.MONSOON, SeasonalSectionHelper.getSeasonForMonth(Calendar.JUNE))
        assertEquals(Season.MONSOON, SeasonalSectionHelper.getSeasonForMonth(Calendar.JULY))
        assertEquals(Season.MONSOON, SeasonalSectionHelper.getSeasonForMonth(Calendar.AUGUST))
        assertEquals(Season.MONSOON, SeasonalSectionHelper.getSeasonForMonth(Calendar.SEPTEMBER))

        // Autumn: October–November (Calendar.OCTOBER = 9, NOVEMBER = 10)
        assertEquals(Season.AUTUMN, SeasonalSectionHelper.getSeasonForMonth(Calendar.OCTOBER))
        assertEquals(Season.AUTUMN, SeasonalSectionHelper.getSeasonForMonth(Calendar.NOVEMBER))

        // Winter: December–February (Calendar.DECEMBER = 11, JANUARY = 0, FEBRUARY = 1)
        assertEquals(Season.WINTER, SeasonalSectionHelper.getSeasonForMonth(Calendar.DECEMBER))
        assertEquals(Season.WINTER, SeasonalSectionHelper.getSeasonForMonth(Calendar.JANUARY))
        assertEquals(Season.WINTER, SeasonalSectionHelper.getSeasonForMonth(Calendar.FEBRUARY))

        // Spring: March–April (Calendar.MARCH = 2, APRIL = 3)
        assertEquals(Season.SPRING, SeasonalSectionHelper.getSeasonForMonth(Calendar.MARCH))
        assertEquals(Season.SPRING, SeasonalSectionHelper.getSeasonForMonth(Calendar.APRIL))

        // Summer: May (Calendar.MAY = 4)
        assertEquals(Season.SUMMER, SeasonalSectionHelper.getSeasonForMonth(Calendar.MAY))
    }

    @Test
    fun testSeasonalTitlesContainSpecifiedEmojiTitles() {
        val monsoonConfig = SeasonalSectionHelper.getSeasonConfig(Season.MONSOON)
        assertEquals(listOf("Rain Therapy ☘️🌧️"), monsoonConfig.titles)

        val autumnConfig = SeasonalSectionHelper.getSeasonConfig(Season.AUTUMN)
        assertEquals(listOf("Autumn Vibes 🍂☕"), autumnConfig.titles)

        val winterConfig = SeasonalSectionHelper.getSeasonConfig(Season.WINTER)
        assertEquals(listOf("Winter Nights ❄️🌙"), winterConfig.titles)

        val springConfig = SeasonalSectionHelper.getSeasonConfig(Season.SPRING)
        assertEquals(listOf("Spring Vibes 🌸🌿"), springConfig.titles)

        val summerConfig = SeasonalSectionHelper.getSeasonConfig(Season.SUMMER)
        assertEquals(listOf("Summer Vibes ☀️🎶"), summerConfig.titles)
    }

    @Test
    fun testSeasonalMoodDescriptors() {
        val monsoonConfig = SeasonalSectionHelper.getSeasonConfig(Season.MONSOON)
        assertTrue(monsoonConfig.moodDescriptors.contains("bollywood romance"))
        assertTrue(monsoonConfig.moodDescriptors.contains("80s/90s nostalgia"))
        assertTrue(monsoonConfig.moodDescriptors.contains("soft romantic"))
        assertTrue(monsoonConfig.moodDescriptors.contains("monsoon bollywood"))
        assertTrue(monsoonConfig.moodDescriptors.contains("emotional melodies"))
        assertTrue(monsoonConfig.moodDescriptors.contains("acoustic/chill"))
        assertTrue(monsoonConfig.moodDescriptors.contains("late-evening"))
        assertTrue(monsoonConfig.moodDescriptors.contains("nostalgic indian music"))

        val autumnConfig = SeasonalSectionHelper.getSeasonConfig(Season.AUTUMN)
        assertTrue(autumnConfig.moodDescriptors.contains("warm acoustic"))
        assertTrue(autumnConfig.moodDescriptors.contains("soft bollywood"))
        assertTrue(autumnConfig.moodDescriptors.contains("nostalgic"))
        assertTrue(autumnConfig.moodDescriptors.contains("romantic"))
        assertTrue(autumnConfig.moodDescriptors.contains("indie/chill"))
        assertTrue(autumnConfig.moodDescriptors.contains("mellow evening"))
        assertTrue(autumnConfig.moodDescriptors.contains("cozy listening"))
        assertTrue(autumnConfig.moodDescriptors.contains("golden-hour"))

        val winterConfig = SeasonalSectionHelper.getSeasonConfig(Season.WINTER)
        assertTrue(winterConfig.moodDescriptors.contains("slow romantic"))
        assertTrue(winterConfig.moodDescriptors.contains("bollywood classics"))
        assertTrue(winterConfig.moodDescriptors.contains("late-night"))
        assertTrue(winterConfig.moodDescriptors.contains("soft acoustic"))
        assertTrue(winterConfig.moodDescriptors.contains("lo-fi/chill"))
        assertTrue(winterConfig.moodDescriptors.contains("cozy"))
        assertTrue(winterConfig.moodDescriptors.contains("nostalgic"))
        assertTrue(winterConfig.moodDescriptors.contains("deep listening"))

        val springConfig = SeasonalSectionHelper.getSeasonConfig(Season.SPRING)
        assertTrue(springConfig.moodDescriptors.contains("feel-good"))
        assertTrue(springConfig.moodDescriptors.contains("fresh bollywood"))
        assertTrue(springConfig.moodDescriptors.contains("romantic"))
        assertTrue(springConfig.moodDescriptors.contains("indie pop"))
        assertTrue(springConfig.moodDescriptors.contains("uplifting"))
        assertTrue(springConfig.moodDescriptors.contains("light acoustic"))
        assertTrue(springConfig.moodDescriptors.contains("happy/chill"))
        assertTrue(springConfig.moodDescriptors.contains("new music"))

        val summerConfig = SeasonalSectionHelper.getSeasonConfig(Season.SUMMER)
        assertTrue(summerConfig.moodDescriptors.contains("bollywood hits"))
        assertTrue(summerConfig.moodDescriptors.contains("dance/pop"))
        assertTrue(summerConfig.moodDescriptors.contains("road-trip"))
        assertTrue(summerConfig.moodDescriptors.contains("energetic"))
        assertTrue(summerConfig.moodDescriptors.contains("feel-good"))
        assertTrue(summerConfig.moodDescriptors.contains("party"))
        assertTrue(summerConfig.moodDescriptors.contains("chill summer"))
        assertTrue(summerConfig.moodDescriptors.contains("popular current music"))
    }

    @Test
    fun testCalendarDrivenTitleSelection() {
        val cal = Calendar.getInstance()

        // Test in July (Monsoon)
        cal.set(2026, Calendar.JULY, 15)
        assertEquals("Rain Therapy ☘️🌧️", SeasonalSectionHelper.getSeasonalSectionTitle(cal))

        // Test in October (Autumn)
        cal.set(2026, Calendar.OCTOBER, 20)
        assertEquals("Autumn Vibes 🍂☕", SeasonalSectionHelper.getSeasonalSectionTitle(cal))

        // Test in January (Winter)
        cal.set(2026, Calendar.JANUARY, 10)
        assertEquals("Winter Nights ❄️🌙", SeasonalSectionHelper.getSeasonalSectionTitle(cal))

        // Test in April (Spring)
        cal.set(2026, Calendar.APRIL, 5)
        assertEquals("Spring Vibes 🌸🌿", SeasonalSectionHelper.getSeasonalSectionTitle(cal))

        // Test in May (Summer)
        cal.set(2026, Calendar.MAY, 18)
        assertEquals("Summer Vibes ☀️🎶", SeasonalSectionHelper.getSeasonalSectionTitle(cal))
    }

    @Test
    fun testSeasonalQueriesNonEmpty() {
        val cal = Calendar.getInstance()
        for (month in Calendar.JANUARY..Calendar.DECEMBER) {
            cal.set(Calendar.MONTH, month)
            val queries = SeasonalSectionHelper.getSeasonalQueries(cal)
            assertTrue("Queries for month $month should not be empty", queries.isNotEmpty())
        }
    }

    @Test
    fun testEnvironmentalSoundFilteringRejectsSoundRecordings() {
        // Monsoon sound recordings
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Rain Sounds 2 Hours", season = Season.MONSOON))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Thunder recordings for sleep", season = Season.MONSOON))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Meditation rain sound", season = Season.MONSOON))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("White noise rain sound effect", season = Season.MONSOON))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Heavy Rain on Tin Roof 10 Hours", season = Season.MONSOON))

        // Autumn sound recordings
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Falling-leaf sound effects", season = Season.AUTUMN))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Forest ambience nature sound", season = Season.AUTUMN))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Autumn forest leaves sound", season = Season.AUTUMN))

        // Winter sound recordings
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Winter Fireplace Sounds", season = Season.WINTER))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Fireplace sounds crackling fire", season = Season.WINTER))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Wind recordings blizzard sound", season = Season.WINTER))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Snow ambience howling wind", season = Season.WINTER))

        // Spring sound recordings
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Spring Bird Sounds", season = Season.SPRING))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Bird chirping forest ambience", season = Season.SPRING))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Nature sound recordings spring birds", season = Season.SPRING))

        // Summer sound recordings
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Summer Beach Sounds", season = Season.SUMMER))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Ocean sounds crashing waves", season = Season.SUMMER))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Beach ambience relaxing waves", season = Season.SUMMER))
        assertFalse(SeasonalSectionHelper.isEligibleSeasonalSong("Fan/AC sounds for sleep", season = Season.SUMMER))
    }

    @Test
    fun testEnvironmentalSoundFilteringAllowsRealMusicTracks() {
        // Genuine musical tracks and albums
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Bollywood Monsoon Romance", "Arijit Singh", season = Season.MONSOON))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Tip Tip Barsa Paani", "Alka Yagnik & Udit Narayan", season = Season.MONSOON))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Baarish", "Ash King", season = Season.MONSOON))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Rimjhim Gire Sawan", "Kishore Kumar", season = Season.MONSOON))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Rain Over Me", "Pitbull ft. Marc Anthony", season = Season.MONSOON))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("November Rain", "Guns N' Roses", season = Season.AUTUMN))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Cozy Winter Nights", "Various Artists", season = Season.WINTER))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Cold Mess", "Prateek Kuhad", season = Season.WINTER))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Fresh Spring Vibes", "Indie Artists", season = Season.SPRING))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Summer Road Trip Hits", "Various Artists", season = Season.SUMMER))
        assertTrue(SeasonalSectionHelper.isEligibleSeasonalSong("Kabira", "Arijit Singh", season = Season.AUTUMN))
    }

    @Test
    fun testSeasonalTitleDetectionForDuplicateProtection() {
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Rain Therapy ☘️🌧️"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Rain Therapy"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Monsoon Mood"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Autumn Vibes 🍂☕"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Autumn Vibes"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Winter Nights ❄️🌙"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Winter Nights"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Spring Vibes 🌸🌿"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Spring Vibes"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Summer Vibes ☀️🎶"))
        assertTrue(SeasonalSectionHelper.isSeasonalSectionTitle("Summer Vibes"))

        assertFalse(SeasonalSectionHelper.isSeasonalSectionTitle("Today's Hits"))
        assertFalse(SeasonalSectionHelper.isSeasonalSectionTitle("Quick picks"))
        assertFalse(SeasonalSectionHelper.isSeasonalSectionTitle("Because you like Pop"))
        assertFalse(SeasonalSectionHelper.isSeasonalSectionTitle("Forgotten Favorites"))
    }
}

