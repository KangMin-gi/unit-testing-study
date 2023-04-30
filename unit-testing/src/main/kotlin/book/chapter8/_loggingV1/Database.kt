package book.chapter8._loggingV1

import java.sql.DriverManager

class Database(
    private val connectionString: String,
) {

    fun getUserById(userId: Int): Array<Any> {

        DriverManager.getConnection(connectionString)
            .use { conn ->
                val query = "SELECT * FROM user WHERE userId = ?"
                conn.prepareStatement(query)
                    .use { stmt ->
                        stmt.setInt(1, userId)
                        stmt.executeQuery(query)
                            .use { rs ->
                                val resultId = rs.getInt("userId")
                                val resultEmail = rs.getString("email")
                                val resultType = rs.getString("type")
                                val resultIsEmailConfirmed = rs.getBoolean("isEmailConfirmed")

                                return arrayOf(resultId, resultEmail, resultType, resultIsEmailConfirmed)
                            }
                    }
            }
    }

    fun saveUser(user: User) {
        DriverManager.getConnection(connectionString)
            .use { conn ->
                val updateQuery = "UPDATE user SET email = ?, type = ?, isEmailConfirmed = ? WHERE userId = ? SELECT userId"
                val insertQuery = "INSERT INTO user (email, type, isEmailConfirmed) VALUES (?, ?, ?) SELECT CAST(SCOPE_IDENTITY() as int)"

                val query = if (user.userId == 0) insertQuery else updateQuery
                conn.prepareStatement(query)
                    .use { stmt ->
                        stmt.setString(1, user.email)
                        stmt.setString(2, user.type.name)
                        stmt.setBoolean(3, user.isEmailConfirmed)
                        stmt.executeUpdate(query)
                    }
            }
    }

    fun getCompany(): Array<Any> {
        DriverManager.getConnection(connectionString)
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
        DriverManager.getConnection(connectionString)
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