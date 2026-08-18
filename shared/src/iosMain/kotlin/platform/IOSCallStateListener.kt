package platform

import co.touchlab.kermit.Logger

class IOSCallStateListener: CallStateListener {
    override fun initialize() {
        Logger.i { "PFASOUND - IOSCallStateListener initialized" }
    }
}