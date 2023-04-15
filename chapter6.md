# 6장 단위 테스트 스타일
* 단위 테스트에는 출력 기반, 상태 기반, 통신 기반 세 가지 테스트 스타일이 있다..
* 품질 : 출력 기반 > 상태 기반 > 통신 기반
* 출력 기반 테스트는 품질이 가장 좋고 순수 함수 방식으로 작성된 코드에만 적용된다.
* 출력 기반 스타일로 변환하려면 함수형 아키텍처를 지향하게끔 재구성해야 한다.

## 6.1 단위 테스트의 세 가지 스타일
* 출력 기반 테스트<sup>output-based testing</sup>
* 상태 기반 테스트<sup>state-based testing</sup>
* 통신 기반 테스트<sup>communication-based testing</sup>

### 6.1.1 출력 기반 테스트 정의
* SUT 에 입력을 넣고 생성되는 출력을 점검하는 방식
* 전역 상태나 내부 상태를 변경하지 않는 코드(=함수)에만 적용되므로 반환 값만 검증하면 된다.
```kotlin
class PriceEngine {
    fun calculateDiscount(vararg product: Product): Double {
        val discount = product.size * 0.01
        return min(discount, 0.2)
    }
}

@Test
fun `discount of two products`() {
    val product1 = Product("Hand wash")
    val product2 = Product("Shampoo")
    val sut = PriceEngine()

    val discount = sut.calculateDiscount(product1, product2)

    assertEquals(0.02, discount)
}
```
*예제 6.1 출력 기반 테스트*
* *예제 6.1*에서 ```calculateDiscount()``` 메소드의 결과는 반환된 discount, 즉 출력값 뿐이다.
* 함수형<sup>functional</sup> 테스트 스타일
* 사이드 이펙트 없는 코드 선호를 강조하는 프로그래밍 방식인 함수형 프로그래밍 기반

### 6.1.2 상태 기반의 스타일 정의
* 작업이 완료된 후 시스템 상태를 확인하는 스타일
* ```상태```라는 용어는 SUT, 협력자, 데이터베이스, 파일 시스템 등의 상태를 의미할 수 있다.
```kotlin
class Order {
    var products = listOf<Product>()

    fun addProduct(product: Product) {
        products += product
    }
}

@Test
fun `adding a product to an order`() {
    val product = Product("Hand wash")
    val sut = Order()

    sut.addProduct(product)

    assertEquals(1, sut.products.count())
    assertEquals(product, sut.products[0])
}
```
*예제 6.2 상태 기반 테스트*
* *예제 6.1*에서 다룬 출력 기반 테스트의 예제와 달리, ```addProduct()```의 결과는 주문 상태의 변경이다.

### 6.1.3 통신 기반의 스타일 정의
* 목을 사용해 테스트 대상 시스템과 협력자 간의 통신을 검증한다.
```kotlin
@Test
fun `sending a greetings email`() {
    val emailGatewayMock = mockk<IEmailGateway>(relaxed = true)
    val sut = Controller(emailGatewayMock)

    sut.greetUser("user@email.com")

    verify(exactly = 1) { emailGatewayMock.sendGreetingsEmail("user@email.com") }
}
```
*예제 6.3 통신 기반 테스트*
* 고전파는 통신 기반 스타일보다 상태 기반 스타일을 선호한다.
* 런던파는 상태 기반 스타일보다 통신 기반 스타일을 선호한다.

## 6.2 단위 테스트 스타일 비교
* 좋은 단위 테스트의 4대 요소로 비교해보자.

### 6.2.1 회귀 방지와 피드백 속도 지표로 스타일 비교하기
* 회귀 방지 지표
  * 회귀 방지 지표를 결정짓는 세 가지 특성
    * 테스트 중에 실행되는 코드의 양
    * 코드 복잡도
    * 도메인 유의성 
  * 보통 실행하는 코드가 많든 적든 원하는 스타일의 테스트를 작성할 수 있다. 즉, 특정 스타일의 테스트를 작성한다고 해서 코드의 양과 상관관계가 없다.
  * 코드 복잡도 또한 상관없이 원하는 스타일의 테스트를 작성할 수 있다. 즉, 특정 스타일의 테스트를 작성한다고 해서 코드 복잡도와 상관관계가 없다. 
  * 도메인 유의성도 상관없이 원하는 스타일의 테스트를 작성할 수 있다. 즉, 특정 스타일의 테스트를 작성한다고 해서 도메인 유의성과 상관관계가 없다.
* 테스트 피드백 속도
  * 테스트 스타일과 테스트 피드백 속도 사이에는 상관관계가 거의 없다.
  * 프로세스 외부 의존성을 사용하지 않는 테스트는 테스트 스타일과 상관없이 실행 속도가 거의 동일하다.
  * 목은 런타임에 지연 시간이 생기는 편이므로 통신 기반 테스트가 약간 나쁠 수는 있지만 테스트가 수만 개 수준이 아니라면 별로 차이는 없다.

