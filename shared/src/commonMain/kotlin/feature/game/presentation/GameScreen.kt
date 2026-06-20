package feature.game.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import app.presentation.HealthBar
import app.theme.playerOneColor
import app.theme.playerTwoColor
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import feature.game.presentation.ui.ActionPanel
import feature.game.presentation.ui.Janome
import feature.game.presentation.ui.IntroCountdownView
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.sand_medium

@Composable
fun GameScreen(
    showInterstitialAd: () -> Unit
) {
    val viewModel: GameViewModel = koinInject()
    val state by viewModel.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val resetThumbPositions by viewModel.resetThumbPositions

    LaunchedEffect(Unit) {
        showInterstitialAd()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.onIntent(GameIntent.GameOver())
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painter = painterResource(Res.drawable.sand_medium),
                contentScale = ContentScale.FillBounds
            ),
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Top Action Panel
            ActionPanel(
                state = state,
                actionPanelType = state.ui.topActionPanel.actionPanelType,
                actionPanelState = state.topPlayer.thumbPrintActionPanelState,
                player = state.topPlayer,
                onIntent = viewModel::onIntent,
                modifier = Modifier.rotate(180f)
            )
            HealthBar(
                health = state.topPlayer.health,
                foregroundColor = playerTwoColor,
                backgroundColor = playerTwoColor.copy(alpha = 0.25f),
                height = 14.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            Janome(
                state = state,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                onDamageDetected = {
                    viewModel.onIntent(GameIntent.PlayerDamaged(it))
                },
                onPressed = { isPressed, player ->
                    viewModel.onIntent(GameIntent.PressStateChanged(isPressed, player))
                },
                onReleased = { player ->
                    viewModel.onIntent(GameIntent.LockThumbs(player = player))
                },
                resetThumbPositions = resetThumbPositions,
                onIntent = viewModel::onIntent
            )
            HealthBar(
                health = state.bottomPlayer.health,
                foregroundColor = playerOneColor,
                backgroundColor = playerOneColor.copy(alpha = 0.25f),
                height = 14.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            // Bottom Action Panel
            ActionPanel(
                state = state,
                actionPanelType = state.ui.bottomActionPanel.actionPanelType,
                actionPanelState = state.bottomPlayer.thumbPrintActionPanelState,
                player = state.bottomPlayer,
                onIntent = viewModel::onIntent,
                modifier = Modifier
            )
        }
    }
    IntroCountdownView(state = state.startCountdownViewState)
}