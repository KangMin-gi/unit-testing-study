package book.chapter10.unitofwork

interface IMessageBus {
    fun sendEmailChangedMessage(userId: Int, newEmail: String)
}