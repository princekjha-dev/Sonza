package com.sonza.app.ai.orbit

/**
 * Authoritative lifecycle states of the Orbit Assistant:
 * IDLE → WAKE_DETECTED → ACTIVATING → LISTENING → PROCESSING → SPEAKING → ENDING → IDLE
 */
enum class OrbitState {
    IDLE,
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

    val isActive: Boolean
        get() = this != IDLE
}
