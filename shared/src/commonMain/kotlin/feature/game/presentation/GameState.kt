package feature.game.presentation

import androidx.compose.ui.graphics.Color
import feature.common.events.Event
import feature.common.model.GameType
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
import sumo.shared.generated.resources.fingerprint

data class GameState(
    val navigateTo: NavigationEvent? = null,
    val events: List<Event> = emptyList(),
    val gameId: String,
    val gameType: GameType,
    val topPlayer: Player,
    val bottomPlayer: Player,
    val gameOverResult: GameOverResult? = null,
    val playState: PlayState = PlayState.NEW,
    val resettingKey: String? = null,
    val startCountdownViewState: StartCountdownViewState? = null,
    val ui: UI = UI()
) : ViewState {
    val isGameOver: Boolean
        get() = gameOverResult != null
}

data class UI(
    val topActionPanel: ActionPanelState = ActionPanelState(),
    val topThumbView: ThumbViewState = ThumbViewState(
        foregroundColor = playerOneColor,
    ),
    val bottomActionPanel: ActionPanelState = ActionPanelState(),
    val bottomThumbView: ThumbViewState = ThumbViewState(
        foregroundColor = playerTwoColor,
    ),
)

data class ThumbViewState(
    val foregroundColor: Color?,
    val foregroundImage: DrawableResource = Res.drawable.fingerprint,
)

data class ActionPanelState(
    val actionPanelType: ActionPanelType = ActionPanelType.START_GAME_UI,
)


enum class ActionPanelType {
    NONE,
    START_GAME_UI,
    GAME_OVER_UI,
    THUMB_PRINT_UI,
}

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
    data class ThumbActionButtonClicked(val player: Player, val isKey: Boolean?) : GameIntent()
    data class LockThumbs(val player: Player? = null) : GameIntent()
    data object ResetThumbs : GameIntent()
    data object ResetThumbsComplete : GameIntent()
}