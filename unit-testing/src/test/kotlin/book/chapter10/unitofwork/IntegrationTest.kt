package book.chapter10.unitofwork

import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.util.function.Function

class IntegrationTest {

    companion object {
        const val connectionString = ""
    }
    @Test
    fun `changing email from corporate to non corporate`(){
        // Arrange
        val user = createUser(
            email = "user@mycorp.com",
            type = UserType.EMPLOYEE,
        )
        createCompany("mycorp.com", 1)

        val busSpy = BusSpy()
        val messageBusMock = MessageBus(busSpy)
        val domainLoggerMock = mockk<IDomainLogger>()

        val result = execute(
            func = { x -> x.changeEmail(user.userId, "new@gmail.com") },
            messageBus = messageBusMock,
            logger = domainLoggerMock)

        // Assert
        assertEquals("OK", result)

        val userFromDb = queryUser(user.userId)
        userFromDb
            .shouldExist()
            .withEmail("new@gmail.com")
            .withType(UserType.CUSTOMER)

        val companyFromDb = queryCompany()
        companyFromDb
            .shouldExist()
            .withNumberOfEmployees()

        busSpy.shouldSendNumberOfMessages(1)
            .withEmailChangedMessage(user.userId, "new@gmail.com")
        verify(exactly = 1) { domainLoggerMock.userTypeHasChanged(user.userId, UserType.EMPLOYEE, UserType.CUSTOMER) }
    }

    private fun createUser(
        email: String = "user@mycorp.com",
        type: UserType = UserType.EMPLOYEE,
        isEmailConfirmed: Boolean = false): User {
        val context = CrmContext(connectionString)
        context.use {
            val user = User(0, email, type, isEmailConfirmed)
            val userRepository = UserRepository(context)
            userRepository.saveUser(user)

            context.saveChanges()

            return user
        }
    }

    private fun createCompany(
        domainName: String = "mycorp.com",
        numberOfEmployees: Int = 0): Company {
        val context = CrmContext(connectionString)
        context.use {
            val company = Company(domainName, numberOfEmployees)
            val companyRepository = CompanyRepository(context)
            companyRepository.saveCompany(company)

            context.saveChanges()

            return company
        }
    }

    private fun execute(
        func: Function<UserController, String>,
        messageBus: MessageBus,
        logger: IDomainLogger,
    ): String {
        val context = CrmContext(connectionString)
        context.use {
            val userController = UserController(context, messageBus, logger)

            return func.apply(userController)
        }
    }

    private fun queryUser(userId: Int): User {
        val context = CrmContext(connectionString)
        context.use {
            val userRepository = UserRepository(context)
            return userRepository.getUserById(userId)
        }
    }

    private fun queryCompany(): Company {
        val context = CrmContext(connectionString)
        context.use {
            val companyRepository = CompanyRepository(context)
            return companyRepository.getCompany()
        }
    }
}

fun User.shouldExist(): User {
    Assertions.assertNotNull(this)
    return this
}

fun User.withEmail(email: String): User {
    assertEquals(email, this.email)
    return this
}

fun User.withType(type: UserType): User {
    assertEquals(type, this.type)
    return this
}

fun Company.shouldExist(): Company {
    Assertions.assertNotNull(this)
    return this
}

fun Company.withNumberOfEmployees(): Company {
    Assertions.assertNotEquals(0, this.numberOfEmployees)
    return this
}