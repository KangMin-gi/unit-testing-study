package book.chapter11.testableprivatemethod

import java.time.LocalDateTime

class Inquiry private constructor(
    var isApproved: Boolean,
    var timeApproved: LocalDateTime,
) {
    fun approve(now: LocalDateTime) {
        if (isApproved) return

        this.isApproved = true
        this.timeApproved = now
    }
}