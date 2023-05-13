package book.chapter10.transaction

class Company(domainName: String, numberOfEmployees: Int) {

    var domainName: String = domainName
        private set
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