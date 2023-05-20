package book.chapter11.privatestate

class Customer {
    private var status = CustomerStatus.REGULAR

    fun promote() {
        this.status = CustomerStatus.PREFERRED
    }

    fun getDiscount(): Double {
        return if (this.status == CustomerStatus.PREFERRED) 0.05 else 0.0
    }
}