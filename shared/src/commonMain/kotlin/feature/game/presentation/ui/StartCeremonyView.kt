package feature.game.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.gunbai_red
import kotlin.math.roundToInt

@Composable
fun StartCeremonyView(
    isVisible: Boolean,
    arenaCentre: Offset?,
    arenaRadius: Float?,
    rikishiRadius: Float?,
    modifier: Modifier = Modifier,
) {
    if (
        arenaCentre == null ||
        arenaRadius == null ||
        rikishiRadius == null
    ) {
        return
    }

    val density = LocalDensity.current
    val gunbaiSizePx = rikishiRadius * 2f * GUNBAI_SIZE_RIKISHI_DIAMETER_MULTIPLIER
    val gunbaiSize = with(density) { gunbaiSizePx.toDp() }
    val tawaraInsetPx = with(density) { GUNBAI_TAWARA_INSET.toPx() }
    val centredVertically = (arenaCentre.y - gunbaiSizePx / 2f).roundToInt()

    AnimatedVisibility(
        visible = isVisible,
        enter = EnterTransition.None,
        exit = fadeOut(
            animationSpec = tween(durationMillis = GUNBAI_FADE_OUT_DURATION_MILLIS),
        ),
    ) {
        val waveTransition = rememberInfiniteTransition(label = "Gunbai wave")
        val waveRotation = waveTransition.animateFloat(
            initialValue = -GUNBAI_WAVE_ANGLE_DEGREES,
            targetValue = GUNBAI_WAVE_ANGLE_DEGREES,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = GUNBAI_WAVE_DURATION_MILLIS,
                    easing = LinearEasing,
                ),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "Gunbai rotation",
        )

        Box(modifier = modifier.fillMaxSize()) {
            WavingGunbai(
                topLeft = IntOffset(
                    x = (arenaCentre.x - arenaRadius + tawaraInsetPx).roundToInt(),
                    y = centredVertically,
                ),
                imageSize = gunbaiSize,
                rotationDegrees = LEFT_GUNBAI_FACING_DEGREES + waveRotation.value,
            )
            WavingGunbai(
                topLeft = IntOffset(
                    x = (
                        arenaCentre.x + arenaRadius - tawaraInsetPx - gunbaiSizePx
                    ).roundToInt(),
                    y = centredVertically,
                ),
                imageSize = gunbaiSize,
                rotationDegrees = RIGHT_GUNBAI_FACING_DEGREES - waveRotation.value,
            )
        }
    }
}

@Composable
private fun BoxScope.WavingGunbai(
    topLeft: IntOffset,
    imageSize: Dp,
    rotationDegrees: Float,
) {
    Image(
        painter = painterResource(Res.drawable.gunbai_red),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.TopStart)
            .offset { topLeft }
            .size(imageSize)
            .rotate(rotationDegrees),
    )
}

private const val GUNBAI_WAVE_ANGLE_DEGREES = 13f
private const val GUNBAI_WAVE_DURATION_MILLIS = 500
private const val GUNBAI_FADE_OUT_DURATION_MILLIS = 1_000
private const val GUNBAI_SIZE_RIKISHI_DIAMETER_MULTIPLIER = 1.5f
private const val LEFT_GUNBAI_FACING_DEGREES = 90f
private const val RIGHT_GUNBAI_FACING_DEGREES = -90f
private val GUNBAI_TAWARA_INSET = 32.dp
