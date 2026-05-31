package feature.feedback

import feature.common.events.Event
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.common.presentation.ViewState

data class UserFeedbackState(
    val navigateTo: NavigationEvent? = null,
    val events: List<Event> = emptyList(),
) : ViewState

sealed interface UserFeedbackIntent : Intent {
    data class SubmitFeedback(
        val feedback: String,
        val type: String,
    ) : UserFeedbackIntent
}
