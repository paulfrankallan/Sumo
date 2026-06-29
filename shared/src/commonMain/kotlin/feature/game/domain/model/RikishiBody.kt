package feature.game.domain.model

import androidx.compose.ui.geometry.Offset

/**
 * Represents the physics state of a single Rikishi in the game world.
 * Centre-based coordinate system — [position] is the centre of the circle.
 */
data class RikishiBody(
    val id: String,
    val position: Offset,
    val radius: Float,
    val velocity: Offset = Offset.Zero,
)
