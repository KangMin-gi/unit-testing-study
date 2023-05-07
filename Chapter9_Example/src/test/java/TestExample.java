import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TestExample {
    private final String connectionString = "DUMMY";

    @Test
    public void changing_email_from_corporate_to_non_corporate() {
        Database db = new Database(connectionString);
        User user = createUser("user@mycorp.com", UserType.Employee, db);
        createCompany("mycorp.com", 1, db);

        BusSpy busSpy = new BusSpy();
        MessageBus messageBus = new MessageBus(busSpy);
        IDomainLogger loggerMock = Mockito.mock(IDomainLogger.class);
        UserController sut = new UserController(db, messageBus, loggerMock);

        String result = sut.changeEmail(user.getUserId(), "new@gmail.com");

        Assertions.assertEquals("OK", result);

        Object[] userData = db.getUserById(user.getUserId());
        User userFromDb = UserFactory.create(userData);
        Assertions.assertEquals("new@gmail.com", userFromDb.getEmail());
        Assertions.assertEquals(UserType.Customer, userFromDb.getType());

        Object[] companyData = db.getCompany();
        Company companyFromDb = CompanyFactory.create(companyData);
        Assertions.assertEquals(0, companyFromDb.getNumberOfEmployees());

        busSpy.shouldSendNumberOfMessages(1).withEmailChangedMessage(user.getUserId(), "new@gmail.com");
        Mockito.verify(loggerMock, Mockito.times(1)).userTypeHasChanged(user.getUserId(), UserType.Employee, UserType.Customer);
    }

    private User createUser(String email, UserType type, Database database) {
        User user = new User(0, email, type);
        database.saveUser(user);
        return user;
    }

    private Company createCompany(String domainName, int numberOfEmployees, Database database) {
        Company company = new Company(domainName, numberOfEmployees);
        database.saveCompany(company);
        return company;
    }
}