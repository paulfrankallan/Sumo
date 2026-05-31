package app.nav

import androidx.navigation.NavBackStackEntry
import feature.ads.AdManager
import feature.common.presentation.CMViewModel
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.feedback.nav.USER_FEEDBACK_SCREEN_ROUTE
import feature.game.nav.PLAY_GAME_ROUTE
import feature.home.nav.HOME_SCREEN_ROUTE
import feature.instructions.nav.INSTRUCTIONS_SCREEN_ROUTE
import feature.settings.nav.SETTINGS_SCREEN_ROUTE
import kotlinx.coroutines.flow.update
import platform.analytics.KMAnalytics
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_BANNER_FEEDBACK_SCREEN
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_BANNER_GAME_SCREEN
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_BANNER_HOME_SCREEN
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_BANNER_INSTRUCTIONS_SCREEN
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_BANNER_SETTINGS_SCREEN
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_INTERSTITIAL_FEEDBACK_SCREEN
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_INTERSTITIAL_GAME_SCREEN
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_INTERSTITIAL_INSTRUCTIONS_SCREEN
import platform.remoteconfig.RemoteConfigConstants.KEY_AD_INTERSTITIAL_SETTINGS_SCREEN

class AppNavViewModel(
    private val adManager: AdManager,
    private val analytics: KMAnalytics
) : CMViewModel<AppNavState, Intent>() {

    fun isBannerAdVisibleForRoute(entry: NavBackStackEntry?): Boolean {
        if (adManager.adsAreOn().not()) {
            return false
        }
        return when (entry?.destination?.route) {
            HOME_SCREEN_ROUTE ->
                adManager.enabledByProbability(KEY_AD_BANNER_HOME_SCREEN)

            SETTINGS_SCREEN_ROUTE ->
                adManager.enabledByProbability(KEY_AD_BANNER_SETTINGS_SCREEN)

            INSTRUCTIONS_SCREEN_ROUTE ->
                adManager.enabledByProbability(KEY_AD_BANNER_INSTRUCTIONS_SCREEN)

            USER_FEEDBACK_SCREEN_ROUTE ->
                adManager.enabledByProbability(KEY_AD_BANNER_FEEDBACK_SCREEN)

            PLAY_GAME_ROUTE -> {
                adManager.enabledByProbability(KEY_AD_BANNER_GAME_SCREEN)
            }

            else -> false
        }
    }

    fun isInterstitialAdVisibleForRoute(entry: NavBackStackEntry?): Boolean {
        if (adManager.adsAreOn().not()) {
            return false
        }
        return when (entry?.destination?.route) {
            SETTINGS_SCREEN_ROUTE ->
                adManager.enabledByProbability(KEY_AD_INTERSTITIAL_SETTINGS_SCREEN)

            INSTRUCTIONS_SCREEN_ROUTE ->
                adManager.enabledByProbability(KEY_AD_INTERSTITIAL_INSTRUCTIONS_SCREEN)

            USER_FEEDBACK_SCREEN_ROUTE ->
                adManager.enabledByProbability(KEY_AD_INTERSTITIAL_FEEDBACK_SCREEN)

            PLAY_GAME_ROUTE -> {
                adManager.enabledByProbability(KEY_AD_INTERSTITIAL_GAME_SCREEN)
            }

            else -> false
        }
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            is AppNavIntent.ScreenView -> {
                intent.screenName?.let {
                    analytics.logScreenView(intent.screenName)
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

    override fun initialViewState(): AppNavState {
        return AppNavState()
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
