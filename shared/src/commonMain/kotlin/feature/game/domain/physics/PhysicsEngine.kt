package feature.game.domain.physics

import androidx.compose.ui.geometry.Offset
import feature.game.domain.input.InputCommand
import feature.game.domain.model.ArenaWorld
import feature.game.domain.model.GameWorld
import feature.game.domain.model.RikishiBody
import kotlin.math.sqrt

// ── Result types ─────────────────────────────────────────────────────────────

/**
 * Result of constraining a [RikishiBody] to the arena boundary.
 */
sealed class ConstrainResult {
    abstract val body: RikishiBody

    /** Body is fully inside the arena. */
    data class Inside(override val body: RikishiBody) : ConstrainResult()

    /** Body's edge touched or crossed the Tawara — damage should be applied. */
    data class BoundaryViolation(override val body: RikishiBody) : ConstrainResult()
}

/**
 * An event produced by a single physics step.
 */
sealed class PhysicsEvent {
    /** A Rikishi touched or crossed the Tawara edge. */
    data class BoundaryViolation(val playerId: String) : PhysicsEvent()

    /** Two Rikishi collided (for future effects: sounds, visual feedback). */
    data class RikishiCollision(val initiatorId: String, val receiverId: String) : PhysicsEvent()
}

/**
 * The full output of one [PhysicsEngine.step] call.
 */
data class PhysicsResult(
    val world: GameWorld,
    val events: List<PhysicsEvent>,
)

// ── Engine ────────────────────────────────────────────────────────────────────

/**
 * Pure, stateless physics engine for the Sumo game.
 *
 * All functions are deterministic and free of side effects — they take immutable
 * inputs and return new values. No coroutines, no Compose state, fully unit-testable.
 */
object PhysicsEngine {

    /**
     * Advances the physics world by one tick.
     *
     * Order of operations each tick:
     * 1. Apply [commands] as velocity deltas to the relevant Rikishi.
     * 2. Detect and resolve Rikishi–Rikishi overlap (push physics).
     * 3. Constrain each Rikishi to the arena boundary.
     * 4. Collect and return any [PhysicsEvent]s produced.
     *
     * The [commands] list may contain multiple commands for the same player (e.g. joystick
     * AND drag active simultaneously). They are summed before application.
     */
    fun step(world: GameWorld, commands: List<InputCommand>): PhysicsResult {
        val events = mutableListOf<PhysicsEvent>()

        // 1. Apply input velocity — sum commands per player so both input modes work together.
        var top = applyCommands(world.topRikishi, commands)
        var bottom = applyCommands(world.bottomRikishi, commands)

        // 2. Resolve Rikishi–Rikishi collision.
        if (doOverlap(top, bottom)) {
            val collision = resolveCollision(top, bottom)
            top = collision.first
            bottom = collision.second
            events += PhysicsEvent.RikishiCollision(top.id, bottom.id)
        }

        // 3. Constrain to arena. Check post-push positions — if the push sent a Rikishi
        //    out of bounds that also counts as a boundary violation.
        val topConstrain = constrainToArena(top, world.arena)
        val bottomConstrain = constrainToArena(bottom, world.arena)

        if (topConstrain is ConstrainResult.BoundaryViolation)
            events += PhysicsEvent.BoundaryViolation(top.id)
        if (bottomConstrain is ConstrainResult.BoundaryViolation)
            events += PhysicsEvent.BoundaryViolation(bottom.id)

        return PhysicsResult(
            world = world.copy(
                topRikishi = topConstrain.body,
                bottomRikishi = bottomConstrain.body,
            ),
            events = events,
        )
    }

    // ── Internal helpers (internal for testing) ───────────────────────────────

    /**
     * Applies all [InputCommand]s addressed to [body]'s player ID as a summed velocity delta.
     */
    internal fun applyCommands(body: RikishiBody, commands: List<InputCommand>): RikishiBody {
        val delta = commands
            .filter { it.playerId == body.id }
            .fold(Offset.Zero) { acc, cmd -> acc + cmd.velocityVector }
        return if (delta == Offset.Zero) body
        else body.copy(position = body.position + delta)
    }

    /**
     * Returns true when the two Rikishi circles overlap.
     */
    internal fun doOverlap(a: RikishiBody, b: RikishiBody): Boolean {
        val dx = a.position.x - b.position.x
        val dy = a.position.y - b.position.y
        val distSq = dx * dx + dy * dy
        val minDist = a.radius + b.radius
        return distSq <= minDist * minDist
    }

    /**
     * Resolves an overlap between two Rikishi by pushing them apart along the
     * collision normal. The separation is split equally between both bodies so
     * neither player gets systematic priority.
     *
     * Returns a [Pair] of (updatedA, updatedB) with non-overlapping positions.
     */
    internal fun resolveCollision(a: RikishiBody, b: RikishiBody): Pair<RikishiBody, RikishiBody> {
        val dx = b.position.x - a.position.x
        val dy = b.position.y - a.position.y
        val dist = sqrt(dx * dx + dy * dy)

        if (dist == 0f) {
            // Perfectly coincident — nudge apart on a fixed axis.
            return a.copy(position = a.position + Offset(-a.radius * 0.5f, 0f)) to
                    b.copy(position = b.position + Offset(b.radius * 0.5f, 0f))
        }

        val nx = dx / dist   // collision normal (a → b)
        val ny = dy / dist

        val overlap = (a.radius + b.radius) - dist
        val halfOverlap = overlap / 2f + SEPARATION_EPSILON

        return a.copy(position = Offset(a.position.x - nx * halfOverlap, a.position.y - ny * halfOverlap)) to
                b.copy(position = Offset(b.position.x + nx * halfOverlap, b.position.y + ny * halfOverlap))
    }

    /**
     * Checks whether [body] is touching or has crossed the [arena] boundary.
     * If it has, returns [ConstrainResult.BoundaryViolation]; otherwise [ConstrainResult.Inside].
     * The body's position is NOT moved — the caller decides what to do (damage + reset).
     */
    internal fun constrainToArena(body: RikishiBody, arena: ArenaWorld): ConstrainResult {
        val dx = body.position.x - arena.centre.x
        val dy = body.position.y - arena.centre.y
        val distFromCentre = sqrt(dx * dx + dy * dy)
        return if (distFromCentre + body.radius >= arena.radius)
            ConstrainResult.BoundaryViolation(body)
        else
            ConstrainResult.Inside(body)
    }
}

/** Small gap added after collision resolution to avoid re-triggering on the next tick. */
private const val SEPARATION_EPSILON = 0.1f
