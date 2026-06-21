package feature.game.joystick.extensions

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputScope

/**
 * Detects tap gestures on the pointer input scope.
 *
 * This function awaits each gesture, invoking the onDown callback when a pointer is pressed down
 * and the onUp callback when the pointer is released (if not cancelled).
 *
 * @param onDown Callback invoked with the position when a pointer is pressed down.
 * @param onUp Callback invoked with the position when a pointer is released.
 */
suspend fun PointerInputScope.detectTapGestures(
    onDown: ((Offset) -> Unit)? = null,
    onUp: ((Offset) -> Unit)? = null,
) {
    awaitEachGesture {
        val down = awaitFirstDown()
        onDown?.invoke(down.position)

        val up = waitForUpOrCancellation()
        if(up != null)
            onUp?.invoke(up.position)
    }
}
