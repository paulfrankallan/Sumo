package feature.game.joystick.ui.state

import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.Offset
import feature.game.joystick.core.control.Direction
import feature.game.joystick.core.geometry.SQRT_2
import feature.game.joystick.ui.tokens.JoystickDefaults
import kotlinx.coroutines.delay

/**
 * A listener for joystick move events, receiving a snapshot of the joystick state.
 */
typealias JoystickMoveListener = (JoystickSnapshot) -> Unit


/**
 * Moves the joystick to the position corresponding to the given direction.
 * @param direction The direction to move the joystick to
 */
fun JoystickState.goto(direction: Direction) {
    goto(
        getPosition(direction)
    )
}

/**
 * Calculates the position offset for the given direction and strength.
 * @param direction The direction to calculate the position for
 * @param strength The strength/magnitude of the movement
 */
fun JoystickState.getPosition(
    direction: Direction,
    @FloatRange(
        from = 0.0, to = 1.0
    ) strength: Float = 1f,
): Offset {
    require(strength in 0f..1f) { "Magnitude must be between 0 and 1, got $strength" }

    val centerPos = center
    val radiusValue = radius

    val force = strength.coerceIn(0f, 1f)
    if (force == 0f || direction == Direction.None) return centerPos.copy()

    val scaledRadius = radiusValue * force

    return when (direction) {
        Direction.None -> centerPos.copy()
        Direction.Up -> Offset(centerPos.x, centerPos.y - scaledRadius)
        Direction.Down -> Offset(centerPos.x, centerPos.y + scaledRadius)
        Direction.Left -> Offset(centerPos.x - scaledRadius, centerPos.y)
        Direction.Right -> Offset(centerPos.x + scaledRadius, centerPos.y)
        else -> {
            // Diagonal directions: scale by 1/sqrt(2) to maintain distance
            val diagonalRadius = (scaledRadius / SQRT_2).toFloat()
            when (direction) {
                Direction.UpLeft -> Offset(
                    centerPos.x - diagonalRadius, centerPos.y - diagonalRadius
                )

                Direction.UpRight -> Offset(
                    centerPos.x + diagonalRadius, centerPos.y - diagonalRadius
                )

                Direction.DownLeft -> Offset(
                    centerPos.x - diagonalRadius, centerPos.y + diagonalRadius
                )

                Direction.DownRight -> Offset(
                    centerPos.x + diagonalRadius, centerPos.y + diagonalRadius
                )

                else -> centerPos.copy()
            }
        }
    }
}

/**
 * Converts the JoystickState to a JoystickSnapshot.
 */
fun JoystickState.toSnapshot(): JoystickSnapshot {
    return JoystickSnapshot(direction, position, strength, angle)
}


/**
 * Moves the joystick to the specified position, triggering the move callbacks.
 *
 * @param position The target position to move to
 * @param onMoveStart Callback invoked when the move starts
 * @param onMoveEnd Callback invoked when the move ends
 * @param interval Delay in milliseconds between move start and end
 */
suspend fun JoystickState.gotoWithReset(
    position: Offset,
    onMoveStart: JoystickMoveListener,
    onMoveEnd: JoystickMoveListener,
    interval: Long = JoystickDefaults.interval,
) {
    goto(position)
    onMoveStart(toSnapshot())
    delay(interval)
    reset()
    onMoveEnd(toSnapshot())
}

/**
 * Moves the joystick to the specified position, triggering the move callbacks.
 *
 * @param position The target position to move to
 * @param onMove Callback invoked for both move start and end
 * @param interval Delay in milliseconds between move start and end
 */
suspend fun JoystickState.gotoWithReset(
    position: Offset,
    onMove: JoystickMoveListener,
    interval: Long = JoystickDefaults.interval,
) {
    gotoWithReset(position, onMove, onMove, interval)
}

/**
 * Moves the joystick to the specified position, triggering the move callbacks.
 *
 * @param direction The direction to move to
 * @param onMoveStart Callback invoked when the move starts
 * @param onMoveEnd Callback invoked when the move ends
 * @param interval Delay in milliseconds between move start and end
 */
suspend fun JoystickState.gotoWithReset(
    direction: Direction,
    onMoveStart: JoystickMoveListener,
    onMoveEnd: JoystickMoveListener,
    interval: Long = JoystickDefaults.interval,
) = gotoWithReset(
    getPosition(direction),
    onMoveStart,
    onMoveEnd,
    interval,
)

/**
 * Moves the joystick to the position corresponding to the given direction, triggering the move callbacks.
 *
 * @param direction The direction to move to
 * @param onMove Callback invoked for both move start and end
 * @param interval Delay in milliseconds between move start and end
 */
suspend fun JoystickState.gotoWithReset(
    direction: Direction,
    onMove: JoystickMoveListener,
    interval: Long = JoystickDefaults.interval,
) = gotoWithReset(
    direction,
    onMove,
    onMove,
    interval,
)

/**
 * Moves the joystick to the specified position emitting the move events.
 *
 * @param position The target position to move to
 * @param interval Delay in milliseconds between move start and end
 */
suspend fun JoystickEventsHolder.gotoWithReset(
    position: Offset,
    interval: Long = JoystickDefaults.interval,
) {
    state.goto(position)
    emitStart()
    delay(interval)
    state.reset()
    emitEnd()
}

/**
 * Moves the joystick to the position corresponding to the given direction emitting the move events.
 *
 * @param direction The direction to move to
 * @param interval Delay in milliseconds between move start and end
 */
suspend fun JoystickEventsHolder.gotoWithReset(
    direction: Direction,
    interval: Long = JoystickDefaults.interval,
) = gotoWithReset(state.getPosition(direction), interval)
