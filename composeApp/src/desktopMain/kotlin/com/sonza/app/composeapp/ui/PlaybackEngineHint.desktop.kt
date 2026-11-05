package com.sonza.app.composeapp.ui

actual val playbackEngineHint: PlaybackEngineHintCopy = PlaybackEngineHintCopy(
    title = "VLC media player not detected",
    body = "Sonza Desktop uses VLC's playback engine (LibVLC). Install VLC media player and relaunch.",
    detail = "Windows: open PowerShell and run  winget install VideoLAN.VLC",
    actionLabel = "Download VLC",
    actionUrl = "https://www.videolan.org/vlc/",
)
