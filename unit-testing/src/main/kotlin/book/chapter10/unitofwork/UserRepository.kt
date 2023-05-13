package book.chapter10.unitofwork

import java.sql.DriverManager

class UserRepository(
    private val context: CrmContext,
) {

    fun getUserById(userId: Int): User {
        return TODO()
    }

    fun saveUser(user: User) {

    }
}