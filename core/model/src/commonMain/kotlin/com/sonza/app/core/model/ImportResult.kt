package com.sonza.app.core.model

data class ImportResult(
    val originalTitle: String,
    val originalArtist: String,
    val matchedSong: Song?
)
