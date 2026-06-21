package feature.game.presentation.model

import feature.common.model.Position
import feature.game.presentation.ThumbState
import feature.game.presentation.ThumbState.RELEASED

data class Player(
    val id: String,
    val position: Position?,
    val thumbState: ThumbState = RELEASED,
    val health: Int = 100,
)