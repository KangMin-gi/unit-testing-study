package book.chapter7.canexecute

class Company(private val domainName: String, numberOfEmployees: Int) {

    var numberOfEmployees: Int = numberOfEmployees
        private set

    fun changeNumberOfEmployees(delta: Int) {
        require(numberOfEmployees + delta >= 0)
        numberOfEmployees += delta
    }

    fun isEmailCorporate(email: String): Boolean {
        val emailDomain = email.split('@')[1]
        return emailDomain == domainName
    }

}