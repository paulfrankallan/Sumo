package feature.game.joystick.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.toSize
import feature.game.joystick.core.control.BackgroundType
import feature.game.joystick.core.control.DirectionType
import feature.game.joystick.core.geometry.Radius
import feature.game.joystick.extensions.modifier.gestures.joystickMovement
import feature.game.joystick.ui.scope.ControlDrawScope
import feature.game.joystick.ui.scope.ControlScope
import feature.game.joystick.ui.scope.draw.image.ImageDrawProperties
import feature.game.joystick.ui.scope.draw.image.drawImage
import feature.game.joystick.ui.scope.draw.path.WedgeProperties
import feature.game.joystick.ui.scope.draw.path.drawWedge
import feature.game.joystick.ui.scope.draw.shapes.ArcProperties
import feature.game.joystick.ui.scope.draw.shapes.CircleArcProperties
import feature.game.joystick.ui.scope.draw.shapes.CircleProperties
import feature.game.joystick.ui.scope.draw.shapes.drawArc
import feature.game.joystick.ui.scope.draw.shapes.drawCircle
import feature.game.joystick.ui.scope.draw.shapes.drawCircleArc
import feature.game.joystick.ui.scope.rememberControlScope
import feature.game.joystick.ui.state.JoystickEventsHolder
import feature.game.joystick.ui.state.JoystickMoveListener
import feature.game.joystick.ui.state.JoystickState
import feature.game.joystick.ui.tokens.JoystickDefaults

/**
 * Remembers and creates a [JoystickState] that survives configuration changes.
 *
 * The state is saved and restored automatically using [rememberSaveable], preserving
 * the joystick position across configuration changes like screen rotations.
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState(
 *     invalidRadius = Radius.Ratio(0.15f),
 *     directionType = DirectionType.Complete
 * )
 * ```
 *
 * @param invalidRadius The radius within which the joystick is considered invalid.
 *                      Can be a fixed pixel value or a ratio of the joystick radius.
 * @param directionType The type of directional input system.
 */
@Composable
fun rememberJoystickState(
    invalidRadius: Radius = JoystickDefaults.radius,
    directionType: DirectionType = JoystickDefaults.directionType,
): JoystickState = rememberSaveable(
    inputs = arrayOf(invalidRadius, directionType),
    saver = JoystickState.Saver(invalidRadius, directionType)
) {
    JoystickState(invalidRadius, directionType)
}

/**
 * Remembers and creates a [JoystickEventsHolder] for the given joystick state.
 *
 * The holder manages a shared flow of joystick events and should be remembered to avoid
 * recreating the event flow on every recomposition. It's automatically recreated when
 * the state instance changes.
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventHolder(state)
 *
 * LaunchedEffect(holder) {
 *     holder.events.collect { event ->
 *         when (event) {
 *             is JoystickMoving -> handleMove(event.snapshot)
 *             else -> {}
 *         }
 *     }
 * }
 * ```
 *
 * @param state The joystick state to create the holder for
 */
@Composable
fun rememberJoystickEventHolder(state: JoystickState): JoystickEventsHolder =
    remember(state) { JoystickEventsHolder(state) }

/**
 * Base composable for creating a virtual joystick without gesture handling.
 *
 * This is the foundation component that provides the joystick layout structure with proper
 * sizing and state management. It doesn't include gesture detection.
 * Use this when you need to add custom gesture handling or build your own joystick variants.
 *
 * The joystick automatically:
 * - Maintains a 1:1 aspect ratio
 * - Updates the state size when the component is measured
 * - Resets the joystick position on size changes
 * - Provides a [ControlScope] for rendering joystick components
 *
 * Example usage for custom joystick:
 * ```
 * BaseVirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     state = rememberJoystickState()
 * ) {
 *     JoystickBackground()
 *     JoystickCanvas {
 *         drawCircle(CircleDrawDefaults.properties())
 *     }
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container. Will be combined with
 *                 aspect ratio and size change detection modifiers.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param content The composable content that receives a [ControlScope] for rendering joystick
 *                visuals like backgrounds, canvases, and custom drawings.
 */
@Composable
fun BaseVirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    content: @Composable (ControlScope.() -> Unit),
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .onSizeChanged {
                state.apply {
                    size = it.toSize()
                    reset()
                }
            }
    ) {
        val scope = rememberControlScope(state)

        scope.content()
    }
}

