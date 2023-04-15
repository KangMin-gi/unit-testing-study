package book.chapter6.listing7.mocks

import java.nio.file.Path

interface IFileSystem {
    fun getFiles(directoryName: String): List<String>
    fun writeAllText(filePath: Path, content: String)
    fun readAllLines(filePath: Path): MutableList<String>
}