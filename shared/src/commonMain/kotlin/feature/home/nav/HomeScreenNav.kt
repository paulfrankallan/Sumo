package feature.home.nav

import androidx.navigation.NavController
import androidx.navigation.NavOptions

const val HOME_SCREEN_ROUTE = "home_screen_route"

fun NavController.navigateToHomeScreen(
    navOptions: NavOptions? = null
) {
    navigate(HOME_SCREEN_ROUTE, navOptions)
}