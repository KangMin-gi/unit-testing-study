package book.chapter9.v1

interface IMessageBus {
    fun sendEmailChangedMessage(userId: Int, newEmail: String)
}