# SONZA — Hear Music Differently.

<p align="center">
  <b>High-Fidelity Audiophile Music Streaming & Social Listening Platform for Android</b>
</p>

---

## 🎧 Overview

**SONZA** is a modern, ad-free Android music application designed for audiophiles and music lovers who care about playback fidelity, dynamic visuals, and social listening.

### Key Highlights

* **Audiophile-Grade Playback**: Bit-perfect reproduction with technical source badges (`FLAC 24-bit / 96kHz Lossless`).
* **Dynamic Album-Art Theming**: Real-time palette extraction with WCAG AA contrast enforcement.
* **10-Band Parametric Equalizer**: `-12 dB` to `+12 dB` range with real-time Bezier curve visualization, preamp controls, and studio presets.
* **Binaural Spatial Audio**: Immersive acoustic simulations (`Natural`, `Wide`, `Cinema`, `Immersive`, `Studio`).
* **Synchronized Lyrics Engine**: LRC parser supporting line-level and word-level highlighting with spring-physics auto-scrolling and tap-to-seek.
* **Social "Listen Together" Rooms**: Sub-100ms synchronized rooms via WebSocket without unauthorized stream redistribution.
* **ReplayGain Loudness Normalization**: Peak limiter preventing digital clipping.
* **Local & Authorized Online Sources**: Native Android `MediaStore` indexer + safe Spotify playlist metadata importer.
* **Beat-Synchronized Musical Haptics**: Transient-detected vibrational accents.

---

## 🛠 Technology Stack

* **Language**: Kotlin 2.1
* **UI**: Jetpack Compose & Material 3
* **Architecture**: Clean Architecture + MVVM + Unidirectional Data Flow (UDF)
* **Audio Layer**: AndroidX Media3 (`ExoPlayer`, `MediaSessionService`)
* **Local Storage**: Room Database & DataStore Preferences
* **Dependency Injection**: Dagger Hilt
* **Image Loading**: Coil 3
* **Networking**: OkHttp & WebSockets

---

## 📁 Architecture Structure

```text
com.sonza.music
│
├── core/
│   ├── common/       # Result wrappers, Dispatcher providers, Formatters
│   ├── database/     # Room Entities (tracks, playlists, history, lyrics) & DAOs
│   ├── model/        # Domain models (Track, AudioQuality, EqualizerPreset, Lyrics)
│   ├── permissions/  # PermissionManager for Android 13+ audio permissions
│   ├── logging/      # Structured privacy-conscious logger
│   └── theme/        # Dark audiophile design tokens & DynamicThemeExtractor
│
├── audio/
│   ├── engine/       # Media3 ExoPlayer wrapper with gapless & sleep timer
│   ├── service/      # SonzaMediaSessionService (MediaSession, lockscreen, notification)
│   ├── equalizer/    # 10-Band EQ (31Hz - 16kHz) & DSP formula engine
│   ├── spatial/      # SpatialAudioProcessor & binaural stereo expander
│   ├── effects/      # ReplayGain normalizer & constant-power crossfade
│   ├── analyzer/     # AudioAnalyzer with real-time FFT & beat detection
│   └── haptics/      # HapticScheduler for musical vibration pulses
│
├── data/
│   ├── local/        # Android MediaStore LocalMusicScanner
│   ├── source/       # High-Res 24-bit/96kHz reference catalog
│   ├── spotify/      # SpotifyPlaylistImporter with fuzzy Levenshtein matching
│   └── repository/   # Music, Playlist, Lyrics, Stats, and Settings repositories
│
└── feature/
    ├── onboarding/   # 5-step interactive audiophile setup flow
    ├── home/         # Dynamic backdrop, continue listening, curated mixes
    ├── search/       # Debounced search with category filter chips
    ├── library/      # Songs, Albums, Artists, Playlists, Favorites, Local files
    ├── player/       # Floating Mini-Player & Fullscreen Now Playing centerpiece
    ├── lyrics/       # Real-time synchronized lyrics with word highlighting
    ├── equalizer/    # 10-band slider interface & frequency curve canvas
    ├── visualizer/   # Real-time visualizer (Waveform, Spectrum, Circular, Pulse)
    ├── listeningroom/# Social Listen Together room UI
    ├── spotify/      # Playlist import analysis and resolution report
    ├── stats/        # Listening statistics, monthly wrap, genre breakdown
    └── settings/     # Playback, DSP, Haptics, Privacy, Storage settings
```

---

## 🚀 Running the Project

1. Open project in **Android Studio Meerkat / Ladybug (2024.3+)**.
2. Ensure JDK 17+ is selected.
3. Sync Gradle and run on device or emulator (Android 8.0+ / API 26+).

### Listen Together WebSocket Server (Optional)

```bash
cd server
pip install websockets
python3 listen_together_server.py
```

### Verification Tests

```bash
python3 server/verify_algorithms.py
```

---

## 📄 License

Apache License 2.0.
