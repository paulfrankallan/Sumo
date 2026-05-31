package feature.game.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.LocalScreen
import app.presentation.DrawableProgressIndicator
import app.theme.appRed
import app.theme.colorHomeLightGreen
import feature.common.model.GameType
import feature.game.presentation.GameState
import feature.game.presentation.model.Player
import feature.game.presentation.model.ThumbPrintState
import feature.stroop.StroopTestView
import feature.stroop.TextCase
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.thumb_locked
import sumo.shared.generated.resources.thumb_unlocked

@Composable
fun ThumbPrintActionButton(
    state: GameState,
    player: Player,
    modifier: Modifier = Modifier,
    onClick: (Player) -> Unit = {},
    thumbState: ThumbPrintState?,
) {
    val screenWidth = LocalScreen.current.width
    val aspectRatio by remember { derivedStateOf { 120f / 100f } }
    val desiredWidth = remember { screenWidth * 0.20f }
    val desiredHeight = remember { desiredWidth * aspectRatio }
    val text = derivedStateOf {
        if (thumbState?.textCase == TextCase.UPPER) {
            thumbState.semanticColour?.label?.uppercase() ?: ""
        } else {
            thumbState?.semanticColour?.label?.lowercase() ?: ""
        }
    }
    val textColor = derivedStateOf { thumbState?.physicalColour?.colour ?: Color.White }
    val dividerColor = derivedStateOf { thumbState?.keyColour?.colour ?: Color.Black }
    val drawableResource = derivedStateOf {
        if (player.isLocked) {
            Res.drawable.thumb_locked
        } else {
            Res.drawable.thumb_unlocked
        }
    }
    Column(
        modifier = modifier
    ) {
        if (state.gameType == GameType.FOCUS) {
            StroopTestView(
                text = text.value,
                textColor = textColor.value,
                backgroundColor = Color.Black,
                fontWeight = thumbState?.fontWeight,
                textCase = thumbState?.textCase,
                modifier = Modifier
                    .alpha(if (player.isLocked) 1f else 0f)
            )
        }
        DrawableProgressIndicator(
            progressPercent = player.health.toFloat(),
            drawableResource = drawableResource.value,
            backgroundColor = appRed,

            foregroundColor = colorHomeLightGreen,
            onClick = { onClick(player) },
            modifier = Modifier
                .align(CenterHorizontally)
                .padding(bottom = 2.dp)
                .width(desiredWidth)
                .height(desiredHeight)
        )
        if (state.gameType == GameType.FOCUS) {
            Divider(
                color = dividerColor.value,
                thickness = 5.dp,
                modifier = Modifier
                    .align(CenterHorizontally)
                    .fillMaxWidth(0.2f)
                    .alpha(if (player.isLocked) 1f else 0f)
                    .padding(vertical = 2.dp)
            )
        }
    }
}