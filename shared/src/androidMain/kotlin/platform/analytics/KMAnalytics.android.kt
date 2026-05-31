package platform.analytics

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class KMAnalytics {
    actual fun logEvent(event: KMEvent) {
        // no-op
    }

    actual fun logScreenView(screenName: String) {
        // no-op
    }
}