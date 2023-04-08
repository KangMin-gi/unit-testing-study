package book.chapter5.listing9

interface IStore {

    fun hasEnoughInventory(product: Product, quantity: Int): Boolean
    fun removeInventory(product: Product, quantity: Int)
    fun addInventory(product: Product, quantity: Int)
    fun getInventory(product: Product): Int

}