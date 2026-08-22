package com.sonza.app.recommendation

import com.sonza.app.core.model.Song
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
 * Centralized helper for determining active seasonal categories, titles, content queries,
 * environmental sound filtering, and duplicate-section protection.
 */
object SeasonalSectionHelper {

    private val UNIVERSAL_NON_MUSIC_PATTERNS = listOf(
        "sound effect", "sound effects", "sound fx", "sfx", "foley",
        "sound recording", "sound recordings", "field recording", "nature recording", "nature recordings",
        "nature sound recording", "nature sound recordings",
        "white noise", "pink noise", "brown noise", "green noise", "grey noise", "black noise",
        "nature ambience", "nature sound", "nature sounds", "forest ambience", "forest sound", "forest sounds",
        "jungle ambience", "jungle sound", "jungle sounds", "sleep sounds", "sleep sound", "sleeping sound",
        "ambient noise", "ambient sound", "ambient sounds", "binaural beat", "binaural beats", "delta wave",
        "delta waves", "theta wave", "theta waves", "brain waves", "isochronic", "asmr",
        "fan sound", "fan sounds", "ac sound", "fan/ac sounds", "air conditioner sound", "ventilation sound"
    )

    private val SEASON_NON_MUSIC_PATTERNS = mapOf(
        Season.MONSOON to listOf(
            "rain sound", "rain sounds", "thunder sound", "thunder sounds", "thunder recording",
            "thunder recordings", "thunderstorm sound", "thunderstorm sounds", "thunderstorm recording",
            "thunderstorm recordings", "lightning sound", "lightning sounds", "meditation rain",
            "rain for sleep", "rain sleep", "heavy rain", "gentle rain sound", "pouring rain",
            "storm ambience", "rain ambience", "rain noise", "rain sound effects", "rain sound effect",
            "rain on tin roof", "rain on roof", "rain on window", "rain on tent"
        ),
        Season.AUTUMN to listOf(
            "falling leaf", "falling leaves", "falling-leaf", "leaf sound", "leaf sounds",
            "leaves sound", "leaves sounds", "autumn forest sound", "autumn ambience sound",
            "crunching leaves", "forest ambience", "nature sound recording", "nature recordings",
            "falling-leaf sound effects", "leaf sound effect", "leaf sound effects"
        ),
        Season.WINTER to listOf(
            "fireplace sound", "fireplace sounds", "winter fireplace sounds", "fireplace crackling",
            "crackling fire", "crackling fireplace", "burning fire sound", "burning wood sound",
            "wind recording", "wind recordings", "wind sound", "wind sounds", "howling wind",
            "snow ambience", "blizzard sound", "blizzard sounds", "snowstorm sound",
            "winter wind sound", "winter ambience", "winter wind"
        ),
        Season.SPRING to listOf(
            "bird sound", "bird sounds", "birds sound", "birds sounds", "bird chirping",
            "birds chirping", "bird singing sound", "birds singing sound", "birdsong",
            "bird song recording", "spring birds sound", "spring bird sounds", "forest ambience",
            "nature sound recording", "nature sound recordings"
        ),
        Season.SUMMER to listOf(
            "ocean sound", "ocean sounds", "summer beach sounds", "ocean wave sound",
            "ocean waves sound", "ocean waves", "sea waves sound", "beach ambience",
            "beach sound", "beach sounds", "crashing waves", "underwater sound",
            "fan sound", "fan sounds", "ac sound", "ac sounds", "fan/ac sounds",
            "air conditioner sound", "air conditioner"
        )
    )

    private val MULTI_HOUR_AMBIENT_REGEX = Regex(
        """(?:\b\d+\s*(?:hours?|hrs?)\b.*(?:sound|noise|rain|sleep|nature|waves|fireplace|wind|ambient|thunder|fan|beach)|(?:sound|noise|rain|sleep|nature|waves|fireplace|wind|ambient|thunder|fan|beach).*\b\d+\s*(?:hours?|hrs?)\b)""",
        RegexOption.IGNORE_CASE
    )

