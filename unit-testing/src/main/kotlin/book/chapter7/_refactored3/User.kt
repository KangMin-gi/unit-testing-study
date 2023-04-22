package book.chapter7._refactored3

class User(val userId: Int, email: String, type: UserType) {

    var email: String = email
        private set
    var type: UserType = type
        private set

    fun changeEmail(newEmail: String, company: Company) {

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
    }
}