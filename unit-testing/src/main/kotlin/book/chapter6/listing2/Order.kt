package book.chapter6.listing2

class Order {
    var products = listOf<Product>()

    fun addProduct(product: Product) {
        products += product
    }
}