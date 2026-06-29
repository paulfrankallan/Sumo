package feature.game.domain.physics

import androidx.compose.ui.geometry.Offset
import feature.game.domain.input.InputCommand
import feature.game.domain.input.InputSource
import feature.game.domain.model.ArenaWorld
import feature.game.domain.model.GameWorld
import feature.game.domain.model.RikishiBody
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PhysicsEngineTest {

    // ── Test fixtures ──────────────────────────────────────────────────────────

    private val arena = ArenaWorld(centre = Offset(200f, 200f), radius = 180f)
    private val topRikishi = RikishiBody(id = "top", position = Offset(200f, 80f), radius = 30f)
    private val bottomRikishi = RikishiBody(id = "bottom", position = Offset(200f, 320f), radius = 30f)
    private val defaultWorld = GameWorld(arena, topRikishi, bottomRikishi)

    private fun cmd(id: String, v: Offset) = InputCommand(id, v, InputSource.JOYSTICK)

    // ── applyCommands ──────────────────────────────────────────────────────────

    @Test
    fun `applyCommands moves body by velocity vector`() {
        val result = PhysicsEngine.applyCommands(topRikishi, listOf(cmd("top", Offset(5f, 3f))))
        assertEquals(Offset(205f, 83f), result.position)
    }

    @Test
    fun `applyCommands sums multiple commands for same player`() {
        val cmds = listOf(cmd("top", Offset(2f, 0f)), cmd("top", Offset(3f, 1f)))
        val result = PhysicsEngine.applyCommands(topRikishi, cmds)
        assertEquals(Offset(205f, 81f), result.position)
    }

    @Test
    fun `applyCommands ignores commands for other players`() {
        val result = PhysicsEngine.applyCommands(topRikishi, listOf(cmd("bottom", Offset(10f, 10f))))
        assertEquals(topRikishi.position, result.position)
    }

    @Test
    fun `applyCommands returns same body when no commands`() {
        val result = PhysicsEngine.applyCommands(topRikishi, emptyList())
        assertEquals(topRikishi, result)
    }

    // ── doOverlap ─────────────────────────────────────────────────────────────

    @Test
    fun `doOverlap returns false for well-separated bodies`() {
        assertFalse(PhysicsEngine.doOverlap(topRikishi, bottomRikishi))
    }

    @Test
    fun `doOverlap returns true when bodies overlap`() {
        val close = topRikishi.copy(position = Offset(200f, 310f))
        assertTrue(PhysicsEngine.doOverlap(close, bottomRikishi))
    }

    @Test
    fun `doOverlap returns true when bodies are exactly touching`() {
        // distance == sum of radii → exactly touching → overlap
        val touching = topRikishi.copy(position = Offset(200f, 260f)) // 320-260=60 = 30+30
        assertTrue(PhysicsEngine.doOverlap(touching, bottomRikishi))
    }

    // ── resolveCollision ──────────────────────────────────────────────────────

    @Test
    fun `resolveCollision separates overlapping bodies`() {
        val a = topRikishi.copy(position = Offset(200f, 310f))
        val b = bottomRikishi
        val (newA, newB) = PhysicsEngine.resolveCollision(a, b)
        assertFalse(PhysicsEngine.doOverlap(newA, newB))
    }

    @Test
    fun `resolveCollision moves both bodies by equal amounts`() {
        val a = RikishiBody("a", Offset(200f, 195f), 30f)
        val b = RikishiBody("b", Offset(200f, 205f), 30f)
        val (newA, newB) = PhysicsEngine.resolveCollision(a, b)
        // Both should have moved by the same amount from their original positions
        val aMove = newA.position.y - a.position.y
        val bMove = b.position.y - newB.position.y
        assertEquals(aMove, bMove, absoluteTolerance = 0.01f)
    }

    // ── constrainToArena ──────────────────────────────────────────────────────

    @Test
    fun `constrainToArena returns Inside for body in centre`() {
        val result = PhysicsEngine.constrainToArena(topRikishi, arena)
        assertIs<ConstrainResult.Inside>(result)
    }

    @Test
    fun `constrainToArena returns BoundaryViolation when body edge touches ring`() {
        // body centre at 200,10: distFromCentre=190, radius=30 → 190+30=220 >= 180 → violation
        val edge = topRikishi.copy(position = Offset(200f, 10f))
        val result = PhysicsEngine.constrainToArena(edge, arena)
        assertIs<ConstrainResult.BoundaryViolation>(result)
    }

    @Test
    fun `constrainToArena returns BoundaryViolation when body is entirely outside`() {
        val outside = topRikishi.copy(position = Offset(200f, -50f))
        val result = PhysicsEngine.constrainToArena(outside, arena)
        assertIs<ConstrainResult.BoundaryViolation>(result)
    }

    // ── step (integration) ────────────────────────────────────────────────────

    @Test
    fun `step with no commands produces no events and unchanged positions`() {
        val result = PhysicsEngine.step(defaultWorld, emptyList())
        assertTrue(result.events.isEmpty())
        assertEquals(defaultWorld.topRikishi.position, result.world.topRikishi.position)
        assertEquals(defaultWorld.bottomRikishi.position, result.world.bottomRikishi.position)
    }

    @Test
    fun `step emits BoundaryViolation when player moves outside ring`() {
        val cmd = cmd("top", Offset(0f, -300f)) // push top far above ring
        val result = PhysicsEngine.step(defaultWorld, listOf(cmd))
        val violations = result.events.filterIsInstance<PhysicsEvent.BoundaryViolation>()
        assertTrue(violations.any { it.playerId == "top" })
    }

    @Test
    fun `step emits collision event when players overlap`() {
        val world = defaultWorld.copy(
            topRikishi = topRikishi.copy(position = Offset(200f, 310f))
        )
        val result = PhysicsEngine.step(world, emptyList())
        val collisions = result.events.filterIsInstance<PhysicsEvent.RikishiCollision>()
        assertTrue(collisions.isNotEmpty())
    }

    @Test
    fun `step emits BoundaryViolation for pushed player`() {
        // Top pushes bottom toward the edge — bottom should get boundary violation
        val world = defaultWorld.copy(
            topRikishi = topRikishi.copy(position = Offset(200f, 290f)),  // near bottom
            bottomRikishi = bottomRikishi.copy(position = Offset(200f, 355f)) // near edge
        )
        val cmd = cmd("top", Offset(0f, 10f)) // push top down toward bottom
        val result = PhysicsEngine.step(world, listOf(cmd))
        val violations = result.events.filterIsInstance<PhysicsEvent.BoundaryViolation>()
        // Either or both might get pushed out — at least one violation expected
        assertTrue(violations.isNotEmpty())
    }

    @Test
    fun `step does not emit duplicate BoundaryViolation for same player in one tick`() {
        val cmd = cmd("top", Offset(0f, -400f))
        val result = PhysicsEngine.step(defaultWorld, listOf(cmd))
        val topViolations = result.events
            .filterIsInstance<PhysicsEvent.BoundaryViolation>()
            .count { it.playerId == "top" }
        assertEquals(1, topViolations)
    }

    @Test
    fun `step can emit BoundaryViolation for both players simultaneously`() {
        val cmds = listOf(
            cmd("top", Offset(0f, -300f)),    // push top out
            cmd("bottom", Offset(0f, 300f)),  // push bottom out
        )
        val result = PhysicsEngine.step(defaultWorld, cmds)
        val playerIds = result.events
            .filterIsInstance<PhysicsEvent.BoundaryViolation>()
            .map { it.playerId }
        assertTrue(playerIds.contains("top"))
        assertTrue(playerIds.contains("bottom"))
    }
}

// Helper for approximate float comparison
private fun assertEquals(expected: Float, actual: Float, absoluteTolerance: Float) {
    assertTrue(
        kotlin.math.abs(expected - actual) <= absoluteTolerance,
        "Expected $expected but was $actual (tolerance $absoluteTolerance)"
    )
}
