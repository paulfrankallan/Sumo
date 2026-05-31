package platform

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class ExternalWebBrowser(private val context: Context) {
    actual fun launch(url: String) {
        Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }.apply {
            resolveActivity(context.packageManager)?.let {
                this.addFlags(FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(this)
            }
        }
    }
}
