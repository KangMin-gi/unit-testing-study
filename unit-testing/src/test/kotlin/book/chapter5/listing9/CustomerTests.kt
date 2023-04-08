package book.chapter5.listing9

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class CustomerTests {

    @Test
    fun `purchase succeeds when enough inventory`() {
        val storeMock = mockk<IStore>(relaxed = true)
        every { storeMock.hasEnoughInventory(Product.Shampoo, 5) } returns true
        val customer = Customer()

        val success = customer.purchase(storeMock, Product.Shampoo, 5)

        assertTrue { success }
        verify(exactly = 1) { storeMock.removeInventory(Product.Shampoo, 5) }
    }
}