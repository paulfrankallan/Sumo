package feature.game.domain

import androidx.compose.ui.geometry.Offset
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Schedules the "hakkeyoi" voice cue while both Rikishi remain within a small
 * area of their positions at the start of a stationary period.
 */
class GyojiVoiceController(
    private val playHakkeyoi: () -> Unit,
) {
    private var stationaryTopPosition: Offset? = null
    private var stationaryBottomPosition: Offset? = null
    private var timeUntilNextHakkeyoi = Float.POSITIVE_INFINITY
    private var lastUpdateTime: TimeMark? = null

    fun update(
        topPosition: Offset,
        bottomPosition: Offset,
        boutInProgress: Boolean,
    ) {
        if (!boutInProgress) {
            reset()
            return
        }

        val elapsedSeconds = lastUpdateTime
            ?.elapsedNow()
            ?.inWholeMilliseconds
            ?.div(1_000f)
            ?: 0f
        lastUpdateTime = TimeSource.Monotonic.markNow()

        val stationaryTop = stationaryTopPosition
        val stationaryBottom = stationaryBottomPosition
        if (stationaryTop == null || stationaryBottom == null) {
            startStationaryPeriod(topPosition, bottomPosition)
            return
        }

        if (
            distanceBetween(topPosition, stationaryTop) > MOVEMENT_THRESHOLD_PX ||
            distanceBetween(bottomPosition, stationaryBottom) > MOVEMENT_THRESHOLD_PX
        ) {
            startStationaryPeriod(topPosition, bottomPosition)
            return
        }

        timeUntilNextHakkeyoi -= elapsedSeconds

        if (timeUntilNextHakkeyoi > 0f) return

        playHakkeyoi()
        timeUntilNextHakkeyoi = randomHakkeyoiInterval()
    }

    private fun startStationaryPeriod(topPosition: Offset, bottomPosition: Offset) {
        stationaryTopPosition = topPosition
        stationaryBottomPosition = bottomPosition
        timeUntilNextHakkeyoi = randomHakkeyoiInterval()
    }

    fun reset() {
        stationaryTopPosition = null
        stationaryBottomPosition = null
        timeUntilNextHakkeyoi = Float.POSITIVE_INFINITY
        lastUpdateTime = null
    }

    private fun randomHakkeyoiInterval(): Float =
        MIN_HAKKEYOI_INTERVAL_SECONDS +
            Random.nextFloat() * (MAX_HAKKEYOI_INTERVAL_SECONDS - MIN_HAKKEYOI_INTERVAL_SECONDS)

    private fun distanceBetween(first: Offset, second: Offset): Float {
        val x = first.x - second.x
        val y = first.y - second.y
        return sqrt(x * x + y * y)
    }

    private companion object {
        // Game-world coordinates are pixels, so this is the five-pixel stationary tolerance.
        const val MOVEMENT_THRESHOLD_PX = 5f
        const val MIN_HAKKEYOI_INTERVAL_SECONDS = 3f
        const val MAX_HAKKEYOI_INTERVAL_SECONDS = 10f
    }
}
