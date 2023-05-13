package book.chapter10.unitofwork

interface IDomainLogger {

    fun userTypeHasChanged(userId: Int, oldType: UserType, newType: UserType)
}