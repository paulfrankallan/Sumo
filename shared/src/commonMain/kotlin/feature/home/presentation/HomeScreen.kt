package feature.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import app.nav.NavController
import app.presentation.DialogWrapper
import app.theme.colorHomeDarkGreen
import co.touchlab.kermit.Logger
import feature.common.events.DialogEvent
import feature.common.presentation.Intent
import feature.game.nav.navigateToPlayGame
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.bg_1

@Composable
fun HomeScreen(
    showInterstitialAd: () -> Unit,
) {
    val viewModel: HomeViewModel = koinInject()
    val state by viewModel.state.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    Logger.d("LifecycleEvent ON_PAUSE called")
                    viewModel.onIntent(HomeIntent.StopMusic)
                }

                Lifecycle.Event.ON_RESUME -> {
                    Logger.d("LifecycleEvent ON_RESUME called")
                    viewModel.onIntent(HomeIntent.StartMusic)
                }

                else -> {
                    Logger.d("LifecycleEvent Event: $event")
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    state.events.filterIsInstance<DialogEvent>().firstOrNull()?.let { dialogEvent ->
        DialogWrapper(dialogEvent) {
            viewModel.onEventComplete(dialogEvent.id)
        }
    }
    HomeContent(
        state = state,
        onIntent = viewModel::onIntent,
    )
}

@Composable
fun HomeContent(
    state: HomeState,
    onIntent: (Intent) -> Unit,
) {
    Scaffold(
        containerColor = colorHomeDarkGreen,
        topBar = {
            HomeTopBar(
                musicOn = state.musicOn,
                toggleMusic = { onIntent(HomeIntent.ToggleMusic) }
            )
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(resource = Res.drawable.bg_1),
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize(),
            )
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                SumoLaunchBtn(
                    modifier = Modifier.weight(1f)
                )
                GameTypeSelectionButton(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 56.dp)
                        .height(50.dp),
                    onIntent = onIntent,
                    gameType = state.gameType
                )
            }
        }
    }
}

@Composable
fun SumoLaunchBtn(
    modifier: Modifier = Modifier,
) {
    val navigator = NavController.current
    val onClick = {
        navigator.navigateToPlayGame()
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        SpinningDiskImageButton(
            onClick = onClick
        )
    }
}
