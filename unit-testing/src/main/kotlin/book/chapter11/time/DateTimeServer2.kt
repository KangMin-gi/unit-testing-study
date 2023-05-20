package book.chapter11.time

import java.time.LocalDateTime

class DateTimeServer2() : IDateTimeServer {
    override val now: LocalDateTime
        get() = LocalDateTime.now()
}