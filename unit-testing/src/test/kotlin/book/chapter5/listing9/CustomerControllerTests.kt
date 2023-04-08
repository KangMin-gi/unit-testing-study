package book.chapter5.listing9

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CustomerControllerTests {

    @Test
    fun `successful purchase`() {
        val mock = mockk<IEmailGateway>(relaxed = true)
        val sut = CustomerController(mock)

        val isSuccess = sut.purchase(customerId = 1, productId = 2, quantity = 5)

        assertTrue { isSuccess }
        verify(exactly = 1) {
            mock.sendReceipt("customer@email.com", "Shampoo", 5)
        }
    }
}