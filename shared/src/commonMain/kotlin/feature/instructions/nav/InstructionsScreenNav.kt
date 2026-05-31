package feature.instructions.nav

import androidx.navigation.NavController
import androidx.navigation.NavOptions

const val INSTRUCTIONS_SCREEN_ROUTE = "instructions_screen_route"

fun NavController.navigateToInstructionsScreen(
    navOptions: NavOptions? = null
) {
    navigate(INSTRUCTIONS_SCREEN_ROUTE, navOptions)
}
