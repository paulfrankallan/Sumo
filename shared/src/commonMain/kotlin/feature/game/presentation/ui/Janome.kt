package feature.game.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.LocalScreen
import app.Screen
import feature.common.model.Position
import feature.common.presentation.Intent
import feature.game.domain.input.InputCommand
import feature.game.joystick.ui.state.JoystickState
import feature.game.presentation.GameState
import feature.game.presentation.SHIKIRI_SEN_CENTER_OFFSET_RATIO
import feature.game.presentation.model.Player
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.janome

@Composable
fun Janome(
    state: GameState,
    modifier: Modifier = Modifier,
    onPressed: (Boolean, Player) -> Unit,
    onReleased: (player: Player) -> Unit = {},
    onIntent: (Intent) -> Unit,
    resetThumbPositions: Boolean,
    onInputCommand: (InputCommand) -> Unit = {},
    topJoystickState: JoystickState? = null,
    bottomJoystickState: JoystickState? = null,
    isFullContact: Boolean = false,
) {
    Box(
        modifier = modifier.padding(horizontal = 8.dp).paint(
            painter = painterResource(Res.drawable.janome),
            contentScale = ContentScale.FillBounds
        )
    ) {
        Dohyo(
            state = state,
            onPressed = onPressed,
            onReleased = onReleased,
            onIntent = onIntent,
            resetThumbPositions = resetThumbPositions,
            onInputCommand = onInputCommand,
            topJoystickState = topJoystickState,
            bottomJoystickState = bottomJoystickState,
            isFullContact = isFullContact,
        )
        StartCeremonyView(
            isVisible = state.isStartCeremonyVisible,
            arenaCentre = state.arenaCentre,
            arenaRadius = state.arenaRadius,
            rikishiRadius = state.rikishiRadius,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Preview(name = "Default")
@Composable
private fun JanomePreview() {
    JanomePreviewContent(isStartCeremonyVisible = false)
}

@Preview(name = "Start ceremony")
@Composable
private fun JanomeStartCeremonyPreview() {
    JanomePreviewContent(isStartCeremonyVisible = true)
}

@Composable
private fun JanomePreviewContent(
    isStartCeremonyVisible: Boolean,
) {
    val previewSize = 400.dp
    val contentWidth = previewSize - 16.dp
    val density = LocalDensity.current
    val previewSizePx = with(density) { previewSize.toPx() }
    val contentWidthPx = with(density) { contentWidth.toPx() }
    val arenaCentre = Offset(
        x = contentWidthPx / 2f,
        y = previewSizePx / 2f,
    )
    val arenaRadius = contentWidthPx * 0.425f
    val rikishiRadius = previewSizePx * 0.08f
    val rikishiOffset = Offset(
        x = 0f,
        y = arenaRadius * SHIKIRI_SEN_CENTER_OFFSET_RATIO + rikishiRadius,
    )

    CompositionLocalProvider(
        LocalScreen provides Screen(width = previewSize, height = previewSize),
    ) {
        Janome(
            state = GameState(
                gameId = "preview",
                topPlayer = Player(id = "top", position = Position.TOP),
                bottomPlayer = Player(id = "bottom", position = Position.BOTTOM),
                isStartCeremonyVisible = isStartCeremonyVisible,
                topRikishiPosition = arenaCentre - rikishiOffset,
                bottomRikishiPosition = arenaCentre + rikishiOffset,
                arenaCentre = arenaCentre,
                arenaRadius = arenaRadius,
                rikishiRadius = rikishiRadius,
            ),
            modifier = Modifier.size(previewSize),
            onPressed = { _, _ -> },
            onIntent = {},
            resetThumbPositions = false,
        )
    }
}
