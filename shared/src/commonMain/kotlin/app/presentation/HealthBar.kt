package app.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.theme.AppColor
import app.theme.sumoDarkWood
import app.theme.sumoInk
import app.theme.sumoRicePaper
import app.theme.sumoSand
import app.theme.sumoStrawGold

@Composable
fun HealthBar(
    modifier: Modifier = Modifier,
    health: Int = 100,
    foregroundColor: Color = AppColor.LIGHT_GREEN.color,
    backgroundColor: Color = sumoDarkWood,
    foregroundImage: Painter? = null,
    backgroundImage: Painter? = null,
    cornerRadius: Float = 16f,
    height: Dp = 12.dp,
) {
    val healthPercentage = remember(health) { health.coerceIn(0, 100) / 100f }
    val outerShape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val innerShape = remember(cornerRadius) {
        RoundedCornerShape(
            topStart = CornerSize(cornerRadius),
            bottomStart = CornerSize(cornerRadius),
            topEnd = CornerSize(cornerRadius),
            bottomEnd = CornerSize(cornerRadius),
        )
    }
    val troughBrush = remember(backgroundColor) {
        Brush.verticalGradient(
            colors = listOf(
                lerp(backgroundColor, sumoSand, 0.16f),
                backgroundColor,
                lerp(backgroundColor, sumoInk, 0.48f),
            ),
        )
    }
    val fillBrush = remember(foregroundColor) {
        Brush.verticalGradient(
            colors = listOf(
                lerp(foregroundColor, sumoRicePaper, 0.25f),
                foregroundColor,
                lerp(foregroundColor, sumoInk, 0.35f),
            ),
        )
    }
    val frameBrush = remember {
        Brush.horizontalGradient(
            colors = listOf(
                sumoSand,
                sumoStrawGold,
                sumoRicePaper,
                sumoStrawGold,
                sumoSand,
            ),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(outerShape)
            .background(troughBrush)
            .border(1.dp, frameBrush, outerShape),
    ) {
        if (backgroundImage != null) {
            Image(
                painter = backgroundImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(outerShape)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(1.dp),
        ) {
            if (foregroundImage != null) {
                Image(
                    painter = foregroundImage,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth(healthPercentage)
                        .fillMaxHeight()
                        .clip(innerShape),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(healthPercentage)
                        .fillMaxHeight()
                        .clip(innerShape)
                        .background(fillBrush),
                )
            }
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(1.dp),
        ) {
            val segmentWidth = size.width / HEALTH_BAR_SEGMENT_COUNT
            val segmentInset = size.height * HEALTH_BAR_SEGMENT_INSET_RATIO
            val dividerColor = sumoInk.copy(alpha = HEALTH_BAR_DIVIDER_ALPHA)
            for (segment in 1 until HEALTH_BAR_SEGMENT_COUNT) {
                val x = segment * segmentWidth
                drawLine(
                    color = dividerColor,
                    start = Offset(x, segmentInset),
                    end = Offset(x, size.height - segmentInset),
                    strokeWidth = HEALTH_BAR_DIVIDER_WIDTH.toPx(),
                )
            }
            drawLine(
                color = sumoRicePaper.copy(alpha = HEALTH_BAR_HIGHLIGHT_ALPHA),
                start = Offset(0f, HEALTH_BAR_HIGHLIGHT_WIDTH.toPx()),
                end = Offset(size.width, HEALTH_BAR_HIGHLIGHT_WIDTH.toPx()),
                strokeWidth = HEALTH_BAR_HIGHLIGHT_WIDTH.toPx(),
            )
        }
    }
}

private const val HEALTH_BAR_SEGMENT_COUNT = 6
private const val HEALTH_BAR_SEGMENT_INSET_RATIO = 0.2f
private const val HEALTH_BAR_DIVIDER_ALPHA = 0.42f
private val HEALTH_BAR_DIVIDER_WIDTH = 0.75.dp
private const val HEALTH_BAR_HIGHLIGHT_ALPHA = 0.22f
private val HEALTH_BAR_HIGHLIGHT_WIDTH = 0.5.dp