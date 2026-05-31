package app.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nav.NavController
import app.theme.AppColor
import app.theme.colorHomeLightGreen
import app.util.popBackStackOrNavToRoute
import feature.home.nav.HOME_SCREEN_ROUTE
import feature.instructions.nav.navigateToInstructionsScreen
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.back
import sumo.shared.generated.resources.back_arrow
import sumo.shared.generated.resources.major
import sumo.shared.generated.resources.outline_help_24

@Composable
fun CustomTopBar(
    title: String,
    titleColor: Color,
    font: FontFamily = FontFamily(Font(Res.font.major)),
    showBackIcon: Boolean = true,
    backIconColor: Color = colorHomeLightGreen,
    showInfoButton: Boolean = false,
    infoButtonColor: Color? = null,
) {
    val navigator = NavController.current
    Spacer(
        modifier = Modifier.padding(
            top = WindowInsets.safeDrawing
                .asPaddingValues()
                .calculateTopPadding()
        )
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackIcon) {
            IconButton(
                onClick = {
                    navigator.popBackStackOrNavToRoute(HOME_SCREEN_ROUTE)
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.back_arrow),
                    contentDescription = stringResource(Res.string.back),
                    tint = backIconColor,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = titleColor,
            fontSize = 32.sp,
            fontFamily = font,
        )
        Spacer(Modifier.weight(1f))
        if (showInfoButton) {
            IconButton(
                onClick = {
                    navigator.navigateToInstructionsScreen()
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.outline_help_24),
                    contentDescription = "Info",
                    tint = infoButtonColor ?: AppColor.BLOOD_RED.color,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}