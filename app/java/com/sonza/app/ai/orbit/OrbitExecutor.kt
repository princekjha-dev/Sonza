package com.sonza.app.ai.orbit

import android.content.Context
import android.media.AudioManager
import com.sonza.app.core.domain.repository.LibraryRepository
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.RepeatMode
import com.sonza.app.core.model.Song
import com.sonza.app.data.repository.DownloadRepository
import com.sonza.app.data.repository.LyricsRepository
import com.sonza.app.data.repository.YouTubeRepository
import com.sonza.app.player.MusicPlayer
import com.sonza.app.player.SleepTimerManager
import com.sonza.app.player.SleepTimerOption
import com.sonza.app.service.DownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

/**
 * Result of an Orbit execution.
 */
data class OrbitExecutionResult(
    val success: Boolean,
    val message: String,
    val spokenResponse: String,
    val resultingState: OrbitState = if (success) OrbitState.SUCCESS else OrbitState.ERROR,
    val navigationTarget: String? = null,
    val expectsFollowUpVoiceInput: Boolean = false,
    val updatedContext: OrbitDialogueContext? = null,
    val actionIndicator: String? = null
)

/**
 * Executes Orbit Intents by operating directly on the authoritative Sonza playback engine.
 * Never creates duplicate playback state or fake mocks.
 */
