package book.chapter6.listing1

import book.chapter6.listing1.PriceEngine
import book.chapter6.listing1.Product
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CustomerControllerTests {

    @Test
    fun `discount of two products`() {
        val product1 = Product("Hand wash")
        val product2 = Product("Shampoo")
        val sut = PriceEngine()

        val discount = sut.calculateDiscount(product1, product2)

        assertEquals(0.02, discount)
    }
}