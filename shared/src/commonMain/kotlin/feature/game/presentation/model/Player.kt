package feature.game.presentation.model

import androidx.compose.ui.text.font.FontWeight
import feature.common.model.Position
import feature.game.presentation.ThumbState
import feature.game.presentation.ThumbState.RELEASED
import feature.stroop.StimuliColour
import feature.stroop.StroopTest
import feature.stroop.TextCase

data class Player(
    val id: String,
    val position: Position?,
    val thumbState: ThumbState = RELEASED,
    val health: Int = 100,
    val isLocked: Boolean = false,
    val isResetting: Boolean = false,
    val thumbPrintActionPanelState: ThumbPrintActionPanelState? = null,
) {
    fun getThumbPrintSateByKey(): ThumbPrintState? {
        return thumbPrintActionPanelState?.thumbPrintStates?.firstOrNull { it.isKey }
    }
}

data class ThumbPrintState(
    val isKey: Boolean,
    val stroopTest: StroopTest,
    val keyColour: StimuliColour? = null,
    val physicalColour: StimuliColour? = null,
    val semanticColour: StimuliColour? = null,
    val textCase: TextCase = TextCase.LOWER,
    val fontWeight: FontWeight = FontWeight.Normal,
)

data class ThumbPrintActionPanelState(
    val thumbPrintStates: List<ThumbPrintState> = emptyList(),
)