package book.chapter7.canexecute

class UserController {

    private val database = Database()
    private val messageBus = MessageBus()

    fun changeEmail(userId: Int, newEmail: String): String {
        val data = database.getUserById(userId)
        val user = UserFactory.create(data!!)

        val companyData = database.getCompany()
        val company = CompanyFactory.create(companyData!!)

        val error = user.canChangeEmail()
        error ?.let { return error }

        user.changeEmail(newEmail, company)

        database.saveCompany(company)
        database.saveUser(user)
        messageBus.sendEmailChangedMessage(userId, newEmail)

        return "OK"
    }
}