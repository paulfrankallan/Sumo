package feature.game.presentation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import feature.common.model.Position
import feature.game.domain.model.GameOverResult
import feature.game.presentation.GameCeremonyTimings
import feature.game.presentation.GameOverAnimationPhase
import feature.game.presentation.GameState
import feature.game.presentation.isActive
import feature.game.presentation.isPointingGunbai
import feature.game.presentation.showsGunbai
import feature.game.presentation.showsResults
import feature.game.presentation.model.Player
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.game_over_grey_scrim
import sumo.shared.generated.resources.game_over_scrim_2
import sumo.shared.generated.resources.gunbai_blue
import sumo.shared.generated.resources.gunbai_red
import sumo.shared.generated.resources.winner_beams
import sumo.shared.generated.resources.winner_beams_2
import sumo.shared.generated.resources.winner_gold_confetti
import sumo.shared.generated.resources.winner_gold_confetti_2
import kotlin.math.sqrt

@Composable
internal fun GameOverBackgroundEffects(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    val phase = state.gameOverAnimationPhase
    if (!phase.isActive) return

    val winnerPosition = state.winnerPosition() ?: return
    val winnerRikishiPosition = state.rikishiPositionFor(winnerPosition) ?: return
    val scrimVisible = remember { MutableTransitionState(false) }
    LaunchedEffect(phase) {
        scrimVisible.targetState = phase != GameOverAnimationPhase.RETURNING_TO_READY
    }
    val beamAlpha by animateFloatAsState(
        targetValue = if (phase.showsResults) 1f else 0f,
        animationSpec = tween(
            durationMillis = GameCeremonyTimings.GAME_OVER_EFFECT_FADE_DURATION_MILLIS,
        ),
        label = "Winner beams alpha",
    )

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(
            visibleState = scrimVisible,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = GameCeremonyTimings.GAME_OVER_DECISIVE_MOMENT_DURATION_MILLIS
                        .toInt(),
                ),
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = GameCeremonyTimings.GAME_OVER_RETURN_FADE_DURATION_MILLIS,
                ),
            ),
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(gameOverEffectAssets.scrim),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        WinnerBeams(
            winnerCentre = winnerRikishiPosition,
            effectAlpha = beamAlpha,
        )
    }
}

@Composable
fun GameOverView(
    state: GameState,
    modifier: Modifier = Modifier,
) {
    val phase = state.gameOverAnimationPhase
    if (!phase.isActive) return

    val arenaCentre = state.arenaCentre ?: return
    val arenaRadius = state.arenaRadius ?: return
    val rikishiRadius = state.rikishiRadius ?: return
    val winnerPosition = state.winnerPosition() ?: return
    val winningTarget = remember(state.gameId, winnerPosition) {
        state.winningGunbaiTarget(
            winnerPosition = winnerPosition,
            arenaCentre = arenaCentre,
            arenaRadius = arenaRadius,
            rikishiRadius = rikishiRadius,
        )
    }
    val winningGunbai = when (winnerPosition) {
        Position.TOP -> Res.drawable.gunbai_blue
        Position.BOTTOM -> Res.drawable.gunbai_red
    }
    val effectAlpha by animateFloatAsState(
        targetValue = if (phase.showsResults) 1f else 0f,
        animationSpec = tween(
            durationMillis = GameCeremonyTimings.GAME_OVER_EFFECT_FADE_DURATION_MILLIS,
        ),
        label = "Game over effect alpha",
    )

    Box(modifier = modifier.fillMaxSize()) {
        WinnerConfetti(
            alignment = winnerPosition.effectAlignment(),
            effectAlpha = effectAlpha,
        )
        AnimatedVisibility(
            visible = phase.showsGunbai,
            enter = fadeIn(
                animationSpec = tween(
                    durationMillis = GameCeremonyTimings.GAME_OVER_GUNBAI_ENTRANCE_DURATION_MILLIS,
                ),
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(
                    durationMillis = GameCeremonyTimings.GAME_OVER_GUNBAI_ENTRANCE_DURATION_MILLIS,
                ),
            ),
            exit = fadeOut(
                animationSpec = tween(
                    durationMillis = GameCeremonyTimings.GAME_OVER_RETURN_FADE_DURATION_MILLIS,
                ),
            ) + scaleOut(
                targetScale = 0.96f,
                animationSpec = tween(
                    durationMillis = GameCeremonyTimings.GAME_OVER_RETURN_FADE_DURATION_MILLIS,
                ),
            ),
        ) {
            GunbaiPair(
                arenaCentre = arenaCentre,
                arenaRadius = arenaRadius,
                rikishiRadius = rikishiRadius,
                leftGunbai = winningGunbai,
                rightGunbai = winningGunbai,
                leftTarget = winningTarget,
                rightTarget = winningTarget,
                isPointing = phase.isPointingGunbai,
                showBackdrop = true,
            )
        }
    }
}

