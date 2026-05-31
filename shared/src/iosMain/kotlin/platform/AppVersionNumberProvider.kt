package platform

import platform.Foundation.NSBundle.Companion.mainBundle

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AppVersionNumberProvider {
    actual fun getAppVersionNumber(): String {
        mainBundle.infoDictionary()?.let {
            return it["CFBundleShortVersionString"].toString()
        }
        return ""
    }
}
