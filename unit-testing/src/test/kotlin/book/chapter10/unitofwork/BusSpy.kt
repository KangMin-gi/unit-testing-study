package book.chapter10.unitofwork

import org.junit.jupiter.api.Assumptions

class BusSpy: IBus {
    private val sentMessages = mutableListOf<String>()

    override fun send(message: String) {
        sentMessages.add(message)
    }

    fun shouldSendNumberOfMessages(number: Int): BusSpy {
        Assumptions.assumeTrue(number == sentMessages.size)

        return this
    }

    fun withEmailChangedMessage(userId: Int, newEmail: String): BusSpy {
        val message = "Type: USER EMAIL CHANGED; " +
                "Id: $userId; " +
                "NewEmail: $newEmail"

        Assumptions.assumeTrue(sentMessages.contains(message))

        return this
    }
}