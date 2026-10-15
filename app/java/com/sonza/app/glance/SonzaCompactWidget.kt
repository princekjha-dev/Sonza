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

class SonzaCompactWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaCompactWidget()
}

/**
 * Widget A — Compact Music Widget
 */
class SonzaCompactWidget : GlanceAppWidget() {

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
                                .size(160, 160)
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
                    Color(domColor).copy(alpha = 0.92f)
                } else {
                    Color(0xFF161720).copy(alpha = 0.96f)
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(backgroundColor))
                        .cornerRadius(20.dp)
                        .clickable(actionRunCallback<OpenAppAction>())
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Artwork thumbnail
                        Box(
                            modifier = GlanceModifier
                                .size(46.dp)
                                .cornerRadius(12.dp)
                                .background(ColorProvider(Color.White.copy(alpha = 0.1f))),
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

                        // Song Info
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            Text(
                                text = currentSong?.title ?: "Sonza Music",
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = currentSong?.artist ?: "Tap to play",
                                style = TextStyle(
                                    color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        // Play/Pause Action Button
                        Box(
                            modifier = GlanceModifier
                                .size(42.dp)
                                .cornerRadius(21.dp)
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
                                modifier = GlanceModifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        // Orbit Button (deep link into voice listening)
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .cornerRadius(18.dp)
                                .background(ColorProvider(Color(0xFF4F86F7).copy(alpha = 0.25f)))
                                .clickable(
                                    actionRunCallback<OpenDeepLinkAction>(
                                        actionParametersOf(DeepLinkKey to "sonza://orbit_listen")
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_mic),
                                contentDescription = "Orbit Voice",
                                colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF4F86F7))),
                                modifier = GlanceModifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
