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

class SonzaCompactWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaCompactWidget()
}

/**
 * Widget 1 — Compact Music Player Controller.
 * Minimal, glanceable music widget showing album artwork, song title, artist,
 * favorite state, and instant playback controls (Previous, Play/Pause, Next).
 */
class SonzaCompactWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        val musicPlayer = entryPoint.getMusicPlayer()
        val currentSongInitial = musicPlayer.playerState.value.currentSong

        // Preload artwork before first RemoteViews layout to prevent placeholder flashing
        val initialArtwork = SonzaWidgetArtworkHelper.loadArtwork(context, currentSongInitial?.thumbnailUrl, 160)

        provideContent {
            GlanceTheme {
                val playerState by musicPlayer.playerState.collectAsState()
                val currentSong = playerState.currentSong

                var artworkBitmap by remember(currentSong?.id) {
                    mutableStateOf(
                        SonzaWidgetArtworkHelper.getCachedArtwork(currentSong?.thumbnailUrl, 160) ?: initialArtwork
                    )
                }

                LaunchedEffect(currentSong?.id, currentSong?.thumbnailUrl) {
                    val loaded = SonzaWidgetArtworkHelper.loadArtwork(context, currentSong?.thumbnailUrl, 160)
                    if (loaded != null) {
                        artworkBitmap = loaded
                    }
                }

                val backgroundColor = SonzaWidgetTheme.computeCardBackground(playerState.dominantColor)

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(ColorProvider(backgroundColor))
                        .cornerRadius(22.dp)
                        .clickable(
                            actionRunCallback<OpenDeepLinkAction>(
                                actionParametersOf(DeepLinkKey to "sonza://player")
                            )
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album Artwork Thumbnail
                        Box(
                            modifier = GlanceModifier
                                .size(46.dp)
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

                        // Track Metadata
                        Column(
                            modifier = GlanceModifier.defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = currentSong?.title ?: "Nothing Playing",
                                style = TextStyle(
                                    color = ColorProvider(SonzaWidgetTheme.TextPrimary),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                maxLines = 1
                            )
                            Spacer(modifier = GlanceModifier.height(2.dp))
                            Text(
                                text = currentSong?.artist ?: "Sonza Music",
                                style = TextStyle(
                                    color = ColorProvider(SonzaWidgetTheme.TextSecondary),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal
                                ),
                                maxLines = 1
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(6.dp))

                        // Favorite Action
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
                                modifier = GlanceModifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(2.dp))

                        // Previous Action
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .cornerRadius(18.dp)
                                .clickable(actionRunCallback<PreviousAction>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_skip_previous),
                                contentDescription = "Previous song",
                                colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.TextPrimary)),
                                modifier = GlanceModifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(4.dp))

                        // Primary Play / Pause Action Button
                        Box(
                            modifier = GlanceModifier
                                .size(42.dp)
                                .cornerRadius(21.dp)
                                .background(ColorProvider(SonzaWidgetTheme.PrimaryButtonFill))
                                .clickable(actionRunCallback<PlayPauseAction>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(
                                    if (playerState.isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                                ),
                                contentDescription = if (playerState.isPlaying) "Pause playback" else "Resume playback",
                                colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.PrimaryButtonIcon)),
                                modifier = GlanceModifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = GlanceModifier.width(4.dp))

                        // Next Action
                        Box(
                            modifier = GlanceModifier
                                .size(36.dp)
                                .cornerRadius(18.dp)
                                .clickable(actionRunCallback<NextAction>()),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(R.drawable.ic_skip_next),
                                contentDescription = "Next song",
                                colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.TextPrimary)),
                                modifier = GlanceModifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
