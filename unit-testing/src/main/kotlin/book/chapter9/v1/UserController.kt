package book.chapter9.v1

class UserController(
    private val database: Database,
    private val messageBus: IMessageBus,
    private val domainLogger: IDomainLogger,
) {
    private val eventDispatcher = EventDispatcher(messageBus, domainLogger)

    fun changeEmail(userId: Int, newEmail: String): String {
        val data = database.getUserById(userId)
        val user = UserFactory.create(data)

        val companyData = database.getCompany()
        val company = CompanyFactory.create(companyData)

        val error = user.canChangeEmail()
        error ?.let { return error }

        user.changeEmail(newEmail, company)

        database.saveCompany(company)
        database.saveUser(user)
        eventDispatcher.dispatch(user.domainEvents)

        return "OK"
    }
}