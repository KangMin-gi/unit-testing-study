package book.chapter10.unitofwork

class UserFactory {

    companion object {
        fun create(data: Array<Any>): User {
            require(data.size >= 3)

            val id = data[0] as Int
            val email = data[1] as String
            val type = data[2] as UserType

            return User(id, email, type, false)
        }
    }
}