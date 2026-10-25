package feature.instructions.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.instructions_getting_started

@Composable
fun GettingStartedInstructions() {
    InstructionText(stringResource(Res.string.instructions_getting_started))
}
