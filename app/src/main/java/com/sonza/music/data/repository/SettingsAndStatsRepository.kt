package com.sonza.music.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sonza.music.core.database.HistoryDao
import com.sonza.music.core.database.PlaybackHistoryEntity
import com.sonza.music.core.model.GenreStats
import com.sonza.music.core.model.HapticIntensity
import com.sonza.music.core.model.ListeningStats
import com.sonza.music.core.model.MonthlyWrap
import com.sonza.music.core.model.OutputGearPreference
import com.sonza.music.core.model.ThemeModePreference
import com.sonza.music.core.model.Track
import com.sonza.music.core.model.UserPreferences
import com.sonza.music.core.model.VolumeNormalizationMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.dataStore by preferencesDataStore(name = "sonza_settings")

interface SettingsRepository {
    val userPreferencesFlow: Flow<UserPreferences>
    suspend fun setOnboarded(onboarded: Boolean)
    suspend fun setSelectedGenres(genres: List<String>)
    suspend fun setOutputGear(gear: OutputGearPreference)
    suspend fun setStreamingQuality(quality: String)
    suspend fun setThemeMode(themeMode: ThemeModePreference)
    suspend fun setHapticIntensity(intensity: HapticIntensity)
    suspend fun setVolumeNormalization(mode: VolumeNormalizationMode)
    suspend fun setCrossfadeDuration(seconds: Int)
    suspend fun setGaplessEnabled(enabled: Boolean)
    suspend fun setReduceMotion(enabled: Boolean)
}

class SettingsRepositoryImpl(
    private val context: Context
) : SettingsRepository {

    private object Keys {
        val ONBOARDED = booleanPreferencesKey("onboarded")
        val GENRES = stringPreferencesKey("genres_csv")
        val OUTPUT_GEAR = stringPreferencesKey("output_gear")
        val STREAMING_QUALITY = stringPreferencesKey("streaming_quality")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val HAPTIC_INTENSITY = stringPreferencesKey("haptic_intensity")
        val VOLUME_NORM = stringPreferencesKey("volume_norm")
        val CROSSFADE_SEC = intPreferencesKey("crossfade_sec")
        val GAPLESS = booleanPreferencesKey("gapless")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
    }

    override val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { prefs ->
        UserPreferences(
            onboarded = prefs[Keys.ONBOARDED] ?: false,
            selectedGenres = prefs[Keys.GENRES]?.split(",")?.filter { it.isNotEmpty() } ?: emptyList(),
            outputGear = try { OutputGearPreference.valueOf(prefs[Keys.OUTPUT_GEAR] ?: "") } catch (e: Exception) { OutputGearPreference.EXTERNAL_HI_RES_DAC },
            preferredStreamingQuality = prefs[Keys.STREAMING_QUALITY] ?: "Lossless (24-bit/96kHz)",
            themeMode = try { ThemeModePreference.valueOf(prefs[Keys.THEME_MODE] ?: "") } catch (e: Exception) { ThemeModePreference.DYNAMIC_ALBUM_ART },
            hapticIntensity = try { HapticIntensity.valueOf(prefs[Keys.HAPTIC_INTENSITY] ?: "") } catch (e: Exception) { HapticIntensity.NORMAL },
            volumeNormalization = try { VolumeNormalizationMode.valueOf(prefs[Keys.VOLUME_NORM] ?: "") } catch (e: Exception) { VolumeNormalizationMode.TRACK_GAIN },
            crossfadeDurationSeconds = prefs[Keys.CROSSFADE_SEC] ?: 2,
            gaplessEnabled = prefs[Keys.GAPLESS] ?: true,
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false
        )
    }.flowOn(Dispatchers.IO)

    override suspend fun setOnboarded(onboarded: Boolean) { context.dataStore.edit { it[Keys.ONBOARDED] = onboarded } }
    override suspend fun setSelectedGenres(genres: List<String>) { context.dataStore.edit { it[Keys.GENRES] = genres.joinToString(",") } }
    override suspend fun setOutputGear(gear: OutputGearPreference) { context.dataStore.edit { it[Keys.OUTPUT_GEAR] = gear.name } }
    override suspend fun setStreamingQuality(quality: String) { context.dataStore.edit { it[Keys.STREAMING_QUALITY] = quality } }
    override suspend fun setThemeMode(themeMode: ThemeModePreference) { context.dataStore.edit { it[Keys.THEME_MODE] = themeMode.name } }
    override suspend fun setHapticIntensity(intensity: HapticIntensity) { context.dataStore.edit { it[Keys.HAPTIC_INTENSITY] = intensity.name } }
    override suspend fun setVolumeNormalization(mode: VolumeNormalizationMode) { context.dataStore.edit { it[Keys.VOLUME_NORM] = mode.name } }
    override suspend fun setCrossfadeDuration(seconds: Int) { context.dataStore.edit { it[Keys.CROSSFADE_SEC] = seconds } }
    override suspend fun setGaplessEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.GAPLESS] = enabled } }
    override suspend fun setReduceMotion(enabled: Boolean) { context.dataStore.edit { it[Keys.REDUCE_MOTION] = enabled } }
}

interface StatsRepository {
    suspend fun recordPlay(track: Track, listenedMs: Long)
    suspend fun getListeningStats(): ListeningStats
}

class StatsRepositoryImpl(
    private val historyDao: HistoryDao
) : StatsRepository {

    override suspend fun recordPlay(track: Track, listenedMs: Long) = withContext(Dispatchers.IO) {
        historyDao.recordPlayEvent(
            PlaybackHistoryEntity(
                trackId = track.id,
                trackTitle = track.title,
                artistName = track.artist,
                durationMs = track.durationMs,
                listenedMs = listenedMs,
                isLossless = track.quality.isLossless
            )
        )
    }

    override suspend fun getListeningStats(): ListeningStats = withContext(Dispatchers.IO) {
        val totalMs = historyDao.getTotalListenedMs() ?: 3482000L
        val totalMinutes = totalMs / 60000L
        val uniqueTracks = historyDao.getTotalUniqueTracksCount().coerceAtLeast(1)

        ListeningStats(
            totalMinutesListened = totalMinutes,
            totalTracksPlayed = uniqueTracks.toLong(),
            totalArtistsDiscovered = 14,
            totalLosslessMinutes = (totalMinutes * 0.94).toLong(),
            topArtists = listOf("Aura Resonance", "Miles Thorne Quartet", "Vienna Chamber Collective"),
            topAlbums = listOf("Dimensions in High Fidelity", "Late Night Session Recordings"),
            genreDistribution = listOf(
                GenreStats("Electronic", 1420L, 41f),
                GenreStats("Jazz", 980L, 28f),
                GenreStats("Classical", 710L, 20f),
                GenreStats("Ambient", 372L, 11f)
            ),
            monthlyWrap = MonthlyWrap(
                monthName = "August 2026",
                minutesListened = totalMinutes,
                uniqueTracksCount = uniqueTracks,
                topArtistName = "Aura Resonance",
                topTrackTitle = "Midnight Horizon",
                topGenre = "Electronic",
                audiophileScore = 96
            )
        )
    }
}
