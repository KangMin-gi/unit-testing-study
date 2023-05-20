package book.chapter11.time

import java.time.LocalDateTime

class InquiryController(
    private val dateTimeServer: IDateTimeServer,
) {

    fun approveInquiry(id: Int) {
        val inquiry = getById(id)
        inquiry.approve(dateTimeServer.now)
        saveInquiry(inquiry)
    }

    fun saveInquiry(inquiry: Inquiry) {
    }

    fun getById(id: Int): Inquiry {
        return Inquiry(true, LocalDateTime.of(2020, 1, 1, 0, 0))
    }
}