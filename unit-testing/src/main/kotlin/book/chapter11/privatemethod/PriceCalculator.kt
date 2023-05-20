package book.chapter11.privatemethod

class PriceCalculator {
    fun calculate(customer: Customer, products: List<Product>): Int {
        val basePrice = 0
        val discounts = 0
        val taxes = 0
        return basePrice - discounts + taxes
    }
}
