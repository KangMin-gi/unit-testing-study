package book.chapter9.v1

data class UserTypeChangedEvent (
    val userId: Int,
    val oldType: UserType,
    val newType: UserType,
) : IDomainEvent {
}