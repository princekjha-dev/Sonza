package com.sonza.app.ai.orbit

import com.sonza.app.core.model.Song

/**
 * Lightweight, in-memory conversational session context for Orbit.
 * Enables follow-up queries ("another one", "play something romantic", "who wrote this",
 * "add it to my favorites"), pronoun resolution ("this", "it", "him", "her"),
 * and disambiguation/correction handling without storing any sensitive personal data.
 */
class OrbitContextMemory {
    var currentSong: Song? = null
        private set
    var currentArtist: String? = null
        private set
    var currentAlbum: String? = null
        private set
    var currentPlaylist: String? = null
        private set
    var currentQueue: List<Song> = emptyList()
        private set

    var lastIntent: OrbitIntent? = null
        private set
    var lastSearchResults: List<Song> = emptyList()
        private set
    var lastSpokenResponse: String? = null
        private set
    var lastUserUtterance: String? = null
        private set
    var lastAction: String? = null
        private set

    var pendingQuestion: String? = null
        private set
    var pendingDisambiguation: List<Song> = emptyList()
        private set
    var disambiguationOptions: List<String> = emptyList()
        private set

    var conversationTurn: Int = 0
        private set

    fun updatePlayback(song: Song?, queue: List<Song> = emptyList(), playlist: String? = null) {
        currentSong = song
        if (song != null) {
            currentArtist = song.artist.takeIf { it.isNotBlank() && it != "<unknown>" }
            currentAlbum = song.album.takeIf { it.isNotBlank() && it != "<unknown>" }
        }
        if (queue.isNotEmpty()) {
            currentQueue = queue
        }
        if (!playlist.isNullOrBlank()) {
            currentPlaylist = playlist
        }
    }

    fun recordTurn(
        userUtterance: String,
        intent: OrbitIntent,
        spokenResponse: String,
        action: String? = null,
        searchResults: List<Song> = emptyList()
    ) {
        lastUserUtterance = userUtterance
        lastIntent = intent
        lastSpokenResponse = spokenResponse
        lastAction = action
        if (searchResults.isNotEmpty()) {
            lastSearchResults = searchResults
        }
        conversationTurn++
    }

    fun setDisambiguation(songs: List<Song>, options: List<String>) {
        pendingDisambiguation = songs
        disambiguationOptions = options
    }

    fun clearDisambiguation() {
        pendingDisambiguation = emptyList()
        disambiguationOptions = emptyList()
    }

    fun setPendingQuestion(question: String?) {
        pendingQuestion = question
    }

    fun resetContext() {
        lastIntent = null
        lastSearchResults = emptyList()
        lastSpokenResponse = null
        lastUserUtterance = null
        lastAction = null
        pendingQuestion = null
        pendingDisambiguation = emptyList()
        disambiguationOptions = emptyList()
        conversationTurn = 0
    }
}
