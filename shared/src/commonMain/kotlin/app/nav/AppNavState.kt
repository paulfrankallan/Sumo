package app.nav

import feature.common.events.Event
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.common.presentation.ViewState

data class AppNavState(
    val navigateTo: NavigationEvent? = null,
    val events: List<Event> = emptyList(),
    val visibleBanners: List<String> = emptyList()
) : ViewState {

}

sealed interface AppNavIntent : Intent {
    data class ScreenView(val screenName: String?) : AppNavIntent
}
