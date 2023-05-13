package book.chapter10.transaction

interface IDomainLogger {

    fun userTypeHasChanged(userId: Int, oldType: UserType, newType: UserType)
}