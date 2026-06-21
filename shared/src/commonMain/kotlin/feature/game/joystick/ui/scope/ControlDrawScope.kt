package feature.game.joystick.ui.scope

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * A scope that provides access to control operations and drawing capabilities.
 */
@Stable
@Immutable
interface ControlDrawScope : ControlScope {
    /**
     * The [DrawScope] for performing drawing operations.
     */
    val drawScope: DrawScope
}

/**
 * Internal implementation of [ControlDrawScope] that delegates control scope operations
 * and provides drawing capabilities.
 *
 * @param scope The control scope to delegate to
 * @param drawScope The draw scope for drawing operations
 */
@Stable
@Immutable
internal class ControlDrawScopeImpl(
    private val scope: ControlScope,
    override val drawScope: DrawScope,
) : ControlScope by scope, ControlDrawScope

