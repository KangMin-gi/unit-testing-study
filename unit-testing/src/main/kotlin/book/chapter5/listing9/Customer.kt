package book.chapter5.listing9

class Customer(
    var email: String? = null
) {

    fun purchase(store: IStore, product: Product, quantity: Int): Boolean {

        if (!store.hasEnoughInventory(product = product, quantity = quantity)) {
            return false
        }

        store.removeInventory(product = product, quantity = quantity)

        return true
    }

}