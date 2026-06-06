package feature.game.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.withSave
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.theme.appClay
import app.theme.appGold
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.fingerprint

@Composable
fun DifficultyModifiers(
    spotDiameter: Dp = 90.dp,
    spotBackgroundColor: Color = appClay,
    spotForegroundColor: Color = appGold,
    spotBackgroundImage: DrawableResource = Res.drawable.fingerprint,
    onPressed: (Boolean) -> Unit = {},
) {
    val density = LocalDensity.current
    val spotDiameterPx = with(density) { spotDiameter.toPx() }
    val spotRadiusPx = spotDiameterPx / 2
    val painterForeground = painterResource(resource = Res.drawable.fingerprint)
    val painterBackground = painterResource(resource = spotBackgroundImage)
    val imageSize = Size(spotDiameterPx, spotDiameterPx)

    Canvas(
        modifier = androidx.compose.ui.Modifier
            .size(spotDiameter)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        onPressed(true)
                        try {
                            awaitRelease()
                        } finally {
                            onPressed(false)
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
        with(painterBackground) {
            drawIntoCanvas { canvas ->
                val path = Path().apply {
                    addOval(Rect(0f, 0f, imageSize.width, imageSize.height))
                }
                canvas.withSave {
                    canvas.clipPath(path)
                    draw(size = imageSize)
                }
            }
        }
        with(painterForeground) {
            draw(
                size = imageSize,
                colorFilter = ColorFilter.tint(
                    color = spotForegroundColor
                ),
            )
        }
    }
}
