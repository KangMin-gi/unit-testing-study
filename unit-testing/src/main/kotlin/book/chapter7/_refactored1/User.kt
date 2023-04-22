package book.chapter7._refactored1

class User(
    private var userId: Int,
    private var email: String,
    private var type: UserType,
) {

    fun changeEmail(newEmail: String, companyDomainName: String, numberOfEmployees: Int): Int {

        if (newEmail == this.email) {
            return numberOfEmployees
        }

        val emailDomain = newEmail.split('@')[1]
        val newType = when(emailDomain == companyDomainName) {
            true -> UserType.EMPLOYEE
            false -> UserType.CUSTOMER
        }

        val newNumberOfEmployees = if (newType != this.type) {
            val delta = when(newType) {
                UserType.EMPLOYEE -> 1
                else -> -1
            }
            numberOfEmployees + delta
        } else {
            numberOfEmployees
        }

        this.email = newEmail
        this.type = newType

        return newNumberOfEmployees
    }
}