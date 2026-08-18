package platform

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SoundAndVibrate {
    fun loopSound(soundResource: String, speed: Float = 1f)
    fun stopSound(soundResource: String)
    fun playSound(soundResource: String)
    // New overload allows subtle pitch/speed & volume variation for short clips
    fun playSound(soundResource: String, speed: Float = 1f, volume: Float = 1f)
    fun vibrate(duration: Long = 100)
}