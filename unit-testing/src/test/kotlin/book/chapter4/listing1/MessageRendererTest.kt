package book.chapter4.listing1

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MessageRendererTest {

    @Test
    fun rendering_a_message() {
        val sut = MessageRenderer()
        val message = Message(
            header = "h",
            body = "b",
            footer = "f",
        )
        val html = sut.render(message)

        assertEquals("<h1>h</h1><b>b</b><i>f</i>", html)
    }

    @Test
    fun `MessageRenderer uses correct sub renderers`() {

        val sut = MessageRenderer()

        val renderers = sut.subRenderers

        assertEquals(3, renderers.size)
        assertTrue { renderers[0] is HeaderRenderer }
        assertTrue { renderers[1] is BodyRenderer }
        assertTrue { renderers[2] is FooterRenderer }
    }
}