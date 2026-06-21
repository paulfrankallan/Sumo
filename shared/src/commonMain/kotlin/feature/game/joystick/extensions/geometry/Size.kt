package feature.game.joystick.extensions.geometry

import androidx.compose.ui.geometry.Size
import kotlin.math.min

/**
 * Clamps this size to fit within the given maximum size, maintaining the aspect ratio.
 *
 * The new Size will be fit within the max Size, scaled down if necessary.
 *
 * @param max The maximum Size to clamp to. If unspecified, returns a copy of this Size.
 */
fun Size.clamp(max: Size): Size {
    if (max == Size.Unspecified)
        return this.copy()

    val scaleFactorWidth = max.width / this.width
    val scaleFactorHeight = max.height / this.height

    val scaleFactor = min(scaleFactorWidth, scaleFactorHeight)

    val finalScale = min(scaleFactor, 1.0f)

    return Size(
        width = this.width * finalScale,
        height = this.height * finalScale
    )
}