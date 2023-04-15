package book.chapter6.listing7.mocks

import java.io.File
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AuditManager(
    private val maxEntriesPerFile: Int,
    private val directoryName: String,
    private val fileSystem: IFileSystem,
) {
    fun addRecord(visitorName: String, timeOfVisit: LocalDateTime) {
        val files = fileSystem.getFiles(directoryName) // <--
        val sorted = sortByIndex(files)

        val newRecord = "$visitorName;${timeOfVisit.format(DateTimeFormatter.ISO_DATE_TIME)}"

        if (sorted.isEmpty()) {
            fileSystem.writeAllText(Path.of(directoryName, "audit_1.txt"), newRecord) // <--
            return
        }

        val currentFilePath = sorted.last()
        val lines = fileSystem.readAllLines(Path.of(currentFilePath)) // <--

        if (lines.size < maxEntriesPerFile) {
            lines.add(newRecord)
            val newContent = lines.joinToString("\r\n")
            fileSystem.writeAllText(Path.of(currentFilePath), newContent) // <--
        } else {
            val newIndex = getIndex(currentFilePath) + 1
            val newName = "audit_$newIndex.txt"
            val newFile = Path.of(directoryName, newName)
            println(newFile)
            fileSystem.writeAllText(newFile, newRecord) // <--
        }
    }

    private fun sortByIndex(files: List<String>): List<String> {
        return files.stream()
            .sorted { o1, o2 -> getIndex(o1) - getIndex(o2) }
            .toList()
    }

    private fun getIndex(filePath: String): Int {
        val fileName = File(filePath).nameWithoutExtension
        return fileName.split("_")[1].toInt()
    }
}