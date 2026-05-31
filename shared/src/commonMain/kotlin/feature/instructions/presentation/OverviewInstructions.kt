package feature.instructions.presentation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import app.theme.colorHomeLightGreen
import org.jetbrains.compose.resources.stringResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.instruct_general_damage
import sumo.shared.generated.resources.instruct_general_modes
import sumo.shared.generated.resources.instruct_general_objective
import sumo.shared.generated.resources.instruct_general_welcome

@Composable
fun OverviewInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_general_welcome))
            }
            append("\n\n")
            withStyle(
                style = SpanStyle(
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_general_objective))
            }
            append("\n\n")
            withStyle(
                style = SpanStyle(
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_general_modes))
            }
            append("\n\n")
            withStyle(
                style = SpanStyle(
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_general_damage))
            }
            append("\n")
        }
    )
}
