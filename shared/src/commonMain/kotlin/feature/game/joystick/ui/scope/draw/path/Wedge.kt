package feature.game.joystick.ui.scope.draw.path

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import feature.game.joystick.core.control.Direction
import feature.game.joystick.core.control.DirectionType
import feature.game.joystick.core.control.toQuadrant
import feature.game.joystick.core.geometry.Radius
import feature.game.joystick.extensions.geometry.parametrize
import feature.game.joystick.extensions.geometry.toRadians
import feature.game.joystick.ui.scope.ControlDrawScope
import feature.game.joystick.ui.scope.draw.shapes.BrushBuilder

/**
 * Defines the drawing style for wedge shapes on the joystick.
 */
enum class WedgeMode {
    /**
     * Wedge is drawn with curved inner and outer edges using arc paths.
     * Creates a smooth, pie-slice-like shape that follows the joystick's circular boundary.
     */
    Curve,

    /**
     * Wedge is drawn with straight lines connecting the inner and outer points.
     * Creates a triangular or trapezoidal shape that follows the joystick's circular boundary.
     */
    Straight
}

/**
 * Properties for configuring how a wedge is drawn on the joystick.
 *
 * Wedges are directional indicators that highlight the current movement direction.
 *
 * @property mode The drawing style for the wedge
 * @property radius The inner radius of the wedge, defining the size of the hollow center
 * @property brush The brush builder function that creates the brush based on position and radius
 */
@Immutable
data class WedgeProperties(
    val mode: WedgeMode,
    val radius: Radius,
    val brush: BrushBuilder,
)

/**
 * Default values for creating wedge drawing configurations.
 */
object WedgeDrawDefaults {
    /**
     * The default drawing mode for wedges.
     */
    val mode: WedgeMode
        get() = WedgeMode.Curve

    /**
     * The default inner radius for wedges.
     */
    val radius: Radius
        get() = Radius.Ratio(0.2f)

    /**
     * The default brush for drawing wedges.
     */
    val brush: Brush
        get() = SolidColor(Color.White.copy(alpha = 0.25f))

    /**
     * Creates wedge properties with a static brush.
     *
     * @param mode The drawing style for the wedge
     * @param radius The inner radius of the wedge, either fixed or as a ratio
     * @param brush The brush to use for drawing the wedge
     */
    fun properties(
        mode: WedgeMode = WedgeDrawDefaults.mode,
        radius: Radius = WedgeDrawDefaults.radius,
        brush: Brush = WedgeDrawDefaults.brush,
    ) = properties({ _, _ -> brush }, mode, radius)

    /**
     * Creates wedge properties with a dynamic brush builder.
     *
     * @param brush The brush builder function that creates brushes based on position and radius
     * @param mode The drawing style for the wedge
     * @param radius The inner radius of the wedge, either fixed or as a ratio
     */
    fun properties(
        brush: BrushBuilder,
        mode: WedgeMode = WedgeDrawDefaults.mode,
        radius: Radius = WedgeDrawDefaults.radius,
    ) = WedgeProperties(mode, radius, brush)
}


/**
 * Draws a wedge on the joystick.
 *
 * The wedge highlights the current direction of joystick movement as a colored segment.
 *
 * @param properties The wedge drawing properties
 */
fun ControlDrawScope.drawWedge(
    properties: WedgeProperties = WedgeDrawDefaults.properties(),
) {
    val innerRadius = when (val r = properties.radius) {
        is Radius.Fixed -> r.value
        is Radius.Ratio -> state.radius * r.value
    }

    drawWedge(
        properties.mode,
        innerRadius,
        properties.brush
    )
}


/**
 * Draws a wedge on the joystick.
 *
 * The wedge highlights the current direction of joystick movement as a colored segment.
 *
 * @param mode The drawing style for the wedge
 * @param radius The inner radius of the wedge in pixels
 * @param brush The static brush to use for drawing the wedge
 */
fun ControlDrawScope.drawWedge(mode: WedgeMode, radius: Float, brush: Brush) {
    drawWedge(
        mode,
        radius
    ) { _, _ -> brush }
}

/**
 * Draws a wedge on the joystick.
 *
 * The wedge highlights the current direction of joystick movement as a colored segment.
 *
 * @param mode The drawing style for the wedge
 * @param radius The inner radius of the wedge in pixels
 * @param brush The brush builder function for creating the brush based on position and radius
 */
