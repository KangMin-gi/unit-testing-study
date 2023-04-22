package book.chapter7.domainevents

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UserTest {
    @Test
    fun `changing email from corporate to non corporate`() {
        val company = Company("mycorp.com", 1)
        val sut = User(1, "user@mycorp.com", UserType.EMPLOYEE, false)

        sut.changeEmail("new@gmail.com", company)

        assertThat(company.numberOfEmployees).isEqualTo(0)
        assertThat(sut.email).isEqualTo("new@gmail.com")
        assertThat(sut.type).isEqualTo(UserType.CUSTOMER)
        assertThat(sut.emailChangedEvents).isEqualTo(listOf(EmailChangedEvent(1, "new@gmail.com")))
    }
}