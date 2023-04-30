package book.chapter8.di

class User(val userId: Int, email: String, type: UserType, isEmailConfirmed: Boolean) {

    var email: String = email
        private set
    var type: UserType = type
        private set
    var isEmailConfirmed: Boolean = isEmailConfirmed
        private set
    var domainEvents: List<IDomainEvent> = listOf()

    fun canChangeEmail(): String? {
        if (isEmailConfirmed) {
            return "Can't change email after it's confirmed"
        }
        return null
    }

    fun changeEmail(newEmail: String, company: Company, logger: ILogger) {

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
            addDomainEvent(UserTypeChangedEvent(userId, type, newType)) // domain event
        }

        this.email = newEmail
        this.type = newType

        addDomainEvent(EmailChangedEvent(userId, newEmail)) // domain event

        // logging
        logger.info("Email is changed for user $userId")
    }

    private fun addDomainEvent(emailChangedEvent: EmailChangedEvent) {}

    private fun addDomainEvent(userTypeChangedEvent: UserTypeChangedEvent) {}
}