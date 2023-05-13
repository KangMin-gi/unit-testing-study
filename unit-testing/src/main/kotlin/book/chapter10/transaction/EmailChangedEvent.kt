package book.chapter10.transaction

data class EmailChangedEvent(
    val userId: Int,
    val newEmail: String,
) : IDomainEvent {
}