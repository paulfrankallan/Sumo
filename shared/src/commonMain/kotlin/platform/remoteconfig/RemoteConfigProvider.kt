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

fun RemoteConfigProvider.getDoubleOrDefault(key: String, fallback: Double = 0.0): Double {
    return getDouble(key) ?: fallback
}

// region Core

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class RemoteConfigProvider {
    fun getDouble(key: String): Double?
    fun getBoolean(key: String): Boolean?
}

// endregion
