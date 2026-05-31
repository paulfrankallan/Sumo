package feature.game.nav

import androidx.navigation.NavController

const val PLAY_GAME_ROUTE = "play_game_route"

fun NavController.navigateToPlayGame(
) {
    navigate(PLAY_GAME_ROUTE)
}
