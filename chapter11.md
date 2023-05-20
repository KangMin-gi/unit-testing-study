# 11장 단위 테스트 안티 패턴

## 11.1 비공개 메서드 단위 테스트

### 11.1.1 비공개 메서드와 테스트 취약성
* 단위 테스트를 하기 위해 비공개 메서드를 노출하는 것은 식별할 수 있는 동작만 테스트하는 것을 위반한다.
* 비공개 메서드는 직접 테스트하지 말고, 포괄적인 식별할 수 있는 동작으로서 간접적으로 테스트하라.

### 11.1.2 비공개 메서드와 불필요한 커버리지
* 복잡한 비공개 메서드가 있고 식별할 수 있는 동작에 합리적인 테스트 커버리지가 있다고 가정했을 때 발생하는 문제 두 가지
  * 죽은 코드. 테스트에서 벗어난 코드가 어디에도 사용되지 않는다면 삭제하는 것이 좋다.
  * 추상화의 누락. 비공개 메서드가 너무 복잡하면 별도의 클래스로 도출해야 하는 추상화가 누락되었다는 징후다.
* 추상화 누락의 예를 들어본다.
```kotlin
class Order(
    private val customer: Customer,
    private val products: List<Product>,
) {

    fun generateDescription(): String {
        return "Customer name: ${customer.name}, " +
                "total number of products: ${products.size}, " +
                "total price: ${getPrice()}"
    }

    private fun getPrice(): Int {
        val basePrice = 0
        val discounts = 0
        val taxes = 0
        return basePrice - discounts + taxes
    }
}
```
*예제 11.1 복잡한 비공개 메서드가 있는 클래스*
* `generateDescription()` 메서드는 간단하지만, 훨씬 더 복잡한 `getPrice()` 비공개 메서드를 사용한다.
* 이를 해결하기 위해 `getPrice` 메서드를 노출하는 것보다는 추상화를 별도의 클래스로 도출해서 명시적으로 작성하자.
```kotlin
class Order(
    private val customer: Customer,
    private val products: List<Product>,
) {

    val calc = PriceCalculator()

    fun generateDescription(): String {
        return "Customer name: ${customer.name}, " +
                "total number of products: ${products.size}, " +
                "total price: ${calc.calculate(customer, products)}"
    }
}
class PriceCalculator {
  fun calculate(customer: Customer, products: List<Product>): Int {
    val basePrice = 0
    val discounts = 0
    val taxes = 0
    return basePrice - discounts + taxes
  }
}
```
*예제 11.2 복잡한 비공개 메서드 추출*
* 이제 `Order`와 별개로 `PriceCalculator`를 테스트할 수 있다.
* `PriceCalculator`에는 숨은 입출력이 없으므로 출력 기반의 단위 테스트를 사용할 수도 있다. 

### 11.1.3 비공개 메서드 테스트가 타당한 경우
* 신용 조회 관리 시스템을 예로 들어본다.
```kotlin
class Inquiry private constructor(
    var isApproved: Boolean,
    var timeApproved: LocalDateTime,
) {
    fun approve(now: LocalDateTime) {
        if (isApproved) return

        this.isApproved = true
        this.timeApproved = now
    }
}
```
*예제 11.3 비공개 생성자가 있는 클래스*
* ORM 라이브러리에 의해 데이터베이스에서 클래스가 복원되기 때문에 생성자가 비공개이다. (ORM 은 공개 생성자가 필요 없다.)
* `Inquiry`의 생성자는 비공개이면서 식별할 수 있는 동작의 예이다.
* 이러한 경우에는 `Inquiry`의 생성자를 공개로 변경해도 테스트가 쉽게 깨지지 않는다.
* 공개로 변경하지 않고 싶다면 테스트에서 리플렉션을 통해 `Inquiry`를 인스턴스화할 수 있다.

## 11.2 비공개 상태 노출
* 단위 테스트 목적으로만 비공개 상태를 노출하는 것도 안티 패턴이다.
```kotlin
class Customer {
    private var status = CustomerStatus.REGULAR

    fun promote() {
        this.status = CustomerStatus.PREFERRED
    }

    fun getDiscount(): Double {
        return if (this.status == CustomerStatus.PREFERRED) 0.05 else 0.0
    }
}
```
*예제 11.4 비공개 상태가 있는 클래스*
* 고객은 각각 `REGULAR` 상태로 생성된 후 `PREFERRED` 상태로 업그레이드 할 수 있고, 업그레이드되면 모든 항목에 대해 5% 할인을 받는다.
* `promote()` 메서드의 사이드 이펙트는 `status` 필드의 변경이지만 이 필드는 비공개이므로 테스트할 수 없4다.
* `status` 필드를 공개하는 것은 구현 세부 사항을 공개하는 것이므로 잘못된 방식이다.
* `promote()` 메서드를 테스트를 하려면 제품 코드가 이 클래스를 어떻게 사용하는지를 대신 확인해보는 것이 좋다.
  * 새로 생성된 고객은 할인이 없고, 업그레이드하면 5% 할인율이 적용된다는 사실을 테스트해보자.