### 6.2.2 리팩터링 내성 지표로 스타일 비교하기
* 리팩터링 내성 - 리팩터링 중에 발생하는 거짓 양성(허위 경보) 수에 대한 척도
* 즉 거짓 양성은 식별할 수 있는 동작이 아닌 구현 세부 사항과 결합한 테스트에서 나온다.
* 출력 기반 테스트의 리팩터링 내성
  * 테스트 대상 메서드가 구현 세부 사항이 아닌 한 거짓 양성 방지가 가장 우수하다.
* 상태 기반 테스트의 리팩터링 내성
  * 테스트와 제품 코드 간의 결합도가 클수록 구현 세부 사항에 테스트가 얽매이게 되어 거짓 양성이 되기 쉽다. 
  * 큰 API 영역에 의존하는 테스트이므로 구현 세부 사항과 결합할 가능성도 더 높다.
* 통신 기반 테스트의 리팩터링 내성
  * 테스트 대역으로 상호 작용을 확인하는 테스트는 스텁과의 상호 작용을 검증함으로써 깨지기 쉬워질 수 있다.
  * 애플리케이션 경계를 넘는 상호 작용, 사이드 이펙트가 외부 환경에 보이는 상호 작용일 때만 목을 쓰는 것이 권장된다.
  * 통신 기반 테스트이더라도 캡슐화를 잘 지키고 테스트를 식별할 수 있는 동작에만 결합하면 거짓 양성을 최소화 할 수 있다.

### 6.2.3 유지 보수성 지표로 스타일 비교하기
* 유지 보수성을 정의하는 두 가지 특성
  * 테스트를 이해하기 얼마나 어려운가(테스트 크기에 대한 함수)
  * 테스트를 실행하기 얼마나 어려운가(테스트에 직접적으로 관련 있는 프로세스 외부 의존성 개수에 대한 함수)
* 출력 기반 테스트의 유지 보수성
  * 테스트 크기 -> 메서드로 입력 공급 + 해당 출력 검증 두 가지로 요약 가능하므로 거의 항상 짧고 간결해서 가장 유지 보수하기 용이하다.
  * 프로세스 외부 의존성 개수 -> 테스트 대상 메소드가 전역 상태나 내부 상태를 변경하지 않으므로 프로세스 외부 의존성을 다루지 않는다. (프로세스 외부 의존성 개수 0개!)
* 상태 기반 테스트의 유지 보수성
  * 테스트 크기 -> 상태 검증이 출력 검증보다 더 많은 코드를 필요로 하므로 일반적으로 출력 기반 테스트보다 유지 보수가 어렵다. 
  ```kotlin
      @Test
      fun `adding a comment to an article`() {
          val sut = Article()
          val text = "Comment text"
          val author = "John Doe"
          val now = LocalDateTime.of(2019, 4, 1, 0, 0)
  
          sut.addComment(text, author, now)
  
          assertEquals(1, sut.comments.count())
          assertEquals(text, sut.comments[0].text)
          assertEquals(author, sut.comments[0].author)
          assertEquals(now, sut.comments[0].dateCreated)
      }
  ```
  *예제 6.4 많은 공간을 차지하는 상태 검증*
  * 테스트는 단순하고 ```Comment```가 1개이지만 검증부는 네 줄에 걸쳐 있다.
  * 상태 기반 테스트는 종종 훨씬 많은 데이터를 확인해야 하므로 크기가 커질 수 있다.
  ```kotlin
      @Test
      fun `adding a comment to an article2`() {
        val sut = Article()
        val text = "Comment text"
        val author = "John Doe"
        val now = LocalDateTime.of(2019, 4, 1, 0, 0)
      
        sut.addComment(text, author, now)
      
        sut.shouldContainNumberOfComments(1)
          .withComment(text, author, now)
      }
  ```
  *예제 6.5 검증문에 헬퍼 메소드 사용*
  * *예제 6.5*와 같이 코드를 숨기고 테스트를 단축하는 헬퍼 메소드로 문제를 완화할 수 있지만. 이러한 메소드를 작성하고 유지하는 데 상당한 노력이 필요하다.
  ```kotlin
      @Test
      fun `adding a comment to an article3`() {
          val sut = Article()
          val comment = Comment(
              text = "Comment text",
              author = "John Doe",
              dateCreated = LocalDateTime.of(2019, 4, 1, 0, 0),
          )
    
          sut.addComment(comment.text, comment.author, comment.dateCreated)

          assertThat(comment).isIn(sut.comments)
      }
  ```
  *예제 6.6 값으로 비교하는 Comment*
  * 상태 기반 테스트를 단축하는 또 다른 방법 -> 검증 대상 클래스의 동등 멤버 정의 (*예제 6.6*)
    * 또한 검증문 라이브러리를 써서 테스트를 단순하게 할 수 있다.
    * ```isIn``` 메소드를 통해 전체 컬렉션을 비교할 수 있으므로 컬렉션 크기 확인이 필요 없다. 
      * 이는 강력한 기술이지만, 본질적으로 element 가 VO 에 해당할 때만 효과적이다.
      * element 가 VO 가 아닌데도 사용하게 되면 코드 오염<sup>code pollution</sup>이 일어난다.
      * 코드 오염 : 단지 단위 테스트를 가능하게 하거나 단순화하기 위한 목적만으로 제품 코드베이스를 오염시키는 것
    * 상태 기반 테스트 크기를 줄이는 방법(헬퍼 메소드 사용, 값 객체로 클래스 변환하기)는 항상 적용할 수 있는 게 아니다.
    * 이런 방법을 적용할 수 있더라도 출력 기반 테스트보다 공간을 더 많이 차지하므로 유지 보수성이 떨어진다.
