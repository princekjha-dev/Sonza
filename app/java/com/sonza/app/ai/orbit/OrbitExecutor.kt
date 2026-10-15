package com.sonza.app.ai.orbit

import android.content.Context
import android.media.AudioManager
import com.sonza.app.core.domain.repository.LibraryRepository
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.RepeatMode
import com.sonza.app.core.model.Song
import com.sonza.app.data.repository.DownloadRepository
import com.sonza.app.data.repository.YouTubeRepository
import com.sonza.app.player.MusicPlayer
import com.sonza.app.player.SleepTimerManager
import com.sonza.app.player.SleepTimerOption
import com.sonza.app.service.DownloadService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of an Orbit execution.
 */
data class OrbitExecutionResult(
    val success: Boolean,
    val message: String,
    val spokenResponse: String,
    val resultingState: OrbitState = if (success) OrbitState.SUCCESS else OrbitState.ERROR,
    val navigationTarget: String? = null
)

/**
 * Executes Orbit Intents by operating directly on REAL Sonza systems.
 * NEVER creates duplicate playback or database instances.
 */
@Singleton
class OrbitExecutor @Inject constructor(
    private val musicPlayer: MusicPlayer,
    private val youTubeRepository: YouTubeRepository,
    private val libraryRepository: LibraryRepository,
    private val downloadRepository: DownloadRepository,
    private val sleepTimerManager: SleepTimerManager,
    private val context: Context
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    suspend fun execute(intent: OrbitIntent, currentSong: Song?): OrbitExecutionResult = withContext(Dispatchers.IO) {
        try {
            when (intent) {
                // --- 1. Playback Controls ---
                is OrbitIntent.PauseIntent -> {
                    musicPlayer.pause()
                    OrbitExecutionResult(true, "Playback paused", "Paused")
                }
                is OrbitIntent.ResumeIntent -> {
                    musicPlayer.play()
                    OrbitExecutionResult(true, "Playback resumed", "Playing")
                }
                is OrbitIntent.StopIntent -> {
                    musicPlayer.stop()
                    OrbitExecutionResult(true, "Playback stopped", "Stopped")
                }
                is OrbitIntent.NextTrackIntent -> {
                    musicPlayer.seekToNext()
                    OrbitExecutionResult(true, "Skipped to next song", "Next track")
                }
                is OrbitIntent.PreviousTrackIntent -> {
                    musicPlayer.seekToPrevious()
                    OrbitExecutionResult(true, "Going back to previous song", "Previous track")
                }
                is OrbitIntent.ReplayIntent -> {
                    musicPlayer.seekTo(0L)
                    OrbitExecutionResult(true, "Restarting song", "Replaying")
                }
                is OrbitIntent.SeekIntent -> {
                    val currentState = musicPlayer.playerState.value
                    val currentPos = currentState.currentPosition
                    val deltaMs = intent.seconds * 1000L
                    val targetPos = if (intent.isForward) currentPos + deltaMs else (currentPos - deltaMs).coerceAtLeast(0L)
                    musicPlayer.seekTo(targetPos)
                    val label = if (intent.isForward) "Skipped forward ${intent.seconds}s" else "Rewound ${intent.seconds}s"
                    OrbitExecutionResult(true, label, label)
                }

                // --- 2. Modes & Volume ---
                is OrbitIntent.ShuffleIntent -> {
                    val currentState = musicPlayer.playerState.value
                    if (currentState.shuffleEnabled != intent.enable) {
                        musicPlayer.toggleShuffle()
                    }
                    val msg = if (intent.enable) "Shuffle enabled" else "Shuffle disabled"
                    OrbitExecutionResult(true, msg, msg)
                }
                is OrbitIntent.RepeatIntent -> {
                    val currentState = musicPlayer.playerState.value
                    if (currentState.repeatMode != intent.mode) {
                        musicPlayer.toggleRepeat()
                    }
                    val msg = "Repeat mode: ${intent.mode.name.lowercase()}"
                    OrbitExecutionResult(true, msg, msg)
                }
                is OrbitIntent.VolumeIntent -> {
                    when (intent.action) {
                        VolumeAction.UP -> {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                            OrbitExecutionResult(true, "Volume raised", "Volume up")
                        }
                        VolumeAction.DOWN -> {
                            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                            OrbitExecutionResult(true, "Volume lowered", "Volume down")
                        }
                        VolumeAction.SET_LEVEL -> {
                            val max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                            val target = ((intent.levelPercent ?: 50) * max) / 100
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, target, AudioManager.FLAG_SHOW_UI)
                            OrbitExecutionResult(true, "Volume set to ${intent.levelPercent}%", "Volume set")
                        }
                    }
                }

                // --- 3. Favorites & Liked Songs ---
                is OrbitIntent.PlayFavoritesIntent -> {
                    val liked = youTubeRepository.getLikedMusic()
                    if (liked.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(liked.first(), liked, 0)
                        }
                        OrbitExecutionResult(true, "Playing Liked Songs", "Playing your liked songs")
                    } else {
                        OrbitExecutionResult(false, "No liked songs found", "You haven't liked any songs yet")
                    }
                }
                is OrbitIntent.FavoriteIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (song != null) {
                        musicPlayer.updateLikeStatus(intent.isFavorite)
                        if (intent.isFavorite) {
                            libraryRepository.savePlaylist(
                                Playlist(id = "LM", title = "Liked Songs", author = "You", thumbnailUrl = song.thumbnailUrl, songs = emptyList())
                            )
                            libraryRepository.addSongToPlaylist("LM", song)
                            OrbitExecutionResult(true, "Added ${song.title} to Liked Songs", "Added to favorites")
                        } else {
                            libraryRepository.removeSongFromPlaylist("LM", song.id)
                            OrbitExecutionResult(true, "Removed ${song.title} from Liked Songs", "Removed from favorites")
                        }
                    } else {
                        OrbitExecutionResult(false, "No song is currently playing", "No song playing")
                    }
                }

                // --- 4. Search & Discovery ---
                is OrbitIntent.PlayTrackIntent -> {
                    val query = if (intent.artistName != null) "${intent.trackName} ${intent.artistName}" else intent.trackName
                    val results = youTubeRepository.search(query)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(results.first(), results, 0)
                        }
                        OrbitExecutionResult(true, "Playing ${results.first().title}", "Playing ${results.first().title}")
                    } else {
                        OrbitExecutionResult(false, "Couldn't find \"$query\"", "No results for $query")
                    }
                }
                is OrbitIntent.PlayArtistIntent -> {
                    val results = youTubeRepository.search(intent.artistName)
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(results.first(), results, 0)
                        }
                        OrbitExecutionResult(true, "Playing songs by ${intent.artistName}", "Playing ${intent.artistName}")
                    } else {
                        OrbitExecutionResult(false, "Couldn't find artist \"${intent.artistName}\"", "Artist not found")
                    }
                }
                is OrbitIntent.PlayGenreOrMoodIntent -> {
                    val results = youTubeRepository.search("${intent.query} music songs")
                    if (results.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(results.first(), results, 0)
                        }
                        OrbitExecutionResult(true, "Playing ${intent.query.capitalize()} mix", "Playing ${intent.query} music")
                    } else {
                        OrbitExecutionResult(false, "Couldn't find ${intent.query} music", "No music found for ${intent.query}")
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
                            OrbitExecutionResult(true, "Playing songs similar to ${song.title}", "Playing similar music")
                        } else {
                            OrbitExecutionResult(false, "No similar songs found", "Couldn't find similar music")
                        }
                    } else {
                        OrbitExecutionResult(false, "Play a song first to find similar tracks", "Play a song first")
                    }
                }
                is OrbitIntent.TrendingIntent -> {
                    val trending = youTubeRepository.search("trending songs")
                    if (trending.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(trending.first(), trending, 0)
                        }
                        OrbitExecutionResult(true, "Playing trending songs", "Playing trending tracks")
                    } else {
                        OrbitExecutionResult(false, "Couldn't load trending music", "Trending music unavailable")
                    }
                }
                is OrbitIntent.NewReleasesIntent -> {
                    val newSongs = youTubeRepository.search("latest new releases")
                    if (newSongs.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(newSongs.first(), newSongs, 0)
                        }
                        OrbitExecutionResult(true, "Playing new releases", "Playing new releases")
                    } else {
                        OrbitExecutionResult(false, "Couldn't load new releases", "New releases unavailable")
                    }
                }
                is OrbitIntent.SurpriseMeIntent -> {
                    val seeds = listOf("top hits", "chill vibe", "feel good", "acoustic", "lofi beats")
                    val seed = seeds.random()
                    val songs = youTubeRepository.search(seed)
                    if (songs.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(songs.first(), songs, 0)
                        }
                        OrbitExecutionResult(true, "Here is something you might like: ${songs.first().title}", "Surprise! Playing ${songs.first().title}")
                    } else {
                        OrbitExecutionResult(false, "Couldn't find surprise tracks", "Failed to surprise")
                    }
                }

                // --- 5. Song Info ---
                is OrbitIntent.SongInfoIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (song != null) {
                        val resp = when (intent.queryType) {
                            SongInfoType.ARTIST -> "${song.artist} sings this song."
                            SongInfoType.TITLE -> "This is \"${song.title}\" by ${song.artist}."
                            SongInfoType.ALBUM -> if (song.album.isNotBlank()) "This is from the album ${song.album}." else "Album info unavailable."
                            SongInfoType.DURATION -> "This track is ${song.duration / 60} minutes and ${song.duration % 60} seconds long."
                            SongInfoType.YEAR -> if (!song.releaseDate.isNullOrBlank()) "Released in ${song.releaseDate}." else "Release date not specified."
                            else -> "\"${song.title}\" by ${song.artist}."
                        }
                        OrbitExecutionResult(true, resp, resp, OrbitState.SPEAKING)
                    } else {
                        OrbitExecutionResult(false, "No song is currently playing", "No song playing")
                    }
                }

                // --- 6. Sleep Timer ---
                is OrbitIntent.SleepTimerIntent -> {
                    if (intent.cancel) {
                        sleepTimerManager.cancelTimer()
                        OrbitExecutionResult(true, "Sleep timer canceled", "Sleep timer canceled")
                    } else {
                        val minutes = intent.minutes ?: 30
                        sleepTimerManager.startTimer(SleepTimerOption.CUSTOM, minutes)
                        OrbitExecutionResult(true, "Sleep timer set for $minutes minutes", "Sleep timer set for $minutes minutes")
                    }
                }

                // --- 7. Queue Operations ---
                is OrbitIntent.QueueAddIntent -> {
                    val song = currentSong ?: musicPlayer.playerState.value.currentSong
                    if (song != null) {
                        if (intent.playNext) {
                            musicPlayer.playNext(song)
                            OrbitExecutionResult(true, "Will play ${song.title} next", "Playing next")
                        } else {
                            musicPlayer.addToQueue(song)
                            OrbitExecutionResult(true, "Added ${song.title} to queue", "Added to queue")
                        }
                    } else {
                        OrbitExecutionResult(false, "No song selected", "Select a song first")
                    }
                }
                is OrbitIntent.QueueClearIntent -> {
                    musicPlayer.clearQueue()
                    OrbitExecutionResult(true, "Queue cleared", "Queue cleared")
                }
                is OrbitIntent.QueueShowIntent -> {
                    OrbitExecutionResult(true, "Showing queue", "Here is your queue", navigationTarget = "queue")
                }

                // --- 8. Lyrics & Navigation ---
                is OrbitIntent.LyricsIntent -> {
                    OrbitExecutionResult(true, if (intent.show) "Opening lyrics" else "Hiding lyrics", if (intent.show) "Showing lyrics" else "Hiding lyrics", navigationTarget = if (intent.show) "lyrics" else null)
                }
                is OrbitIntent.NavigationIntent -> {
                    OrbitExecutionResult(true, "Opening ${intent.screen}", "Opening ${intent.screen}", navigationTarget = intent.screen)
                }

                // --- 9. Downloads ---
                is OrbitIntent.DownloadIntent -> {
                    when (intent.action) {
                        DownloadAction.CURRENT_SONG -> {
                            val song = currentSong ?: musicPlayer.playerState.value.currentSong
                            if (song != null) {
                                DownloadService.startDownload(context, song)
                                OrbitExecutionResult(true, "Starting download for ${song.title}", "Downloading ${song.title}")
                            } else {
                                OrbitExecutionResult(false, "No song playing to download", "No song to download")
                            }
                        }
                        DownloadAction.SHOW_DOWNLOADS -> {
                            OrbitExecutionResult(true, "Opening downloads", "Opening downloads", navigationTarget = "downloads")
                        }
                        else -> OrbitExecutionResult(false, "Download action not supported", "Action not supported")
                    }
                }

                // --- 10. Compound Intent ---
                is OrbitIntent.CompoundIntent -> {
                    val results = mutableListOf<String>()
                    for (subIntent in intent.intents) {
                        val subResult = execute(subIntent, currentSong)
                        results.add(subResult.message)
                    }
                    val combined = results.joinToString(" • ")
                    OrbitExecutionResult(true, combined, results.firstOrNull() ?: "Done")
                }

                is OrbitIntent.HelpIntent -> {
                    OrbitExecutionResult(true, "You can say: \"Play Arijit Singh\", \"Shuffle on\", \"Sleep timer 30 mins\", \"Who sings this?\"", "How can I help with your music?", OrbitState.SPEAKING)
                }
                is OrbitIntent.UnknownIntent -> {
                    // Try general search as fallback
                    val search = youTubeRepository.search(intent.rawQuery)
                    if (search.isNotEmpty()) {
                        withContext(Dispatchers.Main) {
                            musicPlayer.playSong(search.first(), search, 0)
                        }
                        OrbitExecutionResult(true, "Playing ${search.first().title}", "Playing ${search.first().title}")
                    } else {
                        OrbitExecutionResult(false, "I couldn't understand: \"${intent.rawQuery}\"", "I didn't catch that, could you repeat?")
                    }
                }
                else -> {
                    OrbitExecutionResult(false, "Action not recognized", "Sorry, I can't do that yet")
                }
            }
        } catch (e: Exception) {
            OrbitExecutionResult(false, "Failed: ${e.message}", "Something went wrong")
        }
    }
}
