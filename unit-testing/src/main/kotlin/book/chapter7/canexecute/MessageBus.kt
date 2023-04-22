package book.chapter7.canexecute

class MessageBus {

    private val bus: IBus
        get() {
            TODO()
        }

    fun sendEmailChangedMessage(userId: Int, newEmail: String) {
        bus.send("Subject: USER; Type: EMAIL CHANGED; Id: $userId; NewEmail: $newEmail")
    }

}