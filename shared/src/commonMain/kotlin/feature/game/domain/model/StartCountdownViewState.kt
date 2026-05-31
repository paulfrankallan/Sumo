package feature.game.domain.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit

data class StartCountdownViewState(
    val text: String,
    val textSize: TextUnit,
    val textColor: Color,
)