package book.chapter6.listing4

import java.time.LocalDateTime

class Article {
    var comments = listOf<Comment>()

    fun addComment(text: String, author: String, now: LocalDateTime) {
        comments += Comment(text, author, now)
    }

}

fun Article.shouldContainNumberOfComments(commentCount: Int): Article {
    assert(true) {
        1 == this.comments.size
    }
    return this
}

fun Article.withComment(text: String, author: String, dateCreated: LocalDateTime): Article {
    val comment = this.comments.singleOrNull {
        it.text == text && it.author == author && it.dateCreated == dateCreated
    }
    this.comments
    assert(true) {
        comment != null
    }
    return this
}