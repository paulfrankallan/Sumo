package feature.game.joystick.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import feature.game.joystick.core.control.BackgroundType
import feature.game.joystick.ui.icons.JoystickIcons.Background
import feature.game.joystick.ui.icons.background.Classic
import feature.game.joystick.ui.icons.background.Modern
import feature.game.joystick.ui.icons.background.Default
import feature.game.joystick.ui.icons.background.DpadClassic
import feature.game.joystick.ui.icons.background.DpadModern
import feature.game.joystick.ui.icons.background.DpadStandard
import feature.game.joystick.ui.tokens.JoystickDefaults

/**
 * Displays the background image for a virtual joystick.
 *
 * @param modifier The modifier to be applied to the background image
 * @param type The type of background style to display
 * @param contentDescription The content description for accessibility
 */
@Composable
fun JoystickBackground(
    modifier: Modifier = Modifier,
    type: BackgroundType = JoystickDefaults.backgroundType,
    contentDescription: String? = "Virtual Joystick Background",
) {
    Image(
        modifier = modifier
            .fillMaxSize(),
        imageVector = when (type) {
            BackgroundType.Modern -> Background.Modern
            BackgroundType.Classic -> Background.Classic
            BackgroundType.DpadModern -> Background.DpadModern
            BackgroundType.DpadStandard -> Background.DpadStandard
            BackgroundType.DpadClassic -> Background.DpadClassic
            else -> Background.Default
        },
        contentDescription = contentDescription
    )
}