package platform.presentation

import androidx.compose.runtime.Composable

/**
 * A side-effect composable that prevents the screen from dimming or locking
 * while the calling composable is in the composition.
 *
 * The flag is automatically cleared when the composable leaves the composition.
 */
@Composable
expect fun KeepScreenOn()
