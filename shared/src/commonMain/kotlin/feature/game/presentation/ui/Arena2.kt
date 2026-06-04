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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
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
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.b26
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun Arena2(
    state: GameState,
    modifier: Modifier = Modifier,
    onDamageDetected: (Player) -> Unit,
    onPressed: (Boolean, Player) -> Unit,
    onReleased: (player: Player) -> Unit,
    onIntent: (Intent) -> Unit,
    resetThumbPositions: Boolean
) {
    val density = LocalDensity.current

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

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = min(size.width, size.height)

            circleCenter.value = Offset(size.width / 2f, size.height / 2f)

            // This is the IMPORTANT value:
            // the collision radius and the visual OUTER edge both use this exact radius.
            val outerRingRadius = canvasSize * 0.44f
            val baleWidth = canvasSize * 0.055f
            val baleCenterRadius = outerRingRadius - (baleWidth / 2f)

            circleRadius.value = outerRingRadius
            circleDiameter.value = outerRingRadius * 2f

            drawDohyoBales(
                center = circleCenter.value,
                outerRadius = outerRingRadius,
                baleWidth = baleWidth,
                isAlert = isOutOfBounds.value || state.isGameOver
            )
        }

        ThumbView(
            thumbOffsetPosition = topThumbPosition.value,
            isOutOfBounds = isTopThumbOutOfBounds.value,
            updateThumbOffsetPosition = { dragAmount ->
                if (currentState.value.playState != PlayState.IN_PROGRESS) return@ThumbView
                if (currentState.value.topPlayer.isLocked || currentState.value.topPlayer.isResetting) return@ThumbView

                val newSpotPosition = topThumbPosition.value + dragAmount

                if (isDamageDetected(currentState.value, isTopThumbOutOfBounds.value)) {
                    onDamageDetected(currentState.value.topPlayer)
                    return@ThumbView
                }

                if (isDamageDetected(currentState.value, isBottomThumbOutOfBounds.value)) {
                    onDamageDetected(currentState.value.bottomPlayer)
                    return@ThumbView
                }

                if (
                    doThumbSpotsOverlap(
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
            onPressed = { onPressed(it, currentState.value.topPlayer) },
            onReleased = {
                if (
                    shouldSkipOnReleased(
                        currentState.value,
                        currentKey.value,
                        currentState.value.topPlayer
                    )
                ) return@ThumbView

                onReleased(currentState.value.topPlayer)
            },
            spotDiameter = spotDiameter,
            doRotate = true
        )

        ThumbView(
            thumbOffsetPosition = bottomThumbPosition.value,
            isOutOfBounds = isBottomThumbOutOfBounds.value,
            spotBackgroundImage = Res.drawable.b26,
            updateThumbOffsetPosition = { dragAmount ->
                if (currentState.value.playState != PlayState.IN_PROGRESS) return@ThumbView
                if (currentState.value.bottomPlayer.isLocked || currentState.value.bottomPlayer.isResetting) return@ThumbView

                val newSpotPosition = bottomThumbPosition.value + dragAmount

                if (isDamageDetected(currentState.value, isBottomThumbOutOfBounds.value)) {
                    onDamageDetected(currentState.value.bottomPlayer)
                    return@ThumbView
                }

                if (isDamageDetected(currentState.value, isTopThumbOutOfBounds.value)) {
                    onDamageDetected(currentState.value.topPlayer)
                    return@ThumbView
                }

                if (
                    doThumbSpotsOverlap(
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
            onPressed = { onPressed(it, currentState.value.bottomPlayer) },
            onReleased = {
                if (
                    shouldSkipOnReleased(
                        currentState.value,
                        currentKey.value,
                        currentState.value.bottomPlayer
                    )
                ) return@ThumbView

                onReleased(currentState.value.bottomPlayer)
            },
            spotDiameter = spotDiameter
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

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawDohyoBales(
    center: Offset,
    outerRadius: Float,
    baleWidth: Float,
    isAlert: Boolean
) {
    val baleColor = if (isAlert) Color(0xFF8B0000) else Color(0xFFD8B46A)
    val baleDark = if (isAlert) Color(0xFF4F0000) else Color(0xFF7A6138)
    val baleLight = if (isAlert) Color(0xFFB43A3A) else Color(0xFFF3D98E)

    val baleCenterRadius = outerRadius - (baleWidth / 2f)
    val innerRadius = outerRadius - baleWidth

    drawCircle(
        color = baleColor,
        center = center,
        radius = baleCenterRadius,
        style = Stroke(
            width = baleWidth,
            cap = StrokeCap.Round
        )
    )

    drawCircle(
        color = baleDark.copy(alpha = 0.7f),
        center = center,
        radius = outerRadius,
        style = Stroke(width = 2f)
    )

    drawCircle(
        color = baleDark.copy(alpha = 0.35f),
        center = center,
        radius = innerRadius,
        style = Stroke(width = 2f)
    )

    val segmentCount = 56
    repeat(segmentCount) { index ->
        val angle = (index.toFloat() / segmentCount.toFloat()) * 360f
        val radians = angle.toRadians()

        val start = Offset(
            x = center.x + cos(radians) * innerRadius,
            y = center.y + sin(radians) * innerRadius
        )

        val end = Offset(
            x = center.x + cos(radians) * outerRadius,
            y = center.y + sin(radians) * outerRadius
        )

        drawLine(
            color = baleDark.copy(alpha = 0.65f),
            start = start,
            end = end,
            strokeWidth = 2f
        )
    }

    // Light straw strands around the ring.
    repeat(96) { index ->
        val angle = (index.toFloat() / 96f) * 360f
        val radians = angle.toRadians()
        val radius = baleCenterRadius + if (index % 2 == 0) baleWidth * 0.18f else -baleWidth * 0.18f

        val start = Offset(
            x = center.x + cos(radians) * radius,
            y = center.y + sin(radians) * radius
        )

        val endRadians = (angle + 1.8f).toRadians()
        val end = Offset(
            x = center.x + cos(endRadians) * radius,
            y = center.y + sin(endRadians) * radius
        )

        drawLine(
            color = baleLight.copy(alpha = 0.45f),
            start = start,
            end = end,
            strokeWidth = 1.4f
        )
    }

    drawTokudawara(
        center = center,
        outerRadius = outerRadius,
        baleWidth = baleWidth,
        angleDegrees = 0f,
        baleColor = baleColor,
        baleDark = baleDark,
        baleLight = baleLight
    )

    drawTokudawara(
        center = center,
        outerRadius = outerRadius,
        baleWidth = baleWidth,
        angleDegrees = 90f,
        baleColor = baleColor,
        baleDark = baleDark,
        baleLight = baleLight
    )

    drawTokudawara(
        center = center,
        outerRadius = outerRadius,
        baleWidth = baleWidth,
        angleDegrees = 180f,
        baleColor = baleColor,
        baleDark = baleDark,
        baleLight = baleLight
    )

    drawTokudawara(
        center = center,
        outerRadius = outerRadius,
        baleWidth = baleWidth,
        angleDegrees = 270f,
        baleColor = baleColor,
        baleDark = baleDark,
        baleLight = baleLight
    )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawTokudawara(
    center: Offset,
    outerRadius: Float,
    baleWidth: Float,
    angleDegrees: Float,
    baleColor: Color,
    baleDark: Color,
    baleLight: Color
) {
    val length = baleWidth * 2.15f
    val height = baleWidth * 1.05f

    val topLeft = Offset(
        x = center.x - (length / 2f),
        y = center.y - outerRadius
    )

    rotate(
        degrees = angleDegrees,
        pivot = center
    ) {
        drawRoundRect(
            color = baleColor,
            topLeft = topLeft,
            size = Size(length, height),
            cornerRadius = CornerRadius(height / 2f, height / 2f)
        )

        drawRoundRect(
            color = baleDark.copy(alpha = 0.6f),
            topLeft = topLeft,
            size = Size(length, height),
            cornerRadius = CornerRadius(height / 2f, height / 2f),
            style = Stroke(width = 2f)
        )

        val bandCount = 4
        repeat(bandCount) { index ->
            val x = topLeft.x + ((index + 1) * length / (bandCount + 1))

            drawLine(
                color = baleDark.copy(alpha = 0.65f),
                start = Offset(x, topLeft.y + height * 0.12f),
                end = Offset(x, topLeft.y + height * 0.88f),
                strokeWidth = 2f
            )
        }

        drawLine(
            color = baleLight.copy(alpha = 0.5f),
            start = Offset(topLeft.x + length * 0.12f, topLeft.y + height * 0.35f),
            end = Offset(topLeft.x + length * 0.88f, topLeft.y + height * 0.35f),
            strokeWidth = 1.5f
        )
    }
}

private fun Float.toRadians(): Float {
    return (this * PI / 180.0).toFloat()
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