/**
 * Base composable for creating a virtual joystick with callback-based gesture handling.
 *
 * This version extends the basic implementation by adding automatic gesture detection
 * and callback-based event handling. It's ideal for scenarios where you need direct control
 * over joystick events through simple callbacks rather than observing event flows.
 *
 * The joystick automatically:
 * - Detects pointer movement and converts it to joystick position updates
 * - Invokes callbacks at appropriate gesture lifecycle points
 * - Provides snapshot-based state information to callbacks
 * - Handles threshold detection to filter out accidental touches
 * - Supports hold detection for continuous input while stationary
 *
 * Example usage:
 * ```
 * BaseVirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     state = rememberJoystickState(),
 *     onMove = { snapshot ->
 *         updatePlayerMovement(snapshot.direction, snapshot.strength)
 *     },
 *     onMoveStart = { snapshot ->
 *         playFeedback()
 *     }
 * ) {
 *     JoystickBackground()
 *     JoystickCanvas {
 *         drawCircleArc(CircleArcDrawDefaults.properties())
 *     }
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param onMove Required callback invoked during pointer movement and while held.
 * @param onMoveStart Optional callback invoked once when the gesture begins.
 * @param onMoveEnd Optional callback invoked once when the gesture ends.
 * @param threshold Optional minimum distance in pixels before movement is registered. Helps
 *                  prevent accidental activation. If null, any movement is detected.
 * @param interval Time in milliseconds between hold event callbacks.
 * @param content The composable content that receives a [ControlScope] for rendering.
 */
@Composable
fun BaseVirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    onMove: JoystickMoveListener,
    onMoveStart: JoystickMoveListener? = null,
    onMoveEnd: JoystickMoveListener? = null,
    threshold: Float? = null,
    interval: Long = JoystickDefaults.interval,
    content: @Composable (ControlScope.() -> Unit),
) {
    BaseVirtualJoystick(
        modifier = modifier
            .joystickMovement(
                state = state,
                onMove = onMove,
                onMoveStart = onMoveStart,
                onMoveEnd = onMoveEnd,
                threshold = threshold,
                interval = interval
            ),
        content = content,
        state = state
    )
}


