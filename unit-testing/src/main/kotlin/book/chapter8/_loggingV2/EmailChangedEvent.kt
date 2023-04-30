package book.chapter8._loggingV2

data class EmailChangedEvent(
    val userId: Int,
    val newEmail: String,
) : IDomainEvent {
}