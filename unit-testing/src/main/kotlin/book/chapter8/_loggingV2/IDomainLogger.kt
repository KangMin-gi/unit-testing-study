package book.chapter8._loggingV2

internal interface IDomainLogger {

    fun userTypeHasChanged(userId: Int, oldType: UserType, newType: UserType)
}