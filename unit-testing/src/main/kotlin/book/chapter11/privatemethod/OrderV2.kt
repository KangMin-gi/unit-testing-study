package book.chapter11.privatemethod

class OrderV2(
    private val customer: Customer,
    private val products: List<Product>,
) {

    val calc = PriceCalculator()

    fun generateDescription(): String {
        return "Customer name: ${customer.name}, " +
                "total number of products: ${products.size}, " +
                "total price: ${calc.calculate(customer, products)}"
    }
}