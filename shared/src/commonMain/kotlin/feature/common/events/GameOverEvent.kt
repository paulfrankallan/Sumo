package feature.common.events

import feature.game.domain.model.GameOverResult

class GameOverEvent(
    override val id: String,
    val result: GameOverResult
) : Event(id)