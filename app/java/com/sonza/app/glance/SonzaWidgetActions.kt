package com.sonza.app.glance

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import com.sonza.app.MainActivity
import com.sonza.app.player.MusicPlayer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SharedWidgetEntryPoint {
    fun getMusicPlayer(): MusicPlayer
}

val DeepLinkKey = ActionParameters.Key<String>("deep_link_uri")

class PlayPauseAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().togglePlayPause()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class NextAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().seekToNext()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class PreviousAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().seekToPrevious()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class ToggleShuffleAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().toggleShuffle()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class ToggleRepeatAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context, SharedWidgetEntryPoint::class.java)
        entryPoint.getMusicPlayer().toggleRepeat()
        SonzaWidgetManager.updateAllWidgets(context)
    }
}

class ToggleLikeAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val entryPoint = EntryPointAccessors.fromApplication(context, SharedWidgetEntryPoint::class.java)
        val player = entryPoint.getMusicPlayer()
        val currentSong = player.playerState.value.currentSong
        if (currentSong != null) {
            val isLiked = player.playerState.value.isLiked
            player.updateLikeStatus(!isLiked)
            SonzaWidgetManager.updateAllWidgets(context)
        }
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
