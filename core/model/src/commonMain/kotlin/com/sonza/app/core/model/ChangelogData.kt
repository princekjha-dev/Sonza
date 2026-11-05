package com.sonza.app.core.model

data class ChangelogEntry(
    val version: String,
    val date: String,
    val changes: List<String>,
    val isLatest: Boolean = false
)

object ChangelogData {
    val latestEntry = ChangelogEntry(
        version = "2.6.5.0",
        date = "August 21, 2026",
        isLatest = true,
        changes = listOf(
            "Real-Time Profile & Header: Instant authenticated user name and avatar synchronization on login, with dynamic time-of-day home greetings.",
            "Consolidated Dynamic Island & Mini Player: Streamlined floating playback overlay and unified gesture interactions for audio and video.",
            "Architecture Streamlining: Removed legacy Listen Together network protocols and protobuf dependencies for faster, leaner app performance.",
            "Editorial Home Feed Rebuild: Reimagined discovery feed with compact rails, spotlight hero, 4-row high-density song packs, and 16:9 videos.",
            "Streamlined Search UX: Active search mode with clean landing categories, instant query history management, and zero clutter.",
            "Dynamic Island / Floating Player: Fixed Picture-in-Picture auto-enter on gesture navigation and home press, delivering smooth floating playback overlays for both audio and video modes.",
            "Brand & Launcher Refresh: Rebuilt application launcher mipmaps and vector assets across all display densities with crisp high-resolution rendering.",
            "Playlist Engine 2.0: Added multi-format playlist importer/exporter supporting M3U, M3U8 (with UTF-8 BOM & extended metadata), Sonza backup, and JSON formats.",
            "Audio DSP & Playback Responsiveness: Optimized state emissions, audio focus transitions, and equalizer responsiveness for seamless real-time processing.",
            "System & Dependency Upgrades: Modernized dependencies and optimized memory allocation for smoother UI transitions."
        )
    )

    val entries: List<ChangelogEntry> = listOf(latestEntry)
}
