package book.chapter10.unitofwork

import java.sql.DriverManager

class CompanyRepository(
    private val context: CrmContext,
) {

    fun getCompany(): Company {
        return TODO()
    }

    fun saveCompany(company: Company) {

    }
}