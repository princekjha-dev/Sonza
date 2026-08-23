package com.sonza.app.ui.utils

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.unit.dp
import kotlin.math.abs

/**
 * Modifier attached to horizontally scrollable components (LazyRow, carousels)
 * so that horizontal swipe gestures starting inside them are fully contained and consumed locally,
 * preventing accidental triggering of parent page-level horizontal gestures.
 *
 * Characteristics:
 * - Allows normal tapping of cards without consuming touch-down or cancelling clicks.
 * - Allows uninhibited vertical scrolling of parent lists (e.g. LazyColumn) when dragging vertically.
 * - Consumes horizontal drag delta so parent HorizontalPager yields to carousel scrolling.
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
                    if (absDy > slop && absDy > absDx) {
                        isVertical = true
                    } else if (absDx > slop && absDx > absDy) {
                        isHorizontal = true
                    }
                }

                if (isHorizontal) {
                    // Consume horizontal drag delta so parent page swipe navigation is not triggered
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
