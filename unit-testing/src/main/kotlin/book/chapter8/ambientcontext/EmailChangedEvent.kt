package book.chapter8.ambientcontext

data class EmailChangedEvent(
    val userId: Int,
    val newEmail: String,
) : IDomainEvent {
}