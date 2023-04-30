package book.chapter8.__v1

interface IMessageBus {

    fun sendEmailChangedMessage(userId: Int, newEmail: String)
}