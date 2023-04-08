package book.chapter5.listing9

class CustomerController(
    private val emailGateway: IEmailGateway
) {

    private val customerRepository = CustomerRepository()
    private val productRepository = ProductRepository()
    private val mainStore = Store()

    fun purchase(customerId: Int, productId: Int, quantity: Int): Boolean {
        val customer = customerRepository.getById(customerId)
        val product = productRepository.getById(productId)

        val isSuccess = customer.purchase(mainStore, product, quantity)

        if (isSuccess) {
            emailGateway.sendReceipt(customer.email!!, product.name!!, quantity)
        }

        return isSuccess
    }
}