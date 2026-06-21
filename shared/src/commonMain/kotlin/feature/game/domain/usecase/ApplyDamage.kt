package feature.game.domain.usecase

import feature.common.events.GameOverEvent
import feature.common.model.Position
import feature.game.domain.model.GameOverResult
import feature.game.presentation.GameState
import feature.game.presentation.PlayState
import feature.game.presentation.model.Player
import platform.randomUUID

private const val DEFAULT_DAMAGE_AMOUNT = 10

class ApplyDamage {
    operator fun invoke(gameState: GameState, player: Player): GameState {
        return when (player.position) {
            Position.TOP -> {
                val newHealth = gameState.topPlayer.health - DEFAULT_DAMAGE_AMOUNT
                val gameOverResult = GameOverResult(winner = gameState.bottomPlayer).takeIf {
                    newHealth <= 0
                }
                gameState.copy(
                    topPlayer = gameState.topPlayer.copy(
                        health = newHealth
                    ),
                    gameOverResult = gameOverResult,
                    playState = if(gameOverResult == null) PlayState.IN_PROGRESS else PlayState.FINISHED,
                    events = if (gameOverResult == null) {
                        gameState.events
                    } else {
                        gameState.events + GameOverEvent(id = randomUUID(), result = gameOverResult)
                    }
                )
            }

            Position.BOTTOM -> {
                val newHealth = gameState.bottomPlayer.health - DEFAULT_DAMAGE_AMOUNT
                val gameOverResult = GameOverResult(winner = gameState.topPlayer).takeIf {
                    newHealth <= 0
                }
                gameState.copy(
                    bottomPlayer = gameState.bottomPlayer.copy(
                        health = newHealth
                    ),
                    gameOverResult = gameOverResult,
                    playState = if(gameOverResult == null) PlayState.IN_PROGRESS else PlayState.FINISHED,
                    events = if (gameOverResult == null) {
                        gameState.events
                    } else {
                        gameState.events + GameOverEvent(id = randomUUID(), result = gameOverResult)
                    }
                )
            }

            else -> throw IllegalArgumentException("Player not found")
        }
    }
}