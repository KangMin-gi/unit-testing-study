package book.chapter7.__sampleproject

class User(
    private var userId: Int,
    private var email: String,
    private var type: UserType,
) {

    fun changeEmail(userId: Int, newEmail: String) {
        val data = Database.getUserById(userId)
        this.userId = userId
        this.email = data!![1] as String
        this.type = data[2] as UserType

        if (newEmail == this.email) {
            return
        }

        val companyData = Database.getCompany()
        val companyDomainName = companyData!![0] as String
        val numberOfEmployees = companyData[1] as Int

        val emailDomain = newEmail.split('@')[1]
        val newType = when(emailDomain == companyDomainName) {
            true -> UserType.EMPLOYEE
            false -> UserType.CUSTOMER
        }

        if (newType != this.type) {
            val delta = when(newType) {
                UserType.EMPLOYEE -> 1
                else -> -1
            }
            val newNumber = numberOfEmployees + delta
            Database.saveCompany(newNumber)
        }

        this.email = newEmail
        this.type = newType

        Database.saveUser(this)
        MessageBus.sendEmailChangedMessage(userId, newEmail)
    }
}