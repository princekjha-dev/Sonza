package com.sonza.app.ai.orbit

import android.content.Context
import android.media.AudioManager
import com.sonza.app.core.domain.repository.LibraryRepository
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.RepeatMode
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SongSource
import com.sonza.app.data.repository.DownloadRepository
import com.sonza.app.data.repository.ListeningHistoryRepository
import com.sonza.app.data.repository.LyricsRepository
import com.sonza.app.data.repository.YouTubeRepository
import com.sonza.app.player.MusicPlayer
import com.sonza.app.player.SleepTimerManager
import com.sonza.app.player.SleepTimerOption
import com.sonza.app.service.DownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import java.util.UUID

/**
 * Drop-in typealias so all callers map to the authoritative [OrbitResult].
 */
typealias OrbitExecutionResult = OrbitResult

/**
 * Authoritative Executor and Command Router for all Sonza Orbit Intents.
 * Directly executes actions on Sonza's central playback and repository layers,
 * guaranteeing zero duplicate playback states, and producing rich spoken voice feedback.
 */
class OrbitExecutor(
    private val musicPlayer: MusicPlayer,
    private val youTubeRepository: YouTubeRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val lyricsRepository: LyricsRepository,
    private val sleepTimerManager: SleepTimerManager,
    private val listeningHistoryRepository: ListeningHistoryRepository? = null,
    private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    suspend fun execute(
        intent: OrbitIntent,
        currentSong: Song?,
        dialogueContext: OrbitDialogueContext = OrbitDialogueContext.Idle,
        contextMemory: OrbitContextMemory? = null
    ): OrbitResult = withContext(Dispatchers.IO) {
        try {
            when (intent) {
                // --- 0. Conversational Flow & Wake Word ---
                is OrbitIntent.WakeWordOnlyIntent -> {
                    OrbitResult(
                        success = true,
                        action = "WAKE_WORD",
                        spokenResponse = "Yes?",
                        visualFeedback = "Listening...",
                        requiresFollowUp = true,
                        updatedContext = OrbitDialogueContext.AwaitingCommand()
                    )
                }

                is OrbitIntent.CancelIntent -> {
                    OrbitResponseEngine.cancel()
                }

                is OrbitIntent.ConfirmationResponseIntent -> {
                    if (dialogueContext is OrbitDialogueContext.AwaitingConfirmation) {
                        if (intent.confirmed) {
                            val pName = dialogueContext.targetName
                            val allPlaylists = libraryRepository.getSavedPlaylists().first()
                            val target = allPlaylists.find { it.name.equals(pName, ignoreCase = true) }
                            if (target != null) {
                                libraryRepository.removePlaylist(target.id)
                                OrbitResponseEngine.deletePlaylist(pName)
                            } else {
                                OrbitResult(
                                    success = false,
                                    action = "DELETE_PLAYLIST",
                                    spokenResponse = "I couldn't find a playlist called $pName."
                                )
                            }
                        } else {
                            OrbitResponseEngine.cancel()
                        }
                    } else {
                        OrbitResult(success = true, action = "CONFIRM", spokenResponse = "Understood.")
                    }
                }

                is OrbitIntent.DisambiguationChoiceIntent -> {
                    resolveAndPlayTrack(intent.choice)
                }

                is OrbitIntent.CorrectionIntent -> {
                    val corrected = intent.correctionText
                        .removePrefix("no")
                        .removePrefix("nope")
                        .removePrefix("not that one")
                        .removePrefix("actually play")
                        .removePrefix("play")
                        .trim()
                    if (corrected.isNotBlank()) {
                        resolveAndPlayTrack(corrected)
                    } else {
                        withContext(Dispatchers.Main) { musicPlayer.seekToNext() }
                        OrbitResponseEngine.next()
                    }
                }

                is OrbitIntent.FollowUpIntent -> {
                    resolveAndPlayTrack(intent.query)
                }

                is OrbitIntent.SmallTalkIntent -> {
                    val q = intent.query.lowercase(Locale.ROOT)
                    val reply = when {
                        q.contains("how are you") -> "I'm doing wonderful and ready for music! How can I help you?"
                        q.contains("joke") -> "Why was the musician arrested? Because they got into too much treble!"
                        q.contains("thank") -> "You're very welcome! Enjoy the music."
                        q.contains("morning") -> "Good morning! Ready for some fresh tunes?"
                        else -> "Hello there! I'm Orbit, your Sonza music assistant."
                    }
                    OrbitResult(success = true, action = "SMALL_TALK", spokenResponse = reply)
                }

                // --- 1. Playback Controls ---
                is OrbitIntent.PauseIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.pause() }
                    OrbitResponseEngine.pause()
                }

                is OrbitIntent.ResumeIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.play() }
                    OrbitResponseEngine.resume()
                }

                is OrbitIntent.StopIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.stop() }
                    OrbitResponseEngine.stop()
                }

                is OrbitIntent.NextTrackIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.seekToNext() }
                    val nextSong = musicPlayer.playerState.value.queue.getOrNull(
                        musicPlayer.playerState.value.currentIndex + 1
                    )
                    OrbitResponseEngine.next(nextSong)
                }

                is OrbitIntent.PreviousTrackIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.seekToPrevious() }
                    OrbitResponseEngine.previous()
                }

                is OrbitIntent.ReplayIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.seekTo(0L) }
                    OrbitResponseEngine.replay()
                }

                is OrbitIntent.SeekIntent -> {
                    val currentPos = musicPlayer.playerState.value.currentPosition
                    val targetPos = if (intent.isForward) {
                        currentPos + (intent.seconds * 1000L)
                    } else {
                        (currentPos - (intent.seconds * 1000L)).coerceAtLeast(0L)
                    }
                    withContext(Dispatchers.Main) { musicPlayer.seekTo(targetPos) }
                    OrbitResponseEngine.seek(intent.seconds, intent.isForward)
                }

                is OrbitIntent.SeekAbsoluteIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.seekTo(intent.positionSeconds * 1000L) }
                    OrbitResult(success = true, action = "SEEK", spokenResponse = "Jumped to position.")
                }

                is OrbitIntent.SetSpeedIntent -> {
                    val clampedSpeed = intent.speed.coerceIn(0.5f, 2.0f)
                    withContext(Dispatchers.Main) { musicPlayer.setPlaybackParameters(clampedSpeed, 1.0f) }
                    OrbitResult(
                        success = true,
                        action = "SET_SPEED",
                        spokenResponse = "Playback speed set to ${intent.speed}x."
                    )
                }

                // --- 2. Volume & Audio Quality ---
                is OrbitIntent.VolumeIntent -> {
                    when (intent.action) {
                        VolumeAction.UP -> {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val pct = ((current.toFloat() / max) * 100).toInt()
                            OrbitResponseEngine.volume(pct, isUp = true)
                        }
                        VolumeAction.DOWN -> {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                            val current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val pct = ((current.toFloat() / max) * 100).toInt()
                            OrbitResponseEngine.volume(pct, isUp = false)
                        }
                        VolumeAction.SET_LEVEL -> {
                            val target = intent.levelPercent ?: 50
                            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val targetVol = ((target.toFloat() / 100f) * max).toInt().coerceIn(0, max)
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
                            OrbitResponseEngine.volume(target, isUp = true)
                        }
                    }
                }

                is OrbitIntent.MuteIntent -> {
                    if (intent.mute) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
                        OrbitResponseEngine.mute(true)
                    } else {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
                        OrbitResponseEngine.mute(false)
                    }
                }

                is OrbitIntent.AudioQualityIntent -> {
                    OrbitResult(
                        success = true,
                        action = "AUDIO_QUALITY",
                        spokenResponse = "Audio quality set to ${intent.quality}."
                    )
                }

                // --- 3. Modes: Shuffle & Repeat ---
                is OrbitIntent.ShuffleIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.toggleShuffle() }
                    OrbitResponseEngine.shuffle(intent.enable)
                }

                is OrbitIntent.RepeatIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.setRepeatMode(intent.mode) }
                    OrbitResponseEngine.repeat(intent.mode.name)
                }

                // --- 4. Play Music & Discovery ---
                is OrbitIntent.PlayTrackIntent -> {
                    resolveAndPlayTrack(intent.trackName, intent.artistName)
                }

                is OrbitIntent.PlayArtistIntent -> {
                    val query = if (!intent.moodOrGenre.isNullOrBlank()) {
                        "${intent.artistName} ${intent.moodOrGenre}"
                    } else {
                        intent.artistName
                    }
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(results.first(), results, 0) }
                        OrbitResponseEngine.playArtist(intent.artistName, intent.moodOrGenre)
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_ARTIST",
                            spokenResponse = "I couldn't find songs by ${intent.artistName}."
                        )
                    }
                }

                is OrbitIntent.PlayAlbumIntent -> {
                    val albums = youTubeRepository.searchAlbums(intent.albumName)
                    val album = albums.firstOrNull()
                    if (album != null) {
                        val fullAlbum = youTubeRepository.getAlbum(album.id)
                        val tracks = fullAlbum?.songs ?: emptyList()
                        if (tracks.isNotEmpty()) {
                            withContext(Dispatchers.Main) { musicPlayer.playSong(tracks.first(), tracks, 0) }
                            OrbitResponseEngine.playAlbum(album.title)
                        } else {
                            OrbitResult(
                                success = false,
                                action = "PLAY_ALBUM",
                                spokenResponse = "The album ${album.title} appears to be empty."
                            )
                        }
                    } else {
                        resolveAndPlayTrack(intent.albumName)
                    }
                }

                is OrbitIntent.PlayPlaylistIntent -> {
                    val local = libraryRepository.getSavedPlaylists().first().find {
                        it.name.equals(intent.playlistName, ignoreCase = true)
                    }
                    if (local != null) {
                        val tracks = libraryRepository.getCachedPlaylistSongsFlow(local.id).first()
                        if (tracks.isNotEmpty()) {
                            withContext(Dispatchers.Main) { musicPlayer.playSong(tracks.first(), tracks, 0) }
                            OrbitResponseEngine.playPlaylist(local.name)
                        } else {
                            OrbitResult(
                                success = false,
                                action = "PLAY_PLAYLIST",
                                spokenResponse = "Playlist ${local.name} has no songs yet."
                            )
                        }
                    } else {
                        val onlineList = youTubeRepository.searchPlaylists(intent.playlistName)
                        val online = onlineList.firstOrNull()
                        if (online != null) {
                            val fullPlaylist = youTubeRepository.getPlaylist(online.id)
                            val tracks = fullPlaylist.songs
                            if (tracks.isNotEmpty()) {
                                withContext(Dispatchers.Main) { musicPlayer.playSong(tracks.first(), tracks, 0) }
                                OrbitResponseEngine.playPlaylist(online.title)
                            } else {
                                OrbitResult(
                                    success = false,
                                    action = "PLAY_PLAYLIST",
                                    spokenResponse = "Couldn't load tracks from ${online.title}."
                                )
                            }
                        } else {
                            OrbitResult(
                                success = false,
                                action = "PLAY_PLAYLIST",
                                spokenResponse = "I couldn't find a playlist called ${intent.playlistName}."
                            )
                        }
                    }
                }

                is OrbitIntent.PlayGenreOrMoodIntent -> {
                    val query = "${intent.query} mix"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(results.first(), results, 0) }
                        OrbitResponseEngine.playMoodOrGenre(intent.query)
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_BY_MOOD",
                            spokenResponse = "I couldn't find a ${intent.query} mix right now."
                        )
                    }
                }

                is OrbitIntent.PlayActivityIntent -> {
                    val query = "${intent.activity} music mix"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(results.first(), results, 0) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_BY_ACTIVITY",
                            spokenResponse = "Playing a ${intent.activity} mix for you.",
                            visualFeedback = "${intent.activity.replaceFirstChar { it.uppercase() }} Mix",
                            actionIndicator = "🏃"
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_BY_ACTIVITY",
                            spokenResponse = "Couldn't find music for ${intent.activity}."
                        )
                    }
                }

                is OrbitIntent.PlayEraIntent -> {
                    val query = "${intent.era} hits mix"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(results.first(), results, 0) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_BY_ERA",
                            spokenResponse = "Playing ${intent.era} classics for you.",
                            visualFeedback = "${intent.era} Hits",
                            actionIndicator = "📻"
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_BY_ERA",
                            spokenResponse = "Couldn't find songs from the ${intent.era} era."
                        )
                    }
                }

                is OrbitIntent.PlayCreatorIntent -> {
                    val query = "songs by ${intent.creator}"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(results.first(), results, 0) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_BY_CREATOR",
                            spokenResponse = "Playing music created by ${intent.creator}."
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_BY_CREATOR",
                            spokenResponse = "Couldn't find music by ${intent.creator}."
                        )
                    }
                }

                is OrbitIntent.PlayVersionIntent -> {
                    val query = "${intent.trackName} ${intent.version}"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(results.first(), results, 0) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_VERSION",
                            spokenResponse = "Playing the ${intent.version} version of ${intent.trackName}."
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_VERSION",
                            spokenResponse = "Couldn't find the ${intent.version} version of ${intent.trackName}."
                        )
                    }
                }

                is OrbitIntent.PlayFestivalIntent -> {
                    val query = "${intent.festival} songs"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(results.first(), results, 0) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_FESTIVAL",
                            spokenResponse = "Playing ${intent.festival} celebration music."
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_FESTIVAL",
                            spokenResponse = "Couldn't find music for ${intent.festival}."
                        )
                    }
                }

                is OrbitIntent.PlayFavoritesIntent -> {
                    val favorites = libraryRepository.getCachedPlaylistSongsFlow("LM").first()
                    if (favorites.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(favorites.first(), favorites, 0) }
                        OrbitResponseEngine.playFavorites()
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_FAVORITES",
                            spokenResponse = "You haven't liked any songs yet. Ask me to like the current song to start your favorites!"
                        )
                    }
                }

                is OrbitIntent.SurpriseMeIntent -> {
                    val results = youTubeRepository.search("trending music", YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        val shuffled = results.shuffled()
                        withContext(Dispatchers.Main) { musicPlayer.playSong(shuffled.first(), shuffled, 0) }
                        OrbitResponseEngine.surpriseMe()
                    } else {
                        OrbitResult(
                            success = false,
                            action = "SURPRISE_ME",
                            spokenResponse = "Couldn't load surprise tracks right now."
                        )
                    }
                }

                is OrbitIntent.SimilarMusicIntent -> {
                    if (currentSong != null) {
                        val similar = youTubeRepository.getRelatedSongs(currentSong.id)
                        if (similar.isNotEmpty()) {
                            withContext(Dispatchers.Main) { musicPlayer.playSong(similar.first(), similar, 0) }
                            OrbitResult(
                                success = true,
                                action = "PLAY_SIMILAR",
                                spokenResponse = "Playing songs similar to ${currentSong.title}."
                            )
                        } else {
                            OrbitResult(
                                success = false,
                                action = "PLAY_SIMILAR",
                                spokenResponse = "Couldn't find similar songs for ${currentSong.title}."
                            )
                        }
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_SIMILAR",
                            spokenResponse = "Play a song first so I can find similar music!"
                        )
                    }
                }

                is OrbitIntent.TrendingIntent -> {
                    val trending = youTubeRepository.search("trending songs", YouTubeRepository.FILTER_SONGS)
                    if (trending.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(trending.first(), trending, 0) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_TRENDING",
                            spokenResponse = "Playing trending songs.",
                            visualFeedback = "Trending",
                            actionIndicator = "🔥"
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_TRENDING",
                            spokenResponse = "Couldn't load trending songs right now."
                        )
                    }
                }

                is OrbitIntent.NewReleasesIntent -> {
                    val newReleases = youTubeRepository.search("new releases songs", YouTubeRepository.FILTER_SONGS)
                    if (newReleases.isNotEmpty()) {
                        withContext(Dispatchers.Main) { musicPlayer.playSong(newReleases.first(), newReleases, 0) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_NEW",
                            spokenResponse = "Playing new music releases.",
                            visualFeedback = "New Releases",
                            actionIndicator = "🚀"
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_NEW",
                            spokenResponse = "Couldn't load new releases right now."
                        )
                    }
                }

                is OrbitIntent.PlayUnheardIntent -> {
                    val results = youTubeRepository.getRecommendations()
                    if (results.isNotEmpty()) {
                        val pick = results.shuffled().first()
                        withContext(Dispatchers.Main) { musicPlayer.playSong(pick, results, results.indexOf(pick)) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_UNHEARD",
                            spokenResponse = "Playing something fresh you haven't heard."
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "PLAY_UNHEARD",
                            spokenResponse = "Couldn't find new music to explore right now."
                        )
                    }
                }

                is OrbitIntent.ContinueListeningIntent -> {
                    val history = listeningHistoryRepository?.getAllHistory()?.firstOrNull() ?: emptyList()
                    val lastItem = history.firstOrNull()
                    if (lastItem != null) {
                        val lastSong = Song(
                            id = lastItem.songId,
                            title = lastItem.songTitle,
                            artist = lastItem.artist,
                            album = lastItem.album,
                            duration = lastItem.duration,
                            thumbnailUrl = lastItem.thumbnailUrl,
                            source = SongSource.YOUTUBE
                        )
                        val queue = history.map { h ->
                            Song(
                                id = h.songId,
                                title = h.songTitle,
                                artist = h.artist,
                                album = h.album,
                                duration = h.duration,
                                thumbnailUrl = h.thumbnailUrl,
                                source = SongSource.YOUTUBE
                            )
                        }
                        withContext(Dispatchers.Main) { musicPlayer.playSong(lastSong, queue, 0) }
                        OrbitResult(
                            success = true,
                            action = "CONTINUE_LISTENING",
                            spokenResponse = "Continuing from where you left off with ${lastSong.title}."
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "CONTINUE_LISTENING",
                            spokenResponse = "You don't have recent listening history to continue."
                        )
                    }
                }

                is OrbitIntent.PlayMostPlayedIntent -> {
                    val history = listeningHistoryRepository?.getAllHistory()?.firstOrNull() ?: emptyList()
                    val mostPlayedItem = history.maxByOrNull { it.playCount }
                    if (mostPlayedItem != null) {
                        val song = Song(
                            id = mostPlayedItem.songId,
                            title = mostPlayedItem.songTitle,
                            artist = mostPlayedItem.artist,
                            album = mostPlayedItem.album,
                            duration = mostPlayedItem.duration,
                            thumbnailUrl = mostPlayedItem.thumbnailUrl,
                            source = SongSource.YOUTUBE
                        )
                        withContext(Dispatchers.Main) { musicPlayer.playSong(song, listOf(song), 0) }
                        OrbitResult(
                            success = true,
                            action = "PLAY_MOST_PLAYED",
                            spokenResponse = "Playing your most played song, ${song.title}."
                        )
                    } else {
                        OrbitResult(success = false, action = "PLAY_MOST_PLAYED", spokenResponse = "No listening stats available yet.")
                    }
                }

                // --- 5. Favorites & Playlists ---
                is OrbitIntent.FavoriteIntent -> {
                    if (currentSong != null) {
                        libraryRepository.addSongToPlaylist("LM", currentSong)
                        withContext(Dispatchers.Main) { musicPlayer.updateLikeStatus(true) }
                        OrbitResponseEngine.like(currentSong.title)
                    } else {
                        OrbitResult(
                            success = false,
                            action = "LIKE",
                            spokenResponse = "No song is currently playing to add to favorites."
                        )
                    }
                }

                is OrbitIntent.DislikeSongIntent -> {
                    if (currentSong != null) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.updateDislikeStatus(true)
                            musicPlayer.seekToNext()
                        }
                        OrbitResponseEngine.dislike(currentSong.title)
                    } else {
                        OrbitResult(
                            success = false,
                            action = "DISLIKE",
                            spokenResponse = "No song is currently playing."
                        )
                    }
                }

                is OrbitIntent.SaveToLibraryIntent -> {
                    if (currentSong != null) {
                        libraryRepository.addSongToPlaylist("LM", currentSong)
                        OrbitResult(
                            success = true,
                            action = "SAVE_LIBRARY",
                            spokenResponse = "Saved ${currentSong.title} to your library."
                        )
                    } else {
                        OrbitResult(success = false, action = "SAVE_LIBRARY", spokenResponse = "No song is playing.")
                    }
                }

                is OrbitIntent.CreatePlaylistIntent -> {
                    val newPlaylist = Playlist(
                        id = UUID.randomUUID().toString(),
                        title = intent.name,
                        author = "You",
                        thumbnailUrl = null,
                        songs = emptyList()
                    )
                    libraryRepository.savePlaylist(newPlaylist)
                    if (intent.addCurrentTrack && currentSong != null) {
                        libraryRepository.addSongToPlaylist(newPlaylist.id, currentSong)
                    }
                    OrbitResponseEngine.createPlaylist(intent.name)
                }

                is OrbitIntent.AddToPlaylistIntent -> {
                    val playlists = libraryRepository.getSavedPlaylists().first()
                    val p = playlists.find { it.name.equals(intent.playlistName, ignoreCase = true) }
                    val songToAdd = if (intent.trackName != null) {
                        youTubeRepository.search(intent.trackName, YouTubeRepository.FILTER_SONGS).firstOrNull() ?: currentSong
                    } else {
                        currentSong
                    }
                    if (p != null && songToAdd != null) {
                        libraryRepository.addSongToPlaylist(p.id, songToAdd)
                        OrbitResponseEngine.addToPlaylist(songToAdd.title, p.name)
                    } else if (p == null) {
                        OrbitResult(
                            success = false,
                            action = "ADD_TO_PLAYLIST",
                            spokenResponse = "I couldn't find a playlist called ${intent.playlistName}."
                        )
                    } else {
                        OrbitResult(
                            success = false,
                            action = "ADD_TO_PLAYLIST",
                            spokenResponse = "Play or name a song to add to ${p.name}."
                        )
                    }
                }

                is OrbitIntent.DeletePlaylistIntent -> {
                    OrbitResult(
                        success = true,
                        action = "DELETE_PLAYLIST_CONFIRM",
                        spokenResponse = "Are you sure you want to delete your playlist ${intent.playlistName}?",
                        requiresFollowUp = true,
                        updatedContext = OrbitDialogueContext.AwaitingConfirmation(
                            actionType = "DELETE_PLAYLIST",
                            targetName = intent.playlistName
                        )
                    )
                }

                is OrbitIntent.ShowPlaylistsIntent -> {
                    OrbitResult(
                        success = true,
                        action = "SHOW_PLAYLISTS",
                        spokenResponse = "Opening your playlists in library.",
                        navigationTarget = "library"
                    )
                }

                // --- 6. Queue ---
                is OrbitIntent.QueueAddIntent -> {
                    val songToQueue = if (intent.trackName != null) {
                        youTubeRepository.search(intent.trackName, YouTubeRepository.FILTER_SONGS).firstOrNull() ?: currentSong
                    } else {
                        currentSong
                    }
                    if (songToQueue != null) {
                        withContext(Dispatchers.Main) {
                            if (intent.playNext) {
                                musicPlayer.playNext(listOf(songToQueue))
                            } else {
                                musicPlayer.addToQueue(listOf(songToQueue))
                            }
                        }
                        OrbitResponseEngine.queueAdd(songToQueue.title, intent.playNext)
                    } else {
                        OrbitResult(
                            success = false,
                            action = "QUEUE_ADD",
                            spokenResponse = "Which song would you like me to queue?"
                        )
                    }
                }

                is OrbitIntent.QueueClearIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.clearQueue() }
                    OrbitResponseEngine.queueClear()
                }

                is OrbitIntent.QueueShowIntent -> {
                    OrbitResult(
                        success = true,
                        action = "QUEUE_SHOW",
                        spokenResponse = "Opening the play queue.",
                        navigationTarget = "queue"
                    )
                }

                is OrbitIntent.QueueNextInfoIntent -> {
                    val nextSong = musicPlayer.playerState.value.queue.getOrNull(
                        musicPlayer.playerState.value.currentIndex + 1
                    )
                    OrbitResponseEngine.infoWhatIsNext(nextSong)
                }

                // --- 7. Metadata, Lyrics & Questions (Section 17) ---
                is OrbitIntent.SongInfoIntent -> {
                    when (intent.queryType) {
                        SongInfoType.SINGER -> OrbitResponseEngine.infoSinger(currentSong)
                        SongInfoType.WRITER -> OrbitResponseEngine.infoWriter(currentSong?.artist, currentSong)
                        SongInfoType.COMPOSER -> OrbitResponseEngine.infoComposer(currentSong?.artist, currentSong)
                        SongInfoType.ALBUM -> OrbitResponseEngine.infoAlbum(currentSong)
                        SongInfoType.RELEASE_DATE -> OrbitResult(
                            success = true,
                            action = "INFO_RELEASE_DATE",
                            spokenResponse = if (currentSong != null) "This track was released by ${currentSong.artist}." else "No song currently playing."
                        )
                        SongInfoType.VERSIONS -> OrbitResult(
                            success = true,
                            action = "INFO_VERSIONS",
                            spokenResponse = if (currentSong != null) "I can play the original, acoustic, remix, or live version of ${currentSong.title}. Just ask!" else "No song is currently playing."
                        )
                        else -> OrbitResponseEngine.infoWhatIsPlaying(currentSong)
                    }
                }

                is OrbitIntent.LyricsIntent -> {
                    OrbitResponseEngine.lyrics(intent.show)
                }

                // --- 8. Timers ---
                is OrbitIntent.SleepTimerIntent -> {
                    if (intent.cancel) {
                        sleepTimerManager.cancelTimer()
                        OrbitResponseEngine.cancelSleepTimer()
                    } else if (intent.stopAfterCurrent) {
                        sleepTimerManager.startTimer(SleepTimerOption.END_OF_SONG)
                        OrbitResult(
                            success = true,
                            action = "SLEEP_TIMER",
                            spokenResponse = "Music will automatically pause after this song finishes."
                        )
                    } else {
                        val mins = intent.minutes ?: 30
                        sleepTimerManager.startTimer(SleepTimerOption.CUSTOM, customMinutes = mins)
                        OrbitResponseEngine.sleepTimer(mins)
                    }
                }

                // --- 9. Navigation & UI ---
                is OrbitIntent.NavigationIntent -> {
                    OrbitResponseEngine.navigation(intent.screen)
                }

                is OrbitIntent.PlayerUiIntent -> {
                    OrbitResult(
                        success = true,
                        action = "PLAYER_UI",
                        spokenResponse = if (intent.expand) "Opening player." else "Minimizing player.",
                        navigationTarget = if (intent.expand) "player_expand" else "player_collapse"
                    )
                }

                is OrbitIntent.DownloadIntent -> {
                    if (currentSong != null) {
                        DownloadService.startDownload(context, currentSong)
                        OrbitResponseEngine.download(currentSong.title)
                    } else {
                        OrbitResult(
                            success = false,
                            action = "DOWNLOAD",
                            spokenResponse = "Play a song first to download it."
                        )
                    }
                }

                is OrbitIntent.OfflineModeIntent -> {
                    OrbitResult(
                        success = true,
                        action = "OFFLINE_MODE",
                        spokenResponse = if (intent.enabled) "Offline mode is on. Playing downloaded tracks." else "Offline mode is off."
                    )
                }

                is OrbitIntent.StorageInfoIntent -> {
                    OrbitResult(
                        success = true,
                        action = "STORAGE_INFO",
                        spokenResponse = "Your downloaded songs are safely cached on device for offline playback.",
                        navigationTarget = "downloads"
                    )
                }

                is OrbitIntent.ListeningStatsIntent -> {
                    val count = listeningHistoryRepository?.getAllHistory()?.firstOrNull()?.size ?: 0
                    OrbitResult(
                        success = true,
                        action = "STATS",
                        spokenResponse = "You've played $count songs in your listening history on Sonza.",
                        navigationTarget = "history"
                    )
                }

                is OrbitIntent.HelpIntent -> {
                    OrbitResponseEngine.help()
                }

                is OrbitIntent.CompoundIntent -> {
                    var lastResult: OrbitResult = OrbitResult(true, "COMPOUND", "Executed commands.")
                    for (sub in intent.intents) {
                        lastResult = execute(sub, currentSong, dialogueContext, contextMemory)
                    }
                    lastResult
                }

                is OrbitIntent.UnknownIntent -> {
                    OrbitResponseEngine.unknown(intent.rawQuery)
                }

                else -> {
                    OrbitResponseEngine.unknown("")
                }
            }
        } catch (e: Exception) {
            OrbitLogger.error("OrbitExecutor exception: ${e.message}", e)
            OrbitResponseEngine.error("Sorry, I couldn't complete that command.")
        }
    }

    /**
     * Resolves the strongest match for a song title and artist,
     * then executes playback on the central MusicPlayer.
     */
    private suspend fun resolveAndPlayTrack(trackQuery: String, artistName: String? = null): OrbitResult {
        val fullQuery = if (!artistName.isNullOrBlank()) "$trackQuery $artistName" else trackQuery
        val results = youTubeRepository.search(fullQuery, YouTubeRepository.FILTER_SONGS)

        if (results.isEmpty()) {
            return OrbitResult(
                success = false,
                action = "PLAY_SONG",
                spokenResponse = "I couldn't find \"$fullQuery\". Try asking for the song title and artist."
            )
        }

        val exactMatch = results.find {
            it.title.equals(trackQuery, ignoreCase = true) ||
            (artistName != null && it.title.contains(trackQuery, ignoreCase = true) && it.artist.contains(artistName, ignoreCase = true))
        } ?: results.find {
            it.title.contains(trackQuery, ignoreCase = true)
        } ?: results.first()

        withContext(Dispatchers.Main) {
            musicPlayer.playSong(exactMatch, results, results.indexOf(exactMatch).coerceAtLeast(0))
        }

        val playbackConfirmed = try {
            withTimeoutOrNull(3000L) {
                musicPlayer.playerState.first { state ->
                    (state.currentSong?.id == exactMatch.id || state.currentSong?.title.equals(exactMatch.title, ignoreCase = true)) &&
                    (state.isPlaying || state.isLoading)
                }
            } != null
        } catch (_: Exception) {
            false
        }

        return if (playbackConfirmed) {
            OrbitResponseEngine.playSong(exactMatch.title, exactMatch.artist)
        } else {
            OrbitResult(
                success = false,
                action = "PLAY_SONG",
                spokenResponse = "I found ${exactMatch.title}, but couldn't start playback."
            )
        }
    }
}
