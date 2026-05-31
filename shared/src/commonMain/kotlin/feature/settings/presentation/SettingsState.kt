package feature.settings.presentation

import feature.common.events.Event
import feature.common.presentation.NavigationEvent
import feature.common.presentation.ViewState

data class SettingsState(
    val navigateTo: NavigationEvent? = null,
    val events: List<Event> = emptyList(),
    val appVersion: String = "",
    val switchPreferences: Map<String, Boolean>,
    val showUserFeedbackDialog: Boolean = false,
    val showUserFeedbackDialogSuccess: Boolean = false,
) : ViewState