package feature.game.domain.engine

import androidx.compose.ui.geometry.Offset
import feature.game.domain.input.InputCommand
import feature.game.domain.input.InputSource
import feature.game.domain.input.PowerMove
import feature.game.domain.model.GameWorld
import feature.game.domain.model.RikishiBody
import feature.game.domain.physics.PhysicsEngine
import feature.game.domain.physics.PhysicsEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.TimeSource

/**
 * The central game loop. Runs a fixed-timestep coroutine that:
 * 1. Drains pending [InputCommand]s from both input sources.
 * 2. Advances the [PhysicsEngine] by one tick.
 * 3. Emits the new [GameWorld] state and any [PhysicsEvent]s.
 *
 * Ownership: instantiated as a singleton by DI and injected into [GameViewModel].
 * The ViewModel controls start/stop/reset; composables only submit input.
 */
class GameLoop(
    private val physics: PhysicsEngine = PhysicsEngine,
    private val tickMs: Long = TICK_MS,
) {
    private val _worldState = MutableStateFlow<GameWorld?>(null)

    /** Latest physics world state. Null until [start] is called. */
    val worldState: StateFlow<GameWorld?> = _worldState.asStateFlow()

    private val _activePushLungePlayerId = MutableStateFlow<String?>(null)

    /** Player performing the current push lunge, or null when neither player is lunging. */
    val activePushLungePlayerId: StateFlow<String?> = _activePushLungePlayerId.asStateFlow()

    private val _activePullPlayerId = MutableStateFlow<String?>(null)

    /** Player currently pulling and turning an opponent, or null when no pull is in contact. */
    val activePullPlayerId: StateFlow<String?> = _activePullPlayerId.asStateFlow()

    private val _worldFrames = MutableSharedFlow<GameWorld>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Every physics frame, including frames whose world positions are unchanged. */
    val worldFrames: SharedFlow<GameWorld> = _worldFrames.asSharedFlow()

    private val _physicsEvents = MutableSharedFlow<PhysicsEvent>(extraBufferCapacity = 64)

    /** Stream of physics events (boundary violations, collisions). */
    val physicsEvents: SharedFlow<PhysicsEvent> = _physicsEvents.asSharedFlow()

    // Unlimited capacity so input producers never block or drop commands.
    private val inputChannel = Channel<InputCommand>(capacity = Channel.UNLIMITED)
    private val rapidTapChannel = Channel<RapidTapCommand>(capacity = Channel.UNLIMITED)
    private val powerMoveChannel = Channel<PowerMoveCommand>(capacity = Channel.UNLIMITED)
    private val rapidTapStateByPlayer = mutableMapOf<String, RapidTapState>()
    private val inputTimeOrigin = TimeSource.Monotonic.markNow()

    private var loopJob: Job? = null
    private var activePushLunge: PushLunge? = null
    private var activePullAction: PullAction? = null

    // Player IDs whose DIRECT_DRAG commands should be discarded (set during damage reset,
    // cleared when the drag gesture ends so the position can't be pushed back immediately).
    private val dragBlockedPlayers = mutableSetOf<String>()

    /** Submit an input command from any source. Non-blocking, thread-safe. */
    fun submitInput(command: InputCommand) {
        if (command.source == InputSource.DIRECT_DRAG && command.playerId in dragBlockedPlayers) return
        inputChannel.trySend(command)
    }

    /** Queues a directional joystick tap for possible contact-only pushing power. */
    fun submitRapidTap(playerId: String, direction: Offset) {
        if (direction == Offset.Zero) return
        rapidTapChannel.trySend(
            RapidTapCommand(
                playerId = playerId,
                direction = direction,
                timestampMillis = inputTimeOrigin.elapsedNow().inWholeMilliseconds,
            )
        )
    }

    /** Queues a one-shot [powerMove] for [playerId] to apply on the next physics frame. */
    fun submitPowerMove(playerId: String, powerMove: PowerMove) {
        powerMoveChannel.trySend(PowerMoveCommand(playerId, powerMove))
    }

    /**
     * Starts the game loop with [initialWorld] as the starting state.
     * If a loop is already running it is cancelled first.
     */
    fun start(initialWorld: GameWorld, scope: CoroutineScope) {
        loopJob?.cancel()
        dragBlockedPlayers.clear()
        clearPushLunge()
        clearPullAction()
        clearRapidTapPower()
        drainRapidTapChannel()
        drainPowerMoveChannel()
        _worldState.value = initialWorld
        loopJob = scope.launch {
            var world = initialWorld
            val timeSource = TimeSource.Monotonic
            while (isActive) {
                _worldState.value?.let { world = it }
                val frameStart = timeSource.markNow()
                val commands = drainInputChannel()
                val rapidTaps = drainRapidTapChannel()
                drainPowerMoveChannel().forEach { command ->
                    when (command.powerMove) {
                        PowerMove.PUSH -> startPushLunge(world, command.playerId)
                        PowerMove.PULL -> startPullAction(world, command.playerId)
                    }
                }
                val blockedPlayerIds = mutableSetOf<String>()
                // A power move owns both Rikishi until its final physics tick.
                activePushLunge?.let { lunge ->
                    blockedPlayerIds += lunge.playerId
                    blockedPlayerIds += lunge.opponentId
                }
                activePullAction?.let { action ->
                    blockedPlayerIds += action.attackerId
                    blockedPlayerIds += action.defenderId
                }
                val frameCommands = commands
                    .filterNot { it.playerId in blockedPlayerIds }
                    .toMutableList()
                if (activePushLunge == null && activePullAction == null) {
                    recordRapidTaps(world, rapidTaps)
                    frameCommands += rapidTapForceCommands(world)
                } else {
                    clearRapidTapPower()
                }
                activePushLunge?.let { lunge ->
                    frameCommands += InputCommand(
                        playerId = lunge.playerId,
                        velocityVector = lunge.velocityPerTick,
                        source = InputSource.POWER_MOVE,
                    )
                }
                (activePullAction as? PullAction.Lunge)?.let { lunge ->
                    frameCommands += InputCommand(
                        playerId = lunge.attackerId,
                        velocityVector = lunge.velocityPerTick,
                        source = InputSource.POWER_MOVE,
                    )
                }
                (activePullAction as? PullAction.Turn)?.let { turn ->
                    pullTurnMovement(world, turn)?.let { movement ->
                        frameCommands += InputCommand(
                            playerId = turn.defenderId,
                            velocityVector = movement,
                            source = InputSource.POWER_MOVE,
                        )
                    }
                }
                val result = physics.step(world, frameCommands)
                world = result.world
                _worldState.value = world
                _worldFrames.emit(world)
                result.events.forEach { _physicsEvents.tryEmit(it) }
                if (result.events.any { it is PhysicsEvent.BoundaryViolation }) {
                    clearPushLunge()
                    clearPullAction()
                    clearRapidTapPower()
                } else {
                    if (!areRikishiInContact(world.topRikishi, world.bottomRikishi)) {
                        clearRapidTapPower()
                    }
                    advancePushLunge()
                    advancePullAction(world)
                }
                val elapsedMs = frameStart.elapsedNow().inWholeMilliseconds
                val remaining = tickMs - elapsedMs
                if (remaining > 0) kotlinx.coroutines.delay(remaining)
            }
        }
    }

    /** Stops the loop, preserving its state while discarding stale input. */
    fun stop() {
        loopJob?.cancel()
        loopJob = null
        drainInputChannel()
        drainRapidTapChannel()
        drainPowerMoveChannel()
        clearPushLunge()
        clearPullAction()
        clearRapidTapPower()
        dragBlockedPlayers.clear()
    }

    /**
     * Resets positions to [newWorld] without stopping — use between rounds.
     * Any in-flight input commands are discarded.
     */
    fun reset(newWorld: GameWorld) {
        drainInputChannel() // discard stale commands
        drainRapidTapChannel()
        drainPowerMoveChannel()
        clearPushLunge()
        clearPullAction()
        clearRapidTapPower()
        _worldState.value = newWorld
    }

    /**
     * Prevents DIRECT_DRAG commands for [playerId] from reaching the physics engine.
     * Call after a damage-reset when the player was actively dragging, so the in-flight
     * drag gesture can't push the Rikishi straight back to the boundary.
     */
    fun blockDragForPlayer(playerId: String) {
        dragBlockedPlayers.add(playerId)
    }

    /** Lifts the drag block set by [blockDragForPlayer]. Call when the drag gesture ends. */
    fun unblockDragForPlayer(playerId: String) {
        dragBlockedPlayers.remove(playerId)
    }

    /** Drains all currently-queued input commands without suspending. */
    private fun drainInputChannel(): List<InputCommand> {
        val commands = mutableListOf<InputCommand>()
        while (true) {
            val cmd = inputChannel.tryReceive().getOrNull() ?: break
            commands += cmd
        }
        return commands
    }

    /** Drains all pending rapid joystick taps without suspending. */
    private fun drainRapidTapChannel(): List<RapidTapCommand> {
        val commands = mutableListOf<RapidTapCommand>()
        while (true) {
            val command = rapidTapChannel.tryReceive().getOrNull() ?: break
            commands += command
        }
        return commands
    }

    /** Drains all pending power moves without suspending. */
    private fun drainPowerMoveChannel(): List<PowerMoveCommand> {
        val commands = mutableListOf<PowerMoveCommand>()
        while (true) {
            val command = powerMoveChannel.tryReceive().getOrNull() ?: break
            commands += command
        }
        return commands
    }

    private fun recordRapidTaps(
        world: GameWorld,
        rapidTaps: List<RapidTapCommand>,
    ) {
        rapidTaps.forEach { tap ->
            val (player, opponent) = powerMoveParticipants(world, tap.playerId)
                ?: return@forEach
            if (
                !areRikishiInContact(player, opponent) ||
                !isDirectionTowardsOpponent(tap.direction, player, opponent)
            ) {
                return@forEach
            }

            val rapidTapState = rapidTapStateByPlayer.getOrPut(tap.playerId) {
                RapidTapState()
            }
            val previousTapTimestamp = rapidTapState.lastTapTimestampMillis
            val intervalMillis = previousTapTimestamp?.let {
                tap.timestampMillis - it
            }
            if (
                intervalMillis == null ||
                intervalMillis <= 0L ||
                intervalMillis > RAPID_TAP_STREAK_GAP_MILLIS
            ) {
                rapidTapState.intervalsMillis.clear()
            } else {
                rapidTapState.intervalsMillis += intervalMillis
                if (rapidTapState.intervalsMillis.size > RAPID_TAP_RATE_SAMPLE_COUNT) {
                    rapidTapState.intervalsMillis.removeAt(0)
                }
            }
            rapidTapState.lastTapTimestampMillis = tap.timestampMillis
        }
    }

    private fun rapidTapForceCommands(world: GameWorld): List<InputCommand> {
        val topRikishi = world.topRikishi
        val bottomRikishi = world.bottomRikishi
        if (!areRikishiInContact(topRikishi, bottomRikishi)) {
            clearRapidTapPower()
            return emptyList()
        }

        val distance = distanceBetween(topRikishi.position, bottomRikishi.position)
        if (distance == 0f) return emptyList()

        val nowMillis = inputTimeOrigin.elapsedNow().inWholeMilliseconds
        val topForce = rapidTapForce(topRikishi.id, nowMillis)
        val bottomForce = rapidTapForce(bottomRikishi.id, nowMillis)
        if (topForce == 0f && bottomForce == 0f) return emptyList()

        val normal = Offset(
            x = (bottomRikishi.position.x - topRikishi.position.x) / distance,
            y = (bottomRikishi.position.y - topRikishi.position.y) / distance,
        )
        return buildList {
            if (topForce > 0f) {
                add(
                    InputCommand(
                        playerId = topRikishi.id,
                        velocityVector = Offset(
                            x = normal.x * topForce,
                            y = normal.y * topForce,
                        ),
                        source = InputSource.RAPID_TAP,
                    )
                )
            }
            if (bottomForce > 0f) {
                add(
                    InputCommand(
                        playerId = bottomRikishi.id,
                        velocityVector = Offset(
                            x = -normal.x * bottomForce,
                            y = -normal.y * bottomForce,
                        ),
                        source = InputSource.RAPID_TAP,
                    )
                )
            }
        }
    }

    private fun rapidTapForce(playerId: String, nowMillis: Long): Float {
        val rapidTapState = rapidTapStateByPlayer[playerId] ?: return 0f
        val lastTapTimestamp = rapidTapState.lastTapTimestampMillis ?: return 0f
        val idleMillis = nowMillis - lastTapTimestamp
        if (idleMillis >= RAPID_TAP_STREAK_GAP_MILLIS) {
            rapidTapStateByPlayer.remove(playerId)
            return 0f
        }
        if (
            idleMillis >= RAPID_TAP_FORCE_TIMEOUT_MILLIS ||
            rapidTapState.intervalsMillis.isEmpty()
        ) {
            return 0f
        }

        val averageIntervalMillis = rapidTapState.intervalsMillis.average()
        if (averageIntervalMillis <= 0.0) return 0f

        val tapsPerSecond = (
            MILLIS_PER_SECOND / averageIntervalMillis.toFloat()
            ).coerceAtMost(MAX_RAPID_TAPS_PER_SECOND)
        return tapsPerSecond / MAX_RAPID_TAPS_PER_SECOND * RAPID_TAP_MAX_FORCE_PER_TICK
    }

    private fun isDirectionTowardsOpponent(
        direction: Offset,
        player: RikishiBody,
        opponent: RikishiBody,
    ): Boolean {
        val opponentDirection = Offset(
            x = opponent.position.x - player.position.x,
            y = opponent.position.y - player.position.y,
        )
        if (direction == Offset.Zero || opponentDirection == Offset.Zero) return false

        return direction.x * opponentDirection.x + direction.y * opponentDirection.y > 0f
    }

    private fun clearRapidTapPower() {
        rapidTapStateByPlayer.clear()
    }

    private fun startPushLunge(world: GameWorld, playerId: String) {
        if (activePushLunge != null || activePullAction != null) return

        val (pusher, opponent, fallbackDirection) = when (playerId) {
            world.topRikishi.id -> Triple(
                world.topRikishi,
                world.bottomRikishi,
                Offset(0f, 1f),
            )
            world.bottomRikishi.id -> Triple(
                world.bottomRikishi,
                world.topRikishi,
                Offset(0f, -1f),
            )
            else -> return
        }
        val directionX = opponent.position.x - pusher.position.x
        val directionY = opponent.position.y - pusher.position.y
        val directionLength = sqrt(directionX * directionX + directionY * directionY)
        val direction = if (directionLength > 0f) {
            Offset(directionX / directionLength, directionY / directionLength)
        } else {
            fallbackDirection
        }
        val distanceToEdge = distanceToArenaEdge(
            position = pusher.position,
            radius = pusher.radius,
            direction = direction,
            world = world,
        )
        val lungeDistance = distanceToEdge * PUSH_LUNGE_DISTANCE_RATIO
        if (lungeDistance <= 0f) return

        activePushLunge = PushLunge(
            playerId = playerId,
            opponentId = opponent.id,
            velocityPerTick = Offset(
                x = direction.x * lungeDistance / PUSH_LUNGE_TICK_COUNT,
                y = direction.y * lungeDistance / PUSH_LUNGE_TICK_COUNT,
            ),
            ticksRemaining = PUSH_LUNGE_TICK_COUNT,
        )
        clearRapidTapPower()
        _activePushLungePlayerId.value = playerId
    }

    private fun distanceToArenaEdge(
        position: Offset,
        radius: Float,
        direction: Offset,
        world: GameWorld,
    ): Float {
        val safeArenaRadius = (world.arena.radius - radius).coerceAtLeast(0f)
        val fromCentreX = position.x - world.arena.centre.x
        val fromCentreY = position.y - world.arena.centre.y
        val projectedDistance = fromCentreX * direction.x + fromCentreY * direction.y
        val distanceToBoundarySquared =
            safeArenaRadius * safeArenaRadius -
                fromCentreX * fromCentreX -
                fromCentreY * fromCentreY +
                projectedDistance * projectedDistance
        if (distanceToBoundarySquared <= 0f) return 0f

        return (-projectedDistance + sqrt(distanceToBoundarySquared)).coerceAtLeast(0f)
    }

    private fun advancePushLunge() {
        val lunge = activePushLunge ?: return
        if (lunge.ticksRemaining <= 1) {
            clearPushLunge()
        } else {
            activePushLunge = lunge.copy(ticksRemaining = lunge.ticksRemaining - 1)
        }
    }

    private fun clearPushLunge() {
        activePushLunge = null
        _activePushLungePlayerId.value = null
    }

    private fun startPullAction(world: GameWorld, playerId: String) {
        if (activePushLunge != null || activePullAction != null) return

        val (attacker, defender) = powerMoveParticipants(world, playerId) ?: return
        val contactDistance = attacker.radius + defender.radius
        val distance = distanceBetween(attacker.position, defender.position)
        val canReachDefender = distance <= contactDistance * PULL_REACH_MULTIPLIER

        if (areRikishiInContact(attacker, defender)) {
            if (!startPullTurn(world, playerId, defender.id)) {
                startFailedPullLunge(
                    world = world,
                    attacker = attacker,
                    defender = defender,
                )
            }
            clearRapidTapPower()
            return
        }

        val direction = Offset(
            x = (defender.position.x - attacker.position.x) / distance,
            y = (defender.position.y - attacker.position.y) / distance,
        )
        val lungeDistance = minOf(
            distance - contactDistance,
            contactDistance,
        )
        activePullAction = PullAction.Lunge(
            attackerId = playerId,
            defenderId = defender.id,
            shouldAttemptTurn = canReachDefender,
            velocityPerTick = Offset(
                x = direction.x * lungeDistance / PULL_LUNGE_TICK_COUNT,
                y = direction.y * lungeDistance / PULL_LUNGE_TICK_COUNT,
            ),
            ticksRemaining = PULL_LUNGE_TICK_COUNT,
        )
        clearRapidTapPower()
    }

    private fun advancePullAction(world: GameWorld) {
        when (val action = activePullAction) {
            is PullAction.Lunge -> {
                if (action.shouldAttemptTurn && arePullingRikishiInContact(world, action)) {
                    if (!startPullTurn(world, action.attackerId, action.defenderId)) {
                        clearPullAction()
                    }
                } else if (action.ticksRemaining <= 1) {
                    clearPullAction()
                } else {
                    activePullAction = action.copy(ticksRemaining = action.ticksRemaining - 1)
                }
            }
            is PullAction.Turn -> {
                if (action.ticksRemaining <= 1) {
                    clearPullAction()
                } else {
                    activePullAction = action.copy(ticksRemaining = action.ticksRemaining - 1)
                }
            }
            null -> Unit
        }
    }

    private fun startFailedPullLunge(
        world: GameWorld,
        attacker: RikishiBody,
        defender: RikishiBody,
    ) {
        val distance = distanceBetween(attacker.position, defender.position)
        val direction = if (distance > 0f) {
            Offset(
                x = (defender.position.x - attacker.position.x) / distance,
                y = (defender.position.y - attacker.position.y) / distance,
            )
        } else if (attacker.id == world.topRikishi.id) {
            Offset(0f, 1f)
        } else {
            Offset(0f, -1f)
        }
        val lungeDistance = (attacker.radius + defender.radius) *
            PULL_FAILED_CONTACT_LUNGE_DISTANCE_RATIO

        activePullAction = PullAction.Lunge(
            attackerId = attacker.id,
            defenderId = defender.id,
            shouldAttemptTurn = false,
            velocityPerTick = Offset(
                x = direction.x * lungeDistance / PULL_LUNGE_TICK_COUNT,
                y = direction.y * lungeDistance / PULL_LUNGE_TICK_COUNT,
            ),
            ticksRemaining = PULL_LUNGE_TICK_COUNT,
        )
    }

    private fun startPullTurn(
        world: GameWorld,
        attackerId: String,
        defenderId: String,
    ): Boolean {
        val (attacker, defender) = powerMoveParticipants(world, attackerId) ?: return false
        if (defender.id != defenderId) return false

        val defenderRelativeToAttacker = Offset(
            x = defender.position.x - attacker.position.x,
            y = defender.position.y - attacker.position.y,
        )
        val defenderDistanceFromAttacker = distanceBetween(
            attacker.position,
            defender.position,
        )
        if (defenderDistanceFromAttacker == 0f) return false

        val attackerFromCentre = Offset(
            x = attacker.position.x - world.arena.centre.x,
            y = attacker.position.y - world.arena.centre.y,
        )
        val attackerDistanceFromCentre = distanceBetween(
            attacker.position,
            world.arena.centre,
        )
        if (attackerDistanceFromCentre == 0f) return false

        val optimalDefenderRelativePosition = Offset(
            x = attackerFromCentre.x / attackerDistanceFromCentre * defenderDistanceFromAttacker,
            y = attackerFromCentre.y / attackerDistanceFromCentre * defenderDistanceFromAttacker,
        )
        val currentDefenderDistanceFromCentre = distanceBetween(
            defender.position,
            world.arena.centre,
        )
        val optimalDefenderPosition = attacker.position + optimalDefenderRelativePosition
        val optimalDefenderDistanceFromCentre = distanceBetween(
            optimalDefenderPosition,
            world.arena.centre,
        )
        if (
            optimalDefenderDistanceFromCentre <=
                currentDefenderDistanceFromCentre + PULL_MIN_EDGE_GAIN
        ) {
            return false
        }

        val normalizedDot = (
            defenderRelativeToAttacker.x * optimalDefenderRelativePosition.x +
                defenderRelativeToAttacker.y * optimalDefenderRelativePosition.y
            ) / (defenderDistanceFromAttacker * defenderDistanceFromAttacker)
        val turnRadians = acos(normalizedDot.coerceIn(-1f, 1f).toDouble()).toFloat()
        if (turnRadians < PULL_MIN_TURN_RADIANS) return false

        val crossProduct =
            defenderRelativeToAttacker.x * optimalDefenderRelativePosition.y -
                defenderRelativeToAttacker.y * optimalDefenderRelativePosition.x
        val turnDirection = when {
            crossProduct > 0f -> 1f
            crossProduct < 0f -> -1f
            defenderRelativeToAttacker.x >= 0f -> 1f
            else -> -1f
        }
        activePullAction = PullAction.Turn(
            attackerId = attackerId,
            defenderId = defenderId,
            radiansPerTick = turnDirection * turnRadians / PULL_TURN_TICK_COUNT,
            ticksRemaining = PULL_TURN_TICK_COUNT,
        )
        _activePullPlayerId.value = attackerId
        return true
    }

    private fun pullTurnMovement(world: GameWorld, turn: PullAction.Turn): Offset? {
        val (attacker, defender) = powerMoveParticipants(world, turn.attackerId) ?: return null
        if (defender.id != turn.defenderId) return null

        val defenderRelativeToAttacker = Offset(
            x = defender.position.x - attacker.position.x,
            y = defender.position.y - attacker.position.y,
        )
        val rotatedRelativePosition = rotate(
            offset = defenderRelativeToAttacker,
            radians = turn.radiansPerTick,
        )
        return rotatedRelativePosition - defenderRelativeToAttacker
    }

    private fun arePullingRikishiInContact(
        world: GameWorld,
        action: PullAction.Lunge,
    ): Boolean {
        val (attacker, defender) = powerMoveParticipants(world, action.attackerId) ?: return false
        if (defender.id != action.defenderId) return false

        return areRikishiInContact(attacker, defender)
    }

    private fun powerMoveParticipants(
        world: GameWorld,
        attackerId: String,
    ): Pair<RikishiBody, RikishiBody>? =
        when (attackerId) {
            world.topRikishi.id -> world.topRikishi to world.bottomRikishi
            world.bottomRikishi.id -> world.bottomRikishi to world.topRikishi
            else -> null
        }

    private fun distanceBetween(first: Offset, second: Offset): Float {
        val x = first.x - second.x
        val y = first.y - second.y
        return sqrt(x * x + y * y)
    }

    private fun areRikishiInContact(first: RikishiBody, second: RikishiBody): Boolean =
        distanceBetween(first.position, second.position) <=
            first.radius + second.radius + RIKISHI_CONTACT_TOLERANCE

    private fun rotate(offset: Offset, radians: Float): Offset {
        val cosine = cos(radians.toDouble()).toFloat()
        val sine = sin(radians.toDouble()).toFloat()
        return Offset(
            x = offset.x * cosine - offset.y * sine,
            y = offset.x * sine + offset.y * cosine,
        )
    }

    private fun clearPullAction() {
        activePullAction = null
        _activePullPlayerId.value = null
    }

    private data class PowerMoveCommand(
        val playerId: String,
        val powerMove: PowerMove,
    )

    private data class RapidTapCommand(
        val playerId: String,
        val direction: Offset,
        val timestampMillis: Long,
    )

    private data class RapidTapState(
        var lastTapTimestampMillis: Long? = null,
        val intervalsMillis: MutableList<Long> = mutableListOf(),
    )

    private data class PushLunge(
        val playerId: String,
        val opponentId: String,
        val velocityPerTick: Offset,
        val ticksRemaining: Int,
    )

    private sealed class PullAction {
        abstract val attackerId: String
        abstract val defenderId: String

        data class Lunge(
            override val attackerId: String,
            override val defenderId: String,
            val shouldAttemptTurn: Boolean,
            val velocityPerTick: Offset,
            val ticksRemaining: Int,
        ) : PullAction()

        data class Turn(
            override val attackerId: String,
            override val defenderId: String,
            val radiansPerTick: Float,
            val ticksRemaining: Int,
        ) : PullAction()
    }

    companion object {
        const val TICK_MS = 16L  // ~60 Hz
        private const val PUSH_LUNGE_TICK_COUNT = 18
        private const val PUSH_LUNGE_DISTANCE_RATIO = 0.5f
        // Two rikishi widths is the maximum distance from which a pull can connect.
        private const val PULL_REACH_MULTIPLIER = 2f
        private const val RIKISHI_CONTACT_TOLERANCE = 1f
        private const val PULL_LUNGE_TICK_COUNT = 12
        private const val PULL_TURN_TICK_COUNT = 24
        private const val PULL_FAILED_CONTACT_LUNGE_DISTANCE_RATIO = 0.5f
        private const val PULL_MIN_EDGE_GAIN = 1f
        private const val PULL_MIN_TURN_RADIANS = 0.017453292f
        private const val RAPID_TAP_STREAK_GAP_MILLIS = 500L
        private const val RAPID_TAP_FORCE_TIMEOUT_MILLIS = 300L
        private const val RAPID_TAP_RATE_SAMPLE_COUNT = 5
        private const val MAX_RAPID_TAPS_PER_SECOND = 25f
        private const val RAPID_TAP_MAX_FORCE_PER_TICK = 6f
        private const val MILLIS_PER_SECOND = 1_000f
    }
}
