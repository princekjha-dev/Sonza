package com.sonza.app.core.model

/**
 * Single source of truth for the active user identity across Sonza.
 * Supports logged-in YouTube Music accounts and lightweight guest profiles.
 */
data class UserProfile(
    val name: String? = null,
    val avatarUrl: String? = null,
    val isGuest: Boolean = true,
    val preferredLanguages: Set<String> = emptySet()
)
