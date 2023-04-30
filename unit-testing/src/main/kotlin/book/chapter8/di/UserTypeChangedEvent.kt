package book.chapter8.di

data class UserTypeChangedEvent (
    val userId: Int,
    val oldType: UserType,
    val newType: UserType,
) : IDomainEvent {
}