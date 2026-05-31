package platform.remoteconfig

// region Ads

fun RemoteConfigProvider.getAdsEnabled(): Boolean {
    return getBoolean(RemoteConfigConstants.KEY_ADS_ENABLED) ?: true
}

fun RemoteConfigProvider.getAdsProbability(): Float {
    return getDouble(RemoteConfigConstants.KEY_ADS_PROBABILITY)?.toFloat() ?: 1f
}

fun RemoteConfigProvider.getAdProbability(key: String): Float {
    return getDoubleOrDefault(key, 0.0).toFloat()
}

// region OrDefault functions

fun RemoteConfigProvider.getStringOrDefault(key: String, fallback: String = ""): String {
    return getString(key) ?: getDefaults()[key] as? String ?: fallback
}

fun RemoteConfigProvider.getIntOrDefault(key: String, fallback: Int = 0): Int {
    return getInt(key) ?: getDefaults()[key] as? Int ?: fallback
}

fun RemoteConfigProvider.getDoubleOrDefault(key: String, fallback: Double = 0.0): Double {
    return getDouble(key) ?: getDefaults()[key] as? Double ?: fallback
}

fun RemoteConfigProvider.getBooleanOrDefault(key: String, fallback: Boolean = false): Boolean {
    return getBoolean(key) ?: getDefaults()[key] as? Boolean ?: fallback
}

// endregion

// region Core

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class RemoteConfigProvider {
    fun fetch()

    fun getString(key: String): String?
    fun getInt(key: String): Int?
    fun getDouble(key: String): Double?
    fun getBoolean(key: String): Boolean?

    fun getDefaults(): Map<String, Any>
}

// endregion
