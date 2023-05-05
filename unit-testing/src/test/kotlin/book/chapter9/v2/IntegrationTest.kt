package book.chapter9.v2

import book.chapter9.v1.*
import io.mockk.called
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class IntegrationTest {

    companion object {
        private const val connectionString = ""
    }

    @Test
    fun `changing email from corporate to non corporate`(){
        // Arrange
        val db = Database(connectionString)
        val user = createUser("user@mycorp.com", UserType.EMPLOYEE, db)
        createCompany("mycorp.com", 1, db)

        val busSpy = BusSpy()
        val messageBusMock = MessageBus(busSpy)
        val domainLoggerMock = mockk<IDomainLogger>()
        val sut = UserController(db, messageBusMock, domainLoggerMock)

        // Act
        val result = sut.changeEmail(user.userId, "new@gmail.com")

        // Assert
        assertEquals("OK", result)

        val userData = db.getUserById(user.userId)
        val userFromDb = UserFactory.create(userData)
        assertEquals("new@gmail.com", userFromDb.email)
        assertEquals(UserType.CUSTOMER, userFromDb.type)

        val companyData = db.getCompany()
        val companyFromDb = CompanyFactory.create(companyData)
        assertEquals(0, companyFromDb.numberOfEmployees)

        busSpy.shouldSendNumberOfMessages(1)
            .withEmailChangedMessage(user.userId, "new@gmail.com")
        verify(exactly = 1) { domainLoggerMock.userTypeHasChanged(user.userId, UserType.EMPLOYEE, UserType.CUSTOMER) }

        verify { domainLoggerMock wasNot called }
    }

    private fun createUser(email: String, type: UserType, database: Database): User {
        val user = User(0, email, type, false)
        database.saveUser(user)
        return user
    }

    private fun createCompany(domainName: String, numberOfEmployees: Int, database: Database): Company {
        val company = Company(domainName, numberOfEmployees)
        database.saveCompany(company)
        return company
    }

}