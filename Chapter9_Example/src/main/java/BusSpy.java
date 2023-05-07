import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.List;

public class BusSpy implements IBus {
    private final List<String> _sentMessages = new ArrayList<>();

    public void send(String message) {
        _sentMessages.add(message);
    }

    public BusSpy shouldSendNumberOfMessages(int number) {
        Assertions.assertThat(number).isEqualTo(_sentMessages.size());
        return this;
    }

    public BusSpy withEmailChangedMessage(int userId, String newEmail) {
        String message = "Type : USER EMAIL CHANGED; Id : " + userId + "; NewEmail : " + newEmail;
        Assertions.assertThat(_sentMessages).contains(message);
        return this;
    }
}