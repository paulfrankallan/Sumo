package feature.game.joystick.core.geometry


/**
 * A radius value for the joystick.
 *
 * It can be either a fixed value or a ratio of the available space.
 */
sealed class Radius(
    val value: Float,
) {
    /**
     * Represents a fixed radius value.
     */
    class Fixed internal constructor(radius: Float) : Radius(radius) {
        companion object {
            /**
             * Creates a Fixed radius with validation
             * @param radius Must be positive
             * @throws IllegalArgumentException if radius is not positive
             */
            operator fun invoke(radius: Float): Fixed {
                require(radius > 0) { "Fixed radius must be positive, got $radius" }
                return Fixed(radius)
            }
        }
    }

    /**
     * Represents a radius that is a ratio of the available space.
     * @param ratio The ratio value, must be in the range (0.0, 1.0).
     */
    class Ratio internal constructor(
        ratio: Float,
    ) : Radius(ratio) {
        companion object {
            /**
             * Creates a Ratio radius with validation
             * @param ratio Must be in range (0.0, 1.0)
             * @throws IllegalArgumentException if ratio is not in valid range
             */
            operator fun invoke(ratio: Float): Ratio {
                require(ratio > 0 && ratio < 1f) {
                    "Ratio radius must be in range (0.0, 1.0), got $ratio"
                }
                return Ratio(ratio)
            }
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Radius) return false
        return value == other.value
    }

    override fun hashCode(): Int {
        return 31 * value.hashCode()
    }
}