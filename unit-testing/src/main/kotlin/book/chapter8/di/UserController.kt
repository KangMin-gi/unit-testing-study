package book.chapter8.di

class UserController(
    private val database: Database,
    private val messageBus: IMessageBus,
) {
    private val logger: ILogger = LogManager.getLogger(UserController::class.java)

    fun changeEmail(userId: Int, newEmail: String): String {
        val data = database.getUserById(userId)
        val user = UserFactory.create(data)

        val companyData = database.getCompany()
        val company = CompanyFactory.create(companyData)

        val error = user.canChangeEmail()
        error ?.let { return error }

        user.changeEmail(newEmail, company, logger)

        database.saveCompany(company)
        database.saveUser(user)
        EventDispatcher.dispatch(user.domainEvents)

        return "OK"
    }
}