package feature.game.joystick

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import feature.game.joystick.core.control.BackgroundType
import feature.game.joystick.core.control.DirectionType
import feature.game.joystick.ui.scope.draw.shapes.ArcDrawDefaults
import feature.game.joystick.ui.scope.draw.shapes.DrawMode
import feature.game.joystick.ui.scope.draw.shapes.drawArc
import feature.game.joystick.ui.state.JoystickMoveListener
import feature.game.joystick.ui.state.JoystickState
import feature.game.joystick.ui.view.VirtualJoystick
import feature.game.joystick.ui.view.rememberJoystickState

/**
 * A joystick composable tailored for controlling a Rikishi in the Sumo game.
 *
 * Uses an Arc drawer with [DirectionType.Complete] and [DrawMode.Normal], coloured to match
 * the owning player's theme (blue for top player, red for bottom player).
 *
 * @param modifier Modifier applied to the joystick.
 * @param state The joystick state. Defaults to a new [JoystickState] with [DirectionType.Complete].
 * @param primaryColor The primary (arc) colour, matched to the player's Rikishi / health-bar colour.
 * @param accentColor The accent colour used for the arc gradient highlight.
 * @param onMoveStart Optional callback invoked once when the joystick is first touched.
 * @param onMove Callback invoked while the joystick is moved or held.
 * @param onMoveEnd Optional callback invoked when the joystick is released.
 */
@Composable
fun RikishiJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(
        directionType = DirectionType.Complete,
    ),
    primaryColor: Color,
    accentColor: Color,
    onMoveStart: JoystickMoveListener? = null,
    onMove: JoystickMoveListener,
    onMoveEnd: JoystickMoveListener? = null,
) {
    val arcProperties = ArcDrawDefaults.properties(
        brush = { position, radius ->
            if (primaryColor == accentColor) {
                SolidColor(primaryColor)
            } else {
                androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(primaryColor, accentColor),
                    center = position,
                    radius = radius,
                )
            }
        },
        mode = DrawMode.Normal,
    )

    VirtualJoystick(
        modifier = modifier,
        state = state,
        backgroundType = BackgroundType.Default,
        onMoveStart = onMoveStart,
        onMove = onMove,
        onMoveEnd = onMoveEnd,
    ) {
        drawArc(arcProperties)
    }
}
