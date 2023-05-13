package book.chapter10.unitofwork

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