package book.chapter11.codepollution

import org.junit.jupiter.api.Test

class LoggerTest {

    @Test
    fun `some test`() {
        val logger = Logger(true)
        val sut = Controller()

        sut.someMethod(logger)
    }
}