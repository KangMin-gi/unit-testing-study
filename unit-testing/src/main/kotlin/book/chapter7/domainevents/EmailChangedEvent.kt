package book.chapter7.domainevents

data class EmailChangedEvent(
    val userId: Int,
    val newEmail: String,
) {
}