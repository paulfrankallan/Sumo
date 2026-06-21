package feature.game.domain.usecase

import feature.common.model.Position
import feature.game.presentation.GameState
import feature.game.presentation.ThumbState
import feature.game.presentation.model.Player

class UpdatePlayState {
    operator fun invoke(gameState: GameState, player: Player, isPressed: Boolean): GameState {
        return updateThumbState(gameState, player, isPressed)
    }

    private fun updateThumbState(
        gameState: GameState,
        player: Player,
        isPressed: Boolean
    ): GameState {
        return gameState.copy(
            topPlayer = if (player.position == Position.TOP) {
                gameState.topPlayer.copy(
                    thumbState = if (isPressed) ThumbState.PRESSED else ThumbState.RELEASED
                )
            } else {
                gameState.topPlayer
            },
            bottomPlayer = if (player.position == Position.BOTTOM) {
                gameState.bottomPlayer.copy(
                    thumbState = if (isPressed) ThumbState.PRESSED else ThumbState.RELEASED
                )
            } else {
                gameState.bottomPlayer
            }
        )
    }
}