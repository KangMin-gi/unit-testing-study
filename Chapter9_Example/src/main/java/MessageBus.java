public class MessageBus {
    private final IBus _bus;

    public MessageBus(IBus _bus) {
        this._bus = _bus;
    }

    public void sendEmailChangedMessage(int userId, String newEmail) {
        _bus.send("Type : USER EMAIL CHANGED; Id : " + userId + "; NewEmail : " + newEmail);
    }
}
