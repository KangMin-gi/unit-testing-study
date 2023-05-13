package book.chapter10.transaction

interface IMessageBus {
    fun sendEmailChangedMessage(userId: Int, newEmail: String)
}