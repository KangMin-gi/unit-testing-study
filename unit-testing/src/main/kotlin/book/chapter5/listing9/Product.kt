package book.chapter5.listing9

class Product(
    val id: Int? = null,
    val name: String? = null,
) {

    companion object {
        val Shampoo = Product(2, "Shampoo")
    }
}