package com.sonza.app.core.model

data class ChangelogEntry(
    val version: String,
    val date: String,
    val changes: List<String>,
    val isLatest: Boolean = false
)

object ChangelogData {
    val entries = listOf(
        ChangelogEntry(
            version = "2.6.5.0",
            date = "August 19, 2026",
            isLatest = true,
            changes = listOf(
                "Dynamic Island / Floating Player: Fixed Picture-in-Picture auto-enter on gesture navigation and home press, delivering smooth floating playback overlays for both audio and video modes.",
                "Brand & Launcher Refresh: Rebuilt application launcher mipmaps and vector assets across all display densities with crisp high-resolution rendering.",
                "Playlist Engine 2.0: Added multi-format playlist importer/exporter supporting M3U, M3U8 (with UTF-8 BOM & extended metadata), Sonza backup, and JSON formats.",
                "Audio DSP & Playback Responsiveness: Optimized state emissions, audio focus transitions, and equalizer responsiveness for seamless real-time processing.",
                "System & Dependency Upgrades: Modernized dependencies and optimized memory allocation for smoother UI transitions."
            )
        ),
        ChangelogEntry(
            version = "2.6.4.0",
            date = "August 18, 2026",
            isLatest = false,
            changes = listOf(
                "Core Architecture & Models: Refactored model definitions and migrated build cache to core model module with optimized database queries.",
                "Background Workers & DI: Fixed Hilt worker factories for KSP build and streamlined WorkManager periodic tasks.",
                "Multiplatform & Protobuf: Updated Protobuf integration and Compose Multiplatform dependencies for improved performance.",
                "Production Polish: Enhanced error recovery, app stability, and build optimizations for smooth Android playback."
            )
        ),
        ChangelogEntry(
            version = "2.6.3.0",
            date = "August 13, 2026",
            isLatest = false,
            changes = listOf(
                "Player & Artwork Polish: Centered album artwork with Extra Large (92%) and Full Width (100%) presets, Full Width default, left-aligned now playing metadata layout, and customizable player background styles (Ambient, Black, Light).",
                "Per-Song Audio Source Switching: Seamlessly switch between YouTube and HQ Audio per song right from the player controls, preserving playback position.",
                "Download Feedback & Error Recovery: Comprehensive download status tracking with detailed failure reasons, one-tap retries from player snackbar and Downloads screen, offline fast-fail, and per-song notification tracking.",
                "Story & Layout Fixes: Added 'The Story' section in About screen detailing project history and fixed layout stacking issues."
            )
        ),
        ChangelogEntry(
            version = "2.6.2.0",
            date = "July 25, 2026",
            isLatest = false,
            changes = listOf(
                "YouTube Architecture: Modularized YouTube repository into focused domain services and centralized model parsing.",
                "Playlist & Edit Fixes: Fixed silent playlist edit failures, user-editable playlist detection, and playlist creation auth headers.",
                "HQ Audio Dynamic Routing: Added daily timezone-based provider and per-request route interceptor with automated circuit breaker.",
                "Glassmorphism Visual Polish: Frosted artwork backdrop for player sheets and dialogs.",
                "Library Stability: Concurrent isolated section fetches, eliminated refresh double-fetching and collector leaks.",
                "Upgraded NewPipeExtractor to v0.26.4."
            )
        ),
        ChangelogEntry(
            version = "2.6.1.0",
            date = "July 23, 2026",
            isLatest = false,
            changes = listOf(
                "Stability and Error Recovery: Crash-loop safe mode, stuck-playback watchdog, lyrics circuit breaker, offline cache for search, and album error retry.",
                "Player & Library Ergonomics: Mini player swipe-to-skip, queue swipe-to-remove, library sort/search, sleep timer & speed chips, and stream fallback.",
                "Listen Together Enhancements: Song suggestions, leave-button fix, disband/kick feedback, live server stats, and exact-sync toggle.",
                "Jam Mode: Guest participation, queue routing, and control unblock.",
                "User Experience: Settings search, taste onboarding, Hindi & Bengali translations, accessibility labels.",
                "Support: Feedback API with email and Telegram fallback.",
                "Fixed HQ Audio null-safety, dead failover, leaks, races, blank search results, and swipe-to-delete affordance."
            )
        ),
        ChangelogEntry(
            version = "2.6.0.0",
            date = "July 08, 2026",
            isLatest = false,
            changes = listOf(
                "Listen Together — Spotify Jam-style sessions: The host can now let guests add songs to a shared queue and even control playback (play/pause/skip/seek), all toggled live from the room's Guest Permissions.",
                "Suggest a song: Guests can search and send a song to the host, who can play it whenever they approve — and the guest gets a confirmation. Hosts see all pending suggestions with one-tap approve/reject.",
                "Faster, reliable sync: Fixed a bug where a guest could get stuck on the previous song after a track change. Songs now switch over promptly for everyone.",
                "Exact Sync toggle: New option in Listen Together settings — turn it off for faster song switches when you don't need frame-perfect sync between devices.",
                "Cleaner sessions: When the host ends a session the room is closed for everyone, and inactive members are dropped promptly.",
                "Live room stats: Active-room and listener counts now stream over the live connection instead of periodic polling.",
                "Enforced guest controls everywhere: Guest playback restrictions are now honored across mini player, home screen, notification, headset buttons, and Android Auto.",
                "Clearer join flow: Joining now shows a 'waiting for the host' screen with explicit state feedback.",
                "Fixed notification actions: Approve/Reject buttons on join-request and song-suggestion notifications are now fully operational.",
                "UI polish: The Leave Session button is positioned cleanly without overlap."
            )
        ),
        ChangelogEntry(
            version = "2.5.9.0",
            date = "July 06, 2026",
            isLatest = false,
            changes = listOf(
                "Translucent Bottom Nav: Made bottom navigation bar Spotify-style translucent with a darkened top edge for improved visual contrast.",
                "Search Category Grid: Added a Spotify-style 'Start browsing' category grid to search and removed the redundant Local Library tab.",
                "Library Grid Scrollbar: Added a fading overlay scrollbar to the library grid.",
                "Lifecycle-Aware Home Screen: Made Home screen flow collection lifecycle-aware to optimize resource usage.",
                "Android Auto Pagination: Paginated Android Auto browse/search results to avoid Binder transaction overflow.",
                "Tablet Player Layout: Fixed player layout on tablet portrait — no longer uses landscape layout for non-landscape screens.",
                "Spotify Import Fix: Fixed Spotify playlist import capping at 100 tracks with TOTP-based token auth."
            )
        ),
        ChangelogEntry(
            version = "2.5.8.0",
            date = "July 01, 2026",
            isLatest = false,
            changes = listOf(
                "General bug fixes and performance improvements."
            )
        ),
        ChangelogEntry(
            version = "2.5.7.0",
            date = "June 27, 2026",
            isLatest = false,
            changes = listOf(
                "HQ Audio is now the default: Sonza now streams in high-quality 320 kbps audio out of the box with intelligent fallback to YouTube Music.",
                "Cross-Search YouTube Music: YouTube Music tab is always available in Search regardless of active provider.",
                "Unified Liquid-Glass Player: Transparent frosted-glass backdrop now applies across all player styles.",
                "Smoother Player Transitions: Eliminated per-frame UI recompositions when expanding/collapsing player.",
                "Queue & UI Cleanup: Trimmed redundant actions from queue long-press selection bar.",
                "Bug Fixes & Performance: General stability improvements including album track count fixes."
            )
        ),
        ChangelogEntry(
            version = "2.5.5.0",
            date = "June 17, 2026",
            isLatest = false,
            changes = listOf(
                "Liquid Glass UI (App-Wide): Frosted glass across bottom sheets, top bars, and cards app-wide.",
                "Miniplayer Vinyl Spin: Vinyl artwork rotates continuously while playing in all four miniplayer styles.",
                "Lyrics Screen UX: Added floating 'Resume' pill when scrolling ahead; resumes auto-scroll seamlessly.",
                "About Screen Fixes: Social icon badges wrap gracefully with FlowRow; tech-stack row values fully visible.",
                "NewPipe Extractor v0.26.3: Upgraded upstream release resolving SABR audio stream issues."
            )
        ),
        ChangelogEntry(
            version = "2.5.4.0",
            date = "June 10, 2026",
            isLatest = false,
            changes = listOf(
                "Critical bug fixes and performance improvements."
            )
        ),
        ChangelogEntry(
            version = "2.5.3.0",
            date = "June 03, 2026",
            isLatest = false,
            changes = listOf(
                "General bug fixes and performance improvements."
            )
        ),
        ChangelogEntry(
            version = "2.5.2.0",
            date = "May 30, 2026",
            isLatest = false,
            changes = listOf(
                "High-Quality Audio Sources: Enhanced HQ audio source selection and streaming stability.",
                "Performance Optimization: Significant reduction in UI recompositions and general performance tuning."
            )
        ),
        ChangelogEntry(
            version = "2.5.1.0",
            date = "May 28, 2026",
            isLatest = false,
            changes = listOf(
                "General bug fixes and performance improvements."
            )
        ),
        ChangelogEntry(
            version = "2.5.0.0",
            date = "May 17, 2026",
            isLatest = false,
            changes = listOf(
                "General bug fixes and performance improvements."
            )
        ),
        ChangelogEntry(
            version = "2.4.1.0",
            date = "May 09, 2026",
            isLatest = false,
            changes = listOf(
                "Spotify-Style Volume Normalization: Per-track perceptual loudness analysis for consistent volume levels.",
                "Robust Volume Boost: Safer gain clamping and smoother limiter parameters to avoid distortion.",
                "Spatial Audio Intensity: Dedicated slider for static azimuth sweep and crossfeed strength.",
                "Audio Offload: Full user toggle support for hardware audio offload.",
                "Music Haptics: Real-time RMS sampling from native audio engine with adaptive thresholding.",
                "Music Languages: Expanded to 23 languages with localized recommendations.",
                "Queue & Playlist Reordering: Improved touch thresholds and smooth multi-row drag flicks.",
                "High-Resolution Artwork: High-res Coil image pipeline for crisp rendering on home and detail screens.",
                "AI Equalizer Fixes: Improved prompt parsing, state persistence across restarts, and robust fallbacks."
            )
        ),
        ChangelogEntry(
            version = "2.4.0.0",
            date = "May 03, 2026",
            isLatest = false,
            changes = listOf(
                "Dynamic App Icons: Introduced 'Logo Variants' (Aether, Pulse, Resonance).",
                "Unified Multiplatform Architecture: Shift to Kotlin Multiplatform (KMP) shared codebase.",
                "Critical Playback Fixes: Optimized recovery logic and gapless playback transitions.",
                "Enhanced Session Persistence: Accurately remembers last played song, position, and queue.",
                "Next-Gen Audio Features: Integrated Spatial Audio processing and TTS ducking.",
                "Download Stability: Major overhaul of download engine and resume reliability.",
                "Smart Connectivity: Discord RPC sync and refreshed Glance widgets."
            )
        ),
        ChangelogEntry(
            version = "2.3.0.0",
            date = "April 17, 2026",
            isLatest = false,
            changes = listOf(
                "Expressive Player Experience: Tactile bounce feedback for player controls and expanded waveform seekers.",
                "Professional Video Suite: Full-screen Video Player gesture overhaul with double-tap seek and overlay controls.",
                "Smarter Artist Insights: Prioritized official artist channels and direct navigation.",
                "UI Refinements & Polish: Home Screen FAB repositioning and YT Music-style speed dial.",
                "About Sonza: Refreshed vision and developer section.",
                "Bug Fixes & Stability: Resolved seekbar long-press bugs and drawer stability."
            )
        ),
        ChangelogEntry(
            version = "2.2.2.0",
            date = "April 08, 2026",
            isLatest = false,
            changes = listOf(
                "Neural Hardware AI Engine: Hardware-level AI audio engine in C++ with real-time signal analyzer.",
                "Smart AI Equalizer: Professional-grade AI tuning with A/B comparison and prompt history.",
                "AI Feature Transparency: BETA indicators across all AI features.",
                "Listen Together Integration: Audio Output dialog with live session controls.",
                "Core Library Upgrades: Updated to Media3 1.10.0 and Coil 3.4.0.",
                "Bug Fixes: Resolved AI EQ preset type mismatches and state unboxing crashes."
            )
        ),
        ChangelogEntry(
            version = "2.2.1.0",
            date = "April 05, 2026",
            isLatest = false,
            changes = listOf(
                "Real-time Download Progress: Live download percentage and MB transfer stats.",
                "Premium Updater UI: Redesigned System Update screen with expressive gradients.",
                "Performance Overhaul: 70% reduction in redundant recompositions.",
                "UI Modernization: Replaced legacy Toasts with theme-aware Snackbars.",
                "Advanced Lyrics Experience: Redesigned lyrics header and sticky layout.",
                "Local Library Control: Filter local audio by duration.",
                "Playlist & Sync Fixes: Resolved infinite loading loops and improved ownership sync."
            )
        ),
        ChangelogEntry(
            version = "2.2.0.0",
            date = "March 31, 2026",
            isLatest = false,
            changes = listOf(
                "Immersive Artist Experience: Redesigned Artist screen with dynamic colors and multiple artist navigation.",
                "Modernized Player UI: YT Music style with centered Audio/Video switch and marquee titles.",
                "Technical Audio Insights: Song Info sheet displaying bitrate and codec.",
                "Advanced History & Privacy: Date-grouped history with Incognito mode.",
                "Unified Selection & Reordering: Standardized long-press selection with batch actions.",
                "Related Songs & Autoplay: Related sheet and expanded Radio queue.",
                "Playback Excellence: Reactive quality downscaling, Volume Fade-in, and BLE recovery."
            )
        ),
        ChangelogEntry(
            version = "2.1.4.0",
            date = "March 29, 2026",
            isLatest = false,
            changes = listOf(
                "Shuffle Integrity: Resolved song skipping in shuffle mode.",
                "Audio Output Switching: Fixed playback resumption across Bluetooth and speakers.",
                "Visual Error Feedback: Player screen error overlay with retry actions.",
                "Intelligent Recovery: Exponential backoff for network retries and stream fallback.",
                "Battery & Efficiency: Dynamic Audio Offload and optimized memory usage."
            )
        ),
        ChangelogEntry(
            version = "2.1.3.0",
            date = "March 25, 2026",
            isLatest = false,
            changes = listOf(
                "Comprehensive Backup & Restore: Securely back up library cache and settings in .sonza format.",
                "Lyrics Screen Overhaul: Immersive lyrics screen built with Material 3 Expressive components.",
                "Home Screen Personalization: Layout customization and refined Quick Picks.",
                "Enhanced Playlist Management: .sonza and .m3u playlist export options.",
                "Improved Audio Stability: Audio output detection and auto-resume after calls.",
                "Visual Refinements: Wavy seekbar as default and rotating vinyl artwork."
            )
        ),
        ChangelogEntry(
            version = "2.1.2.0",
            date = "March 21, 2026",
            isLatest = false,
            changes = listOf(
                "Modernized Credits & About: Expressive styling and TopAppBar scroll behavior.",
                "Advanced Playlist Management: Redesigned Playlist and Import screens.",
                "Universal Playlist Import: Native import for YouTube Music sources and .m3u files.",
                "Hardware Integration: Added MediaButtonReceiver for media key handling.",
                "Database Integrity: Migrated database to version 9."
            )
        ),
        ChangelogEntry(
            version = "2.1.1.0",
            date = "March 17, 2026",
            isLatest = false,
            changes = listOf(
                "Core Playback Overhaul: 500ms playback startup latency and decoupled position updates.",
                "Skip & Resolution Stability: Resolved orphaned coroutines and auto-skip chains.",
                "Material 3 Expressive UI Polish: Wavy Seekbar, bouncy Like/Dislike animations, and redesigned Credits.",
                "Android Auto Overhaul: Fixed skip buttons, auto-advance, and playlist transitions.",
                "Advanced Playlist Management: Track sorting, mass reordering, and multi-select deletion."
            )
        ),
        ChangelogEntry(
            version = "2.1.0.0",
            date = "March 14, 2026",
            isLatest = false,
            changes = listOf(
                "Material 3 Expressive Redesign: Massive UI overhaul with unified expressive components and squircle shapes.",
                "Visual Consistency: Switched all artwork, thumbnails, and action buttons to modern Squircle shapes.",
                "Redesigned Core Screens: Completely refreshed Settings, Album, Playlist, Artist, and Search screens.",
                "Robust Backup Restore: Fixed crashes after Swift Backup restoration by handling corrupted encryption keys.",
                "Seamless History Sync: Automatically enables 'Sync with YouTube History' for logged-in users.",
                "TV Optimization: Integrated dpadFocusable across all major interactive elements for better remote navigation."
            )
        ),
        ChangelogEntry(
            version = "2.0.0.0",
            date = "March 8, 2026",
            isLatest = false,
            changes = listOf(
                "Spotify Pro Import: Enhanced Spotify integration supporting albums, artists, and individual tracks with real-time fetching progress and mobile share link support.",
                "YouTube Playlist Pagination: Resolved continuation token issues and pagination limits for large playlists, ensuring all songs are loaded correctly.",
                "F-Droid Readiness: Added Fastlane metadata and anti-feature disclosures for F-Droid submission.",
                "Next-Gen Personalization: High-performance Recommendation Engine with JNI-based native scoring, genre affinity vectors, and deep YouTube Music integration.",
                "Persistent Logging & Diagnostics: Integrated a robust file-based logging system that captures startup events and provides a 'Share App Logs' feature for easier troubleshooting.",
                "Performance Optimization: Implemented explicit keys in all major LazyColumn and LazyGrid lists, significantly reducing UI re-composition and ensuring buttery-smooth scrolling.",
                "App Health & Crash Reporting: Fully optimized ACRA integration for Android 16, capturing more detailed system context (RAM, Display, Build ID) in every bug report.",
                "Cinematic Player Transitions: Completely refactored Video Mode using AnimatedContent for seamless cross-fading between artwork and video without UI layout shifts.",
                "Infinite Play (Radio Mode): New toggle in the Queue screen that automatically extends your session with similar songs when the queue nears the end.",
                "Interactive Queue: Added full context (3-dot) menus to every item in the queue, including 'Now Playing', allowing for deep song management without leaving the list.",
                "Adaptive Recommendations: 'Made for You' banners are now closeable with a 7-day persistence logic, automatically switching to a 'Daily Mix' style when dismissed.",
                "Infinite Home Feed: Optimized auto-loading logic that proactively fetches diverse recommendation strategies (Artist Deep-dives, Nostalgia, Blended Genres) as you scroll.",
                "Listen Together 2.0: Massive redesign with Material 3 Expressive UI and ultra-low latency Protobuf-based binary transport for perfect real-time synchronization.",
                "MiniPlayer UI: Added dotted progress indicator for a more refined and modern aesthetic.",
                "Ringtone Engine: Fully restored 'Set as Ringtone' feature with integrated audio trimmer, progress tracking, and robust system permission handling.",
                "Under-the-hood Stability: Resolved JVM signature clashes in logging utilities, fixed critical coroutine import errors, and improved audio decoder resilience on newer Android versions."
            )
        ),
        ChangelogEntry(
            version = "1.3.1.2",
            date = "March 1, 2026",
            isLatest = false,
            changes = listOf(
                "Integrated ACRA crash reporting with Telegram and download log sharing",
                "Added iOS-style liquid glass bottom navigation (toggleable in Settings)",
                "Support for setting custom download locations in Settings",
                "Significant startup performance: fixed 3-second hang in MainActivity",
                "Enhanced splash screen transitions and fixed background morphing on Xiaomi",
                "Increased default navigation bar opacity to 90% for improved visual aesthetics",
                "Dynamic TopBar behavior: hides on scroll in Album/Playlist screens",
                "Fixed full-screen playback on Android 12 by using actual view height",
                "Initial Android TV feature support declarations in AndroidManifest.xml",
                "Restored classic app logo and related branding",
                "Fixed ACRA 5.13 notification configuration to ensure reliable crash reporting",
                "Reduced APK footprint and installation lag via resource optimization"
            )
        ),
        ChangelogEntry(
            version = "1.3.1.1",
            date = "February 27, 2026",
            isLatest = false,
            changes = listOf(
                "Implemented official Android Splash Screen API for smoother startup",
                "Added Adaptive Icon support (Circle.png) with 20% inset for all shapes",
                "Optimized app startup by lazy-loading encrypted session data",
                "Hardware acceleration for Mesh Gradient Background (lower CPU/GPU usage)",
                "Significant UI smoothness improvements in Search and Player Queue",
                "Fixed redundant dependency initialization in MainActivity",
                "Optimized theme-switching performance to reduce UI lag",
                "Fixed coroutine compilation errors in entrance animations",
                "Updated Splash Screen logo to match new branding",
                "Added branding credit to 𝕵𝖊𝖊𝖛𝖊𝖘𝖍 (@JazzeeBlaze) in Credits screen"
            )
        ),
        ChangelogEntry(
            version = "1.3.1.0",
            date = "February 26, 2026",
            isLatest = false,
            changes = listOf(
                "Added visually striking What's New screen",
                "Improved Updater UI with gradient backgrounds and animations",
                "Set default mini player style to Floating Pill",
                "Enhanced Navigation Bar with 15% default transparency",
                "Allowed UI content to flow behind Navigation Bar (Glass Effect)",
                "Added 'Pay via UPI' in Support screen (pkjha2028@okaxis)",
                "Improved Support screen layout with better gradients",
                "Fixed 'Resources\$NotFoundException' crash on launch",
                "Removed QR code scanning and generation to optimize app size",
                "Added transparency customization (0-85%) for mini player and nav bar",
                "Removed blank backgrounds from mini player for all styles",
                "Embedded metadata and album art in downloaded songs",
                "Resolved various compiler warnings and deprecated API usages",
                "Optimized overall system stability and performance"
            )
        )
    )
}
