package feature.spike

import androidx.navigation.NavController
import androidx.navigation.NavOptions

const val SPIKE_SCREEN_ROUTE = "spike_screen_route"

fun NavController.navigateToSpikeScreen(
    navOptions: NavOptions? = null
) {
    navigate(SPIKE_SCREEN_ROUTE, navOptions)
}