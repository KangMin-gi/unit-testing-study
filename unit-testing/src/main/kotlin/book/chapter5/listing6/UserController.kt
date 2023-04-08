package book.chapter5.listing6

class UserController {
    fun renameUser(userId: Int, name: String) {
        val user = getUserFromDatabase(userId)
        user.name = name
        saveUserToDatabase(user)
    }

    private fun saveUserToDatabase(user: User) {
    }

    private fun getUserFromDatabase(userId: Int): User {
        return User()
    }
}