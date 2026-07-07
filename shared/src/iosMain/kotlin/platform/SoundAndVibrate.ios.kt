package platform

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SoundAndVibrate {
    actual fun playSound(soundResource: String) {
    }

    actual fun loopSound(soundResource: String, speed: Float) {
    }

    actual fun stopSound(soundResource: String) {
    }

    actual fun vibrate(
        duration: Long,
    ) {
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
    }
}
