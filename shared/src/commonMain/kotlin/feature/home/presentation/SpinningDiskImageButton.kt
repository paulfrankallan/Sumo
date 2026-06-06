package feature.home.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import app.LocalScreen
import co.touchlab.kermit.Logger
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.btn_4
import sumo.shared.generated.resources.dohyo

@Composable
fun SpinningDiskImageButton(
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val screenWidth = LocalScreen.current.width
    val radius = screenWidth * 0.4f
    val diameter = radius * 2
    val image = painterResource(resource = Res.drawable.dohyo)

    Box(
        modifier = Modifier
            .size(diameter)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        Canvas(
            modifier = Modifier
                .size(screenWidth)
        ) {
            rotate(rotation) {
                with(image) {
                    translate(left = center.x - radius.toPx(), top = center.y - radius.toPx()) {
                        draw(
                            size = Size(radius.toPx() * 2, radius.toPx() * 2),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpinningDiskButton() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val screenWidth = LocalScreen.current.width
    val radius = screenWidth * 0.4f

    Canvas(
        modifier = Modifier
            .clickable {
                Logger.d { "SpinningDiskButton clicked" }
            }
            .size(100.dp)
    ) {
        rotate(rotation) {
            drawCircle(
                color = Color.Red,
                radius = radius.toPx()
            )
            drawLine(
                color = Color.Black,
                start = center.copy(x = center.x - radius.toPx()),
                end = center.copy(x = center.x + radius.toPx()),
                strokeWidth = 4f
            )
        }
    }
}