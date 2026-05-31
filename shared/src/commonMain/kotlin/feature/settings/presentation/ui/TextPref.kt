package feature.settings.presentation.ui

import androidx.compose.foundation.clickable
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.theme.colorHomeLightGreen
import feature.settings.presentation.ifNotNullThen

@ExperimentalMaterialApi
@Composable
fun TextPref(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    darkenOnDisable: Boolean = false,
    minimalHeight: Boolean = false,
    onClick: () -> Unit = {},
    textColor: Color = colorHomeLightGreen,
    secondaryTextColor: Color = colorHomeLightGreen,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    PrefsListItem(
        text = { Text(title) },
        modifier = if (enabled) modifier.clickable { onClick() } else modifier,
        enabled = enabled,
        darkenOnDisable = darkenOnDisable,
        textColor = textColor,
        secondaryTextColor = secondaryTextColor,
        minimalHeight = minimalHeight,
        icon = leadingIcon,
        secondaryText = summary.ifNotNullThen { Text(summary!!) },
        trailing = trailingContent
    )
}