package feature.spike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import feature.stroop.StroopEngine
import feature.stroop.StroopTestView
import feature.stroop.TextCase

@Composable
fun SpikeScreen() {
    val stroopResult = remember { mutableStateOf(StroopEngine.generate()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        StroopTestView(
            text = if (stroopResult.value.textCase == TextCase.UPPER) {
                stroopResult.value.semanticTextColour.label.uppercase()
            } else {
                stroopResult.value.semanticTextColour.label.lowercase()
            },
            textColor = stroopResult.value.physicalTextColour.colour,
            backgroundColor = Color.Black,
            fontWeight = stroopResult.value.fontWeight,
            textCase = stroopResult.value.textCase,
        )
        Spacer(modifier = Modifier.height(96.dp))
        Button(onClick = { stroopResult.value = StroopEngine.generate() }) {
            Text("Generate")
        }
        Spacer(modifier = Modifier.height(48.dp))
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Text: ${stroopResult.value.semanticTextColour.label}", color = Color.Black)
            Text("Text Color: ${stroopResult.value.physicalTextColour.label}", color = Color.Black)
            Text("Font Weight: ${stroopResult.value.fontWeight}", color = Color.Black)
            Text("Text Case: ${stroopResult.value.textCase}", color = Color.Black)
            Text("Stimuli Group: ${stroopResult.value.stimuliGroup}", color = Color.Black)
        }
    }
}