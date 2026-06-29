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
import sumo.shared.generated.resources.rikishi_blue
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@Composable
fun Rikishi(
    spotDiameter: Dp,
    spotBackgroundColor: Color = Color.Transparent,
    spotBackgroundImage: DrawableResource? = null,
    spotForegroundColor: Color?,
    spotForegroundImage: DrawableResource = Res.drawable.rikishi_blue,
    thumbOffsetPosition: Offset,
    isOutOfBounds: Boolean,
    updateThumbOffsetPosition: (Offset) -> Unit = {},
    /** Called with the drag delta — parallel input path for the new game engine. */
    onDragDelta: (Offset) -> Unit = {},
    onPressed: (Boolean) -> Unit,
    onReleased: (Boolean) -> Unit,
    rotationDegrees: Float = 0f,
) {
    val density = LocalDensity.current
    val spotDiameterPx = with(density) { spotDiameter.toPx() }
    val spotRadiusPx = spotDiameterPx / 2
    val painterForeground = painterResource(resource = spotForegroundImage)
    val painterBackground = spotBackgroundImage?.let {
        painterResource(resource = spotBackgroundImage)
    }
    val imageSize = Size(spotDiameterPx, spotDiameterPx)
    val intOffset = remember { mutableStateOf(IntOffset(0, 0)) }
    val circleCenter = Offset(spotRadiusPx, spotRadiusPx)
    val isDragging = remember { mutableStateOf(false) }
    val touchOffset = remember { mutableStateOf(Offset.Zero) }
    val hasReleased = remember { mutableStateOf(false) }

    Canvas(
        modifier = Modifier
            .size(spotDiameter)
            .offset {
                intOffset.value = IntOffset(
                    thumbOffsetPosition.x.roundToInt(),
                    thumbOffsetPosition.y.roundToInt()
                )
                intOffset.value
            }
            // For debugging purposes
//            .pointerInput(Unit) {
//                awaitPointerEventScope {
//                    while (true) {
//                        val event = awaitPointerEvent()
//                        Logger.d { "PFA ${event.type}, ${event.changes.first().position}" }
//                    }
//                }
//            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        isDragging.value = true
                        onPressed(true)
                        // offset is local pointer position inside the ThumbView's Canvas in newer Compose versions
                        // store the local touch offset (not converted by thumb position)
                        touchOffset.value = offset
                        hasReleased.value = false
                    },
                    onDragEnd = {
                        isDragging.value = false
                        onPressed(false)
                        onReleased(true)
                    }
                ) { change, _ ->
                    change.consume()
                    val touchPosition = change.position
                    if (isTouchOutsideCircle(touchPosition, circleCenter, spotRadiusPx)) {
                        onReleased(true)
                        hasReleased.value = true
                    }
                    updateThumbOffsetPosition(touchPosition - touchOffset.value)
                    onDragDelta(touchPosition - touchOffset.value)
                    touchOffset.value = touchPosition
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
    ) {
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