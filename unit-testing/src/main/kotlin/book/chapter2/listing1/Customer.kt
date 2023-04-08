package book.chapter2.listing1

class Customer {
    fun purchase(store: Store, product: Product, quantity: Int): Boolean {
        if (!store.hasEnoughInventory(product, quantity)) {
            return false
        }

        store.removeInventory(product, quantity)

        return true
    }
}