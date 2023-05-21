# 11장. 단위 테스트 안티 패턴

# 1. 비공개 메서드 단위 테스트

- 단위 테스트 하기 위해 비공개 메서드를 노출하면 안된다.
    - 식별할 수 있는 동작만 테스트한다는 단위 테스트 기본 원칙을 위반하는 것이다.
    - 비공개 메서드를 노출하면 테스트가 구현 세부 사항과 결합 되고 리팩터링 내성이 떨어진다.
    - 비공개 메서드를 직접 테스트하는 대신 식별할 수 있는 동작을 통해 간접적으로 테스트해야 한다.
- 비공개 메서드가 너무 복잡해서 식별할 수 있는 동작으로 테스트하기에 충분한 커버리지를 얻을 수 없는 경우가 있다.
    - 식별할 수 있는 동작은 이미 합리적인 테스트 커버리지가 있다고 가정한다.
    - 테스트에서 벗어난 코드가 어디에도 사용 되지 않는다면 죽은 코드이므로 삭제하는 것이 좋다.
    - 비공개 메서드가 너무 복잡하면 별도의 클래스로 도출해야 하는 추상화가 누락 되었다는 징후이다.
        
        ```java
        public class Order {
        	private Customer _customer;
        	private List<Product> _products;
        
        	public String generateDescription() { // 간단한 메서드
        		return "Customer name : " + _customer.getName() + ", total number of products : " + _products.getSize() + ", total price : " + getPrice();
        	}
        
        	private double getPrice() { // 복잡한 메서드
        		double basePrice = // _products에 기반한 계산;
        		double discounts = // _customer에 기반한 계산;
        		double taxes = // _products에 기반한 계산;
        		return basePrice - discounts + taxes;
        	}
        }
        ```
        
        - getPrice 메서드는 중요한 비즈니스 로직이 있기 때문에 테스트를 철저하게 해야 되지만 비공개 메서드이다.
        - 추상화가 누락되어 있으므로 비공개 메서드를 별도의 클래스로 도출해서 명시적으로 작성해야 한다.
        
        ```java
        public class Order {
        	private Customer _customer;
        	private List<Product> _products;
        
        	public String generateDescription() {
        		PriceCalculator priceCalculator = new PriceCalculator();
        		return "Customer name : " + _customer.getName() + ", total number of products : " + _products.getSize() + ", total price : " + priceCalculator.calculate(_customer, _products);
        	}
        }
        
        public class PriceCalculator {
        	public double calculate(Customer customer, List<Product> products) {
        		double basePrice = // _products에 기반한 계산;
        		double discounts = // _customer에 기반한 계산;
        		double taxes = // _products에 기반한 계산;
        		return basePrice - discounts + taxes;
        	}
        }
        ```
        
        - PriceCalculator 클래스의 calculate 메서드는 숨은 입출력이 없기 때문에 출력 기반 스타일의 단위 테스트를 사용할 수 있다.
- 비공개 메서드를 절대 테스트하지 말라는 규칙에도 예외는 있다.

<img width="568" alt="스크린샷 2023-05-19 오후 9 03 45" src="https://github.com/KangMin-gi/unit-testing-study/assets/7659412/c48dc81d-6bfa-4813-9bad-a09bbc61c710">

- 식별할 수 있는 동작이면서 비공개 메서드인 경우, 메서드가 식별할 수 있는 동작이 되려면 클라이언트 코드에서 사용 되어야 하는데 해당 메서드가 비공개인 경우에는 사용이 불가능하다.
- 비공개 메서드를 테스트하는 것 자체는 나쁘지 않지만 구현 세부 사항의 프록시에 해당하므로 나쁜 것이다.

```java
public class Inquiry {
	private boolean isApproved;
	private DateTime timeApproved;

	private Inquiry(bool isApproved, DateTime timeApproved) {
		if(isApproved && Objects.isNull(timeApproved)) {
			throw new Exception();
		}

		isApproved = isApproved;
		timeApproved = timeApproved;
	}

	public void approve(DateTime now) {
		if(isApproved) {
			return;
		}
		isApproved = true;
		timeApproved = now;
	}
}
```

- Inquiry 클래스는 인스턴스화 할 수 없는데 어떻게 테스트 해야 할까?
    - ORM 라이브러리에 의해 데이터베이스에서 클래스가 복원되기 때문에 생성자는 비공개다.
    - 승인 로직은 분명히 중요하기 때문에 단위 테스트를 거쳐야 한다.
    - 생성자를 공개하는 것은 비공개 메서드를 노출하지 않는 규칙을 위반하게 된다.
    - 하지만, 이러한 경우 생성자를 공개한다고 해서 테스트가 쉽게 깨지지 않는다.
    - 클래스의 공개 API 노출 영역을 가능한 한 작게 하려면 테스트에서 리플렉션을 통해 인스턴스화 할 수 있다.

# 2. 비공개 상태 노출

- 단위 테스트 목적으로만 비공개 상태를 노출하는 것은 안티 패턴이다.
- 비공개로 지켜야 하는 상태를 노출하지 말고 식별할 수 있는 동작만 테스트하라는 비공개 메서드 지침과 같다.

