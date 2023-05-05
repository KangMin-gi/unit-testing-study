package book.chapter9.v1

interface IDomainLogger {

    fun userTypeHasChanged(userId: Int, oldType: UserType, newType: UserType)
}