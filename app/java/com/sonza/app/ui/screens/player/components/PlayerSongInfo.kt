package com.sonza.app.ui.screens.player.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material3.*
import androidx.compose.foundation.basicMarquee
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.DominantColors
import com.sonza.app.ui.components.BetaBadge
import com.sonza.app.ui.screens.player.formatDuration
import com.sonza.app.ui.theme.SonzaTypography

import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.CircularProgressIndicator

/**
 * Per-current-song download progress (0f..1f), or null when the current song isn't downloading.
 * Provided by PlayerScreen; consumed by [SongInfoSection] to render the download chip.
 */
val LocalCurrentDownloadProgress = androidx.compose.runtime.compositionLocalOf<Float?> { null }

@Composable
fun SongInfoSection(
    song: Song?,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    isDisliked: Boolean = false,
    onDislikeClick: () -> Unit = {},
    onMoreClick: () -> Unit,
    onArtistClick: (String) -> Unit = {},
    onAlbumClick: (String) -> Unit = {},
    dominantColors: DominantColors,
    isLoading: Boolean = false,
    compact: Boolean = false,
    sleepTimerRemainingMs: Long? = null,
    sleepTimerOption: com.sonza.app.player.SleepTimerOption = com.sonza.app.player.SleepTimerOption.OFF,
    showMoreButton: Boolean = false,
    isClassic: Boolean = false,
    isAIEnabled: Boolean = false,
    aiStatus: String? = null,
    showInlineLikeCapsule: Boolean = false,
    showTitleArrow: Boolean = false,
    onTitleArrowClick: () -> Unit = {},
    activeAudioSource: com.sonza.app.core.model.MusicSource? = null,
    onSwitchAudioSource: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        // Song Title
        AnimatedContent(
            targetState = song?.id,
            transitionSpec = {
                (slideInVertically(
                    animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)
                ) { it / 3 } + fadeIn()) togetherWith
                (slideOutVertically { -it / 3 } + fadeOut())
            },
            label = "songTitleTransition"
        ) { _ ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = song?.title ?: "No song playing",
                    style = if (compact) {
                        SonzaTypography.TitleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        )
                    } else {
                        SonzaTypography.Headline.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp
                        )
                    },
                    color = dominantColors.onBackground,
                    maxLines = 1,
                    modifier = Modifier
                        .basicMarquee(iterations = Int.MAX_VALUE)
                        .weight(1f, fill = false)
                )

                // AI EQ Indicator Badge
                if (isAIEnabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = dominantColors.accent.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, dominantColors.accent.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = dominantColors.accent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "AI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                ),
                                color = dominantColors.accent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            BetaBadge(
                                containerColor = dominantColors.accent,
                                contentColor = Color.White,
                                modifier = Modifier.graphicsLayer { scaleX = 0.7f; scaleY = 0.7f }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))

        // Artist & Album Line
        val artistText = song?.artist ?: ""
        val albumText = song?.album?.takeIf { it.isNotBlank() && !it.startsWith("Artist Radio:") }
        val secondaryMetadata = if (albumText != null && !albumText.equals(artistText, ignoreCase = true)) {
            "$artistText • $albumText"
        } else {
            artistText
        }

        Text(
            text = secondaryMetadata,
            style = if (compact) {
                SonzaTypography.ArtistSubtitle.copy(
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp
                )
            } else {
                SonzaTypography.ArtistSubtitle.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 0.sp
                )
            },
            color = dominantColors.onBackground.copy(alpha = 0.65f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .basicMarquee(iterations = Int.MAX_VALUE)
                .clickable {
                    val target = song?.artistId ?: song?.artist
                    target?.let { onArtistClick(it) }
                }
        )

        // AI Processing Status Indicator
        androidx.compose.animation.AnimatedVisibility(
            visible = aiStatus != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "aiPulse")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "aiPulseAlpha"
                )

                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(dominantColors.accent.copy(alpha = alpha))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = aiStatus ?: "",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    ),
                    color = dominantColors.accent,
                    modifier = Modifier.graphicsLayer { this.alpha = alpha }
                )
            }
        }

        // Sleep Timer indicator
        androidx.compose.animation.AnimatedVisibility(
            visible = sleepTimerOption != com.sonza.app.player.SleepTimerOption.OFF,
            enter = fadeIn() + slideInVertically { -20 },
            exit = fadeOut() + slideOutVertically { -20 }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    modifier = Modifier.size(if (compact) 12.dp else 14.dp),
                    tint = dominantColors.accent.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (sleepTimerOption == com.sonza.app.player.SleepTimerOption.END_OF_SONG) {
                        "Sleep at end of song"
                    } else {
                        sleepTimerRemainingMs?.let { ms ->
                            "Sleep in " + com.sonza.app.util.TimeUtil.formatPosition(ms)
                        } ?: ""
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.3.sp
                    ),
                    color = dominantColors.accent.copy(alpha = 0.9f)
                )
            }
        }

        // Download progress chip
        val downloadProgress = LocalCurrentDownloadProgress.current
        if (song != null && downloadProgress != null) {
            Spacer(modifier = Modifier.height(if (compact) 2.dp else 4.dp))
            DownloadProgressChip(
                progress = downloadProgress,
                dominantColors = dominantColors
            )
        }
    }
}