class OrbitExecutor(
    private val musicPlayer: MusicPlayer,
    private val youTubeRepository: YouTubeRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val lyricsRepository: LyricsRepository,
    private val sleepTimerManager: SleepTimerManager,
    private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    suspend fun execute(
        intent: OrbitIntent,
        currentSong: Song?,
        dialogueContext: OrbitDialogueContext = OrbitDialogueContext.Idle
    ): OrbitExecutionResult = withContext(Dispatchers.IO) {
        try {
            when (intent) {
                // --- 0. Conversational Flow & Wake Word ---
                is OrbitIntent.WakeWordOnlyIntent -> {
                    OrbitExecutionResult(
                        success = true,
                        message = "Listening...",
                        spokenResponse = "Yes?",
                        resultingState = OrbitState.SPEAKING,
                        expectsFollowUpVoiceInput = true,
                        updatedContext = OrbitDialogueContext.AwaitingCommand()
                    )
                }

                is OrbitIntent.CancelIntent -> {
                    OrbitExecutionResult(
                        success = true,
                        message = "Orbit closed",
                        spokenResponse = "Alright",
                        resultingState = OrbitState.IDLE,
                        expectsFollowUpVoiceInput = false,
                        updatedContext = OrbitDialogueContext.Idle
                    )
                }

                is OrbitIntent.ConfirmationResponseIntent -> {
                    if (dialogueContext is OrbitDialogueContext.AwaitingConfirmation) {
                        if (intent.confirmed) {
                            val pName = dialogueContext.targetName
                            val allPlaylists = libraryRepository.getSavedPlaylists().first()
                            val target = allPlaylists.find { it.name.equals(pName, ignoreCase = true) }
                            if (target != null) {
                                libraryRepository.removePlaylist(target.id)
                                OrbitExecutionResult(
                                    success = true,
                                    message = "Deleted playlist $pName",
                                    spokenResponse = "Deleted playlist $pName.",
                                    actionIndicator = "🗑️",
                                    updatedContext = OrbitDialogueContext.Idle
                                )
                            } else {
                                OrbitExecutionResult(
                                    success = false,
                                    message = "Couldn't find playlist $pName",
                                    spokenResponse = "I couldn't find a playlist called $pName.",
                                    updatedContext = OrbitDialogueContext.Idle
                                )
                            }
                        } else {
                            OrbitExecutionResult(
                                success = true,
                                message = "Cancelled",
                                spokenResponse = "Alright, cancelled.",
                                updatedContext = OrbitDialogueContext.Idle
                            )
                        }
                    } else {
                        OrbitExecutionResult(
                            success = true,
                            message = "Understood",
                            spokenResponse = "Understood.",
                            updatedContext = OrbitDialogueContext.Idle
                        )
                    }
                }

                is OrbitIntent.DisambiguationChoiceIntent -> {
                    // Search for the choice
                    val query = intent.choice
                    resolveAndPlayTrack(query)
                }

                // --- 1. Playback Controls ---
                is OrbitIntent.PauseIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.pause() }
                    OrbitExecutionResult(true, "Playback paused", "Paused", actionIndicator = "⏸️")
                }
                is OrbitIntent.ResumeIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.play() }
                    OrbitExecutionResult(true, "Playback resumed", "Playing", actionIndicator = "▶️")
                }
                is OrbitIntent.StopIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.stop() }
                    OrbitExecutionResult(true, "Playback stopped", "Stopped", actionIndicator = "⏹️")
                }
                is OrbitIntent.NextTrackIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.seekToNext() }
                    OrbitExecutionResult(true, "Skipped to next song", "Next track", actionIndicator = "⏭️")
                }
                is OrbitIntent.PreviousTrackIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.seekToPrevious() }
                    OrbitExecutionResult(true, "Going back to previous song", "Previous track", actionIndicator = "⏮️")
                }
                is OrbitIntent.ReplayIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.seekTo(0L) }
                    OrbitExecutionResult(true, "Restarting song", "Replaying", actionIndicator = "🔄")
                }
                is OrbitIntent.SeekIntent -> {
                    val currentState = musicPlayer.playerState.value
                    val currentPos = currentState.currentPosition
                    val deltaMs = intent.seconds * 1000L
                    val targetPos = if (intent.isForward) currentPos + deltaMs else (currentPos - deltaMs).coerceAtLeast(0L)
                    withContext(Dispatchers.Main) { musicPlayer.seekTo(targetPos) }
                    val label = if (intent.isForward) "Skipped forward ${intent.seconds}s" else "Rewound ${intent.seconds}s"
                    OrbitExecutionResult(true, label, label, actionIndicator = "⏩")
                }

                // --- 2. Modes, Volume & Mute ---
                is OrbitIntent.ShuffleIntent -> {
                    val currentState = musicPlayer.playerState.value
                    if (currentState.shuffleEnabled != intent.enable) {
                        withContext(Dispatchers.Main) { musicPlayer.toggleShuffle() }
                    }
                    val msg = if (intent.enable) "Shuffle is on" else "Shuffle is off"
                    OrbitExecutionResult(true, msg, msg, actionIndicator = "🔀")
                }
                is OrbitIntent.RepeatIntent -> {
                    val currentState = musicPlayer.playerState.value
                    if (currentState.repeatMode != intent.mode) {
                        withContext(Dispatchers.Main) { musicPlayer.toggleRepeat() }
                    }
                    val msg = when (intent.mode) {
                        RepeatMode.ONE -> "Repeat current song is on"
                        RepeatMode.ALL -> "Repeat playlist is on"
                        RepeatMode.OFF -> "Repeat is off"
                    }
                    OrbitExecutionResult(true, msg, msg, actionIndicator = "🔁")
                }
                is OrbitIntent.MuteIntent -> {
                    if (intent.mute) {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, AudioManager.FLAG_SHOW_UI)
                        OrbitExecutionResult(true, "Muted", "Muted", actionIndicator = "🔇")
                    } else {
                        audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, AudioManager.FLAG_SHOW_UI)
                        OrbitExecutionResult(true, "Unmuted", "Unmuted", actionIndicator = "🔊")
                    }
                }
                is OrbitIntent.VolumeIntent -> {
                    when (intent.action) {
                        VolumeAction.UP -> {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                            OrbitExecutionResult(true, "Volume increased", "Volume up", actionIndicator = "🔊")
                        }
                        VolumeAction.DOWN -> {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                            OrbitExecutionResult(true, "Volume decreased", "Volume down", actionIndicator = "🔉")
                        }
                        VolumeAction.SET_LEVEL -> {
                            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val target = ((intent.levelPercent ?: 50) * max) / 100
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                            OrbitExecutionResult(true, "Volume set to ${intent.levelPercent}%", "Volume set to ${intent.levelPercent} percent.", actionIndicator = "🔊")
                        }
                    }
                }

                // --- 3. Exact Song Resolution & Search ---
                is OrbitIntent.PlayTrackIntent -> {
                    resolveAndPlayTrack(intent.trackName, intent.artistName)
                }

                is OrbitIntent.PlayArtistIntent -> {
                    val artist = intent.artistName
                    val mood = intent.moodOrGenre
                    if (mood != null) {
                        // User specified artist + mood (e.g. "Play Arijit Singh romantic songs")
                        val query = "$artist $mood songs"
                        val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                        if (results.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                musicPlayer.playSong(results.first(), results, 0)
                            }
                            OrbitExecutionResult(
                                success = true,
                                message = "Playing $mood $artist songs",
                                spokenResponse = "Playing $mood $artist songs.",
                                actionIndicator = "🎵"
                            )
                        } else {
                            OrbitExecutionResult(false, "Couldn't find $mood songs for $artist", "I couldn't find $mood songs by $artist.")
                        }
                    } else {
                        val topSongs = youTubeRepository.search("$artist popular songs", YouTubeRepository.FILTER_SONGS)
                        if (topSongs.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                musicPlayer.playSong(topSongs.first(), topSongs, 0)
                            }
                            OrbitExecutionResult(
                                success = true,
                                message = "Playing songs by $artist",
                                spokenResponse = "Sure. Playing songs by $artist.",
                                actionIndicator = "🎵"
                            )
                        } else {
                            resolveAndPlayTrack(artist)
                        }
                    }
                }

                is OrbitIntent.ModifyCurrentMixIntent -> {
                    val current = currentSong ?: musicPlayer.playerState.value.currentSong
                    val currentTitle = current?.title ?: "music"
                    val query = "$currentTitle ${intent.modifier} songs"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        val firstMatch = results.first()
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(firstMatch, results, 0)
                        }
                        OrbitExecutionResult(
                            success = true,
                            message = "Switching to a more ${intent.modifier} mix",
                            spokenResponse = "Sure. Switching to a more ${intent.modifier} mix.",
                            actionIndicator = "✨"
                        )
                    } else {
                        OrbitExecutionResult(false, "Couldn't find ${intent.modifier} mix", "I couldn't find a more ${intent.modifier} mix.")
                    }
                }

                is OrbitIntent.PlaySoundtrackIntent -> {
                    val movie = intent.movieOrShow
                    val query = "$movie songs soundtrack"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(results.first(), results, 0)
                        }
                        OrbitExecutionResult(
                            success = true,
                            message = "Playing songs from $movie",
                            spokenResponse = "Playing songs from $movie.",
                            actionIndicator = "🎬"
                        )
                    } else {
                        OrbitExecutionResult(false, "Couldn't find soundtrack for $movie", "I couldn't find songs from $movie.")
                    }
                }

                is OrbitIntent.PlayLanguageIntent -> {
                    val lang = intent.language
                    val query = "top trending $lang songs"
                    val results = youTubeRepository.search(query, YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(results.first(), results, 0)
                        }
                        OrbitExecutionResult(
                            success = true,
                            message = "Playing $lang songs",
                            spokenResponse = "Playing $lang songs.",
                            actionIndicator = "🌐"
                        )
                    } else {
                        OrbitExecutionResult(false, "Couldn't find $lang songs", "I couldn't find $lang songs.")
                    }
                }

                is OrbitIntent.PlayGenreOrMoodIntent -> {
                    val mood = intent.query
                    val results = youTubeRepository.search("$mood songs music", YouTubeRepository.FILTER_SONGS)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(results.first(), results, 0)
                        }
                        OrbitExecutionResult(
                            success = true,
                            message = "Playing ${mood.capitalize(Locale.ROOT)} songs",
                            spokenResponse = "Playing ${mood.capitalize(Locale.ROOT)} songs.",
                            actionIndicator = "✨"
                        )
                    } else {
                        OrbitExecutionResult(false, "Couldn't find $mood music", "I couldn't find music for $mood.")
                    }
                }

                is OrbitIntent.PlayAlbumIntent -> {
                    val results = youTubeRepository.searchAlbums(intent.albumName)
                    if (results.isNotEmpty()) {
                        val album = results.first()
                        val songs = youTubeRepository.search(album.title, YouTubeRepository.FILTER_SONGS)
                        if (songs.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                musicPlayer.playSong(songs.first(), songs, 0)
                            }
                            OrbitExecutionResult(true, "Playing album ${album.title}", "Playing album ${album.title}.", actionIndicator = "💿")
                        } else {
                            OrbitExecutionResult(false, "Album ${album.title} is empty", "Couldn't load album tracks.")
                        }
                    } else {
                        OrbitExecutionResult(false, "Album not found", "I couldn't find the album ${intent.albumName}.")
                    }
                }

                is OrbitIntent.PlayPlaylistIntent -> {
                    val allPlaylists = libraryRepository.getSavedPlaylists().first()
                    val local = allPlaylists.find { it.name.equals(intent.playlistName, ignoreCase = true) }
                    val localSongs = if (local != null) libraryRepository.getCachedPlaylistSongs(local.id) else emptyList()
                    if (local != null && localSongs.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(localSongs.first(), localSongs, 0)
                        }
                        OrbitExecutionResult(true, "Playing ${local.name}", "Playing ${local.name}.", actionIndicator = "🎶")
                    } else {
                        val remote = youTubeRepository.searchPlaylists(intent.playlistName)
                        if (remote.isNotEmpty()) {
                            val playlist = remote.first()
                            val songs = youTubeRepository.search(playlist.title, YouTubeRepository.FILTER_SONGS)
                            if (songs.isNotEmpty()) {
                                withContext(Dispatchers.Main) {
                                    musicPlayer.playSong(songs.first(), songs, 0)
                                }
                                OrbitExecutionResult(true, "Playing playlist ${playlist.title}", "Playing playlist ${playlist.title}.", actionIndicator = "🎶")
                            } else {
                                OrbitExecutionResult(false, "Playlist empty", "Couldn't load playlist.")
                            }
                        } else {
                            OrbitExecutionResult(false, "Playlist not found", "I couldn't find playlist ${intent.playlistName}.")
                        }
                    }
                }

                is OrbitIntent.SimilarMusicIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (song != null) {
                        val related = youTubeRepository.getRelatedSongs(song.id)
                        if (related.isNotEmpty()) {
                            withContext(Dispatchers.Main) {
                                musicPlayer.playSong(related.first(), related, 0)
                            }
                            OrbitExecutionResult(true, "Playing songs similar to ${song.title}", "Playing songs similar to ${song.title}.", actionIndicator = "🔮")
                        } else {
                            OrbitExecutionResult(false, "No similar songs found", "I couldn't find similar music.")
                        }
                    } else {
                        OrbitExecutionResult(false, "No song playing", "Play a song first to find similar music.")
                    }
                }

                is OrbitIntent.TrendingIntent -> {
                    val trending = youTubeRepository.search("trending songs", YouTubeRepository.FILTER_SONGS)
                    if (trending.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(trending.first(), trending, 0)
                        }
                        OrbitExecutionResult(true, "Playing trending songs", "Playing trending songs.", actionIndicator = "🔥")
                    } else {
                        OrbitExecutionResult(false, "Trending unavailable", "I couldn't load trending songs right now.")
                    }
                }

                is OrbitIntent.NewReleasesIntent -> {
                    val newSongs = youTubeRepository.search("latest new song releases", YouTubeRepository.FILTER_SONGS)
                    if (newSongs.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(newSongs.first(), newSongs, 0)
                        }
                        OrbitExecutionResult(true, "Playing new releases", "Playing new releases.", actionIndicator = "🚀")
                    } else {
                        OrbitExecutionResult(false, "New releases unavailable", "I couldn't load new releases right now.")
                    }
                }

                is OrbitIntent.SurpriseMeIntent -> {
                    val seeds = listOf("top hits", "chill vibe", "feel good", "acoustic", "lofi beats")
                    val seed = seeds.random()
                    val songs = youTubeRepository.search(seed, YouTubeRepository.FILTER_SONGS)
                    if (songs.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(songs.first(), songs, 0)
                        }
                        OrbitExecutionResult(true, "Playing ${songs.first().title}", "Surprise! Playing ${songs.first().title}.", actionIndicator = "🎁")
                    } else {
                        OrbitExecutionResult(false, "Failed to surprise", "Couldn't find surprise tracks.")
                    }
                }

                // --- 4. Favorites & Liked Songs ---
                is OrbitIntent.PlayFavoritesIntent -> {
                    val liked = youTubeRepository.getLikedMusic()
                    if (liked.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(liked.first(), liked, 0)
                        }
                        OrbitExecutionResult(true, "Playing Liked Songs", "Playing your liked songs.", actionIndicator = "❤️")
                    } else {
                        OrbitExecutionResult(false, "No liked songs found", "You haven't liked any songs yet.")
                    }
                }

                is OrbitIntent.FavoriteIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (song != null) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.updateLikeStatus(intent.isFavorite)
                        }
                        if (intent.isFavorite) {
                            libraryRepository.savePlaylist(
                                Playlist(id = "LM", title = "Liked Songs", author = "You", thumbnailUrl = song.thumbnailUrl, songs = emptyList())
                            )
                            libraryRepository.addSongToPlaylist("LM", song)
                            OrbitExecutionResult(true, "Added ${song.title} to Favorites", "Added to your favorites.", actionIndicator = "❤️")
                        } else {
                            libraryRepository.removeSongFromPlaylist("LM", song.id)
                            OrbitExecutionResult(true, "Removed ${song.title} from Favorites", "Removed from favorites.", actionIndicator = "🤍")
                        }
                    } else {
                        OrbitExecutionResult(false, "No song is currently playing", "No song is playing right now.")
                    }
                }

                // --- 5. Playlist Operations ---
                is OrbitIntent.AddToPlaylistIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (song != null) {
                        val allPlaylists = libraryRepository.getSavedPlaylists().first()
                        val target = allPlaylists.find { it.name.equals(intent.playlistName, ignoreCase = true) }
                        if (target != null) {
                            libraryRepository.addSongToPlaylist(target.id, song)
                            OrbitExecutionResult(true, "Added to ${target.name}", "Added to your ${target.name} playlist.", actionIndicator = "➕")
                        } else {
                            // Auto create playlist if not existing
                            val newId = "PL_${System.currentTimeMillis()}"
                            val newPlaylist = Playlist(id = newId, title = intent.playlistName, author = "You", thumbnailUrl = song.thumbnailUrl, songs = listOf(song))
                            libraryRepository.savePlaylist(newPlaylist)
                            libraryRepository.addSongToPlaylist(newId, song)
                            OrbitExecutionResult(true, "Created ${intent.playlistName} and added song", "Created playlist ${intent.playlistName} and added ${song.title}.", actionIndicator = "➕")
                        }
                    } else {
                        OrbitExecutionResult(false, "No song playing to add", "No song is currently playing.")
                    }
                }

                is OrbitIntent.RemoveFromPlaylistIntent -> {
                    val allPlaylists = libraryRepository.getSavedPlaylists().first()
                    val target = allPlaylists.find { it.name.equals(intent.playlistName, ignoreCase = true) }
                    if (target != null) {
                        val songToRemove = if (intent.trackName != null) {
                            val playlistSongs = libraryRepository.getCachedPlaylistSongs(target.id)
                            playlistSongs.find { it.title.contains(intent.trackName, ignoreCase = true) }
                        } else {
                            currentSong ?: musicPlayer.playerState.value.currentSong
                        }
                        if (songToRemove != null) {
                            libraryRepository.removeSongFromPlaylist(target.id, songToRemove.id)
                            OrbitExecutionResult(true, "Removed ${songToRemove.title} from ${target.name}", "Removed ${songToRemove.title} from ${target.name}.", actionIndicator = "➖")
                        } else {
                            OrbitExecutionResult(false, "Song not found in ${target.name}", "Couldn't find that song in ${target.name}.")
                        }
                    } else {
                        OrbitExecutionResult(false, "Playlist not found", "I couldn't find playlist ${intent.playlistName}.")
                    }
                }

                is OrbitIntent.CreatePlaylistIntent -> {
                    val newId = "PL_${System.currentTimeMillis()}"
                    val song = if (intent.addCurrentTrack) (currentSong ?: musicPlayer.playerState.value.currentSong) else null
                    val newPlaylist = Playlist(id = newId, title = intent.name, author = "You", thumbnailUrl = song?.thumbnailUrl, songs = if (song != null) listOf(song) else emptyList())
                    libraryRepository.savePlaylist(newPlaylist)
                    if (song != null) libraryRepository.addSongToPlaylist(newId, song)
                    val spoken = if (song != null) "Created playlist ${intent.name} and added ${song.title}." else "Created playlist ${intent.name}."
                    OrbitExecutionResult(true, "Created playlist ${intent.name}", spoken, actionIndicator = "📁")
                }

                is OrbitIntent.DeletePlaylistIntent -> {
                    if (!intent.confirmed) {
                        OrbitExecutionResult(
                            success = true,
                            message = "Delete playlist ${intent.playlistName}?",
                            spokenResponse = "Are you sure you want to delete the playlist ${intent.playlistName}?",
                            resultingState = OrbitState.SPEAKING,
                            expectsFollowUpVoiceInput = true,
                            updatedContext = OrbitDialogueContext.AwaitingConfirmation(
                                actionType = "DELETE_PLAYLIST",
                                targetName = intent.playlistName
                            )
                        )
                    } else {
                        val allPlaylists = libraryRepository.getSavedPlaylists().first()
                        val target = allPlaylists.find { it.name.equals(intent.playlistName, ignoreCase = true) }
                        if (target != null) {
                            libraryRepository.removePlaylist(target.id)
                            OrbitExecutionResult(true, "Deleted playlist ${intent.playlistName}", "Deleted playlist ${intent.playlistName}.", actionIndicator = "🗑️")
                        } else {
                            OrbitExecutionResult(false, "Playlist not found", "I couldn't find playlist ${intent.playlistName}.")
                        }
                    }
                }

                is OrbitIntent.ShowPlaylistsIntent -> {
                    OrbitExecutionResult(true, "Opening playlists", "Here are your playlists.", navigationTarget = "library")
                }

                // --- 6. Queue Management ---
                is OrbitIntent.QueueNextInfoIntent -> {
                    val pState = musicPlayer.playerState.value
                    val queue = pState.queue
                    val nextIdx = pState.currentIndex + 1
                    if (nextIdx in queue.indices) {
                        val nextSong = queue[nextIdx]
                        val spoken = "Next up is ${nextSong.title} by ${nextSong.artist}."
                        OrbitExecutionResult(true, spoken, spoken, actionIndicator = "🎶")
                    } else {
                        OrbitExecutionResult(true, "Your queue is empty", "Your queue is empty.", actionIndicator = "📭")
                    }
                }

                is OrbitIntent.QueueRemoveIndexIntent -> {
                    val pState = musicPlayer.playerState.value
                    val queue = pState.queue
                    val targetIdx = intent.indexOneBased - 1
                    if (targetIdx in queue.indices) {
                        val songToRemove = queue[targetIdx]
                        withContext(Dispatchers.Main) {
                            musicPlayer.removeFromQueue(listOf(targetIdx))
                        }
                        OrbitExecutionResult(true, "Removed ${songToRemove.title} from queue", "Removed ${songToRemove.title} from your queue.", actionIndicator = "🗑️")
                    } else {
                        OrbitExecutionResult(false, "Invalid queue item", "Song number ${intent.indexOneBased} isn't in your queue.")
                    }
                }

                is OrbitIntent.QueueAddIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (song != null) {
                        withContext(Dispatchers.Main) {
                            if (intent.playNext) {
                                musicPlayer.playNext(listOf(song))
                            } else {
                                musicPlayer.addToQueue(listOf(song))
                            }
                        }
                        val action = if (intent.playNext) "Will play ${song.title} next" else "Added ${song.title} to queue"
                        val spoken = if (intent.playNext) "Playing ${song.title} next." else "Added to your queue."
                        OrbitExecutionResult(true, action, spoken, actionIndicator = "📋")
                    } else {
                        OrbitExecutionResult(false, "No song selected", "Select a song to add to queue.")
                    }
                }

                is OrbitIntent.QueueClearIntent -> {
                    withContext(Dispatchers.Main) { musicPlayer.clearQueue() }
                    OrbitExecutionResult(true, "Queue cleared", "Queue cleared.", actionIndicator = "🧹")
                }

                is OrbitIntent.QueueShowIntent -> {
                    OrbitExecutionResult(true, "Showing queue", "Here is your queue.", navigationTarget = "queue", actionIndicator = "📋")
                }

                // --- 7. Song Info ---
                is OrbitIntent.SongInfoIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (song != null) {
                        val resp = when (intent.queryType) {
                            SongInfoType.ARTIST -> "${song.artist} sings this song."
                            SongInfoType.TITLE -> "You're listening to ${song.title} by ${song.artist}."
                            SongInfoType.ALBUM -> if (song.album.isNotBlank()) "This is from the album ${song.album}." else "Album info is unavailable for this song."
                            SongInfoType.DURATION -> "This track is ${song.duration / 60} minutes and ${song.duration % 60} seconds long."
                            SongInfoType.YEAR -> if (!song.releaseDate.isNullOrBlank()) "Released in ${song.releaseDate}." else "Release date is not specified."
                            else -> "You're listening to ${song.title} by ${song.artist}."
                        }
                        OrbitExecutionResult(true, resp, resp, OrbitState.SPEAKING, actionIndicator = "ℹ️")
                    } else {
                        OrbitExecutionResult(false, "No song currently playing", "No song is currently playing.")
                    }
                }

                // --- 8. Lyrics ---
                is OrbitIntent.LyricsIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (intent.show) {
                        if (song != null) {
                            val lyrics = lyricsRepository.getLyrics(song)
                            if (lyrics != null && lyrics.lines.isNotEmpty()) {
                                OrbitExecutionResult(true, "Showing lyrics for ${song.title}", "Showing lyrics for ${song.title}.", navigationTarget = "lyrics", actionIndicator = "📜")
                            } else {
                                OrbitExecutionResult(true, "No licensed lyrics found", "I couldn't find licensed lyrics for this song.", navigationTarget = "lyrics", actionIndicator = "📜")
                            }
                        } else {
                            OrbitExecutionResult(false, "No song playing", "No song is currently playing.")
                        }
                    } else {
                        OrbitExecutionResult(true, "Lyrics closed", "Closing lyrics.")
                    }
                }

                // --- 9. Sleep Timer ---
                is OrbitIntent.SleepTimerIntent -> {
                    if (intent.cancel) {
                        sleepTimerManager.cancelTimer()
                        OrbitExecutionResult(true, "Sleep timer canceled", "Sleep timer canceled.", actionIndicator = "⏱️")
                    } else {
                        val minutes = intent.minutes ?: 30
                        sleepTimerManager.startTimer(SleepTimerOption.CUSTOM, minutes)
                        OrbitExecutionResult(true, "Sleep timer set for $minutes minutes", "Sleep timer set for $minutes minutes.", actionIndicator = "⏱️")
                    }
                }

                // --- 10. Downloads ---
                is OrbitIntent.DownloadIntent -> {
                    when (intent.action) {
                        DownloadAction.CURRENT_SONG -> {
                            val song = currentSong ?: musicPlayer.playerState.value.currentSong
                            if (song != null) {
                                DownloadService.startDownload(context, song)
                                OrbitExecutionResult(true, "Downloading ${song.title}", "Downloading ${song.title}.", actionIndicator = "⬇️")
                            } else {
                                OrbitExecutionResult(false, "No song playing", "No song playing to download.")
                            }
                        }
                        DownloadAction.SHOW_DOWNLOADS -> {
                            OrbitExecutionResult(true, "Opening downloads", "Opening downloads.", navigationTarget = "downloads", actionIndicator = "⬇️")
                        }
                        else -> {
                            OrbitExecutionResult(false, "Offline downloads aren't available", "Offline downloads aren't available for this source.")
                        }
                    }
                }

                // --- 11. Navigation ---
                is OrbitIntent.NavigationIntent -> {
                    OrbitExecutionResult(true, "Opening ${intent.screen}", "Opening ${intent.screen}.", navigationTarget = intent.screen)
                }

                is OrbitIntent.HelpIntent -> {
                    val helpText = "You can ask me to play songs, artists, or moods. You can also control playback, manage your queue, and set sleep timers."
                    OrbitExecutionResult(true, helpText, helpText, OrbitState.SPEAKING)
                }

                // --- 12. Compound Intent ---
                is OrbitIntent.CompoundIntent -> {
                    val messages = mutableListOf<String>()
                    val spoken = mutableListOf<String>()
                    for (sub in intent.intents) {
                        val res = execute(sub, currentSong, dialogueContext)
                        messages.add(res.message)
                        spoken.add(res.spokenResponse)
                    }
                    OrbitExecutionResult(true, messages.joinToString(" • "), spoken.joinToString(". "))
                }

                // Fallback
                is OrbitIntent.UnknownIntent -> {
                    resolveAndPlayTrack(intent.rawQuery)
                }
            }
        } catch (e: Exception) {
            OrbitExecutionResult(false, "Something went wrong: ${e.message}", "Something went wrong while processing your request.")
        }
    }

    /**
     * Resolves the strongest exact match for a song title and artist,
     * then executes playback on the central MusicPlayer.
     */
    private suspend fun resolveAndPlayTrack(trackQuery: String, artistName: String? = null): OrbitExecutionResult {
        val fullQuery = if (artistName != null) "$trackQuery $artistName" else trackQuery
        val results = youTubeRepository.search(fullQuery, YouTubeRepository.FILTER_SONGS)

        if (results.isEmpty()) {
            return OrbitExecutionResult(
                success = false,
                message = "Couldn't find \"$fullQuery\"",
                spokenResponse = "I couldn't find that song. Try saying the song name and artist."
            )
        }

        // Exact song resolution
        val exactMatch = results.find {
            it.title.equals(trackQuery, ignoreCase = true) ||
            (artistName != null && it.title.contains(trackQuery, ignoreCase = true) && it.artist.contains(artistName, ignoreCase = true))
        } ?: results.find {
            it.title.contains(trackQuery, ignoreCase = true)
        } ?: results.first()

        withContext(Dispatchers.Main) {
            musicPlayer.playSong(exactMatch, results, results.indexOf(exactMatch).coerceAtLeast(0))
        }

        // Wait for playback state confirmation (up to 3 seconds)
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
            OrbitExecutionResult(
                success = true,
                message = "Playing ${exactMatch.title}",
                spokenResponse = "Playing ${exactMatch.title}.",
                actionIndicator = "🎵"
            )
        } else {
            OrbitExecutionResult(
                success = false,
                message = "Playback failed for ${exactMatch.title}",
                spokenResponse = "I found ${exactMatch.title}, but I couldn't start playback.",
                actionIndicator = "⚠️"
            )
        }
    }
}
