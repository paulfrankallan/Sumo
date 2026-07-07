package feature.game.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
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
import feature.game.domain.engine.GameLoop
import feature.game.domain.model.ArenaWorld
import feature.game.domain.model.GameWorld
import feature.game.domain.model.RikishiBody
import feature.game.domain.model.StartCountdownViewState
import feature.game.domain.physics.PhysicsEvent
import feature.game.domain.usecase.ApplyDamage
import feature.game.domain.usecase.UpdatePlayState
import feature.game.presentation.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import platform.RES_ID_MUSIC_3
import platform.randomUUID
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.loser
import sumo.shared.generated.resources.rikishi_blue
import sumo.shared.generated.resources.rikishi_red
import sumo.shared.generated.resources.winner
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class GameViewModel(
    private val applyDamage: ApplyDamage,
    private val updatePlayState: UpdatePlayState,
    private val soundAndVibration: SoundAndVibrationFeedback,
    val gameLoop: GameLoop = GameLoop(),
) : CMViewModel<GameState, Intent>() {
    private var gameId: String? = null
    private var startGameCountdownTimerJob: Job? = null
    // Per-player flags prevent double damage to the same player in one reset cycle
    // while still allowing both players to be damaged in the same cycle.
    private val isTopResettingAfterDamage = mutableStateOf(false)
    private val isBottomResettingAfterDamage = mutableStateOf(false)
    private var lastClashFeedbackMark = TimeSource.Monotonic.markNow()
    // Single shared position reset — both Rikishi always return to start together.
    private val _resetThumbPositions = mutableStateOf(false)
    val resetThumbPositions: State<Boolean> = _resetThumbPositions

    private fun triggerResetThumbPositions() {
        _resetThumbPositions.value = !_resetThumbPositions.value
    }

    private fun currentInitialWorld(state: GameState): GameWorld? {
        val centre = state.arenaCentre ?: return null
        val arenaRadius = state.arenaRadius ?: return null
        val rikishiRadius = state.rikishiRadius ?: return null
        val offset = Offset(0f, arenaRadius * 0.55f)
        return GameWorld(
            arena = ArenaWorld(centre, arenaRadius),
            topRikishi = RikishiBody(state.topPlayer.id, centre - offset, rikishiRadius),
            bottomRikishi = RikishiBody(state.bottomPlayer.id, centre + offset, rikishiRadius),
        )
    }

    init {
        scope.launch {
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

        // Subscribe to game loop world state → update render positions in GameState.
        scope.launch {
            gameLoop.worldState.filterNotNull().collect { world ->
                _state.update { state ->
                    state.copy(
                        topRikishiPosition = world.topRikishi.position,
                        bottomRikishiPosition = world.bottomRikishi.position,
                        arenaCentre = world.arena.centre,
                        arenaRadius = world.arena.radius,
                        rikishiRadius = world.topRikishi.radius,
                    )
                }
            }
        }

        // Subscribe to physics boundary events → apply damage (replaces onDamageDetected callback).
        scope.launch {
            gameLoop.physicsEvents.collect { event ->
                when (event) {
                    is PhysicsEvent.BoundaryViolation -> {
                        val currentState = state.value
                        val player = when (event.playerId) {
                            currentState.topPlayer.id -> currentState.topPlayer
                            currentState.bottomPlayer.id -> currentState.bottomPlayer
                            else -> null
                        } ?: return@collect

                        val isTop = player.position == Position.TOP
                        val alreadyResetting = if (isTop) isTopResettingAfterDamage.value
                                               else isBottomResettingAfterDamage.value
                        if (!alreadyResetting) {
                            if (isTop) isTopResettingAfterDamage.value = true
                            else isBottomResettingAfterDamage.value = true
                            _state.update { s -> applyDamage(s, player) }
                            currentInitialWorld(currentState)?.let { gameLoop.reset(it) }
                            // If the player was actively dragging when they hit the boundary,
                            // block their drag input until the gesture ends — prevents the
                            // in-flight drag from immediately pushing the Rikishi back out.
                            if (player.thumbState == ThumbState.PRESSED) {
                                gameLoop.blockDragForPlayer(player.id)
                            }
                            triggerResetThumbPositions()
                            scope.launch(Dispatchers.Default) {
                                soundAndVibration.gameOverFeedback()
                            }
                        }
                    }
                    is PhysicsEvent.RikishiCollision -> {
                        val currentState = state.value
                        if (currentState.playState == PlayState.IN_PROGRESS && !currentState.isGameOver) {
                            if (lastClashFeedbackMark.elapsedNow() >= CLASH_FEEDBACK_COOLDOWN) {
                                lastClashFeedbackMark = TimeSource.Monotonic.markNow()
                                scope.launch(Dispatchers.Default) {
                                    soundAndVibration.clashFeedback()
                                }
                            }
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
                if (state.value.playState == PlayState.IN_PROGRESS && !state.value.isGameOver) return
                startGameCountdownTimerJob?.cancel()
                val newTopId = randomUUID()
                val newBottomId = randomUUID()
                _state.update { state ->
                    state.copy(
                        gameId = randomUUID(),
                        gameOverResult = null,
                        topPlayer = Player(id = newTopId, position = Position.TOP),
                        bottomPlayer = Player(id = newBottomId, position = Position.BOTTOM),
                        ui = UI()
                    )
                }
                // Reset game loop world if arena has been measured.
                state.value.let { s ->
                    val centre = s.arenaCentre
                    val arenaRadius = s.arenaRadius
                    val rikishiRadius = s.rikishiRadius
                    if (centre != null && arenaRadius != null && rikishiRadius != null) {
                        val offset = Offset(0f, arenaRadius * 0.55f)
                        gameLoop.stop()
                        gameLoop.start(
                            GameWorld(
                                arena = ArenaWorld(centre, arenaRadius),
                                topRikishi = RikishiBody(newTopId, centre - offset, rikishiRadius),
                                bottomRikishi = RikishiBody(newBottomId, centre + offset, rikishiRadius),
                            ),
                            scope,
                        )
                    }
                }
                triggerResetThumbPositions()
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
                val isTop = intent.player.position == Position.TOP
                val alreadyResetting = if (isTop) isTopResettingAfterDamage.value
                                       else isBottomResettingAfterDamage.value
                if (!alreadyResetting) {
                    if (isTop) isTopResettingAfterDamage.value = true
                    else isBottomResettingAfterDamage.value = true
                    _state.update { state -> applyDamage(state, intent.player) }
                    currentInitialWorld(state.value)?.let { gameLoop.reset(it) }
                    val currentPlayer = if (isTop) state.value.topPlayer else state.value.bottomPlayer
                    if (currentPlayer.thumbState == ThumbState.PRESSED) {
                        gameLoop.blockDragForPlayer(intent.player.id)
                    }
                    // Only the first player to be damaged this cycle triggers the shared
                    // reset — both positions always reset together. The second player's
                    // damage is still applied to their health; they ride the same reset.
                    val otherAlreadyResetting = if (isTop) isBottomResettingAfterDamage.value
                                                else isTopResettingAfterDamage.value
                    if (!otherAlreadyResetting) triggerResetThumbPositions()
                    scope.launch(Dispatchers.Default) {
                        soundAndVibration.gameOverFeedback()
                    }
                }
            }

            is GameIntent.DragEnded -> {
                gameLoop.unblockDragForPlayer(intent.player.id)
            }

            is GameIntent.PressStateChanged -> {
                _state.update { state ->
                    updatePlayState(state, intent.player, intent.isPressed)
                }
            }

            is GameIntent.ResetThumbsComplete -> {
                isTopResettingAfterDamage.value = false
                isBottomResettingAfterDamage.value = false
            }

            is GameIntent.ArenaMeasured -> {
                val currentState = state.value
                val rikishiStartOffset = Offset(0f, intent.arenaRadius * 0.55f)
                val initialWorld = GameWorld(
                    arena = ArenaWorld(centre = intent.centre, radius = intent.arenaRadius),
                    topRikishi = RikishiBody(
                        id = currentState.topPlayer.id,
                        position = intent.centre - rikishiStartOffset,
                        radius = intent.rikishiRadius,
                    ),
                    bottomRikishi = RikishiBody(
                        id = currentState.bottomPlayer.id,
                        position = intent.centre + rikishiStartOffset,
                        radius = intent.rikishiRadius,
                    ),
                )
                gameLoop.start(initialWorld, scope)
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
            startDelayMillis = 1L,
            start = 0,
            end = 0,
            onTick = {
                scope.launch {
                    _state.update { state ->
                        val done = it == 0
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
            delay(2000.milliseconds)
            finishFunction()
        }
    }
}

private val CLASH_FEEDBACK_COOLDOWN = 500.milliseconds
