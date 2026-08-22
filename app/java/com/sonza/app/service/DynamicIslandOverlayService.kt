package com.sonza.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.sonza.app.MainActivity
import com.sonza.app.data.SessionManager
import com.sonza.app.data.repository.ListeningHistoryRepository
import com.sonza.app.player.MusicPlayer
import com.sonza.app.ui.components.dynamicisland.DynamicIsland
import com.sonza.app.ui.theme.SonzaTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * System-level Floating Dynamic Island Overlay Service.
 *
 * Appears floating over the Home screen and other applications when Sonza is backgrounded
 * while music is actively playing. Driven directly by the authoritative [MusicPlayer.playerState].
 */
@AndroidEntryPoint
class DynamicIslandOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    @Inject
    lateinit var musicPlayer: MusicPlayer

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var listeningHistoryRepository: ListeningHistoryRepository

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    private var windowManager: WindowManager? = null
    private var overlayComposeView: ComposeView? = null
    private var isViewAttached = false
    private var isExpanded = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        observePlaybackAndAppVisibility()
    }

    private fun observePlaybackAndAppVisibility() {
        serviceScope.launch {
            combine(
                sessionManager.dynamicIslandEnabledFlow,
                musicPlayer.playerState,
                isAppForeground.asStateFlow()
            ) { dynamicIslandEnabled, playerState, appForeground ->
                val canOverlay = canDrawOverlays()
                val hasSong = playerState.currentSong != null
                val shouldShowOverlay = dynamicIslandEnabled && canOverlay && hasSong && !appForeground
                shouldShowOverlay
            }.collect { shouldShow ->
                if (shouldShow) {
                    showOverlay()
                } else {
                    hideOverlay()
                }
            }
        }
    }

    private fun canDrawOverlays(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun showOverlay() {
        if (isViewAttached || windowManager == null || !canDrawOverlays()) return

        try {
            val composeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@DynamicIslandOverlayService)
                setViewTreeViewModelStoreOwner(this@DynamicIslandOverlayService)
                setViewTreeSavedStateRegistryOwner(this@DynamicIslandOverlayService)

                setContent {
                    SonzaTheme(darkTheme = true) {
                        val playerState by musicPlayer.playerState.collectAsState()
                        DynamicIsland(
                            currentSong = playerState.currentSong,
                            isPlaying = playerState.isPlaying,
                            isLoading = playerState.isLoading,
                            currentPosition = playerState.currentPosition,
                            duration = playerState.duration,
                            isLiked = playerState.isLiked,
                            onPlayPause = { musicPlayer.togglePlayPause() },
                            onNext = { musicPlayer.seekToNext() },
                            onPrevious = { musicPlayer.seekToPrevious() },
                            onSeekTo = { musicPlayer.seekTo(it) },
                            onLikeToggle = {
                                val song = playerState.currentSong ?: return@DynamicIsland
                                serviceScope.launch {
                                    val currentLiked = playerState.isLiked
                                    listeningHistoryRepository.markSongAsLiked(song, !currentLiked)
                                }
                            },
                            onOpenApp = {
                                val intent = Intent(this@DynamicIslandOverlayService, MainActivity::class.java).apply {
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                }
                                startActivity(intent)
                            },
                            onExpandChange = { expanded ->
                                isExpanded = expanded
                                updateOverlayLayoutParams()
                            },
                            modifier = Modifier.fillMaxSize(),
                            topInset = 0.dp
                        )
                    }
                }
            }

            val params = createLayoutParams(isExpanded = false)
            windowManager?.addView(composeView, params)
            overlayComposeView = composeView
            isViewAttached = true
        } catch (e: Exception) {
            android.util.Log.e("DynamicIslandOverlay", "Failed to add overlay view to WindowManager", e)
            isViewAttached = false
        }
    }

    private fun updateOverlayLayoutParams() {
        if (!isViewAttached || overlayComposeView == null || windowManager == null) return
        try {
            val params = createLayoutParams(isExpanded)
            windowManager?.updateViewLayout(overlayComposeView, params)
        } catch (e: Exception) {
            android.util.Log.e("DynamicIslandOverlay", "Failed to update layout params", e)
        }
    }

    private fun createLayoutParams(isExpanded: Boolean): WindowManager.LayoutParams {
        val windowType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val flags = if (isExpanded) {
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        } else {
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }

        return WindowManager.LayoutParams(
            if (isExpanded) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
            if (isExpanded) WindowManager.LayoutParams.MATCH_PARENT else WindowManager.LayoutParams.WRAP_CONTENT,
            windowType,
            flags,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
            y = 0
        }
    }

    private fun hideOverlay() {
        if (!isViewAttached || overlayComposeView == null || windowManager == null) return
        try {
            windowManager?.removeView(overlayComposeView)
        } catch (e: Exception) {
            android.util.Log.e("DynamicIslandOverlay", "Failed to remove overlay view", e)
        } finally {
            overlayComposeView = null
            isViewAttached = false
            isExpanded = false
        }
    }

    override fun onDestroy() {
        hideOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        private val isAppForeground = MutableStateFlow(true)

        fun setAppForeground(foreground: Boolean) {
            isAppForeground.value = foreground
        }

        fun start(context: Context) {
            try {
                val intent = Intent(context, DynamicIslandOverlayService::class.java)
                context.startService(intent)
            } catch (e: Exception) {
                android.util.Log.e("DynamicIslandOverlay", "Could not start DynamicIslandOverlayService", e)
            }
        }

        fun stop(context: Context) {
            try {
                val intent = Intent(context, DynamicIslandOverlayService::class.java)
                context.stopService(intent)
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