* 통신 기반 테스트의 유지 보수성
  * 통신 기반 테스트는 출력 기반 테스트와 상태 기반 테스트보다 유지 보수성이 떨어진다.
  * 테스트 크기 -> 테스트 대역과 상호 작용 검증을 설정해야 하므로 공간을 많이 차지한다. 목이 다른 목을 반환해야 하는 목 사슬이 있을 때 테스트는 더 커지고 유지 보수성이 떨어진다.

### 6.2.4 스타일 비교하기: 결론
|             | 출력 기반 | 상태 기반 | 통신 기반 |
|-------------|:-----:|:-----:|:-----:|
| **회귀 방지**   | 연관 X  | 연관 X  | 연관 X  |
| **피드백 속도**  | 연관 X  | 연관 X  | 연관 X  |
| **리팩터링 내성** |  좋음   |  중간   | 좋지 않음 |
| **유지 보수성**  |  좋음   |  중간   | 좋지 않음 |
* 출력 기반 테스트를 선호하라.

## 6.3 함수형 아키텍처 이해

### 6.3.1 함수형 프로그래밍이란?
* 수학적 함수<sup>mathematical function</sup>(순수 함수<sup>pure function</sup> 라고도 함)를 사용한 프로그래밍
* 수학적 함수
  * 숨은 입출력이 없는 함수(또는 메소드)
  * 모든 입출력은 메소드 이름, 인수, 반환 타입으로 구성된 메소드 시그니처<sup>method signature</sup>에 명시해야 한다.
  * 호출 횟수에 상관없이 주어진 입력에 대해 동일한 출력을 생성한다.
  ```kotlin
    fun calculateDiscount(product: Product): Double {
        val discount = product.size * 0.01
        return min(discount, 0.2)
    }
  ```
  *예제 6.1 의 calculateDiscount 메소드*
  * 하나의 입력(Product 배열)과 하나의 출력(Double 타입의 discount)이 있으며, 둘 다 메소드 시그니처에 명시 되어 있으므로 수학적 함수이다.
  * 이 함수에서 메소드 시그니처는 ```calculateDiscount(product: Product): Double``` 부분!
  * [정의] 수학에서의 함수는 첫 번째 집합<sup>정의역</sup>의 각 요소에 대해 두 번째 집합<sup>공역</sup>에서 정확히 하나의 요소를 찾는 두 집합 사이의 관계이다.
  * 코드 테스트를 힘들게 하는 숨은 입출력의 유형
    * 사이드 이펙트 : 인스턴스의 상태를 변경하거나 디스크의 파일을 업데이트 하는 등
    * 예외 : 예외를 던지는 메소드는 프로그램 흐름에 메소드 시그니처를 벗어나는 경로를 만든다.
    * 내외부 상태에 대한 참조 : 애플리케이션 내부의 정적 속성을 사용한 값 가져오기, 애플리케이션 외부의 데이터베이스에서 데이터를 가져오거나 private mutable 필드 참조 등은 메소드 시그니처에 없는 실행 흐름에 대한 입력이다.
  * 메소드가 수학적 함수인지 판별하는 가장 좋은 방법!
    * 프로그램의 동작을 변경하지 않고 해당 메소드에 대한 호출을 리턴값으로 대체할 수 있는지 확인하는 것
    * 메소드 호출을 해당 값으로 바꾸는 것을 참조 투명성<sup>referential transparency</sup>라고 한다.
  ```kotlin
    fun addComment(text: String, author: String, now: LocalDateTime) {
        comments += Comment(text, author, now)
    }
  ```
  * 수학적 함수처럼 보이지만 사이드 이펙트가 있는 예
  * 필드 ```comments``` 를 변경하기 때문이다.

