package com.sonza.music.audio.haptics

import android.content.Context
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.sonza.music.core.logging.SonzaLogger
import com.sonza.music.core.model.HapticIntensity

class HapticScheduler(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    private var lastHapticTimeMs = 0L
    private val minIntervalMs = 120L // Prevents buzzing sensation, keeps it crisp and musical

    fun triggerBeatPulse(intensity: HapticIntensity) {
        if (intensity == HapticIntensity.OFF) return

        val now = System.currentTimeMillis()
        if (now - lastHapticTimeMs < minIntervalMs) return
        lastHapticTimeMs = now

        try {
            val amplitude = when (intensity) {
                HapticIntensity.OFF -> 0
                HapticIntensity.SUBTLE -> 45
                HapticIntensity.NORMAL -> 110
                HapticIntensity.STRONG -> 210
            }

            val durationMs = when (intensity) {
                HapticIntensity.OFF -> 0L
                HapticIntensity.SUBTLE -> 15L
                HapticIntensity.NORMAL -> 25L
                HapticIntensity.STRONG -> 40L
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(durationMs, amplitude)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(durationMs)
            }
        } catch (e: Exception) {
            SonzaLogger.w("HapticScheduler", "Haptic pulse failed: ${e.message}")
        }
    }
}
