package feature.game.joystick.ui.scope.draw.shapes

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import feature.game.joystick.extensions.geometry.parametrize
import feature.game.joystick.extensions.geometry.toDegrees
import feature.game.joystick.ui.scope.ControlDrawScope

/**
 * Properties for configuring how an arc is drawn on the joystick.
 *
 * @property brush The brush builder function that creates the brush based on position and radius
 * @property sweepAngle The angle in degrees that the arc spans
 * @property strokeWidth The width of the arc stroke in pixels
 * @property mode The drawing mode that determines how the arc is sized relative to the joystick radius
 */
@Immutable
@Stable
data class ArcProperties(
    val brush: BrushBuilder,
    val sweepAngle: Float,
    val strokeWidth: Float,
    val mode: DrawMode,
)

/**
 * Default values for creating arc drawing configurations.
 */
object ArcDrawDefaults {
    /**
     * The default sweep angle for arcs in degrees.
     */
    val sweepAngle
        get() = 90f

    /**
     * The default stroke width for arcs in pixels.
     */
    val strokeWidth
        get() = 12f

    /**
     * The default brush for drawing arcs.
     */
    val brush: Brush
        get() = SolidColor(Color.Red)

    /**
     * Creates arc properties with a static brush.
     *
     * @param brush The brush to use for drawing the arc
     * @param sweepAngle The angle in degrees that the arc spans
     * @param strokeWidth The width of the arc stroke in pixels
     * @param mode The drawing mode for sizing the arc
     */
    fun properties(
        brush: Brush = ArcDrawDefaults.brush,
        sweepAngle: Float = ArcDrawDefaults.sweepAngle,
        strokeWidth: Float = ArcDrawDefaults.strokeWidth,
        mode: DrawMode = DrawMode.Clamped,
    ): ArcProperties = properties({ _, _ -> brush }, sweepAngle, strokeWidth, mode)

    /**
     * Creates arc properties with a dynamic brush builder.
     *
     * @param brush The brush builder function that creates brushes based on position and radius
     * @param sweepAngle The angle in degrees that the arc spans
     * @param strokeWidth The width of the arc stroke in pixels
     * @param mode The drawing mode for sizing the arc
     */
    fun properties(
        brush: BrushBuilder,
        sweepAngle: Float = ArcDrawDefaults.sweepAngle,
        strokeWidth: Float = ArcDrawDefaults.strokeWidth,
        mode: DrawMode = DrawMode.Clamped,
    ): ArcProperties =
        ArcProperties(brush, sweepAngle, strokeWidth, mode)
}


/**
 * Draws an arc on the joystick.
 *
 * The arc indicates the current direction and angle of the joystick with an arrow at the end.
 *
 * @param properties The arc drawing properties
 */
fun ControlDrawScope.drawArc(
    properties: ArcProperties = ArcDrawDefaults.properties(),
) {
    drawArc(
        properties.mode,
        properties.sweepAngle,
        properties.strokeWidth,
        properties.brush
    )
}

/**
 * Draws an arc on the joystick.
 *
 * The arc indicates the current direction and angle of the joystick with an arrow at the end.
 *
 * @param mode The drawing mode that determines how the arc radius is calculated
 * @param sweepAngle The angle in degrees that the arc spans
 * @param strokeWidth The width of the arc stroke in pixels
 * @param brush The brush builder function for creating the brush based on position and radius
 */
fun ControlDrawScope.drawArc(
    mode: DrawMode,
    sweepAngle: Float,
    strokeWidth: Float,
    brush: BrushBuilder,
) {
    if (!state.isValid) return

    val radius = when (mode) {
        DrawMode.Normal -> state.radius
        DrawMode.Clamped -> state.radius - strokeWidth * 2
    }

    drawArc(
        radius,
        sweepAngle,
        strokeWidth,
        brush
    )
}

/**
 * Draws an arc on the joystick.
 *
 * The arc indicates the current direction and angle of the joystick with an arrow at the end.
 *
 * @param drawMode The drawing mode that determines how the arc radius is calculated
 * @param sweepAngle The angle in degrees that the arc spans
 * @param strokeWidth The width of the arc stroke in pixels
 * @param brush The static brush to use for drawing the arc
 */
fun ControlDrawScope.drawArc(
    drawMode: DrawMode,
    sweepAngle: Float,
    strokeWidth: Float,
    brush: Brush,
) {
    drawArc(drawMode, sweepAngle, strokeWidth) { _, _ -> brush }
}

/**
 * Internal function that performs the actual arc drawing with an explicit radius.
 * Draws both the arc and an arrow at the end to indicate direction.
 *
 * @param radius The radius of the arc in pixels
 * @param sweepAngle The angle in degrees that the arc spans
 * @param strokeWidth The width of the arc stroke in pixels
 * @param brush The brush builder function for creating the brush based on position and radius
 */
internal fun ControlDrawScope.drawArc(
    radius: Float,
    sweepAngle: Float,
    strokeWidth: Float,
    brush: BrushBuilder,
) {
    val angle = state.angle.toDouble().toDegrees()
    val oval = Rect(state.center, radius)

    val startAngle = angle - sweepAngle / 2

    drawScope.drawArc(
        brush = brush(state.offset.parametrize(radius) + state.center, radius),
        useCenter = false,
        topLeft = Offset(oval.left, oval.top),
        size = Size(oval.width, oval.height),
        style = Stroke(width = strokeWidth),
        startAngle = startAngle.toFloat(),
        sweepAngle = sweepAngle
    )

    val rotate = angle - 90

    drawArrow(
        radius + strokeWidth, rotate.toFloat(), strokeWidth, brush,
    )
}

/**
 * Internal function that draws an arrow at the end of the arc to indicate direction.
 * The arrow is drawn as a filled triangle that points in the direction of joystick movement.
 *
 * @param radius The distance from the center to where the arrow should be drawn
 * @param rotateAngle The angle in degrees to rotate the arrow to match the joystick direction
 * @param arrowLength The size of the arrow in pixels
 * @param brush The brush builder function for creating the brush based on position and radius
 */
internal fun ControlDrawScope.drawArrow(
    radius: Float,
    rotateAngle: Float,
    arrowLength: Float,
    brush: BrushBuilder,
) {
    val position = state.offset.parametrize(radius) + state.center

    val x = position.x
    val y = position.y

    val path = Path().apply {
        moveTo(x, y)
        lineTo(x + arrowLength, y - arrowLength)
        lineTo(x, y - arrowLength / 2)
        lineTo(x - arrowLength, y - arrowLength)
        close()
    }

    val brush = brush(position, radius)

    drawScope.rotate(rotateAngle, Offset(x, y)) {
        drawPath(path = path, brush = brush)
        drawPath(path = path, brush = brush, style = Stroke(width = arrowLength / 2))
    }
}