/**
 * Creates a fully-featured virtual joystick with callback-based event handling and custom drawing content.
 *
 * This is the primary composable for creating custom joystick visualizations with complete control
 * over the rendering while still getting automatic gesture handling and callbacks.
 *
 * It combines:
 * - Automatic background rendering
 * - Custom drawing through [ControlDrawScope]
 * - Callback-based event handling
 * - Gesture detection without threshold support
 *
 * Example usage:
 * ```
 * VirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     state = rememberJoystickState(),
 *     onMove = { snapshot ->
 *         player.move(snapshot.direction, snapshot.strength)
 *     },
 *     backgroundType = BackgroundType.Default
 * ) {
 *     // Custom drawing in ControlDrawScope
 *     drawCircle(CircleDrawDefaults.properties())
 *     drawArc(ArcDrawDefaults.properties())
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param onMove Required callback invoked during pointer movement and while held.
 * @param onMoveStart Optional callback invoked once when the gesture begins.
 * @param onMoveEnd Optional callback invoked once when the gesture ends.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between hold event callbacks.
 * @param content Custom drawing content for rendering joystick visuals like circles, arcs, or images.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    onMove: JoystickMoveListener,
    onMoveStart: JoystickMoveListener? = null,
    onMoveEnd: JoystickMoveListener? = null,
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    content: (ControlDrawScope.() -> Unit),
) {
    BaseVirtualJoystick(
        modifier = modifier,
        state = state,
        onMove = onMove,
        onMoveStart = onMoveStart,
        onMoveEnd = onMoveEnd,
        interval = interval
    ) {
        JoystickBackground(type = backgroundType)
        JoystickCanvas(content = content)
    }
}

/**
 * Creates a virtual joystick with a circle knob and callback-based event handling.
 *
 * Convenience composable that provides a pre-configured joystick with a circular
 * indicator. The circle follows the joystick position and can be customized through properties.
 *
 * Example usage:
 * ```
 * VirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     properties = CircleDrawDefaults.properties(
 *         radius = Radius.Ratio(0.25f),
 *         brush = SolidColor(Color.Blue)
 *     ),
 *     onMove = { snapshot -> handleMovement(snapshot) }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param onMoveStart Optional callback invoked once when the gesture begins.
 * @param onMoveEnd Optional callback invoked once when the gesture ends.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between hold event callbacks.
 * @param properties The circle drawing properties .
 * @param onMove Required callback invoked during pointer movement and while held.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    onMoveStart: JoystickMoveListener? = null,
    onMoveEnd: JoystickMoveListener? = null,
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: CircleProperties,
    onMove: JoystickMoveListener,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        onMove = onMove,
        onMoveStart = onMoveStart,
        onMoveEnd = onMoveEnd,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawCircle(properties)
    }
}

/**
 * Creates a virtual joystick with an arc indicator and callback-based event handling.
 *
 * Convenience composable provides a joystick with an arc that extends from the outside
 * to indicate the current direction and angle. The arc includes an arrow at the end
 * and can be customized through properties.
 *
 * Example usage:
 * ```
 * VirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     properties = ArcDrawDefaults.properties(
 *         sweepAngle = 120f,
 *         strokeWidth = 15f
 *     ),
 *     onMove = { snapshot -> handleMovement(snapshot) }
 * )
 * ```
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param onMoveStart Optional callback invoked once when the gesture begins.
 * @param onMoveEnd Optional callback invoked once when the gesture ends.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between hold event callbacks.
 * @param properties The arc drawing properties.
 * @param onMove Required callback invoked during pointer movement and while held.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    onMoveStart: JoystickMoveListener? = null,
    onMoveEnd: JoystickMoveListener? = null,
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: ArcProperties,
    onMove: JoystickMoveListener,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        onMove = onMove,
        onMoveStart = onMoveStart,
        onMoveEnd = onMoveEnd,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawArc(properties)
    }
}

/**
 * Creates a virtual joystick with a combined circle and arc indicator with callback-based event handling.
 *
 * Convenience composable combines a circular indicator with an arc that extends.
 * This provides clear visual feedback of both the joystick
 * position and direction. Both components can be customized through properties.
 *
 * Example usage:
 * ```
 * VirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     properties = CircleArcDrawDefaults.properties(
 *         circle = CircleDrawDefaults.properties(radius = Radius.Ratio(0.2f)),
 *         arc = ArcDrawDefaults.properties(sweepAngle = 90f)
 *     ),
 *     onMove = { snapshot -> handleMovement(snapshot) }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param onMoveStart Optional callback invoked once when the gesture begins.
 * @param onMoveEnd Optional callback invoked once when the gesture ends.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between hold event callbacks.
 * @param properties The combined circle and arc drawing properties.
 * @param onMove Required callback invoked during pointer movement and while held.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    onMoveStart: JoystickMoveListener? = null,
    onMoveEnd: JoystickMoveListener? = null,
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: CircleArcProperties,
    onMove: JoystickMoveListener,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        onMove = onMove,
        onMoveStart = onMoveStart,
        onMoveEnd = onMoveEnd,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawCircleArc(properties)
    }
}

/**
 * Creates a virtual joystick with a wedge-shaped directional indicator and callback-based event handling.
 *
 * Convenience composable provides a joystick with a wedge (pie-slice) that highlights
 * the current direction. The wedge can be drawn with curved or straight edges and
 * can be customized through properties.
 *
 * Example usage:
 * ```
 * VirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     properties = WedgeDrawDefaults.properties(
 *         mode = WedgeMode.Curve,
 *         radius = Radius.Ratio(0.3f)
 *     ),
 *     onMove = { snapshot -> handleMovement(snapshot) }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param onMoveStart Optional callback invoked once when the gesture begins.
 * @param onMoveEnd Optional callback invoked once when the gesture ends.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between hold event callbacks.
 * @param properties The wedge drawing properties.
 * @param onMove Required callback invoked during pointer movement and while held.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    onMoveStart: JoystickMoveListener? = null,
    onMoveEnd: JoystickMoveListener? = null,
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: WedgeProperties,
    onMove: JoystickMoveListener,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        onMove = onMove,
        onMoveStart = onMoveStart,
        onMoveEnd = onMoveEnd,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawWedge(properties)
    }
}

/**
 * Creates a virtual joystick with an image-based knob and callback-based event handling.
 *
 * Convenience composable provides a joystick with a custom image that follows the
 * joystick position. The image can be any painter (bitmap, vector, etc.) and
 * can be customized through properties.
 *
 * Example usage:
 * ```
 * VirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     properties = ImageDrawDefaults.properties(
 *         painter = /*Your painter instance*/,
 *         size = Size(100f, 100f)
 *     ),
 *     onMove = { snapshot -> handleMovement(snapshot) }
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param onMoveStart Optional callback invoked once when the gesture begins.
 * @param onMoveEnd Optional callback invoked once when the gesture ends.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between hold event callbacks.
 * @param properties The image drawing properties.
 * @param onMove Required callback invoked during pointer movement and while held.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    onMoveStart: JoystickMoveListener? = null,
    onMoveEnd: JoystickMoveListener? = null,
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: ImageDrawProperties,
    onMove: JoystickMoveListener,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        onMove = onMove,
        onMoveStart = onMoveStart,
        onMoveEnd = onMoveEnd,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawImage(properties)
    }
}

