package platform.di

import app.data.AppRepository
import app.sound.SoundAndVibrationFeedback
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import feature.ads.AdManager
import feature.game.domain.engine.GameLoop
import feature.game.domain.usecase.ApplyDamage
import feature.game.domain.usecase.UpdatePlayState
import feature.settings.data.PrefsRepository
import feature.settings.domain.usecase.ResetAllData
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module
import platform.remoteconfig.RemoteConfigDefaults

expect fun platformModule(): Module
expect fun viewModelsModule(): Module

@OptIn(ExperimentalSettingsApi::class)
fun initKoin() = initKoin { }

@OptIn(ExperimentalSettingsApi::class)
fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(
        module {
            single<CoroutineDispatcher> { Dispatchers.IO }
            singleOf(::RemoteConfigDefaults)
            singleOf(::ResetAllData)
            singleOf(::ApplyDamage)
            singleOf(::UpdatePlayState)
            single { GameLoop() }
            single { SoundAndVibrationFeedback(get(), get()) }
            single { PrefsRepository(Settings()) }
            single { AppRepository(get()) }
            single { AdManager(get()) }
        },
        platformModule(),
        viewModelsModule(),
    )
}
