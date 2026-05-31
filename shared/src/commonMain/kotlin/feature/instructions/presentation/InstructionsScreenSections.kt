package feature.instructions.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.theme.colorHomeLightGreen
import feature.instructions.domain.model.InstructionsScreenSectionType
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.drag_up
import sumo.shared.generated.resources.major

data class InfoScreenSectionData(val instructionsScreenSectionType: InstructionsScreenSectionType)

@Composable
fun InstructionsScreenSections() {
    val instructionsScreenSectionTypes = InstructionsScreenSectionType.entries.toTypedArray()
    val isExpandedMap = remember {
        List(instructionsScreenSectionTypes.size) { index: Int ->
            val isThisTypeSelected =
                instructionsScreenSectionTypes[index] == InstructionsScreenSectionType.Overview
            index to isThisTypeSelected
        }.toMutableStateMap()
    }
    Column {
        LazyColumn(
            content = {
                instructionsScreenSectionTypes.onEachIndexed { index, sectionData ->
                    InfoScreenSection(
                        sectionData = InfoScreenSectionData(sectionData),
                        isExpanded = isExpandedMap[index] ?: true,
                        onHeaderClick = {
                            val wasExpanded = isExpandedMap[index] ?: false
                            isExpandedMap.keys.forEach { key -> isExpandedMap[key] = false }
                            isExpandedMap[index] = !wasExpanded
                        }
                    )
                }
            }
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 6.dp,
            color = colorHomeLightGreen
        )
    }
}

@Suppress("FunctionName")
fun LazyListScope.InfoScreenSection(
    sectionData: InfoScreenSectionData,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit
) {
    item {
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 6.dp,
            color = colorHomeLightGreen
        )
    }
    item {
        InfoScreenSectionHeader(
            text = sectionData.instructionsScreenSectionType.label,
            isExpanded = isExpanded,
            onHeaderClicked = onHeaderClick
        )
    }
    item {
        if (isExpanded) {
            Column {
                InfoScreenItem(sectionData = sectionData)
            }
        }
    }
}

@Composable
fun InfoScreenSectionHeader(text: String, isExpanded: Boolean, onHeaderClicked: () -> Unit) {
    val font = FontFamily(Font(Res.font.major))
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onHeaderClicked() }
            .padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = text,
            fontFamily = font,
            fontSize = 30.sp,
            modifier = Modifier.weight(1.0f),
            color = colorHomeLightGreen
        )
        if (isExpanded) {
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) 0f else 180f,
                animationSpec = tween(500), label = ""
            )
            Icon(
                painter = painterResource(resource = Res.drawable.drag_up),
                tint = colorHomeLightGreen,
                modifier = Modifier
                    .rotate(rotation)
                    .size(52.dp),
                contentDescription = "",
            )
        } else {
            val rotation by animateFloatAsState(
                targetValue = if (isExpanded) -180f else 180f,
                animationSpec = tween(1000), label = ""
            )
            Icon(
                painter = painterResource(resource = Res.drawable.drag_up),
                tint = colorHomeLightGreen,
                modifier = Modifier
                    .rotate(rotation)
                    .size(52.dp),
                contentDescription = "",
            )
        }
    }
}

@Composable
fun InfoScreenItem(
    modifier: Modifier = Modifier,
    sectionData: InfoScreenSectionData
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        when (sectionData.instructionsScreenSectionType) {
            InstructionsScreenSectionType.Overview -> {
                OverviewInstructions()
            }

            InstructionsScreenSectionType.Strength -> {
                StrengthInstructions()
            }

            InstructionsScreenSectionType.Agility -> {
                AgilityInstructions()
            }

            InstructionsScreenSectionType.Focus -> {
                FocusInstructions()
            }
        }
    }
}
