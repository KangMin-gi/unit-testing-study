package book.chapter8._loggingV1

class MessageBus: IMessageBus {

    private val bus: IBus
        get() {
            TODO()
        }

    override fun sendEmailChangedMessage(userId: Int, newEmail: String) {
        bus.send("Subject: USER; Type: EMAIL CHANGED; Id: $userId; NewEmail: $newEmail")
    }

}