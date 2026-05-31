package feature.game.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import app.presentation.AnimatedText
import app.presentation.Pulsating
import feature.game.domain.model.StartCountdownViewState

@Composable
fun IntroCountdownView(
    state: StartCountdownViewState?,
) {
    if (state == null) return
    val colorState = animateColorAsState(targetValue = state.textColor)
    Pulsating {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedText(
                text = state.text,
                textSize = state.textSize,
                color = colorState.value,
                modifier = Modifier
                    .rotate(180f)
                    .offset(y = (120).dp)
                    .align(Alignment.Center)

            )
            AnimatedText(
                text = state.text,
                textSize = state.textSize,
                color = colorState.value,
                modifier = Modifier
                    .offset(y = 120.dp)
                    .align(Alignment.Center)
            )
        }
    }
}