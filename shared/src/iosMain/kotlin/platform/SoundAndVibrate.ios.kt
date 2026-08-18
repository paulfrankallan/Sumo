package platform

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SoundAndVibrate {
    actual fun playSound(soundResource: String) {
    // Minimal iOS implementation placeholder. Audio can be implemented using AVFoundation
    // later; keep no-op to avoid crashes on platforms where audio isn't wired up.
    return
    }

    actual fun playSound(soundResource: String, speed: Float, volume: Float) {
    // Overload for speed/volume; currently a no-op placeholder on iOS target.
    return
    }

    actual fun loopSound(soundResource: String, speed: Float) {
    // Placeholder
    }

    actual fun stopSound(soundResource: String) {
    }

    actual fun vibrate(
        duration: Long,
    ) {
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate)
    }
}