fun ControlDrawScope.drawWedge(mode: WedgeMode, radius: Float, brush: BrushBuilder) {
    drawWedge(
        state.direction,
        state.directionType,
        mode,
        radius,
        brush
    )
}

/**
 * Draws a wedge on the joystick.
 *
 * The wedge highlights the current direction of joystick movement as a colored segment.
 *
 * @param direction The direction in which to draw the wedge
 * @param directionType The type of direction system
 * @param mode The drawing style for the wedge
 * @param radius The inner radius of the wedge in pixels
 * @param brush The static brush to use for drawing the wedge
 */
fun ControlDrawScope.drawWedge(
    direction: Direction,
    directionType: DirectionType,
    mode: WedgeMode,
    radius: Float,
    brush: Brush,
) {
    drawWedge(
        direction,
        directionType,
        mode,
        radius
    ) { _, _ -> brush }
}

/**
 * Draws a wedge on the joystick.
 *
 * The wedge highlights the current direction of joystick movement as a colored segment.
 *
 * @param direction The direction in which to draw the wedge
 * @param directionType The type of direction system
 * @param mode The drawing style for the wedge
 * @param radius The inner radius of the wedge in pixels
 * @param brush The brush builder function for creating the brush based on position and radius
 */
fun ControlDrawScope.drawWedge(
    direction: Direction,
    directionType: DirectionType,
    mode: WedgeMode,
    radius: Float,
    brush: BrushBuilder,
) {
    drawWedge(
        direction.toQuadrant(directionType),
        mode,
        state.radius,
        radius,
        if (directionType == DirectionType.Simple)
            90f
        else 45f,
        brush
    )
}


/**
 * Internal function that performs the actual wedge drawing using a quadrant-based approach.
 * Constructs a Path representing the wedge shape and fills it with the specified brush.
 *
 * The wedge is drawn as a segment between two radios (inner and outer) spanning a sweep angle.
 * For curved mode, uses arc paths for smooth edges. For straight mode, uses lines.
 *
 * @param quadrant The quadrant number indicating the wedge direction
 * @param mode The drawing style
 * @param outerRadius The outer radius of the wedge (typically the joystick radius)
 * @param innerRadius The inner radius of the wedge
 * @param sweepAngle The angle in degrees that the wedge spans
 * @param brush The brush builder function for creating the brush based on position and radius
 */
internal fun ControlDrawScope.drawWedge(
    quadrant: Int,
    mode: WedgeMode,
    outerRadius: Float,
    innerRadius: Float,
    sweepAngle: Float,
    brush: BrushBuilder,
) {
    if (quadrant == 0)
        return

    val isCurve = mode == WedgeMode.Curve
    val hasInner = innerRadius > 0f
    val path = Path()

    val center = state.center
    val radius = innerRadius.coerceAtLeast(0f)

    val outerOval = Rect(center, outerRadius)

    val startAngle = (quadrant - 1) * sweepAngle - sweepAngle / 2.0
    var posAngle = startAngle.toRadians()
        .toFloat()

    val innerPosition = if (hasInner)
        center.parametrize(innerRadius, posAngle) + center
    else center

    if (isCurve) {
        innerPosition.apply {
            path.moveTo(x, y)
        }
    }


    val outerPosition = (center.parametrize(outerRadius, posAngle) + center).apply {
        if (isCurve)
            path.lineTo(x, y)
        else path.moveTo(x, y)
    }


    path.arcTo(outerOval, startAngle.toFloat(), sweepAngle, false)

    if (isCurve) {
        val innerOval = Rect(center, radius)
        val innerAngle = (startAngle + sweepAngle).toRadians()
            .toFloat()

        (center.parametrize(outerRadius, innerAngle) + center).apply {
            path.lineTo(x, y)
        }
        path.arcTo(innerOval, startAngle.toFloat() + sweepAngle, -sweepAngle, false)
    } else {
        posAngle = (startAngle + sweepAngle).toRadians()
            .toFloat()
        val position = if (hasInner)
            center.parametrize(radius, posAngle) + center
        else center

        position.apply {
            path.lineTo(x, y)
        }

        innerPosition.apply {
            path.lineTo(x, y)
        }
    }
    drawScope.drawPath(
        path = path,
        brush = brush(outerPosition, outerRadius),
    )
}
