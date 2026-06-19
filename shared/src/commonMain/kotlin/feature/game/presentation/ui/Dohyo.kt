package feature.game.presentation.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.LocalDensity
import app.LocalScreen
import feature.common.presentation.Intent
import feature.game.domain.logic.ArenaPhysics.calculatePushVector
import feature.game.domain.logic.ArenaPhysics.doThumbSpotsOverlap
import feature.game.domain.logic.ArenaPhysics.isOutOfBounds
import feature.game.presentation.GameIntent
import feature.game.presentation.GameState
import feature.game.presentation.PlayState
import feature.game.presentation.model.Player
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.shikiri_sen
import sumo.shared.generated.resources.tawara
import kotlin.math.PI
import kotlin.math.atan2

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
    val density = LocalDensity.current
    val circleStroke = remember { 5f }
    val circleCenter = remember { mutableStateOf(Offset.Zero) }
    val circleDiameter = remember { mutableStateOf(0f) }
    val circleRadius = remember { mutableStateOf(0f) }
    val topThumbPosition = remember { mutableStateOf(Offset.Zero) }
    val bottomThumbPosition = remember { mutableStateOf(Offset.Zero) }
    val screenWidth = LocalScreen.current.width
    val spotDiameter = remember { screenWidth * 0.20f }
    val spotDiameterPx = with(density) { spotDiameter.toPx() }
    val spotRadiusPx = spotDiameterPx / 2
    val isTopThumbOutOfBounds = remember { mutableStateOf(false) }
    val isBottomThumbOutOfBounds = remember { mutableStateOf(false) }
    val isOutOfBounds = derivedStateOf {
        isTopThumbOutOfBounds.value || isBottomThumbOutOfBounds.value
    }
    val currentState = rememberUpdatedState(state)
    val currentKey = remember { mutableStateOf<String?>(null) }

    val topRotationDegrees = remember {
        derivedStateOf {
            val topCenter = Offset(
                topThumbPosition.value.x + spotRadiusPx,
                topThumbPosition.value.y + spotRadiusPx
            )
            val bottomCenter = Offset(
                bottomThumbPosition.value.x + spotRadiusPx,
                bottomThumbPosition.value.y + spotRadiusPx
            )
            val dx = bottomCenter.x - topCenter.x
            val dy = bottomCenter.y - topCenter.y
            if (dx == 0f && dy == 0f) 0f
            else (atan2(-dx.toDouble(), dy.toDouble()) * 180.0 / PI).toFloat()
        }
    }
    val bottomRotationDegrees = remember {
        derivedStateOf { topRotationDegrees.value + 180f }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        val tawaraBalesPainter = painterResource(resource = Res.drawable.tawara)
        val shikiriSenPainter = painterResource(resource = Res.drawable.shikiri_sen)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            circleCenter.value = Offset(canvasWidth / 2, canvasHeight / 2)
            circleDiameter.value = canvasWidth * 0.85f // 88% width of canvas.
            circleRadius.value = circleDiameter.value / 2
            val imageSize =
                Size(
                    width = circleDiameter.value + 125,
                    height = circleDiameter.value + 125
                )
            drawCircle( // Arena perimeter
                color = if (isOutOfBounds.value) {
//                    Color(0xFF8B0000) // For debugging
                    Color.Transparent
                } else {
//                    Color(0xFF8B0000) // For debugging
                    Color.Transparent
                },
                center = circleCenter.value,
                radius = circleRadius.value,
                style = Stroke(width = circleStroke)
            )
            with(tawaraBalesPainter) {
                withTransform({
                    // Calculate the top-left position based on the circle center and the image size
                    val topLeftX = circleCenter.value.x - (imageSize.width / 2)
                    val topLeftY = circleCenter.value.y - (imageSize.height / 2)
                    translate(topLeftX, topLeftY)
                }) {
                    draw(
                        size = imageSize,
                    )
                }
            }
            val shikiriSenWidth = circleDiameter.value * SHIKIRI_SEN_WIDTH_RATIO
            val shikiriSenHeight = shikiriSenWidth / SHIKIRI_SEN_ASPECT_RATIO
            val shikiriSenSeparation = circleDiameter.value * SHIKIRI_SEN_SEPARATION_RATIO
            val shikiriSenSize = Size(shikiriSenWidth, shikiriSenHeight)
            val topShikiriTopLeft = Offset(
                x = circleCenter.value.x - (shikiriSenWidth / 2),
                y = circleCenter.value.y - (shikiriSenSeparation / 2) - (shikiriSenHeight / 2)
            )
            val bottomShikiriTopLeft = Offset(
                x = circleCenter.value.x - (shikiriSenWidth / 2),
                y = circleCenter.value.y + (shikiriSenSeparation / 2) - (shikiriSenHeight / 2)
            )
            with(shikiriSenPainter) {
                withTransform({
                    translate(topShikiriTopLeft.x, topShikiriTopLeft.y)
                }) {
                    draw(size = shikiriSenSize)
                }
                withTransform({
                    translate(bottomShikiriTopLeft.x, bottomShikiriTopLeft.y)
                }) {
                    draw(size = shikiriSenSize)
                }
            }
        }
        // Top ThumbView
        ThumbView(
            thumbOffsetPosition = topThumbPosition.value,
            isOutOfBounds = isTopThumbOutOfBounds.value,
            updateThumbOffsetPosition = { dragAmount ->
                if (currentState.value.playState != PlayState.IN_PROGRESS) {
                    return@ThumbView
                }
                if (currentState.value.topPlayer.isLocked || currentState.value.topPlayer.isResetting) {
                    return@ThumbView
                }
                val newSpotPosition = Offset(
                    topThumbPosition.value.x + dragAmount.x,
                    topThumbPosition.value.y + dragAmount.y
                )
                if (isDamageDetected(currentState.value, isTopThumbOutOfBounds.value)) {
                    onDamageDetected(currentState.value.topPlayer)
                    return@ThumbView
                }
                if (isDamageDetected(currentState.value, isBottomThumbOutOfBounds.value)) {
                    onDamageDetected(currentState.value.bottomPlayer)
                    return@ThumbView
                }
                if (doThumbSpotsOverlap(
                        firstThumbCenter = newSpotPosition,
                        secondThumbCenter = bottomThumbPosition.value,
                        firstThumbRadiusPx = spotRadiusPx,
                        secondThumbRadiusPx = spotRadiusPx
                    )
                ) {
                    val pushVector = calculatePushVector(
                        movingSpotCenter = newSpotPosition,
                        stationarySpotCenter = bottomThumbPosition.value,
                        dragAmount = dragAmount
                    )
                    bottomThumbPosition.value += pushVector
                } else {
                    topThumbPosition.value = newSpotPosition
                }
                updateOutOfBoundsStates(
                    topThumbPosition = topThumbPosition.value,
                    bottomThumbPosition = bottomThumbPosition.value,
                    circleCenter = circleCenter.value,
                    circleRadius = circleRadius.value,
                    spotRadiusPx = spotRadiusPx,
                    isTopThumbOutOfBounds = isTopThumbOutOfBounds,
                    isBottomThumbOutOfBounds = isBottomThumbOutOfBounds
                )
            },
            spotForegroundColor = state.ui.topThumbView.foregroundColor,
            spotForegroundImage = state.ui.topThumbView.foregroundImage,
            onPressed = {
                onPressed(it, currentState.value.topPlayer)
            },
            onReleased = {
                if (shouldSkipOnReleased(
                        currentState.value,
                        currentKey.value,
                        currentState.value.topPlayer
                    )
                ) return@ThumbView
                onReleased(currentState.value.topPlayer)
            },
            spotDiameter = spotDiameter,
            rotationDegrees = topRotationDegrees.value
        )
        // Bottom ThumbView
        ThumbView(
            thumbOffsetPosition = bottomThumbPosition.value,
            isOutOfBounds = isBottomThumbOutOfBounds.value,
            updateThumbOffsetPosition = { dragAmount ->
                if (currentState.value.playState != PlayState.IN_PROGRESS) {
                    return@ThumbView
                }
                if (currentState.value.bottomPlayer.isLocked || currentState.value.bottomPlayer.isResetting) {
                    return@ThumbView
                }
                val newSpotPosition = Offset(
                    bottomThumbPosition.value.x + dragAmount.x,
                    bottomThumbPosition.value.y + dragAmount.y
                )
                if (isDamageDetected(currentState.value, isBottomThumbOutOfBounds.value)) {
                    onDamageDetected(currentState.value.bottomPlayer)
                    return@ThumbView
                }
                if (isDamageDetected(currentState.value, isTopThumbOutOfBounds.value)) {
                    onDamageDetected(currentState.value.topPlayer)
                    return@ThumbView
                }
                if (doThumbSpotsOverlap(
                        firstThumbCenter = newSpotPosition,
                        secondThumbCenter = topThumbPosition.value,
                        firstThumbRadiusPx = spotRadiusPx,
                        secondThumbRadiusPx = spotRadiusPx
                    )
                ) {
                    val pushVector = calculatePushVector(
                        movingSpotCenter = newSpotPosition,
                        stationarySpotCenter = topThumbPosition.value,
                        dragAmount = dragAmount
                    )
                    topThumbPosition.value += pushVector
                } else {
                    bottomThumbPosition.value = newSpotPosition
                }
                updateOutOfBoundsStates(
                    topThumbPosition = topThumbPosition.value,
                    bottomThumbPosition = bottomThumbPosition.value,
                    circleCenter = circleCenter.value,
                    circleRadius = circleRadius.value,
                    spotRadiusPx = spotRadiusPx,
                    isTopThumbOutOfBounds = isTopThumbOutOfBounds,
                    isBottomThumbOutOfBounds = isBottomThumbOutOfBounds

                )
            },
            spotForegroundColor = state.ui.bottomThumbView.foregroundColor,
            spotForegroundImage = state.ui.bottomThumbView.foregroundImage,
            onPressed = {
                onPressed(it, currentState.value.bottomPlayer)
            },
            onReleased = {
                if (shouldSkipOnReleased(
                        currentState.value,
                        currentKey.value,
                        currentState.value.bottomPlayer
                    )
                ) return@ThumbView
                onReleased(currentState.value.bottomPlayer)
            },
            spotDiameter = spotDiameter,
            rotationDegrees = bottomRotationDegrees.value
        )
        GameOverView(state = currentState.value)
    }

    LaunchedEffect(currentState.value.resettingKey) {
        if (currentState.value.resettingKey != currentKey.value) {
            currentKey.value = currentState.value.resettingKey
        }
    }

    LaunchedEffect(resetThumbPositions) {
        isTopThumbOutOfBounds.value = false
        topThumbPosition.value = Offset(
            circleCenter.value.x - spotRadiusPx,
            (circleCenter.value.y - spotRadiusPx) - spotDiameterPx
        )
        isBottomThumbOutOfBounds.value = false
        bottomThumbPosition.value = Offset(
            circleCenter.value.x - spotRadiusPx,
            (circleCenter.value.y - spotRadiusPx) + spotDiameterPx
        )
    }

    LaunchedEffect(Unit) {
        snapshotFlow { topThumbPosition.value }
            .distinctUntilChanged()
            .collect { newPosition ->
                val topPosition = Offset(
                    circleCenter.value.x - spotRadiusPx,
                    (circleCenter.value.y - spotRadiusPx) - spotDiameterPx
                )
                if (newPosition == topPosition) {
                    onIntent(GameIntent.ResetThumbsComplete)
                }
            }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { bottomThumbPosition.value }
            .distinctUntilChanged()
            .collect { newPosition ->
                val bottomPosition = Offset(
                    circleCenter.value.x - spotRadiusPx,
                    (circleCenter.value.y - spotRadiusPx) + spotDiameterPx
                )
                if (newPosition == bottomPosition) {
                    onIntent(GameIntent.ResetThumbsComplete)
                }
            }
    }
}

