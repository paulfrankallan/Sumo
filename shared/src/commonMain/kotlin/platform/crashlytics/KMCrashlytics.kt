package platform.crashlytics

const val TYPE_BUG_REPORT = "BR"
const val TYPE_FEATURE_REQUEST = "FR"
const val TYPE_GENERAL_QUERY = "GQ"

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class KMCrashlytics {
    fun reportNonFatal(throwable: Throwable)
    fun reportUserFeedback(feedback: String, type: String)
}
