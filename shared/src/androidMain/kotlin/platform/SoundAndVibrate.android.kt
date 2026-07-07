package platform

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import app.util.isTrue
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class SoundAndVibrate(
    private val context: Context,
    private val resourceIdProvider: ResourceIdProvider
) {
    // ConcurrentHashMap ensures thread-safe reads/writes when audio calls arrive
    // from multiple IO coroutines simultaneously.
    private val mediaPlayers = ConcurrentHashMap<String, MediaPlayer>()

    @Synchronized
    actual fun loopSound(soundResource: String, speed: Float) {
        releasePlayer(soundResource)
        val resourceId = resourceIdProvider.getResourceId(soundResource) ?: return
        mediaPlayers[soundResource] = MediaPlayer.create(context, resourceId)?.apply {
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
        mediaPlayers[soundResource] = MediaPlayer.create(context, resourceId)?.apply {
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
        val vibrationDuration = duration.coerceAtLeast(80L)
        val effect = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK)
        } else {
            VibrationEffect.createOneShot(vibrationDuration, VibrationEffect.DEFAULT_AMPLITUDE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            if (manager != null) {
                manager.vibrate(CombinedVibration.createParallel(effect))
                return
            }
        }

        val vibrator = resolveVibrator() ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(vibrationDuration)
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun resolveVibrator(): Vibrator? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            val managerVibrator = manager?.defaultVibrator
            if (managerVibrator != null) return managerVibrator
        }
        @Suppress("DEPRECATION")
        return context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
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
