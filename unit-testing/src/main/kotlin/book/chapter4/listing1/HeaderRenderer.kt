package book.chapter4.listing1

class HeaderRenderer : IRenderer {

    override fun render(message: Message): String {
        return "<h1>${message.header}</h1>"
    }
}
