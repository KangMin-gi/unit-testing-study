package book.chapter10.transaction

class Transaction(
    val connectionString: String,
) {
    fun commit() {
        /* ... */
    }

    fun dispose() {
        /* ... */
    }
}