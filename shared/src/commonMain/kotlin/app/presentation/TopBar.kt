
package app.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.nav.NavController
import app.theme.colorHomeDarkGreen
import app.theme.colorHomeLightGreen
import app.util.popBackStackOrNavToRoute
import feature.home.nav.HOME_SCREEN_ROUTE
import feature.instructions.nav.navigateToInstructionsScreen
import feature.settings.nav.navigateToSettingsScreen
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.back_arrow
import sumo.shared.generated.resources.major
import sumo.shared.generated.resources.outline_help_24
import sumo.shared.generated.resources.settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    titleStart: String,
    titleEnd: String? = null,
    shouldShowSettings: Boolean = true,
    shouldShowBackButton: Boolean = true,
    shouldHideAllActions: Boolean = false,
    shouldShowAdsButton: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    val font = FontFamily(Font(Res.font.major))
    val infiniteTransition = rememberInfiniteTransition(label = "")

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
            },
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    val navigator = NavController.current

    TopAppBar(
        windowInsets = WindowInsets(
            top = 0.dp,
            bottom = 0.dp
        ),
        title = {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = titleStart,
                    fontSize = 32.sp,
                    color = colorHomeLightGreen,
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontFamily = font,
                )
                titleEnd?.let {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = titleEnd,
                        fontSize = 32.sp,
                        color = colorHomeLightGreen,
                        modifier = Modifier.align(Alignment.CenterVertically),
                        fontFamily = font
                    )
                }
                if (shouldShowAdsButton) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "ADS",
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 16.dp)
                                .alpha(alpha)
                                .clickable {
                                },
                            fontSize = 20.sp,
                            fontWeight = Bold,
                            color = colorHomeLightGreen,
                            textAlign = TextAlign.End,
                        )
                    }
                }
            }
        },
        navigationIcon = {
            if (shouldShowBackButton) {
                IconButton(
                    onClick = {
                        navigator.popBackStackOrNavToRoute(HOME_SCREEN_ROUTE)
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.back_arrow),
                        contentDescription = "Back",
                        tint = colorHomeLightGreen,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                }
            }
        },
        actions = {
            if (!shouldHideAllActions) {
                if (shouldShowSettings) {
                    IconButton(
                        onClick = {
                            navigator.navigateToSettingsScreen()
                        }
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.settings),
                            contentDescription = "Settings",
                            tint = colorHomeLightGreen,
                            modifier = Modifier
                                .fillMaxSize()
                        )
                    }
                }
                IconButton(
                    onClick = {
                        navigator.navigateToInstructionsScreen()
                    }
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.outline_help_24),
                        contentDescription = "Instructions",
                        tint = colorHomeLightGreen,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colorHomeDarkGreen
        ),
        modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
    )
}
