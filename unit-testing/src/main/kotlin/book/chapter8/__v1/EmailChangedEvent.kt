package book.chapter8.__v1

data class EmailChangedEvent(
    val userId: Int,
    val newEmail: String,
) {
}