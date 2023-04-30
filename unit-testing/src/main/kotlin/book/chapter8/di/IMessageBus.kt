package book.chapter8.di

interface IMessageBus {

    fun sendEmailChangedMessage(userId: Int, newEmail: String)
}