package com.sonza.app.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sonza.app.core.model.RecentlyPlayed
import com.sonza.app.core.model.Song
import com.sonza.app.recommendation.GenreTaxonomy
import kotlinx.coroutines.delay
import java.util.Calendar

/**
 * Helper to compute context-aware Home screen greetings based on time-of-day
 * and the user's current or recent listening mood.
 */
object HomeGreetingHelper {

    enum class TimePeriod {
        MORNING,
        AFTERNOON,
        EVENING,
        NIGHT
    }

    enum class ListeningMood {
        ROMANTIC,
        SAD,
        ENERGETIC,
        CHILL
    }

    /**
     * Determines the current time period:
     * - Morning: 5:00 AM - 11:59 AM (5..11)
     * - Afternoon: 12:00 PM - 4:59 PM (12..16)
     * - Evening: 5:00 PM - 9:59 PM (17..21)
     * - Night: 10:00 PM - 4:59 AM (22..23, 0..4)
     */
    fun getTimePeriod(hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)): TimePeriod {
        return when (hour) {
            in 5..11 -> TimePeriod.MORNING
            in 12..16 -> TimePeriod.AFTERNOON
            in 17..21 -> TimePeriod.EVENING
            else -> TimePeriod.NIGHT
        }
    }

    /**
     * Detects song mood based on title, artist, and genre inference.
     * Returns null if no clear mood is identified.
     */
    fun detectSongMood(title: String, artist: String): ListeningMood? {
        val text = "$title $artist".lowercase()

        // 1. Romantic / Love keywords and genres
        val romanticKeywords = listOf(
            "love", "romantic", "romance", "heart", "kiss", "forever", "together", "darling",
            "sweetheart", "beloved", "valentine", "crush", "in love", "ishq", "pyaar", "mohabbat",
            "dil", "prem", "sanam", "deewana", "deewani", "humsafar", "pehli nazar", "fall in love",
            "with you", "my everything", "hold me", "you and me", "marry me"
        )
        if (romanticKeywords.any { text.contains(it) }) {
            return ListeningMood.ROMANTIC
        }

        // 2. Sad / Emotional keywords and genres
        val sadKeywords = listOf(
            "sad", "tears", "cry", "crying", "lonely", "alone", "miss you", "pain", "broken",
            "heartbreak", "heartbroken", "gone", "die", "hurt", "depressed", "depression",
            "sorrow", "grief", "dard", "judaai", "rona", "yaad", "tanhai", "alvida",
            "let you go", "without you", "leaving", "empty", "tear", "regret", "lost", "bleed"
        )
        if (sadKeywords.any { text.contains(it) }) {
            return ListeningMood.SAD
        }

        // 3. Energetic / Party keywords and genres
        val energeticKeywords = listOf(
            "party", "dance", "club", "energy", "energetic", "workout", "gym", "hype", "pump",
            "beat", "bass", "drop", "jump", "rock", "metal", "edm", "electro", "fast", "speed",
            "power", "turn up", "rave", "dj", "festival", "hardcore", "fire", "boom", "lit",
            "banger", "nach", "dhamaal", "remix", "techno", "dubstep", "trap"
        )
        if (energeticKeywords.any { text.contains(it) }) {
            return ListeningMood.ENERGETIC
        }

        // 4. Chill / Lo-fi keywords and genres
        val chillKeywords = listOf(
            "chill", "relax", "relaxing", "lo-fi", "lofi", "ambient", "calm", "peaceful", "sleep",
            "dream", "meditation", "slow", "easy", "acoustic", "soft", "study", "vibes", "vibe",
            "cozy", "night drive", "sunset", "breeze", "rain", "coffee", "soothing", "mellow"
        )
        if (chillKeywords.any { text.contains(it) }) {
            return ListeningMood.CHILL
        }

        // 5. Taxonomy-based genre inference fallback
        val genreVector = GenreTaxonomy.inferGenreVector(title, artist)
        if (GenreTaxonomy.isNonZero(genreVector)) {
            val topGenres = GenreTaxonomy.topGenres(genreVector, n = 2)
            if (topGenres.isNotEmpty()) {
                val primaryGenre = topGenres[0].first.lowercase()
                return when (primaryGenre) {
                    "r&b", "soul" -> ListeningMood.ROMANTIC
                    "blues" -> ListeningMood.SAD
                    "edm", "electronic", "rock", "metal", "punk", "hip-hop" -> ListeningMood.ENERGETIC
                    "lo-fi", "ambient", "jazz", "classical", "folk" -> ListeningMood.CHILL
                    else -> null
                }
            }
        }

        return null
    }

    /**
     * Detects mood from currently playing track first, then falls back to recent listening history.
     */
    fun detectMood(
        currentSong: Song?,
        recentlyPlayed: List<RecentlyPlayed>?
    ): ListeningMood? {
        // 1. Current track priority
        if (currentSong != null) {
            val currentMood = detectSongMood(currentSong.title, currentSong.artist)
            if (currentMood != null) {
                return currentMood
            }
        }

        // 2. Recent listening history analysis (last 5 songs)
        if (!recentlyPlayed.isNullOrEmpty()) {
            val recentMoods = recentlyPlayed.take(5).mapNotNull { item ->
                detectSongMood(item.song.title, item.song.artist)
            }

            if (recentMoods.isNotEmpty()) {
                // Find the dominant mood
                val moodCounts = recentMoods.groupingBy { it }.eachCount()
                val (topMood, count) = moodCounts.maxByOrNull { it.value } ?: return null
                // Require at least 2 occurrences or 50%+ of classified songs
                if (count >= 2 || count >= recentMoods.size / 2) {
                    return topMood
                }
            }
        }

        return null
    }

    /**
     * Generates the clean single-line time-based greeting text.
     * When [userName] is provided and non-blank, it is integrated into the greeting.
     *
     * Time-based behavior:
     * - Morning (5..11)   → Good morning 🌅
     * - Afternoon (12..16) → Good afternoon 🌤️
     * - Evening (17..21)   → Good evening 🌆
     * - Night (22..4)     → Good night 🌙
     */
    fun getGreetingText(
        userName: String? = null,
        currentSong: Song? = null,
        recentlyPlayed: List<RecentlyPlayed>? = null,
        hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): String {
        val timePeriod = getTimePeriod(hour)
        val nameSuffix = if (!userName.isNullOrBlank()) ", ${userName.trim()}" else ""

        return when (timePeriod) {
            TimePeriod.MORNING -> "Good morning$nameSuffix 🌅"
            TimePeriod.AFTERNOON -> "Good afternoon$nameSuffix 🌤️"
            TimePeriod.EVENING -> "Good evening$nameSuffix 🌆"
            TimePeriod.NIGHT -> "Good night$nameSuffix 🌙"
        }
    }

    /**
     * Overload for backward compatibility when userName is omitted positionally.
     */
    fun getGreetingText(
        currentSong: Song?,
        recentlyPlayed: List<RecentlyPlayed>?,
        hour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    ): String = getGreetingText(userName = null, currentSong = currentSong, recentlyPlayed = recentlyPlayed, hour = hour)

    /**
     * Composable helper that dynamically maintains a real-time reactive greeting:
     * - Refreshes immediately on app resume (ON_RESUME)
     * - Periodically checks (every 30s) while app remains active so transitions
     *   across time periods (e.g. morning -> afternoon) update automatically
     * - Reacts to user changes, active song changes, and listening history updates
     */
    @Composable
    fun rememberCurrentGreeting(
        userName: String? = null,
        currentSong: Song? = null,
        recentlyPlayed: List<RecentlyPlayed> = emptyList()
    ): String {
        val lifecycleOwner = LocalLifecycleOwner.current
        var currentHour by remember { mutableStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }

        // 1. Refresh when app resumes
        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }

        // 2. Periodic tick (every 30 seconds) to detect crossing time-period boundaries
        LaunchedEffect(Unit) {
            while (true) {
                delay(30_000L)
                val newHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                if (newHour != currentHour) {
                    currentHour = newHour
                }
            }
        }

        return remember(userName, currentSong?.id, recentlyPlayed.firstOrNull()?.song?.id, currentHour) {
            getGreetingText(
                userName = userName,
                currentSong = currentSong,
                recentlyPlayed = recentlyPlayed,
                hour = currentHour
            )
        }
    }
}
