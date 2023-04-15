package book.chapter6.listing4

import java.time.LocalDateTime
import kotlin.math.pow

class Comment(
    val text: String,
    val author: String,
    val dateCreated: LocalDateTime,
) {
    protected fun equals(other: Comment): Boolean {
        return text == other.text
                && author == other.author
                && dateCreated == other.dateCreated
    }

    override fun equals(other: Any?): Boolean {
        if (null == other) {
            return false
        }

        if (this === other) {
            return true
        }

        if (other.javaClass != this.javaClass) {
            return false
        }

        return equals(other as Comment)
    }

    override fun hashCode(): Int {
        var hashCode = if (text.isBlank()) text.hashCode() else 0
        hashCode = (hashCode * 397.0).pow(if (author.isBlank()) author.hashCode() else 0).toInt()
        hashCode = (hashCode * 397.0).pow(dateCreated.hashCode()).toInt()

        return hashCode
    }

}