package platform.analytics

sealed class KMParam(open val key: String) {
    data class StringParam(override val key: String, val value: String?) : KMParam(key)
    data class LongParam(override val key: String, val value: Long) : KMParam(key)
    data class DoubleParam(override val key: String, val value: Double) : KMParam(key)
}