@Composable
private fun BoxScope.WinnerBeams(
    winnerCentre: Offset,
    effectAlpha: Float,
) {
    Image(
        painter = painterResource(gameOverEffectAssets.winnerBeams),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                alpha = effectAlpha
                scaleX = WINNER_BEAM_SCALE
                scaleY = WINNER_BEAM_SCALE
                translationX = winnerCentre.x - size.width / 2f
                translationY = winnerCentre.y - size.height / 2f
            },
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun BoxScope.WinnerConfetti(
    alignment: Alignment,
    effectAlpha: Float,
) {
    Image(
        painter = painterResource(gameOverEffectAssets.winnerConfetti),
        contentDescription = null,
        modifier = Modifier
            .align(alignment)
            .fillMaxWidth()
            .fillMaxHeight(WINNER_EFFECT_HEIGHT_FRACTION)
            .graphicsLayer { alpha = effectAlpha },
        contentScale = ContentScale.Crop,
    )
}

private fun GameState.winnerPosition(): Position? = when (gameOverResult?.winner?.id) {
    topPlayer.id -> Position.TOP
    bottomPlayer.id -> Position.BOTTOM
    else -> null
}

private fun GameState.rikishiPositionFor(position: Position): Offset? = when (position) {
    Position.TOP -> topRikishiPosition
    Position.BOTTOM -> bottomRikishiPosition
}

private fun Position.effectAlignment(): Alignment = when (this) {
    Position.TOP -> Alignment.TopCenter
    Position.BOTTOM -> Alignment.BottomCenter
}

private fun GameState.winningGunbaiTarget(
    winnerPosition: Position,
    arenaCentre: Offset,
    arenaRadius: Float,
    rikishiRadius: Float,
): Offset {
    val winningRikishiPosition = when (winnerPosition) {
        Position.TOP -> topRikishiPosition
        Position.BOTTOM -> bottomRikishiPosition
    } ?: shikiriSenRikishiPosition(
        arenaCentre = arenaCentre,
        arenaRadius = arenaRadius,
        rikishiRadius = rikishiRadius,
        position = winnerPosition,
    )

    return winningRikishiPosition.clampWithinArena(
        arenaCentre = arenaCentre,
        maximumDistance = (arenaRadius - rikishiRadius).coerceAtLeast(0f),
    )
}

private fun Offset.clampWithinArena(
    arenaCentre: Offset,
    maximumDistance: Float,
): Offset {
    val horizontalDistance = x - arenaCentre.x
    val verticalDistance = y - arenaCentre.y
    val distance = sqrt(
        horizontalDistance * horizontalDistance + verticalDistance * verticalDistance,
    )
    if (distance == 0f || distance <= maximumDistance) return this

    return Offset(
        x = arenaCentre.x + horizontalDistance / distance * maximumDistance,
        y = arenaCentre.y + verticalDistance / distance * maximumDistance,
    )
}

private data class GameOverEffectAssets(
    val scrim: DrawableResource,
    val winnerBeams: DrawableResource,
    val winnerConfetti: DrawableResource,
)

private val gameOverEffectAssets = GameOverEffectAssets(
    scrim = GameOverEffectVariant.DEFAULT.select(
        default = Res.drawable.game_over_grey_scrim,
        alternative = Res.drawable.game_over_scrim_2,
    ),
    winnerBeams = GameOverEffectVariant.DEFAULT.select(
        default = Res.drawable.winner_beams,
        alternative = Res.drawable.winner_beams_2,
    ),
    winnerConfetti = GameOverEffectVariant.DEFAULT.select(
        default = Res.drawable.winner_gold_confetti,
        alternative = Res.drawable.winner_gold_confetti_2,
    ),
)

private enum class GameOverEffectVariant {
    DEFAULT,
    ALTERNATIVE,
}

private fun GameOverEffectVariant.select(
    default: DrawableResource,
    alternative: DrawableResource,
): DrawableResource = when (this) {
    GameOverEffectVariant.DEFAULT -> default
    GameOverEffectVariant.ALTERNATIVE -> alternative
}

@Preview(name = "Red victory")
@Composable
private fun RedVictoryPreview() {
    GameOverPreviewContent(winnerPosition = Position.BOTTOM)
}

@Preview(name = "Blue victory")
@Composable
private fun BlueVictoryPreview() {
    GameOverPreviewContent(winnerPosition = Position.TOP)
}

@Composable
private fun GameOverPreviewContent(
    winnerPosition: Position,
) {
    val previewSize = 400.dp
    val previewSizePx = with(LocalDensity.current) { previewSize.toPx() }
    val arenaCentre = Offset(previewSizePx / 2f, previewSizePx / 2f)
    val arenaRadius = previewSizePx * 0.425f
    val rikishiRadius = previewSizePx * 0.08f
    val topPlayer = Player(id = "top", position = Position.TOP)
    val bottomPlayer = Player(id = "bottom", position = Position.BOTTOM)
    val winner = if (winnerPosition == Position.TOP) topPlayer else bottomPlayer

    Box(modifier = Modifier.size(previewSize)) {
        GameOverView(
            state = GameState(
                gameId = "preview",
                topPlayer = topPlayer,
                bottomPlayer = bottomPlayer,
                gameOverResult = GameOverResult(winner = winner),
                gameOverAnimationPhase = GameOverAnimationPhase.SETTLING,
                topRikishiPosition = shikiriSenRikishiPosition(
                    arenaCentre = arenaCentre,
                    arenaRadius = arenaRadius,
                    rikishiRadius = rikishiRadius,
                    position = Position.TOP,
                ),
                bottomRikishiPosition = shikiriSenRikishiPosition(
                    arenaCentre = arenaCentre,
                    arenaRadius = arenaRadius,
                    rikishiRadius = rikishiRadius,
                    position = Position.BOTTOM,
                ),
                arenaCentre = arenaCentre,
                arenaRadius = arenaRadius,
                rikishiRadius = rikishiRadius,
            ),
        )
    }
}

private const val WINNER_BEAM_SCALE = 1.78f
private const val WINNER_EFFECT_HEIGHT_FRACTION = 0.7f
