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
        fetch()
    }

    actual fun fetch() {
        remoteConfig.fetchAndActivate()
    }

    actual fun getString(key: String): String? {
        return remoteConfig.getString(key)
    }

    actual fun getInt(key: String): Int? {
        return remoteConfig.getDouble(key).toInt()
    }

    actual fun getDouble(key: String): Double? {
        return remoteConfig.getDouble(key)
    }

    actual fun getBoolean(key: String): Boolean? {
        return remoteConfig.getBoolean(key)
    }

    actual fun getDefaults(): Map<String, Any> = remoteConfigDefaults.defaults
}
