package feature.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.nav.NavController
import app.presentation.DialogWrapper
import co.touchlab.kermit.Logger
import feature.common.events.DialogEvent
import feature.common.presentation.Intent
import feature.game.nav.navigateToPlayGame
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import sumo.shared.generated.resources.Res
import sumo.shared.generated.resources.home_bg
import sumo.shared.generated.resources.home_sumo_hero
import sumo.shared.generated.resources.start_btn
import sumo.shared.generated.resources.start_btn_rip
import sumo.shared.generated.resources.sumo_title_2

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
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HomeTopBar(
                    musicOn = state.musicOn,
                    toggleMusic = { onIntent(HomeIntent.ToggleMusic) }
                )
            },
            modifier = Modifier
                .paint(
                    painter = painterResource(Res.drawable.home_bg),
                    contentScale = ContentScale.FillBounds
                )
        ) { innerPadding ->
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Image(
                    painter = painterResource(Res.drawable.home_sumo_hero),
                    contentDescription = null,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .width(222.dp),
                    contentScale = ContentScale.Fit,
                )
                Image(
                    painter = painterResource(Res.drawable.sumo_title_2),
                    contentDescription = null,
                    modifier = Modifier
                        .width(222.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
        SumoLaunchBtn(
            modifier = Modifier
        )
    }
}

@Composable
fun SumoLaunchBtn(
    modifier: Modifier = Modifier,
) {
    val navigator = NavController.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val onClick = {
        navigator.navigateToPlayGame()
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        Image(
            painter = painterResource(
                if (isPressed) {
                    Res.drawable.start_btn_rip
                } else {
                    Res.drawable.start_btn
                }
            ),
            contentDescription = null,
            modifier = Modifier
                .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ).padding(top = 64.dp),
            contentScale = ContentScale.Fit,
        )
    }
}