private fun shouldSkipOnReleased(
    state: GameState,
    currentKey: String?,
    player: Player
): Boolean {
    return state.resettingKey != currentKey || state.resettingKey.isNullOrEmpty() || player.isLocked
}

private fun isDamageDetected(
    state: GameState,
    isThumbOutOfBounds: Boolean
): Boolean {
    return isThumbOutOfBounds && state.gameOverResult == null
}

private fun updateOutOfBoundsStates(
    topThumbPosition: Offset,
    bottomThumbPosition: Offset,
    circleCenter: Offset,
    circleRadius: Float,
    spotRadiusPx: Float,
    isTopThumbOutOfBounds: MutableState<Boolean>,
    isBottomThumbOutOfBounds: MutableState<Boolean>
) {
    isTopThumbOutOfBounds.value = isOutOfBounds(
        spotPosition = topThumbPosition,
        circleCenter = circleCenter,
        circleRadius = circleRadius,
        spotRadiusPx = spotRadiusPx
    )
    isBottomThumbOutOfBounds.value = isOutOfBounds(
        spotPosition = bottomThumbPosition,
        circleCenter = circleCenter,
        circleRadius = circleRadius,
        spotRadiusPx = spotRadiusPx
    )
}

private const val SHIKIRI_SEN_ASPECT_RATIO = 749f / 65f
private const val SHIKIRI_SEN_WIDTH_RATIO = 0.2f
private const val SHIKIRI_SEN_SEPARATION_RATIO = 0.16f