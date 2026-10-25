package feature.game.presentation.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.presentation.AnimatedText
import app.presentation.Pulsating
import app.theme.appRed
import feature.common.model.Position
import feature.game.domain.model.GameOverResult
import feature.game.presentation.GameState
import feature.game.presentation.model.Player

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

@Preview
@Composable
private fun GameOverViewPreview() {
    val topPlayer = Player(id = "top", position = Position.TOP)
    val bottomPlayer = Player(id = "bottom", position = Position.BOTTOM)

    Box(modifier = Modifier.size(400.dp)) {
        GameOverView(
            state = GameState(
                gameId = "preview",
                topPlayer = topPlayer,
                bottomPlayer = bottomPlayer,
                gameOverResult = GameOverResult(winner = bottomPlayer),
            ),
        )
    }
}