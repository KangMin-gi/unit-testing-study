package book.chapter11.codepollution

class Controller {
    fun someMethod(logger: Logger) {
        logger.log("Some method is called")
    }
}