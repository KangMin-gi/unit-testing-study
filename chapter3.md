# 3장 단위 테스트 구조
## 3.1 단위 테스트를 구성하는 방법
### 3.1.1 AAA 패턴 사용
* AAA 패턴은 각 테스트를 준비(Arrange), 실행(Act), 검증(Assert)이라는 세 부분으로 나눌 수 있다. 3A 패턴이라고도 한다.
  1. 준비: 테스트 대상 시스템<sup>SUT, System Under Test</sup> 과 해당 의존성을 원하는 상태로 만든다.
  2. 실행: SUT 에서 메서드를 호출하고 준비된 의존성을 전달하며 (출력이 있으면) 출력 값을 캡처한다.
  3. 검증: 결과를 검증한다. 결과는 리턴 값이나 SUT 와 협력자의 최종 상태, SUT 가 협력자에 호출한 메서드 등으로 표시될 수 있다.
* Given-When-Then 패턴
  * Given = Arrange / When = Act / Then = Assert
  * 테스트 구성 측면에서 AAA 패턴과 동일하다.
  * AAA 패턴과의 유일한 차이점은 프로그래머가 아닌 사람에게 Given-When-Then 구조가 더 읽기 쉽다는 것이다.
* TDD 의 경우 => 먼저 기대하는 동작 (Assert) 으로 윤곽을 잡은 다음, 이러한 기대에 부응하기 위한 시스템을 어떻게 개발할 지 아는 것이 좋다.
* 제품 코드를 테스트 코드보다 먼저 작성하는 경우 (TDD) => 테스트를 작성할 시점에 실행에서 무엇을 예상하는지 이미 알고 있으므로 준비 구절부터 시작하는 것이 좋다.

### 3.1.2 여러 개의 준비, 실행, 검증 구절 피하기
* [준비] - [실행] - [검증] - [좀 더 실행] - [다시 검증] 과 같은 테스트는 여러 개의 동작 단위를 검증하므로 더 이상 단위 테스트가 아닌 통합 테스트다.
* 여러 개의 실행 구절이 있는 테스트는 리팩터링 해야 한다. 각 동작을 고유의 테스트로 도출하자.
* 통합 테스트에서는 실행 구절을 여러 개 두는 것이 괜찮을 때도 있다.

### 3.1.3 테스트 내 if 문 피하기
* 테스트 내 if 문은 안티 패턴이다.
* if 문은 테스트가 한 번에 너무 많은 것을 검증한다는 표시이므로, 이러한 테스트는 반드시 여러 테스트로 나누어야 한다.
* 통합 테스트에도 if 문은 없어야 한다.

### 3.1.4 각 구절은 얼마나 커야 하는가?
* 준비 구절이 가장 큰 경우
  * 일반적으로 준비 구절이 가장 크다.
  * 너무 커지는 경우 같은 테스트 클래스 내 private 메서드로 빼거나 별도의 팩토리 클래스로 뽑아내는 게 좋다.
  * 준비 구절의 코드 재사용에 적용 가능한 패턴으로는 오브젝트 마더<sup>Object Mother</sup> 와 테스트 데이터 빌더<sup>Test Data Builder</sup> 가 있다.
    * Object Mother : 테스트에서 필요한 객체를 생성하는 팩터리 메서드를 가진 클래스를 사용하는 패턴
    * Test Data Builder : 테스트에서 필요한 객체를 빌더를 통해 생성하고 사용하는 패턴
* 실행 구절이 한 줄 이상인 경우를 경계하라
  * 실행 구절은 보통 코드 한 줄이므로, 두 줄 이상인 경우 SUT 의 공개 API 에 문제가 있을 수 있다.
  * 단일 작업을 수행하는데 두 개의 메서드 호출이 필요하다는 것은 하나만 호출했을 때는 모순이 생긴다는 것이고 이를 불변 위반<sup>invariant violation</sup>이라 한다.
  * 이러한 잠재적 모순으로부터 코드를 보호하는 행위는 캡슐화<sup>encapsulation</sup> 라고 한다.
  * 비즈니스 로직은 실행 구절이 무조건 한 줄이어야 하지만. 유틸리티나 인프라 코드는 덜 적용되므로 주의하자.

### 3.1.5 검증 구절에는 검증문이 얼마나 있어야 하는가
* 검증 구절은 여러 개를 가져도 된다.
* 다만 검증 구절이 너무 커질 경우 제품 코드에서 추상화가 누락됐을 수 있다.

