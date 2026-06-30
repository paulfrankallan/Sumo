package feature.game.domain.engine

import feature.game.domain.input.InputCommand
import feature.game.domain.input.InputSource
import feature.game.domain.model.GameWorld
import feature.game.domain.physics.PhysicsEngine
import feature.game.domain.physics.PhysicsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.TimeSource

/**
 * The central game loop. Runs a fixed-timestep coroutine that:
 * 1. Drains pending [InputCommand]s from both input sources.
 * 2. Advances the [PhysicsEngine] by one tick.
 * 3. Emits the new [GameWorld] state and any [PhysicsEvent]s.
 *
 * Ownership: instantiated as a singleton by DI and injected into [GameViewModel].
 * The ViewModel controls start/stop/reset; composables only submit input.
 */
class GameLoop(
    private val physics: PhysicsEngine = PhysicsEngine,
    private val tickMs: Long = TICK_MS,
) {
    private val _worldState = MutableStateFlow<GameWorld?>(null)

    /** Latest physics world state. Null until [start] is called. */
    val worldState: StateFlow<GameWorld?> = _worldState.asStateFlow()

    private val _physicsEvents = MutableSharedFlow<PhysicsEvent>(extraBufferCapacity = 64)

    /** Stream of physics events (boundary violations, collisions). */
    val physicsEvents: SharedFlow<PhysicsEvent> = _physicsEvents.asSharedFlow()

    // Unlimited capacity so input producers never block or drop commands.
    private val inputChannel = Channel<InputCommand>(capacity = Channel.UNLIMITED)

    private var loopJob: Job? = null

    // Player IDs whose DIRECT_DRAG commands should be discarded (set during damage reset,
    // cleared when the drag gesture ends so the position can't be pushed back immediately).
    private val dragBlockedPlayers = mutableSetOf<String>()

    /** Submit an input command from any source. Non-blocking, thread-safe. */
    fun submitInput(command: InputCommand) {
        if (command.source == InputSource.DIRECT_DRAG && command.playerId in dragBlockedPlayers) return
        inputChannel.trySend(command)
    }

    /**
     * Starts the game loop with [initialWorld] as the starting state.
     * If a loop is already running it is cancelled first.
     */
    fun start(initialWorld: GameWorld, scope: CoroutineScope) {
        loopJob?.cancel()
        dragBlockedPlayers.clear()
        _worldState.value = initialWorld
        loopJob = scope.launch {
            var world = initialWorld
            val timeSource = TimeSource.Monotonic
            while (isActive) {
                _worldState.value?.let { world = it }
                val frameStart = timeSource.markNow()
                val commands = drainInputChannel()
                val result = physics.step(world, commands)
                world = result.world
                _worldState.value = world
                result.events.forEach { _physicsEvents.tryEmit(it) }
                val elapsedMs = frameStart.elapsedNow().inWholeMilliseconds
                val remaining = tickMs - elapsedMs
                if (remaining > 0) kotlinx.coroutines.delay(remaining)
            }
        }
    }

    /** Stops the loop. State is preserved. */
    fun stop() {
        loopJob?.cancel()
        loopJob = null
    }

    /**
     * Resets positions to [newWorld] without stopping — use between rounds.
     * Any in-flight input commands are discarded.
     */
    fun reset(newWorld: GameWorld) {
        drainInputChannel() // discard stale commands
        _worldState.value = newWorld
    }

    /**
     * Prevents DIRECT_DRAG commands for [playerId] from reaching the physics engine.
     * Call after a damage-reset when the player was actively dragging, so the in-flight
     * drag gesture can't push the Rikishi straight back to the boundary.
     */
    fun blockDragForPlayer(playerId: String) {
        dragBlockedPlayers.add(playerId)
    }

    /** Lifts the drag block set by [blockDragForPlayer]. Call when the drag gesture ends. */
    fun unblockDragForPlayer(playerId: String) {
        dragBlockedPlayers.remove(playerId)
    }

    /** Drains all currently-queued input commands without suspending. */
    private fun drainInputChannel(): List<InputCommand> {
        val commands = mutableListOf<InputCommand>()
        while (true) {
            val cmd = inputChannel.tryReceive().getOrNull() ?: break
            commands += cmd
        }
        return commands
    }

    companion object {
        const val TICK_MS = 16L  // ~60 Hz
    }
}
