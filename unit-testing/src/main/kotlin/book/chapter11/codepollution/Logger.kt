package book.chapter11.codepollution

class Logger(
    private val isTestEnvironment: Boolean,
) {

    fun log(text: String) {
        if (isTestEnvironment) return

        /* log the text */
    }
}