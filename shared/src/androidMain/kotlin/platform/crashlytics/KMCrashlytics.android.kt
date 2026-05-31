package platform.crashlytics

import com.google.firebase.crashlytics.FirebaseCrashlytics

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual class KMCrashlytics {
    private val crashlytics = FirebaseCrashlytics.getInstance()

    actual fun reportNonFatal(throwable: Throwable) {
        crashlytics.recordException(throwable)
    }

    actual fun reportUserFeedback(feedback: String, type: String) {
        crashlytics.setCustomKey(type, feedback)
        crashlytics.recordException(UserFeedbackNonFatal)
    }
}