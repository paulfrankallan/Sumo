package feature.game.joystick.ui.tokens

import feature.game.joystick.core.control.BackgroundType
import feature.game.joystick.core.control.DirectionType
import feature.game.joystick.core.geometry.Radius

/**
 * Provides default values for the virtual joystick configuration.
 */
object JoystickDefaults {
    /**
     * The default interval in milliseconds for joystick hold updates.
     */
    val interval: Long
        get() = 175L

    /**
     * The default radius of the joystick `dead zone`.
     */
    val radius: Radius
        get() = Radius.Ratio(0.2f)

    /**
     * The default direction type for the joystick.
     */
    val directionType
        get() = DirectionType.Complete

    /**
     * The default background type for the joystick.
     */
    val backgroundType: BackgroundType
        get() = BackgroundType.Default
}