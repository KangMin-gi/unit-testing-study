package book.chapter11.mockingclasses

interface IDeliveryGateway {
    fun getDeliveries(customerId: Int): List<DeliveryRecord>
}