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
import sumo.shared.generated.resources.instruct_focus_description
import sumo.shared.generated.resources.instruct_focus_stroop_1
import sumo.shared.generated.resources.instruct_focus_stroop_2
import sumo.shared.generated.resources.instruct_focus_stroop_intro
import sumo.shared.generated.resources.instruct_focus_stroop_outro
import sumo.shared.generated.resources.instruct_focus_title

@Composable
fun FocusInstructions() {
    Text(
        buildAnnotatedString {
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_focus_title))
            }
            withStyle(
                style = SpanStyle(
                    color = colorHomeLightGreen
                )
            ) {
                append("${stringResource(Res.string.instruct_focus_description)}\n\n")
            }
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_focus_stroop_intro))
            }
            append("\n")
            append("\n")
            withStyle(
                style = SpanStyle(
                    color = colorHomeLightGreen
                )
            ) {
                append("\u2022 ${stringResource(Res.string.instruct_focus_stroop_1)}\n")
                append("\u2022 ${stringResource(Res.string.instruct_focus_stroop_2)}\n")
            }
            append("\n")
            withStyle(
                style = SpanStyle(
                    color = colorHomeLightGreen
                )
            ) {
                append(stringResource(Res.string.instruct_focus_stroop_outro))
            }
            append("\n")
        }
    )
}
