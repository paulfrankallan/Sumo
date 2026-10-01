package platform.remoteconfig

import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class RemoteConfigProvider(
    private val remoteConfigDefaults: RemoteConfigDefaults
) {
    private val remoteConfig = Firebase.remoteConfig

    init {
        remoteConfig.setDefaultsAsync(remoteConfigDefaults.defaults)
        remoteConfig.fetchAndActivate()
    }

    actual fun getDouble(key: String): Double? {
        return remoteConfig.getDouble(key)
    }

    actual fun getBoolean(key: String): Boolean? {
        return remoteConfig.getBoolean(key)
    }
}
