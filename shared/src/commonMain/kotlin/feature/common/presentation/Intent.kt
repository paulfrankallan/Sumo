package feature.common.presentation

interface Intent

class UpdateSwitchPreference(val key: String, val value: Boolean) : Intent
class ExternalBrowser(val url: String) : Intent
object ResetAppData : Intent
object OpenUserFeedback : Intent
