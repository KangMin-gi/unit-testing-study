package book.chapter10.transaction

import java.sql.DriverManager

class UserRepository(
    private val transaction: Transaction,
) {

    fun getUserById(userId: Int): Array<Any> {

        DriverManager.getConnection(transaction.connectionString)
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
        DriverManager.getConnection(transaction.connectionString)
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
}