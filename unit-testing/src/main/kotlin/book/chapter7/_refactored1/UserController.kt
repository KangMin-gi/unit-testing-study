package book.chapter7._refactored1

class UserController {

    private val database = Database()
    private val messageBus = MessageBus()

    fun changeEmail(userId: Int, newEmail: String) {
        val data = database.getUserById(userId)
        val email = data!![1] as String
        val type = data[2] as UserType
        val user = User(userId, email, type)

        val companyData = database.getCompany()
        val companyDomainName = companyData!![0] as String
        val numberOfEmployees = companyData[1] as Int

        val newNumberOfEmployees = user.changeEmail(newEmail, companyDomainName, numberOfEmployees)

        database.saveCompany(newNumberOfEmployees)
        database.saveUser(user)
        messageBus.sendEmailChangedMessage(userId, newEmail)
    }
}