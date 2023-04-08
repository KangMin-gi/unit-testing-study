package book.chapter3.listing1

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CalculatorTest {

    @Test
    fun sum_of_two_numbers() {
        // Arrange
        val first: Double = 10.0
        val second: Double = 20.0
        val calculator = Calculator()

        // Act
        val result = calculator.sum(first, second)

        // Assert
//        assertEquals(30.0, result)
        assertThat(result).isEqualTo(30.0)
    }
}