### 6.3.2 함수형 아키텍처란?
* 사이드 이펙트가 없는 애플리케이션은 존재할 수 없고, 결국 함수형 프로그래밍의 목표는 비즈니스 로직을 처리하는 코드와 사이트 이펙트를 일으키는 코드를 분리하는 것이다.
* [정의] 함수형 아키텍처는 사이드 이펙트를 다루는 코드를 최소화하면서 순수 함수(불변) 방식으로 작성한 코드의 양을 극대화한다. '불변(immutable)'이란 변하지 않는 것을 의미한다. 일단 객체가 생성되면 그 상태는 바꿀 수 없다. 이는 생성 후 수정할 수 있는 변경 가능한(mutable) 객체와 대조적이다.
* 비즈니스 로직과 사이드 이펙트를 분리하기 위한 코드 유형 구분
  * 결정을 내리는 코드 : 사이드 이펙트가 필요 없으므로 수학적 함수로 작성 가능 (함수형 코어<sup>functional core</sup>, 불변 코어<sup>immutable core</sup> 라고도 함)
  * 해당 결정에 따라 작용하는 코드 : 수학적 함수에 의해 이뤄진 결정을 데이터베이스의 변경이나 메시지 버스로 전송된 메시지 같이 가시적인 부분으로 변환한다. (가변 셸<sup>mutable shell</sup> 이라고도 함)
* 함수형 코어와 가변 셸이 협력하는 방식
  1. 가변 셸은 모든 입력을 수집한다.
  2. 함수형 코어는 결정을 생성한다.
  3. 셸은 결정을 사이드 이펙트로 변환한다.
* 함수형 코어와 가변 셸을 잘 분리하려면 가변 셸이 의사 결정을 추가하지 않도록 함수형 코어에 정보가 충분히 있는지 확인하라!
* 목표는 출력 기반 테스트로 함수형 코어를 테스트하고 가변 셸은 적은 수의 통합 테스트에 맡기는 것이다.
* 캡슐화와 불변성
  * 캡슐화와 불변성은 '소프트웨어의 지속적인 성장을 가능하게 하는 것'이라는 같은 목표를 가진다.
  * 클래스 내부가 변질되지 않도록 하는 **캡슐화**
    * 데이터 수정이 가능한 API 노출 영역 축소
    * 나머지 API 를 철저히 조사
  * 불변성
    * 불변 클래스를 사용하여 변경을 막아 상태 변질을 막는다.
    * 그래서 함수형 프로그래밍에서는 캡슐화를 할 필요가 없다.
    * 즉, 클래스를 만들 때 클래스의 상태가 불변인지 한 번만 확인하면 된다.
  * 마이클 페더스 *객체 지향 프로그래밍은 작동 부분을 캡슐화해 코드를 이해할 수 있게 한다. 함수형 프로그래밍은 작동 부분을 최소화해 코드를 이해할 수 있게 한다.*

### 6.3.3 함수형 아키텍처와 육각형 아키텍처 비교
* 비슷한 점 : 관심사 분리
  * 헥사고날 아키텍처는 외부 애플리케이션과의 통신에 책임이 있는 애플리케이션 서비스 계층과 비즈니스 로직에 책임이 있는 도메인 계층을 분리한다.
  * 함수형 아키텍처는 결정과 실행을 분리하므로 헥사고날 아키텍처와 유사하다.
* 비슷한 점 : 의존성 간 단방향 흐름
  * 헥사고날 아키텍처에서 도메인 계층 내 클래스는 서로에게만 의존해야 하고 애플리케이션 서비스 게층에 의존하지 않는다.
  * 함수형 아키텍처의 불변 코어도 가변 셸에 의존하지 않는다.
* 다른 점 : 사이드 이펙트에 대한 처리
  * 함수형 아키텍처는 모든 사이드 이펙트를 불변 코어에서 비즈니스 연산 가장자리로 밀어내고, 이 가장자리를 가변 셸이 처리한다.
  * 헥사고날 아키텍처는 모든 수정 사항이 도메인 계층 내에만 있으면 괜찮다.(도메인 계층은 수정 사항만 가지고 있고, 애플리케이션 서비스 계층이 데이터베이스에 이 변경 사항을 적용한다.)
* [참고] 함수형 아키텍처는 헥사고날 아키텍처의 하위 집합이고, 극단적으로는 헥사고날 아키텍처 그 자체로 볼 수도 있다.

## 6.4 함수형 아키텍처와 출력 기반 테스트로의 전환
* 함수형 아키텍처로 리팩토링하는 단계
  * 프로세스 외부 의존성에서 목으로 변경
  * 목에서 함수형 아키텍처로 변경

