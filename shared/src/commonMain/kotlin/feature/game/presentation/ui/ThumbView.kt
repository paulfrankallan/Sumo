package feature.game.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.rotate
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import app.theme.appRed
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.rikishi_blue_standing
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun Rikishi(
    spotDiameter: Dp,
    spotBackgroundColor: Color = Color.Transparent,//Color.Black,
    spotBackgroundImage: DrawableResource? = null,
    spotForegroundColor: Color?,
    spotForegroundImage: DrawableResource = Res.drawable.rikishi_blue_standing,
    thumbOffsetPosition: Offset,
    isOutOfBounds: Boolean,
    updateThumbOffsetPosition: (Offset) -> Unit = {},
    /** Called with the drag delta — parallel input path for the new game engine. */
    onDragDelta: (Offset) -> Unit = {},
    onDragEnd: () -> Unit = {},
    onPressed: (Boolean) -> Unit,
    onReleased: (Boolean) -> Unit,
    rotationDegrees: Float = 0f,
    isPushing: Boolean = false,
    // When false, disable touch-based control — only joystick input will move Rikishi.
    isFullContact: Boolean = false,
) {
    val density = LocalDensity.current
    val spotDiameterPx = with(density) { spotDiameter.toPx() }
    val spotRadiusPx = spotDiameterPx / 2
    val painterForeground = painterResource(resource = spotForegroundImage)
    val painterBackground = spotBackgroundImage?.let {
        painterResource(resource = spotBackgroundImage)
    }
    val imageSize = Size(spotDiameterPx, spotDiameterPx)
    val foregroundImageOffset = if (isPushing) {
        pushImageOffset(rotationDegrees, spotDiameterPx)
    } else {
        Offset.Zero
    }
    val intOffset = remember { mutableStateOf(IntOffset(0, 0)) }
    val circleCenter = Offset(spotRadiusPx, spotRadiusPx)
    val isDragging = remember { mutableStateOf(false) }

    // Build the modifier and only attach touch handlers when full-contact is enabled.
    var canvasModifier = Modifier
        .size(spotDiameter)
        .offset {
            intOffset.value = IntOffset(
                thumbOffsetPosition.x.roundToInt(),
                thumbOffsetPosition.y.roundToInt()
            )
            intOffset.value
        }

    if (isFullContact) {
        canvasModifier = canvasModifier
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { _ ->
                        isDragging.value = true
                        onPressed(true)
                    },
                    onDragEnd = {
                        isDragging.value = false
                        onPressed(false)
                        onReleased(true)
                        onDragEnd()
                    },
                    onDragCancel = {
                        isDragging.value = false
                        onPressed(false)
                        onReleased(true)
                        onDragEnd()
                    }
                ) { change, dragAmount ->
                    change.consume()
                    // dragAmount is the screen-space delta — unaffected by the composable's
                    // own position changing (avoids the feedback-loop wobble that occurs when
                    // using change.position in local coordinates).
                    if (isTouchOutsideCircle(change.position, circleCenter, spotRadiusPx)) {
                        onReleased(true)
                    }
                    updateThumbOffsetPosition(dragAmount)
                    onDragDelta(dragAmount)
                }
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressed(true)
                        try {
                            awaitRelease()
                        } finally {
                            if (!isDragging.value) {
                                onPressed(false)
                                onReleased(true)
                            }
                        }
                    }
                )
            }
    }

    Canvas(modifier = canvasModifier) {
        drawCircle(
            color = spotBackgroundColor,
            center = Offset(size.width / 2, size.height / 2),
            radius = spotRadiusPx
        )
        painterBackground?.let {
            with(painterBackground) {
                drawIntoCanvas { canvas ->
                    val path = Path().apply {
                        addOval(Rect(0f, 0f, imageSize.width, imageSize.height))
                    }
                    canvas.withSave {
                        canvas.clipPath(path)
                        draw(
                            size = imageSize,
                        )
                    }
                }
            }
        }
        with(painterForeground) {
            drawIntoCanvas { canvas ->
                canvas.withSave {
                    canvas.translate(foregroundImageOffset.x, foregroundImageOffset.y)
                    canvas.rotate(rotationDegrees, center.x, center.y)
                    draw(
                        size = imageSize,
                        colorFilter = spotForegroundColor?.let {
                            ColorFilter.tint(
                                color = if (isOutOfBounds) {
                                    appRed
                                } else {
                                    spotForegroundColor
                                }
                            )
                        },
                    )
                }
            }
        }
    }
}

fun isTouchOutsideCircle(
    touchPosition: Offset,
    circleCenter: Offset,
    circleRadius: Float
): Boolean {
    val distance = sqrt(
        (touchPosition.x - circleCenter.x)
            .pow(2) + (touchPosition.y - circleCenter.y)
            .pow(2)
    )
    return distance > circleRadius
}

private fun pushImageOffset(
    rotationDegrees: Float,
    spotDiameterPx: Float,
): Offset {
    val radians = rotationDegrees.toDouble() * PI / 180.0
    val distance = spotDiameterPx * PUSH_IMAGE_FORWARD_OFFSET_RATIO
    return Offset(
        x = (-sin(radians) * distance).toFloat(),
        y = (cos(radians) * distance).toFloat(),
    )
}

// Counteracts transparent padding at the pushing hands without changing collision physics.
private const val PUSH_IMAGE_FORWARD_OFFSET_RATIO = 0.015f