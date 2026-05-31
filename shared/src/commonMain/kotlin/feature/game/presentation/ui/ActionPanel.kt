package feature.game.presentation.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theme.appGold
import app.theme.appRed
import app.theme.colorHomeLightGreen
import feature.game.presentation.ActionPanelType
import feature.game.presentation.GameIntent
import feature.game.presentation.GameState
import feature.game.presentation.model.Player
import feature.game.presentation.model.ThumbPrintActionPanelState
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.fight
import sumo.shared.generated.resources.major

@Composable
fun ActionPanel(
    state: GameState,
    actionPanelType: ActionPanelType,
    actionPanelState: ThumbPrintActionPanelState?,
    player: Player,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    when (actionPanelType) {
        ActionPanelType.START_GAME_UI -> {
            GameButton(onIntent = onIntent, modifier = modifier)
        }

        ActionPanelType.THUMB_PRINT_UI -> {
            ThumbPrintActionPanel(
                state = state,
                player = player,
                thumbPrintActionPanelState = actionPanelState,
                onIntent = onIntent,
                modifier = modifier
            )
        }

        ActionPanelType.GAME_OVER_UI -> {
            GameButton(onIntent = onIntent, modifier = modifier)
        }

        ActionPanelType.NONE -> {}
    }
}

@Composable
fun GameButton(
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val font = FontFamily(Font(Res.font.major))
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = {
                onIntent(GameIntent.StartGame)
            },
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = appRed,
                contentColor = colorHomeLightGreen
            ),
            border = BorderStroke(2.dp, appGold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(resource = Res.string.fight),
                fontSize = 48.sp,
                fontFamily = font,
            )
        }
    }
}