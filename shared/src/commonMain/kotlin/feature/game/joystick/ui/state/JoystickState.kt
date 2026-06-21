package feature.game.joystick.ui.state

import androidx.annotation.FloatRange
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import feature.game.joystick.core.geometry.Radius
import feature.game.joystick.core.control.Direction
import feature.game.joystick.core.control.DirectionType
import feature.game.joystick.core.control.MaxQuadrants
import feature.game.joystick.core.geometry.RADIAN_SPIN
import feature.game.joystick.extensions.geometry.getAngle
import feature.game.joystick.extensions.geometry.parametrize
import feature.game.joystick.extensions.geometry.toDegrees
import feature.game.joystick.extensions.geometry.toQuadrant

@Stable
/**
 * Represents the state of a virtual joystick, managing position, size, and derived properties.
 */
class JoystickState(
    /**
     * The radius within which the joystick is considered invalid (`dead zone`).
     */
    invalidRadius: Radius,
    /**
     * The type of directions supported by the joystick.
     */
    val directionType: DirectionType,
) {
    private val _invalidRadius = invalidRadius

    /**
     * The current position of the joystick.
     */
    var position by mutableStateOf(Offset.Zero)
        private set

    /**
     * The size of the joystick area.
     */
    var size by mutableStateOf(Size.Zero)
        internal set

    companion object {
        /**
         * Creates a Saver for the JoystickState to enable state restoration.
         */
        fun Saver(
            invalidRadius: Radius,
            directionType: DirectionType,
        ): Saver<JoystickState, *> = listSaver(
            save = { listOf(it.position.x, it.position.y) },
            restore = {
                JoystickState(
                    invalidRadius = invalidRadius,
                    directionType = directionType,
                ).apply {
                    goto(Offset(it[0], it[1]))
                }
            }
        )
    }

    /**
     * Moves the joystick to the specified position, clamping it to the radius if necessary.
     */
    fun goto(position: Offset) {
        if (size == Size.Zero || size == Size.Unspecified)
            return

        val centerPos = center
        val radiusValue = radius
        val offset = position - centerPos

        val distanceSquared = offset.getDistanceSquared()

        this.position = when {
            distanceSquared > radiusValue * radiusValue -> offset.parametrize(radiusValue) + centerPos
            else -> position
        }
    }

    /**
     * Resets the state values.
     */
    fun reset() {
        goto(center)
    }

    /**
     * Whether the current position is valid (outside the invalid radius).
     */
    val isValid: Boolean by derivedStateOf {
        val distSq = (position - center).getDistanceSquared()
        val r = this.invalidRadius
        distSq > r * r
    }

    /**
     * Computes the invalid radius value.
     */
    val invalidRadius: Float
        get() = when (val r = _invalidRadius) {
            is Radius.Fixed -> r.value
            is Radius.Ratio -> radius * r.value
        }

    /**
     * Whether the joystick is at the center position.
     */
    val isCentered: Boolean
        get() = center == position

    /**
     * The center position of the joystick area.
     */
    val center: Offset
        get() = size.center

    /**
     * The radius of the joystick.
     */
    val radius: Float
        get() = size.minDimension / 2f

    /**
     * The offset from the center to the current position.
     */
    val offset: Offset
        get() = position - center

    /**
     * The distance from the center to the current position.
     */
    val distance: Float
        get() = offset.getDistance()

    /**
     * The angle of the current position in radians.
     */
    val angle: Float
        @FloatRange(
            from = 0.0,
            to = RADIAN_SPIN
        )
        get() = offset.getAngle()

    /**
     * The direction based on the current position.
     */
    val direction: Direction by derivedStateOf { getDirection(position) }

    /**
     * The position clamped to the radius boundary.
     */
    val parametricPosition: Offset
        get() = offset.parametrize(radius) + center


    /**
     * Calculates the direction for a given position.
     */
    fun getDirection(position: Offset): Direction {
        val offset = position - center

        val squaredDistance = offset.getDistanceSquared()
        val squaredInvalidRadius = invalidRadius * invalidRadius

        if (squaredDistance <= squaredInvalidRadius) return Direction.None

        val angle = offset.getAngle()

        val quadrantType = if (directionType == DirectionType.Complete) MaxQuadrants.Eight
        else MaxQuadrants.Four

        val quadrant = angle.toDouble()
            .toDegrees()
            .toQuadrant(quadrantType, true)

        return Direction.fromQuadrant(quadrant, quadrantType)
    }

    /**
     * Returns the strength/magnitude of the joystick position
     * > The value 0.0 is at center and 1.0 is at the edge
     */
    val strength: Float
        @FloatRange(
            from = 0.0,
            to = 1.0
        )
        get() = if (!isValid) 0f
        else (distance / radius).coerceIn(0f, 1f)

    /**
     * Whether the joystick is currently being moved
     */
    val isActive: Boolean
        get() = isValid && !isCentered
}
