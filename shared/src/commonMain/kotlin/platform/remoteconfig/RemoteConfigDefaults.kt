package platform.remoteconfig

import platform.remoteconfig.RemoteConfigConstants.KEY_ADS_ENABLED
import platform.remoteconfig.RemoteConfigConstants.KEY_ADS_PROBABILITY

class RemoteConfigDefaults {
    val defaults: Map<String, Any> = mapOf(
        KEY_ADS_ENABLED to false,
        KEY_ADS_PROBABILITY to "1"
    )
}
