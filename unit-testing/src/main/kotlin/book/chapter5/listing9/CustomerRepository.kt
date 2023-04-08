package book.chapter5.listing9

class CustomerRepository {

    fun getById(customerId: Int): Customer {
        return Customer("customer@email.com")
    }
}