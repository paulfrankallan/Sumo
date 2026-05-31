package app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import app.nav.AppNav
import app.nav.NavController

data class Screen(val width: Dp = 0.dp, val height: Dp = 0.dp, val default: Dp = 0.dp)
val LocalScreen = compositionLocalOf { Screen() }

@Composable
fun App(
    bannerHeight: Int = 0,
    navController: NavHostController = rememberNavController(),
    showInterstitialAd: () -> Unit,
    showBannerAd: (Boolean) -> Unit,
) {
    val screen = remember { mutableStateOf(Screen()) }
    Layout(
        content = {
            CompositionLocalProvider(LocalScreen provides screen.value) {
                CompositionLocalProvider(NavController provides navController) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Surface(color = Color.White) {
                            AppNav(
                                bannerHeight = bannerHeight,
                                showInterstitialAd = showInterstitialAd,
                                showBannerAd = showBannerAd
                            )
                        }
                    }
                }
            }
        },
        measurePolicy = { measurables, constraints ->
            // Use the max width and height from the constraints
            val width = constraints.maxWidth
            val height = constraints.maxHeight

            screen.value = Screen(width = width.toDp(), height = height.toDp())

            // Measure and place children composables
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            layout(width, height) {
                var yPosition = 0
                placeables.forEach { placeable ->
                    placeable.placeRelative(x = 0, y = yPosition)
                    yPosition += placeable.height
                }
            }
        }
    )
}
