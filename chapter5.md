# 5장 목과 테스트 취약성
* 목은 리팩터링 내성이 부족한 테스트를 초래할 수 있다.
* 하지만 경우에 따라 목을 적용할 수 있고, 목 사용이 바람직할 때도 있다.

## 5.1 목과 스텁 구분
* 테스트 대역의 유형에는 목<sup>mock</sup>과 스텁<sup>stub</sup>이 있다.

### 5.1.1 테스트 대역 유형
* 테스트 대역의 주 용도: 테스트를 편리하게 하는 것
* 테스트 대역은 테스트 대상 시스템으로 실제 의존성 대신 전달되므로 설정이나 유지 보수가 어려울 수 있다.
* 테스트 대역에는 더미, 스텁, 스파이, 목, 페이크 라는 다섯 가지가 있다. 실제로는 목과 스텁 두 가지 유형으로 나눌 수 있다.
* 목
    * 목과 스파이가 포함된다.
    * 외부로 나가는 상호 작용을 모방하고 검사하는 데 도움이 된다. 이러한 상호 작용은 SUT가 상태를 변경하기 위한 의존성을 호출하는 것에 해당한다.
    * 예: 이메일 발송을 위한 SMTP 서버 (SMTP 서버에 사이드 이펙트를 초래하는 상호 작용)
    * 스파이, 목
        * 스파이: 수동으로 작성
        * 목: mock 프레임워크의 도움을 받아 생성
* 스텁
    * 스텁, 더미, 페이크가 포함된다.
    * 내부로 들어오는 상호 작용을 모방하는 데 도움이 된다. 이러한 상호 작용은 SUT가 입력 데이터를 얻기 위한 의존성을 호출하는 것에 해당한다.
    * 예: DB 조회 (DB에서 데이터를 검색하는 것. 사이드 이펙트 X)
    * 더미, 스텁, 페이크
        * 더미: null 값이나 가짜 문자열과 같이 단순하고 하드코딩된 값
        * 스텁: 시나리오마다 다른 값을 반환하게끔 구성할 수 있도록 필요한 것을 다 갖춘 완전한 의존성
        * 페이크: 스텁과 유사하지만 아직 존재하지 않는 의존성을 대체하고자 구현함