### 6.4.1 감사 시스템 소개
* 상태 기반 테스트와 통신 기반 테스트를 출력 기반 테스트로 리팩토링하기 위한 샘플 프로젝트 -> 조직의 모든 방문자를 추적하는 감사 시스템
```kotlin
class AuditManager(
    private val maxEntriesPerFile: Int,
    private val directoryName: String,
) {
    fun addRecord(visitorName: String, timeOfVisit: LocalDateTime) {
        val files = File(directoryName).list()!!.toList()
        val sorted = sortByIndex(files)

        val newRecord = "$visitorName;${timeOfVisit.format(DateTimeFormatter.ISO_DATE_TIME)}"

        if (sorted.isEmpty()) {
            Files.write(Path.of(directoryName, "audit_1.txt"), newRecord.toByteArray())
            return
        }

        val currentFilePath = sorted.last()
        val lines = Files.readAllLines(Path.of(currentFilePath)).toMutableList()

        if (lines.size < maxEntriesPerFile) {
            lines.add(newRecord)
            val newContent = lines.joinToString("\r\n")
            Files.write(Path.of(currentFilePath), newContent.toByteArray())
        } else {
            val newIndex = currentFilePath + 1
            val newName = "audit_$newIndex.txt"
            val newFile = Path.of(directoryName, newName)
            Files.write(newFile, newRecord.toByteArray())
        }
    }

    private fun sortByIndex(files: List<String>): List<String> {
        return files.stream()
            .sorted { o1, o2 -> getIndex(o1) - getIndex(o2) }
            .toList()
    }

    private fun getIndex(filePath: String): Int {
        val fileName = File(filePath).nameWithoutExtension
        return fileName.split("_")[1].toInt();
    }
}
```
*예제 6.8 감사 시스템의 초기 구현*
* 생성자 - 파일당 최대 항목 수와 작업 디렉터리를 설정 매개변수로 받는다.
* 공개 메소드인 ```addRecord()```
  1. 작업 디렉토리에서 전체 파일 목록을 검색한다.
  2. 인덱스별로 정렬한다.
  3. 아직 감사 파일이 없으면 단일 레코드로 첫번째 파일을 생성한다.
  4. 감사 파일이 있으면 최신 파일을 가져와서 파일의 항목 수가 한계에 도달했는지에 따라 새 레코드를 추가하거나 새 파일을 생성한다.
* ```AuditManager``` 클래스는 파일 시스템과 연결되어 있어 테스트 하기가 어렵다. 테스트 전에 파일을 배치하고, 끝나면 삭제해야 한다.
  * 공유 의존성인 파일 시스템이 테스트 실행 흐름에 방해가 된다. (유지 보수성 BAD)
  * 파일 시스템을 사용하면 테스트가 느려진다. (빠른 피드백 BAD)

|             | 초기 버전 | 
|-------------|:-----:|
| **회귀 방지**   |  좋음   |
| **리팩터링 내성** |  좋음   |
| **빠른 피드백**  |  나쁨   |
| **유지 보수성**  |  나쁨   |
* 파일 시스템을 직접 사용하는 테스트는 단위 테스트가 아닌 통합 테스트이다.

