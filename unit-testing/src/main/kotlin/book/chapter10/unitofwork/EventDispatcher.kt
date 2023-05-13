package book.chapter10.unitofwork

class EventDispatcher(
    private val messageBus: IMessageBus,
    private val domainLogger: IDomainLogger,
) {

    fun dispatch(events: List<IDomainEvent>) {
        for (event in events) {
            dispatch(event)
        }
    }

   private fun dispatch(event: IDomainEvent) {
        when(event) {
            is EmailChangedEvent -> messageBus.sendEmailChangedMessage(event.userId, event.newEmail)
            is UserTypeChangedEvent -> domainLogger.userTypeHasChanged(event.userId, event.oldType, event.newType)
        }
    }
}