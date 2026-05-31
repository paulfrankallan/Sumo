@file:OptIn(ExperimentalMaterialApi::class)

package feature.settings.presentation.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import app.theme.colorHomeLightGreen

@ExperimentalMaterialApi
@Composable
fun SwitchPref(
    key: String,
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    checked: Boolean? = null,
    defaultChecked: Boolean = false,
    onCheckedChange: ((Boolean) -> Unit)? = null,
    textColor: Color = colorHomeLightGreen,
    secondaryTextColor: Color = colorHomeLightGreen,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    edit: (String, Boolean) -> Unit
) {
    var internalChecked by remember { mutableStateOf(defaultChecked) }
    checked?.let {
        internalChecked = it
    }
    TextPref(
        title = title,
        modifier = modifier,
        textColor = textColor,
        secondaryTextColor = secondaryTextColor,
        summary = summary,
        darkenOnDisable = true,
        leadingIcon = leadingIcon,
        enabled = enabled,
        onClick = {
            internalChecked = !internalChecked
            edit(key, internalChecked)
        }
    ) {
        Switch(
            checked = internalChecked,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colorHomeLightGreen,
                checkedTrackColor = colorHomeLightGreen.copy(alpha = 0.5f),
                uncheckedThumbColor = Color.DarkGray,
                uncheckedTrackColor = Color.DarkGray.copy(alpha = 0.5f),
            ),
            onCheckedChange = {
                edit(key, it)
            }
        )
    }
}
