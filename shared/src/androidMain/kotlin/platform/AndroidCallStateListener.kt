package platform

import co.touchlab.kermit.Logger

class AndroidCallStateListener: CallStateListener {
    override fun initialize() {
        Logger.i { "PFASOUND - AndroidCallStateListener initialized" }
    }
}