package feature.home.presentation

import app.data.AppRepository
import app.sound.SoundAndVibrationFeedback
import feature.common.presentation.CMViewModel
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import kotlinx.coroutines.flow.update
import platform.RES_ID_HOME_LOOP

class HomeViewModel(
    private val repository: AppRepository,
    private val soundAndVibration: SoundAndVibrationFeedback,
) : CMViewModel<HomeState, Intent>() {

    init {
        _state.update { state ->
            state.copy(
                musicOn = repository.isMusicOn(),
            )
        }
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            is HomeIntent.StartMusic -> {
                if (repository.isMusicOn()) {
                    soundAndVibration.startMusic(musicResourceId = RES_ID_HOME_LOOP)
                }
            }

            is HomeIntent.StopMusic -> {
                soundAndVibration.stopMusic(musicResourceId = RES_ID_HOME_LOOP)
            }

            is HomeIntent.ToggleMusic -> {
                val musicOn = repository.toggleMusic()
                if (musicOn) {
                    soundAndVibration.startMusic(musicResourceId = RES_ID_HOME_LOOP)
                } else {
                    soundAndVibration.stopMusic(musicResourceId = RES_ID_HOME_LOOP)
                }
                _state.update { state ->
                    state.copy(musicOn = musicOn)
                }
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

    override fun initialViewState(): HomeState {
        return HomeState()
    }

    override fun onNavigationComplete(navigationEvent: NavigationEvent) {
        _state.update { state ->
            state.copy(navigateTo = null)
        }
    }

    private fun navigateTo(navigationEvent: NavigationEvent) {
        _state.update { state ->
            state.copy(navigateTo = navigationEvent)
        }
    }
}
