@file:OptIn(ExperimentalMaterialApi::class, ExperimentalSettingsApi::class)

package feature.settings.presentation

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.nav.NavController
import app.presentation.DialogWrapper
import app.theme.colorHomeDarkGreen
import app.theme.colorHomeLightGreen
import com.russhwolf.settings.ExperimentalSettingsApi
import feature.common.events.DialogEvent
import feature.common.presentation.ExternalBrowser
import feature.common.presentation.Intent
import feature.common.presentation.OpenUserFeedback
import feature.common.presentation.ResetAppData
import feature.common.presentation.UpdateSwitchPreference
import feature.common.presentation.UserFeedback
import feature.feedback.nav.navigateToUserFeedbackScreen
import feature.settings.data.PrefsRepository.Companion.PREF_KEY_MUSIC
import feature.settings.data.PrefsRepository.Companion.PREF_KEY_SOUND
import feature.settings.data.PrefsRepository.Companion.PREF_KEY_VIBRATE
import feature.settings.presentation.ui.SwitchPref
import feature.settings.presentation.ui.TextPref
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.settings_app_version
import sumo.shared.generated.resources.settings_music
import sumo.shared.generated.resources.settings_music_description
import sumo.shared.generated.resources.settings_privacy_policy
import sumo.shared.generated.resources.settings_privacy_policy_description
import sumo.shared.generated.resources.settings_reset_data
import sumo.shared.generated.resources.settings_reset_data_description
import sumo.shared.generated.resources.settings_sound
import sumo.shared.generated.resources.settings_sound_description
import sumo.shared.generated.resources.settings_user_feedback
import sumo.shared.generated.resources.settings_user_feedback_description
import sumo.shared.generated.resources.settings_vibrate
import sumo.shared.generated.resources.settings_vibrate_description

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(
    showInterstitialAd: () -> Unit
) {
    LaunchedEffect(Unit) {
        showInterstitialAd()
    }
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val navigator = NavController.current

    state.events.forEach { event ->
        when (event) {
            is DialogEvent -> {
                DialogWrapper(event) {
                    viewModel.onEventComplete(event.id)
                }
            }
        }
    }

    state.navigateTo?.let { navTarget ->
        when (navTarget) {
            is UserFeedback -> {
                navigator.navigateToUserFeedbackScreen()
            }
        }
        viewModel.onNavigationComplete(navTarget)
    }

    SettingsContent(
        state = state,
        onIntent = viewModel::onIntent
    )
}

@ExperimentalMaterialApi
@Composable
fun SettingsContent(
    state: SettingsState,
    onIntent: (Intent) -> Unit
) {
    Scaffold(
        containerColor = colorHomeDarkGreen,
        topBar = {
            SettingsTopBar()
        },
        contentWindowInsets = WindowInsets.displayCutout
    ) {
        Surface(
            color = colorHomeDarkGreen,
            modifier = Modifier
                .fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = spacedBy(8.dp)
            ) {
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 3.dp,
                        color = colorHomeLightGreen
                    )
                }
                item {
                    state.switchPreferences[PREF_KEY_VIBRATE]?.let { prefValue ->
                        SwitchPref(
                            key = PREF_KEY_VIBRATE,
                            title = stringResource(Res.string.settings_vibrate),
                            summary = stringResource(Res.string.settings_vibrate_description),
                            enabled = true,
                            checked = prefValue,
                            edit = { key, value ->
                                onIntent(UpdateSwitchPreference(key, value))
                            }
                        )
                    }
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 3.dp,
                        color = colorHomeLightGreen
                    )
                }
                item {
                    state.switchPreferences[PREF_KEY_SOUND]?.let { prefValue ->
                        SwitchPref(
                            key = PREF_KEY_SOUND,
                            title = stringResource(Res.string.settings_sound),
                            summary = stringResource(Res.string.settings_sound_description),
                            enabled = true,
                            checked = prefValue,
                            edit = { key, value ->
                                onIntent(UpdateSwitchPreference(key, value))
                            }
                        )
                    }
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 3.dp,
                        color = colorHomeLightGreen
                    )
                }
                item {
                    state.switchPreferences[PREF_KEY_MUSIC]?.let { prefValue ->
                        SwitchPref(
                            key = PREF_KEY_MUSIC,
                            title = stringResource(Res.string.settings_music),
                            summary = stringResource(Res.string.settings_music_description),
                            enabled = true,
                            checked = prefValue,
                            edit = { key, value ->
                                onIntent(UpdateSwitchPreference(key, value))
                            }
                        )
                    }
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 3.dp,
                        color = colorHomeLightGreen
                    )
                }
                item {
                    TextPref(
                        title = stringResource(Res.string.settings_privacy_policy),
                        summary = stringResource(Res.string.settings_privacy_policy_description),
                        onClick = {
                            onIntent(
                                ExternalBrowser(
                                    "https://www.corbstech.com/sumo_privacy_policy.html"
                                )
                            )
                        }
                    )
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 3.dp,
                        color = colorHomeLightGreen
                    )
                }
                item {
                    TextPref(
                        title = stringResource(Res.string.settings_user_feedback),
                        summary = stringResource(Res.string.settings_user_feedback_description),
                        onClick = {
                            onIntent(OpenUserFeedback)
                        }
                    )
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 3.dp,
                        color = colorHomeLightGreen
                    )
                }
                item {
                    TextPref(
                        title = stringResource(Res.string.settings_reset_data),
                        summary = stringResource(Res.string.settings_reset_data_description),
                        onClick = {
                            onIntent(ResetAppData)
                        }
                    )
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 3.dp,
                        color = colorHomeLightGreen
                    )
                }
                item {
                    TextPref(
                        title = stringResource(Res.string.settings_app_version),
                        summary = state.appVersion,
                    )
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 3.dp,
                        color = colorHomeLightGreen
                    )
                }
            }
        }
    }
}
