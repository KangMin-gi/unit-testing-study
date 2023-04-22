package book.chapter7._refactored3

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class UserTest {
    @Test
    fun `changing email without changing user type`() {
        val company = Company("mycorp.com", 1)
        val sut = User(1, "user@mycorp.com", UserType.EMPLOYEE)

        sut.changeEmail("new@mycorp.com", company)

        assertEquals(1, company.numberOfEmployees)
        assertEquals("new@mycorp.com", sut.email)
        assertEquals(UserType.EMPLOYEE, sut.type)
    }

    @Test
    fun `changing email from corporate to non corporate`() {
        val company = Company("mycorp.com", 1)
        val sut = User(1, "user@mycorp.com", UserType.EMPLOYEE)

        sut.changeEmail("new@gmail.com", company)

         assertEquals(0, company.numberOfEmployees)
         assertEquals("new@gmail.com", sut.email)
         assertEquals(UserType.CUSTOMER, sut.type)
    }

    @Test
    fun `changing email from non corporate to corporate`() {
        val company = Company("mycorp.com", 1)
        val sut = User(1, "user@gmail.com", UserType.CUSTOMER)

        sut.changeEmail("new@mycorp.com", company)

         assertEquals(2, company.numberOfEmployees)
         assertEquals("new@mycorp.com", sut.email)
         assertEquals(UserType.EMPLOYEE, sut.type)
    }

    @Test
    fun `changing email to the same one`() {
        val company = Company("mycorp.com", 1)
        val sut = User(1, "user@gmail.com", UserType.CUSTOMER)

        sut.changeEmail("user@gmail.com", company)

         assertEquals(1, company.numberOfEmployees)
         assertEquals("user@gmail.com", sut.email)
         assertEquals(UserType.CUSTOMER, sut.type)
    }
    
    @ParameterizedTest
    @CsvSource(
        "mycorp.com, email@mycorp.com, true",
        "mycorp.com, email@gmail.com, false"
    )
    fun `differentiates a corporate email from non corporate`(domain: String, email: String, expectedResult: Boolean) {
        val sut = Company(domain, 0)

        val isEmailCorporate = sut.isEmailCorporate(email)

        assertEquals(expectedResult, isEmailCorporate)
    }
}