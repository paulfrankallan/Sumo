package feature.game.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import feature.common.events.Event
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.common.presentation.ViewState
import feature.game.domain.model.GameOverResult
import feature.game.domain.model.StartCountdownViewState
import feature.game.presentation.model.Player
import org.jetbrains.compose.resources.DrawableResource
import app.theme.playerOneColor
import app.theme.playerTwoColor
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.rikishi_blue
import sumo.shared.generated.resources.rikishi_red

data class GameState(
    val navigateTo: NavigationEvent? = null,
    val events: List<Event> = emptyList(),
    val gameId: String,
    val topPlayer: Player,
    val bottomPlayer: Player,
    val gameOverResult: GameOverResult? = null,
    val playState: PlayState = PlayState.NEW,
    val startCountdownViewState: StartCountdownViewState? = null,
    val ui: UI = UI(),
    // Render positions from the game loop — null until the arena is measured.
    val topRikishiPosition: Offset? = null,
    val bottomRikishiPosition: Offset? = null,
    val arenaCentre: Offset? = null,
    val arenaRadius: Float? = null,
    val rikishiRadius: Float? = null,
) : ViewState {
    val isGameOver: Boolean
        get() = gameOverResult != null
}

data class UI(
    val topThumbView: ThumbViewState = ThumbViewState(
        foregroundColor = null,
        foregroundImage = Res.drawable.rikishi_blue,
    ),
    val bottomThumbView: ThumbViewState = ThumbViewState(
        foregroundColor = null,
        foregroundImage = Res.drawable.rikishi_red,
    ),
)

data class ThumbViewState(
    val foregroundColor: Color?,
    val foregroundImage: DrawableResource,
)

enum class ThumbState {
    PRESSED,
    RELEASED
}

enum class PlayState {
    NEW,
    IN_PROGRESS,
    FINISHED
}

sealed class GameIntent : Intent {
    data object StartGame : GameIntent()
    data class GameOver(val result: GameOverResult? = null) : GameIntent()
    data class PlayerDamaged(val player: Player) : GameIntent()
    data class PressStateChanged(val isPressed: Boolean, val player: Player) : GameIntent()
    data object ResetThumbsComplete : GameIntent()
    data class DragEnded(val player: Player) : GameIntent()
    /** Arena dimensions measured by the UI — initialises the game loop world. */
    data class ArenaMeasured(
        val centre: Offset,
        val arenaRadius: Float,
        val rikishiRadius: Float,
    ) : GameIntent()
}