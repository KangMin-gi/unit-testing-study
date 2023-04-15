package book.chapter6.listing4

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CustomerControllerTests {

    @Test
    fun `adding a comment to an article`() {
        val sut = Article()
        val text = "Comment text"
        val author = "John Doe"
        val now = LocalDateTime.of(2019, 4, 1, 0, 0)

        sut.addComment(text, author, now)

        assertEquals(1, sut.comments.count())
        assertEquals(text, sut.comments[0].text)
        assertEquals(author, sut.comments[0].author)
        assertEquals(now, sut.comments[0].dateCreated)
    }

    @Test
    fun `adding a comment to an article2`() {
        val sut = Article()
        val text = "Comment text"
        val author = "John Doe"
        val now = LocalDateTime.of(2019, 4, 1, 0, 0)

        sut.addComment(text, author, now)

        sut.shouldContainNumberOfComments(1)
            .withComment(text, author, now)
    }

    @Test
    fun `adding a comment to an article3`() {
        val sut = Article()
        val comment = Comment(
            text = "Comment text",
            author = "John Doe",
            dateCreated = LocalDateTime.of(2019, 4, 1, 0, 0),
            )

        sut.addComment(comment.text, comment.author, comment.dateCreated)

        assertThat(comment).isIn(sut.comments)
    }
}