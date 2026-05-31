package feature.game.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import feature.common.model.GameType
import feature.game.presentation.GameIntent
import feature.game.presentation.GameState
import feature.game.presentation.model.Player
import feature.game.presentation.model.ThumbPrintActionPanelState

@Composable
fun ThumbPrintActionPanel(
    state: GameState,
    player: Player,
    thumbPrintActionPanelState: ThumbPrintActionPanelState?,
    onIntent: (GameIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.SpaceAround,
        modifier = modifier
            .fillMaxWidth()
    ) {
        if (state.gameType == GameType.FOCUS) {
            val thumbPrintState1 = thumbPrintActionPanelState?.thumbPrintStates?.get(0)
            ThumbPrintActionButton(
                state = state,
                player = player,
                modifier = Modifier.weight(1f),
                onClick = { player ->
                    onIntent(
                        GameIntent.ThumbActionButtonClicked(
                            player,
                            thumbPrintState1?.isKey
                        )
                    )
                },
                thumbState = thumbPrintState1,
            )
        }
        val thumbPrintState2 = if (state.gameType == GameType.FOCUS) {
            thumbPrintActionPanelState?.thumbPrintStates?.get(1)
        } else {
            null
        }
        ThumbPrintActionButton(
            state = state,
            player = player,
            modifier = Modifier.weight(1f),
            onClick = { player ->
                onIntent(
                    GameIntent.ThumbActionButtonClicked(
                        player,
                        thumbPrintState2?.isKey
                    )
                )
            },
            thumbState = thumbPrintState2,
        )
        if (state.gameType == GameType.FOCUS) {
            val thumbPrintState3 = thumbPrintActionPanelState?.thumbPrintStates?.get(2)
            ThumbPrintActionButton(
                state = state,
                player = player,
                modifier = Modifier.weight(1f),
                onClick = { player ->
                    onIntent(
                        GameIntent.ThumbActionButtonClicked(
                            player,
                            thumbPrintState3?.isKey
                        )
                    )
                },
                thumbState = thumbPrintState3,
            )
        }
    }
}