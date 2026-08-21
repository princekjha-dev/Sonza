package com.sonza.app.ui.components

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.view.Surface
import android.view.TextureView
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sonza.app.R

/**
 * Official Sonza Video Loading Indicator.
 *
 * Plays `loding.mp4` continuously in a hardware-accelerated TextureView.
 * - Hardware accelerated, transparent backdrop (no black bars/flash)
 * - Automatic looping and immediate start from frame 0
 * - Strictly lifecycle-aware: pauses when offscreen/backgrounded, releases on dispose
 * - Zero audio interference (silent volume = 0f)
 * - Fixed 1:1 aspect ratio matching the 200x200 source video
 */
@Composable
fun SonzaVideoLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isVideoReady by remember { mutableStateOf(false) }

    // Smooth fade-in once first frame is ready to avoid black flash
    val alphaAnim by animateFloatAsState(
        targetValue = if (isVideoReady) 1f else 0f,
        animationSpec = tween(durationMillis = 150),
        label = "sonzaVideoLoadingFade"
    )

    val mediaPlayerHolder = remember { arrayOfNulls<MediaPlayer>(1) }

    // Sync with Activity Lifecycle
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val player = mediaPlayerHolder[0] ?: return@LifecycleEventObserver
            when (event) {
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
                    try {
                        if (player.isPlaying) {
                            player.pause()
                        }
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_RESUME -> {
                    try {
                        if (!player.isPlaying) {
                            player.start()
                        }
                    } catch (_: Exception) {}
                }
                Lifecycle.Event.ON_DESTROY -> {
                    try {
                        player.stop()
                        player.release()
                    } catch (_: Exception) {}
                    mediaPlayerHolder[0] = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mediaPlayerHolder[0]?.let { player ->
                    if (player.isPlaying) {
                        player.stop()
                    }
                    player.release()
                }
            } catch (_: Exception) {}
            mediaPlayerHolder[0] = null
        }
    }

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .alpha(alphaAnim),
            factory = { ctx ->
                TextureView(ctx).apply {
                    isOpaque = false
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                        override fun onSurfaceTextureAvailable(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {
                            val surface = Surface(surfaceTexture)
                            try {
                                val player = MediaPlayer().apply {
                                    setSurface(surface)
                                    setVolume(0f, 0f)
                                    isLooping = true
                                    val afd = ctx.resources.openRawResourceFd(R.raw.loding)
                                    setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                                    afd.close()
                                    setOnPreparedListener { mp ->
                                        mp.start()
                                        isVideoReady = true
                                    }
                                    setOnInfoListener { _, what, _ ->
                                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                                            isVideoReady = true
                                        }
                                        false
                                    }
                                    setOnErrorListener { mp, _, _ ->
                                        mp.reset()
                                        true
                                    }
                                    prepareAsync()
                                }
                                mediaPlayerHolder[0] = player
                            } catch (_: Exception) {
                                isVideoReady = true
                            }
                        }

                        override fun onSurfaceTextureSizeChanged(
                            surfaceTexture: SurfaceTexture,
                            width: Int,
                            height: Int
                        ) {}

                        override fun onSurfaceTextureDestroyed(surfaceTexture: SurfaceTexture): Boolean {
                            try {
                                mediaPlayerHolder[0]?.let { player ->
                                    if (player.isPlaying) {
                                        player.stop()
                                    }
                                    player.release()
                                }
                            } catch (_: Exception) {}
                            mediaPlayerHolder[0] = null
                            surfaceTexture.release()
                            return true
                        }

                        override fun onSurfaceTextureUpdated(surfaceTexture: SurfaceTexture) {}
                    }
                }
            }
        )
    }
}

/**
 * Convenience overload accepting fixed Dp dimensions.
 */
@Composable
fun SonzaVideoLoadingIndicator(
    size: Dp,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    SonzaVideoLoadingIndicator(
        modifier = modifier.size(size),
        color = color
    )
}