* 목과 스텁의 차이: 목은 SUT와 관련 의존성 간의 상호 작용을 **모방하고 검사**하는 반면, 스텁은 **모방만** 한다.
  * 마틴 파울러의 글 [목은 스텁이 아니다](https://jaime-note.tistory.com/330) 참조
  * 일반적으로 단위 테스트를 작성할 때, mock 객체의 메서드의 리턴만 모방하는 경우는 스텁으로 본다.

### 5.1.2 도구로서의 목과 테스트 대역으로서의 목
* mock 의 의미
  * 목 라이브러리로 만드는 mock (도구로서의 mock)
    * Mockito 의 ```Mock()```, mockk 의 ```mock()``` 과 같은 도구로 만드는 mock
    * 이렇게 만들어진 mock 은 그 자체로는 (테스트 대역으로서의) mock이 아니다.
    * 테스트 대역 유형 중 mock 또는 stub 이 생성된다. 
  * 테스트 대역으로서의 mock: Mock 클래스로 만든 인스턴스
```kotlin
    @Test
    fun `sending a greetings email`() {
        val emailGatewayMock = mockk<IEmailGateway>(relaxed = true)
        val sut = Controller(emailGatewayMock)

        sut.greetUser("user@email.com")

        verify(exactly = 1) { emailGatewayMock.sendGreetingsEmail("user@email.com") }
    }
```
*예제 5.1*
```kotlin
    @Test
    fun `creating a report`() {
        val stub = mockk<IDatabase>()
        every { stub.getNumberOfUsers() } returns 10
        val sut = Controller(stub)

        val report = sut.createReport()

        assertEquals(10, report.numberOfUsers)
    }
```
*예제 5.2*
* 이 예제의 테스트들은 목 라이브러리로 생성된 mock 이다.
* *예제 5.1*의 ```emailGatewayMock```의 ```sendGreetingsEmail``` 메서드는 외부로 나가는 상호 작용(사이드 이펙트 있는 상호 작용)이므로 ```emailGatewayMock```는 (테스트 대역으로서의) mock 이다.
* *예제 5.2*의 ```stub```의 ```getNumberOfUsers``` 메서드는 내부로 들어오는 상호 작용을 모방하므로 ```stub```는 stub 이다.

### 5.1.3 스텁으로 상호 작용을 검증하지 말라
* mock 은 나가는 상호 작용을 모방하고 검사하는 반면, stub 은 내부로 들어오는 상호 작용만 모방하고 검사하지 않는다.
* stub 과의 상호 작용은 검증해서는 안된다.
  * stub 과의 상호 작용, 즉 SUT 에서 stub 으로의 호출은 SUT 가 생성하는 최종 결과가 아니다.
  * 최종 결과가 아닌 최종 결과를 산출하기 위한 수단일 뿐이다.
  * stub 은 SUT 가 출력을 생성하도록 입력을 제공하는 것이다.
  * stub 과의 상호 작용을 검증하는 것은 취약한 테스트를 야기하는 일반적인 안티 패턴이다. (stub 과의 상호 작용은 구현 세부 사항에 해당)

```kotlin
verify(exactly = 1) { emailGatewayMock.sendGreetingsEmail("user@email.com") }
```
* *예제 5.1*의 이 구문은 실제 결과에 부합하며, 도메인 전문가(=비개발자)에게 의미가 있다.
```kotlin
stub.getNumberOfUsers()
```
* *예제 5.2*의 이 구문은 SUT 가 보고서 작성에 필요한 데이터를 수집하는 방법에 대한 구현 세부 사항이다. 따라서 이러한 호출을 검증하는 것은 리팩터링 내성을 떨어뜨린다.
* *예제 5.2*는 ```report.numberOfUsers```, 즉 stub 과의 상호 작용을 검증한다.
  * 최종 결과가 아닌 사항을 검증하는 관행을 과잉 명세<sup>overspecification</sup>이라 한다.
  * 과잉 명세는 stub 과의 상호 작용을 검사할 때 가장 흔하게 발생한다.

### 5.1.4 목과 스텁 함께 쓰기
* 때로는 목과 스텁의 특성을 모두 나타내는 테스트 대역을 만들 필요가 있다.
```kotlin
    @Test
    fun `Purchase fails when not enough inventory`() {
    val storeMock = spyk(Store())
    every { storeMock.hasEnoughInventory(Product.Shampoo, 5) } returns false
    val customer = Customer()

    val success: Boolean = customer.purchase(storeMock, Product.Shampoo, 5)

    assertFalse(success)
    verify(exactly = 0) {
        storeMock.removeInventory(Product.Shampoo, 5)
    }
}
```
* 다음과 같은 2장의 테스트에서는 두 가지 목적으로 ```storeMock```을 사용한다.
  ```kotlin
    every { storeMock.hasEnoughInventory(Product.Shampoo, 5) } returns false
  ```
    * 준비된 응답을 반환한다.
  ```kotlin
    verify(exactly = 0) {
        storeMock.removeInventory(Product.Shampoo, 5)
    }
  ```
    * SUT 에서 수행한 메서드를 검증한다.
    * 하지만 이러한 두 메서드는 서로 다른 메서드이다. 
    * 메서드가 서로 다르면 'stub 과의 상호 작용을 검증하지 말라'는 규칙을 어기는 것이 아니다.
* mock 과 stub 의 특성을 모두 나타내는 테스트 대역은 mock 이라고 부른다. mock 이라는 사실이 stub 이라는 사실보다 더 중요하기 때문이다.

### 5.1.5 목과 스텁은 명령과 조회에 어떻게 관련돼 있는가?
* 명령 조회 분리<sup>CQS, Command Query Separation</sup> 원칙
    * 모든 메서드는 명령이거나 조회여야 하며, 이 둘을 혼용해서는 안된다.
    * 명령은 사이드 이펙트를 일으키고 어떤 값도 반환하지 않는 메서드다. (사이드 이펙트 예: 객체 상태 변경, 파일 시스템 내 파일 변경 등)
    * 조회는 그 반대로, 사이드 이펙트가 없고(멱등성 보장) 값을 반환한다.
* 명령과 조회를 명확히 분리하면 코드를 읽기가 쉽다.
* 하지만 항상 CQS 원칙을 따를 수 있는 것은 아니다. 사이드 이펙트를 초래하면서 값을 반환하는 게 적절한 메서드가 있다. (예: stack.pop()) 그래도 되도록이면 따르자!
* 명령을 대체하는 테스트 대역 => mock / 조회를 대체하는 테스트 대역 => stub
* *예제 5.1*의 SendGreetingEmail()은 이메일을 보내는 사이드 이펙트가 있으므로 mock, *예제 5.2*에서 GetNumberOfUsers()은 값을 반환하고 데이터베이스 상태를 변경하지 않는 조회이므로 stub

## 5.2 식별할 수 있는 동작과 구현 세부 사항
* 좋은 단위 테스트의 두 번째 특성인 리팩터링 내성은 테스트 취약성과 관련이 있고, 테스트가 취약한 이유는 코드의 내부 구현 세부 사항과 테스트가 결합되어 있기 때문이다.
* 구현 세부 사항과의 강결합을 피하기 위해서는 코드가 생성하는 최종 결과(식별할 수 있는 동작)를 검증하는 것이다.
* 즉, 테스트는 '어떻게'가 아니라 '무엇'에 중점을 두어야 한다.

### 5.2.1 식별할 수 있는 동작은 공개 API 와 다르다
* 제품 코드의 구분 기준 2가지
    * 공개 API 또는 비공개 API
        * 구분 방법: private, public, internal 등의 키워드 사용
    * 식별할 수 있는 동작 또는 구현 세부 사항
        * 구분 방법: 식별할 수 있는 동작이려면 그 메서드는 두 가지 중 한 가지 특징을 가진다. 구현 세부 사항은 둘 다 가지지 않는다.
            1. 클라이언트가 목표를 달성하는 데 도움이 되는 연산(계산을 수행하거나 사이드 이펙트를 초래하거나 둘 다) 제공
            2. 클라이언트가 목표를 달성하는 데 도움이 되는 상태(시스템의 현재 상태) 제공
        * 코드를 호출하는 클라이언트가 이 코드를 호출하는 목표와 직접적인 관계가 있어야 식별할 수 있는 동작이다.
* 이상적으로 시스템의 공개 API == 식별할 수 있는 동작, 비공개 API == 구현 세부 사항
* 공개 API 가 식별할 수 있는 동작의 범위를 넘어서면 시스템은 구현 세부 사항을 유출한다.

### 5.2.2 구현 세부 사항 유출: 연산의 예
```kotlin
data class User(
    var name: String? = null,
) {

    fun normalizeName(name: String?): String {
        name?.let {
                val result = it.trim()

                if (result.length > 50) {
                    return result.substring(0, 50)
                }

                return result
            }
            ?: return ""
    }
}

class UserController {

    fun renameUser(userId: Int, name: String) {
        val user = getUserFromDatabase(userId)

        val normalizedName = user.normalizeName(name)
        user.name = normalizedName

        saveUserToDatabase(user)
    }

    private fun saveUserToDatabase(user: User) {
    }

    private fun getUserFromDatabase(userId: Int): User {
        return User()
    }
}
```
*예제 5.5 구현 세부 사항을 유출하는 User 클래스*
* User 클래스의 API 가 잘 설계되지 않은 이유
    * Name 의 setter: 클라이언트 UserController 의 사용자 이름 변경이라는 목표를 달성할 수 있도록 하는 메서드
    * NormalizeName: 클라이언트의 목표에 직결되지 않는다. User 의 불변 속성을 만족시키기 위한 메서드이므로 이 메서드는 공개 API 로 유출되는 구현 세부 사항이다.
    * 해결하려면 NormalizeName 메서드를 숨기고 Name 의 setter 를 내부적으로 호출해야 한다.
* 구현 세부 사항이 유출되는지 판단하는 규칙: 단일한 목표를 달성하고자 클래스에서 호출해야 하는 연산의 수가 1보다 크면 유출 가능성이 있다.

### 5.2.3 잘 설계된 API 와 캡슐화
* 캡슐화는 불변성 위반이라고도 하는 모순을 방지하는 조치다. 
* *예제 5.5*의 User 클래스에는 사용자 이름이 초과하면 안된다는 불변성이 있었지만, 이 구현 세부 사항을 노출하여 불변성 위반을 가져왔다.
* 점점 증가하는 코드 복잡도에 대처할 수 있는 방법 => 캡슐화!
* 캡슐화는 코드가 변경됐을 때 모순이 생기지 않도록 해준다. 개발자가 실수할 가능성을 줄여준다.
* 마틴 파울러의 '묻지 말고 말하라<sup>tell-don't-ask</sup>'라는 원칙은, 구현 세부 사항을 숨기고 데이터를 연산 기능과 결합하라는 의미이다.
  ![img.png](https://mblogthumb-phinf.pstatic.net/MjAyMDAyMDVfMTE4/MDAxNTgwODM1NDc0NjYx.5e-6y3zzeWx0nuZ4VLjcCfQsWmiHIhJdfm_2999crgsg.5YsY4DFsSpI0eWtgaGSfAsddSLBzbxa3NbhZyZKxsmcg.PNG.sorang226/sketch.png?type=w800)
  * 왼쪽 객체에서 우측 데이터 객체로 계속해서 뭔가를 물어보고, 그 결과를 확인한 뒤에 로직을 처리하고 있습니다. 마틴 파울러는 이와 같은 방식이 OOP 방식에 맞지 않는다고 말한다. 그러면서 아래와 같이, 왼쪽 객체는 단순히 무언가를 해달라고 말만(Tell) 하면 되고, 그 Tell 은 곧 명령이 되어 우측 객체에서 로직과 데이터가 캡슐화 되어 처리된다.
* 구현 세부 사항을 숨기고 데이터와 기능을 결합하면 코드 캡슐화가 가능하다.
  * 구현 세부 사항을 숨기면 => 클라이언트 시야에서 클래스 내부를 가릴 수 있어서 내부를 손상시킬 위험이 적다!
  * 데이터와 연산을 결합하면 => 해당 연산이 클래스의 불변성을 위반하지 않도록 할 수 있다!

### 5.2.4 구현 세부 사항 유출: 상태의 예
* API 를 잘 설계하면 단위 테스트의 품질도 자동으로 좋아진다.
  * 모든 구현 세부 사항을 비공개로 하면 테스트가 식별할 수 있는 동작을 검증하는 것 말고는 할 수 있는 것이 없고, 따라서 리팩터링 내성도 자동으로 좋아진다.
  * 연산과 상태를 최소한으로 노출하자. 클라이언트가 목표를 달성하는 데 직접적으로 도움이 되는 코드만 공개해야 하며, 다른 것들은 모두 구현 세부 사항이므로 비공개해야 한다.

| | 식별할 수 있는 동작 | 구현 세부 사항 |
|:---|:---:|:---:|
| **공개** | 좋음 | 나쁨 |
| **비공개** | 불가능함 | 좋음 |

## 5.3 목과 테스트 취약성 간의 관계

### 5.3.1 육각형 아키텍처<sup>hexagonal architecture</sup> 정의
* 전형적인 애플리케이션은 도메인 계층(비즈니스 로직)과 애플리케이션 서비스 계층(외부 환경과의 통신 조정)으로 이루어져 있다.
* 이러한 여러 개의 육각형이 서로 소통하는 구조가 헥사고날 아키텍처이다.
* 헥사고날 아키텍처의 세 가지 중요 지침
    * **도메인 계층과 애플리케이션 서비스 계층 간의 관심사 분리**: 도메인 계층은 비즈니스 로직에 대해서만 책임을 져야 하며, 다른 모든 책임에서는 제외되어야 한다. 반대로 애플리케이션 서비스에서는 어떤 비즈니스 로직도 있으면 안된다.
    * **애플리케이션 내부 통신**: 헥사고날 아키텍처는 애플리케이션 서비스 계층에서 도메인 계층으로 흐르는 단방향 의존성 흐름을 규정한다. 도메인 계층 내부의 클래스는 그들끼리만 의존하고 애플리케이션 서비스 계층의 클래스에는 의존하지 않는다. 도메인 계층은 외부 환경에서 완전히 격리되어야 한다.
    * **애플리케이션 간의 통신**: 외부 애플리케이션은 애플리케이션 서비스 계층에 있는 공통 인터페이스를 통해 해당 애플리케이션에 연결된다. 아무도 도메인 계층에 직접 접근할 수 없다.
* API가 잘 설계되면 프랙탈 특성을 가지게 되고 테스트도 프랙탈 구조를 가진다. 즉, 달성하는 목표는 같지만 서로 다른 수준에서 동작을 검증한다.
    * 애플리케이션 서비스 계층의 테스트 -> 외부 클라이언트에게 매우 중요하고 큰 목표를 어떻게 이루는지 확인
    * 도메인 계층의 테스트 -> 그 큰 목표의 하위 목표를 검증
* 이전 장의 '어떤 테스트든 비즈니스 요구 사항으로 거슬러 올라갈 수 있어야 한다'는 원칙과 헥사고날 아키텍처 사이의 관계
    * 식별할 수 있는 동작은 바깥 계층에서 안쪽으로 흐른다. 외부 클라이언트에게 중요한 목표는 개별 도메인 클래스에서 달성한 하위 목표로 변환된다.
    * 잘 설계된 API에서의 테스트는 식별할 수 있는 동작만 결합되어 있기 떄문에 비즈니스 요구 사항과 관계가 있다.
```kotlin
class User{
    var name: String? = null
        set(value) {
            field = normalizeName(value)
        }

    private fun normalizeName(name: String?): String {
        name?.let {
            val result = it.trim()

            if (result.length > 50) {
                return result.substring(0, 50)
            }

            return result
        }
            ?: return ""
    }
}

class UserController {
    fun renameUser(userId: Int, name: String) {
        val user = getUserFromDatabase(userId)
        user.name = name
        saveUserToDatabase(user)
    }

    private fun saveUserToDatabase(user: User) {
    }

    private fun getUserFromDatabase(userId: Int): User {
        return User()
    }
}
```
*예제 5.8 애플리케이션 서비스와 도메인 클래스*
* 이 예제에서 ```normalizeName``` 메서드는 구현 세부 사항이므로 비공개로 해야 한다.
* 또한 이 메서드를 직접 확인하면 안되고, 식별할 수 있는 동작(이 예제에서는 name 의 setter)으로서만 검증해야 한다.

### 5.3.2 시스템 내부 통신과 시스템 간 통신
* 시스템 내부<sup>inter-system</sup> 통신: 애플리케이션 내 클래스 간의 통신
    * 예: 도메인 클래스 간의 협력 => 식별할 수 있는 동작이 아니므로 **구현 세부 사항**
    * mock 을 사용하면 테스트가 구현 세부 사항과 결합하여 리팩터링 내성이 떨어진다.
* 시스템 간<sup>intra-system</sup> 통신: 애플리케이션이 다른 애플리케이션과 통신하는 것
    * 예: 시스템 외부 환경과 통신하는 방식 => 클라이언트의 목표와 관계가 있으므로 **식별할 수 있는 동작**
    * mock 을 사용하면 통신 패턴을 확인하기가 좋다.

### 5.3.3 시스템 내부 통신과 시스템 간 통신의 예
```kotlin
class CustomerController(
    private val emailGateway: IEmailGateway
) {

    private val customerRepository = CustomerRepository()
    private val productRepository = ProductRepository()
    private val mainStore = Store()

    fun purchase(customerId: Int, productId: Int, quantity: Int): Boolean {
        val customer = customerRepository.getById(customerId)
        val product = productRepository.getById(productId)

        val isSuccess = customer.purchase(mainStore, product, quantity)

        if (isSuccess) {
            emailGateway.sendReceipt(customer.email!!, product.name!!, quantity)
        }

        return isSuccess
    }
}
```
*예제 5.9 외부 애플리케이션과 도메인 모델 연결하기*
* 다음 예제에서 ```CustomerController``` 클래스는 도메인 클래스(```Customer```, ```Product```, ```Store```)와 외부 애플리케이션(SMTP 서비스의 프록시인 ```EmailGateway```) 간의 작업을 조정하는 애플리케이션 서비스이다.
* purchase 라는 동작은 시스템 내부 통신과 시스템 간 통신이 모두 있는 비즈니스 유즈케이스이다.
* SMTP 서비스에 대한 호출은 외부 환경에서 볼 수 있는 사이드 이펙트이므로 **식별할 수 있는 동작**이다.
* SMTP 서비스에 대한 호출을 mock 으로 하는 것이 당연하다. 리팩터링 후에도 이러한 통신 유형이 그대로 유지되도록 하기 때문에 테스트 취약성을 야기하지 않는다.
```kotlin
    @Test
    fun `successful purchase`() {
        val mock = mockk<IEmailGateway>(relaxed = true)
        val sut = CustomerController(mock)

        val isSuccess = sut.purchase(customerId = 1, productId = 2, quantity = 5)

        assertTrue { isSuccess }
        verify(exactly = 1) {
            mock.sendReceipt("customer@email.com", "Shampoo", 5)
        }
    }
```
*예제 5.10 취약한 테스트로 이어지지 않는 mock 사용*
```kotlin
    @Test
    fun `purchase succeeds when enough inventory`() {
        val storeMock = mockk<IStore>(relaxed = true)
        every { storeMock.hasEnoughInventory(Product.Shampoo, 5) } returns true
        val customer = Customer()

        val success = customer.purchase(storeMock, Product.Shampoo, 5)

        assertTrue { success }
        verify(exactly = 1) { storeMock.removeInventory(Product.Shampoo, 5) }
    }
```
*예제 5.11 취약한 테스트로 이어지는 mock 사용*
* *예제 5.10*의 ```CustomerController```와 SMTP 서비스 간의 통신과 달리, ```Customer``` 클래스에서 ```Store``` 클래스로의 메서드 호출은 시스템 내부 호출이다.
* 클라이언트의 목표는 '구매'이고, ```store.removeInventory()```는 이 목표와 직접적인 연관이 없다.

## 5.4 단위 테스트의 고전파와 런던파 재고
* 런던파는 시스템 내 통신과 시스템 간 통신을 구분하지 않는다. 목을 무분별하게 사용하면 종종 구현 세부 사항에 결합돼 리팩터링 내성이 떨어진다.
* 고전파는 테스트 간에 공유하는 의존성만 교체하자는 입장이므로 이 문제에 더 유리하다. 그래도 고전파 역시 시스템 간 통신 처리에 이상적이지 않다. 런던파 만큼은 아니지만 mock 사용을 장려한다.

### 5.4.1 모든 프로세스 외부 의존성을 목으로 해야 하는 것은 아니다
* 공유 의존성 -> 테스트 격리를 위해 공유 의존성을 재사용하지 않도록 장려한다.
* 프로세스 외부 의존성 -> 인스턴스화가 어려우므로 일반적으로 목이나 스텁으로 교체한다. 
  * 의존성 중 해당 애플리케이션을 통해서만 접근할 수 있으면 식별할 수 있는 동작이 아니다. 예를 들면 데이터베이스가 있다.
  * 완전히 통제권을 가진 프로세스 외부 의존성에 mock 을 사용하면 깨지기 쉬운 테스트로 이어진다.

### 5.4.2 목을 사용한 동작 검증
* 종종 mock 이 동작을 검증한다고 하지만, 대부분의 경우 이 동작이라고 불리는 것들은 구현 세부 사항이다. 
* mock 은 애플리케이션의 경계를 넘나드는 상호 작용을 검증할 때와 이러한 상호 작용의 사이드 이펙트가 외부 환경에서 보일 때만 '동작'이다.