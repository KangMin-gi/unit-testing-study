package book.chapter10.transaction

class DomainLogger(
    private val logger: ILogger,
) : IDomainLogger {

    override fun userTypeHasChanged(userId: Int, oldType: UserType, newType: UserType) {
        logger.info("User $userId changed type from $oldType to $newType")
    }

}