package feature.settings.presentation

import com.russhwolf.settings.ExperimentalSettingsApi
import feature.common.events.DialogButton
import feature.common.events.DialogEvent
import feature.common.presentation.CMViewModel
import feature.common.presentation.ExternalBrowser
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.common.presentation.OpenUserFeedback
import feature.common.presentation.ResetAppData
import feature.common.presentation.UpdateSwitchPreference
import feature.common.presentation.UserFeedback
import feature.settings.data.PrefsRepository
import feature.settings.domain.usecase.ResetAllData
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import platform.AppVersionNumberProvider
import platform.ExternalWebBrowser
import platform.randomUUID

@ExperimentalSettingsApi
class SettingsViewModel(
    private val preferences: PrefsRepository,
    private val appVersionNumberProvider: AppVersionNumberProvider,
    private val externalWebBrowser: ExternalWebBrowser,
    private val resetAllData: ResetAllData
) : CMViewModel<SettingsState, Intent>() {

    init {
        _state.update { state ->
            state.copy(
                appVersion = appVersionNumberProvider.getAppVersionNumber()
            )
        }
        scope.launch {
            preferences.switchPreferencesFlow.collectLatest {
                _state.update { state ->
                    state.copy(switchPreferences = it)
                }
            }
        }
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            is UpdateSwitchPreference -> {
                scope.launch {
                    preferences.updateSwitchPreference(intent.key, intent.value)
                }
            }

            is ExternalBrowser -> {
                // TODO - Improve this to remove the need for FLAG_ACTIVITY_NEW_TASK.
                // Need to figure out how to get access to the Activity context.
                externalWebBrowser.launch(intent.url)
            }

            is ResetAppData -> {
                handleRestAllData()
            }

            is OpenUserFeedback -> {
                navigateTo(UserFeedback)
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

    override fun initialViewState(): SettingsState {
        return SettingsState(switchPreferences = mapOf())
    }

    private fun navigateTo(navTarget: NavigationEvent) {
        _state.update { state ->
            state.copy(navigateTo = navTarget)
        }
    }

    private fun handleRestAllData() {
        _state.update { state ->
            state.copy(
                events = state.events + DialogEvent(
                    id = randomUUID(),
                    title = "Reset all data!",
                    body = "Deletes all game data including user preferences.",
                    positiveButton = DialogButton(
                        ctaLabel = "Reset",
                        action = {
                            resetAllData()
                        }
                    ),
                    negativeButton = DialogButton(
                        ctaLabel = "Cancel"
                    )
                )
            )
        }
    }
}
