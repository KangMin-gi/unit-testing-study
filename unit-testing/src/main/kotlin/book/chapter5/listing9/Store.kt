package book.chapter5.listing9

class Store() : IStore {

    private val inventory = HashMap<Product, Int>()
    private var id: Int? = null

    init {
        inventory[Product.Shampoo] = 10
    }

    override fun hasEnoughInventory(product: Product, quantity: Int): Boolean {
        return getInventory(product) >= quantity
    }

    override fun removeInventory(product: Product, quantity: Int) {
        if (!hasEnoughInventory(product, quantity)) {
            throw Exception("Not enough inventory")
        }

        inventory[product] = inventory[product]!! - quantity
    }

    override fun addInventory(product: Product, quantity: Int) {
        if (inventory.containsKey(product)) {
            inventory[product] = inventory[product]!! + quantity
        } else {
            inventory[product] = quantity
        }
    }

    override fun getInventory(product: Product): Int {
        return inventory.getOrDefault(product, 0)
    }
}