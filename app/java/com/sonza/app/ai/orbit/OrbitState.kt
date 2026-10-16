package com.sonza.app.ai.orbit

/**
 * Authoritative lifecycle states of the Orbit Assistant:
 * Base states:
 *   IDLE → ORBITING (smooth circular floating motion, no music)
 *   MUSIC_PLAYING → HEADPHONES_ON → MUSIC_REACTIVE (wearing headphones, reacting to music)
 *   MUSIC_PAUSED → HEADPHONES_OFF → ORBITING (headphones smoothly disappear, return to idle orbit)
 *
 * Voice interaction layers on top:
 *   IDLE / MUSIC → WAKE_DETECTED → ACTIVATING → LISTENING → PROCESSING → SPEAKING → return to previous state
 */
enum class OrbitState {
    IDLE,
    ORBITING,
    MUSIC_PLAYING,
    HEADPHONES_ON,
    MUSIC_REACTIVE,
    MUSIC_PAUSED,
    HEADPHONES_OFF,
    WAKE_DETECTED,
    ACTIVATING,
    LISTENING,
    PROCESSING,
    THINKING,
    WORKING,
    SPEAKING,
    ENDING,
    SUCCESS,
    ERROR;

    val isListening: Boolean
        get() = this == LISTENING || this == WAKE_DETECTED || this == ACTIVATING

    val isVoiceActive: Boolean
        get() = this == WAKE_DETECTED || this == ACTIVATING || this == LISTENING ||
                this == PROCESSING || this == THINKING || this == WORKING ||
                this == SPEAKING || this == ENDING || this == SUCCESS || this == ERROR

    val isMusicMode: Boolean
        get() = this == MUSIC_PLAYING || this == HEADPHONES_ON || this == MUSIC_REACTIVE

    val isActive: Boolean
        get() = this != IDLE && this != ORBITING
}
