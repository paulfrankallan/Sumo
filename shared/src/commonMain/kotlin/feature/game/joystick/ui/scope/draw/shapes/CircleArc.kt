package feature.game.joystick.ui.scope.draw.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Brush
import feature.game.joystick.core.geometry.Radius
import feature.game.joystick.ui.scope.ControlDrawScope

/**
 * Properties for configuring how a combined circle and arc are drawn on the joystick.
 *
 * This allows drawing a circle knob with an arc indicator showing direction and angle.
 *
 * @property circle The properties for drawing the circle component
 * @property arc The properties for drawing the arc component
 */
@Immutable
@Stable
data class CircleArcProperties(
    val circle: CircleProperties,
    val arc: ArcProperties,
)

/**
 * Default values for creating combined circle and arc drawing configurations.
 */
object CircleArcDrawDefaults {

    /**
     * Creates combined circle and arc properties.
     *
     * @param circle The circle properties, defaults to [CircleDrawDefaults.properties]
     * @param arc The arc properties, defaults to [ArcDrawDefaults.properties]
     */
    fun properties(
        circle: CircleProperties = CircleDrawDefaults.properties(),
        arc: ArcProperties = ArcDrawDefaults.properties(),
    ): CircleArcProperties =
        CircleArcProperties(circle, arc)
}

/**
 * Draws a combined circle and arc on the joystick.
 *
 * The arc will use default properties.
 *
 * @param properties The circle properties to use.
 */
fun ControlDrawScope.drawCircleArc(
    properties: CircleProperties,
) = drawCircleArc(
    CircleArcDrawDefaults.properties(
        circle = properties
    )
)

/**
 * Draws a combined circle and arc on the joystick.
 *
 * The circle will use default properties.
 *
 * @param properties The arc properties to use.
 */
fun ControlDrawScope.drawCircleArc(
    properties: ArcProperties,
) = drawCircleArc(
    CircleArcDrawDefaults.properties(
        arc = properties
    )
)

/**
 * Draws a combined circle and arc on the joystick.
 *
 * The drawing is a circle with an arc indicator.
 *
 * The arc extends from the circle position, showing direction and movement.
 *
 * @param properties The combined circle and arc drawing properties
 */
fun ControlDrawScope.drawCircleArc(
    properties: CircleArcProperties = CircleArcDrawDefaults.properties(),
) {

    val (circleProperties, arcProperties) = properties

    val circleRadius = when (val r = circleProperties.radius) {
        is Radius.Fixed -> r.value
        is Radius.Ratio -> state.radius * r.value
    }

    drawCircleArc(
        circleProperties.mode,
        circleRadius,
        arcProperties.strokeWidth,
        arcProperties.brush
    )

    drawCircleArc(
        arcProperties.mode,
        circleRadius,
        arcProperties.sweepAngle,
        arcProperties.strokeWidth,
        arcProperties.brush
    )
}

/**
 * Draws the circle component of the combined circle-arc visualization.
 *
 * @param mode The drawing mode that determines how the circle is positioned
 * @param radius The radius of the circle in pixels
 * @param arcStrokeWidth The width of the arc stroke in pixels
 * @param brush The static brush to use for drawing the circle
 */
fun ControlDrawScope.drawCircleArc(
    mode: DrawMode,
    radius: Float,
    arcStrokeWidth: Float,
    brush: Brush,
) {
    drawCircleArc(mode, radius, arcStrokeWidth) { _, _ -> brush }
}

/**
 * Draws the circle component of the combined circle-arc visualization.
 *
 * @param mode The drawing mode that determines how the circle is positioned
 * @param radius The radius of the circle in pixels
 * @param arcStrokeWidth The width of the arc stroke in pixels
 * @param brush The brush builder function for creating the brush based on position and radius
 */
fun ControlDrawScope.drawCircleArc(
    mode: DrawMode,
    radius: Float,
    arcStrokeWidth: Float,
    brush: BrushBuilder,
) {
    when (mode) {
        DrawMode.Normal -> drawCircle(
            radius,
            DrawMode.Clamped,
            brush
        )

        DrawMode.Clamped -> drawCircle(
            state.radius - arcStrokeWidth,
            radius,
            DrawMode.Clamped,
            brush,
        )
    }
}

/**
 * Draws the arc component of the combined circle-arc visualization.
 *
 * @param mode The drawing mode that determines how the arc is sized
 * @param circleRadius The radius of the circle component in pixels
 * @param sweepAngle The angle in degrees that the arc spans
 * @param strokeWidth The width of the arc stroke in pixels
 * @param brush The static brush to use for drawing the arc
 */
fun ControlDrawScope.drawCircleArc(
    mode: DrawMode,
    circleRadius: Float,
    sweepAngle: Float,
    strokeWidth: Float,
    brush: Brush,
) {
    drawCircleArc(mode, circleRadius, sweepAngle, strokeWidth) { _, _ -> brush }
}


/**
 * Draws the arc component of the combined circle-arc visualization.
 *
 * @param mode The drawing mode that determines how the arc is sized
 * @param circleRadius The radius of the circle component in pixels
 * @param sweepAngle The angle in degrees that the arc spans
 * @param strokeWidth The width of the arc stroke in pixels
 * @param brush The static brush to use for drawing the arc
 */
fun ControlDrawScope.drawCircleArc(
    mode: DrawMode,
    circleRadius: Float,
    sweepAngle: Float,
    strokeWidth: Float,
    brush: BrushBuilder,
) {
    if (state.isCentered) return

    val radius = (state.distance + circleRadius).coerceAtMost(state.radius)
    when (mode) {
        DrawMode.Normal -> drawArc(
            radius,
            sweepAngle,
            strokeWidth,
            brush,
        )

        DrawMode.Clamped -> {
            drawArc(
                radius,
                sweepAngle,
                strokeWidth,
                brush
            )
        }
    }
}