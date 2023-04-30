package book.chapter8.ambientcontext

internal interface IDomainLogger {

    fun userTypeHasChanged(userId: Int, oldType: UserType, newType: UserType)
}