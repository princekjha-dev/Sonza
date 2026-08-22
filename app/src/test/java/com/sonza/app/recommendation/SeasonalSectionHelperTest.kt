package com.sonza.app.recommendation

import org.junit.Assert.assertEquals
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
    fun testSeasonalTitlesContainSpecifiedTitles() {
        val monsoonConfig = SeasonalSectionHelper.getSeasonConfig(Season.MONSOON)
        assertEquals(listOf("Rain Therapy", "Monsoon Mood", "Rainy Day Listening"), monsoonConfig.titles)

        val autumnConfig = SeasonalSectionHelper.getSeasonConfig(Season.AUTUMN)
        assertEquals(listOf("Autumn Evenings", "Cozy Autumn", "Golden Hour Listening"), autumnConfig.titles)

        val winterConfig = SeasonalSectionHelper.getSeasonConfig(Season.WINTER)
        assertEquals(listOf("Winter Nights", "Cozy Winter", "Cold Night Sessions"), winterConfig.titles)

        val springConfig = SeasonalSectionHelper.getSeasonConfig(Season.SPRING)
        assertEquals(listOf("Spring Vibes", "Fresh Start", "Feel-Good Spring"), springConfig.titles)

        val summerConfig = SeasonalSectionHelper.getSeasonConfig(Season.SUMMER)
        assertEquals(listOf("Summer Vibes", "Summer Drive", "Feel-Good Summer"), summerConfig.titles)
    }

    @Test
    fun testSeasonalMoodDescriptors() {
        val monsoonConfig = SeasonalSectionHelper.getSeasonConfig(Season.MONSOON)
        assertTrue(monsoonConfig.moodDescriptors.contains("rain"))
        assertTrue(monsoonConfig.moodDescriptors.contains("romantic"))
        assertTrue(monsoonConfig.moodDescriptors.contains("calm"))
        assertTrue(monsoonConfig.moodDescriptors.contains("nostalgic"))
        assertTrue(monsoonConfig.moodDescriptors.contains("soft acoustic"))

        val autumnConfig = SeasonalSectionHelper.getSeasonConfig(Season.AUTUMN)
        assertTrue(autumnConfig.moodDescriptors.contains("mellow bollywood"))
        assertTrue(autumnConfig.moodDescriptors.contains("acoustic"))
        assertTrue(autumnConfig.moodDescriptors.contains("romantic"))
        assertTrue(autumnConfig.moodDescriptors.contains("chill"))
        assertTrue(autumnConfig.moodDescriptors.contains("nostalgic evening music"))

        val winterConfig = SeasonalSectionHelper.getSeasonConfig(Season.WINTER)
        assertTrue(winterConfig.moodDescriptors.contains("cozy"))
        assertTrue(winterConfig.moodDescriptors.contains("sleep"))
        assertTrue(winterConfig.moodDescriptors.contains("late-night"))
        assertTrue(winterConfig.moodDescriptors.contains("soft romantic"))
        assertTrue(winterConfig.moodDescriptors.contains("lo-fi/chill"))

        val springConfig = SeasonalSectionHelper.getSeasonConfig(Season.SPRING)
        assertTrue(springConfig.moodDescriptors.contains("uplifting"))
        assertTrue(springConfig.moodDescriptors.contains("fresh"))
        assertTrue(springConfig.moodDescriptors.contains("indie"))
        assertTrue(springConfig.moodDescriptors.contains("feel-good"))
        assertTrue(springConfig.moodDescriptors.contains("romantic"))

        val summerConfig = SeasonalSectionHelper.getSeasonConfig(Season.SUMMER)
        assertTrue(summerConfig.moodDescriptors.contains("energetic"))
        assertTrue(summerConfig.moodDescriptors.contains("road trip"))
        assertTrue(summerConfig.moodDescriptors.contains("bollywood hits"))
        assertTrue(summerConfig.moodDescriptors.contains("dance/pop"))
        assertTrue(summerConfig.moodDescriptors.contains("summer chill"))
    }

    @Test
    fun testCalendarDrivenTitleSelection() {
        val cal = Calendar.getInstance()

        // Test in July (Monsoon)
        cal.set(2026, Calendar.JULY, 15)
        val monsoonTitle = SeasonalSectionHelper.getSeasonalSectionTitle(cal)
        assertTrue(
            monsoonTitle in listOf("Rain Therapy", "Monsoon Mood", "Rainy Day Listening")
        )

        // Test in October (Autumn)
        cal.set(2026, Calendar.OCTOBER, 20)
        val autumnTitle = SeasonalSectionHelper.getSeasonalSectionTitle(cal)
        assertTrue(
            autumnTitle in listOf("Autumn Evenings", "Cozy Autumn", "Golden Hour Listening")
        )

        // Test in January (Winter)
        cal.set(2026, Calendar.JANUARY, 10)
        val winterTitle = SeasonalSectionHelper.getSeasonalSectionTitle(cal)
        assertTrue(
            winterTitle in listOf("Winter Nights", "Cozy Winter", "Cold Night Sessions")
        )

        // Test in April (Spring)
        cal.set(2026, Calendar.APRIL, 5)
        val springTitle = SeasonalSectionHelper.getSeasonalSectionTitle(cal)
        assertTrue(
            springTitle in listOf("Spring Vibes", "Fresh Start", "Feel-Good Spring")
        )

        // Test in May (Summer)
        cal.set(2026, Calendar.MAY, 18)
        val summerTitle = SeasonalSectionHelper.getSeasonalSectionTitle(cal)
        assertTrue(
            summerTitle in listOf("Summer Vibes", "Summer Drive", "Feel-Good Summer")
        )
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
}
