package feature.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import app.nav.NavController
import app.theme.colorHomeDarkGreen
import app.theme.colorHomeLightGreen
import app.util.popBackStackOrNavToRoute
import feature.home.nav.HOME_SCREEN_ROUTE
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import platform.AndroidVersion
import platform.getPlatformInfo
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.back
import sumo.shared.generated.resources.back_arrow
import sumo.shared.generated.resources.screen_settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFeedbackTopBar() {
    val navigator = NavController.current
    val platformInfo = remember { getPlatformInfo() }
    val modifier = if (platformInfo.isAndroid && platformInfo.version >= AndroidVersion.R) {
        Modifier
            .background(colorHomeDarkGreen)
            .safeDrawingPadding()
            .systemBarsPadding()
    } else {
        Modifier
            .background(colorHomeDarkGreen)
    }
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.screen_settings),
                fontSize = 32.sp,
                color = colorHomeLightGreen,
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    navigator.popBackStackOrNavToRoute(HOME_SCREEN_ROUTE)
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.back_arrow),
                    contentDescription = stringResource(Res.string.back),
                    tint = colorHomeLightGreen,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorHomeDarkGreen,
        ),
        modifier = modifier
    )
}