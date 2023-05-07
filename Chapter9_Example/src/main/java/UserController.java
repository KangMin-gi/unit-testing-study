public class UserController {
    private final Database _database;
    private final EventDispatcher _eventDispatcher;

    public UserController(
            Database database,
            MessageBus messageBus,
            IDomainLogger domainLogger) {
        _database = database;
        _eventDispatcher = new EventDispatcher(messageBus, domainLogger);
    }

    public String changeEmail(int userId, String newEmail) {
        Object[] userData = _database.getUserById(userId);
        User user = UserFactory.create(userData);

        String error = user.canChangeEmail();
        if(error != null) {
            return error;
        }

        Object[] companyData = _database.getCompany();
        Company company = CompanyFactory.create(companyData);

        user.changeEmail(newEmail, company);

        _database.saveCompany(company);
        _database.saveUser(user);
        _eventDispatcher.dispatch(user.getDomainEvents());

        return "OK";
    }
}