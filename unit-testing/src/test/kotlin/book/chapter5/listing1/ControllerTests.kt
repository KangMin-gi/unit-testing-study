package book.chapter5.listing1

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ControllerTests {

    @Test
    fun `sending a greetings email`() {
        val emailGatewayMock = mockk<IEmailGateway>(relaxed = true)
        val sut = Controller(emailGatewayMock)

        sut.greetUser("user@email.com")

        verify(exactly = 1) { emailGatewayMock.sendGreetingsEmail("user@email.com") }
    }

    @Test
    fun `creating a report`() {
        val stub = mockk<IDatabase>()
        every { stub.getNumberOfUsers() } returns 10
        val sut = Controller(stub)

        val report = sut.createReport()

        assertEquals(10, report.numberOfUsers)
    }
}