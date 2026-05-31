package platform

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
    }
}
