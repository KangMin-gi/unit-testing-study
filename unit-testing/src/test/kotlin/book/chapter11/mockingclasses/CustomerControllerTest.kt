package book.chapter11.mockingclasses

import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CustomerControllerTest {

    @Test
    fun `customer with no deliveries`() {
        val gateway = mockk<IDeliveryGateway>()
        val sut = CustomerController(StatisticsCalculator(), gateway)

        val result = sut.getStatistics(1)

        assertEquals("Total weight delivered: 0, Total cost: 0", result)
    }
}