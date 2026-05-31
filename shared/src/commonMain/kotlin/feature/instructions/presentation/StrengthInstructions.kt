package feature.instructions.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import app.theme.colorHomeLightGreen
import org.jetbrains.compose.resources.stringResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.instruct_strength_description
import sumo.shared.generated.resources.instruct_strength_title

@Composable
fun StrengthInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_strength_title))
            }
            withStyle(
                style = SpanStyle(
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_strength_description))
            }
            append("\n")
        }
    )
}