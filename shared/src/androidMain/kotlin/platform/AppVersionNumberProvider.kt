package platform

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class AppVersionNumberProvider(private val context: Context) {
    @Suppress("TooGenericExceptionCaught")
    actual fun getAppVersionNumber(): String {
        return try {
            context.getPackageInfo().versionName ?: ""
        } catch (e: Exception) {
            ""
        }
    }

    private fun Context.getPackageInfo(): PackageInfo {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
        } else {
            packageManager.getPackageInfo(packageName, 0)
        }
    }
}