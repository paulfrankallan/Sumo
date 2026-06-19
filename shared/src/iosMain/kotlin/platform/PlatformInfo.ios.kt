package platform

import platform.UIKit.UIDevice

class IOSPlatformInfo: PlatformInfo {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val isAndroid: Boolean = false
    override val isIos: Boolean = true
    override val version: Int = UIDevice.currentDevice.systemVersion.substringBefore(".").toInt()
}

actual fun getPlatformInfo(): PlatformInfo = IOSPlatformInfo()