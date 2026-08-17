package com.sonza.music.audio.effects

import kotlin.math.cos
import kotlin.math.sin

object CrossfadeController {

    /**
     * Constant-power crossfade curve to prevent volume drop during track transitions
     * progress: 0.0 (outgoing full, incoming silent) to 1.0 (outgoing silent, incoming full)
     */
    fun calculateVolumes(progress: Float): Pair<Float, Float> {
        val clamped = progress.coerceIn(0.0f, 1.0f)
        val angle = clamped * (Math.PI.toFloat() / 2.0f)
        val outgoingVolume = cos(angle)
        val incomingVolume = sin(angle)
        return Pair(outgoingVolume, incomingVolume)
    }
}
