package book.chapter8.__v1

class User(val userId: Int, email: String, type: UserType, isEmailConfirmed: Boolean) {

    var email: String = email
        private set
    var type: UserType = type
        private set
    var isEmailConfirmed: Boolean = isEmailConfirmed
        private set
    var emailChangedEvents: List<EmailChangedEvent> = listOf()

    fun canChangeEmail(): String? {
        if (isEmailConfirmed) {
            return "Can't change email after it's confirmed"
        }
        return null
    }

    fun changeEmail(newEmail: String, company: Company) {

        require(canChangeEmail() == null)

        if (newEmail == this.email) {
            return
        }

        val newType = when(company.isEmailCorporate(newEmail)) {
            true -> UserType.EMPLOYEE
            false -> UserType.CUSTOMER
        }

        if (newType != this.type) {
            val delta = when(newType) {
                UserType.EMPLOYEE -> 1
                else -> -1
            }
            company.changeNumberOfEmployees(delta)
        }

        this.email = newEmail
        this.type = newType

        this.emailChangedEvents += EmailChangedEvent(userId, newEmail)
    }
}