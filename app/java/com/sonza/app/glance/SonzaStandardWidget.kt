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
 * Widget B — Standard Now Playing Widget (Medium)
 */
class SonzaStandardWidget : GlanceAppWidget() {

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
                                .size(240, 240)
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
                    Color(0xFF161720).copy(alpha = 0.96f)
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
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        // Top Section: Artwork + Info + Favorite & Orbit
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Large Artwork
                            Box(
                                modifier = GlanceModifier
                                    .size(62.dp)
                                    .cornerRadius(14.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.12f))),
                                contentAlignment = Alignment.Center
                            ) {
                                if (artworkBitmap != null) {
                                    Image(
                                        provider = ImageProvider(artworkBitmap!!),
                                        contentDescription = "Artwork",
                                        contentScale = ContentScale.Crop,
                                        modifier = GlanceModifier.fillMaxSize().cornerRadius(14.dp)
                                    )
                                } else {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_music_note),
                                        contentDescription = "Artwork",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White.copy(alpha = 0.6f))),
                                        modifier = GlanceModifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(12.dp))

                            // Titles
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = currentSong?.title ?: "Not Playing",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                                Spacer(modifier = GlanceModifier.height(3.dp))
                                Text(
                                    text = currentSong?.artist ?: "Sonza Player",
                                    style = TextStyle(
                                        color = ColorProvider(Color.White.copy(alpha = 0.72f)),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
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
                                    .size(24.dp)
                                    .clickable(actionRunCallback<ToggleLikeAction>())
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Progress Indicator line
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(ColorProvider(Color.White.copy(alpha = 0.18f)))
                                .cornerRadius(1.5.dp)
                        ) {}

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Playback Controls Row: [Orbit] [Prev] [Play/Pause] [Next] [Shuffle]
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Orbit Voice deep link
                            Box(
                                modifier = GlanceModifier
                                    .size(34.dp)
                                    .cornerRadius(17.dp)
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
                                    contentDescription = "Orbit AI",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF4F86F7))),
                                    modifier = GlanceModifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(18.dp))

                            // Previous
                            Image(
                                provider = ImageProvider(R.drawable.ic_skip_previous),
                                contentDescription = "Previous",
                                colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                modifier = GlanceModifier
                                    .size(28.dp)
                                    .clickable(actionRunCallback<PreviousAction>())
                            )

                            Spacer(modifier = GlanceModifier.width(18.dp))

                            // Play/Pause
                            Box(
                                modifier = GlanceModifier
                                    .size(46.dp)
                                    .cornerRadius(23.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.18f)))
                                    .clickable(actionRunCallback<PlayPauseAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(
                                        if (playerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                    ),
                                    contentDescription = "Play/Pause",
                                    colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                    modifier = GlanceModifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(18.dp))

                            // Next
                            Image(
                                provider = ImageProvider(R.drawable.ic_skip_next),
                                contentDescription = "Next",
                                colorFilter = ColorFilter.tint(ColorProvider(Color.White)),
                                modifier = GlanceModifier
                                    .size(28.dp)
                                    .clickable(actionRunCallback<NextAction>())
                            )

                            Spacer(modifier = GlanceModifier.width(18.dp))

                            // Shuffle Toggle
                            Image(
                                provider = ImageProvider(
                                    if (playerState.shuffleEnabled) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle
                                ),
                                contentDescription = "Shuffle",
                                colorFilter = ColorFilter.tint(
                                    ColorProvider(if (playerState.shuffleEnabled) Color(0xFF4F86F7) else Color.White.copy(alpha = 0.65f))
                                ),
                                modifier = GlanceModifier
                                    .size(22.dp)
                                    .clickable(actionRunCallback<ToggleShuffleAction>())
                            )
                        }
                    }
                }
            }
        }
    }
}
