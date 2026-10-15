package com.sonza.app.glance

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.sonza.app.MainActivity
import com.sonza.app.core.domain.repository.LibraryRepository
import com.sonza.app.core.model.Playlist
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.SongSource
import com.sonza.app.data.repository.ListeningHistoryRepository
import com.sonza.app.data.repository.YouTubeRepository
import com.sonza.app.player.MusicPlayer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.firstOrNull

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharedWidgetEntryPoint {
    fun getMusicPlayer(): MusicPlayer
    fun getListeningHistoryRepository(): ListeningHistoryRepository
    fun getLibraryRepository(): LibraryRepository
    fun getYouTubeRepository(): YouTubeRepository
}

val DeepLinkKey = ActionParameters.Key<String>("deep_link_uri")
val QuickActionTypeKey = ActionParameters.Key<String>("quick_action_type")

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        val player = entryPoint.getMusicPlayer()
        val currentSong = player.playerState.value.currentSong

        if (currentSong != null) {
            player.togglePlayPause()
        } else {
            // Nothing currently playing: try to resume recently played track
            val history = entryPoint.getListeningHistoryRepository().getRecentlyPlayed(1).firstOrNull()
            val recent = history?.firstOrNull()
            if (recent != null) {
                val song = Song(
                    id = recent.songId,
                    title = recent.songTitle,
                    artist = recent.artist,
                    album = recent.album,
                    duration = recent.duration,
                    thumbnailUrl = recent.thumbnailUrl,
                    source = try { SongSource.valueOf(recent.source) } catch (_: Exception) { SongSource.YOUTUBE },
                    localUri = recent.localUri,
                    artistId = recent.artistId
                )
                player.playSong(song, listOf(song), 0)
            } else {
                // Open app
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
                context.startActivity(intent)
            }
        }
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class NextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().seekToNext()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class PreviousAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().seekToPrevious()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class ToggleShuffleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().toggleShuffle()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class ToggleRepeatAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().toggleRepeat()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class ToggleLikeAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        val player = entryPoint.getMusicPlayer()
        val currentSong = player.playerState.value.currentSong ?: return

        val newLikeState = !player.playerState.value.isLiked
        player.updateLikeStatus(newLikeState)

        // Persist to LibraryRepository ("LM" playlist)
        try {
            val libraryRepo = entryPoint.getLibraryRepository()
            if (newLikeState) {
                libraryRepo.savePlaylist(
                    Playlist(
                        id = "LM",
                        title = "Liked Songs",
                        author = "You",
                        thumbnailUrl = currentSong.thumbnailUrl,
                        songs = emptyList()
                    )
                )
                libraryRepo.addSongToPlaylist("LM", currentSong)
            } else {
                libraryRepo.removeSongFromPlaylist("LM", currentSong.id)
            }
        } catch (_: Exception) {}

        // Persist to listening history database
        try {
            val historyRepo = entryPoint.getListeningHistoryRepository()
            historyRepo.markSongAsLiked(currentSong, newLikeState)
        } catch (_: Exception) {}

        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class QuickActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val actionType = parameters[QuickActionTypeKey] ?: return
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        val player = entryPoint.getMusicPlayer()
        val libraryRepo = entryPoint.getLibraryRepository()
        val historyRepo = entryPoint.getListeningHistoryRepository()
        val ytRepo = entryPoint.getYouTubeRepository()

        when (actionType) {
            "liked" -> {
                var songs = libraryRepo.getCachedPlaylistSongs("LM")
                if (songs.isEmpty()) {
                    try {
                        songs = ytRepo.getLikedMusic()
                    } catch (_: Exception) {}
                }
                if (songs.isNotEmpty()) {
                    player.playSong(songs.first(), songs, 0)
                } else {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sonza://playlist/LM"), context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    }
                    context.startActivity(intent)
                }
            }
            "romantic" -> {
                val songs = try {
                    ytRepo.search("Romantic songs mix")
                } catch (_: Exception) { emptyList() }
                if (songs.isNotEmpty()) {
                    player.playSong(songs.first(), songs, 0)
                }
            }
            "workout" -> {
                val songs = try {
                    ytRepo.search("Workout gym music mix")
                } catch (_: Exception) { emptyList() }
                if (songs.isNotEmpty()) {
                    player.playSong(songs.first(), songs, 0)
                }
            }
            "discover" -> {
                val songs = try {
                    ytRepo.getRecommendations().ifEmpty {
                        ytRepo.search("Top trending hits")
                    }
                } catch (_: Exception) { emptyList() }
                if (songs.isNotEmpty()) {
                    player.playSong(songs.first(), songs, 0)
                }
            }
            "recent" -> {
                val recentHistory = historyRepo.getRecentlyPlayed(25).firstOrNull() ?: emptyList()
                val songs = recentHistory.map { h ->
                    Song(
                        id = h.songId,
                        title = h.songTitle,
                        artist = h.artist,
                        album = h.album,
                        duration = h.duration,
                        thumbnailUrl = h.thumbnailUrl,
                        source = try { SongSource.valueOf(h.source) } catch (_: Exception) { SongSource.YOUTUBE },
                        localUri = h.localUri,
                        artistId = h.artistId
                    )
                }
                if (songs.isNotEmpty()) {
                    player.playSong(songs.first(), songs, 0)
                }
            }
        }
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class OpenAppAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    }
}

class OpenDeepLinkAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val uriString = parameters[DeepLinkKey] ?: "sonza://player"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString), context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        context.startActivity(intent)
    }
}
