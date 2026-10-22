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
import sumo.shared.generated.resources.rikishi_blue_crouch
import sumo.shared.generated.resources.rikishi_blue_fall
import sumo.shared.generated.resources.rikishi_blue_push
import sumo.shared.generated.resources.rikishi_blue_standing
import sumo.shared.generated.resources.rikishi_red_crouch
import sumo.shared.generated.resources.rikishi_red_fall
import sumo.shared.generated.resources.rikishi_red_push
import sumo.shared.generated.resources.rikishi_red_standing
import sumo.shared.generated.resources.winner
import kotlin.random.Random
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class GameViewModel(
    private val applyDamage: ApplyDamage,
    private val updatePlayState: UpdatePlayState,
    private val soundAndVibration: SoundAndVibrationFeedback,
    val gameLoop: GameLoop = GameLoop(),
) : CMViewModel<GameState, Intent>() {
    private var startCeremonyJob: Job? = null
    private var damageResetJob: Job? = null
    private var mattaNashiJob: Job? = null
    private val powerMoveTimerJobs = mutableMapOf<PowerMove, Job>()
    private val gameSession = MutableStateFlow(0)
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
    private var previousWalkingWorld: GameWorld? = null

    // Single shared position reset — both Rikishi always return to start together.
    private val _resetThumbPositions = mutableStateOf(false)
    val resetThumbPositions: State<Boolean> = _resetThumbPositions

    private fun triggerResetThumbPositions() {
        _resetThumbPositions.value = !_resetThumbPositions.value
    }

    private fun clearWalkingAppearance() {
        previousWalkingWorld = null
        _state.update { state ->
            if (!state.ui.isTopWalking && !state.ui.isBottomWalking) {
                state
            } else {
                state.copy(
                    ui = state.ui.copy(
                        isTopWalking = false,
                        isBottomWalking = false,
                    ),
                )
            }
        }
    }

    private fun updateWalkingAppearance(
        previousWorld: GameWorld?,
        world: GameWorld,
    ) {
        val topHasMoved = previousWorld?.let {
            it.topRikishi.id == world.topRikishi.id &&
                it.topRikishi.position != world.topRikishi.position
        } == true
        val bottomHasMoved = previousWorld?.let {
            it.bottomRikishi.id == world.bottomRikishi.id &&
                it.bottomRikishi.position != world.bottomRikishi.position
        } == true

        _state.update { state ->
            val shouldAnimateWalking = !state.isGameOver && (
                state.playState == PlayState.STARTING ||
                    state.playState == PlayState.IN_PROGRESS
                )
            val isTopWalking = shouldAnimateWalking && topHasMoved
            val isBottomWalking = shouldAnimateWalking && bottomHasMoved
            if (
                state.ui.isTopWalking == isTopWalking &&
                state.ui.isBottomWalking == isBottomWalking
            ) {
                state
            } else {
                state.copy(
                    ui = state.ui.copy(
                        isTopWalking = isTopWalking,
                        isBottomWalking = isBottomWalking,
                    ),
                )
            }
        }
    }

    private fun newStateWorld(state: GameState): GameWorld? {
        val centre = state.arenaCentre ?: return null
        val arenaRadius = state.arenaRadius ?: return null
        val rikishiRadius = state.rikishiRadius ?: return null
        val tawaraInset = state.newRikishiTawaraInsetPx
            ?: return null
        val offset = Offset(
            x = 0f,
            y = (arenaRadius - rikishiRadius - tawaraInset).coerceAtLeast(0f),
        )
        return GameWorld(
            arena = ArenaWorld(centre, arenaRadius),
            topRikishi = RikishiBody(state.topPlayer.id, centre - offset, rikishiRadius),
            bottomRikishi = RikishiBody(state.bottomPlayer.id, centre + offset, rikishiRadius),
        )
    }

    private fun shikiriSenWorld(state: GameState): GameWorld? {
        val centre = state.arenaCentre ?: return null
        val arenaRadius = state.arenaRadius ?: return null
        val rikishiRadius = state.rikishiRadius ?: return null
        val offset = Offset(
            x = 0f,
            y = arenaRadius * SHIKIRI_SEN_CENTER_OFFSET_RATIO + rikishiRadius,
        )
        return GameWorld(
            arena = ArenaWorld(centre, arenaRadius),
            topRikishi = RikishiBody(state.topPlayer.id, centre - offset, rikishiRadius),
            bottomRikishi = RikishiBody(state.bottomPlayer.id, centre + offset, rikishiRadius),
        )
    }

    private fun isGameInProgress(): Boolean =
        state.value.playState == PlayState.IN_PROGRESS && !state.value.isGameOver

    private fun canApplyDamage(): Boolean =
        !state.value.isGameOver && (
            state.value.playState == PlayState.IN_PROGRESS ||
                state.value.playState == PlayState.DAMAGE_RESETTING
            )

    fun submitInput(command: InputCommand) {
        if (isGameInProgress()) {
            if (command.velocityVector != Offset.Zero) {
                showStandingPose(command.playerId)
            }
            gameLoop.submitInput(command)
        }
    }

    fun submitRapidTap(playerId: String, direction: Offset) {
        if (isGameInProgress()) {
            if (direction != Offset.Zero) {
                showStandingPose(playerId)
            }
            gameLoop.submitRapidTap(playerId, direction)
        }
    }

    fun playHyoshigiFeedback() {
        soundAndVibration.hyoshigiFeedback()
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
        damageResetJob?.cancel()
        damageResetJob = null
        mattaNashiJob?.cancel()
        mattaNashiJob = null
        disableAllPowerMoves()
        gyojiController.reset()
        gameLoop.stop()
        clearWalkingAppearance()
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
            delay(randomPowerMoveDelayMillis().milliseconds)
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
        showStandingPose(playerId)
        disablePowerMove(powerMove)
        gameLoop.submitPowerMove(playerId, powerMove)
        scheduleNextPowerMove(powerMove)
    }

    private fun markPlayerReady(playerId: String) {
        var shouldStartGame = false
        _state.update { state ->
            if (state.playState != PlayState.NEW && state.playState != PlayState.FINISHED) {
                return@update state
            }

            val updatedState = when (playerId) {
                state.topPlayer.id -> {
                    if (state.isTopPlayerReady) {
                        state
                    } else {
                        state.copy(isTopPlayerReady = true)
                    }
                }

                state.bottomPlayer.id -> {
                    if (state.isBottomPlayerReady) {
                        state
                    } else {
                        state.copy(isBottomPlayerReady = true)
                    }
                }

                else -> state
            }
            shouldStartGame = updatedState != state && updatedState.arePlayersReady
            updatedState
        }
        if (!shouldStartGame) return

        scope.launch {
            delay(READY_BUTTON_TRANSITION_DURATION_MILLIS.milliseconds)
            val currentState = state.value
            if (
                currentState.arePlayersReady &&
                (
                    currentState.playState == PlayState.NEW ||
                        currentState.playState == PlayState.FINISHED
                    )
            ) {
                onIntent(GameIntent.StartGame)
            }
        }
    }

    private fun showStandingPose(playerId: String) {
        _state.update { state ->
            when (playerId) {
                state.topPlayer.id -> if (state.ui.isTopCrouching) {
                    state.copy(
                        ui = state.ui.withRikishiStanding(Position.TOP),
                    )
                } else {
                    state
                }

                state.bottomPlayer.id -> if (state.ui.isBottomCrouching) {
                    state.copy(
                        ui = state.ui.withRikishiStanding(Position.BOTTOM),
                    )
                } else {
                    state
                }

                else -> state
            }
        }
    }

    private fun handlePlayerDamage(player: Player) {
        if (!canApplyDamage()) return

        val isTop = player.position == Position.TOP
        val alreadyResetting = if (isTop) isTopResettingAfterDamage.value
                               else isBottomResettingAfterDamage.value
        if (alreadyResetting) return

        if (isTop) isTopResettingAfterDamage.value = true
        else isBottomResettingAfterDamage.value = true
        _state.update { state -> applyDamage(state, player) }
        hasVibratedSinceDamage = false

        if (state.value.isGameOver) {
            stopGameActivity()
            return
        }

        clearWalkingAppearance()
        _state.update { state ->
            state.copy(ui = state.ui.withDamagedRikishi(player.position))
        }
        prevTouching = false
        disableAllPowerMoves()
        gyojiController.reset()
        gameLoop.stop()
        scheduleDamageReset()
        launchDamageResetAudio {
            soundAndVibration.gameOverFeedback()
        }
    }

    private fun scheduleDamageReset() {
        if (damageResetJob?.isActive == true) return

        val session = gameSession.value
        damageResetJob = scope.launch {
            delay(DAMAGE_RESET_DURATION_MILLIS.milliseconds)
            damageResetJob = null
            if (
                session != gameSession.value ||
                state.value.playState != PlayState.DAMAGE_RESETTING ||
                state.value.isGameOver
            ) {
                return@launch
            }
            resumeAfterDamageReset()
        }
    }

    private fun resumeAfterDamageReset() {
        val currentState = state.value
        val initialWorld = shikiriSenWorld(currentState) ?: return
        val blockTopDrag =
            isTopResettingAfterDamage.value &&
                currentState.topPlayer.thumbState == ThumbState.PRESSED
        val blockBottomDrag =
            isBottomResettingAfterDamage.value &&
                currentState.bottomPlayer.thumbState == ThumbState.PRESSED

        clearWalkingAppearance()
        gameLoop.start(initialWorld, scope)
        if (blockTopDrag) gameLoop.blockDragForPlayer(currentState.topPlayer.id)
        if (blockBottomDrag) gameLoop.blockDragForPlayer(currentState.bottomPlayer.id)
        _state.update { state ->
            state.copy(
                playState = PlayState.IN_PROGRESS,
                ui = state.ui.withStandingRikishiImages(),
            )
        }
        triggerResetThumbPositions()
        PowerMove.entries.forEach(::resetPowerMoveTimer)
        launchGameAudio {
            soundAndVibration.nokottaFeedback()
        }
    }

    private fun launchDamageResetAudio(playAudio: () -> Unit) {
        val session = gameSession.value
        scope.launch(Dispatchers.Default) {
            if (
                session == gameSession.value &&
                state.value.playState == PlayState.DAMAGE_RESETTING &&
                !state.value.isGameOver
            ) {
                playAudio()
            }
        }
    }

    private fun deactivateGame(playFinalGameOverSound: Boolean = false) {
        stopGameActivity()
        _state.update { state ->
            state.copy(
                playState = PlayState.FINISHED,
                isStartCeremonyVisible = false,
                isTopPlayerReady = false,
                isBottomPlayerReady = false,
            )
        }
        if (playFinalGameOverSound) {
            launchFinalGameOverAudio()
        }
    }

    private fun launchFinalGameOverAudio() {
        val session = gameSession.value
        scope.launch(Dispatchers.Default) {
            if (session == gameSession.value && state.value.isGameOver) {
                soundAndVibration.finalGameOverFeedback()
            }
            delay(HYOSHIGI_AFTER_GAME_OVER_DELAY_MILLIS.milliseconds)
            if (
                session == gameSession.value &&
                state.value.playState == PlayState.FINISHED
            ) {
                soundAndVibration.hyoshigiFeedback()
            }
        }
    }

    private fun startMattaNashiFeedback() {
        val session = gameSession.value
        mattaNashiJob?.cancel()
        mattaNashiJob = scope.launch {
            while (
                session == gameSession.value &&
                state.value.playState == PlayState.NEW
            ) {
                delay(randomMattaNashiDelayMillis().milliseconds)
                if (
                    session != gameSession.value ||
                    state.value.playState != PlayState.NEW
                ) {
                    break
                }
                soundAndVibration.mattaNashiFeedback()
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
                updateWalkingAppearance(previousWalkingWorld, world)
                previousWalkingWorld = world
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
                        if (!canApplyDamage()) return@collect
                        val player = when (event.playerId) {
                            currentState.topPlayer.id -> currentState.topPlayer
                            currentState.bottomPlayer.id -> currentState.bottomPlayer
                            else -> null
                        } ?: return@collect

                        handlePlayerDamage(player)
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

        startMattaNashiFeedback()
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
                if (!state.value.arePlayersReady) {
                    return
                }
                if (
                    state.value.playState == PlayState.STARTING ||
                    state.value.playState == PlayState.DAMAGE_RESETTING ||
                    (state.value.playState == PlayState.IN_PROGRESS && !state.value.isGameOver)
                ) {
                    return
                }
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
                        isStartCeremonyVisible = false,
                        ui = UI(),
                    )
                }
                newStateWorld(state.value)?.let { gameLoop.start(it, scope) }
                triggerResetThumbPositions()
                startGameCeremony()
            }

            is GameIntent.PlayerReady -> {
                markPlayerReady(intent.playerId)
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
                handlePlayerDamage(intent.player)
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
                _state.update { state ->
                    state.copy(
                        arenaCentre = intent.centre,
                        arenaRadius = intent.arenaRadius,
                        rikishiRadius = intent.rikishiRadius,
                        newRikishiTawaraInsetPx = intent.newRikishiTawaraInsetPx,
                    )
                }
                newStateWorld(state.value)?.let { gameLoop.start(it, scope) }
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
                if (state.ui.isTopPushing) {
                    Res.drawable.rikishi_blue_push
                } else {
                    Res.drawable.rikishi_blue_standing
                }
            } else {
                if (state.ui.isBottomPushing) {
                    Res.drawable.rikishi_red_push
                } else {
                    Res.drawable.rikishi_red_standing
                }
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
        stopGameActivity()
        super.onCleared()
    }

    private fun isStartCeremonyActive(session: Int): Boolean =
        session == gameSession.value && state.value.playState == PlayState.STARTING

    private suspend fun moveRikishiToShikiriSen(session: Int): Boolean {
        val startWorld = gameLoop.worldState.value ?: return false
        val targetWorld = shikiriSenWorld(state.value) ?: return false
        val startTime = TimeSource.Monotonic.markNow()

        while (isStartCeremonyActive(session)) {
            val progress = (
                startTime.elapsedNow().inWholeMilliseconds.toFloat() /
                    RIKISHI_WALK_DURATION_MILLIS
                ).coerceIn(0f, 1f)
            gameLoop.reset(startWorld.interpolateTowards(targetWorld, progress))
            if (progress >= 1f) return true
            delay(CEREMONY_MOVEMENT_FRAME_MILLIS.milliseconds)
        }
        return false
    }

    private fun startGameCeremony() {
        val session = gameSession.value
        startCeremonyJob = scope.launch {
            soundAndVibration.jikanDesuFeedback()
            _state.update { state ->
                state.copy(isStartCeremonyVisible = true)
            }
            delay(GUNBAI_DURATION_MILLIS.milliseconds)
            if (!isStartCeremonyActive(session)) return@launch
            _state.update { state ->
                state.copy(isStartCeremonyVisible = false)
            }
            if (!moveRikishiToShikiriSen(session)) return@launch
            delay(SHIKIRI_SEN_PAUSE_DURATION_MILLIS.milliseconds)
            if (!isStartCeremonyActive(session)) return@launch
            soundAndVibration.teOTsuiteFeedback()
            delay(CROUCH_DELAY_AFTER_TE_O_TSUITE_MILLIS.milliseconds)
            if (!isStartCeremonyActive(session)) return@launch
            _state.update { state ->
                state.copy(
                    ui = state.ui.withRikishiCrouching(),
                )
            }
            _state.update { state ->
                state.copy(
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

private val GameState.arePlayersReady: Boolean
    get() = isTopPlayerReady && isBottomPlayerReady

private fun GameState.withPowerMoveEnabled(
    powerMove: PowerMove,
    enabled: Boolean,
): GameState = when (powerMove) {
    PowerMove.PUSH -> copy(isPushPowerMoveEnabled = enabled)
    PowerMove.PULL -> copy(isPullPowerMoveEnabled = enabled)
}

internal const val POWER_MOVE_MIN_DELAY_MILLIS = 8_888L
internal const val POWER_MOVE_MAX_DELAY_MILLIS = 30_000L
private const val RIKISHI_WALK_DURATION_MILLIS = 1_000L
private const val SHIKIRI_SEN_PAUSE_DURATION_MILLIS = 500L
private const val CROUCH_DELAY_AFTER_TE_O_TSUITE_MILLIS = 2_000L
private const val GUNBAI_DURATION_MILLIS = 3_000L
private const val HYOSHIGI_AFTER_GAME_OVER_DELAY_MILLIS = 1_000L
private const val CEREMONY_MOVEMENT_FRAME_MILLIS = 16L
private const val DAMAGE_RESET_DURATION_MILLIS = 3_000L
private const val MATTA_NASHI_MIN_DELAY_MILLIS = 5_000L
private const val MATTA_NASHI_MAX_DELAY_MILLIS = 15_000L

internal fun randomPowerMoveDelayMillis(random: Random = Random.Default): Long =
    random.nextLong(
        from = POWER_MOVE_MIN_DELAY_MILLIS,
        until = POWER_MOVE_MAX_DELAY_MILLIS + 1,
    )

private fun randomMattaNashiDelayMillis(): Long =
    Random.nextLong(
        from = MATTA_NASHI_MIN_DELAY_MILLIS,
        until = MATTA_NASHI_MAX_DELAY_MILLIS + 1,
    )

private fun GameState.withPowerMoveAppearance(
    touching: Boolean,
    pushLungingPlayerId: String?,
    pullAttackerId: String?,
): GameState {
    if (isGameOver || playState != PlayState.IN_PROGRESS) return this

    val activeAttackerId = pullAttackerId ?: pushLungingPlayerId
    val isTopPushing = touching || activeAttackerId == topPlayer.id
    val isBottomPushing = touching || activeAttackerId == bottomPlayer.id
    val updatedUi = ui.withRikishiImages(
        topIsPushing = isTopPushing,
        bottomIsPushing = isBottomPushing,
    )

    return if (ui == updatedUi) this else copy(ui = updatedUi)
}

private fun UI.withRikishiImages(
    topIsPushing: Boolean,
    bottomIsPushing: Boolean,
): UI = copy(
    topThumbView = topThumbView.copy(
        foregroundImage = when {
            topIsPushing -> Res.drawable.rikishi_blue_push
            isTopCrouching -> Res.drawable.rikishi_blue_crouch
            else -> Res.drawable.rikishi_blue_standing
        },
    ),
    bottomThumbView = bottomThumbView.copy(
        foregroundImage = when {
            bottomIsPushing -> Res.drawable.rikishi_red_push
            isBottomCrouching -> Res.drawable.rikishi_red_crouch
            else -> Res.drawable.rikishi_red_standing
        },
    ),
    isTopPushing = topIsPushing,
    isBottomPushing = bottomIsPushing,
    isTopCrouching = isTopCrouching && !topIsPushing,
    isBottomCrouching = isBottomCrouching && !bottomIsPushing,
)

private fun UI.withRikishiCrouching(): UI = copy(
    topThumbView = topThumbView.copy(
        foregroundImage = Res.drawable.rikishi_blue_crouch,
    ),
    bottomThumbView = bottomThumbView.copy(
        foregroundImage = Res.drawable.rikishi_red_crouch,
    ),
    isTopPushing = false,
    isBottomPushing = false,
    isTopCrouching = true,
    isBottomCrouching = true,
    isTopWalking = false,
    isBottomWalking = false,
)

private fun UI.withStandingRikishiImages(): UI = copy(
    topThumbView = topThumbView.copy(
        foregroundImage = Res.drawable.rikishi_blue_standing,
    ),
    bottomThumbView = bottomThumbView.copy(
        foregroundImage = Res.drawable.rikishi_red_standing,
    ),
    isTopPushing = false,
    isBottomPushing = false,
    isTopCrouching = false,
    isBottomCrouching = false,
    isTopWalking = false,
    isBottomWalking = false,
)

private fun UI.withRikishiStanding(position: Position): UI = when (position) {
    Position.TOP -> copy(
        topThumbView = topThumbView.copy(
            foregroundImage = Res.drawable.rikishi_blue_standing,
        ),
        isTopPushing = false,
        isTopCrouching = false,
        isTopWalking = false,
    )

    Position.BOTTOM -> copy(
        bottomThumbView = bottomThumbView.copy(
            foregroundImage = Res.drawable.rikishi_red_standing,
        ),
        isBottomPushing = false,
        isBottomCrouching = false,
        isBottomWalking = false,
    )
}

private fun GameWorld.interpolateTowards(target: GameWorld, progress: Float): GameWorld = copy(
    topRikishi = topRikishi.copy(
        position = topRikishi.position.interpolateTowards(
            target = target.topRikishi.position,
            progress = progress,
        ),
    ),
    bottomRikishi = bottomRikishi.copy(
        position = bottomRikishi.position.interpolateTowards(
            target = target.bottomRikishi.position,
            progress = progress,
        ),
    ),
)

private fun Offset.interpolateTowards(target: Offset, progress: Float): Offset = Offset(
    x = x + (target.x - x) * progress,
    y = y + (target.y - y) * progress,
)

private fun UI.withDamagedRikishi(position: Position?): UI = when (position) {
    Position.TOP -> copy(
        topThumbView = topThumbView.copy(
            foregroundColor = null,
            foregroundImage = Res.drawable.rikishi_blue_fall,
        ),
        isTopPushing = false,
        isTopCrouching = false,
        isTopWalking = false,
    )

    Position.BOTTOM -> copy(
        bottomThumbView = bottomThumbView.copy(
            foregroundColor = null,
            foregroundImage = Res.drawable.rikishi_red_fall,
        ),
        isBottomPushing = false,
        isBottomCrouching = false,
        isBottomWalking = false,
    )

    null -> this
}
