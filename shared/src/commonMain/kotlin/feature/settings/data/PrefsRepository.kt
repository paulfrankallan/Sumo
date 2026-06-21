package feature.settings.data

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

@ExperimentalSettingsApi
class PrefsRepository(
    private val settings: Settings
) {
    companion object {
        const val PREF_KEY_SOUND = "PREF_KEY_SOUND"
        const val PREF_KEY_VIBRATE = "PREF_KEY_VIBRATE"
        const val PREF_KEY_MUSIC = "PREF_KEY_MUSIC"
    }

    private val _switchPreferences = MutableSharedFlow<Map<String, Boolean>>(replay = 1)
    val switchPreferencesFlow = _switchPreferences.asSharedFlow()

    init {
        refreshSwitchPreferences()
    }

    fun updateSwitchPreference(key: String, value: Boolean) {
        settings.putBoolean(key, value)
        refreshSwitchPreferences()
    }

    private fun refreshSwitchPreferences() {
        _switchPreferences.tryEmit(
            mapOf(
                PREF_KEY_SOUND to settings.getBoolean(PREF_KEY_SOUND, true),
                PREF_KEY_MUSIC to settings.getBoolean(PREF_KEY_MUSIC, true),
                PREF_KEY_VIBRATE to settings.getBoolean(PREF_KEY_VIBRATE, true),
            )
        )
    }

    fun isSoundEnabled(): Boolean {
        return settings.getBoolean(PREF_KEY_SOUND, true)
    }

    fun isMusicEnabled(): Boolean {
        return settings.getBoolean(PREF_KEY_MUSIC, true)
    }

    fun setMusicEnabled(enabled: Boolean) {
        settings.putBoolean(PREF_KEY_MUSIC, enabled)
        refreshSwitchPreferences()
    }

    fun isVibrateEnabled(): Boolean {
        return settings.getBoolean(PREF_KEY_VIBRATE, true)
    }

    fun clear() {
        settings.clear()
        refreshSwitchPreferences()
    }
}
