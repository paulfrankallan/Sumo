package feature.game.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import app.LocalScreen
import feature.common.presentation.Intent
import feature.game.domain.input.InputCommand
import feature.game.domain.input.InputSource
import feature.game.joystick.ui.state.JoystickState
import feature.game.presentation.GameIntent
import feature.game.presentation.GameState
import feature.game.presentation.model.Player
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.shikiri_sen
import sumo.shared.generated.resources.tawara
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun Dohyo(
    state: GameState,
    modifier: Modifier = Modifier,
    onPressed: (Boolean, Player) -> Unit,
    onReleased: (player: Player) -> Unit,
    onIntent: (Intent) -> Unit,
    resetThumbPositions: Boolean,
    onInputCommand: (InputCommand) -> Unit = {},
    topJoystickState: JoystickState? = null,
    bottomJoystickState: JoystickState? = null,
) {
    val density = LocalDensity.current
    val currentState = rememberUpdatedState(state)
    val circleStroke = remember { 5f }
    val arenaMeasuredSent = remember { mutableStateOf(false) }
    val circleCenter = remember { mutableStateOf(Offset.Zero) }
    val circleRadius = remember { mutableStateOf(0f) }
    val screenWidth = LocalScreen.current.width
    val spotDiameter = remember(screenWidth) { screenWidth * 0.20f }
    val spotDiameterPx = with(density) { spotDiameter.toPx() }
    val spotRadiusPx = spotDiameterPx / 2f

    val topThumbPosition = state.topRikishiPosition?.let {
        Offset(it.x - spotRadiusPx, it.y - spotRadiusPx)
    } ?: Offset.Zero
    val bottomThumbPosition = state.bottomRikishiPosition?.let {
        Offset(it.x - spotRadiusPx, it.y - spotRadiusPx)
    } ?: Offset.Zero

    val rotationDegrees = remember(state.topRikishiPosition, state.bottomRikishiPosition) {
        val topCenter = state.topRikishiPosition
        val bottomCenter = state.bottomRikishiPosition
        if (topCenter == null || bottomCenter == null) {
            0f
        } else {
            val dx = bottomCenter.x - topCenter.x
            val dy = bottomCenter.y - topCenter.y
            if (dx == 0f && dy == 0f) 0f
            else (atan2(-dx.toDouble(), dy.toDouble()) * 180.0 / PI).toFloat()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val tawaraBalesPainter = painterResource(resource = Res.drawable.tawara)
        val shikiriSenPainter = painterResource(resource = Res.drawable.shikiri_sen)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            circleCenter.value = Offset(canvasWidth / 2f, canvasHeight / 2f)
            circleRadius.value = canvasWidth * 0.425f

            if (!arenaMeasuredSent.value && circleRadius.value > 0f) {
                arenaMeasuredSent.value = true
                onIntent(
                    GameIntent.ArenaMeasured(
                        centre = circleCenter.value,
                        arenaRadius = circleRadius.value,
                        rikishiRadius = spotRadiusPx,
                    )
                )
            }

            val imageSize = Size(
                width = circleRadius.value * 2f + 125f,
                height = circleRadius.value * 2f + 125f,
            )

            drawCircle(
                color = Color.Transparent,
                center = circleCenter.value,
                radius = circleRadius.value,
                style = Stroke(width = circleStroke)
            )

            with(tawaraBalesPainter) {
                withTransform({
                    translate(
                        circleCenter.value.x - (imageSize.width / 2f),
                        circleCenter.value.y - (imageSize.height / 2f),
                    )
                }) {
                    draw(size = imageSize)
                }
            }

            val shikiriSenWidth = circleRadius.value * 2f * SHIKIRI_SEN_WIDTH_RATIO
            val shikiriSenHeight = shikiriSenWidth / SHIKIRI_SEN_ASPECT_RATIO
            val shikiriSenSeparation = circleRadius.value * 2f * SHIKIRI_SEN_SEPARATION_RATIO
            val shikiriSenSize = Size(shikiriSenWidth, shikiriSenHeight)
            val topShikiriTopLeft = Offset(
                x = circleCenter.value.x - (shikiriSenWidth / 2f),
                y = circleCenter.value.y - (shikiriSenSeparation / 2f) - (shikiriSenHeight / 2f),
            )
            val bottomShikiriTopLeft = Offset(
                x = circleCenter.value.x - (shikiriSenWidth / 2f),
                y = circleCenter.value.y + (shikiriSenSeparation / 2f) - (shikiriSenHeight / 2f),
            )
            with(shikiriSenPainter) {
                withTransform({ translate(topShikiriTopLeft.x, topShikiriTopLeft.y) }) {
                    draw(size = shikiriSenSize)
                }
                withTransform({ translate(bottomShikiriTopLeft.x, bottomShikiriTopLeft.y) }) {
                    draw(size = shikiriSenSize)
                }
            }
        }

        Rikishi(
            thumbOffsetPosition = topThumbPosition,
            isOutOfBounds = false,
            onDragDelta = { delta ->
                onInputCommand(
                    InputCommand(
                        playerId = currentState.value.topPlayer.id,
                        velocityVector = delta,
                        source = InputSource.DIRECT_DRAG,
                    )
                )
            },
            onDragEnd = { onIntent(GameIntent.DragEnded(currentState.value.topPlayer)) },
            spotForegroundColor = state.ui.topThumbView.foregroundColor,
            spotForegroundImage = state.ui.topThumbView.foregroundImage,
            onPressed = { onPressed(it, currentState.value.topPlayer) },
            onReleased = { onReleased(currentState.value.topPlayer) },
            spotDiameter = spotDiameter,
            rotationDegrees = rotationDegrees
        )

        Rikishi(
            thumbOffsetPosition = bottomThumbPosition,
            isOutOfBounds = false,
            onDragDelta = { delta ->
                onInputCommand(
                    InputCommand(
                        playerId = currentState.value.bottomPlayer.id,
                        velocityVector = delta,
                        source = InputSource.DIRECT_DRAG,
                    )
                )
            },
            onDragEnd = { onIntent(GameIntent.DragEnded(currentState.value.bottomPlayer)) },
            spotForegroundColor = state.ui.bottomThumbView.foregroundColor,
            spotForegroundImage = state.ui.bottomThumbView.foregroundImage,
            onPressed = { onPressed(it, currentState.value.bottomPlayer) },
            onReleased = { onReleased(currentState.value.bottomPlayer) },
            spotDiameter = spotDiameter,
            rotationDegrees = rotationDegrees + 180f
        )

        GameOverView(state = currentState.value)
    }

    LaunchedEffect(resetThumbPositions) {
        onIntent(GameIntent.ResetThumbsComplete)
    }

    LaunchedEffect(topJoystickState) {
        if (topJoystickState == null) return@LaunchedEffect
        while (true) {
            if (topJoystickState.isValid) {
                val angle = atan2(topJoystickState.offset.y, topJoystickState.offset.x)
                onInputCommand(
                    InputCommand(
                        playerId = currentState.value.topPlayer.id,
                        velocityVector = Offset(
                            x = -cos(angle) * topJoystickState.strength * JOYSTICK_MOVEMENT_SPEED_PX,
                            y = -sin(angle) * topJoystickState.strength * JOYSTICK_MOVEMENT_SPEED_PX,
                        ),
                        source = InputSource.JOYSTICK,
                    )
                )
            }
            delay(JOYSTICK_TICK_MS)
        }
    }

    LaunchedEffect(bottomJoystickState) {
        if (bottomJoystickState == null) return@LaunchedEffect
        while (true) {
            if (bottomJoystickState.isValid) {
                val angle = atan2(bottomJoystickState.offset.y, bottomJoystickState.offset.x)
                onInputCommand(
                    InputCommand(
                        playerId = currentState.value.bottomPlayer.id,
                        velocityVector = Offset(
                            x = cos(angle) * bottomJoystickState.strength * JOYSTICK_MOVEMENT_SPEED_PX,
                            y = sin(angle) * bottomJoystickState.strength * JOYSTICK_MOVEMENT_SPEED_PX,
                        ),
                        source = InputSource.JOYSTICK,
                    )
                )
            }
            delay(JOYSTICK_TICK_MS)
        }
    }
}

private const val SHIKIRI_SEN_ASPECT_RATIO = 749f / 65f
private const val SHIKIRI_SEN_WIDTH_RATIO = 0.2f
private const val SHIKIRI_SEN_SEPARATION_RATIO = 0.16f
private const val JOYSTICK_MOVEMENT_SPEED_PX = 6f
private const val JOYSTICK_TICK_MS = 16L
