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
 * Modifier that detects intentional horizontal swipe gestures to navigate between pages.
 *
 * Characteristics:
 * - Direction-slop filtered: If initial movement is vertical, immediately yields to allow
 *   uninhibited vertical scrolling of lists (LazyColumn, ScrollableColumn).
 * - Triggers only when horizontal intent is established (dx > dy * 1.25) and crosses
 *   either a distance threshold (default 60dp) or a fast fling velocity threshold.
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
            val startTime = System.currentTimeMillis()

            do {
                val event = awaitPointerEvent(pass = PointerEventPass.Initial)
                val dragChange = event.changes.firstOrNull { it.id == down.id } ?: break

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

                if (isVerticalScrolling) {
                    break
                }
            } while (event.changes.any { it.pressed })

            val endTime = System.currentTimeMillis()
            val duration = (endTime - startTime).coerceAtLeast(1L)
            val velocityX = (totalDx / duration) * 1000f // px/s

            if (isHorizontalDirectionDecided && !isVerticalScrolling) {
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
