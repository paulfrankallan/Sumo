package feature.game.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import feature.common.presentation.Intent
import feature.game.joystick.ui.state.JoystickState
import feature.game.presentation.GameState
import feature.game.presentation.model.Player
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.janome

@Composable
fun Janome(
    state: GameState,
    modifier: Modifier = Modifier,
    onDamageDetected: (Player) -> Unit,
    onPressed: (Boolean, Player) -> Unit,
    onReleased: (player: Player) -> Unit,
    onIntent: (Intent) -> Unit,
    resetTopThumbPosition: Boolean,
    resetBottomThumbPosition: Boolean,
    topJoystickState: JoystickState? = null,
    bottomJoystickState: JoystickState? = null,
) {
    Box(
        modifier = modifier.padding(horizontal = 8.dp).paint(
            painter = painterResource(Res.drawable.janome),
            contentScale = ContentScale.FillBounds
        )
    ) {
        Dohyo(
            state = state,
            onDamageDetected = onDamageDetected,
            onPressed = onPressed,
            onReleased = onReleased,
            onIntent = onIntent,
            resetTopThumbPosition = resetTopThumbPosition,
            resetBottomThumbPosition = resetBottomThumbPosition,
            topJoystickState = topJoystickState,
            bottomJoystickState = bottomJoystickState,
        )
    }
}
