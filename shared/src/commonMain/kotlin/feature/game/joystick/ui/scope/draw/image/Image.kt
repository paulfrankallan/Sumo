package feature.game.joystick.ui.scope.draw.image

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntSize
import androidx.compose.ui.unit.toSize
import feature.game.joystick.extensions.geometry.clamp
import feature.game.joystick.extensions.geometry.parametrize
import feature.game.joystick.ui.scope.ControlDrawScope
import feature.game.joystick.ui.scope.draw.shapes.DrawMode

@Stable
@Immutable
/**
 * Properties for configuring how an image is drawn on the joystick.
 *
 * Painter can be used to represent the joystick knob or any visual indicator that follows the joystick position.
 *
 * @property mode The drawing mode that determines how the image is positioned relative to the joystick boundary
 * @property size The size of the image result in pixels
 * @property painter The painter used to draw the image content
 * @property colorFilter Optional color filter to apply tinting or color transformations to the painter
 */
data class ImageDrawProperties(
    val mode: DrawMode,
    val size: Size,
    val painter: Painter,
    val colorFilter: ColorFilter? = null,
)

/**
 * Default values for creating image drawing configurations.
 */
object ImageDrawDefaults {
    /**
     * The default drawing mode for images.
     */
    val mode: DrawMode
        get() = DrawMode.Clamped

    /**
     * The default size for images results in pixels.
     */
    val size: Size
        get() = Size(125f, 125f)

    /**
     * Creates image drawing properties with the specified painter.
     *
     * @param painter The painter to use for drawing the image
     * @param mode The drawing mode for positioning the image
     * @param size The size of the image in pixels
     * @param colorFilter Optional color filter to apply to the image
     */
    fun properties(
        painter: Painter,
        mode: DrawMode = ImageDrawDefaults.mode,
        size: Size = ImageDrawDefaults.size,
        colorFilter: ColorFilter? = null,
    ) = ImageDrawProperties(mode, size, painter, colorFilter)
}

/**
 * Draws an image on the joystick.
 *
 * @param painter The painter to use for drawing the image
 */
fun ControlDrawScope.drawImage(
    painter: Painter,
) = drawImage(ImageDrawDefaults.properties(painter))

/**
 * Draws an image on the joystick
 *
 * The image follows the joystick position and can represent the knob or any visual indicator.
 * The painter's intrinsic size is used as the base size, clamped to the specified property size.
 *
 * @param properties The image drawing properties
 */
fun ControlDrawScope.drawImage(
    properties: ImageDrawProperties,
) {
    drawImage(
        properties.painter,
        properties.mode,
        properties.size,
        properties.colorFilter
    )
}

/**
 * Draws an image on the joystick.
 *
 * The image follows the joystick position and can represent the knob or any visual indicator.
 * The painter's intrinsic size is used as the base size, clamped to the specified size or joystick size.
 *
 * @param painter The painter to use for drawing the image
 * @param mode The drawing mode that determines how the image is positioned
 * @param size Optional size constraint for the image in pixels. If null, uses the joystick size
 * @param colorFilter Optional color filter to apply tinting or color transformations
 */
fun ControlDrawScope.drawImage(
    painter: Painter,
    mode: DrawMode,
    size: Size? = null,
    colorFilter: ColorFilter? = null,
) {
    val size = painter.intrinsicSize
        .clamp(size ?: state.size)

    val position = getImagePosition(
        size,
        mode
    )

    drawImage(
        position,
        painter,
        colorFilter,
        size
    )
}

/**
 * Draws an image on the joystick using an ImageBitmap.
 *
 * The image follows the joystick position and can represent the knob or any visual indicator.
 * The image size is used as the base size, clamped to the specified size or joystick size.
 *
 * @param image The ImageBitmap to draw on the joystick
 * @param mode The drawing mode that determines how the image is positioned
 * @param size Optional size constraint for the image in pixels. If null, uses the joystick size
 * @param style The drawing style for rendering the image
 * @param colorFilter Optional color filter to apply tinting or color transformations
 * @param blendMode The blending mode to use when drawing the image
 */
fun ControlDrawScope.drawImage(
    image: ImageBitmap,
    mode: DrawMode,
    size: Size? = null,
    style: DrawStyle = Fill,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DrawScope.DefaultBlendMode,
) {
    val size = IntSize(image.width, image.height)
        .toSize()
        .clamp(size ?: state.size)

    val position = getImagePosition(
        size,
        mode
    )

    drawImage(
        position,
        image,
        size,
        style,
        colorFilter,
        blendMode
    )
}

/**
 * Internal function that performs the actual image drawing with a Painter.
 *
 * Uses translate to position the painter at the correct location and then draws it.
 *
 * @param position The position offset where the image should be drawn (top-left corner)
 * @param painter The painter to use for drawing the image content
 * @param colorFilter Optional color filter to apply to the image
 * @param size The size to draw the image at in pixels
 */
internal fun ControlDrawScope.drawImage(
    position: Offset,
    painter: Painter,
    colorFilter: ColorFilter?,
    size: Size,
) {
    drawScope.translate(position.x, position.y) {
        with(painter) {
            drawScope.draw(size, colorFilter = colorFilter)
        }
    }
}

/**
 * Internal function that performs the actual image drawing with an ImageBitmap.
 * Draws the bitmap directly to the draw scope at the specified position and size.
 *
 * @param position The position offset where the image should be drawn (top-left corner)
 * @param image The ImageBitmap to draw
 * @param size The size to draw the image at in pixels
 * @param style The drawing style (Fill or Stroke) for rendering
 * @param colorFilter Optional color filter to apply to the image
 * @param blendMode The blending mode to use when drawing
 */
internal fun ControlDrawScope.drawImage(
    position: Offset,
    image: ImageBitmap,
    size: Size,
    style: DrawStyle = Fill,
    colorFilter: ColorFilter? = null,
    blendMode: BlendMode = DrawScope.DefaultBlendMode,
) {
    drawScope.drawImage(
        image,
        dstOffset = position.round(),
        dstSize = size
            .roundToIntSize(),
        style = style,
        colorFilter = colorFilter,
        blendMode = blendMode
    )
}

/**
 * Calculates the position where an image should be drawn.
 *
 * Handles positioning logic based on the draw mode, clamping the image to stay within boundaries.
 *
 * The function ensures that images don't extend beyond the joystick boundary
 * by adjusting the effective radius based on the image's half-dimensions.
 *
 * @param size The size of the image to be positioned
 * @param mode The drawing mode that determines positioning behavior
 *
 * @return The offset position (top-left corner) where the image should be drawn
 */
fun ControlDrawScope.getImagePosition(
    size: Size,
    mode: DrawMode,
): Offset {
    val halfHeight = size.height / 2f
    val halfWidth = size.width / 2f

    val radius = when (mode) {
        DrawMode.Normal -> state.radius
        DrawMode.Clamped -> state.radius - maxOf(halfWidth, halfHeight)
    }

    val center = state.center

    val position = if (radius <= 0f) {
        center
    } else if (state.offset.getDistanceSquared() > radius * radius) {
        center.parametrize(radius, state.angle) + center
    } else state.position

    return position - Offset(halfWidth, halfHeight)
}