package app.presentation

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import org.jetbrains.compose.resources.Font
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.major

@Composable
fun Pulsating(pulseFraction: Float = 1f, content: @Composable () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = pulseFraction,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}

@Composable
fun AnimatedText(
    text: String,
    textSize: TextUnit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        color = color,
        fontSize = textSize,
        textAlign = TextAlign.Center,
        fontFamily = FontFamily(Font(Res.font.major)),
        modifier = modifier.animateContentSize()
    )
}