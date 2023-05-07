import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class User {
    private final int userId;
    private String email;
    private UserType type;
    private boolean isEmailConfirmed;
    private List<IDomainEvent> domainEvents;

    public User(int userId, String email, UserType type) {
        this.userId = userId;
        this.email = email;
        this.type = type;
        this.domainEvents = new ArrayList<>();
    }

    public List<IDomainEvent> getDomainEvents() {
        return domainEvents;
    }

    public int getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public UserType getType() {
        return type;
    }

    public void changeEmail(String newEmail, Company company) {
        assert canChangeEmail() == null;
        if(Objects.equals(email, newEmail))
            return;

        UserType newType = company.isEmailCorporate(newEmail) ? UserType.Employee : UserType.Customer;

        if(type != newType) {
            int delta = newType == UserType.Employee ? 1 : -1;
            company.changeNumberOfEmployees(delta);
            addDomainEvent(new UserTypeChangedEvent(userId, type, newType));
        }

        email = newEmail;
        type = newType;
        addDomainEvent(new EmailChangedEvent(userId, newEmail));
    }

    private void addDomainEvent(IDomainEvent domainEvent) {
        domainEvents.add(domainEvent);
    }

    public String canChangeEmail() {
        if(isEmailConfirmed) {
            return "Can't change a confirmed email";
        }
        return null;
    }
}