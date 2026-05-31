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
import androidx.compose.ui.unit.sp
import app.presentation.AnimatedText
import app.presentation.Pulsating
import app.theme.appRed
import feature.game.presentation.GameState

@Composable
fun GameOverView(state: GameState) {
    if (!state.isGameOver) return
    val colorState = animateColorAsState(targetValue = appRed)
    Pulsating {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AnimatedText(
                text = "Game Over",
                textSize = 36.sp,
                color = colorState.value,
                modifier = Modifier
                    .rotate(180f)
                    .offset(y = (14).dp)
                    .align(Alignment.Center)

            )
            AnimatedText(
                text = "Game Over",
                textSize = 36.sp,
                color = colorState.value,
                modifier = Modifier
                    .offset(y = 14.dp)
                    .align(Alignment.Center)
            )
        }
    }
}