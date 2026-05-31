package feature.home.presentation

import feature.common.events.Event
import feature.common.model.GameType
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.common.presentation.ViewState

data class HomeState(
    val navigateTo: NavigationEvent? = null,
    val events: List<Event> = emptyList(),
    val musicOn: Boolean = false,
    val gameType: GameType = GameType.STRENGTH,
) : ViewState

sealed class HomeIntent : Intent {
    data object StartMusic : HomeIntent()
    data object StopMusic : HomeIntent()
    data object ToggleMusic : HomeIntent()
    data class GameTypeSelected(val gameType: GameType) : HomeIntent()
}
