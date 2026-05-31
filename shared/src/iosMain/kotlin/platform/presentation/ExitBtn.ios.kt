package platform.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ExitArrow(modifier: Modifier) {
    PlatformExitArrow(
        modifier = modifier,
        onClick = {
            // Implement iOS-specific exit functionality here
        }
    )
}