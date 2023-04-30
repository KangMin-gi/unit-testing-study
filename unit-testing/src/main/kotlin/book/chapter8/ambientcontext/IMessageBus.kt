package book.chapter8.ambientcontext

interface IMessageBus {

    fun sendEmailChangedMessage(userId: Int, newEmail: String)
}