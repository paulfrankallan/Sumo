package feature.game.joystick.extensions.geometry

import feature.game.joystick.core.control.MaxQuadrants
import feature.game.joystick.core.geometry.DEGREE_SPIN
import kotlin.math.PI

/** Converts this angle from degrees to radians. */
fun Double.toRadians(): Double = this / 180.0 * PI

/** Converts this angle from radians to degrees. */
fun Double.toDegrees(): Double = this * 180.0 / PI

/**
 * Converts this angle (degrees) to a quadrant number based on the maximum quadrants.
 *
 * @param maxQuadrants The maximum number of quadrants to divide the circle into ([MaxQuadrants.Four] or [MaxQuadrants.Eight]).
 * @param useMiddle Whether to use the middle of each quadrant for calculation.
 *
 * @throws IllegalArgumentException if the angle is negative.
 */
fun Double.toQuadrant(
    maxQuadrants: MaxQuadrants = MaxQuadrants.Four,
    useMiddle: Boolean = false,
): Int {
    val angle = this
    require(angle >= 0) { "Angle must be positive, got $angle" }
    val quadrants = maxQuadrants.toInt()
    val angleQuadrant = DEGREE_SPIN / quadrants

    var startAngle = 0.0f
    var end = if (useMiddle) angleQuadrant / 2 else angleQuadrant

    val mAngle = if (angle > DEGREE_SPIN) angle % DEGREE_SPIN else angle

    for (quadrant in 0..quadrants) {
        if (mAngle in startAngle..end) {
            return if (useMiddle && quadrant == quadrants) 1
            else quadrant + 1
        }

        startAngle = end
        end += angleQuadrant
    }

    return quadrants
}