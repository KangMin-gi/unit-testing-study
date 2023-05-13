package book.chapter10.unitofwork

class CrmContext(
    private val connectionString: String,
) : AutoCloseable {

    fun saveChanges() {

    }

    override fun close() {
        TODO("Not yet implemented")
    }
}