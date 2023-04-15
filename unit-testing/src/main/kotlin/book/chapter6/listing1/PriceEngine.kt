package book.chapter6.listing1

import kotlin.math.min

class PriceEngine {
    fun calculateDiscount(vararg product: Product): Double {
        val discount = product.size * 0.01
        return min(discount, 0.2)
    }
}