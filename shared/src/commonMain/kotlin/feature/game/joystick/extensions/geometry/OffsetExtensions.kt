package feature.game.joystick.extensions.geometry

import androidx.annotation.FloatRange
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import feature.game.joystick.core.geometry.RADIAN_SPIN
import kotlin.jvm.JvmOverloads
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/** Calculates the angle in radians from the positive x-axis to this offset. */
@FloatRange(
    from = 0.0,
    to = RADIAN_SPIN
)
@Stable
fun Offset.getAngle(): Float {
    val angle = atan2(y, x)
    return if (angle < 0)
        (angle + RADIAN_SPIN).toFloat()
    else angle
}


/**
 * Parametrize this position to an offset with the given side length and angle.
 *
 * @param side The length of the offset vector.
 * @param angle The angle in radians from the positive x-axis.
 */
@Stable
@JvmOverloads
fun Offset.parametrize(
    side: Float,
    @FloatRange(
        from = 0.0,
        to = RADIAN_SPIN
    )
    angle: Float = this.getAngle(),
): Offset {
    return Offset(
        x = side * cos(angle),
        y = side * sin(angle)
    )
}
