package book.chapter3.listing1

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class CalculatorTest2 {

    private val calculator: Calculator = Calculator()

    @Test
    fun sum_of_two_numbers() {
        // Arrange
        val first: Double = 10.0
        val second: Double = 20.0

        // Act
        val result = calculator.sum(first, second)

        // Assert
        Assertions.assertEquals(30.0, result)
    }

    @Test
    fun dispose() {
        calculator.cleanUp()
    }

}