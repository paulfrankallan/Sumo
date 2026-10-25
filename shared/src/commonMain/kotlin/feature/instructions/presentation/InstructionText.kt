package feature.instructions.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import app.theme.colorHomeLightGreen

@Composable
internal fun InstructionText(
    text: String,
) {
    Text(
        text = text,
        color = colorHomeLightGreen,
    )
}
