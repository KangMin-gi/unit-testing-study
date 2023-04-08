package book.chapter5.listing5

data class User(
    var name: String? = null,
) {

    fun normalizeName(name: String?): String {
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