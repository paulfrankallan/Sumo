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
            playbackParams = playbackParams.setSpeed(speed)
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
        // Maintain backward-compatible behavior by delegating to the richer overload.
        playSound(soundResource, speed = 1f, volume = 1f)
    }

    @Synchronized
    actual fun playSound(soundResource: String, speed: Float, volume: Float) {
        val resourceId = resourceIdProvider.getResourceId(soundResource) ?: run {
            co.touchlab.kermit.Logger.e { "PFASOUND - SoundAndVibrate.android: unknown resource name=$soundResource" }
            return
        }
        co.touchlab.kermit.Logger.i { "PFASOUND - SoundAndVibrate.android: playSound resource=$resourceId speed=$speed volume=$volume" }

        // Debug: note when hakkeyoi resource is requested (no UI toast)
        if (soundResource == RES_ID_HAKKEYOI) {
            co.touchlab.kermit.Logger.d { "PFASOUND - SoundAndVibrate.android: hakkeyoi resource requested (debug)" }
        }

        val player = MediaPlayer.create(appContext, resourceId)
        if (player == null) {
            co.touchlab.kermit.Logger.e { "PFASOUND - SoundAndVibrate.android: MediaPlayer.create returned null for resource=$resourceId" }
            return
        }

        mediaPlayers[soundResource] = player.apply {
            try {
                // Apply subtle speed variation where supported.
                try {
                    playbackParams = playbackParams.setSpeed(speed)
                } catch (_: Throwable) {
                    // Ignore devices that don't support playback speed changes.
                }
                setVolume(volume.coerceIn(0f, 1f), volume.coerceIn(0f, 1f))
                if (!isPlaying.isTrue()) start()
                setOnCompletionListener {
                    it.reset()
                    it.release()
                    mediaPlayers.remove(soundResource, it)
                }
            } catch (e: IOException) {
                co.touchlab.kermit.Logger.e { "PFASOUND - SoundAndVibrate.android: IOException while preparing player: ${e.message}" }
                release()
            }
        }
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