### 6.4.2 테스트를 파일 시스템에서 분리하기 위한 목 사용
* 파일 시스템과 관련된 모든 연산을 별도의 클래스(```IFileSystem```)로 도출하고 생성자로 해당 클래스를 주입해본다.
```kotlin
class AuditManager(
  private val maxEntriesPerFile: Int,
  private val directoryName: String,
  private val fileSystem: IFileSystem,
) {
  fun addRecord(visitorName: String, timeOfVisit: LocalDateTime) {
    val files = fileSystem.getFiles(directoryName) // <--
    val sorted = sortByIndex(files)

    val newRecord = "$visitorName;${timeOfVisit.format(DateTimeFormatter.ISO_DATE_TIME)}"

    if (sorted.isEmpty()) {
      fileSystem.writeAllText(Path.of(directoryName, "audit_1.txt"), newRecord) // <--
      return
    }

    val currentFilePath = sorted.last()
    val lines = fileSystem.readAllLines(Path.of(currentFilePath)) // <--

    if (lines.size < maxEntriesPerFile) {
      lines.add(newRecord)
      val newContent = lines.joinToString("\r\n")
      fileSystem.writeAllText(Path.of(currentFilePath), newContent) // <--
    } else {
      val newIndex = getIndex(currentFilePath) + 1
      val newName = "audit_$newIndex.txt"
      val newFile = Path.of(directoryName, newName)
      fileSystem.writeAllText(newFile, newRecord) // <--
    }
  }

  private fun sortByIndex(files: List<String>): List<String> {
    return files.stream()
      .sorted { o1, o2 -> getIndex(o1) - getIndex(o2) }
      .toList()
  }

  private fun getIndex(filePath: String): Int {
    val fileName = File(filePath).nameWithoutExtension
    return fileName.split("_")[1].toInt()
  }
}
```
*예제 6.10 새로운 IFileSystem 인터페이스 사용*
* 파일 시스템이라는 공유 의존성이 사라진 ```AuditManager``` 에 대해 테스트를 독립적으로 실행할 수 있다.
```kotlin
    @Test
    fun `a new file is created when the current file overflows`() {
        val fileSystemMock = mockk<IFileSystem>(relaxed = true)
        every { fileSystemMock.getFiles("audits") } returns listOf(
            "audits\\audits_1.txt",
            "audits\\audits_2.txt",
        )
        every { fileSystemMock.readAllLines(Path.of("audits\\audits_2.txt")) } returns mutableListOf<String>(
            "Peter;2019-04-06T16:30:00",
            "Jane;2019-04-06T16:40:00",
            "Jack;2019-04-06T17:00:00",
        )
        val sut = AuditManager(3, "audits", fileSystemMock)

        sut.addRecord("Alice", LocalDateTime.of(2019, 4, 6, 18, 0, 0))

        verify { fileSystemMock.writeAllText(
            filePath = Path.of("audits", "audit_3.txt"),
            content = "Alice;2019-04-06T18:00:00",
        ) }
    }
```
*예제 6.11 목을 이용한 감사 시스템의 동작 확인*
* 애플리케이션은 최종 사용자가 볼 수 있는 파일을 생성하므로 이러한 사이드 이펙트는 식별할 수 있는 동작이다.
* 따라서 목을 사용하기에 타당하다.
* 이 테스트는 파일 시스템에 접근하지 않으므로 더 빨리 실행된다. (빠른 피드백 GOOD)
* 테스트를 만족시키려고 파일 시스템을 다룰 필요가 없으므로 유지비가 절감된다. (유지 보수성 GOOD)

|             | 초기 버전 | 목 사용 |
|-------------|:-----:|:----:|
| **회귀 방지**   |  좋음   |  좋음  |
| **리팩터링 내성** |  좋음   |  좋음  |
| **빠른 피드백**  |  나쁨   |  좋음  |
| **유지 보수성**  |  나쁨   |  중간  |

