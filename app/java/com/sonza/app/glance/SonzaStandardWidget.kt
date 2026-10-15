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
import com.sonza.app.R
import dagger.hilt.android.EntryPointAccessors

class SonzaStandardWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaStandardWidget()
}

/**
 * Widget 5 — Queue & Now Playing Controller.
 * Features:
 * - Current track artwork, title, artist, and favorite state
 * - Real queue synchronization ("Up Next: [Song]", queue track count)
 * - Playback controls (Previous, Hero Play/Pause, Next)
 * - Direct queue launch button opening the live player queue
 */
class SonzaStandardWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        val musicPlayer = entryPoint.getMusicPlayer()
        val currentSongInitial = musicPlayer.playerState.value.currentSong

        // Preload artwork before RemoteViews composition
        val initialArtwork = SonzaWidgetArtworkHelper.loadArtwork(context, currentSongInitial?.thumbnailUrl, 200)

        provideContent {
            GlanceTheme {
                val playerState by musicPlayer.playerState.collectAsState()
                val currentSong = playerState.currentSong
                val queue = playerState.queue
                val currentIndex = playerState.currentIndex
                val nextSong = if (currentIndex in 0 until queue.size - 1) queue[currentIndex + 1] else null
                val queueCount = queue.size

                var artworkBitmap by remember(currentSong?.id) {
                    mutableStateOf(
                        SonzaWidgetArtworkHelper.getCachedArtwork(currentSong?.thumbnailUrl, 200) ?: initialArtwork
                    )
                }

                LaunchedEffect(currentSong?.id, currentSong?.thumbnailUrl) {
                    val loaded = SonzaWidgetArtworkHelper.loadArtwork(context, currentSong?.thumbnailUrl, 200)
                    if (loaded != null) {
                        artworkBitmap = loaded
                    }
                }

                val backgroundColor = SonzaWidgetTheme.computeCardBackground(playerState.dominantColor)

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
                        // Section 1: Header + Now Playing Row
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
                                val bmp = artworkBitmap
                                if (bmp != null && !bmp.isRecycled) {
                                    Image(
                                        provider = ImageProvider(bmp),
                                        contentDescription = currentSong?.title?.let { "Artwork for $it" } ?: "Album Artwork",
                                        contentScale = ContentScale.Crop,
                                        modifier = GlanceModifier.fillMaxSize().cornerRadius(12.dp)
                                    )
                                } else {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_music_note),
                                        contentDescription = "Default Music Artwork",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White.copy(alpha = 0.6f))),
                                        modifier = GlanceModifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(10.dp))

                            // Current Song Info
                            Column(
                                modifier = GlanceModifier.defaultWeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NOW PLAYING",
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.AccentCyan),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = currentSong?.title ?: "Nothing Playing",
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.TextPrimary),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = currentSong?.artist ?: "Sonza Music",
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.TextSecondary),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            // Favorite Toggle
                            Box(
                                modifier = GlanceModifier
                                    .size(36.dp)
                                    .cornerRadius(18.dp)
                                    .clickable(actionRunCallback<ToggleLikeAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(
                                        if (playerState.isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
                                    ),
                                    contentDescription = if (playerState.isLiked) "Unlike song" else "Like song",
                                    colorFilter = ColorFilter.tint(
                                        ColorProvider(if (playerState.isLiked) SonzaWidgetTheme.AccentRed else Color.White.copy(alpha = 0.70f))
                                    ),
                                    modifier = GlanceModifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Section 2: Up Next & Queue Card
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .cornerRadius(12.dp)
                                .background(ColorProvider(SonzaWidgetTheme.ControlPill))
                                .clickable(
                                    actionRunCallback<OpenDeepLinkAction>(
                                        actionParametersOf(DeepLinkKey to "sonza://queue")
                                    )
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = GlanceModifier.defaultWeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (nextSong != null) "Up Next: ${nextSong.title}" else "Up Next: End of queue",
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.TextPrimary),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    maxLines = 1
                                )
                                Text(
                                    text = if (queueCount > 0) "$queueCount songs in queue" else "Queue is empty",
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.AccentCyan),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )
                            }

                            // Direct Queue Deep Link Action
                            Box(
                                modifier = GlanceModifier
                                    .cornerRadius(8.dp)
                                    .background(ColorProvider(SonzaWidgetTheme.AccentCyan.copy(alpha = 0.20f)))
                                    .clickable(
                                        actionRunCallback<OpenDeepLinkAction>(
                                            actionParametersOf(DeepLinkKey to "sonza://queue")
                                        )
                                    )
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Queue",
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.AccentCyan),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Section 3: Playback Controls
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Previous
                            Box(
                                modifier = GlanceModifier
                                    .size(38.dp)
                                    .cornerRadius(19.dp)
                                    .clickable(actionRunCallback<PreviousAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_skip_previous),
                                    contentDescription = "Previous song",
                                    colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.TextPrimary)),
                                    modifier = GlanceModifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(20.dp))

                            // Play / Pause Button
                            Box(
                                modifier = GlanceModifier
                                    .size(44.dp)
                                    .cornerRadius(22.dp)
                                    .background(ColorProvider(SonzaWidgetTheme.PrimaryButtonFill))
                                    .clickable(actionRunCallback<PlayPauseAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(
                                        if (playerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                    ),
                                    contentDescription = if (playerState.isPlaying) "Pause song" else "Play song",
                                    colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.PrimaryButtonIcon)),
                                    modifier = GlanceModifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(20.dp))

                            // Next
                            Box(
                                modifier = GlanceModifier
                                    .size(38.dp)
                                    .cornerRadius(19.dp)
                                    .clickable(actionRunCallback<NextAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_skip_next),
                                    contentDescription = "Next song",
                                    colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.TextPrimary)),
                                    modifier = GlanceModifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
