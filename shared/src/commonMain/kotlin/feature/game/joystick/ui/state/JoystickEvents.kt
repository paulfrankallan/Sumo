package feature.game.joystick.ui.state


/**
 * Sealed class representing events from the virtual joystick.
 */
sealed class JoystickEvent() {
    abstract val snapshot: JoystickSnapshot
}

/**
 * Event indicating the start of a joystick movement.
 */
class JoystickMoveStart(
    override val snapshot: JoystickSnapshot,
) : JoystickEvent()

/**
 * Event indicating ongoing joystick movement.
 */
class JoystickMoving(
    override val snapshot: JoystickSnapshot,
) : JoystickEvent()

/**
 * Event indicating the joystick is being held.
 */
class JoystickMoveHeld(
    override val snapshot: JoystickSnapshot,
) : JoystickEvent()

/**
 * Event indicating the end of a joystick movement.
 */
class JoystickMoveEnd(
    override val snapshot: JoystickSnapshot,
) : JoystickEvent()


/**
 * Converts the JoystickState to a JoystickMoveStart event.
 */
fun JoystickState.toStartEvent(): JoystickMoveStart = JoystickMoveStart(toSnapshot())

/**
 * Converts the JoystickState to a JoystickMoving event.
 */
fun JoystickState.toMoveEvent(): JoystickMoving = JoystickMoving(toSnapshot())

/**
 * Converts the JoystickState to a JoystickMoveHeld event.
 */
fun JoystickState.toHeldEvent(): JoystickMoveHeld = JoystickMoveHeld(toSnapshot())

/**
 * Converts the JoystickState to a JoystickMoveEnd event.
 */
fun JoystickState.toEndEvent(): JoystickMoveEnd = JoystickMoveEnd(toSnapshot())