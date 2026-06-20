package app.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import app.theme.AppColor

@Composable
fun HealthBar(
    modifier: Modifier = Modifier,
    health: Int = 100,
    foregroundColor: Color = AppColor.LIGHT_GREEN.color,
    backgroundColor: Color = Color.Gray,
    foregroundImage: Painter? = null,
    backgroundImage: Painter? = null,
    cornerRadius: Float = 45f,
    height: Dp = 20.dp
) {
    val healthPercentage = remember(health) { health.coerceIn(0, 100) / 100f }
    val outerShape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val innerShape = remember(cornerRadius) {
        RoundedCornerShape(
            topStart = CornerSize(cornerRadius),
            bottomStart = CornerSize(cornerRadius),
            topEnd = CornerSize(0f),
            bottomEnd = CornerSize(0f)
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(outerShape)
            .background(backgroundColor)
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
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
                    .clip(outerShape)
                    .background(backgroundColor)
            )
        }

        if (foregroundImage != null) {
            Image(
                painter = foregroundImage,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(healthPercentage)
                    .height(height)
                    .clip(innerShape)
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth(healthPercentage)
                    .height(height)
                    .clip(innerShape)
                    .background(foregroundColor)
            )
        }
    }
}