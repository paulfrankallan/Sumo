package feature.game.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.presentation.HealthBar
import app.theme.playerOneColor
import app.theme.playerTwoColor
import feature.common.model.Position
import feature.common.presentation.Intent
import feature.game.domain.input.InputCommand
import feature.game.domain.input.InputSource
import feature.game.joystick.RikishiJoystick
import feature.game.joystick.ui.state.JoystickState
import feature.game.joystick.ui.view.rememberJoystickState
import feature.game.presentation.model.Player
import feature.game.presentation.ui.IntroCountdownView
import feature.game.presentation.ui.Janome
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.sand_dark
import sumo.shared.generated.resources.shikiri_sen

@Composable
fun GameScreen(
    showInterstitialAd: () -> Unit
) {
    val viewModel: GameViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val resetThumbPositions by viewModel.resetThumbPositions

    val topJoystickState = rememberJoystickState()
    val bottomJoystickState = rememberJoystickState()

    LaunchedEffect(Unit) {
        showInterstitialAd()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onIntent(GameIntent.GameOver())
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    GameScreenContent(
        state = state,
        resetThumbPositions = resetThumbPositions,
        topJoystickState = topJoystickState,
        bottomJoystickState = bottomJoystickState,
        onIntent = viewModel::onIntent,
        onSubmitInput = viewModel.gameLoop::submitInput,
    )
}

@Composable
fun GameScreenContent(
    state: GameState,
    resetThumbPositions: Boolean,
    topJoystickState: JoystickState,
    bottomJoystickState: JoystickState,
    onIntent: (Intent) -> Unit,
    onSubmitInput: (InputCommand) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(Res.drawable.sand_dark),
                contentScale = ContentScale.FillBounds
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().run {
                val insets = WindowInsets.safeContent.asPaddingValues()
                val symmetricPadding =
                    max(
                        insets.calculateTopPadding(),
                        insets.calculateBottomPadding()
                    )
                padding(vertical = symmetricPadding)
            }
        ) {
            // Sandbag strip — rotated 180° to face the top player's direction.
            Image(
                painter = painterResource(Res.drawable.shikiri_sen),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .rotate(180f),
                contentScale = ContentScale.FillBounds,
            )
            RikishiJoystick(
                state = topJoystickState,
                primaryColor = playerTwoColor,
                accentColor = playerTwoColor,
                onMoveStart = {
                    if (state.playState != PlayState.IN_PROGRESS || state.isGameOver) {
                        onIntent(GameIntent.StartGame)
                    }
                },
                onMove = { snapshot ->
                    onSubmitInput(InputCommand(
                        playerId = state.topPlayer.id,
                        // Negate: top joystick is rotated 180° visually.
                        velocityVector = Offset(
                            x = -snapshot.position.x.minus(topJoystickState.center.x)
                                .div(topJoystickState.radius)
                                .times(snapshot.strength * JOYSTICK_INPUT_SCALE),
                            y = -snapshot.position.y.minus(topJoystickState.center.y)
                                .div(topJoystickState.radius)
                                .times(snapshot.strength * JOYSTICK_INPUT_SCALE),
                        ),
                        source = InputSource.JOYSTICK,
                    ))
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp)
                    .rotate(180f)
            )
            HealthBar(
                health = state.topPlayer.health,
                foregroundColor = playerTwoColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            Janome(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                onPressed = { isPressed, player ->
                    onIntent(GameIntent.PressStateChanged(isPressed, player))
                },
                resetThumbPositions = resetThumbPositions,
                onInputCommand = onSubmitInput,
                onIntent = onIntent,
                topJoystickState = topJoystickState,
                bottomJoystickState = bottomJoystickState,
            )
            HealthBar(
                health = state.bottomPlayer.health,
                foregroundColor = playerOneColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            RikishiJoystick(
                state = bottomJoystickState,
                primaryColor = playerOneColor,
                accentColor = playerOneColor,
                onMoveStart = {
                    if (state.playState != PlayState.IN_PROGRESS || state.isGameOver) {
                        onIntent(GameIntent.StartGame)
                    }
                },
                onMove = { snapshot ->
                    onSubmitInput(InputCommand(
                        playerId = state.bottomPlayer.id,
                        velocityVector = Offset(
                            x = snapshot.position.x.minus(bottomJoystickState.center.x)
                                .div(bottomJoystickState.radius)
                                .times(snapshot.strength * JOYSTICK_INPUT_SCALE),
                            y = snapshot.position.y.minus(bottomJoystickState.center.y)
                                .div(bottomJoystickState.radius)
                                .times(snapshot.strength * JOYSTICK_INPUT_SCALE),
                        ),
                        source = InputSource.JOYSTICK,
                    ))
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            )
            // Sandbag strip at the bottom edge.
            Image(
                painter = painterResource(Res.drawable.shikiri_sen),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.FillBounds,
            )
        }
    }
    IntroCountdownView(state = state.startCountdownViewState)
}

@Preview
@Composable
private fun GameScreenContentPreview() {
    GameScreenContent(
        state = GameState(
            gameId = "preview",
            topPlayer = Player(id = "top", position = Position.TOP),
            bottomPlayer = Player(id = "bottom", position = Position.BOTTOM),
        ),
        resetThumbPositions = false,
        topJoystickState = rememberJoystickState(),
        bottomJoystickState = rememberJoystickState(),
        onIntent = {},
        onSubmitInput = {},
    )
}

private const val JOYSTICK_INPUT_SCALE = 6f
