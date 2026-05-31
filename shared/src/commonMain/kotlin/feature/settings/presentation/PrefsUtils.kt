package feature.settings.presentation

import androidx.compose.runtime.Composable

fun Any?.ifNotNullThen(content: @Composable () -> Unit): (@Composable () -> Unit)? =
    if (this != null) content else null
