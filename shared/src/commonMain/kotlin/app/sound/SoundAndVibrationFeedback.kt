package app.sound

import com.russhwolf.settings.ExperimentalSettingsApi
import feature.settings.data.PrefsRepository
import platform.RES_ID_DIE_1
import platform.RES_ID_DIE_2
import platform.RES_ID_DIE_3
import platform.RES_ID_DIE_4
import platform.RES_ID_DIE_5
import platform.RES_ID_HAKKEYOI
import platform.RES_ID_SHOBUARI_GAME_OVER
import platform.SoundAndVibrate

@OptIn(ExperimentalSettingsApi::class)
class SoundAndVibrationFeedback(
    private val preferences: PrefsRepository,
    private val soundAndVibrate: SoundAndVibrate,
) {
    fun clashFeedback(duration: Long = 100) {
        if (preferences.isVibrateEnabled()) {
            soundAndVibrate.vibrate(duration)
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

    fun finalGameOverFeedback() {
        if (preferences.isSoundEnabled()) {
            soundAndVibrate.playSound(RES_ID_SHOBUARI_GAME_OVER)
        }
    }

    fun stopGameFeedback() {
        soundAndVibrate.stopSound(RES_ID_HAKKEYOI)
        soundAndVibrate.stopSound(RES_ID_SHOBUARI_GAME_OVER)
        listOf(
            RES_ID_DIE_1,
            RES_ID_DIE_2,
            RES_ID_DIE_3,
            RES_ID_DIE_4,
            RES_ID_DIE_5,
        ).forEach(soundAndVibrate::stopSound)
        soundAndVibrate.cancelVibration()
    }

    /**
     * Play the hakkeyoi cue with subtle variation. Intensity can be used to choose
     * a more emphatic variant where multiple recordings exist. For now we vary speed
     * and volume slightly so the single asset sounds more natural when repeated.
     */
    fun hakkeyoiFeedback(intensity: Int = 1) {
        if (!preferences.isSoundEnabled()) return
        val clamped = intensity.coerceIn(1, 3)
        val sMin: Double
        val sMax: Double
        val vMin: Double
        val vMax: Double
        when (clamped) {
            1 -> {
                sMin = 0.98; sMax = 1.02; vMin = 0.94; vMax = 1.0
            }
            2 -> {
                sMin = 0.99; sMax = 1.03; vMin = 0.98; vMax = 1.0
            }
            else -> {
                sMin = 0.995; sMax = 1.04; vMin = 1.0; vMax = 1.0
            }
        }
        val speed = (sMin..sMax).random().toFloat()
        val volume = (vMin..vMax).random().toFloat().coerceIn(0f, 1f)
        soundAndVibrate.playSound(RES_ID_HAKKEYOI, speed = speed, volume = volume)
    }
}

// Helpers for compact random ranges
private fun ClosedFloatingPointRange<Double>.random(): Double =
    kotlin.random.Random.nextDouble(start, endInclusive)