### 6.4.3 함수형 아키텍처로 리팩터링하기
* 사이드 이펙트를 클래스 외부로 완전히 이동시켜보자!
* ```AuditManager```는 파일에 수행할 작업을 둘러싼 결정만 하고, 새로운 클래스인 ```Persister```는 그 결정에 따라 파일 시스템에 업데이트를 적용한다.
* ```AuditManager``` -> 함수형 코어 / ```Persister``` -> 가변 셸
```kotlin
class AuditManager(
    private val maxEntriesPerFile: Int,
) {
    fun addRecord(files: List<FileContent>, visitorName: String, timeOfVisit: LocalDateTime): FileUpdate {
        val sorted = sortByIndex(files)

        val newRecord = "$visitorName;${timeOfVisit.format(DateTimeFormatter.ISO_DATE_TIME)}"

        if (sorted.isEmpty()) {
            return FileUpdate("audit_1.txt", newRecord)
        }

        val currentFileContent = sorted.last()
        val lines = currentFileContent.lines.toMutableList()

        return if (lines.size < maxEntriesPerFile) {
            lines.add(newRecord)
            val newContent = lines.joinToString("\r\n")
            FileUpdate(currentFileContent.fileName, newContent)
        } else {
            val newIndex = getIndex(currentFileContent.fileName) + 1
            val newName = "audit_$newIndex.txt"
            FileUpdate(newName, newRecord)
        }
    }

    private fun sortByIndex(files: List<FileContent>): List<FileContent> {
        return files.stream()
            .sorted { o1, o2 -> getIndex(o1.fileName) - getIndex(o2.fileName) }
            .toList()
    }

    private fun getIndex(filePath: String): Int {
        val fileName = File(filePath).nameWithoutExtension
        return fileName.split("_")[1].toInt();
    }
}
```
*예제 6.12 리팩터링 후의 AuditManager*
* ```AuditManager```는 직접 작업 디렉토리에 접근하지 않을 것이므로 디렉토리 경로 대신 `FileContent`을 받는다. 이 클래스가 가진 정보들로 결정을 내릴 수 있다.
* ```AuditManager```는 이제 직접 사이드 이펙트를 수행하지 않고 수행하고자 하는 사이드 이펙트에 대한 명령만을 리턴한다.
```kotlin
class Persister {
    fun readDirectory(directoryName: String): List<FileContent> {
        return Arrays.stream(File(directoryName).list())
            .map { file -> FileContent(Path.of(file).name, Files.readAllLines(Path.of(file))) }
            .toList()
    }

    fun applyUpdate(directoryName: String, update: FileUpdate) {
        val filePath = Path.of(directoryName, update.fileName)
        Files.write(filePath, update.newContent.toByteArray())
    }
}
```
*예제 6.13 AuditManager 의 결정에 영향을 받는 가변 셸 Persister*
* `Persister` 클래스는 작업 디렉토리를 읽는 것과 `AuditManager`로부터 받은 업데이트 명령을 작업 디렉토리에 수행하는 것만 한다.
* `Persister` 클래스에는 분기가 없다. 즉 비즈니스 로직이 없다. 비즈니스 로직은 모두 `AuditManager`에 있다. 이것이 비즈니스 로직과 사이드 이펙트의 분리이다.
* 파싱과 준비는 모두 함수형 코어에서 수행하므로, 가변 셸의 코드는 간결하게 유지된다.
* 헥사고날 아키텍처 분류 체계상 `AuditManager`와 `Persister` 사이에 애플리케이션 서비스라는 또 다른 클래스가 필요하다.
```kotlin
class ApplicationService(
    private val directoryName: String,
    private val auditManager: AuditManager,
) {
    private val persister by lazy { Persister() }

    fun addRecord(visitorName: String, timeOfVisit: LocalDateTime) {
        val files = persister.readDirectory(directoryName)
        val update = auditManager.addRecord(
            files = files,
            visitorName = visitorName,
            timeOfVisit = timeOfVisit,
            )
        persister.applyUpdate(directoryName, update)
    }
}
```
*예제 6.14 함수형 코어와 가변 셸 붙이기*
![img.png](img.png)
* 함수형 코어와 가변 셸을 붙이면서 애플리케이션 서비스가 외부 클라이언트를 위한 시스템의 진입점을 제공한다.
```kotlin
    @Test
    fun `A new file is created when the current file overflows`() {
        val sut = AuditManager(3)
        val files = listOf<FileContent>(
            FileContent("audit_1.txt", listOf()),
            FileContent("audit_2.txt", listOf(
                "Peter;2019-04-06T16:30:00",
                "Jane;2019-04-06T16:40:00",
                "Jack;2019-04-06T17:00:00",
            ))
        )

        val update = sut.addRecord(files, "Alice", LocalDateTime.of(2019,4,6,18,0,0))

        assertEquals("audit_3.txt", update.fileName)
        assertEquals("Alice;2019-04-06T18:00:00", update.newContent)
        
        // FileUpdate 클래스를 값 객체로 전환
        assertEquals(
            FileUpdate("audit_3.txt", "Alice;2019-04-06T18:00:00"),
            update
        )
        // FileUpdate 클래스를 값 객체로 전환 + Fluent Assertions
        Assertions.assertThat(update)
            .isEqualTo(FileUpdate("audit_3.txt", "Alice;2019-04-06T18:00:00"))
    }
```
*예제 6.15 목 없이 작성된 테스트*

|             | 초기 버전 | 목 사용 | 출력 기반 |
|-------------|:-----:|:----:|:-----:|
| **회귀 방지**   |  좋음   |  좋음  |  좋음   |
| **리팩터링 내성** |  좋음   |  좋음  |  좋음   |
| **빠른 피드백**  |  나쁨   |  좋음  |  좋음   |
| **유지 보수성**  |  나쁨   |  중간  |  좋음   |

* 초기 버전에 비해 빠른 피드백이 개선되었고 유지 보수성 지표도 향상되었다.
* 복잡한 목 설정이 필요 없고 단순한 입출력만 필요하므로 테스트 가독성이 올라간다.
* 함수형 코어가 생성한 명령은 항상 값이거나 값 객체이므로 `FileUpdate`를 값 객체로 전환하면 테스트 가독성이 향상된다.
```kotlin
        // FileUpdate 클래스를 값 객체로 전환
        assertEquals(
            FileUpdate("audit_3.txt", "Alice;2019-04-06T18:00:00"),
            update
        )

        // FileUpdate 클래스를 값 객체로 전환 + Fluent Assertions
        Assertions.assertThat(update)
            .isEqualTo(FileUpdate("audit_3.txt", "Alice;2019-04-06T18:00:00"))
```
*FileUpdate 를 값 객체(VO)로 전환한 후의 검증문*

### 6.4.4 예상되는 추가 개발
* 특정 방문자에 대한 언급을 모두 삭제해야 하는 유스케이스가 생긴다면?
  * 여러 파일에 영향을 주게 되므로, 새 메소드는 여러 개의 파일 명령을 리턴해야 한다.
  * 삭제 후 파일이 비게 되면 해당 파일도 같이 제거해야 할 수 있다. 이러한 경우 `FileUpdate`를 `FileAction`이라는 이름으로 바꾸고 `ActionType` enum 필드를 추가해서 수행할 명령 종류를 나타낼 수 있다.
  * `FileUpdate` 클래스에 에러 필드를 추가하거나 별도의 에러를 메소드 시그니처에 포함할 수 있다.
  * 애플리케이션 서비스에서는 받은 오류를 확인하면 파일 관련 명령을 `Persister`에 넘기지 않고 사용자에게 오류 메시지를 전달한다.

