package book.chapter8._loggingV2

interface IMessageBus {

    fun sendEmailChangedMessage(userId: Int, newEmail: String)
}