package com.sonza.app.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Global navigation lock coordinating root page swipe transitions.
 * Guarantees that:
 * 1. Exactly ONE completed swipe produces exactly ONE page transition.
 * 2. Rapid or multi-touch gestures cannot queue multiple page transitions.
 * 3. A cooldown lock prevents another page trigger while a transition is in flight.
 */
object RootNavLock {
    @Volatile
    private var isTransitionRunning = false

    @Volatile
    private var lastCommitTime = 0L

    /** Duration in ms matching the NavHost slide transition (300ms) plus a safety cooldown buffer. */
    const val COOLDOWN_MS = 380L

    private val lockScope = CoroutineScope(Dispatchers.Main.immediate)

    fun canNavigate(): Boolean {
        val now = System.currentTimeMillis()
        return !isTransitionRunning && (now - lastCommitTime > COOLDOWN_MS)
    }

    fun commitNavigation(action: () -> Unit): Boolean {
        val now = System.currentTimeMillis()
        synchronized(this) {
            if (isTransitionRunning || (now - lastCommitTime <= COOLDOWN_MS)) {
                return false
            }
            isTransitionRunning = true
            lastCommitTime = now
        }

        try {
            action()
        } finally {
            lockScope.launch {
                delay(COOLDOWN_MS)
                isTransitionRunning = false
            }
        }
        return true
    }

    fun reset() {
        isTransitionRunning = false
        lastCommitTime = 0L
    }
}

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
            val slop = 8.dp.toPx()

            do {
                val event = awaitPointerEvent(pass = PointerEventPass.Main)
                val change = event.changes.firstOrNull { it.id == down.id } ?: break

                val drag = change.positionChange()
                totalDx += drag.x
                totalDy += drag.y

                val absDx = abs(totalDx)
                val absDy = abs(totalDy)

                if (!isHorizontal && !isVertical) {
                    if (absDy > slop && absDy > absDx * 1.2f) {
                        isVertical = true
                    } else if (absDx > slop && absDx > absDy * 1.2f) {
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
 * Modifier that attaches gesture-driven horizontal page swipe navigation.
 *
 * Characteristics:
 * - Real-time finger following: Page shifts smoothly with the user's finger via [translationX].
 * - Direction-slop filtered: Requires strict horizontal dominance (horizontal > vertical * 1.75)
 *   so vertical list scrolling is never hijacked.
 * - Child-gesture aware: If any child horizontally scrollable component (e.g. LazyRow carousel)
 *   consumes the horizontal gesture, this modifier immediately yields.
 * - Boundary resistance: When dragging past boundaries (e.g. Home swiping right or Profile swiping left),
 *   applies elastic damping and snaps back cleanly on release.
 * - Settle animation: If swipe distance/velocity does not cross the threshold, smoothly springs back to 0.
 * - Transition locking: Coordinated with [RootNavLock] to ensure 1 swipe = 1 page jump.
 */
fun Modifier.horizontalSwipeNavigation(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    threshold: Dp = 80.dp,
    enabled: Boolean = true
): Modifier = composed {
    if (!enabled || (onSwipeLeft == null && onSwipeRight == null)) return@composed this

    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val density = LocalDensity.current
    val thresholdPx = with(density) { threshold.toPx() }
    val minVelocityThreshold = with(density) { 800.dp.toPx() }
    val minFlickDistancePx = with(density) { 36.dp.toPx() }
    val maxDragDampingPx = with(density) { 40.dp.toPx() }

    DisposableEffect(Unit) {
        onDispose {
            // Cleanup on leaving composition
        }
    }

    this
        .graphicsLayer {
            translationX = offsetX.value
        }
        .pointerInput(enabled, onSwipeLeft, onSwipeRight) {
            val velocityTracker = VelocityTracker()
            val touchSlop = viewConfiguration.touchSlop * 1.25f

            awaitEachGesture {
                if (!RootNavLock.canNavigate()) {
                    // Transition or cooldown in progress; ignore this touch sequence for page navigation
                    return@awaitEachGesture
                }

                val down = awaitFirstDown(pass = PointerEventPass.Initial)
                velocityTracker.resetTracking()
                velocityTracker.addPosition(down.uptimeMillis, down.position)

                var totalDx = 0f
                var totalDy = 0f
                var isHorizontalIntent = false
                var isVerticalScrolling = false
                var isConsumedByChild = false

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
                        if (absDy > touchSlop && absDy > absDx * 1.5f) {
                            // Clear vertical intent -> let vertical scroll handle everything
                            isVerticalScrolling = true
                        } else if (absDx > touchSlop && absDx > absDy * 1.75f) {
                            // Clear horizontal intent -> commit to horizontal tracking
                            isHorizontalIntent = true
                        }
                    }

                    if (isHorizontalIntent) {
                        // Consume horizontal drag delta
                        dragChange.consume()

                        val rawDx = totalDx - (if (totalDx > 0) touchSlop else -touchSlop)
                        val targetOffset = when {
                            rawDx > 0 && onSwipeRight == null -> {
                                // Boundary: swiping right with no target (Home) -> rubber band
                                (rawDx * 0.18f).coerceAtMost(maxDragDampingPx)
                            }
                            rawDx < 0 && onSwipeLeft == null -> {
                                // Boundary: swiping left with no target (Profile) -> rubber band
                                (rawDx * 0.18f).coerceAtLeast(-maxDragDampingPx)
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

                // Pointer released — evaluate velocity and distance
                val velocityX = velocityTracker.calculateVelocity().x
                val currentOffset = offsetX.value

                val isFastSwipeRight = velocityX > minVelocityThreshold && currentOffset > minFlickDistancePx
                val isFastSwipeLeft = velocityX < -minVelocityThreshold && currentOffset < -minFlickDistancePx
                val isDistanceSwipeRight = currentOffset >= thresholdPx
                val isDistanceSwipeLeft = currentOffset <= -thresholdPx

                val shouldNavigateRight = (isDistanceSwipeRight || isFastSwipeRight) && onSwipeRight != null
                val shouldNavigateLeft = (isDistanceSwipeLeft || isFastSwipeLeft) && onSwipeLeft != null

                if (shouldNavigateRight && RootNavLock.canNavigate()) {
                    val committed = RootNavLock.commitNavigation {
                        onSwipeRight()
                    }
                    coroutineScope.launch {
                        if (committed) {
                            offsetX.snapTo(0f)
                        } else {
                            offsetX.animateTo(
                                0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }
                } else if (shouldNavigateLeft && RootNavLock.canNavigate()) {
                    val committed = RootNavLock.commitNavigation {
                        onSwipeLeft()
                    }
                    coroutineScope.launch {
                        if (committed) {
                            offsetX.snapTo(0f)
                        } else {
                            offsetX.animateTo(
                                0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                    }
                } else {
                    // Settle back to original position smoothly
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
