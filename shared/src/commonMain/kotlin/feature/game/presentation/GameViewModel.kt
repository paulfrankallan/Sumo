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
import sumo.shared.generated.resources.rikishi_blue_push
import sumo.shared.generated.resources.rikishi_red
import sumo.shared.generated.resources.rikishi_red_push
import sumo.shared.generated.resources.winner
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource
import feature.game.domain.GyojiVoiceController

class GameViewModel(
    private val applyDamage: ApplyDamage,
    private val updatePlayState: UpdatePlayState,
    private val soundAndVibration: SoundAndVibrationFeedback,
    val gameLoop: GameLoop = GameLoop(),
) : CMViewModel<GameState, Intent>() {
    companion object {
        // One-shot direct test to bypass the controller and exercise the
        // feedback->platform audio path. Set to false to disable after testing.
        var DIRECT_HAKKEYOI_TEST = true
    }
    private var gameId: String? = null
    private var startGameCountdownTimerJob: Job? = null
    // Per-player flags prevent double damage to the same player in one reset cycle
    // while still allowing both players to be damaged in the same cycle.
    private val isTopResettingAfterDamage = mutableStateOf(false)
    private val isBottomResettingAfterDamage = mutableStateOf(false)
    private var lastClashFeedbackMark = TimeSource.Monotonic.markNow()
    // Tracks whether a clash vibration has been emitted since the last health damage.
    private var hasVibratedSinceDamage = false

    // Gyoji voice controller and cached positions for activity estimation.
    private val gyojiController by lazy {
        GyojiVoiceController { intensity ->
            co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: gyoji playHakkeyoi requested intensity=$intensity" }
            scope.launch(Dispatchers.Default) {
                soundAndVibration.hakkeyoiFeedback(intensity)
            }
        }
    }

    // Previous positions used to estimate movement/velocity.
    private var prevTopPos: Offset? = null
    private var prevBottomPos: Offset? = null
    
    // Track previous touching state to detect transitions
    private var prevTouching = false

    // Simple fallback stall detector state (pixel-based)
    private var simpleStalledFor = 0f
    private var simpleTimeUntilNextHakkeyoi = Float.POSITIVE_INFINITY
    private var simpleHakkeyoiCount = 0

    private fun simpleResetStall() {
        if (simpleStalledFor > 0f) co.touchlab.kermit.Logger.d { "GameViewModel: simpleResetStall — movement resumed, clearing stall (was ${"%.2f".format(simpleStalledFor)}s)" }
        simpleStalledFor = 0f
        simpleTimeUntilNextHakkeyoi = Float.POSITIVE_INFINITY
        simpleHakkeyoiCount = 0
    }

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
        co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel init" }
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

        // Ensure gyojiController is constructed early so its logs appear in startup
        // (it is lazy to avoid work until VM created; force-init here).
        co.touchlab.kermit.Logger.d { "PFASOUND - Forcing gyojiController init" }
        val _forceGyoji = gyojiController

        // Debug: optional direct test that bypasses the controller and invokes
        // the hakkeyoi feedback/play path once at startup. Useful to confirm
        // the feedback->SoundAndVibrate path is working.
        if (DIRECT_HAKKEYOI_TEST) {
            scope.launch(Dispatchers.Default) {
                co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: direct hakkeyoi test (startup)" }
                try {
                    soundAndVibration.hakkeyoiFeedback(1)
                } catch (t: Throwable) {
                    co.touchlab.kermit.Logger.e { "PFASOUND - GameViewModel: direct hakkeyoi test threw: ${t.message}" }
                }
            }
        }

        // Subscribe to game loop world state → update render positions in GameState and feed the gyoji.
        scope.launch {
            gameLoop.worldState.filterNotNull().collect { world ->
                val deltaSeconds = GameLoop.TICK_MS / 1000f

                val topPos = world.topRikishi.position
                val bottomPos = world.bottomRikishi.position

                val prevTop = prevTopPos
                val prevBottom = prevBottomPos

                // Distance moved this frame (linear displacement)
                val topMove = if (prevTop != null) {
                    val dx = topPos.x - prevTop.x
                    val dy = topPos.y - prevTop.y
                    kotlin.math.sqrt(dx * dx + dy * dy)
                } else 0f

                val bottomMove = if (prevBottom != null) {
                    val dx = bottomPos.x - prevBottom.x
                    val dy = bottomPos.y - prevBottom.y
                    kotlin.math.sqrt(dx * dx + dy * dy)
                } else 0f

                // Change in inter-rikishi distance (struggle)
                val prevDist = if (prevTop != null && prevBottom != null) {
                    val dx = prevTop.x - prevBottom.x
                    val dy = prevTop.y - prevBottom.y
                    kotlin.math.sqrt(dx * dx + dy * dy)
                } else null

                val currentDist = run {
                    val dx = topPos.x - bottomPos.x
                    val dy = topPos.y - bottomPos.y
                    kotlin.math.sqrt(dx * dx + dy * dy)
                }

                val distanceChange = if (prevDist != null) kotlin.math.abs(currentDist - prevDist) else 0f

                // Normalize movement by arena radius so thresholds are unitless.
                val arenaRadius = world.arena.radius.coerceAtLeast(1f)
                val topRelSpeed = (topMove / deltaSeconds) / arenaRadius
                val bottomRelSpeed = (bottomMove / deltaSeconds) / arenaRadius
                val struggleComponent = (distanceChange / arenaRadius)

                val rawActivity = topRelSpeed + bottomRelSpeed + struggleComponent

                // Are they touching/gripping? Use small epsilon to account for float math.
                val threshold = world.topRikishi.radius + world.bottomRikishi.radius + 1.0f
                val touching = currentDist <= threshold

                // Bout finished or falling
                val boutFinished = state.value.isGameOver || state.value.playState != PlayState.IN_PROGRESS
                val wrestlerFalling = false

                // Pixel-speed used for fallback
                val topPxPerSec = (topMove / deltaSeconds)
                val bottomPxPerSec = (bottomMove / deltaSeconds)

                gyojiController.update(deltaSeconds, rawActivity, touching, boutFinished, wrestlerFalling)

                // Debug per-frame (only when touching to limit noise)
                co.touchlab.kermit.Logger.d {
                    "PFASOUND - Frame: touching=$touching currentDist=${"%.2f".format(currentDist)} threshold=${"%.2f".format(world.topRikishi.radius + world.bottomRikishi.radius + 0.1f)} playState=${state.value.playState} boutFinished=$boutFinished rawActivity=${"%.3f".format(rawActivity)} topPxPerSec=${"%.1f".format(topPxPerSec)} bottomPxPerSec=${"%.1f".format(bottomPxPerSec)} simpleStalledFor=${"%.2f".format(simpleStalledFor)}"
                }

                // --- Fallback simple stall detector (pixel-based) ---
                // Tracks low-movement stalls in case normalized activity thresholds
                // don't match the physical scale on some devices.
                // Pixel-speed threshold (px/sec) below which we consider 'still'.
                val pixelStillThreshold = 20f

                // Additionally consider proximity even when strict 'touching' is false
                // (small separation due to animation/physics). This is intentionally
                // conservative and only affects hakkeyoi audio; game mechanics remain
                // unchanged.
                val proximityMarginPx = 24f
                val proximity = currentDist <= (world.topRikishi.radius + world.bottomRikishi.radius + proximityMarginPx)

                // Simple stall state stored in GameViewModel fields (lazy init below)
                if (boutFinished) {
                    if (simpleStalledFor > 0f) co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: boutFinished — resetting simple stall state" }
                    simpleResetStall()
                } else {
                    val proximityOrTouch = (touching || proximity)
                    if (proximity && !touching) {
                        co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: using proximity-based simple detector (dist=${"%.1f".format(currentDist)}, threshold=${"%.1f".format(world.topRikishi.radius + world.bottomRikishi.radius + proximityMarginPx)})" }
                    }

                    if (proximityOrTouch && topPxPerSec <= pixelStillThreshold && bottomPxPerSec <= pixelStillThreshold) {
                        val wasStalled = simpleStalledFor > 0f
                        simpleStalledFor += deltaSeconds

                        // If we've just confirmed a stall, schedule the first hakkeyoi shortly after.
                        val confirmationThreshold = 1.6f
                        if (!wasStalled && simpleStalledFor >= confirmationThreshold) {
                            // First hakkeyoi delay: 0.4–1.1s after stall confirmation
                            simpleTimeUntilNextHakkeyoi = 0.4f + kotlin.random.Random.nextFloat() * (1.1f - 0.4f)
                            co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: stall confirmed, scheduling first simple hakkeyoi in ${"%.2f".format(simpleTimeUntilNextHakkeyoi)}s" }
                        }

                        // Decrease timer if scheduled
                        if (simpleTimeUntilNextHakkeyoi.isFinite()) {
                            simpleTimeUntilNextHakkeyoi -= deltaSeconds
                        }

                        if (simpleTimeUntilNextHakkeyoi <= 0f) {
                            // Debug log scheduling/play
                            co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: simple hakkeyoi play triggered (count=${simpleHakkeyoiCount + 1}, stalledFor=${"%.2f".format(simpleStalledFor)}s)" }
                            // play a gentle hakkeyoi
                            scope.launch(Dispatchers.Default) {
                                co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: invoking hakkeyoiFeedback (simple detector)" }
                                soundAndVibration.hakkeyoiFeedback(1)
                            }
                            simpleHakkeyoiCount++
                            // schedule next between 1.8–3.5s
                            simpleTimeUntilNextHakkeyoi = 1.8f + kotlin.random.Random.nextFloat() * (3.5f - 1.8f)
                            co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: next simple hakkeyoi in ${"%.2f".format(simpleTimeUntilNextHakkeyoi)}s" }
                            // occasionally insert a longer pause after several calls
                            if (simpleHakkeyoiCount >= 4 && kotlin.random.Random.nextFloat() < 0.3f) {
                                simpleTimeUntilNextHakkeyoi = 3.5f + kotlin.random.Random.nextFloat() * (5.0f - 3.5f)
                                co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: inserting longer pause, next in ${"%.2f".format(simpleTimeUntilNextHakkeyoi)}s" }
                                simpleHakkeyoiCount = 0
                            }
                        }
                    } else {
                        simpleResetStall()
                    }
                }

                // persist positions for next frame
                prevTopPos = topPos
                prevBottomPos = bottomPos

                // Update UI state - ALWAYS update positions and only update images when touching state changes
                _state.update { state ->
                    // Only update images if touching state changed
                    val ui = if (touching != prevTouching) {
                        val topImage = if (touching) Res.drawable.rikishi_blue_push else Res.drawable.rikishi_blue
                        val bottomImage = if (touching) Res.drawable.rikishi_red_push else Res.drawable.rikishi_red
                        co.touchlab.kermit.Logger.d { "PFASOUND - GameViewModel: Rikishi touching state changed! touching=$touching topImage=$topImage bottomImage=$bottomImage" }
                        state.ui.copy(
                            topThumbView = state.ui.topThumbView.copy(foregroundImage = topImage),
                            bottomThumbView = state.ui.bottomThumbView.copy(foregroundImage = bottomImage),
                            isTopPushing = touching,
                            isBottomPushing = touching,
                        )
                    } else {
                        state.ui
                    }
                    
                    state.copy(
                        topRikishiPosition = topPos,
                        bottomRikishiPosition = bottomPos,
                        arenaCentre = world.arena.centre,
                        arenaRadius = world.arena.radius,
                        rikishiRadius = world.topRikishi.radius,
                        ui = ui,
                    )
                }
                
                prevTouching = touching
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
                            // Reset clash vibration allowance — next clash should vibrate.
                            hasVibratedSinceDamage = false

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
                        // Inform the gyoji controller about a recent collision impulse.
                        try {
                            gyojiController.onCollision(1.0f)
                        } catch (_: Throwable) { }

                        val currentState = state.value
                        if (currentState.playState == PlayState.IN_PROGRESS && !currentState.isGameOver) {
                            // Only vibrate on the first clash since the last health damage.
                            if (!hasVibratedSinceDamage) {
                                hasVibratedSinceDamage = true
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
                    // Damage happened — allow the next clash to vibrate.
                    hasVibratedSinceDamage = false

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
