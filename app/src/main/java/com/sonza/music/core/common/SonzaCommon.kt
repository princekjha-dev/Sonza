package com.sonza.music.core.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import java.util.Locale

sealed interface SonzaResult<out T> {
    data class Success<T>(val data: T) : SonzaResult<T>
    data class Error(val exception: Throwable, val message: String? = exception.message) : SonzaResult<Nothing>
    data object Loading : SonzaResult<Nothing>
}

interface DispatcherProvider {
    val main: CoroutineDispatcher
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val unconfined: CoroutineDispatcher
}

class DefaultDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val io: CoroutineDispatcher get() = Dispatchers.IO
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val unconfined: CoroutineDispatcher get() = Dispatchers.Unconfined
}

object Constants {
    const val PREFERENCES_NAME = "sonza_audiophile_prefs"
    const val DATABASE_NAME = "sonza_audiophile.db"
    const val NOTIFICATION_CHANNEL_ID = "sonza_playback_channel_v1"
    const val NOTIFICATION_ID = 1001

    const val DEFAULT_CROSSFADE_SECONDS = 2
    const val DRIFT_TOLERANCE_MS = 90L // <100ms sync target for social listening
    const val DEFAULT_PREAMP_GAIN_DB = 0.0f
    
    // Audio Intent Action
    const val ACTION_PLAY = "com.sonza.music.ACTION_PLAY"
    const val ACTION_PAUSE = "com.sonza.music.ACTION_PAUSE"
    const val ACTION_NEXT = "com.sonza.music.ACTION_NEXT"
    const val ACTION_PREV = "com.sonza.music.ACTION_PREV"
    const val ACTION_TOGGLE_FAVORITE = "com.sonza.music.ACTION_TOGGLE_FAVORITE"
}

object TimeFormatter {
    fun formatDuration(durationMs: Long): String {
        val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hours = minutes / 60
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes % 60, seconds)
        } else {
            String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
        }
    }

    fun formatLrcTimestamp(ms: Long): String {
        val totalSeconds = ms / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hundredths = (ms % 1000) / 10
        return String.format(Locale.getDefault(), "[%02d:%02d.%02d]", minutes, seconds, hundredths)
    }
}
