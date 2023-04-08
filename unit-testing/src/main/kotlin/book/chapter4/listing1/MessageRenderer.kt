package book.chapter4.listing1

class MessageRenderer() : IRenderer {

    val subRenderers = listOf(
        HeaderRenderer(),
        BodyRenderer(),
        FooterRenderer(),
    )

    override fun render(message: Message): String {
        return subRenderers.joinToString("") { it.render(message) }
    }

}
