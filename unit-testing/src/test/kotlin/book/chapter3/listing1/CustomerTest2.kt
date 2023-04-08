package book.chapter3.listing1

import book.chapter2.listing1.Customer
import book.chapter2.listing1.Product
import book.chapter2.listing1.Store
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CustomerTest2 {

    companion object {
        private fun createCustomer(): Customer {
            return Customer()
        }

        private fun createStoreWithInventory(product: Product, quantity: Int): Store {
            val store = Store()
            store.addInventory(product, quantity)
            return store
        }
    }

    @Test
    fun purchase_succeeds_when_enough_inventory() {
        val store = createStoreWithInventory(Product.Shampoo, 10)
        val sut = createCustomer()

        val success = sut.purchase(store, Product.Shampoo, 5)

        assertEquals(true, success)
        assertEquals(5, store.getInventory(Product.Shampoo))
    }

    @Test
    fun purchase_fails_when_not_enough_inventory() {
        val store = createStoreWithInventory(Product.Shampoo, 10)
        val sut = createCustomer()

        val success = sut.purchase(store, Product.Shampoo, 15)

        assertEquals(false, success)
        assertEquals(10, store.getInventory(Product.Shampoo))
    }
}