package book.chapter8._loggingV1

interface IMessageBus {

    fun sendEmailChangedMessage(userId: Int, newEmail: String)
}