package com.sonza.app.ui.components.dynamicisland

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.sonza.app.R
import com.sonza.app.core.model.Song
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.theme.SonzaTypography

/**
 * Premium Dynamic Island Floating Music Pill with custom image assets and system overlay support.
 *
 * Positioned seamlessly below the camera cutout / top status bar.
 * - Compact State: Sleek 36dp squircle pill hugging the hardware cutout with album artwork,
 *   smooth marquee song title, dynamic loading indicator, and 4-bar audio waveform visualizer.
 * - Expanded State: Fluid spring-morph transition to full floating playback deck with high-res art,
 *   custom image playback controls (play/pause, rewind, forward, favorite, close),
 *   seek scrubber, and responsive animations.
 */
@Composable
fun DynamicIsland(
    currentSong: Song?,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    isLiked: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onLikeToggle: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    onOpenApp: (() -> Unit)? = null,
    onExpandChange: ((Boolean) -> Unit)? = null,
    topInset: Dp = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
) {
    if (currentSong == null) return

    var isExpanded by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val islandCornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else 20.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMediumLow),
        label = "IslandCornerRadius"
    )

    val islandElevation by animateDpAsState(
        targetValue = if (isExpanded) 20.dp else 10.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "IslandElevation"
    )

    val topPadding = remember(topInset) {
        if (topInset > 0.dp) (topInset - 2.dp).coerceAtLeast(6.dp) else 8.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(
                if (isExpanded) {
                    Modifier.pointerInput(Unit) {
                        detectTapGestures {
                            isExpanded = false
                            onExpandChange?.invoke(false)
                        }
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        // Dim scrim backdrop when expanded
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(180))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.50f))
            )
        }

        // Island Capsule
        Surface(
            modifier = Modifier
                .padding(top = topPadding)
                .padding(horizontal = 12.dp)
                .shadow(
                    elevation = islandElevation,
                    shape = RoundedCornerShape(islandCornerRadius),
                    ambientColor = Color.Black.copy(alpha = 0.70f),
                    spotColor = Color.Black.copy(alpha = 0.55f)
                )
                .clip(RoundedCornerShape(islandCornerRadius))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {
                        if (!isExpanded) {
                            isExpanded = true
                            onExpandChange?.invoke(true)
                        }
                    }
                )
                .draggable(
                    orientation = Orientation.Vertical,
                    state = rememberDraggableState { delta ->
                        if (delta > 15f && !isExpanded) {
                            isExpanded = true
                            onExpandChange?.invoke(true)
                        } else if (delta < -15f && isExpanded) {
                            isExpanded = false
                            onExpandChange?.invoke(false)
                        }
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
            border = androidx.compose.foundation.BorderStroke(
                width = 0.85.dp,
                color = Color.White.copy(alpha = if (isExpanded) 0.22f else 0.14f)
            )
        ) {
            if (isExpanded) {
                ExpandedIslandContent(
                    song = currentSong,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    currentPosition = currentPosition,
                    duration = duration,
                    isLiked = isLiked,
                    screenWidth = screenWidth,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSeekTo = onSeekTo,
                    onLikeToggle = onLikeToggle,
                    onOpenApp = onOpenApp,
                    onCollapse = {
                        isExpanded = false
                        onExpandChange?.invoke(false)
                    }
                )
            } else {
                CompactIslandContent(
                    song = currentSong,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    onOpenApp = onOpenApp
                )
            }
        }
    }
}

/**
 * Compact Pill state: Sleek 36dp hardware cutout music capsule using image assets.
 */
@Composable
private fun CompactIslandContent(
    song: Song,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    onOpenApp: (() -> Unit)? = null
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .height(36.dp)
            .widthIn(min = 175.dp, max = 235.dp)
            .padding(start = 5.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Leading Mini Artwork
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .border(0.6.dp, Color.White.copy(alpha = 0.20f), CircleShape)
                .background(Color(0xFF18181A))
                .then(
                    if (onOpenApp != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenApp
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(song.thumbnailUrl?.takeIf { it.isNotBlank() } ?: R.drawable.di_album_placeholder)
                    .crossfade(true)
                    .build(),
                contentDescription = "Album Art",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center Song Title with Marquee
        Text(
            text = if (isLoading && song.title.isBlank()) "Buffering..." else song.title,
            style = SonzaTypography.NavLabel.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            ),
            color = Color.White,
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .basicMarquee(iterations = Int.MAX_VALUE)
                .then(
                    if (onOpenApp != null) {
                        Modifier.clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenApp
                        )
                    } else Modifier
                )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Trailing Waveform Visualizer or Single Loading GIF
        if (isLoading) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.raw.loding)
                    .crossfade(false)
                    .build(),
                contentDescription = "Buffering",
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            SoundwaveVisualizer(isPlaying = isPlaying, accentColor = accentColor)
        }
    }
}

