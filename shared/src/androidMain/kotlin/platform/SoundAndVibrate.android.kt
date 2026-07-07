package platform

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import app.util.isTrue
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SoundAndVibrate(
    private val context: Context,
    private val resourceIdProvider: ResourceIdProvider
) {
    private val appContext = context.applicationContext

    // ConcurrentHashMap ensures thread-safe reads/writes when audio calls arrive
    // from multiple IO coroutines simultaneously.
    private val mediaPlayers = ConcurrentHashMap<String, MediaPlayer>()

    @Synchronized
    actual fun loopSound(soundResource: String, speed: Float) {
        releasePlayer(soundResource)
        val resourceId = resourceIdProvider.getResourceId(soundResource) ?: return
        mediaPlayers[soundResource] = MediaPlayer.create(appContext, resourceId)?.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                playbackParams = playbackParams.setSpeed(speed)
            }
            setOnCompletionListener {
                it.reset()
                it.release()
                mediaPlayers.remove(soundResource, it)
            }
            isLooping = true
            start()
        } ?: return
    }

    @Synchronized
    actual fun stopSound(soundResource: String) {
        releasePlayer(soundResource)
    }

    @Synchronized
    actual fun playSound(soundResource: String) {
        val resourceId = resourceIdProvider.getResourceId(soundResource) ?: return
        mediaPlayers[soundResource] = MediaPlayer.create(appContext, resourceId)?.apply {
            try {
                if (!isPlaying.isTrue()) start()
                setOnCompletionListener {
                    it.reset()
                    it.release()
                    mediaPlayers.remove(soundResource, it)
                }
            } catch (e: IOException) {
                release()
            }
        } ?: return
    }

    @Suppress("DEPRECATION")
    @SuppressLint("ObsoleteSdkInt")
    actual fun vibrate(duration: Long) {
        val vibrator = resolveVibrator() ?: return
        if (!vibrator.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationDuration = duration.coerceAtLeast(220L)
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0L, vibrationDuration, 80L, vibrationDuration),
                    intArrayOf(0, 255, 0, 255),
                    -1,
                )
            )
        } else {
            val vibrationDuration = duration.coerceAtLeast(300L)
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationDuration)
        }
    }

    private fun resolveVibrator(): Vibrator? {
        @Suppress("DEPRECATION")
        return appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private fun releasePlayer(soundResource: String) {
        mediaPlayers.remove(soundResource)?.let { player ->
            try {
                if (player.isPlaying) player.stop()
                player.release()
            } catch (_: IllegalStateException) {
                // Player may already be released via completion listener.
            }
        }
    }
}
