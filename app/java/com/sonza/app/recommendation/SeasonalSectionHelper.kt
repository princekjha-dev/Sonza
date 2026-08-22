package com.sonza.app.recommendation

import java.util.Calendar

/**
 * Supported seasons in Sonza's editorial calendar.
 */
enum class Season(val displayName: String) {
    MONSOON("Monsoon"),
    AUTUMN("Autumn"),
    WINTER("Winter"),
    SPRING("Spring"),
    SUMMER("Summer")
}

/**
 * Configuration metadata for a season, including curated titles and content search queries.
 */
data class SeasonConfig(
    val season: Season,
    val titles: List<String>,
    val searchQueries: List<String>,
    val moodDescriptors: List<String>,
    val calendarMonths: Set<Int> // 0-based Calendar month constants (Calendar.JANUARY..Calendar.DECEMBER)
)

/**
 * Centralized helper for determining active seasonal categories, titles, and content queries
 * based on the current date throughout the year.
 */
object SeasonalSectionHelper {

    private val seasonConfigs: Map<Season, SeasonConfig> = mapOf(
        Season.MONSOON to SeasonConfig(
            season = Season.MONSOON,
            titles = listOf("Rain Therapy", "Monsoon Mood", "Rainy Day Listening"),
            searchQueries = listOf(
                "rain therapy soothing songs",
                "monsoon mood romantic songs",
                "rainy day listening acoustic",
                "rain calm nostalgic mellow songs",
                "soft acoustic rain chill music",
                "romantic rain bollywood songs",
                "monsoon acoustic vibes"
            ),
            moodDescriptors = listOf("rain", "romantic", "calm", "nostalgic", "soft acoustic"),
            calendarMonths = setOf(Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER)
        ),
        Season.AUTUMN to SeasonConfig(
            season = Season.AUTUMN,
            titles = listOf("Autumn Evenings", "Cozy Autumn", "Golden Hour Listening"),
            searchQueries = listOf(
                "autumn evenings mellow music",
                "cozy autumn acoustic chill",
                "golden hour listening romantic songs",
                "mellow bollywood autumn evening",
                "nostalgic evening acoustic chill songs",
                "autumn chill romantic acoustic"
            ),
            moodDescriptors = listOf("mellow bollywood", "acoustic", "romantic", "chill", "nostalgic evening music"),
            calendarMonths = setOf(Calendar.OCTOBER, Calendar.NOVEMBER)
        ),
        Season.WINTER to SeasonConfig(
            season = Season.WINTER,
            titles = listOf("Winter Nights", "Cozy Winter", "Cold Night Sessions"),
            searchQueries = listOf(
                "winter nights cozy music",
                "cozy winter late night chill",
                "cold night sessions soft romantic",
                "winter sleep lofi chill beats",
                "soft romantic acoustic winter",
                "cozy late night winter songs"
            ),
            moodDescriptors = listOf("cozy", "sleep", "late-night", "soft romantic", "lo-fi/chill"),
            calendarMonths = setOf(Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY)
        ),
        Season.SPRING to SeasonConfig(
            season = Season.SPRING,
            titles = listOf("Spring Vibes", "Fresh Start", "Feel-Good Spring"),
            searchQueries = listOf(
                "spring vibes uplifting music",
                "fresh start indie feel good songs",
                "feel-good spring romantic hits",
                "uplifting spring indie acoustic",
                "fresh morning feel good songs",
                "romantic spring acoustic vibes"
            ),
            moodDescriptors = listOf("uplifting", "fresh", "indie", "feel-good", "romantic"),
            calendarMonths = setOf(Calendar.MARCH, Calendar.APRIL)
        ),
        Season.SUMMER to SeasonConfig(
            season = Season.SUMMER,
            titles = listOf("Summer Vibes", "Summer Drive", "Feel-Good Summer"),
            searchQueries = listOf(
                "summer vibes road trip songs",
                "summer drive upbeat pop hits",
                "feel-good summer bollywood hits",
                "energetic summer dance music",
                "summer chill pop road trip",
                "feel good summer dance hits"
            ),
            moodDescriptors = listOf("energetic", "road trip", "bollywood hits", "dance/pop", "summer chill"),
            calendarMonths = setOf(Calendar.MAY)
        )
    )

    /**
     * Resolves the [Season] for a 0-indexed month (0 = January, 11 = December).
     */
    fun getSeasonForMonth(month: Int): Season {
        return when (month) {
            Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER -> Season.MONSOON
            Calendar.OCTOBER, Calendar.NOVEMBER -> Season.AUTUMN
            Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY -> Season.WINTER
            Calendar.MARCH, Calendar.APRIL -> Season.SPRING
            Calendar.MAY -> Season.SUMMER
            else -> Season.MONSOON
        }
    }

    /**
     * Resolves the current [Season] based on the provided [Calendar] instance.
     */
    fun getCurrentSeason(calendar: Calendar = Calendar.getInstance()): Season {
        val month = calendar.get(Calendar.MONTH)
        return getSeasonForMonth(month)
    }

    /**
     * Returns the [SeasonConfig] for a given [Season].
     */
    fun getSeasonConfig(season: Season): SeasonConfig {
        return seasonConfigs[season] ?: seasonConfigs[Season.MONSOON]!!
    }

    /**
     * Returns the [SeasonConfig] for the current date.
     */
    fun getCurrentSeasonConfig(calendar: Calendar = Calendar.getInstance()): SeasonConfig {
        val season = getCurrentSeason(calendar)
        return getSeasonConfig(season)
    }

    /**
     * Resolves the title for the seasonal section.
     * Uses a deterministic rotation over the season's curated titles based on the day of the year,
     * ensuring freshness while adhering to the specified season titles.
     */
    fun getSeasonalSectionTitle(calendar: Calendar = Calendar.getInstance()): String {
        val config = getCurrentSeasonConfig(calendar)
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear / 3) % config.titles.size
        return config.titles[index]
    }

    /**
     * Returns the list of search queries for the current season.
     */
    fun getSeasonalQueries(calendar: Calendar = Calendar.getInstance()): List<String> {
        return getCurrentSeasonConfig(calendar).searchQueries
    }

    /**
     * Returns all configured season configurations.
     */
    fun getAllSeasonConfigs(): Map<Season, SeasonConfig> = seasonConfigs
}
