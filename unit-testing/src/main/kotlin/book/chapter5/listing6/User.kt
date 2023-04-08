package book.chapter5.listing6

class User{
    var name: String? = null
        set(value) {
            field = normalizeName(value)
        }

    private fun normalizeName(name: String?): String {
        name?.let {
            val result = it.trim()

            if (result.length > 50) {
                return result.substring(0, 50)
            }

            return result
        }
            ?: return ""
    }
}