/**
 * Base composable for creating a virtual joystick with event-based gesture handling.
 *
 * This version uses a [JoystickEventsHolder] to emit events through a shared flow, providing
 * a reactive approach to joystick input. This is ideal for scenarios where:
 * - Multiple components need to observe the same joystick events
 * - You want to use coroutine-based event collection
 * - You need to decouple event producers from consumers
 * - You want to apply flow operators (buffer, debounce, etc.)
 *
 * The joystick automatically:
 * - Detects pointer movement and converts it to joystick position updates
 * - Emits appropriate events (start, move, held, end) through the holder
 * - Provides snapshot-based state information in events
 * - Handles continuous event emission during hold
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventHolder(state)
 *
 * LaunchedEffect(holder) {
 *     holder.events.collect { event ->
 *         when (event) {
 *             is JoystickMoving -> handleMove(event.snapshot)
 *             else -> {}
 *         }
 *     }
 * }
 *
 * BaseVirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     state = state,
 *     holder = holder
 * ) {
 *     JoystickBackground()
 *     JoystickCanvas {
 *         drawWedge(WedgeDrawDefaults.properties())
 *     }
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param holder The events holder that manages the shared flow of joystick events.
 * @param interval Time in milliseconds between held event emissions.
 * @param content The composable content that receives a [ControlScope] for rendering.
 */
@Composable
fun BaseVirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    holder: JoystickEventsHolder = rememberJoystickEventHolder(state),
    interval: Long = JoystickDefaults.interval,
    content: @Composable (ControlScope.() -> Unit),
) {
    BaseVirtualJoystick(
        modifier = modifier.joystickMovement(
            state = state,
            holder = holder,
            interval = interval,
        ),
        content = content,
        state = state
    )
}

