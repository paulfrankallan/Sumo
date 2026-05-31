package platform.di

import app.nav.AppNavViewModel
import com.russhwolf.settings.ExperimentalSettingsApi
import feature.feedback.UserFeedbackViewModel
import feature.game.presentation.GameViewModel
import feature.home.presentation.HomeViewModel
import feature.settings.presentation.SettingsViewModel
import org.koin.core.KoinApplication
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module
import platform.AppVersionNumberProvider
import platform.ExternalWebBrowser
import platform.ResourceIdProvider
import platform.SoundAndVibrate
import platform.analytics.KMAnalytics
import platform.crashlytics.KMCrashlytics
import platform.remoteconfig.RemoteConfigProvider

@OptIn(ExperimentalSettingsApi::class)
@Suppress("unused")
fun initKoinIos(

): KoinApplication {
    return initKoin()
}

@OptIn(ExperimentalSettingsApi::class)
@Suppress("unused")
fun doInitKoinIos() {
    initKoinIos()
}

actual fun platformModule() = module {
    single { AppVersionNumberProvider() }
    single { ExternalWebBrowser() }
    single { ResourceIdProvider() }
    single { KMCrashlytics() }
    single { KMAnalytics() }
    single { RemoteConfigProvider(get()) }
    single { SoundAndVibrate() }
}

@OptIn(ExperimentalSettingsApi::class)
actual fun viewModelsModule() = module {
    factoryOf(::HomeViewModel)
    factoryOf(::GameViewModel)
    factoryOf(::SettingsViewModel)
    factoryOf(::UserFeedbackViewModel)
    factoryOf(::AppNavViewModel)
}
