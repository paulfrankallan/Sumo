package platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ExternalWebBrowser {
    actual fun launch(url: String) {
        val nsUrl = NSURL(string = url)
        nsUrl.let {
            UIApplication.sharedApplication.openURL(it)
        }
    }
}


