package platform.crashlytics

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class KMCrashlytics {
    actual fun reportNonFatal(throwable: Throwable) {
        throwable.printStackTrace()
    }

    actual fun reportUserFeedback(feedback: String, type: String) {
        println("User feedback [$type]: $feedback")
    }
}