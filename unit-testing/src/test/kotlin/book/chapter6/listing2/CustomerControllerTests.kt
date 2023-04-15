package book.chapter6.listing2

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class CustomerControllerTests {

    @Test
    fun `adding a product to an order`() {
        val product = Product("Hand wash")
        val sut = Order()

        sut.addProduct(product)

        assertEquals(1, sut.products.count())
        assertEquals(product, sut.products[0])
    }
}