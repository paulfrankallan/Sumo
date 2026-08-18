package feature.game.domain

import kotlin.math.max
import kotlin.random.Random

/**
 * Controller that decides when to play the "hakkeyoi" voice cue based on a
 * simplified activity measure of the two Rikishi. Placed in domain for easy
 * access from GameViewModel.
 */
class GyojiVoiceController(
    private val playHakkeyoi: (intensity: Int) -> Unit,
) {
    private enum class BoutState { ACTIVE, STRUGGLING, STALLED }

    private var state = BoutState.ACTIVE
    private var smoothedActivity = 0f
    private var stalledFor = 0f
    private var timeUntilNextHakkeyoi = Float.POSITIVE_INFINITY
    private var hakkeyoiCount = 0

    // Collision impulse accumulator (decays each frame). External callers should
    // call onCollision when a collision event occurs to add a short-lived impulse.
    private var collisionImpulse = 0f

    // Tunable weights.
    private val collisionWeight = 0.35f

    fun onCollision(impulse: Float) {
        collisionImpulse = max(collisionImpulse, impulse)
    }

    fun update(
        deltaSeconds: Float,
        rawActivity: Float,
        wrestlersTouching: Boolean,
        boutFinished: Boolean,
        wrestlerFalling: Boolean,
    ) {
        // Debug: log incoming parameters each update so we can trace why stalls
        // are not reaching the hakkeyoi play path.
        co.touchlab.kermit.Logger.d {
            "PFASOUND - GyojiVoiceController.update: delta=${"%.3f".format(deltaSeconds)} rawActivity=${"%.3f".format(rawActivity)} touching=$wrestlersTouching boutFinished=$boutFinished wrestlerFalling=$wrestlerFalling smoothedActivity=${"%.3f".format(smoothedActivity)} collisionImpulse=${"%.3f".format(collisionImpulse)}"
        }

        if (boutFinished || wrestlerFalling || !wrestlersTouching) {
            resetHakkeyoiState()
            return
        }

        val activityWithCollisions = rawActivity + collisionImpulse * collisionWeight

        // smooth activity to prevent flicker
        smoothedActivity = lerp(smoothedActivity, activityWithCollisions, 0.15f)

        // decay collision impulse
        collisionImpulse = (collisionImpulse - deltaSeconds * 1.5f).coerceAtLeast(0f)

        val newState = when (state) {
            BoutState.STALLED -> if (smoothedActivity > 0.25f) BoutState.ACTIVE else BoutState.STALLED
            else -> when {
                smoothedActivity < 0.15f -> BoutState.STALLED
                smoothedActivity < 0.45f -> BoutState.STRUGGLING
                else -> BoutState.ACTIVE
            }
        }

        if (newState != state) {
            onStateChanged(state, newState)
            state = newState
        }

        when (state) {
            BoutState.ACTIVE, BoutState.STRUGGLING -> stalledFor = 0f
            BoutState.STALLED -> {
                // Debug: report smoothed activity and time until hakkeyoi so we can trace why it didn't fire
                co.touchlab.kermit.Logger.d { "GyojiVoiceController: update STALLED smoothedActivity=${"%.3f".format(smoothedActivity)} stalledFor=${"%.2f".format(stalledFor)}s timeUntilNextHakkeyoi=${"%.2f".format(timeUntilNextHakkeyoi)}s" }
                updateStalledState(deltaSeconds)
            }
        }
    }

    private fun onStateChanged(oldState: BoutState, newState: BoutState) {
        // Debug: state transitions
        co.touchlab.kermit.Logger.d { "PFASOUND - GyojiVoiceController: state change $oldState -> $newState (smoothedActivity=${"%.3f".format(smoothedActivity)})" }
        if (newState == BoutState.STALLED) {
            stalledFor = 0f
            hakkeyoiCount = 0
            timeUntilNextHakkeyoi = randomBetween(1.6f, 3.1f)
        }

        if (oldState == BoutState.STALLED && newState != BoutState.STALLED) {
            resetHakkeyoiState()
        }
    }

    private fun updateStalledState(deltaSeconds: Float) {
        stalledFor += deltaSeconds
        timeUntilNextHakkeyoi -= deltaSeconds

        if (timeUntilNextHakkeyoi > 0f) return

        val intensity = when {
            stalledFor >= 10f -> 3
            stalledFor >= 5f -> 2
            else -> 1
        }

        // Debug: log when gyoji decides to call hakkeyoi
        co.touchlab.kermit.Logger.d { "PFASOUND - GyojiVoiceController: playing hakkeyoi intensity=$intensity stalledFor=${"%.2f".format(stalledFor)}s" }
        playHakkeyoi(intensity)
        hakkeyoiCount++

        timeUntilNextHakkeyoi = calculateNextInterval()
    }

    private fun calculateNextInterval(): Float {
        if (hakkeyoiCount >= 3 && Random.nextFloat() < 0.25f) {
            return randomBetween(3.5f, 5.0f)
        }
        return when {
            stalledFor >= 10f -> randomBetween(1.4f, 2.5f)
            stalledFor >= 5f -> randomBetween(1.8f, 3.0f)
            else -> randomBetween(2.2f, 3.5f)
        }
    }

    private fun resetHakkeyoiState() {
        co.touchlab.kermit.Logger.d { "PFASOUND - GyojiVoiceController: resetHakkeyoiState called (smoothedActivity=${"%.3f".format(smoothedActivity)}, hakkeyoiCount=$hakkeyoiCount, timeUntilNextHakkeyoi=${"%.2f".format(timeUntilNextHakkeyoi)}s)" }
        state = BoutState.ACTIVE
        stalledFor = 0f
        hakkeyoiCount = 0
        timeUntilNextHakkeyoi = Float.POSITIVE_INFINITY
        collisionImpulse = 0f
    }

    private fun lerp(from: Float, to: Float, amount: Float): Float = from + (to - from) * amount
    private fun randomBetween(minimum: Float, maximum: Float): Float = minimum + Random.nextFloat() * (maximum - minimum)
}
