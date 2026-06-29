package feature.game.domain.model

/**
 * The complete physics world state for one frame of the game.
 * Immutable — a new instance is produced each physics tick.
 */
data class GameWorld(
    val arena: ArenaWorld,
    val topRikishi: RikishiBody,
    val bottomRikishi: RikishiBody,
)
