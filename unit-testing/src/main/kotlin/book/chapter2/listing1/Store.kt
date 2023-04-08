package book.chapter2.listing1

class Store {

    private val inventory = mutableMapOf<Product, Int>()

    fun hasEnoughInventory(product: Product, quantity: Int): Boolean {
        return getInventory(product) >= quantity
    }

    fun removeInventory(product: Product, quantity: Int) {
        if (!hasEnoughInventory(product, quantity)) {
            throw Exception("Not enough inventory")
        }

        inventory[product] = inventory[product]!! - quantity
    }

    fun addInventory(product: Product, quantity: Int): Unit {
        inventory[product] = inventory[product]
            ?.let { it + quantity }
            ?: quantity
    }

    fun getInventory(product: Product): Int {
        return inventory.getOrDefault(product, 0)
    }
}