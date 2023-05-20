package book.chapter11.mockingclasses

class DeliveryGateway: IDeliveryGateway {
    override fun getDeliveries(customerId: Int): List<DeliveryRecord> {
        return TODO("프로세스 외부 의존성을 호출해 배달 목록 조회")
    }
}