```java
public class Customer {
	private CustomerStatus _status = CustomerStatus.Regular; // 비공개 상태

	public void promote() {
		_status = CustomerStatus.Preferred;
	}

	public double getDiscount() {
		return _status == CustomerStatus.Preferred ? 0.05 : 0.0;
	}
}

public enum CustomerStatus {
	Regular, Preferred
}
```

- promote 메서드를 어떻게 테스트할 것인가?
    - 사이드 이펙트는 _status 필드의 변경이지만, 해당 필드는 비공개이므로 테스트할 수 없다.
    - 테스트는 제품 코드와 정확히 같은 방식으로 테스트 대상 시스템과 상호 작용해야 하며, 특별한 권한이 있으면 안된다.
    - 제품 코드에서 Customer 클래스를 사용할 때 고객의 상태에는 관심이 없고 승격 후 고객이 받는 할인에 관심이 있다. 이 부분이 테스트에서 확인해야 할 부분이다.
    - 나중에 제품 코드가 고객 상태 필드를 사용하면 SUT의 식별할 수 있는 동작이 되기 때문에 테스트에서 해당 필드를 사용할 수 있다.
- 테스트 유의성을 위해 공개 API 노출 영역을 넓히는 것은 좋지 않다.

# 3. 테스트로 유출된 도메인 지식

- 도메인 지식을 테스트로 유출하는 것 또한 안티 패턴이다.
- 보통 복잡한 알고리즘을 다루는 테스트에서 일어난다.

```java
public static class Calculator {
	// 복잡한 알고리즘이라고 가정
	public static int add(int value1, int value2) {
		return value1 + value2;
	}
}
```

```java
public class CalculatorTests {
	@Test
	public void adding_two_numbers() {
		int value1 = 1;
		int value2 = 3;
		int expected = value1 + value2; // 유출

		int actual = Calculator.add(value1, value2);

		Assertions.assertEquals(expected, actual);
	}
}
```

- 테스트는 제품 코드에서 알고리즘 구현을 복사 했기 때문에 구현 세부 사항과 결합 되어 리팩터링 내성 지표가 낮다.

```java
public class CalculatorTests {
@ParameterizedTest
  @CsvSource({
        "1,3,4",
        "11,33,44",
        "100,500,600"
  })
	public void adding_two_numbers(int value1, int value2, int expected) {
		int actual = Calculator.add(value1, value2);

		Assertions.assertEquals(expected, actual);		
	}
}
```

- 단위 테스트에서는 예상 결과를 하드코딩 하는 것이 좋다.
    - 하드코딩된 값의 중요한 부분은 SUT가 아닌 다른 것을 사용해 미리 계산하는 것이다.
    - 레거시 애플리케이션을 리팩터링할 경우에는 레거시 코드가 이러한 결과를 생성하도록 한 후 테스트에서 예상 값으로 사용한다.

# 4. 코드 오염

- 코드 오염은 테스트에만 필요한 코드를 제품 코드에 추가하는 것으로 안티 패턴이다.

```java
public class Logger {
	private final boolean _isTestEnvironment; // 스위치

	public Logger(boolean isTestEnvironment) {
		_isTestEnvironment = isTestEnvironment;
	}

	public void log(String text) {
		if(_isTestEnvironment) { // 스위치
			return;
		}
		// text에 대한 로깅
	}
}
```

- Logger가 운영 환경에서 실행되는 지 여부를 나타내는 생성자 매개변수가 있다.
    - 운영 환경에서 실행되면 로거는 메시지를 파일에 기록하고, 그렇지 않으면 아무것도 하지 않는다.

```java
@Test
public void some_test() {
	Logger logger = new Logger(true); // 테스트 환경임을 나타내고자 매개변수를 true로 설정
	Controller sut = new Controller();

	sut.someMethod(logger);

	// 검증
}
```

- 테스트 코드와 제품 코드가 혼재 되어 유지비가 증가하므로 테스트 코드를 제품 코드베이스와 분리해야 한다.

```java
public interface ILogger {
	void log(String text);
}

public class Logger implements ILogger {
	public void log(String text) {
		// text에 대한 로깅
	}
}

public class FakeLogger implements ILogger {
	public void log(String text) {
		// 아무것도 하지 않음
	}
}

public class Contoller {
	public void someMethod(ILogger logger) {
		logger.log("SomeMethod 호출");
	}
}
```

# 5. 구체 클래스를 목으로 처리하기

- 구체 클래스를 목으로 처리해서 클래스의 기능 일부를 보존할 수 있다.
    - 단일 책임 원칙을 위배한다는 단점이 있다.