> |참고| 테스트 유의성을 위해 공개 API 노출 영역을 넓히는 것은 좋지 않은 관습이다.

## 11.3 테스트로 유출된 도메인 지식
* 도메인 지식을 테스트로 유출하는 것도 흔한 안티 패턴이다.
```kotlin
object Calculator {
    fun add(value1: Int, value2: Int): Int {
        return value1 + value2
    }
}

class CalculatorTest {
  @Test
  fun `adding two numbers`() {
    val value1 = 1
    val value2 = 3
    val expected = value1 + value2

    val actual = Calculator.add(value1, value2)

    assertEquals(expected, actual)
  }

  @ParameterizedTest
  @CsvSource("1,3", "11,33", "100,500")
  fun `adding two numbers version 2`(value1: Int, value2: Int) {
    val expected = value1 + value2

    val actual = Calculator.add(value1, value2)

    assertEquals(expected, actual)
  }
}
```
*예제 11.5 알고리즘 구현 유출*
* 테스트가 제품 코드의 알고리즘 구현을 복사했다. => 안티 패턴이다.
* 구현 세부 사항과 결합되며, 리팩터링 내성 지표가 거의 0점이다.
```kotlin
  @ParameterizedTest
  @CsvSource("1,3,4", "11,33,44", "100,500,600")
  fun `adding two numbers version 2`(value1: Int, value2: Int, expected: Int) {
    val actual = Calculator.add(value1, value2)

    assertEquals(expected, actual)
  }
```
*예제 11.7 도메인 지식이 없는 테스트*
* 결과를 테스트에 하드코딩했다.
* 이는 테스트를 작성할 때 특정 구현을 암시하지 않는 것이므로 올바른 방식이다.

## 11.4 코드 오염
> |정의| 코드 오염은 테스트에만 필요한 제품 코드를 추가하는 것이다.
```kotlin
class Logger(
    private val isTestEnvironment: Boolean,
) {

    fun log(text: String) {
        if (isTestEnvironment) return

        /* log the text */
    }
}

class Controller {
  fun someMethod(logger: Logger) {
    logger.log("Some method is called")
  }
}
```
*예제 11.8 불 스위치가 있는 로거*
* 이 예제의 `Logger`에는 운영 환경인지 여부를 나타내는 매개변수가 있다.
* 이러한 Boolean switch 를 사용하면 다음처럼 테스트 실행 중에 로거를 비활성화할 수 있다.
```kotlin
@Test
fun `some test`() {
    val logger = Logger(true) // 테스트 환경임을 나타내고자 매개변수를 true 로 설정
    val sut = Controller()
    
    sut.someMethod(logger)
}
```
*예제 11.9 불 스위치를 사용한 테스트*
* 코드 오염은 테스트 코드와 제품 코드가 혼재돼 유지비 증가를 초래한다.
* 코드 오염이라는 안티 패턴을 방지하려면 테스트 코드를 제품 코드와 분리해야 한다.
* 이 예제에서는 `ILogger` 인터페이스를 도입해 두 가지 구현을 생성하라. 하나는 운영용, 하나는 테스트용.
* 처음의 `Logger`와 달리 `ILogger`에서는 운영 목적으로 사용하지 않는 코드 경로를 잘못 호출할 일이 없다. 즉 잠재적인 버그에 대한 노출 영역이 없다.

## 11.5 구체 클래스를 목으로 처리하기
* 구체 클래스를 목으로 처리해서 본래 클래스의 기능 일부를 보존하는 방법이 있다. 하지만 단일 책임 원칙을 위배한다는 단점이 있다.
```kotlin
class StatisticsCalculator {
    fun calculate(customerId: Int): ReturnType {
        val records = getDeliveries(customerId)
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

    fun getDeliveries(customerId: Int): List<DeliveryRecord> {
        return TODO("프로세스 외부 의존성을 호출해 배달 목록 조회")
    }
}
```
*예제 11.11 통계를 계산하는 클래스*
```kotlin
class CustomerController(
    private val calculator: StatisticsCalculator,
) {
    fun getStatistics(customerId: Int): String {
        val (totalWeight, totalCost) = calculator.calculate(customerId)

        return "Total weight delivered: ${totalWeight}. " +
                "Total cost: $totalCost"
    }
}
```
*예제 11.12 StatisticCalculator 를 사용하는 컨트롤러*
* `CustomerController`를 테스트하기 위해 실제 `StatisticCalculator` 인스턴스를 사용할 수 없다. 비관리 프로세스 외부 의존성을 참조하기 때문이다.
* 비관리 의존성을 스텁으로 대체하면서 `StatisticCalculator`를 완전히 교체하고 싶다면, 이 클래스 목으로 처리하고 `getDeliveries()` 메서드만 재정의하라.
```kotlin
@Test
fun `customer with no deliveries`() {
  val stub = spyk<StatisticsCalculator>()
  every { stub.getDeliveries(1) } returns listOf()
  val sut = CustomerController(stub)

  val result = sut.getStatistics(1)

  assertEquals("Total weight delivered: 0, Total cost: 0", result)
}
```
*예제 11.13 구체 클래스를 목으로 처리하는 테스트*
* `spyk`를 통해 Moq 의 `CallBase = true`와 동일한 상태의 목 클래스를 생성했다.
  * 명시적으로 재정의된 동작을 제외한 나머지 동작은 실제 클래스의 동작을 사용한다.
