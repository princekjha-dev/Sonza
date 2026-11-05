package com.sonza.app.ui.components.player

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import com.sonza.app.core.model.Song
import com.sonza.app.core.model.MiniPlayerStyle
import com.sonza.app.ui.components.DominantColors
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

import com.sonza.app.ui.components.player.miniplayer.LiquidGlassMiniPlayer
import com.sonza.app.ui.components.player.miniplayer.PillMiniPlayer
import com.sonza.app.ui.components.player.miniplayer.StandardMiniPlayer
import com.sonza.app.ui.components.player.miniplayer.YTMusicMiniPlayer

/**
 * YouTube Music-style expandable player sheet.
 *
 * This composable manages both the collapsed mini-player row and the expanded
 * full-player content in a single, continuously draggable panel.
 *
 * Architecture:
 * - expansion = 0f → Collapsed mini player (64dp peek)
 * - expansion = 1f → Full-screen player
 * - 0..1 → Smooth interpolation of height, artwork, text alpha
 *
 * Drag gesture is handled internally. The parent only
 * needs to provide a slot for the expanded content.
 */

import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import com.sonza.app.navigation.Destination
import com.sonza.app.ui.components.FloatingNavCircleButton
import com.sonza.app.ui.components.player.miniplayer.CompactFloatingMiniPlayer
import com.sonza.app.ui.components.LocalSonzaDynamicColors
import com.sonza.app.ui.theme.SpacingTokens

private val MiniPlayerHeight = 52.dp

