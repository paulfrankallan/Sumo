package feature.game.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import feature.common.presentation.Intent
import feature.game.presentation.GameState
import feature.game.presentation.model.Player
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.dohyo
import sumo.shared.generated.resources.dohyo3
import sumo.shared.generated.resources.dohyo4

@Composable
fun Dohyo(
    state: GameState,
    modifier: Modifier = Modifier,
    onDamageDetected: (Player) -> Unit,
    onPressed: (Boolean, Player) -> Unit,
    onReleased: (player: Player) -> Unit,
    onIntent: (Intent) -> Unit,
    resetThumbPositions: Boolean
) {
    Box(
        modifier = modifier.paint(
            painter = painterResource(Res.drawable.dohyo4),
            contentScale = ContentScale.FillBounds
        )
    ) {
        Arena(
            state = state,
            onDamageDetected = onDamageDetected,
            onPressed = onPressed,
            onReleased = onReleased,
            onIntent = onIntent,
            resetThumbPositions = resetThumbPositions
        )
    }
}
