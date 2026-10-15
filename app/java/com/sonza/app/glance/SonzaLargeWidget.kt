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
import androidx.glance.appwidget.LinearProgressIndicator
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

class SonzaLargeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SonzaLargeWidget()
}

/**
 * Widget 2 — Premium Large Music Player.
 * Dominant album artwork, real-time progress bar, time/duration displays,
 * and comprehensive music controls (Shuffle, Previous, Primary Play/Pause, Next, Repeat, Favorite).
 */
class SonzaLargeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = EntryPointAccessors.fromApplication(context.applicationContext, SharedWidgetEntryPoint::class.java)
        val musicPlayer = entryPoint.getMusicPlayer()
        val currentSongInitial = musicPlayer.playerState.value.currentSong

        // Preload high-res artwork before RemoteViews composition
        val initialArtwork = SonzaWidgetArtworkHelper.loadArtwork(context, currentSongInitial?.thumbnailUrl, 320)

        provideContent {
            GlanceTheme {
                val playerState by musicPlayer.playerState.collectAsState()
                val currentSong = playerState.currentSong

                var artworkBitmap by remember(currentSong?.id) {
                    mutableStateOf(
                        SonzaWidgetArtworkHelper.getCachedArtwork(currentSong?.thumbnailUrl, 320) ?: initialArtwork
                    )
                }

                LaunchedEffect(currentSong?.id, currentSong?.thumbnailUrl) {
                    val loaded = SonzaWidgetArtworkHelper.loadArtwork(context, currentSong?.thumbnailUrl, 320)
                    if (loaded != null) {
                        artworkBitmap = loaded
                    }
                }

                val backgroundColor = SonzaWidgetTheme.computeCardBackground(playerState.dominantColor)
                val currentPos = playerState.currentPosition.coerceAtLeast(0L)
                val totalDuration = playerState.duration.coerceAtLeast(0L)
                val progressRatio = if (totalDuration > 0L) {
                    (currentPos.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
                } else {
                    0f
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
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Top Section: Hero Album Artwork + Track Information + Favorite
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().defaultWeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Dominant Album Artwork
                            Box(
                                modifier = GlanceModifier
                                    .size(100.dp)
                                    .cornerRadius(18.dp)
                                    .background(ColorProvider(Color.White.copy(alpha = 0.12f))),
                                contentAlignment = Alignment.Center
                            ) {
                                val bmp = artworkBitmap
                                if (bmp != null && !bmp.isRecycled) {
                                    Image(
                                        provider = ImageProvider(bmp),
                                        contentDescription = currentSong?.title?.let { "Album artwork for $it" } ?: "Album Artwork",
                                        contentScale = ContentScale.Crop,
                                        modifier = GlanceModifier.fillMaxSize().cornerRadius(18.dp)
                                    )
                                } else {
                                    Image(
                                        provider = ImageProvider(R.drawable.ic_music_note),
                                        contentDescription = "Default Music Artwork",
                                        colorFilter = ColorFilter.tint(ColorProvider(Color.White.copy(alpha = 0.6f))),
                                        modifier = GlanceModifier.size(44.dp)
                                    )
                                }
                            }

                            Spacer(modifier = GlanceModifier.width(14.dp))

                            // Track Metadata & Favorite Button
                            Column(
                                modifier = GlanceModifier.defaultWeight(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = currentSong?.title ?: "Nothing Playing",
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.TextPrimary),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    maxLines = 2
                                )
                                Spacer(modifier = GlanceModifier.height(3.dp))
                                Text(
                                    text = currentSong?.artist ?: "Sonza Music",
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.TextSecondary),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Normal
                                    ),
                                    maxLines = 1
                                )

                                if (!currentSong?.album.isNullOrBlank()) {
                                    Spacer(modifier = GlanceModifier.height(2.dp))
                                    Text(
                                        text = currentSong!!.album,
                                        style = TextStyle(
                                            color = ColorProvider(SonzaWidgetTheme.TextMuted),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Normal
                                        ),
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = GlanceModifier.height(8.dp))

                                // Favorite Toggle Pill
                                Box(
                                    modifier = GlanceModifier
                                        .cornerRadius(12.dp)
                                        .background(
                                            ColorProvider(
                                                if (playerState.isLiked) SonzaWidgetTheme.AccentRed.copy(alpha = 0.20f)
                                                else Color.White.copy(alpha = 0.10f)
                                            )
                                        )
                                        .clickable(actionRunCallback<ToggleLikeAction>())
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Image(
                                            provider = ImageProvider(
                                                if (playerState.isLiked) R.drawable.ic_heart else R.drawable.ic_heart_outline
                                            ),
                                            contentDescription = if (playerState.isLiked) "Favorited" else "Add to Favorites",
                                            colorFilter = ColorFilter.tint(
                                                ColorProvider(
                                                    if (playerState.isLiked) SonzaWidgetTheme.AccentRed else Color.White.copy(alpha = 0.8f)
                                                )
                                            ),
                                            modifier = GlanceModifier.size(16.dp)
                                        )
                                        Spacer(modifier = GlanceModifier.width(6.dp))
                                        Text(
                                            text = if (playerState.isLiked) "Liked" else "Favorite",
                                            style = TextStyle(
                                                color = ColorProvider(
                                                    if (playerState.isLiked) SonzaWidgetTheme.AccentRed else Color.White.copy(alpha = 0.85f)
                                                ),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(12.dp))

                        // Middle Section: Progress Bar & Timestamps
                        Column(modifier = GlanceModifier.fillMaxWidth()) {
                            LinearProgressIndicator(
                                progress = progressRatio,
                                modifier = GlanceModifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .cornerRadius(2.dp),
                                color = ColorProvider(SonzaWidgetTheme.AccentCyan),
                                backgroundColor = ColorProvider(Color.White.copy(alpha = 0.16f))
                            )

                            Spacer(modifier = GlanceModifier.height(4.dp))

                            Row(
                                modifier = GlanceModifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SonzaWidgetTheme.formatDuration(currentPos),
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.TextSecondary),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                )
                                Spacer(modifier = GlanceModifier.defaultWeight())
                                Text(
                                    text = SonzaWidgetTheme.formatDuration(totalDuration),
                                    style = TextStyle(
                                        color = ColorProvider(SonzaWidgetTheme.TextMuted),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(10.dp))

                        // Bottom Section: Playback Controls
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Shuffle Button
                            Box(
                                modifier = GlanceModifier
                                    .size(38.dp)
                                    .cornerRadius(19.dp)
                                    .clickable(actionRunCallback<ToggleShuffleAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(
                                        if (playerState.shuffleEnabled) R.drawable.ic_shuffle_on else R.drawable.ic_shuffle
                                    ),
                                    contentDescription = if (playerState.shuffleEnabled) "Shuffle on" else "Shuffle off",
                                    colorFilter = ColorFilter.tint(
                                        ColorProvider(
                                            if (playerState.shuffleEnabled) SonzaWidgetTheme.AccentCyan else Color.White.copy(alpha = 0.65f)
                                        )
                                    ),
                                    modifier = GlanceModifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            // Previous Track
                            Box(
                                modifier = GlanceModifier
                                    .size(44.dp)
                                    .cornerRadius(22.dp)
                                    .clickable(actionRunCallback<PreviousAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_skip_previous),
                                    contentDescription = "Previous song",
                                    colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.TextPrimary)),
                                    modifier = GlanceModifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            // Primary Play / Pause Hero Button
                            Box(
                                modifier = GlanceModifier
                                    .size(54.dp)
                                    .cornerRadius(27.dp)
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
                                    modifier = GlanceModifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            // Next Track
                            Box(
                                modifier = GlanceModifier
                                    .size(44.dp)
                                    .cornerRadius(22.dp)
                                    .clickable(actionRunCallback<NextAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(R.drawable.ic_skip_next),
                                    contentDescription = "Next song",
                                    colorFilter = ColorFilter.tint(ColorProvider(SonzaWidgetTheme.TextPrimary)),
                                    modifier = GlanceModifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = GlanceModifier.defaultWeight())

                            // Repeat Button
                            Box(
                                modifier = GlanceModifier
                                    .size(38.dp)
                                    .cornerRadius(19.dp)
                                    .clickable(actionRunCallback<ToggleRepeatAction>()),
                                contentAlignment = Alignment.Center
                            ) {
                                val isRepeatActive = playerState.repeatMode != com.sonza.app.core.model.RepeatMode.OFF
                                Image(
                                    provider = ImageProvider(
                                        when (playerState.repeatMode) {
                                            com.sonza.app.core.model.RepeatMode.ONE -> R.drawable.ic_repeat_one_on
                                            com.sonza.app.core.model.RepeatMode.ALL -> R.drawable.ic_repeat_all_on
                                            com.sonza.app.core.model.RepeatMode.OFF -> R.drawable.ic_repeat
                                        }
                                    ),
                                    contentDescription = "Repeat mode: ${playerState.repeatMode}",
                                    colorFilter = ColorFilter.tint(
                                        ColorProvider(
                                            if (isRepeatActive) SonzaWidgetTheme.AccentCyan else Color.White.copy(alpha = 0.65f)
                                        )
                                    ),
                                    modifier = GlanceModifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