@Composable
fun ExpandablePlayerSheet(
    currentSong: Song?,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    progressProvider: () -> Float,
    dominantColors: DominantColors,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    bottomPadding: Float,
    isExpanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    userAlpha: Float = 0f,
    swipeDownToDismissEnabled: Boolean = true,
    style: MiniPlayerStyle = MiniPlayerStyle.YT_MUSIC,
    artworkShape: String = "ROUNDED_SQUARE",
    glassBlurAmount: Float = 50f,
    currentDestination: Destination = Destination.Home,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    isPhoneLike: Boolean = true,
    expandedContent: @Composable (onCollapse: () -> Unit) -> Unit
) {
    var lastSong by remember { mutableStateOf<Song?>(currentSong) }
    LaunchedEffect(currentSong) {
        if (currentSong != null) {
            lastSong = currentSong
        }
    }
    val song = currentSong ?: lastSong ?: return
    val coroutineScope = rememberCoroutineScope()
    val dynamicColors = LocalSonzaDynamicColors.current
    val accentColor = dynamicColors.accent

    // Animation State
    val expansion = remember { Animatable(if (isExpanded) 1f else 0f) }

    // Sync with external state
    LaunchedEffect(isExpanded) {
        val target = if (isExpanded) 1f else 0f
        if (expansion.value != target) {
            expansion.animateTo(
                targetValue = target,
                animationSpec = tween(
                    durationMillis = 350,
                    easing = FastOutSlowInEasing
                )
            )
        }
    }

    val density = LocalDensity.current
    val view = LocalView.current
    val screenHeightPx = view.height.toFloat()
    val miniPlayerHeightPx = with(density) { MiniPlayerHeight.toPx() }

    val stylePaddingOffset = 0f
    val adjustedBottomPadding = (bottomPadding - stylePaddingOffset).coerceAtLeast(0f)

    val collapsedHeightPx = miniPlayerHeightPx + adjustedBottomPadding
    val dragRange = (screenHeightPx - (miniPlayerHeightPx + bottomPadding)).coerceAtLeast(1f)

    // Back Handler to collapse on system back gesture
    BackHandler(enabled = isExpanded) {
        onExpandChange(false)
        coroutineScope.launch {
            expansion.animateTo(0f, tween(300, easing = FastOutSlowInEasing))
        }
    }

    // The entire expandable panel. Height / alpha / offset all interpolate with the
    // drag `expansion`, but we read `expansion.value` ONLY inside layout/draw-phase
    // lambdas (.layout{}, .graphicsLayer{}, .offset{}) — never in the composition
    // scope. That keeps this sheet (and the heavy expandedContent / PlayerScreen it
    // hosts) from recomposing on every animation frame; the interpolation still runs
    // smoothly because layout/draw re-read the value each frame without recomposing.
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Panel height: lerp from mini player (+ padding) to full screen, measured
            // in the layout phase so a height change doesn't trigger recomposition.
            .layout { measurable, constraints ->
                val h = ((miniPlayerHeightPx + bottomPadding) +
                    dragRange * expansion.value.coerceAtLeast(0f)).roundToInt()
                val placeable = measurable.measure(constraints.copy(minHeight = h, maxHeight = h))
                layout(placeable.width, h) { placeable.place(0, 0) }
            }
    ) {
        // ── Collapsed Mini Player Row ──
        // Visible when expansion < ~0.4 (fades out as it expands) or while it's being
        // swipe-dismissed (expansion < 0). Gated by a derivedStateOf boolean so this
        // block is added/removed at most once per transition, not every frame.
        val showMiniPlayer by remember { derivedStateOf { expansion.value < 0.4f } }
        // Horizontal swipe on the mini player skips tracks (left = next, right =
        // previous), matching the YT Music / Spotify convention. The row follows
        // the finger and springs back after the skip fires.
        val horizontalDrag = remember { Animatable(0f) }
        val skipThresholdPx = with(density) { 96.dp.toPx() }
        if (showMiniPlayer) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(MiniPlayerHeight)
                    .align(Alignment.TopCenter)
                    .offset {
                        val e = expansion.value
                        val px = if (e >= 0f) (bottomPadding - adjustedBottomPadding) * (1f - e) else 0f
                        IntOffset(0, px.roundToInt())
                    }
                    .graphicsLayer {
                        translationX = horizontalDrag.value
                        alpha = (1f - expansion.value * 2.5f).coerceIn(0f, 1f)
                        if (expansion.value < 0f && swipeDownToDismissEnabled) {
                            translationY = -expansion.value * dragRange
                            alpha = (1f + expansion.value * 1.5f).coerceIn(0.25f, 1f)
                        }
                    }
                    .zIndex(if (isExpanded) 0f else 1f)
            ) {
                CollapsedMiniPlayer(
                    song = song,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    dominantColors = dominantColors,
                    progressProvider = progressProvider,
                    onPlayPause = onPlayPause,
                    onNext = onNext,
                    onClose = onClose,
                    userAlpha = userAlpha,
                    style = style,
                    artworkShape = artworkShape,
                    glassBlurAmount = glassBlurAmount,
                    onTap = {
                        coroutineScope.launch {
                            expansion.animateTo(
                                targetValue = 1f,
                                animationSpec = tween(
                                    durationMillis = 350,
                                    easing = FastOutSlowInEasing
                                )
                            )
                            onExpandChange(true)
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        val total = horizontalDrag.value
                                        when {
                                            total <= -skipThresholdPx -> onNext()
                                            total >= skipThresholdPx -> onPrevious()
                                        }
                                        horizontalDrag.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        horizontalDrag.animateTo(0f, tween(200, easing = FastOutSlowInEasing))
                                    }
                                },
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        horizontalDrag.snapTo(horizontalDrag.value + dragAmount)
                                    }
                                }
                            )
                        }
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    coroutineScope.launch {
                                        val targetValue = if (expansion.value > 0.35f) 1f else 0f
                                        expansion.animateTo(
                                            targetValue = targetValue,
                                            animationSpec = tween(
                                                durationMillis = 250,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        onExpandChange(targetValue == 1f)
                                    }
                                },
                                onDragCancel = {
                                    coroutineScope.launch {
                                        val targetValue = if (expansion.value > 0.35f) 1f else 0f
                                        expansion.animateTo(
                                            targetValue = targetValue,
                                            animationSpec = tween(
                                                durationMillis = 250,
                                                easing = FastOutSlowInEasing
                                            )
                                        )
                                        onExpandChange(targetValue == 1f)
                                    }
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    val delta = -dragAmount / dragRange
                                    coroutineScope.launch {
                                        expansion.snapTo(
                                            (expansion.value + delta).coerceIn(0f, 1f)
                                        )
                                    }
                                }
                            )
                        }
                )
            }
        }

        // ── Expanded Full Player ──
        // Composed only once expansion crosses ~0.3. Gated by a derivedStateOf so the
        // heavy expandedContent (full PlayerScreen) is added at most once per
        // transition rather than re-invoked on every animation frame.
        val showFullPlayer by remember { derivedStateOf { expansion.value > 0.3f } }
        if (showFullPlayer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = ((expansion.value - 0.3f) / 0.7f).coerceIn(0f, 1f) }
                    .zIndex(if (isExpanded) 1f else 0f)
                    // Add gesture detection to Full Player
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                // Threshold 0.8f makes it easier to collapse (only need to drag down 20%)
                                val targetValue = if (expansion.value > 0.8f) 1f else 0f 
                                
                                coroutineScope.launch {
                                     expansion.animateTo(
                                        targetValue = targetValue,
                                        animationSpec = tween(
                                            durationMillis = 250,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    onExpandChange(targetValue == 1f)
                                }
                            },
                            onDragCancel = {
                                coroutineScope.launch {
                                    val targetValue = if (expansion.value > 0.8f) 1f else 0f
                                    expansion.animateTo(
                                        targetValue = targetValue,
                                        animationSpec = tween(
                                            durationMillis = 250,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                    onExpandChange(targetValue == 1f)
                                }
                            },
                            onVerticalDrag = { change, dragAmount ->
                                change.consume()
                                val delta = -dragAmount / dragRange
                                coroutineScope.launch {
                                    expansion.snapTo(
                                        (expansion.value + delta).coerceIn(0f, 1f)
                                    )
                                }
                            }
                        )
                    }
            ) {
                expandedContent {
                    // onCollapse callback
                    coroutineScope.launch {
                        expansion.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = FastOutSlowInEasing
                            )
                        )
                        onExpandChange(false)
                    }
                }
            }
        }
    }
}

