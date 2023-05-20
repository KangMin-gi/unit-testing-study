package book.chapter11.time

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class DateTimeServerTest {

    @Test
    fun `ambient context`() {
        DateTimeServer.init { LocalDateTime.now() }
        DateTimeServer.init { LocalDateTime.of(2020, 1, 1, 0, 0) }
    }
}