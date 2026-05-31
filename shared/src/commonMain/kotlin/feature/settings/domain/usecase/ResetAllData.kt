@file:OptIn(ExperimentalSettingsApi::class)

package feature.settings.domain.usecase

//import cm.app.core.data.AppRepository
import com.russhwolf.settings.ExperimentalSettingsApi
import feature.settings.data.PrefsRepository

class ResetAllData(
    //private val repository: AppRepository,
    private val preferences: PrefsRepository,
) {
    operator fun invoke() {
        preferences.clear()
//        repository.resetAllData()
    }
}
