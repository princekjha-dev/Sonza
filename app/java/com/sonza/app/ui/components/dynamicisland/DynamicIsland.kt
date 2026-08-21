package com.sonza.app.ui.components.dynamicisland

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.sonza.app.core.model.Song
import com.sonza.app.ui.theme.SonzaColors
import com.sonza.app.ui.theme.SonzaTypography

/**
 * Production-quality Dynamic Island-style floating music pill.
 *
 * Driven strictly by the authoritative playback state:
 * - Persistent & stable: Remains visible throughout song playback and pause.
 * - State-driven transitions: Smooth spring animation between idle, compact, and expanded.
 * - Non-blocking: When compact, it does not intercept touch gestures on the underlying screen.
 * - Rich controls: Expands on tap to provide interactive progress scrubbing, track controls, and favorites.
 */
@Composable
fun DynamicIsland(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isLiked: Boolean,
    isVisible: Boolean = currentSong != null,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onLikeToggle: () -> Unit,
    onOpenFullPlayer: () -> Unit = {},
    modifier: Modifier = Modifier,
    topInset: Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }

    // If visibility is revoked (e.g. song ended or player opened), collapse expanded state
    LaunchedEffect(isVisible) {
        if (!isVisible) {
            isExpanded = false
        }
    }

    if (isExpanded) {
        BackHandler {
            isExpanded = false
        }
    }

    val islandCornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else 22.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "IslandCornerRadius"
    )

    val islandElevation by animateDpAsState(
        targetValue = if (isExpanded) 16.dp else 8.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "IslandElevation"
    )

    // Outer wrapper: Full-screen scrim when expanded; minimal top-aligned container when compact
    Box(
        modifier = if (isExpanded) {
            modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { isExpanded = false }
                }
        } else {
            modifier.wrapContentSize(Alignment.TopCenter)
        },
        contentAlignment = Alignment.TopCenter
    ) {
        // Dim scrim background when expanded
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(250)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
            )
        }

        // The Island Capsule with smooth spring entrance/exit
        AnimatedVisibility(
            visible = isVisible && currentSong != null,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                    scaleIn(
                        initialScale = 0.85f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
            exit = fadeOut(tween(180)) +
                    scaleOut(
                        targetScale = 0.85f,
                        animationSpec = tween(180)
                    )
        ) {
            val song = currentSong ?: return@AnimatedVisibility

            Surface(
                modifier = Modifier
                    .padding(top = topInset + 6.dp)
                    .padding(horizontal = 16.dp)
                    .shadow(
                        elevation = islandElevation,
                        shape = RoundedCornerShape(islandCornerRadius),
                        ambientColor = Color.Black.copy(alpha = 0.60f),
                        spotColor = Color.Black.copy(alpha = 0.50f)
                    )
                    .clip(RoundedCornerShape(islandCornerRadius))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            if (!isExpanded) isExpanded = true
                        }
                    )
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ),
                shape = RoundedCornerShape(islandCornerRadius),
                color = Color(0xFF000000),
                border = BorderStroke(
                    width = 0.75.dp,
                    color = Color.White.copy(alpha = if (isExpanded) 0.22f else 0.14f)
                )
            ) {
                if (isExpanded) {
                    ExpandedIslandContent(
                        song = song,
                        isPlaying = isPlaying,
                        currentPosition = currentPosition,
                        duration = duration,
                        isLiked = isLiked,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        onPrevious = onPrevious,
                        onSeekTo = onSeekTo,
                        onLikeToggle = onLikeToggle,
                        onOpenFullPlayer = {
                            isExpanded = false
                            onOpenFullPlayer()
                        },
                        onCollapse = { isExpanded = false }
                    )
                } else {
                    CompactIslandContent(
                        song = song,
                        isPlaying = isPlaying
                    )
                }
            }
        }
    }
}

/**
 * Compact Pill state: Minimal footprint near top cutout.
 * Shows artwork, title marquee, and synchronized equalizer / paused indicator.
 */
@Composable
private fun CompactIslandContent(
    song: Song,
    isPlaying: Boolean
) {
    Row(
        modifier = Modifier
            .height(44.dp)
            .widthIn(min = 195.dp, max = 250.dp)
            .padding(start = 6.dp, end = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Leading Album Artwork
        AsyncImage(
            model = song.thumbnailUrl,
            contentDescription = "Album Art",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1E)),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Center Song Title & Artist
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                style = SonzaTypography.BodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (song.artist.isNotBlank()) {
                Text(
                    text = song.artist,
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.60f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Trailing Animated Soundwave Equalizer (smoothly settles when paused)
        SoundwaveVisualizer(isPlaying = isPlaying)
    }
}

/**
 * Expanded state: Full rich music controls with continuous spring animation.
 */
@Composable
private fun ExpandedIslandContent(
    song: Song,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isLiked: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onLikeToggle: () -> Unit,
    onOpenFullPlayer: () -> Unit,
    onCollapse: () -> Unit
) {
    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f

    Column(
        modifier = Modifier
            .widthIn(min = 320.dp, max = 370.dp)
            .padding(18.dp)
    ) {
        // Top Header with Artwork, Metadata, Full-Player Expand, and Collapse/Close Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = "Album Artwork",
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF1C1C1E))
                    .clickable(onClick = onOpenFullPlayer),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onOpenFullPlayer)
            ) {
                Text(
                    text = song.title,
                    style = SonzaTypography.BodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = song.artist,
                    style = SonzaTypography.BodySmall,
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onLikeToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = "Like Song",
                    tint = if (isLiked) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.70f),
                    modifier = Modifier.size(20.dp)
                )
            }

            IconButton(
                onClick = onOpenFullPlayer,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.OpenInFull,
                    contentDescription = "Open Full Player",
                    tint = Color.White.copy(alpha = 0.60f),
                    modifier = Modifier.size(17.dp)
                )
            }

            IconButton(
                onClick = onCollapse,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Collapse Island",
                    tint = Color.White.copy(alpha = 0.60f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress Bar with Time Labels
        Column(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value = progress,
                onValueChange = { newProgress ->
                    val newPos = (newProgress * duration).toLong()
                    onSeekTo(newPos)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = if (isPlaying) SonzaColors.ActivePlayback else Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.20f)
                )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPosition),
                    style = SonzaTypography.LabelSmall,
                    color = Color.White.copy(alpha = 0.50f)
                )
                Text(
                    text = formatTime(duration),
                    style = SonzaTypography.LabelSmall,
                    color = Color.White.copy(alpha = 0.50f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playback Control Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous Song",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Big Play/Pause Button
            Surface(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPlayPause),
                color = if (isPlaying) SonzaColors.ActivePlayback else Color(0xFF2C2C2E),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) Color.Black else Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next Song",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

/**
 * Programmatic audio waveform / equalizer bars indicating active playback without any GIFs.
 * When paused, bars smoothly settle into a subtle static indicator.
 */
@Composable
private fun SoundwaveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SoundwaveTransition")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )

    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )

    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )

    Row(
        modifier = modifier
            .height(20.dp)
            .padding(end = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barColor = if (isPlaying) SonzaColors.ActivePlayback else Color.White.copy(alpha = 0.40f)

        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(if (isPlaying) bar1Height.dp else 4.dp)
                .clip(CircleShape)
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(if (isPlaying) bar2Height.dp else 8.dp)
                .clip(CircleShape)
                .background(barColor)
        )
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(if (isPlaying) bar3Height.dp else 4.dp)
                .clip(CircleShape)
                .background(barColor)
        )
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
