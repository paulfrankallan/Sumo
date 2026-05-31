package feature.settings.nav

import androidx.navigation.NavController
import androidx.navigation.NavOptions

const val SETTINGS_SCREEN_ROUTE = "settings_screen_route"

fun NavController.navigateToSettingsScreen(
    navOptions: NavOptions? = null
) {
    navigate(SETTINGS_SCREEN_ROUTE, navOptions)
}