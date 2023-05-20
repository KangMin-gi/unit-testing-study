package book.chapter11.mockingclasses

class CustomerController(
    private val calculator: StatisticsCalculator,
    private val gateway: IDeliveryGateway,
) {
    fun getStatistics(customerId: Int): String {
        val records = gateway.getDeliveries(customerId)
        val (totalWeight, totalCost) = calculator.calculate(records)

        return "Total weight delivered: ${totalWeight}. " +
                "Total cost: $totalCost"
    }
}