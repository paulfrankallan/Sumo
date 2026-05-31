package platform.di

import app.nav.AppNavViewModel
import com.russhwolf.settings.ExperimentalSettingsApi
import feature.feedback.UserFeedbackViewModel
import feature.game.presentation.GameViewModel
import feature.home.presentation.HomeViewModel
import feature.settings.presentation.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module
import platform.AppVersionNumberProvider
import platform.ExternalWebBrowser
import platform.ResourceIdProvider
import platform.SoundAndVibrate
import platform.analytics.KMAnalytics
import platform.crashlytics.KMCrashlytics
import platform.remoteconfig.RemoteConfigProvider

actual fun platformModule() = module {
    single { AppVersionNumberProvider(get()) }
    single { ExternalWebBrowser(get()) }
    single { ResourceIdProvider() }
    single { KMCrashlytics() }
    single { KMAnalytics() }
    single { RemoteConfigProvider(get()) }
    single { SoundAndVibrate(get(), get()) }
}

@OptIn(ExperimentalSettingsApi::class)
actual fun viewModelsModule() = module {
    viewModelOf(::UserFeedbackViewModel)
    viewModelOf(::HomeViewModel)
    viewModelOf(::GameViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::AppNavViewModel)
}
