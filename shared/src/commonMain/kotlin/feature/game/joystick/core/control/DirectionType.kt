package feature.game.joystick.core.control

import feature.game.joystick.extensions.firstOrdinal
import kotlin.jvm.JvmStatic

/**
 * The type that will determine how many directions the control will be able to return.
 */
enum class DirectionType {
    /**
     * Determines that the joystick will be able to return all the entries of [Direction] enum.
     */
    Complete,

    /**
     * Determines that the joystick will only be able to return 5 directions.
     *
     * [Direction.Right], [Direction.Down], [Direction.Left], [Direction.Up] and [Direction.None]
     */
    Simple;

    companion object {
        /**
         * @param id The id for the enum value
         * @return The enum value for the given id. If not found, returns the value [Complete].
         */
        @JvmStatic
        fun fromId(id: Int): DirectionType {
            return entries.firstOrdinal(id, Complete)
        }
    }
}

/**
 * The type that will determine how many quadrants will be available in the joystick.
 */
enum class MaxQuadrants {
    /**
     * Determines that there will be four quadrants in the joystick.
     */
    Four,

    /**
     * Determines that there will be eight quadrants in the joystick.
     */
    Eight;

    /**
     * Converts to its integer representation of the number of quadrants.
     */
    fun toInt(): Int {
        return if (this == Four) 4
        else 8
    }
}