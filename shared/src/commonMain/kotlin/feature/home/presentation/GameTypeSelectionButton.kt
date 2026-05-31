package feature.home.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.LocalScreen
import app.presentation.segmentedbutton.SegmentedButton
import app.presentation.segmentedbutton.SegmentedButtonDefaults
import app.presentation.segmentedbutton.SegmentedButtonItem
import feature.common.model.GameType
import feature.common.presentation.Intent
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.stringResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.agility
import sumo.shared.generated.resources.focus
import sumo.shared.generated.resources.major
import sumo.shared.generated.resources.strength

@Composable
fun GameTypeSelectionButton(
    modifier: Modifier = Modifier,
    onIntent: (Intent) -> Unit,
    gameType: GameType,
) {
    val screenWidth = LocalScreen.current.width
    val fontSize = if (screenWidth <= 360.dp) 14.sp else 18.sp
    val font = FontFamily(Font(Res.font.major))
    SegmentedButton(
        defaultSelectedItemIndex = when (gameType) {
            GameType.STRENGTH -> 0
            GameType.AGILITY -> 1
            GameType.FOCUS -> 2
        },
        items = listOf(
            SegmentedButtonItem(
                title = {
                    Text(
                        text = stringResource(resource = Res.string.strength),
                        fontSize = fontSize,
                        fontFamily = font,
                        softWrap = false,
                    )
                },
                onClick = { onIntent(HomeIntent.GameTypeSelected(GameType.STRENGTH)) }
            ),
            SegmentedButtonItem(
                title = {
                    Text(
                        text = stringResource(resource = Res.string.agility),
                        fontSize = fontSize,
                        fontFamily = font,
                        softWrap = false,
                    )
                },
                onClick = { onIntent(HomeIntent.GameTypeSelected(GameType.AGILITY)) }
            ),
            SegmentedButtonItem(
                title = {
                    Text(
                        text = stringResource(resource = Res.string.focus),
                        fontSize = fontSize,
                        fontFamily = font,
                        softWrap = false,
                    )
                },
                onClick = { onIntent(HomeIntent.GameTypeSelected(GameType.FOCUS)) }
            ),
        ),
        color = SegmentedButtonDefaults.segmentedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            selectedContainerColor = MaterialTheme.colorScheme.onPrimary,
            outlineColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            selectedContentColor = MaterialTheme.colorScheme.primary,
        ),
        modifier = modifier
    )
}