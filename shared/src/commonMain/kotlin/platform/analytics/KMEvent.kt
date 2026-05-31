package platform.analytics

data class KMEvent constructor(
    val eventName: String,
    val params: List<KMParam>? = null,
) {
    constructor(
        eventName: String,
        params: () -> List<KMParam>,
    ) : this(
        eventName,
        params.invoke()
    )
}
