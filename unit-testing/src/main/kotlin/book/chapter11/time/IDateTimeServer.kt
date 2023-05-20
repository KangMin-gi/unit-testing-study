package book.chapter11.time

import java.time.LocalDateTime

interface IDateTimeServer {
    val now: LocalDateTime
        get
}