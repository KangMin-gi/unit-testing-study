package book.chapter4.listing1

class FooterRenderer: IRenderer {
    override fun render(message: Message): String {
        return "<i>${message.footer}</i>"
    }
}