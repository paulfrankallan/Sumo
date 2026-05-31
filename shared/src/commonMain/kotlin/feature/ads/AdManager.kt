package feature.ads

import platform.remoteconfig.RemoteConfigProvider
import platform.remoteconfig.getAdProbability
import platform.remoteconfig.getAdsEnabled
import platform.remoteconfig.getAdsProbability
import kotlin.random.Random

class AdManager(
    private val config: RemoteConfigProvider
) {
    fun adsAreOn() = getAdsEnabled() && enabledByProbability()

    fun getAdsEnabled(): Boolean {
        return config.getAdsEnabled()
    }

    fun getAdsProbability(): Float {
        return config.getAdsProbability()
    }

    fun getAdProbability(key: String): Float {
        return config.getAdProbability(key)
    }

    fun enabledByProbability(
        key: String
    ) = weightedRandomBoolean(getAdProbability(key))

    fun enabledByProbability() = weightedRandomBoolean()

    private fun weightedRandomBoolean(
        probabilityOfTrue: Float = getAdsProbability()
    ) = Random.nextFloat() < probabilityOfTrue
}
