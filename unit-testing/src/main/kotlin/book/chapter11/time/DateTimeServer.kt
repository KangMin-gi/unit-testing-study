package book.chapter11.time

import java.time.LocalDateTime

object DateTimeServer {
    lateinit var func: () -> LocalDateTime
    val NOW = func.invoke()

    fun init(func: () -> LocalDateTime) {
        this.func = DateTimeServer.func
    }
}