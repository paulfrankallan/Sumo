package feature.feedback.nav

import androidx.navigation.NavController
import androidx.navigation.NavOptions

const val USER_FEEDBACK_SCREEN_ROUTE = "user_feedback_screen_route"

fun NavController.navigateToUserFeedbackScreen(
    navOptions: NavOptions? = null
) {
    navigate(USER_FEEDBACK_SCREEN_ROUTE, navOptions)
}