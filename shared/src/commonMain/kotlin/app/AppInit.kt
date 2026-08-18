package app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import app.theme.AppTheme
import co.touchlab.kermit.Logger
import feature.common.presentation.Intent
import org.koin.compose.KoinContext

typealias OnIntentCallback = (Intent) -> Unit

val OnIntentProvider = compositionLocalOf<OnIntentCallback> { {} }

@Composable
fun AppInit(
    showInterstitialAd: () -> Unit,
    showBannerAd: (Boolean) -> Unit,
    bannerHeight: Int,
) {
    // Use a consistent tag so all debug output from the sound system is easy to filter.
    Logger.setTag("PFASOUND")
    KoinContext {
        AppTheme {
            App(
                bannerHeight = bannerHeight,
                showInterstitialAd = showInterstitialAd,
                showBannerAd = showBannerAd,
            )
        }
    }
}
