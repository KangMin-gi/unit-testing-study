package book.chapter3.listing6

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDateTime
import java.util.stream.Stream

class DeliveryServiceTest {

    @Test
    fun delivery_with_a_past_date_is_invalid() {
        val sut = DeliveryService()
        val pastDate = LocalDateTime.now().plusDays(-1L)
        val delivery = Delivery(date = pastDate)

        val isValid = sut.isDeliveryValid(delivery)

        assertFalse(isValid)
    }

    companion object {
        @JvmStatic
        fun arguments(): Stream<Arguments> = Stream.of(
            Arguments.of(-1, false),
            Arguments.of(0, false),
            Arguments.of(1, false),
            Arguments.of(2, true),
        )
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun can_detect_an_invalid_delivery_date(daysFromNow: Long, expected: Boolean) {
        val sut = DeliveryService()
        val deliveryDate = LocalDateTime.now().plusDays(daysFromNow)
        val delivery = Delivery(date = deliveryDate)

        val isValid = sut.isDeliveryValid(delivery)

        assertEquals(expected, isValid)
    }

    @ParameterizedTest
    @ValueSource(ints = [-1, 0, 1])
    fun detects_an_invalid_delivery_date(daysFromNow: Long) {
        val sut = DeliveryService()
        val deliveryDate = LocalDateTime.now().plusDays(daysFromNow)
        val delivery = Delivery(date = deliveryDate)

        val isValid = sut.isDeliveryValid(delivery)

        assertEquals(false, isValid)
    }

    @Test
    fun the_soonest_delivery_date_is_two_days_from_now() {
        val sut = DeliveryService()
        val deliveryDate = LocalDateTime.now().plusDays(2)
        val delivery = Delivery(date = deliveryDate)

        val isValid = sut.isDeliveryValid(delivery)

        assertEquals(true, isValid)
    }
}