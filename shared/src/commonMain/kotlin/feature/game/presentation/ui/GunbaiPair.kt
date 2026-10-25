package feature.game.presentation.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import feature.common.model.Position
import feature.game.presentation.GameCeremonyTimings
import feature.game.presentation.SHIKIRI_SEN_CENTER_OFFSET_RATIO
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.roundToInt

@Composable
internal fun BoxScope.GunbaiPair(
    arenaCentre: Offset,
    arenaRadius: Float,
    rikishiRadius: Float,
    leftGunbai: DrawableResource,
    rightGunbai: DrawableResource,
    leftTarget: Offset,
    rightTarget: Offset,
    isPointing: Boolean,
    showBackdrop: Boolean = false,
) {
    val density = LocalDensity.current
    val gunbaiSizePx = rikishiRadius * GUNBAI_SIZE_RIKISHI_DIAMETER_MULTIPLIER
    val gunbaiSize = with(density) { gunbaiSizePx.toDp() }
    val tawaraInsetPx = with(density) { GUNBAI_TAWARA_INSET.toPx() }
    val leftTopLeft = Offset(
        x = arenaCentre.x - arenaRadius + tawaraInsetPx,
        y = arenaCentre.y - gunbaiSizePx / 2f,
    )
    val rightTopLeft = Offset(
        x = arenaCentre.x + arenaRadius - tawaraInsetPx - gunbaiSizePx,
        y = arenaCentre.y - gunbaiSizePx / 2f,
    )
    val waveTransition = rememberInfiniteTransition(label = "Gunbai wave")
    val waveRotation by waveTransition.animateFloat(
        initialValue = -GUNBAI_WAVE_ANGLE_DEGREES,
        targetValue = GUNBAI_WAVE_ANGLE_DEGREES,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = GameCeremonyTimings.GUNBAI_WAVE_DURATION_MILLIS,
                easing = LinearEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Gunbai wave rotation",
    )
    val pointingProgress by animateFloatAsState(
        targetValue = if (isPointing) 1f else 0f,
        animationSpec = tween(
            durationMillis = GameCeremonyTimings.GUNBAI_POINTING_DURATION_MILLIS,
            easing = FastOutSlowInEasing,
        ),
        label = "Gunbai pointing progress",
    )

    if (showBackdrop) {
        GunbaiBackdrop(
            topLeft = leftTopLeft,
            imageSizePx = gunbaiSizePx,
        )
        GunbaiBackdrop(
            topLeft = rightTopLeft,
            imageSizePx = gunbaiSizePx,
        )
    }

    Gunbai(
        resource = leftGunbai,
        topLeft = leftTopLeft,
        imageSize = gunbaiSize,
        rotationDegrees = interpolateRotation(
            from = LEFT_GUNBAI_START_ROTATION_DEGREES + waveRotation,
            to = gunbaiRotationTowards(
                pivot = gunbaiPivot(leftTopLeft, gunbaiSizePx),
                target = leftTarget,
            ),
            progress = pointingProgress,
        ),
    )
    Gunbai(
        resource = rightGunbai,
        topLeft = rightTopLeft,
        imageSize = gunbaiSize,
        rotationDegrees = interpolateRotation(
            from = RIGHT_GUNBAI_START_ROTATION_DEGREES - waveRotation,
            to = gunbaiRotationTowards(
                pivot = gunbaiPivot(rightTopLeft, gunbaiSizePx),
                target = rightTarget,
            ),
            progress = pointingProgress,
        ),
    )
}

internal fun shikiriSenRikishiPosition(
    arenaCentre: Offset,
    arenaRadius: Float,
    rikishiRadius: Float,
    position: Position,
): Offset {
    val offset = Offset(
        x = 0f,
        y = arenaRadius * SHIKIRI_SEN_CENTER_OFFSET_RATIO + rikishiRadius,
    )
    return when (position) {
        Position.TOP -> arenaCentre - offset
        Position.BOTTOM -> arenaCentre + offset
    }
}

@Composable
private fun BoxScope.GunbaiBackdrop(
    topLeft: Offset,
    imageSizePx: Float,
) {
    val density = LocalDensity.current
    val backdropSizePx = imageSizePx * GUNBAI_BACKDROP_SIZE_MULTIPLIER
    val backdropSize = with(density) { backdropSizePx.toDp() }
    val backdropTopLeft = topLeft - Offset(
        x = (backdropSizePx - imageSizePx) / 2f,
        y = (backdropSizePx - imageSizePx) / 2f,
    )

    Canvas(
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset {
                IntOffset(
                    x = backdropTopLeft.x.roundToInt(),
                    y = backdropTopLeft.y.roundToInt(),
                )
            }
            .size(backdropSize),
    ) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Black.copy(alpha = GUNBAI_BACKDROP_CENTRE_ALPHA),
                    Color.Black.copy(alpha = GUNBAI_BACKDROP_EDGE_ALPHA),
                    Color.Transparent,
                ),
                center = center,
                radius = size.minDimension / 2f,
            ),
        )
    }
}

@Composable
private fun BoxScope.Gunbai(
    resource: DrawableResource,
    topLeft: Offset,
    imageSize: Dp,
    rotationDegrees: Float,
) {
    Image(
        painter = painterResource(resource),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset {
                IntOffset(
                    x = topLeft.x.roundToInt(),
                    y = topLeft.y.roundToInt(),
                )
            }
            .size(imageSize)
            .graphicsLayer {
                transformOrigin = TransformOrigin(
                    pivotFractionX = GUNBAI_PIVOT_X,
                    pivotFractionY = GUNBAI_PIVOT_Y,
                )
                rotationZ = rotationDegrees
            },
    )
}

private fun gunbaiPivot(
    topLeft: Offset,
    imageSizePx: Float,
): Offset = topLeft + Offset(
    x = imageSizePx * GUNBAI_PIVOT_X,
    y = imageSizePx * GUNBAI_PIVOT_Y,
)

private fun gunbaiRotationTowards(
    pivot: Offset,
    target: Offset,
): Float {
    val horizontalDistance = target.x - pivot.x
    val verticalDistance = target.y - pivot.y
    if (horizontalDistance == 0f && verticalDistance == 0f) return 0f

    return (
        atan2(
            horizontalDistance.toDouble(),
            -verticalDistance.toDouble(),
        ) * 180.0 / PI
        ).toFloat()
}

private fun interpolateRotation(
    from: Float,
    to: Float,
    progress: Float,
): Float = from + shortestRotationDelta(from, to) * progress

private fun shortestRotationDelta(
    from: Float,
    to: Float,
): Float {
    var delta = (to - from) % 360f
    if (delta > 180f) delta -= 360f
    if (delta < -180f) delta += 360f
    return delta
}

private const val GUNBAI_SIZE_RIKISHI_DIAMETER_MULTIPLIER = 2.34375f
private const val GUNBAI_WAVE_ANGLE_DEGREES = 13f
private const val LEFT_GUNBAI_START_ROTATION_DEGREES = 90f
private const val RIGHT_GUNBAI_START_ROTATION_DEGREES = -90f
private const val GUNBAI_PIVOT_X = 0.5f
private const val GUNBAI_PIVOT_Y = 0.68f
private const val GUNBAI_BACKDROP_SIZE_MULTIPLIER = 1.3f
private const val GUNBAI_BACKDROP_CENTRE_ALPHA = 0.7f
private const val GUNBAI_BACKDROP_EDGE_ALPHA = 0.35f
private val GUNBAI_TAWARA_INSET = 32.dp