/**
 * The collapsed mini player row — a compact horizontal bar showing
 * artwork, song title/artist, and play/next controls.
 */
@Composable
private fun CollapsedMiniPlayer(
    song: Song,
    isPlaying: Boolean,
    isLoading: Boolean = false,
    dominantColors: DominantColors,
    progressProvider: () -> Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClose: () -> Unit,
    onTap: () -> Unit,
    userAlpha: Float = 0f,
    style: MiniPlayerStyle = MiniPlayerStyle.YT_MUSIC,
    artworkShape: String = "ROUNDED_SQUARE",
    glassBlurAmount: Float = 50f,
    modifier: Modifier = Modifier
) {
    when (style) {
        MiniPlayerStyle.LIQUID_GLASS -> {
            LiquidGlassMiniPlayer(
                song = song,
                isPlaying = isPlaying,
                isLoading = isLoading,
                dominantColors = dominantColors,
                progressProvider = progressProvider,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onClose = onClose,
                onTap = onTap,
                userAlpha = userAlpha,
                artworkShape = artworkShape,
                blurAmount = glassBlurAmount,
                modifier = modifier
            )
        }
        MiniPlayerStyle.FLOATING_PILL -> {
            PillMiniPlayer(
                song = song,
                isPlaying = isPlaying,
                isLoading = isLoading,
                dominantColors = dominantColors,
                progressProvider = progressProvider,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onClose = onClose,
                onTap = onTap,
                userAlpha = userAlpha,
                artworkShape = artworkShape,
                modifier = modifier
            )
        }
        MiniPlayerStyle.YT_MUSIC -> {
            YTMusicMiniPlayer(
                song = song,
                isPlaying = isPlaying,
                isLoading = isLoading,
                dominantColors = dominantColors,
                progressProvider = progressProvider,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onClose = onClose,
                onTap = onTap,
                userAlpha = userAlpha,
                artworkShape = artworkShape,
                modifier = modifier
            )
        }
        else -> {
            StandardMiniPlayer(
                song = song,
                isPlaying = isPlaying,
                isLoading = isLoading,
                dominantColors = dominantColors,
                progressProvider = progressProvider,
                onPlayPause = onPlayPause,
                onNext = onNext,
                onClose = onClose,
                onTap = onTap,
                userAlpha = userAlpha,
                artworkShape = artworkShape,
                modifier = modifier
            )
        }
    }
}

