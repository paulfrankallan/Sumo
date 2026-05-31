package app.sound

import com.russhwolf.settings.ExperimentalSettingsApi
import feature.settings.data.PrefsRepository
import platform.RES_ID_DIE_1
import platform.RES_ID_DIE_2
import platform.RES_ID_DIE_3
import platform.RES_ID_DIE_4
import platform.RES_ID_DIE_5
import platform.SoundAndVibrate

@OptIn(ExperimentalSettingsApi::class)
class SoundAndVibrationFeedback(
    private val preferences: PrefsRepository,
    private val soundAndVibrate: SoundAndVibrate,
) {
    fun pressFeedback(soundResource: String) {
        if (preferences.isSoundEnabled()) {
            soundAndVibrate.playSound(soundResource)
        }
        if (preferences.isVibrateEnabled()) {
            soundAndVibrate.vibrate()
        }
    }

    fun gameOverFeedback() {
        if (preferences.isSoundEnabled()) {
            soundAndVibrate.playSound(
                listOf(
                    RES_ID_DIE_1,
                    RES_ID_DIE_2,
                    RES_ID_DIE_3,
                    RES_ID_DIE_4,
                    RES_ID_DIE_5,
                ).random()
            )
        }
        if (preferences.isVibrateEnabled()) {
            soundAndVibrate.vibrate()
        }
    }


    fun startMusic(musicResourceId: String, speed: Float = 1f) {
        if (preferences.isMusicEnabled()) {
            soundAndVibrate.loopSound(musicResourceId, speed)
        }
    }

    fun stopMusic(musicResourceId: String) {
        soundAndVibrate.stopSound(musicResourceId)
    }
}
