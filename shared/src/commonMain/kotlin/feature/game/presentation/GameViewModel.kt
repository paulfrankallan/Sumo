package feature.game.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import app.sound.SoundAndVibrationFeedback
import app.theme.AppColor
import app.util.CountUpTimer
import feature.common.events.GameOverEvent
import feature.common.model.Position
import feature.common.presentation.CMViewModel
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.game.domain.model.StartCountdownViewState
import feature.game.domain.usecase.ApplyDamage
import feature.game.domain.usecase.UpdatePlayState
import feature.game.presentation.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import platform.RES_ID_MUSIC_3
import platform.randomUUID
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.rikishi_blue
import sumo.shared.generated.resources.rikishi_red
import sumo.shared.generated.resources.loser
import sumo.shared.generated.resources.winner

class GameViewModel(
    private val applyDamage: ApplyDamage,
    private val updatePlayState: UpdatePlayState,
    private val soundAndVibration: SoundAndVibrationFeedback,
) : CMViewModel<GameState, Intent>() {
    private var gameId: String? = null
    private var startGameCountdownTimerJob: Job? = null
    private val isResettingAfterDamage = mutableStateOf(false)
    private val _resetThumbPositions = mutableStateOf(false)
    val resetThumbPositions: State<Boolean> = _resetThumbPositions

    private fun triggerResetThumbPositions() {
        _resetThumbPositions.value = !_resetThumbPositions.value
    }

    init {
        scope.launch {
            // distinctUntilChangedBy events prevents re-processing the same event list
            // after onEventComplete removes an entry and re-emits an otherwise identical state.
            _state.distinctUntilChangedBy { it.events }
                .collect { gameState ->
                    gameState.events.forEach { event ->
                        when (event) {
                            is GameOverEvent -> {
                                onIntent(GameIntent.GameOver(event.result))
                                onEventComplete(event.id)
                            }
                        }
                    }
                }
        }
    }

    override fun initialViewState(): GameState {
        return GameState(
            gameId = randomUUID(),
            topPlayer = Player(id = randomUUID(), position = Position.TOP),
            bottomPlayer = Player(id = randomUUID(), position = Position.BOTTOM),
        )
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            GameIntent.StartGame -> {
                // Ignore rapid duplicate taps: only allow starting when not already mid-game.
                if (state.value.playState == PlayState.IN_PROGRESS && !state.value.isGameOver) return
                startGameCountdownTimerJob?.cancel()
                _state.update { state ->
                    state.copy(
                        gameId = randomUUID(),
                        gameOverResult = null,
                        topPlayer = Player(id = randomUUID(), position = Position.TOP),
                        bottomPlayer = Player(id = randomUUID(), position = Position.BOTTOM),
                        ui = UI()
                    )
                }
                invokeGameStartCountdownTimer()
            }

            is GameIntent.GameOver -> {
                _state.update { state ->
                    state.copy(
                        ui = state.ui.copy(
                            topThumbView = state.ui.topThumbView.copy(
                                foregroundColor = getThumbViewForegroundColor(state, state.topPlayer),
                                foregroundImage = getThumbViewForegroundImage(state, state.topPlayer),
                            ),
                            bottomThumbView = state.ui.bottomThumbView.copy(
                                foregroundColor = getThumbViewForegroundColor(state, state.bottomPlayer),
                                foregroundImage = getThumbViewForegroundImage(state, state.bottomPlayer),
                            ),
                        )
                    )
                }
                // Audio is I/O-bound — run off the Main thread to avoid blocking the UI.
                scope.launch(Dispatchers.Default) {
                    soundAndVibration.stopMusic(musicResourceId = RES_ID_MUSIC_3)
                }
                if (intent.result == null) return
                if (gameId == null || gameId != state.value.gameId) {
                    gameId = state.value.gameId
                    scope.launch(Dispatchers.Default) {
                        soundAndVibration.gameOverFeedback()
                    }
                }
            }

            is GameIntent.PlayerDamaged -> {
                if (isResettingAfterDamage.value.not()) {
                    isResettingAfterDamage.value = true
                    _state.update { state -> applyDamage(state, intent.player) }
                    triggerResetThumbPositions()
                    // Sound only when damage is actually applied — not on subsequent
                    // blocked calls that arrive during the reset window.
                    scope.launch(Dispatchers.Default) {
                        soundAndVibration.gameOverFeedback()
                    }
                }
            }

            is GameIntent.PressStateChanged -> {
                _state.update { state ->
                    updatePlayState(state, intent.player, intent.isPressed)
                }
            }

            is GameIntent.ResetThumbsComplete -> {
                isResettingAfterDamage.value = false
            }
        }
    }

    private fun getThumbViewForegroundColor(state: GameState, player: Player): Color? {
        return if (state.isGameOver) {
            null
        } else {
            if (player.health == 0) {
                AppColor.BLOOD_RED.color
            } else state.ui.topThumbView.foregroundColor
        }
    }

    private fun getThumbViewForegroundImage(state: GameState, player: Player): DrawableResource {
        return if(state.isGameOver) {
            if (player.health == 0) {
                Res.drawable.loser
            } else {
                Res.drawable.winner
            }
        } else {
            if (player.position == Position.TOP) Res.drawable.rikishi_blue else Res.drawable.rikishi_red
        }
    }

    override fun onNavigationComplete(navigationEvent: NavigationEvent) {}

    override fun onEventComplete(eventId: String) {
        _state.update { currentState ->
            currentState.copy(events = currentState.events.filter { it.id != eventId })
        }
    }

    private fun invokeGameStartCountdownTimer() {
        startGameCountdownTimerJob = CountUpTimer()(
            startDelayMillis = 500L,
            start = 1,
            end = 1,
            onTick = {
                scope.launch {
                    _state.update { state ->
                        val done = it == 1
                        state.copy(
                            startCountdownViewState = StartCountdownViewState(
                                text = if (done) "FIGHT" else it.toString(),
                                textColor = AppColor.BLOOD_RED.color,
                                textSize = if (done) 48.sp else 192.sp,
                            ),
                        )
                    }
                }
            },
            onComplete = {
                delayedFinish {
                    _state.update { state ->
                        state.copy(
                            startCountdownViewState = null,
                            playState = PlayState.IN_PROGRESS,
                        )
                    }
                    // Audio off Main thread.
                    scope.launch(Dispatchers.Default) {
                        soundAndVibration.startMusic(musicResourceId = RES_ID_MUSIC_3)
                    }
                }
            }
        )
    }

    private fun delayedFinish(finishFunction: () -> Unit) {
        scope.launch {
            delay(3000)
            finishFunction()
        }
    }
}
