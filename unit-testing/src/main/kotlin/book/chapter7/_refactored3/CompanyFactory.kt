package book.chapter7._refactored3

class CompanyFactory {
    companion object {
        fun create(data: Array<Any>): Company {
            require(data.size >= 2)

            val domainName = data[0] as String
            val numberOfEmployees = data[1] as Int

            return Company(domainName, numberOfEmployees)
        }
    }
}