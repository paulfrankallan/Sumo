package feature.game.domain.input

import androidx.compose.ui.geometry.Offset

/**
 * A unified input command produced by any input source (joystick, direct drag, future: AI/network).
 * The physics engine only sees velocity vectors — it never knows the input source.
 *
 * @param playerId  The ID of the [feature.game.domain.model.RikishiBody] this command targets.
 * @param velocityVector  Desired movement delta for this tick (pixels). Already scaled by
 *                        speed and dt by the input mapper — physics applies it directly.
 * @param source    The origin of this command, for debugging and future game-mode logic.
 */
data class InputCommand(
    val playerId: String,
    val velocityVector: Offset,
    val source: InputSource,
)

enum class InputSource {
    JOYSTICK,
    DIRECT_DRAG,
}
