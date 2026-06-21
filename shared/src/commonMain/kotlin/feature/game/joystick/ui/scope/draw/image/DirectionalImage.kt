package feature.game.joystick.ui.scope.draw.image

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toSize
import feature.game.joystick.core.control.Direction
import feature.game.joystick.ui.scope.ControlDrawScope

/**
 * A function that provides a list of Painters based on the current direction.
 *
 * Takes a Direction parameter and returns a list of Painters to draw for that direction.
 *
 * Returns null if no painters should be drawn for the given direction.
 */
typealias PainterBuilder = (Direction) -> List<Painter>?

/**
 * A function that provides a list of ImageBitmaps based on the current direction.
 *
 * Takes a Direction parameter and returns a list of ImageBitmaps to draw for that direction.
 *
 * Returns null if no images should be drawn for the given direction.
 */
typealias ImageBitmapBuilder = (Direction) -> List<ImageBitmap>?

/**
 * Defines how directional images are sized when drawn.
 */
enum class SizeMode {
    /**
     * The image automatically sizes to match the joystick size.
     * The entire joystick area is filled with the image.
     */
    AutoSize,

    /**
     * The image uses its intrinsic size (absolute dimensions).
     * The image maintains its original width and height from the painter or bitmap.
     */
    Absolute
}

/**
 * Properties for configuring how directional images are drawn on the joystick.
 *
 * Directional images change based on the joystick direction, allowing different
 * visuals for each direction (e.g., different arrows for up, down, left, right).
 *
 * @property mode The sizing mode that determines how the image size is calculated
 * @property painter The painter builder function that provides painters based on direction
 * @property colorFilter Optional color filter to apply tinting or color transformations to the images
 */
@Stable
@Immutable
data class DirectionalImageProperties(
    val mode: SizeMode,
    val painter: PainterBuilder,
    val colorFilter: ColorFilter? = null,
)

/**
 * Default values for creating directional image drawing configurations.
 */
object DirectionalImageDrawDefaults {
    /**
     * The default sizing mode for directional images.
     */
    val mode: SizeMode
        get() = SizeMode.AutoSize

    /**
     * Creates directional image properties with the specified painter builder.
     *
     * @param mode The sizing mode that determines how images are sized
     * @param colorFilter Optional color filter to apply to the images
     * @param painter The painter builder function that provides painters based on direction
     */
    fun properties(
        mode: SizeMode = DirectionalImageDrawDefaults.mode,
        colorFilter: ColorFilter? = null,
        painter: PainterBuilder,
    ) = DirectionalImageProperties(mode, painter, colorFilter)
}

/**
 * Draws directional images on the joystick.
 *
 * The images displayed change based on the current joystick direction, allowing
 * different visual representations for each direction.
 *
 * @param properties The directional image drawing properties
 */
fun ControlDrawScope.drawDirectionalImage(
    properties: DirectionalImageProperties,
) {
    drawDirectionalImage(
        properties.mode,
        properties.colorFilter,
        properties.painter
    )
}

/**
 * Draws directional images on the joystick.
 *
 * The images displayed change based on the current joystick direction, allowing
 * different visual representations for each direction.
 *
 * @param painter The painter builder function that provides painters based on direction
 */
fun ControlDrawScope.drawDirectionalImage(
    painter: PainterBuilder,
) {
    drawDirectionalImage(DirectionalImageDrawDefaults.properties(painter = painter))
}

/**
 * Draws directional images on the joystick.
 *
 * The images displayed change based on the current joystick direction, allowing
 * different visual representations for each direction.
 *
 * @param mode The sizing mode that determines how images are sized
 * @param colorFilter Optional color filter to apply to all painters
 * @param painter The painter builder function that provides a list of painters based on direction
 */
fun ControlDrawScope.drawDirectionalImage(
    mode: SizeMode,
    colorFilter: ColorFilter? = null,
    painter: PainterBuilder,
) {
    val direction = state.direction
    val painters = painter(direction) ?: return

    if (painters.isEmpty())
        return

    painters.forEach { painter ->
        val oval = getDirectionLocation(painter.intrinsicSize, mode)
        drawImage(
            oval.topLeft,
            painter,
            colorFilter,
            oval.size
        )
    }
}

/**
 * Draws directional images on the joystick.
 *
 * The images displayed change based on the current joystick direction, allowing
 * different visual representations for each direction.
 *
 * @param mode The sizing mode that determines how images are sized
 * @param colorFilter Optional color filter to apply to all painters
 * @param style The drawing style for rendering the images
 * @param blendMode The blending mode to use when drawing the images
 * @param image The image bitmap builder function that provides a list of bitmaps based on direction
 */
fun ControlDrawScope.drawDirectionalImage(
    mode: SizeMode,
    colorFilter: ColorFilter? = null,
    style: DrawStyle = Fill,
    blendMode: BlendMode = DrawScope.DefaultBlendMode,
    image: ImageBitmapBuilder,
) {
    val direction = state.direction
    val images = image(direction) ?: return

    if (images.isEmpty())
        return

    images.forEach { image ->
        val size = IntSize(image.width, image.height)
            .toSize()
        val result = getDirectionLocation(size, mode)

        drawImage(
            result.topLeft,
            image,
            result.size,
            style,
            colorFilter,
            blendMode
        )
    }

}


/**
 * Internal function that calculates the position and size for drawing a directional image.
 *
 * Determines the appropriate size based on the SizeMode and centers the image on the joystick.
 * Returns a Rect representing the drawing bounds (position and size).
 *
 * @param size The intrinsic size of the image to be positioned
 * @param mode The sizing mode that determines the target size
 *
 * @return A Rect containing the top-left position and size for drawing the image
 */
internal fun ControlDrawScope.getDirectionLocation(
    size: Size,
    mode: SizeMode,
): Rect {
    var size = size
    if (mode == SizeMode.AutoSize)
        size = state.size

    return Rect(
        state.center - Offset(
            size.width / 2f,
            size.height / 2f,
        ),
        size
    )
}