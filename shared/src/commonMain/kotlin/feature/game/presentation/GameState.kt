package feature.game.presentation

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import feature.common.events.Event
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.common.presentation.ViewState
import feature.game.domain.input.PowerMove
import feature.game.domain.model.GameOverResult
import feature.game.presentation.model.Player
import org.jetbrains.compose.resources.DrawableResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.rikishi_blue_standing
import sumo.shared.generated.resources.rikishi_red_standing

data class GameState(
    val navigateTo: NavigationEvent? = null,
    val events: List<Event> = emptyList(),
    val gameId: String,
    val topPlayer: Player,
    val bottomPlayer: Player,
    val gameOverResult: GameOverResult? = null,
    val playState: PlayState = PlayState.NEW,
    val isStartCeremonyVisible: Boolean = false,
    val isTopPlayerReady: Boolean = false,
    val isBottomPlayerReady: Boolean = false,
    val ui: UI = UI(),
    // Render positions from the game loop — null until the arena is measured.
    val topRikishiPosition: Offset? = null,
    val bottomRikishiPosition: Offset? = null,
    val arenaCentre: Offset? = null,
    val arenaRadius: Float? = null,
    val rikishiRadius: Float? = null,
    val newRikishiTawaraInsetPx: Float? = null,
    /** Whether either player may claim the shared push power move. */
    val isPushPowerMoveEnabled: Boolean = false,
    /** Whether either player may claim the shared pull power move. */
    val isPullPowerMoveEnabled: Boolean = false,
) : ViewState {
    val isGameOver: Boolean
        get() = gameOverResult != null
}

data class UI(
    val topThumbView: ThumbViewState = ThumbViewState(
        foregroundColor = null,
        foregroundImage = Res.drawable.rikishi_blue_standing,
    ),
    val bottomThumbView: ThumbViewState = ThumbViewState(
        foregroundColor = null,
        foregroundImage = Res.drawable.rikishi_red_standing,
    ),
    val isTopPushing: Boolean = false,
    val isBottomPushing: Boolean = false,
    val isTopCrouching: Boolean = false,
    val isBottomCrouching: Boolean = false,
    val isTopWalking: Boolean = false,
    val isBottomWalking: Boolean = false,
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
    STARTING,
    IN_PROGRESS,
    DAMAGE_RESETTING,
    FINISHED
}

internal const val READY_BUTTON_TRANSITION_DURATION_MILLIS = 300

sealed class GameIntent : Intent {
    data object StartGame : GameIntent()
    data class PlayerReady(val playerId: String) : GameIntent()
    data object StopGame : GameIntent()
    data class GameOver(val result: GameOverResult? = null) : GameIntent()
    data class PlayerDamaged(val player: Player) : GameIntent()
    data class PressStateChanged(val isPressed: Boolean, val player: Player) : GameIntent()
    data object ResetThumbsComplete : GameIntent()
    data class DragEnded(val player: Player) : GameIntent()
    data class PowerMovePressed(
        val playerId: String,
        val powerMove: PowerMove,
    ) : GameIntent()
    /** Arena dimensions measured by the UI — initialises the game loop world. */
    data class ArenaMeasured(
        val centre: Offset,
        val arenaRadius: Float,
        val rikishiRadius: Float,
        val newRikishiTawaraInsetPx: Float,
    ) : GameIntent()
}

internal const val SHIKIRI_SEN_CENTER_OFFSET_RATIO = 0.16f
