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
    val haptics = com.sonza.app.ui.utils.rememberHaptics()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left Column: Title + Artist + Indicators
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
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
                                letterSpacing = (-0.3).sp,
                                fontSize = 18.sp
                            )
                        } else {
                            SonzaTypography.Headline.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp
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
                        Spacer(modifier = Modifier.width(6.dp))
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
                                    modifier = Modifier.size(11.dp),
                                    tint = dominantColors.accent
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    "AI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 9.sp
                                    ),
                                    color = dominantColors.accent
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 1.dp else 3.dp))

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
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    )
                } else {
                    SonzaTypography.ArtistSubtitle.copy(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
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
                    modifier = Modifier.padding(top = 3.dp)
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
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(dominantColors.accent.copy(alpha = alpha))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
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
                    modifier = Modifier.padding(top = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        modifier = Modifier.size(if (compact) 12.dp else 13.dp),
                        tint = dominantColors.accent.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
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
                            letterSpacing = 0.2.sp,
                            fontSize = 11.sp
                        ),
                        color = dominantColors.accent.copy(alpha = 0.9f)
                    )
                }
            }

            // Download progress chip
            val downloadProgress = LocalCurrentDownloadProgress.current
            if (song != null && downloadProgress != null) {
                Spacer(modifier = Modifier.height(if (compact) 2.dp else 3.dp))
                DownloadProgressChip(
                    progress = downloadProgress,
                    dominantColors = dominantColors
                )
            }
        }

        // Right Column: Action Buttons (Favorite + More Options)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Favorite Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isFavorite) dominantColors.accent.copy(alpha = 0.16f)
                        else dominantColors.onBackground.copy(alpha = 0.08f)
                    )
                    .com.sonza.app.ui.components.bounceClick(scaleDown = com.sonza.app.ui.theme.MotionTokens.CardTapScale) {
                        haptics.thump()
                        onFavoriteClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = isFavorite,
                    transitionSpec = {
                        (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                        (scaleOut() + fadeOut())
                    },
                    label = "favoriteToggleAnim"
                ) { fav ->
                    Icon(
                        imageVector = if (fav) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (fav) "Remove from favorites" else "Add to favorites",
                        tint = if (fav) dominantColors.accent else dominantColors.onBackground.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // More Options Button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(dominantColors.onBackground.copy(alpha = 0.08f))
                    .com.sonza.app.ui.components.bounceClick(scaleDown = com.sonza.app.ui.theme.MotionTokens.CardTapScale) {
                        haptics.tick()
                        onMoreClick()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Song options",
                    tint = dominantColors.onBackground.copy(alpha = 0.8f),
                    modifier = Modifier.size(20.dp)
                )
            }
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

    val formatLabel = remember(audioCodec, audioBitrate, activeAudioSource) {
        val codecText = audioCodec?.uppercase()
        val bitrateText = audioBitrate?.let { "${it} kbps" }
            ?: if (activeAudioSource == com.sonza.app.core.model.MusicSource.REMOTE) "320 kbps" else null
        val sourceBadge = if (activeAudioSource == com.sonza.app.core.model.MusicSource.REMOTE) "HQ" else null
        when {
            sourceBadge != null && bitrateText != null -> "$sourceBadge • $bitrateText"
            codecText != null && bitrateText != null -> "$codecText • $bitrateText"
            sourceBadge != null -> sourceBadge
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
        Box(modifier = Modifier.size(14.dp), contentAlignment = Alignment.Center) {
            com.sonza.app.ui.components.SonzaLoadingIndicator(
                modifier = Modifier.size(14.dp)
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
