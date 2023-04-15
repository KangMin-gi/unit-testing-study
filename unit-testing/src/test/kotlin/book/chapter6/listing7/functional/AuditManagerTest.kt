package book.chapter6.listing7.functional

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class AuditManagerTest {
    @Test
    fun `A new file is created when the current file overflows`() {
        val sut = AuditManager(3)
        val files = listOf<FileContent>(
            FileContent("audit_1.txt", listOf()),
            FileContent("audit_2.txt", listOf(
                "Peter;2019-04-06T16:30:00",
                "Jane;2019-04-06T16:40:00",
                "Jack;2019-04-06T17:00:00",
            ))
        )

        val update = sut.addRecord(files, "Alice", LocalDateTime.of(2019,4,6,18,0,0))

        assertEquals("audit_3.txt", update.fileName)
        assertEquals("Alice;2019-04-06T18:00:00", update.newContent)

        // FileUpdate 클래스를 값 객체로 전환
        assertEquals(
            FileUpdate("audit_3.txt", "Alice;2019-04-06T18:00:00"),
            update
        )

        // FileUpdate 클래스를 값 객체로 전환 + Fluent Assertions
        Assertions.assertThat(update)
            .isEqualTo(FileUpdate("audit_3.txt", "Alice;2019-04-06T18:00:00"))
    }
}