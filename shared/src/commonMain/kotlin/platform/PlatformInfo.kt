package platform

interface PlatformInfo {
    val name: String
    val isAndroid: Boolean
    val isIos: Boolean
    val version: Int
}

expect fun getPlatformInfo(): PlatformInfo

class AndroidVersion {
    companion object {
        const val O: Int = 26
        const val O_MR1: Int = 27
        const val P: Int = 28
        const val Q: Int = 29
        const val R: Int = 30
        const val S: Int = 31
        const val S_V2: Int = 32
        const val TIRAMISU: Int = 33
        const val UPSIDE_DOWN_CAKE: Int = 34
        const val VANILLA_ICE_CREAM: Int = 35
    }
}