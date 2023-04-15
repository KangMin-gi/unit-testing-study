package book.chapter6.listing7.mocks

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.time.LocalDateTime

class AuditManagerTest {

    @Test
    fun `a new file is created for the first entry`() {
        val fileSystemMock = mockk<IFileSystem>(relaxed = true)
        every { fileSystemMock.getFiles("audits") } returns listOf()
        val sut = AuditManager(3, "audits", fileSystemMock)

        sut.addRecord("Peter", LocalDateTime.of(2019, 4, 9, 13, 0, 0))

        verify { fileSystemMock.writeAllText(
            filePath = Path.of("audits\\audit_1.txt"),
            content = "Peter;2019-04-09T13:00:00"
        ) }
    }

    @Test
    fun `a new file is created when the current file overflows`() {
        val fileSystemMock = mockk<IFileSystem>(relaxed = true)
        every { fileSystemMock.getFiles("audits") } returns listOf(
            "audits\\audits_1.txt",
            "audits\\audits_2.txt",
        )
        every { fileSystemMock.readAllLines(Path.of("audits\\audits_2.txt")) } returns mutableListOf<String>(
            "Peter;2019-04-06T16:30:00",
            "Jane;2019-04-06T16:40:00",
            "Jack;2019-04-06T17:00:00",
        )
        val sut = AuditManager(3, "audits", fileSystemMock)

        sut.addRecord("Alice", LocalDateTime.of(2019, 4, 6, 18, 0, 0))

        verify { fileSystemMock.writeAllText(
            filePath = Path.of("audits", "audit_3.txt"),
            content = "Alice;2019-04-06T18:00:00",
        ) }
    }
}