### 3.1.6 종료 단계는 어떤가
* 준비, 실행, 검증 이후의 네 번째 구절로 종료 구절을 따로 구분하기도 한다.
* 테스트에서 사용한 파일 삭제, 데이터베이스 연결 종료 등
* 일반적으로 별도의 메서드로 도출되어 클래스 내 모든 테스트에서 재사용된다.
* 대부분의 단위 테스트는 프로세스 외부 종속성을 사용하지 않으므로 종료 구절이 필요 없다.
* 통합 테스트에서는 사용할 수 있다.

### 3.1.7 테스트 대상 시스템 구별하기
* SUT (테스트 대상) 과 의존성을 구분하는 것이 중요하다.
* 테스트 내 SUT 의 인스턴스명을 sut로 하라.

### 3.1.8 준비, 실행, 검증 주석 제거하기
* ```// 준비```, ```// 실행```, ```// 검증``` 과 같은 주석을 제거하고 빈 줄로 구분하여 분리하자.
* 대규모 테스트에서는 준비 단계 내에서도 빈 줄을 추가해 설정 단계를 구분할 수 있으므로, 주석을 제거하지 말자.

## 3.2 xUnit 테스트 프레임워크 살펴보기

## 3.3 테스트 간 테스트 픽스처 재사용
* 테스트 단순화를 위해 준비 구절에서 코드를 재사용하는 올바른 방법에 대한 내용을 다룬다.
* 테스트 픽스처
  * 테스트 실행 대상 객체를 말한다. 이 객체는 데이터베이스에 있는 데이터, 하드 디스크의 파일과 같은 SUT 로 전달되는 인수이다. 이러한 객체는 각 테스트 실행 전에 알려진 고정 상태로 유지하기 때문에 동일한 결과를 생성한다. 따라서 픽스처라는 단어가 나왔다.
  * NUnit 프레임워크의 [TestFixture]: 테스트가 포함된 클래스를 표시
* 테스트 픽스처를 재사용하는 방법 첫번째: 테스트 생성자나 setUp 메서드에서 픽스처를 초기화 한다.
  * 단점: 테스트 간 결합도가 높아진다. 테스트 가독성이 떨어진다.
* 테스트 픽스처를 재사용하는 방법 두번째 => 3.3.3에 나옴

### 3.3.1 테스트 간의 높은 결합도는 안티 패턴이다
* 테스트를 수정해도 다른 테스트에 영향을 주어서는 안된다.
* 이 지침을 따르려면 테스트 클래스에 공유 상태를 두지 말아야 한다.

### 3.3.2 테스트 가독성을 떨어뜨리는 생성자 사용
* 준비 코드를 생성자로 추출하면 테스트 메서드가 무엇을 하는지 이해하기가 어렵다.

### 3.3.3 더 나은 테스트 픽스처 재사용법
* 테스트 픽스처를 재사용하는 방법 두번째: 테스트 클래스에 private 팩토리 메서드를 만든다.
  * 테스트 코드가 짧아지고, 동시에 테스트 진행 상황에 대한 전체 맥락을 유지할 수 있다.
  * 비공개 메서드를 충분히 일반화하면 테스트가 서로 결합되지 않는다.
```kotlin
class CalculatorTest3 {

    companion object {
        private fun createCustomer(): Customer {
            return Customer()
        }

        private fun createStoreWithInventory(product: Product, quantity: Int): Store {
            val store = Store()
            store.addInventory(product, quantity)
            return store
        }
    }

    @Test
    fun purchase_succeeds_when_enough_inventory() {
        val store = createStoreWithInventory(Product.Shampoo, 10)
        val sut = createCustomer()

        val success = sut.purchase(store, Product.Shampoo, 5)

        assertEquals(true, success)
        assertEquals(5, store.getInventory(Product.Shampoo))
    }

    @Test
    fun purchase_fails_when_not_enough_inventory() {
        val store = createStoreWithInventory(Product.Shampoo, 10)
        val sut = createCustomer()

        val success = sut.purchase(store, Product.Shampoo, 15)

        assertEquals(false, success)
        assertEquals(10, store.getInventory(Product.Shampoo))
    }
}
```
  * 예제의 createStoreWithInventory 라는 팩토리 메서드는 매우 읽기 쉽고 재사용이 가능하다.
  * 생성된 Store의 특성을 이해하려고 팩토리 메서드 내부를 알아볼 필요가 없어서 가독성이 좋다.
  
```kotlin
class CustomerTest3 : IntegrationTest(Database()) {
    
    @Test
    fun purchase_succeeds_when_enough_inventory() {
        /* 여기서 database 사용 */
        database.select()
    }
}

open class IntegrationTest(
    protected val database: Database,
) {
    
    fun select() {
        database.select()
    }

    fun dispose() {
        database.dispose()
    }
}

class Database {

    fun select() {
        println("select")
    }

    fun dispose() {}
}
```
* 모든 테스트 클래스에서 사용되는 픽스처(예: 데이터베이스 연결)의 경우 베이스 클래스를 두고 베이스 클래스의 생성자에서 초기화하는 것은 괜찮다.
* 예제는 IntegrationTests 라는 베이스 클래스 상속을 통해 database 인스턴스에 접근한다.

