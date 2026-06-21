package feature.game.joystick.extensions.modifier.gestures

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A modifier that detects and handles continuous movement gestures with support for drag, hold, and threshold detection.
 *
 * This modifier is designed for joystick-like interactions where you need to track pointer movement
 * and respond to various gesture states. It provides callbacks for gesture start, movement, hold (when
 * the pointer stays in place), and end events.
 *
 * The modifier uses a coroutine-based approach to handle gesture detection and includes:
 * - **Threshold detection**: Optional minimum distance before movement is registered
 * - **Hold detection**: Periodic callbacks while the pointer remains stationary
 * - **Continuous movement tracking**: Position updates during dragging
 *
 * Example usage:
 * ```
 * Box(
 *     modifier = Modifier
 *         .movementGestures(
 *             onMoveStart = { offset -> /* Handle gesture start */ },
 *             onMove = { offset -> /* Update movement position */ },
 *             onHold = { offset -> /* Continuous input while held */ },
 *             onMoveEnd = { offset -> /* Reset movement */ }
 *         )
 * ) {}
 * ```
 *
 * @param keys Variable number of keys used to restart the gesture detection when any key changes.
 *             Use this to properly scope the gesture detection to specific state changes.
 * @param threshold Optional minimum distance in pixels that the pointer must move before drag is detected.
 *                  If null, any movement immediately triggers movement. If specified, movement smaller
 *                  than this threshold is ignored until the threshold is crossed.
 * @param interval The time interval in milliseconds between hold event callbacks. Only used when [onHold]
 *                 is provided.
 * @param onMoveStart Optional callback invoked once when the pointer initially touches down.
 *                    Receives the initial pointer position.
 * @param onMoveEnd Optional callback invoked once when the pointer is released or the gesture is cancelled.
 *                  Receives the final pointer position.
 * @param onHold Optional callback invoked repeatedly at [interval] milliseconds while the pointer is held
 *               in place. Receives the current pointer position.
 * @param onMove Callback invoked during pointer movement after the threshold (if any) is crossed.
 *               Receives the current pointer position.
 */
fun Modifier.movementGestures(
    vararg keys: Any? = arrayOf(Unit),
    threshold: Float? = null,
    interval: Long = 175L,
    onMoveStart: ((Offset) -> Unit)? = null,
    onMoveEnd: ((Offset) -> Unit)? = null,
    onHold: ((Offset) -> Unit)? = null,
    onMove: (Offset) -> Unit,
): Modifier {
    return pointerInput(keys = keys) {
        coroutineScope {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                var currentPosition = down.position

                onMoveStart?.invoke(currentPosition)

                var holdJob: Job? = null

                fun startHoldTimer() {
                    if (onHold == null) return
                    holdJob?.cancel()
                    holdJob = launch {
                        while (isActive) {
                            delay(interval)
                            onHold(currentPosition)
                        }
                    }
                }

                startHoldTimer()

                var dragging = false

                try {
                    drag(down.id) { change ->
                        val changeAmount = change.position - change.previousPosition
                        val distSq = changeAmount.getDistanceSquared()

                        if (dragging || threshold == null || distSq >= threshold * threshold) {
                            dragging = true
                            currentPosition = change.position
                            change.consume()
                            onMove(currentPosition)
                            startHoldTimer()
                        }
                    }
                } finally {
                    holdJob?.cancel()
                    onMoveEnd?.invoke(currentPosition)
                }
            }
        }
    }
}