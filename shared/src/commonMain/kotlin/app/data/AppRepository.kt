package app.data

import com.russhwolf.settings.ExperimentalSettingsApi
import feature.common.model.GameType
import feature.settings.data.PrefsRepository

@OptIn(ExperimentalSettingsApi::class)
class AppRepository(
    private val preferences: PrefsRepository,
) {
    // region Preferences

    fun setMusicOn(isMusicOn: Boolean) {
        preferences.setMusicEnabled(isMusicOn)
    }

    fun isMusicOn(): Boolean {
        return preferences.isMusicEnabled()
    }

    fun toggleMusic(): Boolean {
        val toggled = isMusicOn().not()
        setMusicOn(toggled)
        return toggled
    }

    fun getGameType(): GameType {
        return preferences.getGameType()
    }

    fun setGameType(gameType: GameType) {
        preferences.setGameType(gameType)
    }

    // endregion Preferences
}