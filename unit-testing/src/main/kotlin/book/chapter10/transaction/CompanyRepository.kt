package book.chapter10.transaction

import java.sql.DriverManager

class CompanyRepository(
    private val transaction: Transaction,
) {

    fun getCompany(): Array<Any> {
        DriverManager.getConnection(transaction.connectionString)
            .use { conn ->
                val query = "SELECT * FROM company"
                conn.prepareStatement(query)
                    .use { stmt ->
                        stmt.executeQuery(query)
                            .use { rs ->
                                val resultDomainName = rs.getInt("domainName")
                                val resultNumberOfEmployees = rs.getString("numberOfEmployees")

                                return arrayOf(resultDomainName, resultNumberOfEmployees)
                            }
                    }
            }
    }

    fun saveCompany(company: Company) {
        DriverManager.getConnection(transaction.connectionString)
            .use { conn ->
                val query = "UPDATE company SET domainName = ?, numberOfEmployees = ?"
                conn.prepareStatement(query)
                    .use { stmt ->
                        stmt.setString(1, company.domainName)
                        stmt.setInt(2, company.numberOfEmployees)
                        stmt.executeUpdate(query)
                    }
            }
    }
}