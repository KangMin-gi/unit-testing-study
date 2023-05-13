package book.chapter10.unitofwork

data class EmailChangedEvent(
    val userId: Int,
    val newEmail: String,
) : IDomainEvent {
}