* 하지만 이 방식은 안티 패턴이다.
> |참고| 일부 기능을 지키려고 구체 클래스를 목으로 처리해야 하면, 이는 단일 책임 원칙을 위반하는 결과다.
* `StatisticCalculator`에는 비관리 의존성과 통신하는 책임과 통계를 계산하는 책임이 서로 관련이 없음에도 결합돼 있다. 이를 분리하자.
```kotlin
class DeliveryGateway: IDeliveryGateway {
  override fun getDeliveries(customerId: Int): List<DeliveryRecord> {
    return TODO("프로세스 외부 의존성을 호출해 배달 목록 조회")
  }
}

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
}
```
*예제 11.14 StatisticsCalculator 를 두 클래스로 나누기*
```kotlin
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
```
*예제 11.15 리팩터링 후의 컨트롤러*
```kotlin
@Test
fun `customer with no deliveries`() {
    val gateway = mockk<IDeliveryGateway>()
    val sut = CustomerController(StatisticsCalculator(), gateway)

    val result = sut.getStatistics(1)

    assertEquals("Total weight delivered: 0, Total cost: 0", result)
}
```
*리팩터링 후의 테스트*

## 11.6 시간 처리하기
* 시간에 따라 달라지는 기능을 테스트하면 거짓 양성이 발생할 수 있다.
* 이러한 시간에 대한 의존성을 안정화하는 데 세 가지 방법이 있고, 그 중 하나는 안티 패턴이다.

### 11.6.1 앰비언트 컨텍스트로서의 시간
> Ambient Context 는 정적 클래스 멤버를 사용하여 Volatile Dependency 또는 해당 동작 에 대한 전역 액세스와 함께 Composition Root 외부의 애플리케이션 코드를 제공합니다 .
* 첫 번째 방법은 앰비언트 컨텍스트<sup>ambient context</sup> 패턴을 사용하는 것이다.
```kotlin
object DateTimeServer {
    lateinit var func: () -> LocalDateTime
    val NOW = func.invoke()
    
    fun init(func: () -> LocalDateTime) {
        this.func = DateTimeServer.func
    }
}

DateTimeServer.init { LocalDateTime.now() }
DateTimeServer.init { LocalDateTime.of(2020, 1, 1, 0, 0) }
```
*예제 11.16 앰비언트 컨텍스트로서의 현재 날짜와 시간*
* 시간을 앰비언트 컨텍스트로 사용하는 것도 안티 패턴이다.
  * 앰비언트 컨텍스트는 제품 코드를 오염시키고 테스트를 더 어렵게 한다.
  * 정적 필드는 테스트 간에 공유하는 의존성을 도입해 해당 테스트를 통합 테스트 영역으로 전환한다.

### 11.6.2 명시적 의존성으로서의 시간
* 두 번째와 세 번째 방법은 서비스 또는 일반 값으로 시간 의존성을 명시적으로 주입하는 방법이다.
```kotlin
interface IDateTimeServer {
    val now: LocalDateTime
        get
}

class DateTimeServer() : IDateTimeServer {
  override val now: LocalDateTime
    get() = LocalDateTime.now()
}

class InquiryController(
  private val dateTimeServer: IDateTimeServer,
) {

  fun approveInquiry(id: Int) {
    val inquiry = getById(id)
    inquiry.approve(dateTimeServer.now)
    saveInquiry(inquiry)
  }

  fun saveInquiry(inquiry: Inquiry) {
  }

  fun getById(id: Int): Inquiry {
    return Inquiry(true, LocalDateTime.of(2020, 1, 1, 0, 0))
  }
}
```
*예제 11.17 명시적 의존성으로서의 현재 날짜와 시간*
* 두 방법 중에서는 값으로 주입하는 것이 더 낫다. 제품 코드에서 일반 값으로 작업하는 것이 더 쉽고, 테스트에서 해당 값을 스텁으로 처리하기도 더 쉽다.
* 의존성 주입 프레임워크는 값 객체 주입과 잘 맞지 않아 시간을 항상 일반 값으로 주입하기 어렵다. 그러므로 주입은 서비스로 하고 나머지 연산에서 값으로 전달하자.
* 컨트롤러가 생성자에서 `DateTimeServer`라는 서비스를 주입받지만, 이후 `Inquiry` 도메인 클래스에 `DateTime` 값을 전달한다.