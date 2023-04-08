package book.chapter5.listing9

interface IEmailGateway {

    fun sendReceipt(email: String, productName: String, quantity: Int)
}