package feature.game.joystick.extensions.modifier.gestures

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import feature.game.joystick.ui.state.JoystickEventsHolder
import feature.game.joystick.ui.state.JoystickMoveListener
import feature.game.joystick.ui.state.JoystickState
import feature.game.joystick.ui.state.toSnapshot
import feature.game.joystick.ui.tokens.JoystickDefaults

/**
 * A modifier that connects joystick movement gestures to a [JoystickState] and [JoystickEventsHolder].
 *
 * This integrates gesture detection with the joystick state management  and event emission system.
 *
 * It automatically:
 * - Updates the joystick position based on pointer movement
 * - Emits appropriate events through the holder
 * - Resets the joystick to center when the gesture ends
 *
 * The events are emitted using non-blocking `tryEmit` calls to avoid suspending the gesture handling.
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventsHolder(state)
 *
 * Box(
 *     modifier = Modifier
 *         .size(200.dp)
 *         .joystickMovement(
 *             state = state,
 *             holder = holder,
 *         )
 * ) {}
 * ```
 *
 * @param state The joystick state that tracks position, direction, and other properties
 * @param holder The events holder that manages the flow of joystick events to subscribers
 * @param threshold Optional minimum distance in pixels before movement is registered. Useful for
 *                  ignoring accidental touches or small movements. If null, any movement is detected.
 * @param interval The time in milliseconds between held event emissions when the pointer is stationary.
 */
fun Modifier.joystickMovement(
    state: JoystickState,
    holder: JoystickEventsHolder,
    threshold: Float? = null,
    interval: Long = JoystickDefaults.interval,
): Modifier {
    return movementGestures(
        keys = arrayOf(state, holder),
        interval = interval,
        threshold = threshold,
        onMoveStart = {
            state.goto(it)
            holder.tryEmitStart()
        },
        onMoveEnd = {
            state.reset()
            holder.tryEmitEnd()
        },
        onHold = {
            state.goto(it)
            holder.tryEmitHeld()
        }
    ) {
        state.goto(it)
        holder.tryEmitMove()
    }
}

/**
 * A modifier that connects joystick movement gestures to a [JoystickState] with callback listeners.
 *
 * This function provides a more flexible alternative to the events-based approach, allowing you to
 * directly handle joystick state changes through callback functions. It's ideal when you need:
 * - Direct state observation without setting up event flows
 * - Custom logic that depends on immediate joystick snapshots
 * - Integration with imperative APIs or non-reactive systems
 *
 * The modifier automatically:
 * - Updates the joystick position based on pointer movement
 * - Creates snapshots of the joystick state at each event point
 * - Invokes the appropriate callbacks with the current state snapshot
 * - Resets the joystick to center when the gesture ends
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 *
 * Box(
 *     modifier = Modifier
 *         .size(200.dp)
 *         .joystickMovement(
 *             state = state,
 *             onMoveStart = { snapshot ->
 *                 println("Started: ${snapshot.direction}")
 *             },
 *             onMove = { snapshot ->
 *                 updateGameCharacter(snapshot.direction, snapshot.strength)
 *             },
 *             onMoveEnd = { snapshot ->
 *                 println("Ended at: ${snapshot.position}")
 *             }
 *         )
 * )
 * ```
 *
 * @param state The joystick state that tracks position, direction, and other properties
 * @param onMoveStart Optional callback invoked once when the gesture begins.
 * @param onMoveEnd Optional callback invoked once when the gesture ends (pointer released).
 * @param threshold Optional minimum distance in pixels before movement is registered. Useful for
 *                  ignoring accidental touches or small movements. If null, any movement is detected.
 * @param interval The time in milliseconds between hold event callbacks when the pointer is stationary.
 * @param onMove Callback invoked during pointer movement and periodically while held.
 */
fun Modifier.joystickMovement(
    state: JoystickState,
    onMoveStart: JoystickMoveListener? = null,
    onMoveEnd: JoystickMoveListener? = null,
    threshold: Float? = null,
    interval: Long = JoystickDefaults.interval,
    onMove: JoystickMoveListener,
): Modifier {

    val joystickMovement: (position: Offset) -> Unit = {
        state.goto(it)
        val snapshot = state.toSnapshot()
        onMove(snapshot)
    }

    return movementGestures(
        keys = arrayOf(state),
        onMoveStart = {
            state.goto(it)
            val snapshot = state.toSnapshot()
            onMoveStart?.invoke(snapshot)
        },
        onMoveEnd = {
            state.reset()
            val snapshot = state.toSnapshot()
            onMoveEnd?.invoke(snapshot)
        },
        threshold = threshold,
        interval = interval,
        onHold = joystickMovement,
        onMove = joystickMovement
    )
}