## 6.5 함수형 아키텍처의 단점 이해하기

### 6.5.1 함수형 아키텍처 적용 가능성
* 실행 흐름이 간단하지 않은 경우 함수형 아키텍처 적용이 어려울 수 있다.
  * 의사 결정 절차의 중간 결과에 따라 프로세스 외부 의존성에서 데이터를 질의한다든가...
  * 예를 들어, 방문 횟수의 임계치 초과 시 감사 시스템이 방문자의 접근 레벨을 확인해야 하고 이 데이터가 데이터베이스에 저장된 경우 ```AddRecord``` 에서 데이터베이스 조회가 필요하다. 
    이런 경우 숨은 입력이 생기므로 수학적 함수가 될 수 없고, 출력 기반 테스트 적용도 불가능하다.
  * 이러한 상황에 대한 두 가지 해결책
    * 디렉토리 내용과 방문자 접근 레벨을 함께 수집한다.
    * ```AuditManager``` 에 ```isAccessLevelCheckRequired()``` 와 같은 새로운 메소드를 둔다. ```addRecord()``` 호출 전에 이 메소드를 호출해서 방문자 접근 레벨을 가져올지를 결정한다.
  * 첫번째 방법의 장단점
    * 단점 : 성능 저하. 접근 레벨이 필요 없는 경우에도 무조건 데이터베이스에 조회한다.
    * 장점 : 비즈니스 로직과 외부 시스템과의 통신을 완전히 분리할 수 있다. 즉 이전처럼 모든 의사 결정이 ```AuditManager```에 있다.
  * 두번째 방법의 장단점
    * 장점 : 첫번째 방법의 단점 해결을 위해 ```AuditManager```의 클라이언트가 데이터베이스 조회 여부를 결정한다. 
* 협력자와 값
  * ```AuditManager```의 ```addRecord()``` 메소드는 메소드 시그니처에 없는 의존성이 있다. (```maxEntriesPErFile``` 필드)
  * 메소드 argument 에 없을지라도 의존성을 숨긴 것이 아니다. 생성자 시그니처에는 존재하고 불변한 **값**이므로 괜찮다.
  * ```IDatabase``` 의존성은 **값**이 아닌 협력자이므로 상황이 다르다.
    * 협력자는 다음 중 하나에 해당하는 의존성이다.
      * 가변
      * 아직 메모리에 있지 않은 데이터에 대한 프록시 (공유 의존성)
    * ```IDatabase``` 인스턴스는 두번째 범주에 속하므로 협력자이다.
    * 프로세스 외부 의존성에 대한 호출이 더 필요하므로 출력 기반 테스트 사용이 불가능하다. (?)

### 6.5.2 성능 단점
* 함수형 아키텍처의 테스트의 성능은 좋다. (출력 기반 테스트이므로..)
* 시스템은 프로세스 외부 의존성을 더 많이 호출하므로 성능이 떨어진다.
* 감사 시스템의 초기 버전과 목을 사용한 버전은 모두 작업 디렉토리에서 모든 파일을 읽지 않았지만, 최종 버전은 읽고-결정하고-실행하기<sup>read-decide-act</sup> 방식을 따르도록 작업 디렉토리의 모든 파일을 읽었다.
* 함수형 아키텍처와 전통적인 아키텍처 사이의 선택 = 성능과 유지 보수성의 trade-off

### 6.5.3 코드베이스 크기 증가
* 함수형 아키텍처는 함수형 코어와 가변 셸을 분리하면서 코드 복잡도가 낮아지고 유지 보수성이 향상되지만 초기에 작성하는 코드베이스 크기가 커진다.
* 초기 코드베이스 작성에 들이는 노력에 의미가 없는 시스템도 있으므로 시스템의 복잡도와 중요성을 고려해 함수형 아키텍처를 전략적으로 적용하라.
* 함수의 순수성에 많은 비용이 든다면 순수성을 따르지 말라.
  * 대체로 모든 도메인 모델을 불변으로 할 수 없기 때문에 출력 기반 테스트에만 의존할 수 없다.
  * 그래서 출력 기반 스타일과 상태 기반 스타일을 조합하게 되며, 통신 기반 스타일을 약간 섞어도 된다.
  * 모든 테스트를 출력 기반 스타일로 만들자는 것이 아니라 가능한 한 많은 테스트를 전환하는 것을 목표로 하라.