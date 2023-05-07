import java.util.List;

public class EventDispatcher {
    private final MessageBus _messageBus;
    private final IDomainLogger _domainLogger;

    public EventDispatcher(
            MessageBus messageBus,
            IDomainLogger domainLogger) {
        _messageBus = messageBus;
        _domainLogger = domainLogger;
    }

    public void dispatch(List<IDomainEvent> events) {
        for(IDomainEvent ev : events) {
            dispatch(ev);
        }
    }

    private void dispatch(IDomainEvent ev) {
        switch (ev) {
            case EmailChangedEvent emailChangedEvent ->
                    _messageBus.sendEmailChangedMessage(emailChangedEvent.userId(), emailChangedEvent.newEmail());
            case UserTypeChangedEvent userTypeChangedEvent ->
                    _domainLogger.userTypeHasChanged(userTypeChangedEvent.userId(), userTypeChangedEvent.oldType(), userTypeChangedEvent.newType());
            default -> throw new IllegalArgumentException("지원하는 이벤트 타입이 아닙니다.");
        }
    }
}