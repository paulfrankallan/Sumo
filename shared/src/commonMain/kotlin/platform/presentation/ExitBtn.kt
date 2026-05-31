package platform.presentation

import androidx.compose.foundation.clickable
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.theme.AppColor
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.back_arrow

@Composable
expect fun ExitArrow(modifier: Modifier = Modifier)

@Composable
fun PlatformExitArrow(
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Icon(
        painter = painterResource(resource = Res.drawable.back_arrow),
        tint = AppColor.BLOOD_RED.color,
        contentDescription = "Exit App Arrow",
        modifier = modifier.clickable { onClick() }
    )
}