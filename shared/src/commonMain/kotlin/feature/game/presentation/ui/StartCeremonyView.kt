package feature.game.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import feature.common.model.Position
import feature.game.presentation.GameCeremonyTimings
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.gunbai_blue
import sumo.shared.generated.resources.gunbai_red
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

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

    val isPointing = remember { mutableStateOf(false) }
    LaunchedEffect(isVisible) {
        if (isVisible) {
            isPointing.value = false
            delay(GameCeremonyTimings.START_GUNBAI_POINTING_DELAY_MILLIS.milliseconds)
            isPointing.value = true
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = EnterTransition.None,
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = GameCeremonyTimings.GUNBAI_FADE_OUT_DURATION_MILLIS,
            ),
        ),
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            GunbaiPair(
                arenaCentre = arenaCentre,
                arenaRadius = arenaRadius,
                rikishiRadius = rikishiRadius,
                leftGunbai = Res.drawable.gunbai_red,
                rightGunbai = Res.drawable.gunbai_blue,
                leftTarget = shikiriSenRikishiPosition(
                    arenaCentre = arenaCentre,
                    arenaRadius = arenaRadius,
                    rikishiRadius = rikishiRadius,
                    position = Position.BOTTOM,
                ),
                rightTarget = shikiriSenRikishiPosition(
                    arenaCentre = arenaCentre,
                    arenaRadius = arenaRadius,
                    rikishiRadius = rikishiRadius,
                    position = Position.TOP,
                ),
                isPointing = isPointing.value,
            )
        }
    }
}
