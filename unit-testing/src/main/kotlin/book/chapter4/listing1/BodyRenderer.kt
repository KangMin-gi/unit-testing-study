package book.chapter4.listing1

class BodyRenderer: IRenderer {

    override fun render(message: Message): String {
        return "<b>${message.body}</b>"
    }

}