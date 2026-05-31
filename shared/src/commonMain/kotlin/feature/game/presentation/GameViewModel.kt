package feature.game.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import app.data.AppRepository
import app.sound.SoundAndVibrationFeedback
import app.theme.AppColor
import app.util.CountUpTimer
import app.util.CountdownTimer
import feature.common.events.GameOverEvent
import feature.common.events.ResetThumbsEvent
import feature.common.model.GameType
import feature.common.model.Position
import feature.common.presentation.CMViewModel
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.game.domain.model.StartCountdownViewState
import feature.game.domain.usecase.ApplyDamage
import feature.game.domain.usecase.GetThumbPrintActionPanelState
import feature.game.domain.usecase.UpdatePlayState
import feature.game.presentation.model.Player
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.getString
import platform.RES_ID_MUSIC_3
import platform.randomUUID
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.fingerprint
import sumo.shared.generated.resources.i_declare_a_thumb_war
import sumo.shared.generated.resources.loser
import sumo.shared.generated.resources.winner
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

class GameViewModel(
    private val applyDamage: ApplyDamage,
    private val updatePlayState: UpdatePlayState,
    private val soundAndVibration: SoundAndVibrationFeedback,
    private val lockTimer: CountdownTimer,
    private val getThumbPrintActionPanelState: GetThumbPrintActionPanelState,
    private val repository: AppRepository,
) : CMViewModel<GameState, Intent>() {
    private var gameId: String? = null
    private var lockTimerJob: Job? = null
    private var startGameCountdownTimerJob: Job? = null
    private val isResettingAfterDamage = mutableStateOf(false)
    private val _resetThumbPositions = mutableStateOf(false)
    val resetThumbPositions: State<Boolean> = _resetThumbPositions

    private fun triggerResetThumbPositions() {
        startLockTimer()
        _resetThumbPositions.value = !_resetThumbPositions.value
    }

    init {
        scope.launch {
            _state.collect {
                it.events.forEach { event ->
                    when (event) {
                        is ResetThumbsEvent -> {
                            onIntent(GameIntent.ResetThumbs)
                            onEventComplete(event.id)
                        }

                        is GameOverEvent -> {
                            onIntent(GameIntent.GameOver())
                            onEventComplete(event.id)
                        }
                    }
                }
            }
        }
        throw RuntimeException("Test Crash")
    }

    override fun initialViewState(): GameState {
        return GameState(
            gameId = randomUUID(),
            gameType = repository.getGameType(),
            topPlayer = Player(id = randomUUID(), position = Position.TOP),
            bottomPlayer = Player(id = randomUUID(), position = Position.BOTTOM),
        )
    }

    override fun onIntent(intent: Intent) {
        when (intent) {
            GameIntent.StartGame -> {
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
                startLockTimer()
            }

            GameIntent.ResetThumbs -> {
                // noop
            }

            is GameIntent.GameOver -> {
                _state.update { state ->
                    state.copy(
                        ui = state.ui.copy(
                            topActionPanel = state.ui.topActionPanel.copy(
                                actionPanelType = ActionPanelType.GAME_OVER_UI,
                            ),
                            topThumbView = state.ui.topThumbView.copy(
                                foregroundColor = getThumbViewForegroundColor(state, state.topPlayer),
                                foregroundImage = getThumbViewForegroundImage(state, state.topPlayer),
                            ),
                            bottomActionPanel = state.ui.bottomActionPanel.copy(
                                actionPanelType = ActionPanelType.GAME_OVER_UI,
                            ),
                            bottomThumbView = state.ui.bottomThumbView.copy(
                                foregroundColor = getThumbViewForegroundColor(state, state.bottomPlayer),
                                foregroundImage = getThumbViewForegroundImage(state, state.bottomPlayer),
                            ),
                        )
                    )
                }
                soundAndVibration.stopMusic(musicResourceId = RES_ID_MUSIC_3)
                stopLockTimer()
                if (intent.result == null) return
                if (gameId == null || gameId != state.value.gameId) {
                    gameId = state.value.gameId
                    soundAndVibration.gameOverFeedback()
                }
            }

            is GameIntent.PlayerDamaged -> {
                if (isResettingAfterDamage.value.not()) {
                    _state.update { state ->
                        isResettingAfterDamage.value = true
                        applyDamage(state, intent.player)
                    }
                    triggerResetThumbPositions()
                }
                onIntent(GameIntent.LockThumbs())
                soundAndVibration.gameOverFeedback()
            }

            is GameIntent.PressStateChanged -> {
                _state.update { state ->
                    updatePlayState(state, intent.player, intent.isPressed)
                }
            }

            is GameIntent.ThumbActionButtonClicked -> {
                if (state.value.isGameOver.not()) {
                    _state.update { state ->
                        // Return the player as is if the player is not locked or if isKey is false.
                        // Otherwise, return the player with isLocked set to false (unlock the player).
                        var tempState = state.copy(
                            topPlayer = state.topPlayer.takeIf {
                                intent.player.position != Position.TOP || !it.isLocked || intent.isKey == false
                            } ?: state.topPlayer.copy(isLocked = false),
                            bottomPlayer = state.bottomPlayer.takeIf {
                                intent.player.position != Position.BOTTOM || !it.isLocked || intent.isKey == false
                            } ?: state.bottomPlayer.copy(isLocked = false),
                            resettingKey = randomUUID()
                        )
                        if(state.gameType != GameType.STRENGTH) {
                            if (!tempState.topPlayer.isLocked && !tempState.bottomPlayer.isLocked) {
                                startLockTimer()
                            }
                        }
                        // Penalty for wrong key button (apply damage & reset)
                        // Identify the player who pressed the button.
                        // Check if they pressed the incorrect key;
                        // If they did, apply damage to the player.
                        if (intent.player.position == Position.TOP) {
                            if (intent.isKey == false) {
                                tempState = applyDamage(tempState, state.topPlayer)
                                tempState = tempState.handleLockThumbs(state.topPlayer)
                            }
                        } else {
                            if (intent.isKey == false) {
                                tempState = applyDamage(tempState, state.bottomPlayer)
                                tempState = tempState.handleLockThumbs(state.bottomPlayer)
                            }
                        }
                        tempState
                    }
                }
            }

            is GameIntent.LockThumbs -> {
                handleLockThumbsUpdate(intent.player)
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
            Res.drawable.fingerprint
        }
    }

    private fun GameState.handleLockThumbs(player: Player?): GameState {
        val isTop = player?.position == Position.TOP // set top ignore bottom
        val isBottom = player?.position == Position.BOTTOM // set bottom ignore top
        val isBoth = isTop.not() && isBottom.not() // set both
        return copy(
            topPlayer = topPlayer.copy(
                isLocked = isBoth || if (isTop) true else topPlayer.isLocked,
                thumbPrintActionPanelState = if (isBoth || isTop) {
                    getThumbPrintActionPanelState()
                } else {
                    topPlayer.thumbPrintActionPanelState
                }
            ),
            bottomPlayer = bottomPlayer.copy(
                isLocked = isBoth || if (isBottom) true else bottomPlayer.isLocked,
                thumbPrintActionPanelState = if (isBoth || isBottom) {
                    getThumbPrintActionPanelState()
                } else {
                    bottomPlayer.thumbPrintActionPanelState
                }
            )
        )
    }

    private fun handleLockThumbsUpdate(player: Player?) {
        if (!state.value.isGameOver) {
            val isTop = player?.position == Position.TOP // set top ignore bottom
            val isBottom = player?.position == Position.BOTTOM // set bottom ignore top
            val isBoth = isTop.not() && isBottom.not() // set both
            _state.update { state ->
                val isTopLocked = isBoth || if (isTop) true else state.topPlayer.isLocked
                val isBottomLocked = isBoth || if (isBottom) true else state.bottomPlayer.isLocked
                val copy = state.copy(
                    topPlayer = state.topPlayer.copy(
                        isLocked = isTopLocked && state.gameType != GameType.STRENGTH,
                        isResetting = isTopLocked && state.gameType == GameType.STRENGTH,
                        thumbPrintActionPanelState = if (isBoth || isTop) {
                            getThumbPrintActionPanelState()
                        } else {
                            state.topPlayer.thumbPrintActionPanelState
                        }
                    ),
                    bottomPlayer = state.bottomPlayer.copy(
                        isLocked = isBottomLocked && state.gameType != GameType.STRENGTH,
                        isResetting = isBottomLocked && state.gameType == GameType.STRENGTH,
                        thumbPrintActionPanelState = if (isBoth || isBottom) {
                            getThumbPrintActionPanelState()
                        } else {
                            state.bottomPlayer.thumbPrintActionPanelState
                        }
                    )
                )
                copy
            }
        }
    }

    override fun onNavigationComplete(navigationEvent: NavigationEvent) {

    }

    override fun onEventComplete(eventId: String) {
        _state.update { currentState ->
            currentState.copy(events = currentState.events.filter { it.id != eventId })
        }
    }

    private fun startLockTimer() {
        val randomDuration = Random.nextDouble(7.0, 31.0).seconds
        lockTimerJob = lockTimer(
            duration = randomDuration,
            cancelExisting = true,
            onComplete = {
                onIntent(GameIntent.LockThumbs())
            }
        )
    }

    private fun stopLockTimer() {
        lockTimerJob?.cancel()
    }

    private fun invokeGameStartCountdownTimer() {
        startGameCountdownTimerJob = CountUpTimer()(
            startDelayMillis = 500L,
            start = 1,
            end = 5,
            onStarted = {
                _state.update { state ->
                    state.copy(
                        ui = state.ui.copy(
                            topActionPanel = state.ui.topActionPanel.copy(
                                actionPanelType = ActionPanelType.THUMB_PRINT_UI,
                            ),
                            bottomActionPanel = state.ui.bottomActionPanel.copy(
                                actionPanelType = ActionPanelType.THUMB_PRINT_UI,
                            )
                        )
                    )
                }
            },
            onTick = {
                scope.launch {
                    _state.update { state ->
                        val elapsedSeconds = it
                        val done = elapsedSeconds == 5
                        state.copy(
                            startCountdownViewState = StartCountdownViewState(
                                text = if (done) {
                                    getString(Res.string.i_declare_a_thumb_war)
                                } else {
                                    (it).toString()
                                },
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
                    soundAndVibration.startMusic(musicResourceId = RES_ID_MUSIC_3)
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