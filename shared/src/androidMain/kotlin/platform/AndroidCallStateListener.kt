package platform

class AndroidCallStateListener: CallStateListener {
    override fun initialize() {
        println("AndroidCallStateListener initialized")
    }
}