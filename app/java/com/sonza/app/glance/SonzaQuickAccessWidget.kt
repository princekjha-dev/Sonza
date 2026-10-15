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

class SonzaQuickAccessWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaQuickAccessWidget()
}

/**
 * Widget D — AI Quick Access + Now Playing Widget
 * Directly deep-links to existing Sonza features: Orbit Voice AI, Equalizer, Lyrics, and Search,
 * while staying synchronized with the single authoritative playback state.
 */
class SonzaQuickAccessWidget : GlanceAppWidget() {

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
                    Color(domColor).copy(alpha = 0.94f)
                } else {
                    Color(0xFF14151F).copy(alpha = 0.96f)
                }

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(backgroundColor))
                        .cornerRadius(24.dp)
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top Section: Now Playing Banner (if active) or Orbit Header
                        if (currentSong != null) {
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .cornerRadius(14.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.1f)))
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Mini Artwork
                                Box(
                                    modifier = GlanceModifier
                                        .size(36.dp)
                                        .cornerRadius(8.dp)
                                        .background(ColorProvider(Color.White.copy(alpha = 0.1f)))
                                        .clickable(
                                            actionRunCallback<OpenDeepLinkAction>(
                                                actionParametersOf(DeepLinkKey to "sonza://player")
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (artworkBitmap != null) {
                                        Image(
                                            provider = ImageProvider(artworkBitmap!!),
                                            contentDescription = "Artwork",
                                            contentScale = ContentScale.Crop,
                                            modifier = GlanceModifier.fillMaxSize().cornerRadius(8.dp)
                                        )
                                    } else {
                                        Image(
                                            provider = ImageProvider(R.drawable.ic_music_note),
                                            contentDescription = "Artwork",
                                            colorFilter = ColorFilter.tint(ColorProvider(Color.White.copy(alpha = 0.6f))),
                                            modifier = GlanceModifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = GlanceModifier.width(8.dp))

                                // Song Info
                                Column(
                                    modifier = GlanceModifier
                                        .defaultWeight()
                                        .clickable(
                                            actionRunCallback<OpenDeepLinkAction>(
                                                actionParametersOf(DeepLinkKey to "sonza://player")
                                            )
                                        )
                                ) {
                                    Text(
                                        text = currentSong.title,
                                        style = TextStyle(
                                            color = ColorProvider(Color.White),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        maxLines = 1
                                    )
                                    Text(
                                        text = currentSong.artist,
                                        style = TextStyle(
                                            color = ColorProvider(Color.White.copy(alpha = 0.7f)),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = GlanceModifier.width(6.dp))

                                // Favorite Heart
                                Image(
                                    provider = ImageProvider(
                                        if (playerState.isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
                                    ),
                                    contentDescription = "Favorite",
                                    colorFilter = ColorFilter.tint(
                                        ColorProvider(if (playerState.isLiked) Color(0xFFFF4081) else Color.White.copy(alpha = 0.7f))
                                    ),
                                    modifier = GlanceModifier
                                        .size(20.dp)
                                        .clickable(actionRunCallback<ToggleLikeAction>())
                                )

                                Spacer(modifier = GlanceModifier.width(8.dp))

                                // Play/Pause
                                Box(
                                    modifier = GlanceModifier
                                        .size(32.dp)
                                        .cornerRadius(16.dp)
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
                                        modifier = GlanceModifier.size(18.dp)
                                    )
                                }
                            }
                        } else {
                            // Header when idle
                            Row(
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .clickable(
                                        actionRunCallback<OpenDeepLinkAction>(
                                            actionParametersOf(DeepLinkKey to "sonza://orbit_listen")
                                        )
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = GlanceModifier
                                        .size(32.dp)
                                        .cornerRadius(16.dp)
                                        .background(ColorProvider(Color(0xFF38BDF8).copy(alpha = 0.3f))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_mic),
                                        contentDescription = "Orbit",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color(0xFF38BDF8))),
                                        modifier = GlanceModifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = GlanceModifier.width(10.dp))

                                Column(modifier = GlanceModifier.defaultWeight()) {
                                    Text(
                                        text = "SONZA ORBIT",
                                        style = TextStyle(
                                            color = ColorProvider(Color(0xFF38BDF8)),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = "Ask Orbit to play anything",
                                        style = TextStyle(
                                            color = ColorProvider(Color.White.copy(alpha = 0.8f)),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Normal
                                        )
                                    )
                                }
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // 4 Quick Action Tiles: [Orbit] [EQ] [Lyrics] [Search]
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Orbit Voice AI Tile
                            QuickActionTile(
                                label = "Orbit",
                                iconRes = R.drawable.ic_mic,
                                tintColor = Color(0xFF38BDF8),
                                deepLinkUri = "sonza://orbit_listen",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            // 2. Equalizer Tile
                            QuickActionTile(
                                label = "EQ / DSP",
                                iconRes = R.drawable.ic_equalizer,
                                tintColor = Color(0xFF00E5FF),
                                deepLinkUri = "sonza://eq",
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3. Lyrics Tile
                            QuickActionTile(
                                label = "Lyrics",
                                iconRes = R.drawable.ic_lyrics,
                                tintColor = Color(0xFFBA68C8),
                                deepLinkUri = "sonza://lyrics",
                                modifier = GlanceModifier.defaultWeight()
                            )

                            Spacer(modifier = GlanceModifier.width(8.dp))

                            // 4. Search Tile
                            QuickActionTile(
                                label = "Search",
                                iconRes = R.drawable.ic_search,
                                tintColor = Color(0xFF38D39F),
                                deepLinkUri = "sonza://search",
                                modifier = GlanceModifier.defaultWeight()
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun QuickActionTile(
        label: String,
        iconRes: Int,
        tintColor: Color,
        deepLinkUri: String,
        modifier: GlanceModifier
    ) {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .cornerRadius(14.dp)
                .background(ColorProvider(Color.White.copy(alpha = 0.08f)))
                .clickable(
                    actionRunCallback<OpenDeepLinkAction>(
                        actionParametersOf(DeepLinkKey to deepLinkUri)
                    )
                )
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(iconRes),
                    contentDescription = label,
                    colorFilter = ColorFilter.tint(ColorProvider(tintColor)),
                    modifier = GlanceModifier.size(24.dp)
                )
                Spacer(modifier = GlanceModifier.height(4.dp))
                Text(
                    text = label,
                    style = TextStyle(
                        color = ColorProvider(Color.White),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
