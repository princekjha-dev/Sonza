package com.sonza.app.glance

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.toBitmap
import com.sonza.app.R
import dagger.hilt.android.EntryPointAccessors

class SonzaLargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaLargeWidget()
}

/**
 * Widget C — Large Music + Orbit Widget
 */
class SonzaLargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        val musicPlayer = entryPoint.getMusicPlayer()

        provideContent {
            GlanceTheme {
                val playerState by musicPlayer.playerState.collectAsState()
                val currentSong = playerState.currentSong

                var artworkBitmap by remember(currentSong?.id) { mutableStateOf<Bitmap?>(null) }

                DisposableEffect(currentSong?.id) {
                    onDispose {
                        artworkBitmap?.let { bmp -> if (!bmp.isRecycled) bmp.recycle() }
                        artworkBitmap = null
                    }
                }

                LaunchedEffect(currentSong?.id) {
                    if (currentSong?.thumbnailUrl != null) {
                        try {
                            val request = ImageRequest.Builder(context)
                                .data(currentSong.thumbnailUrl)
                                .size(320, 320)
                                .allowHardware(false)
                                .build()
                            val result = context.imageLoader.execute(request)
                            if (result is SuccessResult) {
                                val shared = result.image.toBitmap()
                                artworkBitmap = shared.copy(Bitmap.Config.ARGB_8888, false)
                            }
                        } catch (_: Exception) {}
                    }
                }

                val domColor = playerState.dominantColor
                val backgroundColor = if (domColor != -16777216 && domColor != 0) {
                    Color(domColor).copy(alpha = 0.94f)
                } else {
                    Color(0xFF14151E).copy(alpha = 0.97f)
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(backgroundColor))
                        .cornerRadius(28.dp)
                        .clickable(
                            actionRunCallback<OpenDeepLinkAction>(
                                actionParametersOf(DeepLinkKey to "sonza://player")
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        // Top Orbit Assistant Header
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .cornerRadius(16.dp)
                                .background(ColorProvider(Color.White.copy(alpha = 0.08f)))
                                .clickable(
                                    actionRunCallback<OpenDeepLinkAction>(
                                        actionParametersOf(DeepLinkKey to "sonza://orbit_listen")
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Orbit Avatar icon
                            Box(
                                modifier = GlanceModifier
                                    .size(28.dp)
                                    .cornerRadius(14.dp)
                                    .background(ColorProvider(Color(0xFF4F86F7).copy(alpha = 0.35f))),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_mic),
                                    contentDescription = "Orbit",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF4F86F7))),
                                    modifier = GlanceModifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(10.dp))

                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = "ORBIT",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF4F86F7)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "What would you like to listen to?",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White.copy(alpha = 0.85f)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(12.dp))

                        // Middle Section: Large Album Art + Info
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = GlanceModifier
                                    .size(80.dp)
                                    .cornerRadius(16.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.12f))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (artworkBitmap != null) {
                                    Image(
                                        provider = ImageProvider(artworkBitmap!!),
                                        contentDescription = "Artwork",
                                        contentScale = ContentScale.Crop,
                                        modifier = GlanceModifier.fillMaxSize().cornerRadius(16.dp)
                                    )
                                } else {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_music_note),
                                        contentDescription = "Artwork",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White.copy(alpha = 0.6f))),
                                        modifier = GlanceModifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(14.dp))

                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = currentSong?.title ?: "Not Playing",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = GlanceModifier.height(3.dp))
                                Text(
                                    text = currentSong?.artist ?: "Sonza Music",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White.copy(alpha = 0.75f)),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )

                                Spacer(modifier = GlanceModifier.height(8.dp))

                                // Quick deep link buttons: Lyrics & Queue
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Lyrics
                                    Box(
                                        modifier = GlanceModifier
                                            .cornerRadius(12.dp)
                                            .background(ColorProvider(Color.White.copy(alpha = 0.12f)))
                                            .clickable(
                                                actionRunCallback<OpenDeepLinkAction>(
                                                    actionParametersOf(DeepLinkKey to "sonza://lyrics")
                                                )
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Lyrics",
                                            style = TextStyle(
                                                color = ColorProvider(Color.White),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }

                                    Spacer(modifier = GlanceModifier.width(8.dp))

                                    // Queue
                                    Box(
                                        modifier = GlanceModifier
                                            .cornerRadius(12.dp)
                                            .background(ColorProvider(Color.White.copy(alpha = 0.12f)))
                                            .clickable(
                                                actionRunCallback<OpenDeepLinkAction>(
                                                    actionParametersOf(DeepLinkKey to "sonza://queue")
                                                )
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "Queue",
                                            style = TextStyle(
                                                color = ColorProvider(Color.White),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }

                            // Favorite Toggle
                            Image(
                                provider = ImageProvider(
                                    if (playerState.isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
                                ),
                                contentDescription = "Favorite",
                                colorFilter = ColorFilter.tint(
                                    ColorProvider(if (playerState.isLiked) Color(0xFFFF5252) else Color.White.copy(alpha = 0.7f))
                                ),
                                modifier = GlanceModifier
                                    .size(26.dp)
                                    .clickable(actionRunCallback<ToggleLikeAction>())
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Progress Indicator line
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(3.5.dp)
                                .background(ColorProvider(Color.White.copy(alpha = 0.18f)))
                                .cornerRadius(2.dp)
                        ) {}

                        Spacer(modifier = GlanceModifier.height(12.dp))

                        // Playback Controls Row
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Shuffle
                            Image(
                                provider = ImageProvider(
                                    if (playerState.shuffleEnabled) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle
                                ),
                                contentDescription = "Shuffle",
                                colorFilter = ColorFilter.tint(
                                    ColorProvider(if (playerState.shuffleEnabled) Color(0xFF4F86F7) else Color.White.copy(alpha = 0.65f))
                                ),
                                modifier = GlanceModifier
                                    .size(24.dp)
                                    .clickable(actionRunCallback<ToggleShuffleAction>())
                            )

                            Spacer(modifier = GlanceModifier.width(24.dp))

                            // Previous
                            Image(
                                provider = ImageProvider(R.drawable.ic_skip_previous),
                                contentDescription = "Previous",
                                colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                modifier = GlanceModifier
                                    .size(32.dp)
                                    .clickable(actionRunCallback<PreviousAction>())
                            )

                            Spacer(modifier = GlanceModifier.width(24.dp))

                            // Play/Pause
                            Box(
                                modifier = GlanceModifier
                                    .size(52.dp)
                                    .cornerRadius(26.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
                                    .clickable(actionRunCallback<PlayPauseAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(
                                        if (playerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                    ),
                                    contentDescription = "Play/Pause",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(30.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(24.dp))

                            // Next
                            Image(
                                provider = ImageProvider(R.drawable.ic_skip_next),
                                contentDescription = "Next",
                                colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                modifier = GlanceModifier
                                    .size(32.dp)
                                    .clickable(actionRunCallback<NextAction>())
                            )

                            Spacer(modifier = GlanceModifier.width(24.dp))

                            // Repeat
                            Image(
                                provider = ImageProvider(
                                    when (playerState.repeatMode) {
                                        com.sonza.app.core.model.RepeatMode.ONE -> R.drawable.ic_repeat_one_on
                                        com.sonza.app.core.model.RepeatMode.ALL -> R.drawable.ic_repeat_all_on
                                        com.sonza.app.core.model.RepeatMode.OFF -> R.drawable.ic_repeat
                                    }
                                ),
                                contentDescription = "Repeat",
                                colorFilter = ColorFilter.tint(
                                    ColorProvider(if (playerState.repeatMode != com.sonza.app.core.model.RepeatMode.OFF) Color(0xFF4F86F7) else Color.White.copy(alpha = 0.65f))
                                ),
                                modifier = GlanceModifier
                                    .size(24.dp)
                                    .clickable(actionRunCallback<ToggleRepeatAction>())
                            )
                        }
                    }
                }
            }
        }
    }
}