@Composable
fun TimeLabelsWithQuality(
    currentPositionProvider: () -> Long,
    durationProvider: () -> Long,
    dominantColors: DominantColors,
    audioCodec: String? = null,
    audioBitrate: Int? = null,
    activeAudioSource: com.sonza.app.core.model.MusicSource? = null,
    horizontalPadding: androidx.compose.ui.unit.Dp = 8.dp
) {
    // Derive the formatted strings so recomposition only happens when the
    // second-resolution text actually changes, not every ~400ms position tick.
    val posText by remember {
        derivedStateOf { formatDuration(currentPositionProvider()) }
    }
    val remainingText by remember {
        derivedStateOf { "-${formatDuration(durationProvider() - currentPositionProvider())}" }
    }

    val formatLabel = remember(audioCodec, audioBitrate) {
        val codecText = audioCodec?.uppercase()
        val bitrateText = audioBitrate?.let { "${it} kbps" }
        when {
            codecText != null && bitrateText != null -> "$codecText • $bitrateText"
            codecText != null -> codecText
            bitrateText != null -> bitrateText
            else -> null
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = posText,
            style = MaterialTheme.typography.labelMedium,
            color = dominantColors.onBackground.copy(alpha = 0.7f)
        )

        if (formatLabel != null) {
            Surface(
                color = dominantColors.accent.copy(alpha = 0.12f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = formatLabel,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.4.sp
                    ),
                    color = dominantColors.accent,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Text(
            text = remainingText,
            style = MaterialTheme.typography.labelMedium,
            color = dominantColors.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun DownloadProgressChip(
    progress: Float,
    dominantColors: DominantColors
) {
    var expanded by remember { mutableStateOf(false) }
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 250),
        label = "downloadProgress"
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = dominantColors.accent.copy(alpha = 0.18f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { expanded = !expanded }
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Box(modifier = Modifier.size(12.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(12.dp),
                color = dominantColors.accent,
                strokeWidth = 1.5.dp,
                trackColor = dominantColors.accent.copy(alpha = 0.25f)
            )
            Icon(
                imageVector = Icons.Filled.Download,
                contentDescription = null,
                modifier = Modifier.size(7.dp),
                tint = dominantColors.accent
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (expanded) "${(animatedProgress * 100).toInt()}%" else "Downloading",
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.3.sp
            ),
            color = dominantColors.accent
        )
    }
}

/**
 * Per-song source switch that lives at the end of the like/dislike capsule. It shows
 * the source you'd get by tapping — the YouTube logo while HQ Audio is playing, the
 * HQ badge while YouTube is playing.
 */
@Composable
private fun SourceSwitchButton(
    activeAudioSource: com.sonza.app.core.model.MusicSource,
    onClick: () -> Unit,
    dominantColors: DominantColors,
    buttonSize: androidx.compose.ui.unit.Dp,
    iconSize: androidx.compose.ui.unit.Dp
) {
    val playingHq = activeAudioSource == com.sonza.app.core.model.MusicSource.REMOTE
    IconButton(onClick = onClick, modifier = Modifier.size(buttonSize)) {
        if (playingHq) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.sonza.app.R.drawable.ic_youtube),
                contentDescription = "Switch to YouTube",
                tint = dominantColors.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(iconSize)
            )
        } else {
            Icon(
                imageVector = Icons.Filled.HighQuality,
                contentDescription = "Switch to HQ Audio",
                tint = dominantColors.onBackground.copy(alpha = 0.7f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}
