package platform

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import app.util.isTrue
import java.io.IOException

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SoundAndVibrate(
    private val context: Context,
    private val resourceIdProvider: ResourceIdProvider
) {
    private val mediaPlayers = mutableMapOf<String, MediaPlayer?>()

    @SuppressLint("ObsoleteSdkInt")
    actual fun loopSound(soundResource: String, speed: Float) {
        mediaPlayers[soundResource]?.stop()
        mediaPlayers[soundResource]?.release()
        val resourceId = resourceIdProvider.getResourceId(soundResource)
        resourceId?.let {
            mediaPlayers[soundResource] = MediaPlayer.create(context, resourceId).apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val params = playbackParams
                    params.speed = speed
                    playbackParams = params
                }
                setOnCompletionListener {
                    reset()
                    release()
                    mediaPlayers[soundResource] = null
                }
                isLooping = true
                start()
            }
        }
    }

    actual fun stopSound(soundResource: String) {
        mediaPlayers[soundResource]?.let { mediaPlayer ->
            if (mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.release()
            }
        }
        mediaPlayers[soundResource] = null
    }

    actual fun playSound(soundResource: String) {
        val resourceId = resourceIdProvider.getResourceId(soundResource)
        resourceId?.let {
            mediaPlayers[soundResource] = MediaPlayer.create(context, resourceId).apply {
                try {
                    if (!isPlaying.isTrue()) {
                        start()
                    }
                    setOnCompletionListener {
                        reset()
                        release()
                    }
                } catch (e: IOException) {
                    release()
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ObsoleteSdkInt")
    actual fun vibrate(
        duration: Long
    ) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    duration,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(777)
        }
    }
}