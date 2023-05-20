package book.chapter11.mockingclasses

class StatisticsCalculator {

    fun calculate(records: List<DeliveryRecord>): ReturnType {
        val totalWeight = records.sumOf { it.weight }
        val totalCost = records.sumOf { it.cost }

        return ReturnType(totalWeight, totalCost)
    }

    class ReturnType(
        private val totalWeight: Double,
        private val totalCost: Double
    ) {
        operator fun component1() = totalWeight
        operator fun component2() = totalCost
    }

//    fun getDeliveries(customerId: Int): List<DeliveryRecord> {
//        return TODO("프로세스 외부 의존성을 호출해 배달 목록 조회")
//    }
}