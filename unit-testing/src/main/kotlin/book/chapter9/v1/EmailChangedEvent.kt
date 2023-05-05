package book.chapter9.v1

data class EmailChangedEvent(
    val userId: Int,
    val newEmail: String,
) : IDomainEvent {
}