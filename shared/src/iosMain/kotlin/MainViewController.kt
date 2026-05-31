import androidx.compose.ui.window.ComposeUIViewController
import app.App

fun MainViewController() = ComposeUIViewController { App(
    showInterstitialAd = { /* showInterstitialAd() */ },
    showBannerAd = { /* showBannerAd(it) */ }
) }