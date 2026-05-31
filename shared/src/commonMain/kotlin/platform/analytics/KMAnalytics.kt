package platform.analytics

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class KMAnalytics {
    fun logEvent(event: KMEvent)
    fun logScreenView(screenName: String)
}