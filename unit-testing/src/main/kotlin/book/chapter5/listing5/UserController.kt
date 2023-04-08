package book.chapter5.listing5

class UserController {

    fun renameUser(userId: Int, name: String) {
        val user = getUserFromDatabase(userId)

        val normalizedName = user.normalizeName(name)
        user.name = normalizedName

        saveUserToDatabase(user)
    }

    private fun saveUserToDatabase(user: User) {
    }

    private fun getUserFromDatabase(userId: Int): User {
        return User()
    }
}