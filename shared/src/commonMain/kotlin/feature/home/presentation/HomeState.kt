package feature.home.presentation

import feature.common.events.Event
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.common.presentation.ViewState

data class HomeState(
    val navigateTo: NavigationEvent? = null,
    val events: List<Event> = emptyList(),
    val musicOn: Boolean = false,
) : ViewState

sealed class HomeIntent : Intent {
    data object StartMusic : HomeIntent()
    data object StopMusic : HomeIntent()
    data object ToggleMusic : HomeIntent()
}
