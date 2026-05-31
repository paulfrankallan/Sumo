package feature.instructions.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.presentation.TopBar
import app.theme.colorHomeDarkGreen
import org.jetbrains.compose.resources.stringResource
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.instructions

@Composable
fun InstructionsScreen(
    showInterstitialAd: () -> Unit,
) {
    LaunchedEffect(Unit) {
        showInterstitialAd()
    }
    InfoScreenContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoScreenContent(
) {
    Scaffold(
        topBar = {
            TopBar(
                titleStart = stringResource(Res.string.instructions),
                shouldHideAllActions = true,
            )
        },
        containerColor = colorHomeDarkGreen
    ) { innerPadding ->
        InstructionsScreenView(
            innerPadding = innerPadding,
        )
    }
}

@Composable
fun InstructionsScreenView(
    innerPadding: PaddingValues,
) {
    Box(
        contentAlignment = Alignment.TopCenter,
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        InstructionsScreenSections()
    }
}