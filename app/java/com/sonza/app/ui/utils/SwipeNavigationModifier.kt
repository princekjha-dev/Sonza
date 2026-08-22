package com.sonza.app.ui.utils

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

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
 * Modifier that detects intentional horizontal swipe gestures to navigate between pages.
 *
 * Characteristics:
 * - Direction-slop filtered: If initial movement is vertical, immediately yields to allow
 *   uninhibited vertical scrolling of lists (LazyColumn, ScrollableColumn).
 * - Child-gesture aware: If any child horizontally scrollable component (e.g. LazyRow carousel)
 *   consumes or handles the horizontal gesture, this modifier immediately yields and will NOT
 *   trigger page navigation.
 * - Triggers only when horizontal intent is established (dx > dy * 1.25) outside horizontally
 *   scrollable children and crosses either a distance threshold (default 60dp) or a fast fling velocity threshold.
 */
fun Modifier.horizontalSwipeNavigation(
    onSwipeLeft: (() -> Unit)? = null,
    onSwipeRight: (() -> Unit)? = null,
    threshold: Dp = 60.dp,
    enabled: Boolean = true
): Modifier {
    if (!enabled || (onSwipeLeft == null && onSwipeRight == null)) return this

    return this.pointerInput(enabled, onSwipeLeft, onSwipeRight) {
        val thresholdPx = threshold.toPx()
        val minVelocityThreshold = 650.dp.toPx()

        awaitEachGesture {
            val down = awaitFirstDown(pass = PointerEventPass.Initial)
            var totalDx = 0f
            var totalDy = 0f
            var isHorizontalDirectionDecided = false
            var isVerticalScrolling = false
            var isConsumedByChild = false
            val startTime = System.currentTimeMillis()

            do {
                // Inspect event in Final pass so we see whether children consumed the drag in Main pass
                val event = awaitPointerEvent(pass = PointerEventPass.Final)
                val dragChange = event.changes.firstOrNull { it.id == down.id } ?: break

                if (dragChange.isConsumed) {
                    isConsumedByChild = true
                }

                val positionChange = dragChange.positionChange()
                totalDx += positionChange.x
                totalDy += positionChange.y

                // Determine gesture direction once movement exceeds initial slop
                if (!isHorizontalDirectionDecided && !isVerticalScrolling) {
                    val absDx = abs(totalDx)
                    val absDy = abs(totalDy)
                    val slop = 12.dp.toPx()

                    if (absDy > slop && absDy > absDx * 1.25f) {
                        // Gesture is vertical -> let vertical scrolling handle everything
                        isVerticalScrolling = true
                    } else if (absDx > slop && absDx > absDy * 1.25f) {
                        // Gesture is horizontal
                        isHorizontalDirectionDecided = true
                    }
                }

                if (isVerticalScrolling || isConsumedByChild) {
                    break
                }
            } while (event.changes.any { it.pressed })

            val endTime = System.currentTimeMillis()
            val duration = (endTime - startTime).coerceAtLeast(1L)
            val velocityX = (totalDx / duration) * 1000f // px/s

            if (isHorizontalDirectionDecided && !isVerticalScrolling && !isConsumedByChild) {
                val isFastSwipeRight = velocityX > minVelocityThreshold && totalDx > 25.dp.toPx()
                val isFastSwipeLeft = velocityX < -minVelocityThreshold && totalDx < -25.dp.toPx()
                val isDistanceSwipeRight = totalDx >= thresholdPx
                val isDistanceSwipeLeft = totalDx <= -thresholdPx

                if ((isDistanceSwipeRight || isFastSwipeRight) && onSwipeRight != null) {
                    onSwipeRight()
                } else if ((isDistanceSwipeLeft || isFastSwipeLeft) && onSwipeLeft != null) {
                    onSwipeLeft()
                }
            }
        }
    }
}