/**
 * Expanded state: Full rich music controls with image assets and spring physics.
 */
@Composable
private fun ExpandedIslandContent(
    song: Song,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    currentPosition: Long,
    duration: Long,
    isLiked: Boolean,
    screenWidth: Dp,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onLikeToggle: () -> Unit,
    onOpenApp: (() -> Unit)? = null,
    onCollapse: () -> Unit
) {
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent
    val context = LocalContext.current
    val progress = if (duration > 0) (currentPosition.toFloat() / duration.toFloat()).coerceIn(0f, 1f) else 0f
    val targetWidth = (screenWidth - 32.dp).coerceIn(310.dp, 360.dp)

    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.15f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "likeScale"
    )

    Column(
        modifier = Modifier
            .width(targetWidth)
            .padding(16.dp)
    ) {
        // Top Header: Artwork, Title, Artist, Like & Collapse
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(0.75.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                    .background(Color(0xFF18181A))
                    .then(
                        if (onOpenApp != null) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenApp
                            )
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(song.thumbnailUrl?.takeIf { it.isNotBlank() } ?: R.drawable.di_album_placeholder)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Album Artwork",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (onOpenApp != null) {
                            Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onOpenApp
                            )
                        } else Modifier
                    )
            ) {
                Text(
                    text = song.title,
                    style = SonzaTypography.SongTitle.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = song.artist,
                    style = SonzaTypography.ArtistSubtitle.copy(
                        fontSize = 13.sp
                    ),
                    color = Color.White.copy(alpha = 0.65f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onLikeToggle,
                modifier = Modifier
                    .size(36.dp)
                    .scale(likeScale)
            ) {
                Icon(
                    painter = painterResource(if (isLiked) R.drawable.di_favorite_filled else R.drawable.di_favorite_empty),
                    contentDescription = "Like Song",
                    tint = if (isLiked) Color(0xFFFF2D55) else Color.White.copy(alpha = 0.70f),
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f))
            ) {
                Icon(
                    painter = painterResource(R.drawable.di_close),
                    contentDescription = "Collapse Island",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(14.dp)
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
                    activeTrackColor = accentColor,
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
                    style = SonzaTypography.LabelSmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.50f)
                )
                Text(
                    text = formatTime(duration),
                    style = SonzaTypography.LabelSmall.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.50f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Playback Controls using image assets
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
                    painter = painterResource(R.drawable.di_rewind),
                    contentDescription = "Previous Song",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }

            // Central Play/Pause Button with Loading state support
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onPlayPause),
                color = if (isPlaying) accentColor else Color(0xFF2C2C2E),
                shape = CircleShape,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(R.raw.loding)
                                .crossfade(false)
                                .build(),
                            contentDescription = "Buffering",
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.di_pause else R.drawable.di_play),
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) Color.Black else Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.di_forward),
                    contentDescription = "Next Song",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

/**
 * 4-bar dynamic soundwave visualizer.
 */
@Composable
private fun SoundwaveVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SoundwaveTransition")

    val bar1Height by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 380, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar1"
    )

    val bar2Height by infiniteTransition.animateFloat(
        initialValue = 14f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar2"
    )

    val bar3Height by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 420, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar3"
    )

    val bar4Height by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 460, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Bar4"
    )

    Row(
        modifier = modifier
            .height(18.dp)
            .padding(end = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val barColor = if (isPlaying) accentColor else Color.White.copy(alpha = 0.35f)

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
                .height(if (isPlaying) bar2Height.dp else 7.dp)
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
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .height(if (isPlaying) bar4Height.dp else 6.dp)
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
