package feature.game.presentation

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import app.sound.SoundAndVibrationFeedback
import app.theme.AppColor
import feature.common.events.GameOverEvent
import feature.common.model.Position
import feature.common.presentation.CMViewModel
import feature.common.presentation.Intent
import feature.common.presentation.NavigationEvent
import feature.game.domain.GyojiVoiceController
import feature.game.domain.engine.GameLoop
import feature.game.domain.input.InputCommand
import feature.game.domain.input.PowerMove
import feature.game.domain.model.ArenaWorld
import feature.game.domain.model.GameWorld
import feature.game.domain.model.RikishiBody
import feature.game.domain.physics.PhysicsEvent
import feature.game.domain.usecase.ApplyDamage
import feature.game.domain.usecase.UpdatePlayState
import feature.game.presentation.model.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
import sumo.shared.generated.resources.rikishi_blue_push
import sumo.shared.generated.resources.rikishi_red
import sumo.shared.generated.resources.rikishi_red_push
import sumo.shared.generated.resources.winner
import kotlin.random.Random

class GameViewModel(
    private val applyDamage: ApplyDamage,
    private val updatePlayState: UpdatePlayState,
    private val soundAndVibration: SoundAndVibrationFeedback,
    val gameLoop: GameLoop = GameLoop(),
) : CMViewModel<GameState, Intent>() {
    private var startCeremonyJob: Job? = null
    private val powerMoveTimerJobs = mutableMapOf<PowerMove, Job>()
    private val gameSession = MutableStateFlow(0)
    private var finalGameOverSoundPending = false
    // Per-player flags prevent double damage to the same player in one reset cycle
    // while still allowing both players to be damaged in the same cycle.
    private val isTopResettingAfterDamage = mutableStateOf(false)
    private val isBottomResettingAfterDamage = mutableStateOf(false)
    // Tracks whether a clash vibration has been emitted since the last health damage.
    private var hasVibratedSinceDamage = false

    private val gyojiController by lazy {
        GyojiVoiceController {
            launchGameAudio {
                soundAndVibration.hakkeyoiFeedback()
            }
        }
    }

    // Tracks normal collision contact for Rikishi power-move artwork.
    private var prevTouching = false

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

    private fun isGameInProgress(): Boolean =
        state.value.playState == PlayState.IN_PROGRESS && !state.value.isGameOver

    fun submitInput(command: InputCommand) {
        if (isGameInProgress()) {
            gameLoop.submitInput(command)
        }
    }

    private fun launchGameAudio(playAudio: () -> Unit) {
        val session = gameSession.value
        scope.launch(Dispatchers.Default) {
            if (session == gameSession.value && isGameInProgress()) {
                playAudio()
            }
        }
    }

    private fun stopGameActivity() {
        gameSession.update { it + 1 }
        startCeremonyJob?.cancel()
        startCeremonyJob = null
        disableAllPowerMoves()
        gyojiController.reset()
        gameLoop.stop()
        soundAndVibration.stopMusic(musicResourceId = RES_ID_MUSIC_3)
        soundAndVibration.stopGameFeedback()
    }

    private fun disableAllPowerMoves() {
        PowerMove.entries.forEach(::disablePowerMove)
    }

    private fun disablePowerMove(powerMove: PowerMove) {
        powerMoveTimerJobs.remove(powerMove)?.cancel()
        _state.update { state -> state.withPowerMoveEnabled(powerMove, false) }
    }

    private fun scheduleNextPowerMove(powerMove: PowerMove) {
        val session = gameSession.value
        powerMoveTimerJobs[powerMove] = scope.launch {
            delay(randomPowerMoveDelayMillis())
            if (session == gameSession.value && isGameInProgress()) {
                _state.update { state -> state.withPowerMoveEnabled(powerMove, true) }
            }
        }
    }

    private fun resetPowerMoveTimer(powerMove: PowerMove) {
        disablePowerMove(powerMove)
        scheduleNextPowerMove(powerMove)
    }

    private fun claimPowerMove(playerId: String, powerMove: PowerMove) {
        val currentState = state.value
        val playerExists = playerId == currentState.topPlayer.id ||
            playerId == currentState.bottomPlayer.id
        if (
            !playerExists ||
            !currentState.isPowerMoveEnabled(powerMove) ||
            !isGameInProgress()
        ) {
            return
        }
        disablePowerMove(powerMove)
        gameLoop.submitPowerMove(playerId, powerMove)
        scheduleNextPowerMove(powerMove)
    }

    private fun deactivateGame(playFinalGameOverSound: Boolean = false) {
        val shouldPlayFinalGameOverSound = playFinalGameOverSound && finalGameOverSoundPending
        finalGameOverSoundPending = false
        stopGameActivity()
        _state.update { state ->
            state.copy(
                playState = PlayState.FINISHED,
                isStartCeremonyVisible = false,
            )
        }
        if (shouldPlayFinalGameOverSound) {
            launchFinalGameOverAudio()
        }
    }

    private fun launchFinalGameOverAudio() {
        val session = gameSession.value
        scope.launch(Dispatchers.Default) {
            if (session == gameSession.value && state.value.isGameOver) {
                soundAndVibration.finalGameOverFeedback()
            }
        }
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

        scope.launch {
            gameLoop.activePushLungePlayerId.collect { playerId ->
                _state.update { state ->
                    state.withPowerMoveAppearance(
                        touching = prevTouching,
                        pushLungingPlayerId = playerId,
                        pullAttackerId = gameLoop.activePullPlayerId.value,
                    )
                }
            }
        }

        scope.launch {
            gameLoop.activePullPlayerId.collect { playerId ->
                _state.update { state ->
                    state.withPowerMoveAppearance(
                        touching = prevTouching,
                        pushLungingPlayerId = gameLoop.activePushLungePlayerId.value,
                        pullAttackerId = playerId,
                    )
                }
            }
        }

        // Subscribe to the latest world state for rendering.
        scope.launch {
            gameLoop.worldState.filterNotNull().collect { world ->
                val topPos = world.topRikishi.position
                val bottomPos = world.bottomRikishi.position

                val horizontalDistance = topPos.x - bottomPos.x
                val verticalDistance = topPos.y - bottomPos.y
                val currentDist = kotlin.math.sqrt(
                    horizontalDistance * horizontalDistance + verticalDistance * verticalDistance
                )

                // Are they touching/gripping? Use small epsilon to account for float math.
                val threshold = world.topRikishi.radius + world.bottomRikishi.radius + 1.0f
                val touching = currentDist <= threshold

                prevTouching = touching
                _state.update { state ->
                    state.copy(
                        topRikishiPosition = topPos,
                        bottomRikishiPosition = bottomPos,
                        arenaCentre = world.arena.centre,
                        arenaRadius = world.arena.radius,
                        rikishiRadius = world.topRikishi.radius,
                    ).withPowerMoveAppearance(
                        touching = touching,
                        pushLungingPlayerId = gameLoop.activePushLungePlayerId.value,
                        pullAttackerId = gameLoop.activePullPlayerId.value,
                    )
                }
            }
        }

        // StateFlow suppresses equal worlds while the Rikishi are idle, so use the
        // per-frame stream to keep the Hakkeyoi timer advancing during a stall.
        scope.launch {
            gameLoop.worldFrames.collect { world ->
                gyojiController.update(
                    topPosition = world.topRikishi.position,
                    bottomPosition = world.bottomRikishi.position,
                    boutInProgress = state.value.playState == PlayState.IN_PROGRESS && !state.value.isGameOver,
                )
            }
        }

        // Subscribe to physics boundary events → apply damage (replaces onDamageDetected callback).
        scope.launch {
            gameLoop.physicsEvents.collect { event ->
                when (event) {
                    is PhysicsEvent.BoundaryViolation -> {
                        val currentState = state.value
                        if (!isGameInProgress()) return@collect
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
                            // Reset clash vibration allowance — next clash should vibrate.
                            hasVibratedSinceDamage = false

                            if (state.value.isGameOver) {
                                finalGameOverSoundPending = true
                                stopGameActivity()
                            } else {
                                currentInitialWorld(currentState)?.let { gameLoop.reset(it) }
                                // If the player was actively dragging when they hit the boundary,
                                // block their drag input until the gesture ends — prevents the
                                // in-flight drag from immediately pushing the Rikishi back out.
                                if (player.thumbState == ThumbState.PRESSED) {
                                    gameLoop.blockDragForPlayer(player.id)
                                }
                                triggerResetThumbPositions()
                                launchGameAudio {
                                    soundAndVibration.gameOverFeedback()
                                }
                            }
                        }
                    }
                    is PhysicsEvent.RikishiCollision -> {
                        if (isGameInProgress()) {
                            // Only vibrate on the first clash since the last health damage.
                            if (!hasVibratedSinceDamage) {
                                hasVibratedSinceDamage = true
                                launchGameAudio {
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
                if (
                    state.value.playState == PlayState.STARTING ||
                    (state.value.playState == PlayState.IN_PROGRESS && !state.value.isGameOver)
                ) {
                    return
                }
                finalGameOverSoundPending = false
                stopGameActivity()
                isTopResettingAfterDamage.value = false
                isBottomResettingAfterDamage.value = false
                hasVibratedSinceDamage = false
                val newTopId = randomUUID()
                val newBottomId = randomUUID()
                _state.update { state ->
                    state.copy(
                        gameId = randomUUID(),
                        gameOverResult = null,
                        topPlayer = Player(id = newTopId, position = Position.TOP),
                        bottomPlayer = Player(id = newBottomId, position = Position.BOTTOM),
                        playState = PlayState.STARTING,
                        isStartCeremonyVisible = true,
                        ui = UI(),
                    )
                }
                // Reset game loop world if arena has been measured.
                state.value.let { s ->
                    val centre = s.arenaCentre
                    val arenaRadius = s.arenaRadius
                    val rikishiRadius = s.rikishiRadius
                    if (centre != null && arenaRadius != null && rikishiRadius != null) {
                        val offset = Offset(0f, arenaRadius * 0.55f)
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
                startGameCeremony()
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
                deactivateGame(playFinalGameOverSound = intent.result != null)
            }

            GameIntent.StopGame -> {
                deactivateGame()
            }

            is GameIntent.PlayerDamaged -> {
                val isTop = intent.player.position == Position.TOP
                val alreadyResetting = if (isTop) isTopResettingAfterDamage.value
                                       else isBottomResettingAfterDamage.value
                if (!alreadyResetting) {
                    if (isTop) isTopResettingAfterDamage.value = true
                    else isBottomResettingAfterDamage.value = true
                    _state.update { state -> applyDamage(state, intent.player) }
                    // Damage happened — allow the next clash to vibrate.
                    hasVibratedSinceDamage = false

                    if (state.value.isGameOver) {
                        finalGameOverSoundPending = true
                        stopGameActivity()
                    } else {
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
                        launchGameAudio {
                            soundAndVibration.gameOverFeedback()
                        }
                    }
                }
            }

            is GameIntent.DragEnded -> {
                gameLoop.unblockDragForPlayer(intent.player.id)
            }

            is GameIntent.PowerMovePressed -> {
                claimPowerMove(intent.playerId, intent.powerMove)
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
            if (player.position == Position.TOP) {
                if (state.ui.isTopPushing) Res.drawable.rikishi_blue_push else Res.drawable.rikishi_blue
            } else {
                if (state.ui.isBottomPushing) Res.drawable.rikishi_red_push else Res.drawable.rikishi_red
            }
        }
    }

    override fun onNavigationComplete(navigationEvent: NavigationEvent) {}

    override fun onEventComplete(eventId: String) {
        _state.update { currentState ->
            currentState.copy(events = currentState.events.filter { it.id != eventId })
        }
    }

    override fun onCleared() {
        finalGameOverSoundPending = false
        stopGameActivity()
        super.onCleared()
    }

    private fun startGameCeremony() {
        val session = gameSession.value
        startCeremonyJob = scope.launch {
            delay(START_CEREMONY_DURATION_MILLIS)
            if (
                session != gameSession.value ||
                state.value.playState != PlayState.STARTING
            ) {
                return@launch
            }
            soundAndVibration.jikanDesuFeedback()
            _state.update { state ->
                state.copy(
                    isStartCeremonyVisible = false,
                    playState = PlayState.IN_PROGRESS,
                )
            }
            PowerMove.entries.forEach(::resetPowerMoveTimer)
            launchGameAudio {
                soundAndVibration.startMusic(musicResourceId = RES_ID_MUSIC_3)
            }
        }
    }
}

private fun GameState.isPowerMoveEnabled(powerMove: PowerMove): Boolean = when (powerMove) {
    PowerMove.PUSH -> isPushPowerMoveEnabled
    PowerMove.PULL -> isPullPowerMoveEnabled
}

private fun GameState.withPowerMoveEnabled(
    powerMove: PowerMove,
    enabled: Boolean,
): GameState = when (powerMove) {
    PowerMove.PUSH -> copy(isPushPowerMoveEnabled = enabled)
    PowerMove.PULL -> copy(isPullPowerMoveEnabled = enabled)
}

internal const val POWER_MOVE_MIN_DELAY_MILLIS = 8_000L
internal const val POWER_MOVE_MAX_DELAY_MILLIS = 30_000L
private const val START_CEREMONY_DURATION_MILLIS = 3_000L

internal fun randomPowerMoveDelayMillis(random: Random = Random.Default): Long =
    random.nextLong(
        from = POWER_MOVE_MIN_DELAY_MILLIS,
        until = POWER_MOVE_MAX_DELAY_MILLIS + 1,
    )

private fun GameState.withPowerMoveAppearance(
    touching: Boolean,
    pushLungingPlayerId: String?,
    pullAttackerId: String?,
): GameState {
    if (isGameOver) return this

    val activeAttackerId = pullAttackerId ?: pushLungingPlayerId
    val hasActivePowerMove = activeAttackerId == topPlayer.id ||
        activeAttackerId == bottomPlayer.id
    val isTopPushing = if (hasActivePowerMove) {
        activeAttackerId == topPlayer.id
    } else {
        touching
    }
    val isBottomPushing = if (hasActivePowerMove) {
        activeAttackerId == bottomPlayer.id
    } else {
        touching
    }
    if (
        ui.isTopPushing == isTopPushing &&
        ui.isBottomPushing == isBottomPushing
    ) {
        return this
    }

    return copy(
        ui = ui.copy(
            topThumbView = ui.topThumbView.copy(
                foregroundImage = if (isTopPushing) {
                    Res.drawable.rikishi_blue_push
                } else {
                    Res.drawable.rikishi_blue
                },
            ),
            bottomThumbView = ui.bottomThumbView.copy(
                foregroundImage = if (isBottomPushing) {
                    Res.drawable.rikishi_red_push
                } else {
                    Res.drawable.rikishi_red
                },
            ),
            isTopPushing = isTopPushing,
            isBottomPushing = isBottomPushing,
        )
    )
}
