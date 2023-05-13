package book.chapter10.transaction

class MessageBus(
    private val bus: IBus,
): IMessageBus {
    override fun sendEmailChangedMessage(userId: Int, newEmail: String) {
        bus.send("Type: USER; Type: EMAIL CHANGED; Id: $userId; NewEmail: $newEmail")
    }

}