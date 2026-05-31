package platform.presentation

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun ExitArrow(modifier: Modifier) {
    val activity = LocalActivity.current
    PlatformExitArrow(
        modifier = modifier,
        onClick = { activity?.finishAffinity() }
    )
}