package platform.remoteconfig

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class RemoteConfigProvider(
    private val remoteConfigDefaults: RemoteConfigDefaults
) {
    private val values = remoteConfigDefaults.defaults.toMutableMap()

    actual fun getDouble(key: String): Double? {
        return when (val value = values[key]) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull()
            else -> null
        }
    }

    actual fun getBoolean(key: String): Boolean? {
        return values[key] as? Boolean
    }
}