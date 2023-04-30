package book.chapter8.ambientcontext

data class UserTypeChangedEvent (
    val userId: Int,
    val oldType: UserType,
    val newType: UserType,
) : IDomainEvent {
}