package feature.game.joystick.core.control

import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic

/**
 * The possible directions of the joystick.
 */
enum class Direction(val quadrant: Int) {
    Right(1),
    DownRight(2),
    Down(3),
    DownLeft(4),
    Left(5),
    UpLeft(6),
    Up(7),
    UpRight(8),
    None(0);

    companion object {
        /**
         * Converts a quadrant number to the corresponding Direction.
         *
         * @param quadrant The quadrant number (1-8 for [MaxQuadrants.Eight] quadrants, 1-4 for [MaxQuadrants.Four]).
         * @param quadrantType The type of quadrants, default is [MaxQuadrants.Eight].
         */
        @JvmStatic
        @JvmOverloads
        fun fromQuadrant(
            quadrant: Int,
            quadrantType: MaxQuadrants = MaxQuadrants.Eight,
        ): Direction {
            if (quadrantType == MaxQuadrants.Eight)
                return when (quadrant) {
                    1 -> Right
                    2 -> DownRight
                    3 -> Down
                    4 -> DownLeft
                    5 -> Left
                    6 -> UpLeft
                    7 -> Up
                    8 -> UpRight
                    else -> None
                }
            return when (quadrant) {
                1 -> Right
                2 -> Down
                3 -> Left
                4 -> Up
                else -> None
            }
        }
    }

}

/**
 * Converts the [Direction] to the corresponding quadrant number.
 *
 * [MaxQuadrants.Eight] quadrants include diagonal directions, while [MaxQuadrants.Four] quadrants map directly opposite directions.
 *
 * @param quadrantType The type of quadrants, determines the mapping of directions to quadrants.
 */
fun Direction.toQuadrant(quadrantType: MaxQuadrants): Int {
    if (quadrantType == MaxQuadrants.Eight)
        return quadrant

    return when (this) {
        Direction.Right -> 1
        Direction.Down, Direction.DownRight, Direction.DownLeft -> 2
        Direction.Left -> 3
        Direction.Up, Direction.UpRight, Direction.UpLeft -> 4
        else -> 0
    }
}

/**
 * Converts the [Direction] to the corresponding quadrant number based on the direction type.
 *
 * @param directionType The type of direction, determines whether to use complete or simple quadrants.
 */
fun Direction.toQuadrant(directionType: DirectionType): Int {
    val type = if (directionType == DirectionType.Complete)
        MaxQuadrants.Eight
    else MaxQuadrants.Four

    return toQuadrant(type)
}

/**
 * Clamps the [Direction] to a simpler form if necessary.
 *
 * @param type The type of direction, determines whether to use complete or simple quadrants.
 */
fun Direction.clamp(type: DirectionType): Direction {
    if (type == DirectionType.Simple)
        return when (this) {
            Direction.Up, Direction.UpRight, Direction.UpLeft -> Direction.Up
            Direction.Down, Direction.DownRight, Direction.DownLeft -> Direction.Down
            else -> this
        }
    return this
}
