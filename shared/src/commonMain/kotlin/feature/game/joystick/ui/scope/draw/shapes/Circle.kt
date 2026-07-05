package feature.game.joystick.ui.scope.draw.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import feature.game.joystick.core.geometry.Radius
import feature.game.joystick.extensions.geometry.parametrize
import feature.game.joystick.ui.scope.ControlDrawScope

/**
 * Defines how shapes are positioned relative to the joystick boundary.
 */
enum class DrawMode {
    /**
     * Shape follows the joystick position without any clamping.
     * The shape can extend beyond the joystick boundary.
     */
    Normal,

    /**
     * Shape is clamped to stay within the joystick boundary.
     * When the joystick reaches the edge, the shape stops at a calculated distance from the center.
     */
    Clamped
}

/**
 * A function that creates a [Brush] based on the center position and radius.
 *
 * The first parameter is the center position where the brush will be applied.
 * The second parameter is the radius (or side) of the shape being drawn.
 *
 * Returns a [Brush] configured for the given position and size.
 */
typealias BrushBuilder = (center: Offset, radius: Float) -> Brush


/**
 * Properties for configuring how a circle is drawn on the joystick.
 *
 * @property brush The brush builder function that creates the brush based on position and radius
 * @property radius The radius of the circle, either as a fixed value or a ratio of the joystick radius
 * @property mode The drawing mode that determines how the circle is positioned relative to the joystick boundary
 */
@Immutable
data class CircleProperties(
    val brush: BrushBuilder,
    val mode: DrawMode,
    val radius: Radius,
)

/**
 * Default values for creating circle drawing configurations.
 */
object CircleDrawDefaults {
    /**
     * The default radius for circles.
     */
    val radius: Radius
        get() = Radius.Ratio(0.20f)

    /**
     * The default brush for drawing circles.
     */
    val brush: Brush
        get() = SolidColor(Color.Red)

    /**
     * Creates circle properties with a static brush.
     *
     * @param radius The radius of the circle, either fixed or as a ratio
     * @param brush The brush to use for drawing the circle
     * @param mode The drawing mode for positioning the circle
     */
    fun properties(
        radius: Radius = CircleDrawDefaults.radius,
        brush: Brush = CircleDrawDefaults.brush,
        mode: DrawMode = DrawMode.Clamped,
    ): CircleProperties = properties({ _, _ -> brush }, radius, mode)

    /**
     * Creates circle properties with a dynamic brush builder.
     *
     * @param brush The brush builder function that creates brushes based on position and radius
     * @param radius The radius of the circle, either fixed or as a ratio
     * @param mode The drawing mode for positioning the circle
     */
    fun properties(
        brush: BrushBuilder,
        radius: Radius = CircleDrawDefaults.radius,
        mode: DrawMode = DrawMode.Clamped,
    ): CircleProperties = CircleProperties(brush, mode, radius)

}

/**
 * Draws a circle on the joystick.
 *
 * @param properties The circle drawing properties
 */
fun ControlDrawScope.drawCircle(
    properties: CircleProperties = CircleDrawDefaults.properties(),
) {
    val radiusValue = when (val r = properties.radius) {
        is Radius.Fixed -> r.value
        is Radius.Ratio -> state.radius * r.value
    }

    drawCircle(
        radiusValue, properties.mode, properties.brush
    )
}


/**
 * Draws a circle on the joystick.
 *
 * @param radius The radius of the circle in pixels
 * @param mode The drawing mode that determines how the circle is positioned
 * @param brush The brush builder function for creating the brush based on position and radius
 */
fun ControlDrawScope.drawCircle(
    radius: Float,
    mode: DrawMode,
    brush: BrushBuilder,
) {
    drawCircle(
        state.radius, radius, mode, brush
    )
}

/**
 * Draws a circle on the joystick.
 *
 * @param radius The radius of the circle in pixels
 * @param mode The drawing mode that determines how the circle is positioned
 * @param brush The static brush to use for drawing the circle
 */
fun ControlDrawScope.drawCircle(
    radius: Float,
    mode: DrawMode,
    brush: Brush,
) {
    drawCircle(radius, mode) { _, _ -> brush }
}

/**
 * Internal function that performs the actual circle drawing with explicit max radius and circle radius.
 * Handles positioning logic based on the draw mode and clamps the circle position if needed.
 *
 * @param maxRadius The maximum radius of the joystick in pixels
 * @param radius The radius of the circle to draw in pixels
 * @param mode The drawing mode that determines positioning behavior
 * @param brush The brush builder function for creating the brush based on position and radius
 */
internal fun ControlDrawScope.drawCircle(
    maxRadius: Float,
    radius: Float,
    mode: DrawMode,
    brush: BrushBuilder,
) {
    val position = when (mode) {
        DrawMode.Normal -> state.position
        DrawMode.Clamped -> {
            val radius = maxRadius - radius
            val squaredDistance = state.offset.getDistanceSquared()
            if (squaredDistance > radius * radius) {
                state.offset.parametrize(radius) + state.center
            } else state.position
        }
    }


    drawScope.drawCircle(
        radius = radius,
        brush = brush(position, radius),
        center = position,
    )
}