## 3.4 단위 테스트 명명법
* 가장 유명하지만 가장 도움이 되지 않는 방법 중 하나 => `{테스트 대상 메서드}_{시나리오}_{예상 결과}`
  * 동작 대신 세부 사항에 집중하게끔 부추긴다.
* 간단하고 쉬운 영어 구문이 더 효과적이고 표현력이 뛰어나다.
* 수수께끼 같은 이름은 기능의 구체적인 내용을 잃어버린 채 테스트를 작성하거나 동료가 작성한 테스트를 이해하려고 할 때 부담이 가중된다.

### 3.4.1 단위 테스트 명명 지침
* 엄격한 명명 정책을 따르지 않는다. 복잡한 동작에 대한 높은 수준의 설명을 이러한 정책의 좁은 상자 안에 넣을 수 없다. 표현의 자유를 허용하자.
* 문제 도메인에 익숙한 비개발자들에게 시나리오를 설명하는 것처럼 테스트 이름을 짓자. 도메인 전문가나 비즈니스 분석가가 좋은 예다.
* 단어를 underscore 표시로 구분한다. 그러면 특히 긴 이름에서 가독성을 향상시키는 데 도움이 된다.
* 테스트 클래스 이름을 정할 때 [클래스명]Test 패턴을 사용하지만, 단위 테스트는 동작 단위이지 클래스 단위가 아닌 것을 명심하자. 해당 테스트 클래스에 여러 클래스가 걸쳐 있을 수 있다.

### 3.4.2 예제: 지침에 따른 테스트 이름 변경
* 테스트 이름에 SUT 의 메서드 이름을 포함하지 말라. 코드를 테스트하는 것이 아니라 애플리케이션 동작을 테스트하는 것이라는 점을 명심하자.
  * 해당 명명 규칙을 사용하면 메서드 이름 변경 시 테스트 메서드 이름도 바꿔야 한다.
  * 유틸리티 코드의 경우는 단순 보조 기능이므로 SUT 메서드 이름을 사용해도 괜찮다.
```kotlin
  @Test
  fun isDeliveryValid_invalidDate_returnsFalse() {
      val sut = DeliveryService()
      val pastDate = LocalDateTime.now().plusDays(-1L)
      val delivery = Delivery(date = pastDate)

      val isValid = sut.isDeliveryValid(delivery)

      assertFalse(isValid)
  }
```
  * isDeliveryValid_invalidDate_returnsFalse 라는 이름을 delivery_with_a_past_date_is_invalid 라는 이름으로 바꿔보자.

## 3.5 매개변수화된 테스트 리팩터링하기
* 단위 테스트 프레임워크의 parameterized test 기능 (책에서는 .NET 의 xUnit을 다뤘지만 나는 JUnit5 로...)
```kotlin
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
```
* 매개변수화된 테스트를 사용하면 테스트 코드의 양을 크게 줄일 수 있지만, 테스트 메서드가 나타내는 사실을 파악하기가 어려워진다.
```kotlin
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
```
* 절충안 -> 긍정적인 테스트 케이스는 고유한 테스트로 도출하고, 가장 중요한 부분을 잘 설명하는 이름을 사용하자.
* 입력 매개변수만으로 expected 값도 결정할 수 있다면 긍정적인 테스트 케이스와 부정적인 테스트 케이스를 하나의 메서드로 합쳐라.
* 동작이 너무 복잡하면 parameterized 테스트를 사용하지 말라. 긍정/부정을 각각의 고유의 테스트 메서드로 나타내라.

### 3.5.1 매개변수화된 테스트를 위한 데이터 생성

## 3.6 검증문 라이브러리를 사용한 테스트 가독성 향상
* 검증문 라이브러리를 사용하면 검증문을 재구성해 가독성을 높일 수 있다.
* Java 에는 Fluent Assertions 라이브러리로 AssertJ 가 있다.
```kotlin
  @Test
  fun sum_of_two_numbers() {
      // Arrange
      val first: Double = 10.0
      val second: Double = 20.0
      val calculator = Calculator()

      // Act
      val result = calculator.sum(first, second)

      // Assert
      // assertEquals(30.0, result) 
      assertThat(result).isEqualTo(30.0)
  }
```
* junit5 의 기본 assert 문이 아닌 AssertJ 의 assert 문을 사용한 예제
* 유일한 단점은 프로젝트에 의존성을 추가해야 한다는 것이다.
