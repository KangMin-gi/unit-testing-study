package book.chapter2.listing1

import io.mockk.every
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CustomerTest {

    @Test
    fun `Purchase succeeds when not enough inventory`() {

        // Arrange
        val store = Store()
        store.addInventory(Product.Shampoo, 10)
        val customer = Customer()

        // Act
        val success: Boolean = customer.purchase(store, Product.Shampoo, 5)

        // Assert
        assertTrue(success)
        assertEquals(5, store.getInventory(Product.Shampoo))
    }

    @Test
    fun `Purchase fails when not enough inventory`() {
        val storeMock = spyk(Store())
        every { storeMock.hasEnoughInventory(Product.Shampoo, 5) } returns false
        val customer = Customer()

        val success: Boolean = customer.purchase(storeMock, Product.Shampoo, 5)

        assertFalse(success)
        verify(exactly = 0) {
            storeMock.removeInventory(Product.Shampoo, 5)
        }
    }
}