package app.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import app.theme.appRed
import app.theme.colorHomeLightGreen
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import kotlin.math.ceil

@Composable
fun DrawableProgressIndicator(
    progressPercent: Float,
    drawableResource: DrawableResource,
    foregroundColor: Color = colorHomeLightGreen,
    backgroundColor: Color = appRed,
    durationMillis: Int = 5000,
    direction: Direction = Direction.Down,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val progressFloat = progressPercent.coerceIn(0f, 100f) / 100f
    var initialHealthPercentage by remember { mutableStateOf(0f) }
    val animatedHealthPercentage by animateFloatAsState(
        targetValue = initialHealthPercentage,
        animationSpec = tween(
            durationMillis = durationMillis,
            easing = LinearEasing
        )
    )
    LaunchedEffect(progressFloat) {
        initialHealthPercentage = progressFloat
    }
    val density = LocalDensity.current
    var widthPx by remember { mutableStateOf(0) }
    var heightPx by remember { mutableStateOf(0) }
    val widthDp = with(density) { widthPx.toDp() }
    val heightDp = with(density) { heightPx.toDp() }
    val imageBitmap = imageResource(drawableResource)
    Box(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                widthPx = layoutCoordinates.size.width
                heightPx = layoutCoordinates.size.height
            }
            .clip(RoundedCornerShape(25))
            .clickable { onClick() }
            .drawWithContent {
                with(drawContext.canvas) {
                    saveLayer(
                        bounds = Rect(0f, 0f, size.width, size.height),
                        paint = Paint(),
                    )
                    drawContent()
                    drawImage(
                        image = imageBitmap,
                        dstSize = IntSize(
                            width = ceil(widthDp.toPx()).toInt(),
                            height = ceil(heightDp.toPx()).toInt()
                        ),
                        blendMode = BlendMode.DstIn
                    )
                    restore()
                }
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp)
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height((heightDp * animatedHealthPercentage))
                    .background(foregroundColor)
                    .align(
                        if (direction == Direction.Down) {
                            Alignment.TopCenter
                        } else {
                            Alignment.BottomCenter
                        }
                    )
            )
        }
    }
}

enum class Direction {
    Up,
    Down,
}