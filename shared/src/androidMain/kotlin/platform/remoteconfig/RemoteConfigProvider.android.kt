package platform.remoteconfig

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class RemoteConfigProvider(
    private val remoteConfigDefaults: RemoteConfigDefaults
) {
    private val values = mutableMapOf<String, Any>()

    init {
        fetch()
    }

    actual fun fetch() {
        values.putAll(remoteConfigDefaults.defaults)
    }

    actual fun getString(key: String): String? {
        return values[key] as? String
    }

    actual fun getInt(key: String): Int? {
        return (values[key] as? Number)?.toInt()
    }

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

    actual fun getDefaults(): Map<String, Any> = remoteConfigDefaults.defaults.toMap()
}
