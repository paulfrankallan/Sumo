package feature.settings.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
@ExperimentalMaterialApi
fun PrefsListItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    darkenOnDisable: Boolean = true,
    icon: @Composable (() -> Unit)? = null,
    secondaryText: @Composable (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    secondaryTextColor: Color = MaterialTheme.colorScheme.onBackground,
    minimalHeight: Boolean = true,
    text: @Composable () -> Unit
) {

    val typography = MaterialTheme.typography

    val styledText = applyTextStyle(
        typography.headlineSmall,
        textColor,
        when {
            enabled || !darkenOnDisable -> ContentAlpha.high
            else -> ContentAlpha.disabled
        },
        text
    )!!
    val styledSecondaryText = applyTextStyle(
        typography.titleMedium,
        secondaryTextColor,
        when {
            enabled || !darkenOnDisable -> ContentAlpha.medium
            else -> ContentAlpha.disabled
        },
        secondaryText
    )
    val styledTrailing = applyTextStyle(
        typography.titleMedium,
        textColor,
        when {
            enabled || !darkenOnDisable -> ContentAlpha.high
            else -> ContentAlpha.disabled
        },
        trailing
    )

    AnyLine.CustomListItem(
        modifier = modifier,
        minimalHeight,
        icon,
        styledSecondaryText,
        styledTrailing,
        styledText
    )
}

private object AnyLine {
    private val MinHeight = 48.dp
    private val MinHeightSmaller = 32.dp
    private val IconMinPaddedWidth = 40.dp
    private val ContentPadding = 12.dp
    private val VerticalPadding = 10.dp
    private val SingleLinePadding = 4.dp //used when no secondary text is supplied

    @Composable
    fun CustomListItem(
        modifier: Modifier = Modifier,
        minimalHeight: Boolean = true,
        icon: @Composable (() -> Unit)? = null,
        secondaryText: (@Composable (() -> Unit))? = null,
        trailing: @Composable (() -> Unit)? = null,
        text: @Composable (() -> Unit)
    ) {
        Row(
            modifier
                .heightIn(min = if (minimalHeight) MinHeightSmaller else MinHeight)
                .padding(
                    start = ContentPadding,
                    end = ContentPadding,
                    top = when {
                        secondaryText == null && !minimalHeight -> SingleLinePadding
                        else -> VerticalPadding
                    },
                    bottom = when {
                        minimalHeight -> 0.dp
                        secondaryText == null -> SingleLinePadding
                        else -> VerticalPadding
                    }
                )
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (icon != null) {
                val minSize = IconMinPaddedWidth
                Box(
                    Modifier.sizeIn(minWidth = minSize, minHeight = minSize),
                    contentAlignment = Alignment.CenterStart
                ) { icon() }
            }

            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                text()
                secondaryText?.invoke()
            }
            trailing?.invoke()
        }
    }
}

private fun applyTextStyle(
    textStyle: TextStyle,
    textColor: Color,
    contentAlpha: Float,
    content: @Composable (() -> Unit)?
): @Composable (() -> Unit)? {
    if (content == null) return null
    return {
        val newTextStyle = textStyle.copy(
            color = textColor.copy(alpha = contentAlpha)
        )
        CompositionLocalProvider(
            LocalContentAlpha provides contentAlpha,
            LocalTextStyle provides newTextStyle
        ) {
            content()
        }
    }
}



