package feature.stroop

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.LocalScreen

@Composable
fun StroopTestView(
    stroopTest: StroopTest,
    modifier: Modifier = Modifier
) {
    StroopTestView(
        text = if (stroopTest.textCase == TextCase.UPPER) {
            stroopTest.semanticTextColour.label.uppercase()
        } else {
            stroopTest.semanticTextColour.label.lowercase()
        },
        textColor = stroopTest.physicalTextColour.colour,
        backgroundColor = Color.Black,
        fontWeight = stroopTest.fontWeight,
        textCase = stroopTest.textCase,
        modifier = modifier
    )
}

@Composable
fun StroopTestView(
    text: String,
    textColor: Color = Color.White,
    backgroundColor: Color = Color.Black,
    fontWeight: FontWeight? = FontWeight.Normal,
    textCase: TextCase? = TextCase.UPPER,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalScreen.current.width
    val fontSize = if (screenWidth <= 360.dp) {
        if (textCase == TextCase.UPPER) 16.sp else 18.sp
    } else {
        if (textCase == TextCase.UPPER) 19.sp else 21.sp
    }
    val displayText = if (textCase == TextCase.UPPER) {
        text.uppercase()
    } else {
        text.lowercase()
    }
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(backgroundColor)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = displayText,
                color = textColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = TextAlign.Center,
                softWrap = false,
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .weight(1f)
            )
        }
    }
}