package feature.game.joystick

import app.theme.playerOneColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import feature.game.joystick.core.control.DirectionType
import feature.game.joystick.ui.scope.draw.shapes.ArcDrawDefaults
import feature.game.joystick.ui.scope.draw.shapes.DrawMode
import feature.game.joystick.ui.scope.draw.shapes.drawArc
import feature.game.joystick.ui.state.JoystickMoveListener
import feature.game.joystick.ui.state.JoystickState
import feature.game.joystick.ui.view.BaseVirtualJoystick
import feature.game.joystick.ui.view.JoystickCanvas
import feature.game.joystick.ui.view.rememberJoystickState
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.controller
import sumo.shared.generated.resources.golden_overlay
import sumo.shared.generated.resources.pull_btn
import sumo.shared.generated.resources.push_btn
import sumo.shared.generated.resources.throw_btn
import sumo.shared.generated.resources.thrust_btn

/**
 * A joystick composable tailored for controlling a Rikishi in the Sumo game.
 *
 * Uses the controller.png asset as the background, with an Arc drawn on top to
 * indicate joystick direction. The top-player instance is rotated 180° at the call
 * site (see [feature.game.presentation.GameScreen]) so it faces the correct direction.
 * Pull and push controls are positioned on either side of the joystick.
 *
 * @param modifier Modifier applied to the joystick and action controls.
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

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        val joystickSize = minOf(
            maxHeight,
            (maxWidth - JOYSTICK_ACTIONS_WIDTH).coerceAtLeast(0.dp),
        )

        Row(
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                JoystickActionButton(Res.drawable.pull_btn)
                JoystickActionButton(Res.drawable.thrust_btn)
            }
            //Spacer(modifier = Modifier.width(JOYSTICK_ACTION_BUTTON_SPACING))
            BaseVirtualJoystick(
                modifier = Modifier.size(joystickSize),
                state = state,
                onMoveStart = onMoveStart,
                onMove = onMove,
                onMoveEnd = onMoveEnd,
            ) {
                Image(
                    painter = painterResource(Res.drawable.controller),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
                JoystickCanvas {
                    drawArc(arcProperties)
                }
            }
            //Spacer(modifier = Modifier.width(JOYSTICK_ACTION_BUTTON_SPACING))
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                JoystickActionButton(Res.drawable.push_btn)
                JoystickActionButton(Res.drawable.throw_btn)
            }
        }
    }
}

@Preview
@Composable
private fun RikishiJoystickPreview() {
    RikishiJoystick(
        modifier = Modifier.size(width = 392.dp, height = 200.dp),
        primaryColor = playerOneColor,
        accentColor = playerOneColor,
        onMove = {},
    )
}

@Composable
private fun JoystickActionButton(buttonImage: DrawableResource) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .size(JOYSTICK_ACTION_BUTTON_SIZE)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = {},
            ),
    ) {
        Image(
            painter = painterResource(buttonImage),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
        )
        if (isPressed) {
            Image(
                painter = painterResource(Res.drawable.golden_overlay),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

private val JOYSTICK_ACTION_BUTTON_SIZE = 64.dp
private val JOYSTICK_ACTION_BUTTON_SPACING = 12.dp
private val JOYSTICK_ACTIONS_WIDTH =
    (JOYSTICK_ACTION_BUTTON_SIZE + JOYSTICK_ACTION_BUTTON_SPACING) * 2
