package book.chapter8._loggingV1

data class EmailChangedEvent(
    val userId: Int,
    val newEmail: String,
) {
}