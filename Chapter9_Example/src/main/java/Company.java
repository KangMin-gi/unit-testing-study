import java.util.Objects;

public class Company {
    private final String domainName;
    private int numberOfEmployees;

    public Company(String domainName, int numberOfEmployees) {
        this.domainName = domainName;
        this.numberOfEmployees = numberOfEmployees;
    }

    public String getDomainName() {
        return domainName;
    }

    public int getNumberOfEmployees() {
        return numberOfEmployees;
    }

    public void changeNumberOfEmployees(int delta) {
        assert numberOfEmployees + delta >= 0;
        numberOfEmployees += delta;
    }

    public boolean isEmailCorporate(String email) {
        String emailDomain = email.split("@")[1];
        return Objects.equals(emailDomain, domainName);
    }
}