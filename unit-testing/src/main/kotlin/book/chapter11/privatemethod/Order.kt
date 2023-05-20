package book.chapter11.privatemethod

class Order(
    private val customer: Customer,
    private val products: List<Product>,
) {

    fun generateDescription(): String {
        return "Customer name: ${customer.name}, " +
                "total number of products: ${products.size}, " +
                "total price: ${getPrice()}"
    }

    private fun getPrice(): Int {
        val basePrice = 0
        val discounts = 0
        val taxes = 0
        return basePrice - discounts + taxes
    }
}