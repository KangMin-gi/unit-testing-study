package book.chapter6.listing7.functional

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AuditManager(
    private val maxEntriesPerFile: Int,
) {
    fun addRecord(files: List<FileContent>, visitorName: String, timeOfVisit: LocalDateTime): FileUpdate {
        val sorted = sortByIndex(files)

        val newRecord = "$visitorName;${timeOfVisit.format(DateTimeFormatter.ISO_DATE_TIME)}"

        if (sorted.isEmpty()) {
            return FileUpdate("audit_1.txt", newRecord)
        }

        val currentFileContent = sorted.last()
        val lines = currentFileContent.lines.toMutableList()

        return if (lines.size < maxEntriesPerFile) {
            lines.add(newRecord)
            val newContent = lines.joinToString("\r\n")
            FileUpdate(currentFileContent.fileName, newContent)
        } else {
            val newIndex = getIndex(currentFileContent.fileName) + 1
            val newName = "audit_$newIndex.txt"
            FileUpdate(newName, newRecord)
        }
    }

    private fun sortByIndex(files: List<FileContent>): List<FileContent> {
        return files.stream()
            .sorted { o1, o2 -> getIndex(o1.fileName) - getIndex(o2.fileName) }
            .toList()
    }

    private fun getIndex(filePath: String): Int {
        val fileName = File(filePath).nameWithoutExtension
        return fileName.split("_")[1].toInt();
    }
}