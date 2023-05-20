package book.chapter11.leakingknowledge

import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.assertEquals

class CalculatorTest {
    @Test
    fun `adding two numbers`() {
        val value1 = 1
        val value2 = 3
        val expected = value1 + value2

        val actual = Calculator.add(value1, value2)

        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource("1,3", "11,33", "100,500")
    fun `adding two numbers version 2`(value1: Int, value2: Int) {
        val expected = value1 + value2

        val actual = Calculator.add(value1, value2)

        assertEquals(expected, actual)
    }

    @ParameterizedTest
    @CsvSource("1,3,4", "11,33,44", "100,500,600")
    fun `adding two numbers version 2`(value1: Int, value2: Int, expected: Int) {
        val actual = Calculator.add(value1, value2)

        assertEquals(expected, actual)
    }
}