/**
 * Creates a fully-featured virtual joystick with event-based handling and custom drawing content.
 *
 * Composable uses a [JoystickEventsHolder] to emit events through a shared flow, providing
 * a reactive approach to joystick input. It's ideal when you need:
 * - Multiple observers for the same joystick events
 * - Coroutine-based event processing
 * - Custom visual rendering with full drawing control
 * - Flow operators for event transformation
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventHolder(state)
 *
 * LaunchedEffect(holder) {
 *     holder.events.collect { event ->
 *         when (event) {
 *             is JoystickMoving -> handleMove(event.snapshot)
 *             else -> {}
 *         }
 *     }
 * }
 *
 * VirtualJoystick(
 *     modifier = Modifier.size(200.dp),
 *     state = state,
 *     holder = holder
 * ) {
 *     drawCircle(CircleDrawDefaults.properties())
 *     drawWedge(WedgeDrawDefaults.properties())
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param holder The events holder that manages the shared flow of joystick events.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between held event emissions.
 * @param content Custom drawing lambda that receives a [ControlDrawScope] for rendering.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    holder: JoystickEventsHolder = rememberJoystickEventHolder(state),
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    content: (ControlDrawScope.() -> Unit),
) {
    BaseVirtualJoystick(
        modifier = modifier,
        state = state,
        holder = holder,
        interval = interval
    ) {
        JoystickBackground(type = backgroundType)
        JoystickCanvas(content = content)
    }
}

/**
 * Creates a virtual joystick with a circle knob and event-based handling.
 *
 * Convenience composable that combines event-based input handling with a pre-configured
 * circle indicator. Observers collect events from the holder's shared flow.
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventHolder(state)
 *
 * LaunchedEffect(holder) {
 *     holder.events.collect { event ->
 *         when (event) {
 *             is JoystickMoving -> handleMove(event.snapshot)
 *             else -> {}
 *         }
 *     }
 * }
 *
 * VirtualJoystick(
 *     state = state,
 *     holder = holder,
 *     properties = CircleDrawDefaults.properties()
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param holder The events holder that manages the shared flow of joystick events.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between hold event emissions.
 * @param properties The circle drawing properties .
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    holder: JoystickEventsHolder = rememberJoystickEventHolder(state),
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: CircleProperties,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        holder = holder,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawCircle(properties)
    }
}

/**
 * Creates a virtual joystick with an arc indicator and event-based handling.
 *
 * Convenience composable that combines event-based input handling with a pre-configured
 * arc indicator. Observers collect events from the holder's shared flow.
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventHolder(state)
 *
 * LaunchedEffect(holder) {
 *     holder.events.collect { event ->
 *         when (event) {
 *             is JoystickMoving -> handleMove(event.snapshot)
 *             else -> {}
 *         }
 *     }
 * }
 *
 * VirtualJoystick(
 *     state = state,
 *     holder = holder,
 *     properties = ArcDrawDefaults.properties()
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param holder The events holder that manages the shared flow of joystick events.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between hold event emissions.
 * @param properties The arc drawing properties.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    holder: JoystickEventsHolder = rememberJoystickEventHolder(state),
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: ArcProperties,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        holder = holder,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawArc(properties)
    }
}

/**
 * Creates a virtual joystick with a combined circle knob and arc indicator with event-based handling.
 *
 * Convenience composable that combines event-based input handling with pre-configured
 * circle and arc indicators. Observers collect events from the holder's shared flow.
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventHolder(state)
 *
 * LaunchedEffect(holder) {
 *     holder.events.collect { event ->
 *         when (event) {
 *             is JoystickMoving -> handleMove(event.snapshot)
 *             else -> {}
 *         }
 *     }
 * }
 *
 * VirtualJoystick(
 *     state = state,
 *     holder = holder,
 *     properties = CircleArcDrawDefaults.properties()
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param holder The events holder that manages the shared flow of joystick events.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between held event emissions.
 * @param properties The combined circle and arc drawing properties.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    holder: JoystickEventsHolder = rememberJoystickEventHolder(state),
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: CircleArcProperties,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        holder = holder,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawCircleArc(properties)
    }
}

/**
 * Creates a virtual joystick with a wedge-shaped directional indicator and event-based handling.
 *
 * Convenience composable that combines event-based input handling with a pre-configured
 * wedge indicator. Observers collect events from the holder's shared flow.
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventHolder(state)
 *
 * LaunchedEffect(holder) {
 *     holder.events.collect { event ->
 *         when (event) {
 *             is JoystickMoving -> handleMove(event.snapshot)
 *             else -> {}
 *         }
 *     }
 * }
 *
 * VirtualJoystick(
 *     state = state,
 *     holder = holder,
 *     properties = WedgeDrawDefaults.properties()
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param holder The events holder that manages the shared flow of joystick events.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between held event emissions.
 * @param properties The wedge drawing properties.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    holder: JoystickEventsHolder = rememberJoystickEventHolder(state),
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: WedgeProperties,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        holder = holder,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawWedge(properties)
    }
}

/**
 * Creates a virtual joystick with an image-based knob and event-based handling.
 *
 * Convenience composable that combines event-based input handling with a pre-configured
 * image knob. Observers collect events from the holder's shared flow.
 *
 * Example usage:
 * ```
 * val state = rememberJoystickState()
 * val holder = rememberJoystickEventHolder(state)
 *
 * LaunchedEffect(holder) {
 *     holder.events.collect { event ->
 *         when (event) {
 *             is JoystickMoving -> updatePlayerPosition(event.snapshot)
 *             else -> {}
 *         }
 *     }
 * }
 *
 * VirtualJoystick(
 *     state = state,
 *     holder = holder,
 *     properties = ImageDrawDefaults.properties(
 *         painter = painterResource("joystick_knob.png")
 *     )
 * )
 * ```
 *
 * @param modifier Modifier to be applied to the joystick container.
 * @param state The joystick state that manages position, direction, and other properties.
 * @param holder The events holder that manages the shared flow of joystick events.
 * @param backgroundType The type of background to display.
 * @param interval Time in milliseconds between held event emissions.
 * @param properties The image drawing properties.
 */
@Composable
fun VirtualJoystick(
    modifier: Modifier = Modifier,
    state: JoystickState = rememberJoystickState(),
    holder: JoystickEventsHolder = rememberJoystickEventHolder(state),
    backgroundType: BackgroundType = JoystickDefaults.backgroundType,
    interval: Long = JoystickDefaults.interval,
    properties: ImageDrawProperties,
) {
    VirtualJoystick(
        modifier = modifier,
        state = state,
        holder = holder,
        backgroundType = backgroundType,
        interval = interval
    ) {
        drawImage(properties)
    }
}