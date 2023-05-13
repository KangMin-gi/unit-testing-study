package book.chapter10.transaction

class UserController(
    private val transaction: Transaction,
    private val messageBus: IMessageBus,
    private val domainLogger: IDomainLogger,
) {
    private val userRepository = UserRepository(transaction)
    private val companyRepository = CompanyRepository(transaction)
    private val eventDispatcher = EventDispatcher(messageBus, domainLogger)

    fun changeEmail(userId: Int, newEmail: String): String {
        val data = userRepository.getUserById(userId)
        val user = UserFactory.create(data)

        val companyData = companyRepository.getCompany()
        val company = CompanyFactory.create(companyData)

        val error = user.canChangeEmail()
        error ?.let { return error }

        user.changeEmail(newEmail, company)

        companyRepository.saveCompany(company)
        userRepository.saveUser(user)
        eventDispatcher.dispatch(user.domainEvents)

        transaction.commit()

        return "OK"
    }
}