    private val ALL_SEASONAL_TITLES = setOf(
        "Rain Therapy ☘️🌧️", "Rain Therapy", "Monsoon Mood", "Rainy Day Listening",
        "Autumn Vibes 🍂☕", "Autumn Vibes", "Autumn Evenings", "Cozy Autumn", "Golden Hour Listening",
        "Winter Nights ❄️🌙", "Winter Nights", "Cozy Winter", "Cold Night Sessions",
        "Spring Vibes 🌸🌿", "Spring Vibes", "Fresh Start", "Feel-Good Spring",
        "Summer Vibes ☀️🎶", "Summer Vibes", "Summer Drive", "Feel-Good Summer"
    )

    private val seasonConfigs: Map<Season, SeasonConfig> = mapOf(
        Season.MONSOON to SeasonConfig(
            season = Season.MONSOON,
            titles = listOf("Rain Therapy ☘️🌧️"),
            searchQueries = listOf(
                "bollywood monsoon romance songs",
                "80s 90s nostalgia bollywood romantic songs",
                "soft romantic monsoon songs",
                "monsoon bollywood melodies",
                "emotional melodies hindi romantic",
                "acoustic chill monsoon songs",
                "late evening hindi songs soft romantic",
                "nostalgic indian rain songs"
            ),
            moodDescriptors = listOf(
                "bollywood romance",
                "80s/90s nostalgia",
                "soft romantic",
                "monsoon bollywood",
                "emotional melodies",
                "acoustic/chill",
                "late-evening",
                "nostalgic indian music"
            ),
            calendarMonths = setOf(Calendar.JUNE, Calendar.JULY, Calendar.AUGUST, Calendar.SEPTEMBER)
        ),
        Season.AUTUMN to SeasonConfig(
            season = Season.AUTUMN,
            titles = listOf("Autumn Vibes 🍂☕"),
            searchQueries = listOf(
                "autumn vibes warm acoustic music",
                "soft bollywood nostalgic songs",
                "cozy acoustic romantic songs",
                "indie chill acoustic songs",
                "mellow evening acoustic music",
                "cozy listening golden hour songs",
                "warm indie folk acoustic playlist",
                "nostalgic romantic acoustic songs"
            ),
            moodDescriptors = listOf(
                "warm acoustic",
                "soft bollywood",
                "nostalgic",
                "romantic",
                "indie/chill",
                "mellow evening",
                "cozy listening",
                "golden-hour"
            ),
            calendarMonths = setOf(Calendar.OCTOBER, Calendar.NOVEMBER)
        ),
        Season.WINTER to SeasonConfig(
            season = Season.WINTER,
            titles = listOf("Winter Nights ❄️🌙"),
            searchQueries = listOf(
                "winter nights cozy songs",
                "slow romantic bollywood songs",
                "bollywood classics slow romantic",
                "late night soft acoustic songs",
                "winter lofi chill music",
                "cozy winter evening playlists songs",
                "nostalgic slow romantic songs",
                "deep listening soft acoustic winter"
            ),
            moodDescriptors = listOf(
                "slow romantic",
                "bollywood classics",
                "late-night",
                "soft acoustic",
                "lo-fi/chill",
                "cozy",
                "nostalgic",
                "deep listening"
            ),
            calendarMonths = setOf(Calendar.DECEMBER, Calendar.JANUARY, Calendar.FEBRUARY)
        ),
        Season.SPRING to SeasonConfig(
            season = Season.SPRING,
            titles = listOf("Spring Vibes 🌸🌿"),
            searchQueries = listOf(
                "fresh spring vibes feel good songs",
                "fresh bollywood romantic songs",
                "indie pop uplifting songs",
                "feel good happy acoustic songs",
                "uplifting romantic tracks",
                "light acoustic spring playlist songs",
                "happy chill acoustic pop songs",
                "fresh bright feel good music"
            ),
            moodDescriptors = listOf(
                "feel-good",
                "fresh bollywood",
                "romantic",
                "indie pop",
                "uplifting",
                "light acoustic",
                "happy/chill",
                "new music"
            ),
            calendarMonths = setOf(Calendar.MARCH, Calendar.APRIL)
        ),
        Season.SUMMER to SeasonConfig(
            season = Season.SUMMER,
            titles = listOf("Summer Vibes ☀️🎶"),
            searchQueries = listOf(
                "summer road trip hits songs",
                "summer dance pop hits",
                "feel good summer bollywood hits",
                "energetic summer party playlist songs",
                "chill summer tracks pop songs",
                "summer vibes popular current music",
                "upbeat road trip party songs",
                "feel good dance pop hits"
            ),
            moodDescriptors = listOf(
                "bollywood hits",
                "dance/pop",
                "road-trip",
                "energetic",
                "feel-good",
                "party",
                "chill summer",
                "popular current music"
            ),
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
     * Uses the consistent emoji-bearing title for the season.
     */
    fun getSeasonalSectionTitle(calendar: Calendar = Calendar.getInstance()): String {
        val config = getCurrentSeasonConfig(calendar)
        return config.titles.first()
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

    /**
     * Returns all known seasonal section titles (including variations) for duplicate protection.
     */
    fun getAllSeasonalTitles(): Set<String> = ALL_SEASONAL_TITLES

    /**
     * Checks if a section title is a seasonal recommendation title.
     */
    fun isSeasonalSectionTitle(title: String): Boolean {
        val normalized = title.trim().lowercase()
        val stripped = title.replace(Regex("[^a-zA-Z0-9 ]"), "").trim().lowercase()
        return ALL_SEASONAL_TITLES.any { candidate ->
            val normCandidate = candidate.trim().lowercase()
            val stripCandidate = candidate.replace(Regex("[^a-zA-Z0-9 ]"), "").trim().lowercase()
            normCandidate == normalized || stripCandidate == stripped
        }
    }

    /**
     * Filters out non-musical environmental recordings, white noise, sound effects, and ambient loops
     * to ensure seasonal recommendations represent actual music tracks.
     */
    fun isEligibleSeasonalSong(
        title: String,
        artist: String = "",
        durationMs: Long = 0L,
        season: Season = Season.MONSOON
    ): Boolean {
        val normalizedTitle = title.lowercase().trim()
        val normalizedArtist = artist.lowercase().trim()
        val combined = "$normalizedTitle $normalizedArtist"

        // 1. Universal non-music terms check
        for (pattern in UNIVERSAL_NON_MUSIC_PATTERNS) {
            if (combined.contains(pattern)) return false
        }

        // 2. Season-specific environmental sound terms check
        SEASON_NON_MUSIC_PATTERNS[season]?.let { patterns ->
            for (pattern in patterns) {
                if (combined.contains(pattern)) return false
            }
        }

        // 3. Multi-hour ambient noise title pattern (e.g. "Rain Sounds 2 Hours", "10 Hours White Noise")
        if (MULTI_HOUR_AMBIENT_REGEX.containsMatchIn(combined)) {
            return false
        }

        // 4. Excessive duration ambient loop check (> 15 minutes with ambient keywords)
        if (durationMs > 15 * 60 * 1000L) {
            val ambientKeywords = listOf("sound", "noise", "rain", "sleep", "nature", "waves", "fireplace", "wind", "ambient", "relaxation", "thunder")
            if (ambientKeywords.any { combined.contains(it) }) {
                return false
            }
        }

        return true
    }

    /**
     * Overload for [Song] model.
     */
    fun isEligibleSeasonalSong(song: Song, season: Season): Boolean {
        return isEligibleSeasonalSong(
            title = song.title,
            artist = song.artist,
            durationMs = song.duration,
            season = season
        )
    }
}