```java
public class StatisticsCalculator {
	public (double totalWeight, double totalCost) calculate(int customerId) {
		List<DeliveryRecord> records = getDeliveries(customerId);
		double totalWeight = records.sum(x => x.weight);
		double totalCost = records.sum(x => x.cost);

		return (totalWeight, totalCost);
	}

	public List<DeliveryRecord> getDeliveries(int customerId) {
		// 프로세스 외부 의존성을 호출해 배달 목록 조회
	}
}

public class CustomerController {
	private final StatisticsCalculator _calculator;

	public CustomerController(StatisticsCalculator calculator) {
		_calculator = calculator;
	}

	public String getStatistics(int customerId) {
		(double totalWeight, double totalCost) = _calculator.calculate(customerId);
		return "Total weight delivered : " + totalWeight + ". Total cost : " + totalCost;
	}
}
```

- CustomerController 컨트롤러를 테스트 하기 위해 StatisticsCalculator 클래스를 목으로 처리하고 getDeliveries 메서드만 재정의 할 수 있다.

```java
@Test
public void customer_with_no_deliveries() {
	var stub = new Mock<StatisticsCalculator> { CallBase = true };
	stub.setup(x => x.getDeliveries(1)).returns(new List<DeliveryRecord>());
	var sut = new CustomerController(stub.Object);

	String result = sut.getStatistics(1);

	Assertions.assertEquals("Total weight delivered : 0. Total Cost : 0", result);
}
```

- CallBase = true 설정은 명시적으로 재정의하지 않는 한 목이 기초 클래스의 동작을 유지한다.
- 일부 기능을 지키려고 구체 클래스를 목으로 처리하면 단일 책임 원칙을 위반하므로 안티 패턴이다.
- StatisticsCalculator 클래스에는 비관리 의존성과 통신하는 책임과 통계를 계산하는 책임이 서로 관련이 없음에도 결합돼 있다.

```java
public class DeliveryGateway : IDeliveryGateway {
	public List<DeliveryRecord> getDeliveries(int customerId) {
		// 프로세스 외부 의존성을 호출해 배달 목록 조회
	}
}

public class StatisticsCalculator {
	public (double totalWeight, double totalCost) calculate(List<DeliveryRecord> records) {
		double totalWeight = records.sum(x => x.weight);
		double totalCost = records.sum(x => x.cost);

		return (totalWeight, totalCost);
	}
}

public class CustomerController {
	private final StatisticsCalculator _calculator;
	private final IDeliveryGateway _gateway;

	public CustomerController(StatisticsCalculator calculator, IDeliveryGateway gateway) {
		_calculator = calculator;
		_gateway = gateway;
	}

	public String getStatistics(int customerId) {
		var records = _gateway.getDeliveries(customerId);
		(double totalWeight, double totalCost) = _calculator.calculate(records);
		return "Total weight delivered : " + totalWeight + ". Total cost : " + totalCost;
	}
}
```

- 비관리 의존성과 통신하는 책임은 IDeliveryGateway에 있으므로 인터페이스를 목으로 처리할 수 있다.
- 험블 객체 디자인 패턴의 예제다.

# 6. 시간 처리하기

- 많은 어플리케이션의 기능에서는 현재 날짜와 시간에 대한 접근이 필요하다.
- 시간에 따라 달라지는 기능을 테스트하면 거짓 양성이 발생할 수 있다.
- 실행 단계의 시간이 검증 단계의 시간과 다를 수 있다.

### 앰비언트 컨텍스트로서의 시간

```java
public static class DateTimeServer {
	private static Func<DateTime> _func;
	public static Datetime now => _func();

	public static void init(Func<DateTime> func) {
		_func = func;
	}
}

DateTimeServer.init(() => DateTime.now()); // 운영 환경 초기화 코드
DateTimeServer.init(() => DateTime.of(2020,1,1)); // 단위 테스트 환경 초기화 코드
```

- 앰비언트 컨텍스트로 사용하는 것은 안티 패턴이다.
- 제품 코드를 오염시키고 테스트를 더 어렵게 한다.
- 정적 필드는 테스트 간에 공유하는 의존성을 도입해 해당 테스트를 통합 테스트 영역으로 전환한다.

### 명시적 의존성으로서의 시간

- 서비스 또는 일반 값으로 시간 의존성을 명시적으로 주입하는 것

```java
public interface IDateTimeServer {
	DateTime now;
}

public class DateTimeServer implements IDateTimeServer {
	public DateTime now => DateTime.now;
}

public class InquiryController {
	private final IDateTimeServer _dateTimeServer;

	public InquiryController(IDateTimeServer dateTimeServer) { // 시간을 서비스로 주입
		_dateTimeServer = dateTimeServer;
	}

	public void approveInquiry(int id) {
		Inquiry inquiry = getById(id);
		inquiry.approve(_dateTimeServer.now); // 시간을 값으로 주입
		saveInquiry(inquiry);
	}
}
```

- 시간을 서비스로 주입하는 것보다는 값으로 주입하는 것이 더 낫다.
- 제품 코드에서 일반 값으로 작업하는 것이 더 쉽고, 테스트에서 해당 값을 처리하기도 더 쉽다.
- 시간을 항상 일반 값으로 주입할 수는 없을 것이다. 의존성 주입 프레임워크가 값 객체와 잘 어울리지 않기 때문이다.
- 비즈니스 연산을 시작할 때는 서비스로 시간을 주입한 다음, 나머지 연산에서 값으로 전달하는 것이 좋다.
