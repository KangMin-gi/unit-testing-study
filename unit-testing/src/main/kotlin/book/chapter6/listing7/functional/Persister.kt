package book.chapter6.listing7.functional

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.io.path.name

class Persister {
    fun readDirectory(directoryName: String): List<FileContent> {
        return Arrays.stream(File(directoryName).list())
            .map { file -> FileContent(Path.of(file).name, Files.readAllLines(Path.of(file))) }
            .toList()
    }

    fun applyUpdate(directoryName: String, update: FileUpdate) {
        val filePath = Path.of(directoryName, update.fileName)
        Files.write(filePath, update.newContent.toByteArray())
    }
}