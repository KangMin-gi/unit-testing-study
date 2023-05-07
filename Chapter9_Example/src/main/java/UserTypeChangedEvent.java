public record UserTypeChangedEvent(int userId, UserType oldType, UserType newType) implements IDomainEvent {
}
