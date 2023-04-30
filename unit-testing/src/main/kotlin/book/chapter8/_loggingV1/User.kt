package book.chapter8._loggingV1

class User(val userId: Int, email: String, type: UserType, isEmailConfirmed: Boolean) {

    private var logger: Logger = Logger()

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

        // logging
        logger.info("Changing email for user $userId to $newEmail")

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
            // logging
            logger.info("User $userId changed type from $type to $newType")
        }

        this.email = newEmail
        this.type = newType

        this.emailChangedEvents += EmailChangedEvent(userId, newEmail)

        // logging
        logger.info("Email is changed for user $userId")
    }
}