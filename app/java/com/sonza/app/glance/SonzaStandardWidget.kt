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

class SonzaStandardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaStandardWidget()
}

/**
 * Widget 5 — Queue / Now Playing Widget.
 * Shows:
 * - Current song
 * - Next song
 * - Queue count
 * - Play/pause button
 * - Next track button
 * - Queue button to open queue directly
 */
class SonzaStandardWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        val musicPlayer = entryPoint.getMusicPlayer()

        provideContent {
            GlanceTheme {
                val playerState by musicPlayer.playerState.collectAsState()
                val currentSong = playerState.currentSong
                val queue = playerState.queue
                val currentIndex = playerState.currentIndex
                val nextSong = if (currentIndex in 0 until queue.size - 1) queue[currentIndex + 1] else null
                val queueCount = queue.size

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
                                .size(200, 200)
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
                    Color(0xFF161722).copy(alpha = 0.96f)
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(backgroundColor))
                        .cornerRadius(24.dp)
                        .clickable(
                            actionRunCallback<OpenDeepLinkAction>(
                                actionParametersOf(DeepLinkKey to "sonza://player")
                            )
                        )
                        .padding(14.dp)
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top Row: Artwork + Current Song Info + Play/Pause & Next controls
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Artwork
                            Box(
                                modifier = GlanceModifier
                                    .size(52.dp)
                                    .cornerRadius(12.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.12f))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (artworkBitmap != null) {
                                    Image(
                                        provider = ImageProvider(artworkBitmap!!),
                                        contentDescription = "Artwork",
                                        contentScale = ContentScale.Crop,
                                        modifier = GlanceModifier.fillMaxSize().cornerRadius(12.dp)
                                    )
                                } else {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_music_note),
                                        contentDescription = "Artwork",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White.copy(alpha = 0.6f))),
                                        modifier = GlanceModifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(10.dp))

                            // Current Song Info
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = currentSong?.title ?: "Not Playing",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = currentSong?.artist ?: "Sonza Player",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            // Play/Pause Button
                            Box(
                                modifier = GlanceModifier
                                    .size(38.dp)
                                    .cornerRadius(19.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.16f)))
                                    .clickable(actionRunCallback<PlayPauseAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(
                                        if (playerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                    ),
                                    contentDescription = "Play/Pause",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            // Next Button
                            Box(
                                modifier = GlanceModifier
                                    .size(34.dp)
                                    .cornerRadius(17.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.08f)))
                                    .clickable(actionRunCallback<NextAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_skip_next),
                                    contentDescription = "Next",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Bottom Row: Next Song preview + Queue Count + Queue Open Button
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .cornerRadius(12.dp)
                                .background(ColorProvider(Color.White.copy(alpha = 0.08f)))
                                .clickable(
                                    actionRunCallback<OpenDeepLinkAction>(
                                        actionParametersOf(DeepLinkKey to "sonza://queue")
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = if (nextSong != null) "Up Next: ${nextSong.title}" else "Up Next: End of queue",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White.copy(alpha = 0.9f)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = "$queueCount songs in queue",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF38BDF8)),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            }

                            // Direct Queue Button
                            Box(
                                modifier = GlanceModifier
                                    .cornerRadius(8.dp)
                                    .background(ColorProvider(Color(0xFF38BDF8).copy(alpha = 0.2f)))
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Queue",
                                    style = TextStyle(
                                        color = ColorProvider(Color(0xFF38BDF8)),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
