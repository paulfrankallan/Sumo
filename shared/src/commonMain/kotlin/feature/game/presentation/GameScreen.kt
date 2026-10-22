package feature.game.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.presentation.HealthBar
import app.theme.playerOneColor
import app.theme.playerTwoColor
import com.russhwolf.settings.ExperimentalSettingsApi
import feature.common.model.Position
import feature.common.presentation.Intent
import feature.game.domain.input.InputCommand
import feature.game.domain.input.InputSource
import feature.game.domain.input.PowerMove
import feature.game.joystick.RikishiJoystick
import feature.game.joystick.ui.state.JoystickState
import feature.game.joystick.ui.view.rememberJoystickState
import feature.game.presentation.model.Player
import feature.game.presentation.ui.Janome
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import feature.settings.data.PrefsRepository
import platform.presentation.KeepScreenOn
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.game_bg
import sumo.shared.generated.resources.ready_btn
import sumo.shared.generated.resources.ready_btn_rip

@OptIn(ExperimentalSettingsApi::class)
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
        viewModel.playHyoshigiFeedback()
        showInterstitialAd()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onIntent(GameIntent.StopGame)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.onIntent(GameIntent.StopGame)
        }
    }

    val prefs: PrefsRepository = koinInject()
    val isFullContact = prefs.isFullContactEnabled()

    GameScreenContent(
        state = state,
        resetThumbPositions = resetThumbPositions,
        topJoystickState = topJoystickState,
        bottomJoystickState = bottomJoystickState,
        onIntent = viewModel::onIntent,
        onSubmitInput = viewModel::submitInput,
        onSubmitRapidTap = viewModel::submitRapidTap,
        isFullContact = isFullContact,
    )

    KeepScreenOn()
}

@OptIn(ExperimentalSettingsApi::class)
@Composable
fun GameScreenContent(
    state: GameState,
    resetThumbPositions: Boolean,
    topJoystickState: JoystickState,
    bottomJoystickState: JoystickState,
    onIntent: (Intent) -> Unit,
    onSubmitInput: (InputCommand) -> Unit,
    onSubmitRapidTap: (String, Offset) -> Unit,
    isFullContact: Boolean = false,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(Res.drawable.game_bg),
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
            ReadyOrJoystick(
                isReady = state.isTopPlayerReady,
                onReady = {
                    onIntent(GameIntent.PlayerReady(state.topPlayer.id))
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 8.dp)
                    .rotate(180f)
            ) { modifier ->
                RikishiJoystick(
                    state = topJoystickState,
                    primaryColor = playerTwoColor,
                    accentColor = playerTwoColor,
                    isPullPowerMoveEnabled = state.isPullPowerMoveEnabled,
                    onPull = {
                        onIntent(
                            GameIntent.PowerMovePressed(
                                playerId = state.topPlayer.id,
                                powerMove = PowerMove.PULL,
                            )
                        )
                    },
                    isPushPowerMoveEnabled = state.isPushPowerMoveEnabled,
                    onPush = {
                        onIntent(
                            GameIntent.PowerMovePressed(
                                playerId = state.topPlayer.id,
                                powerMove = PowerMove.PUSH,
                            )
                        )
                    },
                    onMoveStart = { snapshot ->
                        joystickTapDirection(
                            position = snapshot.position,
                            center = topJoystickState.center,
                            strength = snapshot.strength,
                            reverse = true,
                        )?.let { direction ->
                            onSubmitRapidTap(state.topPlayer.id, direction)
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
                    modifier = modifier,
                )
            }
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
                isFullContact = isFullContact,
            )
            HealthBar(
                health = state.bottomPlayer.health,
                foregroundColor = playerOneColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            ReadyOrJoystick(
                isReady = state.isBottomPlayerReady,
                onReady = {
                    onIntent(GameIntent.PlayerReady(state.bottomPlayer.id))
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 8.dp)
            ) { modifier ->
                RikishiJoystick(
                    state = bottomJoystickState,
                    primaryColor = playerOneColor,
                    accentColor = playerOneColor,
                    isPullPowerMoveEnabled = state.isPullPowerMoveEnabled,
                    onPull = {
                        onIntent(
                            GameIntent.PowerMovePressed(
                                playerId = state.bottomPlayer.id,
                                powerMove = PowerMove.PULL,
                            )
                        )
                    },
                    isPushPowerMoveEnabled = state.isPushPowerMoveEnabled,
                    onPush = {
                        onIntent(
                            GameIntent.PowerMovePressed(
                                playerId = state.bottomPlayer.id,
                                powerMove = PowerMove.PUSH,
                            )
                        )
                    },
                    onMoveStart = { snapshot ->
                        joystickTapDirection(
                            position = snapshot.position,
                            center = bottomJoystickState.center,
                            strength = snapshot.strength,
                            reverse = false,
                        )?.let { direction ->
                            onSubmitRapidTap(state.bottomPlayer.id, direction)
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
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun ReadyOrJoystick(
    isReady: Boolean,
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
    joystick: @Composable (Modifier) -> Unit,
) {
    Box(modifier = modifier) {
        AnimatedVisibility(
            visible = !isReady,
            enter = fadeIn(tween(READY_BUTTON_TRANSITION_DURATION_MILLIS)),
            exit = fadeOut(tween(READY_BUTTON_TRANSITION_DURATION_MILLIS)),
            modifier = Modifier.fillMaxSize(),
        ) {
            ReadyButton(
                onReady = onReady,
                modifier = Modifier.fillMaxSize(),
            )
        }
        AnimatedVisibility(
            visible = isReady,
            enter = fadeIn(tween(READY_BUTTON_TRANSITION_DURATION_MILLIS)),
            exit = fadeOut(tween(READY_BUTTON_TRANSITION_DURATION_MILLIS)),
            modifier = Modifier.fillMaxSize(),
        ) {
            joystick(Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun ReadyButton(
    onReady: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Image(
        painter = painterResource(
            if (isPressed) Res.drawable.ready_btn_rip else Res.drawable.ready_btn,
        ),
        contentDescription = null,
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onReady,
        ),
        contentScale = ContentScale.Fit,
    )
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
        onSubmitRapidTap = { _, _ -> },
    )
}

private fun joystickTapDirection(
    position: Offset,
    center: Offset,
    strength: Float,
    reverse: Boolean,
): Offset? {
    if (strength <= 0f) return null

    val direction = Offset(
        x = position.x - center.x,
        y = position.y - center.y,
    )
    if (direction == Offset.Zero) return null

    return if (reverse) {
        Offset(x = -direction.x, y = -direction.y)
    } else {
        direction
    }
}

private const val JOYSTICK_INPUT_SCALE = 6f
