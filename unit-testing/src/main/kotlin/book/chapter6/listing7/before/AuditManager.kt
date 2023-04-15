package book.chapter6.listing7.before

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AuditManager(
    private val maxEntriesPerFile: Int,
    private val directoryName: String,
) {
    fun addRecord(visitorName: String, timeOfVisit: LocalDateTime) {
        val files = File(directoryName).list()!!.toList()
        val sorted = sortByIndex(files)

        val newRecord = "$visitorName;${timeOfVisit.format(DateTimeFormatter.ISO_DATE_TIME)}"

        if (sorted.isEmpty()) {
            Files.write(Path.of(directoryName, "audit_1.txt"), newRecord.toByteArray())
            return
        }

        val currentFilePath = sorted.last()
        val lines = Files.readAllLines(Path.of(currentFilePath)).toMutableList()

        if (lines.size < maxEntriesPerFile) {
            lines.add(newRecord)
            val newContent = lines.joinToString("\r\n")
            Files.write(Path.of(currentFilePath), newContent.toByteArray())
        } else {
            val newIndex = getIndex(currentFilePath) + 1
            val newName = "audit_$newIndex.txt"
            val newFile = Path.of(directoryName, newName)
            Files.write(newFile, newRecord.toByteArray())
        }
    }

    private fun sortByIndex(files: List<String>): List<String> {
        return files.stream()
            .sorted { o1, o2 -> getIndex(o1) - getIndex(o2) }
            .toList()
    }

    private fun getIndex(filePath: String): Int {
        val fileName = File(filePath).nameWithoutExtension
        return fileName.split("_")[1].toInt();
    }
}