package feature.game.joystick.ui.state

import androidx.annotation.FloatRange
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import feature.game.joystick.core.control.Direction
import feature.game.joystick.core.geometry.RADIAN_SPIN

/**
 * Represents a snapshot of the joystick's current state.
 */
@Immutable
@Stable
data class JoystickSnapshot(
    /**
     * The direction of the joystick.
     */
    val direction: Direction = Direction.None,
    /**
     * The position of the joystick.
     */
    val position: Offset = Offset.Unspecified,
    /**
     * The strength of the joystick movement.
     */
    @param:FloatRange(
        from = 0.0,
        to = 1.0
    )
    val strength: Float = 0f,
    /**
     * The angle of the joystick in radians.
     */
    @param:FloatRange(
        from = 0.0,
        to = RADIAN_SPIN
    )
    val angle: Float = 0f,
)