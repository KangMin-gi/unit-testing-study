package book.chapter11.time

import java.time.LocalDateTime

class Inquiry(
    isApproved: Boolean,
    timeApproved: LocalDateTime?,
) {
    var isApproved: Boolean? = null
        get
        private set

    var timeApproved: LocalDateTime? = null
        get
        private set


    init {
        require(isApproved && timeApproved != null)
    }

    fun approve(now: LocalDateTime) {
        if (isApproved == true) return

        isApproved = true
        timeApproved = now
    }

}