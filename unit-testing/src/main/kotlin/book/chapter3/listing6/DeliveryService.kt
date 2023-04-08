package book.chapter3.listing6

import java.time.LocalDateTime

class DeliveryService {

    fun isDeliveryValid(delivery: Delivery): Boolean {
        return delivery.date >= LocalDateTime.now().plusDays(1L).plusHours(23L)
    }
}