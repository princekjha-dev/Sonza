package com.sonza.app.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlinx.coroutines.launch

/**
 * Modifier attached to horizontally scrollable components (LazyRow, carousels)
 * so that horizontal swipe gestures starting inside them are fully contained and consumed locally,
 * completely preventing accidental triggering of page-level navigation (e.g. Home -> Search).
 *
 * Characteristics:
 * - Allows normal tapping of cards without consuming touch-down or cancelling clicks.
 * - Allows uninhibited vertical scrolling of parent lists (e.g. LazyColumn) when dragging vertically.
 * - Consumes horizontal drag delta so parent [horizontalSwipeNavigation] detects child consumption and yields.
 */
fun Modifier.carouselSwipeShield(): Modifier {
    return this.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            var totalDx = 0f
            var totalDy = 0f
            var isHorizontal = false
            var isVertical = false
            val slop = 6.dp.toPx()

            do {
                val event = awaitPointerEvent(pass = PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                val drag = change.positionChange()
                totalDx += drag.x
                totalDy += drag.y

                val absDx = abs(totalDx)
                val absDy = abs(totalDy)

                if (!isHorizontal && !isVertical) {
                    if (absDy > slop && absDy > absDx * 1.15f) {
                        isVertical = true
                    } else if (absDx > slop && absDx > absDy * 1.15f) {
                        isHorizontal = true
                    }
                }

                if (isHorizontal) {
                    // Consume horizontal drag delta so parent page swipe navigation is cancelled
                    change.consume()
                }

                if (isVertical) {
                    // Vertical drag -> let parent LazyColumn handle vertical scrolling smoothly
                    break
                }
            } while (event.changes.any { it.pressed })
        }
    }
}

/**
 * Modifier that attaches Apple Music-style interactive horizontal page swipe navigation.
 *
 * Characteristics:
 * - Real-time finger following: Page shifts smoothly with the user's finger via [translationX].
 * - Direction-slop filtered: If initial movement is vertical, immediately yields to allow
 *   uninhibited vertical scrolling of lists (LazyColumn, ScrollableColumn).
 * - Child-gesture aware: If any child horizontally scrollable component (e.g. LazyRow carousel)
 *   consumes the horizontal gesture, this modifier immediately yields and will NOT trigger page navigation.
 * - Rubber-band resistance: When dragging past boundaries (e.g. Home swiping right or Profile swiping left),
 *   applies elastic damping and snaps back cleanly on release.
 * - Settle animation: If swipe distance/velocity does not cross the threshold, smoothly animates back to 0.
 * - Single-event locking: Prevents rapid multi-swipes from queueing repeated navigation events.
 */
fun Modifier.horizontalSwipeNavigation(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    threshold: Dp = 72.dp,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled || (onSwipeLeft == null && onSwipeRight == null)) return@composed this

    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var isNavigating by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    val minVelocityThreshold = with(density) { 700.dp.toPx() }
    val maxDragDampingPx = with(density) { 36.dp.toPx() }

    DisposableEffect(Unit) {
        onDispose {
            isNavigating = false
        }
    }

    this
        .graphicsLayer {
            translationX = offsetX.value
        }
        .pointerInput(enabled, onSwipeLeft, onSwipeRight) {
            val velocityTracker = VelocityTracker()
            val touchSlop = viewConfiguration.touchSlop

            awaitEachGesture {
                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                velocityTracker.resetTracking()
                velocityTracker.addPosition(down.uptimeMillis, down.position)

                var totalDx = 0f
                var totalDy = 0f
                var isHorizontalIntent = false
                var isVerticalScrolling = false
                var isConsumedByChild = false

                if (isNavigating) return@awaitEachGesture

                do {
                    val event = awaitPointerEvent(pass = PointerEventPass.Main)
                    val dragChange = event.changes.firstOrNull { it.id == down.id } ?: break

                    if (dragChange.isConsumed) {
                        isConsumedByChild = true
                        break
                    }

                    velocityTracker.addPosition(dragChange.uptimeMillis, dragChange.position)
                    val positionChange = dragChange.positionChange()
                    totalDx += positionChange.x
                    totalDy += positionChange.y

                    val absDx = abs(totalDx)
                    val absDy = abs(totalDy)

                    // Determine gesture direction once movement exceeds initial slop
                    if (!isHorizontalIntent && !isVerticalScrolling) {
                        if (absDy > touchSlop && absDy > absDx * 1.25f) {
                            // Gesture is vertical -> let vertical scrolling handle everything
                            isVerticalScrolling = true
                        } else if (absDx > touchSlop && absDx > absDy * 1.25f) {
                            // Gesture is horizontal
                            isHorizontalIntent = true
                        }
                    }

                    if (isHorizontalIntent) {
                        // Consume horizontal drag delta so parent or other components don't interfere
                        dragChange.consume()

                        val rawDx = totalDx - (if (totalDx > 0) touchSlop else -touchSlop)
                        val targetOffset = when {
                            rawDx > 0 && onSwipeRight == null -> {
                                // Swiping right but no previous page (e.g. Home): rubber-band damping
                                (rawDx * 0.22f).coerceAtMost(maxDragDampingPx)
                            }
                            rawDx < 0 && onSwipeLeft == null -> {
                                // Swiping left but no next page (e.g. Profile): rubber-band damping
                                (rawDx * 0.22f).coerceAtLeast(-maxDragDampingPx)
                            }
                            else -> rawDx
                        }

                        coroutineScope.launch {
                            offsetX.snapTo(targetOffset)
                        }
                    }

                    if (isVerticalScrolling || isConsumedByChild) {
                        break
                    }
                } while (event.changes.any { it.pressed })

                if (isConsumedByChild || isVerticalScrolling || !isHorizontalIntent) {
                    if (offsetX.value != 0f) {
                        coroutineScope.launch {
                            offsetX.animateTo(
                                0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }
                    return@awaitEachGesture
                }

                // Pointer released — calculate velocity and determine whether to commit navigation
                val velocityX = velocityTracker.calculateVelocity().x
                val currentOffset = offsetX.value

                val isFastSwipeRight = velocityX > minVelocityThreshold && currentOffset > 24.dp.toPx()
                val isFastSwipeLeft = velocityX < -minVelocityThreshold && currentOffset < -24.dp.toPx()
                val isDistanceSwipeRight = currentOffset >= thresholdPx
                val isDistanceSwipeLeft = currentOffset <= -thresholdPx

                val shouldNavigateRight = (isDistanceSwipeRight || isFastSwipeRight) && onSwipeRight != null
                val shouldNavigateLeft = (isDistanceSwipeLeft || isFastSwipeLeft) && onSwipeLeft != null

                if (shouldNavigateRight && !isNavigating) {
                    isNavigating = true
                    coroutineScope.launch {
                        try {
                            onSwipeRight?.invoke()
                        } finally {
                            offsetX.snapTo(0f)
                        }
                    }
                } else if (shouldNavigateLeft && !isNavigating) {
                    isNavigating = true
                    coroutineScope.launch {
                        try {
                            onSwipeLeft?.invoke()
                        } finally {
                            offsetX.snapTo(0f)
                        }
                    }
                } else {
                    // Settle back to original position with smooth spring animation
                    coroutineScope.launch {
                        offsetX.animateTo(
                            0f,
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessMediumLow
                            )
                        )
                    }
                }
            }
        }
}
