package book.chapter10.unitofwork

data class UserTypeChangedEvent (
    val userId: Int,
    val oldType: UserType,
    val newType: UserType,
) : IDomainEvent {
}