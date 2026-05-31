package com.corbstech.sumo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import app.AppInit

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            AppInit(
                bannerHeight = 0,
                showInterstitialAd = { },
                showBannerAd = { },
            )
        }
        configureScreen()
    }
}


private fun ComponentActivity.configureScreen() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.light(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        ),
        navigationBarStyle = SystemBarStyle.light(
            android.graphics.Color.TRANSPARENT,
            android.graphics.Color.TRANSPARENT,
        ),
    )
    with(WindowCompat.getInsetsController(window, window.decorView)) {
        hide(WindowInsetsCompat.Type.systemBars())
        systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
}