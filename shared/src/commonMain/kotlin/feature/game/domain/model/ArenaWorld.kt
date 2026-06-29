package feature.game.domain.model

import androidx.compose.ui.geometry.Offset

/**
 * The circular arena (Dohyo) in which Rikishi compete.
 * A Rikishi is out-of-bounds when the edge of its circle touches or crosses [radius].
 */
data class ArenaWorld(
    val centre: Offset,
    val radius: Float,
)
