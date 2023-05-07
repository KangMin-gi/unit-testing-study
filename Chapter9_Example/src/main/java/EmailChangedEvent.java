public record EmailChangedEvent(int userId, String newEmail) implements IDomainEvent {
}