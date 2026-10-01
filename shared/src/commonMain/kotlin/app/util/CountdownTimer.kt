package app.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class CountUpTimer {
    private var job: Job? = null

    operator fun invoke(
        startDelayMillis: Long = 0,
        start: Int,
        end: Int,
        tickDurationSeconds: Int = 1,
        onStarted: () -> Unit = {},
        onTick: (Int) -> Unit,
        onComplete: () -> Unit,
        cancelExisting: Boolean = true,
    ): Job? {
        if (cancelExisting) {
            job?.cancel()
        }

        job = CoroutineScope(Dispatchers.Default).launch {
            onStarted()
            delay(startDelayMillis)
            var currentCount = start
            while (currentCount <= end) {
                onTick(currentCount)
                delay(tickDurationSeconds.seconds.inWholeMilliseconds)
                currentCount++
            }
            onComplete()
        }

        return job
    }
    fun clear() {
        job?.cancel()
        job = null
    }
}
