package feature.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.unit.dp
import app.nav.NavController
import app.theme.AppColor
import app.theme.appGold
import feature.instructions.nav.navigateToInstructionsScreen
import feature.settings.nav.navigateToSettingsScreen
import org.jetbrains.compose.resources.painterResource
import platform.AndroidVersion
import platform.getPlatformInfo
import platform.presentation.ExitArrow
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.music_off
import sumo.shared.generated.resources.music_on
import sumo.shared.generated.resources.outline_help_24
import sumo.shared.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    musicOn: Boolean,
    toggleMusic: () -> Unit
) {
    val navigator = NavController.current
    val platformInfo = remember { getPlatformInfo() }
    val modifier = if (platformInfo.isAndroid && platformInfo.version >= AndroidVersion.R) {
        Modifier
            .safeDrawingPadding()
            .systemBarsPadding()
    } else {
        Modifier
            .background(Transparent)
    }
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(
                onClick = {
                    navigator.navigateToSettingsScreen()
                },
                modifier = Modifier
                    .padding(8.dp)
            ) {
                ExitArrow(
                    modifier = Modifier
                        .fillMaxSize()
                )
            }
        },
        actions = {
            IconButton(
                onClick = {
                    navigator.navigateToSettingsScreen()
                },
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.settings),
                    contentDescription = "Settings",
                    tint = appGold,
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                onClick = toggleMusic,
                modifier = Modifier
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(
                        if (musicOn) {
                            Res.drawable.music_on
                        } else {
                            Res.drawable.music_off
                        }
                    ),
                    contentDescription = "Music toggle",
                    tint = if (musicOn) {
                        appGold
                    } else {
                        AppColor.BLOOD_RED.color
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            IconButton(
                modifier = Modifier
                    .padding(8.dp),
                onClick = {
                    navigator.navigateToInstructionsScreen()
                }
            ) {
                Icon(
                    painter = painterResource(Res.drawable.outline_help_24),
                    contentDescription = "Instructions",
                    tint = appGold,
                    modifier = Modifier.fillMaxSize()
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Transparent,
        ),
        modifier = modifier
    )
}