package platform

import android.os.Build

class AndroidPlatform : PlatformInfo {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
    override val isAndroid: Boolean = true
    override val isIos: Boolean = false
    override val version: Int = Build.VERSION.SDK_INT
}

actual fun getPlatformInfo(): PlatformInfo = AndroidPlatform()