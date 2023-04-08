package book.chapter3.listing1

import org.junit.jupiter.api.Test

class CustomerTest3 : IntegrationTest(Database()) {

    @Test
    fun purchase_succeeds_when_enough_inventory() {
        /* 여기서 database 사용 */
        database.select()
    }
}

open class IntegrationTest(
    protected val database: Database,
) {

    fun select() {
        database.select()
    }

    fun dispose() {
        database.dispose()
    }
}

class Database {

    fun select() {
        println("select")
    }

    fun dispose() {}
}