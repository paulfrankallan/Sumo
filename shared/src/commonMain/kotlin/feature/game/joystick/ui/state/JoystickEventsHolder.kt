package feature.game.joystick.ui.state

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Holder for joystick events, managing a shared flow of events.
 */
class JoystickEventsHolder(
    val state: JoystickState,
) {
    private val _events = MutableSharedFlow<JoystickEvent>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Public shared flow exposing the joystick events.
     */
    val events: SharedFlow<JoystickEvent>
        get() = _events.asSharedFlow()


    /**
     * Emits a start event to the shared flow.
     */
    suspend fun emitStart() {
        _events.emit(state.toStartEvent())
    }

    /**
     * Emits a move event to the shared flow.
     */
    suspend fun emitMove() {
        _events.emit(state.toMoveEvent())
    }


    /**
     * Emits an end event to the shared flow.
     */
    suspend fun emitEnd() {
        _events.emit(state.toEndEvent())
    }

    /**
     * Emits a held event to the shared flow.
     */
    suspend fun emitHeld() {
        _events.emit(state.toHeldEvent())
    }

    /**
     * Attempts to emit a start event to the shared flow without suspending.
     */
    fun tryEmitStart() {
        _events.tryEmit(state.toStartEvent())
    }

    /**
     * Attempts to emit a move event to the shared flow without suspending.
     */
    fun tryEmitMove() {
        _events.tryEmit(state.toMoveEvent())
    }

    /**
     * Attempts to emit an end event to the shared flow without suspending.
     */
    fun tryEmitEnd() {
        _events.tryEmit(state.toEndEvent())
    }

    /**
     * Attempts to emit a held event to the shared flow without suspending.
     */
    fun tryEmitHeld() {
        _events.tryEmit(state.toHeldEvent())
    }
}