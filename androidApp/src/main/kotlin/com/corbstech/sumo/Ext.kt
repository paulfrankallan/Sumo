package com.corbstech.sumo

import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.animation.AnticipateInterpolator
import androidx.core.animation.doOnEnd

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Context.startOpenLinkActivity(urlLink: String) {
    Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(urlLink)
    }.apply {
        resolveActivity(packageManager)?.let {
            this@startOpenLinkActivity.startActivity(this)
        }
    }
}

fun Double.toTwoDecimalPlaces() = String.format("%.2f", this)


@Suppress("TooGenericExceptionCaught")
fun Context.getAppVersionNumber(): String {
    return try {
        getPackageInfo().versionName ?: ""
    } catch (e: Exception) {
        ""
    }
}

fun Context.getPackageInfo(): PackageInfo {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    } else {
        packageManager.getPackageInfo(packageName, 0)
    }
}

fun Activity.injectSplashScreen() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        //installSplashScreen()
        // adding this fake just to clear any screen transition flicker issues
        splashScreen.setOnExitAnimationListener { splashScreenView ->
            ObjectAnimator.ofFloat(
                splashScreenView,
                View.TRANSLATION_Y,
                0f,
                -splashScreenView.height.toFloat()
            ).apply {
                interpolator = AnticipateInterpolator()
                duration = 0L
                doOnEnd { splashScreenView.remove() }
                start()
            }
        }
    }
}

fun Context.checkIntentAvailable(intent: Intent): Boolean =
    intent.resolveActivity(packageManager)?.let { true } ?: false