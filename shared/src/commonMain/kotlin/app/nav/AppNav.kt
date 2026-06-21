package app.nav

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.russhwolf.settings.ExperimentalSettingsApi
import feature.feedback.UserFeedbackScreen
import feature.feedback.nav.USER_FEEDBACK_SCREEN_ROUTE
import feature.game.nav.PLAY_GAME_ROUTE
import feature.game.presentation.GameScreen
import feature.home.nav.HOME_SCREEN_ROUTE
import feature.home.presentation.HomeScreen
import feature.instructions.nav.INSTRUCTIONS_SCREEN_ROUTE
import feature.instructions.presentation.InstructionsScreen
import feature.settings.nav.SETTINGS_SCREEN_ROUTE
import feature.settings.presentation.SettingsScreen
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.koinInject

val NavController = compositionLocalOf<NavHostController> { error("No NavController found!") }

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalSettingsApi::class, ExperimentalResourceApi::class
)
@Composable
fun AppNav(
    bannerHeight: Int = 0,
    showInterstitialAd: () -> Unit,
    showBannerAd: (Boolean) -> Unit
) {
    val viewModel: AppNavViewModel = koinInject()
    val backStackEntry by NavController.current.currentBackStackEntryAsState()
    val isBannerAdVisible = remember { mutableStateOf(false) }
    val isInterstitialAdVisible = remember { mutableStateOf(false) }
    val showInterstitial = {
        if (isInterstitialAdVisible.value) {
            showInterstitialAd()
        }
    }
    LaunchedEffect(backStackEntry?.destination?.route) {
        viewModel.onIntent(AppNavIntent.ScreenView(backStackEntry?.destination?.route))
        isBannerAdVisible.value = viewModel.isBannerAdVisibleForRoute(backStackEntry)
        isInterstitialAdVisible.value = viewModel.isInterstitialAdVisibleForRoute(backStackEntry)
        showBannerAd(isBannerAdVisible.value)
    }
    NavHost(
        navController = NavController.current,
        startDestination = HOME_SCREEN_ROUTE,
        modifier = Modifier
            .padding(bottom = if(isBannerAdVisible.value) bannerHeight.dp else 0.dp)
            .fillMaxSize()
    ) {
        composable(route = HOME_SCREEN_ROUTE) {
            HomeScreen(
                showInterstitialAd = showInterstitial,
            )
        }
        composable(
            route = PLAY_GAME_ROUTE,
            arguments = listOf(),
        ) { backStackEntry ->
            GameScreen(
                showInterstitialAd = showInterstitial,
            )
        }
        composable(route = SETTINGS_SCREEN_ROUTE) {
            SettingsScreen(
                showInterstitialAd = showInterstitial
            )
        }
        composable(
            route = INSTRUCTIONS_SCREEN_ROUTE
        ) {
            InstructionsScreen(
                showInterstitialAd = showInterstitial
            )
        }
        composable(route = USER_FEEDBACK_SCREEN_ROUTE) {
            UserFeedbackScreen(
                showInterstitialAd = showInterstitial
            )
        }
    }
}
