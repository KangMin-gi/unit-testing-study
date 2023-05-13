package book.chapter10.transaction

data class UserTypeChangedEvent (
    val userId: Int,
    val oldType: UserType,
    val newType: UserType,
) : IDomainEvent {
}