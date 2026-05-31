package feature.feedback

import androidx.compose.material3.ExperimentalMaterial3Api
import com.russhwolf.settings.ExperimentalSettingsApi
import feature.common.presentation.CMViewModel
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import kotlinx.coroutines.flow.update
import platform.crashlytics.KMCrashlytics

@ExperimentalSettingsApi
class UserFeedbackViewModel(
    private val crashlytics: KMCrashlytics,
) : CMViewModel<UserFeedbackState, Intent>() {

    @ExperimentalMaterial3Api
    override fun onIntent(intent: Intent) {
        when (intent) {
            is UserFeedbackIntent.SubmitFeedback -> {
                crashlytics.reportUserFeedback(
                    feedback = intent.feedback,
                    type = intent.type
                )
            }
        }
    }

    override fun onEventComplete(eventId: String) {
        _state.update { state ->
            state.copy(
                events = state.events.filter { it.id != eventId }
            )
        }
    }

    override fun onNavigationComplete(navigationEvent: NavigationEvent) {
        _state.update { state ->
            state.copy(navigateTo = null)
        }
    }

    override fun initialViewState(): UserFeedbackState {
        return UserFeedbackState()
    }

    private fun navigateTo(navTarget: NavigationEvent) {
        _state.update { state ->
            state.copy(navigateTo = navTarget)
        }
    }
}
