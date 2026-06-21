package feature.game.joystick.ui.view

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.graphicsLayer
import feature.game.joystick.ui.scope.ControlScope
import feature.game.joystick.ui.scope.ControlDrawScope
import feature.game.joystick.ui.scope.ControlDrawScopeImpl

/**
 * A composable canvas for drawing joystick visualizations.
 *
 * Provides a drawing surface that integrates with the control operations through [ControlDrawScope],
 * allowing rendering of joystick components like knobs, arcs, circles, and directional indicators.
 *
 * This composable uses a cached drawing approach for performance, automatically redrawing when
 * the joystick state changes. The content receives a [ControlDrawScope] that provides
 * access to both the control operations and drawing capabilities.
 *
 * @param modifier The modifier to be applied to the canvas.
 * @param content The drawing content for rendering.
 * This is invoked during the drawing phase and should contain drawing operations.
 */
@Composable
fun ControlScope.JoystickCanvas(
    modifier: Modifier = Modifier,
    content: (ControlDrawScope.() -> Unit),
) {
    Spacer(
        modifier = modifier.fillMaxSize()
            .graphicsLayer()
            .drawWithCache {
                onDrawBehind {
                    val scope = ControlDrawScopeImpl(this@JoystickCanvas, this)
                    content.invoke(scope)
                }
            }
    )
}