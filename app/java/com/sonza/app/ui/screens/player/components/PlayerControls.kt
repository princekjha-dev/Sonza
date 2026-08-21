package com.sonza.app.ui.screens.player.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lyrics
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.sonza.app.ui.components.SonzaLoadingLogo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sonza.app.core.model.RepeatMode
import com.sonza.app.ui.components.DominantColors
import com.sonza.app.ui.components.bounceClick
import com.sonza.app.ui.theme.MotionTokens
import com.sonza.app.ui.theme.SpacingTokens

/**
 * Main Playback Controls: [Previous] [PLAY/PAUSE] [Next]
 */
@Composable
fun MainPlaybackControls(
    isPlaying: Boolean,
    isLoading: Boolean = false,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    dominantColors: DominantColors,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val haptics = com.sonza.app.ui.utils.rememberHaptics()
    val playSize = if (compact) 68.dp else 80.dp
    val playIconSize = if (compact) 34.dp else 42.dp
    val skipSize = if (compact) 48.dp else 56.dp
    val skipIconSize = if (compact) 30.dp else 36.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Track Button
        Box(
            modifier = Modifier
                .size(skipSize)
                .clip(CircleShape)
                .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                    haptics.tick()
                    onPrevious()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous track",
                tint = dominantColors.onBackground,
                modifier = Modifier.size(skipIconSize)
            )
        }

        Spacer(modifier = Modifier.size(if (compact) 24.dp else 36.dp))

        // Hero Play/Pause Button
        val playCircleColor = dominantColors.onBackground
        val playIconTint = if (playCircleColor.luminance() > 0.5f) Color.Black else Color.White
        Box(
            modifier = Modifier
                .size(playSize)
                .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                    haptics.tick()
                    onPlayPause()
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(playCircleColor),
                contentAlignment = Alignment.Center
            ) {
                val buttonState = when {
                    isLoading -> HeroPlaybackButtonState.BUFFERING
                    isPlaying -> HeroPlaybackButtonState.PLAYING
                    else -> HeroPlaybackButtonState.PAUSED
                }

                AnimatedContent(
                    targetState = buttonState,
                    transitionSpec = {
                        (scaleIn(spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium)) + fadeIn()) togetherWith
                        (scaleOut() + fadeOut())
                    },
                    label = "heroPlaybackState"
                ) { state ->
                    when (state) {
                        HeroPlaybackButtonState.BUFFERING -> {
                            SonzaLoadingLogo(
                                modifier = Modifier.size(playIconSize),
                                color = playIconTint
                            )
                        }
                        HeroPlaybackButtonState.PLAYING -> {
                            Icon(
                                imageVector = Icons.Default.Pause,
                                contentDescription = "Pause",
                                tint = playIconTint,
                                modifier = Modifier.size(playIconSize)
                            )
                        }
                        HeroPlaybackButtonState.PAUSED -> {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play",
                                tint = playIconTint,
                                modifier = Modifier.size(playIconSize)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(if (compact) 24.dp else 36.dp))

        // Next Track Button
        Box(
            modifier = Modifier
                .size(skipSize)
                .clip(CircleShape)
                .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                    haptics.tick()
                    onNext()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next track",
                tint = dominantColors.onBackground,
                modifier = Modifier.size(skipIconSize)
            )
        }
    }
}

/**
 * Secondary Controls Row: [Like] [Shuffle] [Repeat] [Queue] [Lyrics]
 */
@Composable
fun SecondaryPlayerControls(
    isFavorite: Boolean,
    onToggleLike: () -> Unit,
    shuffleEnabled: Boolean,
    onShuffleToggle: () -> Unit,
    repeatMode: RepeatMode,
    onRepeatToggle: () -> Unit,
    onShowQueue: () -> Unit,
    onShowLyrics: () -> Unit,
    dominantColors: DominantColors,
    modifier: Modifier = Modifier
) {
    val haptics = com.sonza.app.ui.utils.rememberHaptics()
    val buttonSize = 48.dp
    val iconSize = 24.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like / Favorite Button
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                    haptics.thump()
                    onToggleLike()
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isFavorite,
                transitionSpec = {
                    (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                    (scaleOut() + fadeOut())
                },
                label = "likeFavoriteSwap"
            ) { liked ->
                Icon(
                    imageVector = if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (liked) "Remove from favorites" else "Add to favorites",
                    tint = if (liked) dominantColors.accent else dominantColors.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        // Shuffle Button
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                    haptics.tick()
                    onShuffleToggle()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Shuffle,
                contentDescription = if (shuffleEnabled) "Shuffle on" else "Shuffle off",
                tint = if (shuffleEnabled) dominantColors.accent else dominantColors.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.size(iconSize)
            )
        }

        // Repeat Button
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                    haptics.tick()
                    onRepeatToggle()
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = repeatMode,
                transitionSpec = {
                    (scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()) togetherWith
                    (scaleOut() + fadeOut())
                },
                label = "repeatModeSwap"
            ) { mode ->
                Icon(
                    imageVector = when (mode) {
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = when (mode) {
                        RepeatMode.ONE -> "Repeat one"
                        RepeatMode.ALL -> "Repeat all"
                        RepeatMode.OFF -> "Repeat off"
                    },
                    tint = if (mode != RepeatMode.OFF) dominantColors.accent else dominantColors.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.size(iconSize)
                )
            }
        }

        // Queue Button
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                    haptics.tick()
                    onShowQueue()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                contentDescription = "Queue",
                tint = dominantColors.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.size(iconSize)
            )
        }

        // Lyrics Button
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .bounceClick(scaleDown = MotionTokens.CardTapScale) {
                    haptics.tick()
                    onShowLyrics()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lyrics,
                contentDescription = "Lyrics",
                tint = dominantColors.onBackground.copy(alpha = 0.6f),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Combined PlaybackControls for compatibility
 */
@Composable
fun PlaybackControls(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: RepeatMode,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onShuffleToggle: () -> Unit,
    onRepeatToggle: () -> Unit,
    dominantColors: DominantColors,
    compact: Boolean = false,
    isLoading: Boolean = false
) {
    MainPlaybackControls(
        isPlaying = isPlaying,
        isLoading = isLoading,
        onPlayPause = onPlayPause,
        onNext = onNext,
        onPrevious = onPrevious,
        dominantColors = dominantColors,
        compact = compact
    )
}

private enum class HeroPlaybackButtonState {
    BUFFERING,
    PLAYING,
    PAUSED
}
