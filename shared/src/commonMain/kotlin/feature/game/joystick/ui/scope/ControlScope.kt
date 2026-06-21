package feature.game.joystick.ui.scope

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import feature.game.joystick.ui.state.JoystickState

/**
 * A scope that provides access to control operations.
 */
@Stable
@Immutable
interface ControlScope {
    /**
     * The current state of the joystick.
     */
    val state: JoystickState
}

/**
 * Internal implementation of [ControlScope].
 *
 * @param state The joystick state to provide access to
 */
@Stable
@Immutable
internal class ControlScopeImpl(
    override val state: JoystickState,
) : ControlScope

/**
 * Creates and remembers a [ControlScope] for the given joystick state.
 *
 * @param state The joystick state to create the scope for
 */
@Composable
internal fun rememberControlScope(
    state: JoystickState,
): ControlScope = remember(state) {
    ControlScopeImpl(state)
}
