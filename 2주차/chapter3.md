# 3장 단위 테스트 구조
일반적으로 준비(Arrange), 실행(Act), 검증(Assert) 패턴 (AAA패턴) 으로 작성된 단위 테스트의 구조를 살펴볼 것이다.  
또한 단위 테스트 프레임 워크(xUnit)를 소개하고 왜 xUnit을 선택했는지 설명한다.

### 3.1 단위 테스트를 구성하는 방법
```java
 public class CalculatorTests    // 응집도 있는 테스트 세트를 위한 클래스 컨테이너
 {
   [Fact] // 테스트를 나타내는 xUnit 속성
   public void Sum_of_two_numbers()  // 단위 테스트 이름
   {
     // 준비 
     double first = 10;
     double second = 20;
     var calculator = new Calculator();
 
     // 실행
     double result = calculator.Sum(first, second); // 실행 구절
 
     // 검증
     Assert.Eqaul(30, result); // 검증 구절
   }
 }
```
- 준비 구절에서 테스트 대상 시스템(SUT)과 해당 의존성을 원하는 상태로 만든다.
- 실행 구절에서는 SUT에서 메서드를 호출하고 준비된 의존성을 전달하며 출력 값을 캡처한다.
- 검증 구절에서는 결과를 검증한다.

Given-When-Then 패턴 
AAA와 유사한 패턴
- Given - 준비 구절
- When - 실행 구절
- Then - 검증 구절
구성 측면에서는 두 가지 패턴 사이에 차이는 없다.  
유일한 차이는 프로그래머가 아닌 사람에게 Given-When-Then 구조가 더 읽기 쉽다는 것이다.  

**여러 개의 준비, 실행, 검증 구절 피하기**    
테스트 준비 -> 실행 -> 검증 -> 좀 더 실행 -> 다시 검증   
이러한 테스트 구조는 피하는 것이 좋다.  
실행이 하나면 간단하고 빠르고 이해하기 쉽다.  

통합 테스트에서는 실행 구절을 여러 개 두는 것이 괜찮을 때도 있다.
  
**테스트 내 if 문 피하기**  
테스트내에 if 문을 작성하는 것은 안티패턴이다.  
단위 테스트든 통합 테스트든 테스트는 분기가 없는 간단한 일련의 단계여야 한다.   

**각 구절은 얼마나 커야 하는가?**
- 준비 구절이 가장 큰 경우  
  일반적으로 준비 구절이 가장 크며 실행과 검증을 합친 만큼 클 수도 있다.  
같은 테스트 클래스 내 비공개 메서드 또는 별도의 팩토리 클래스로 도출하는 것이 좋다.  
준비 구절에서 사용되는 패턴 : 오브젝트 마더, 테스트 데이터 빌더   


- 실행 구절이 한 줄 이상인 경우를 경계하라  
실행 구절은 보통 코드 한줄이다.


- 검증 구절에는 검증문이 얼마나 있어야 하는가  
테스트당 하나의 검증을 가져야 한다.  
검증 구절이 너무 커지는 것은 경계야한다.


- 종료 단계는 어떤가.  
종료 단계는 통합 테스트 영역이다.   
단위 테스트는 종료 구절이 없지만   
예를 들어 테스트에 의해 작성된 파일을 지우거나 데이터베이스 연결을 종료하고 할 때 사용한다.  

**준비, 실행, 검증 주석 제거하기**  
테스트 내에 특정 부분이 어떤 구절에 속해 있는지 파악하는 데 시간을 많이 들이지 않도록  
각 구절을 시작하기 전에 주석(준비, 실행, 검증)을 다는 것이 좋다.  
- AAA 패턴을 따르고 준비 및 검증 구절에 빈줄을 추가하지 않아도 되는 테스트라면 구절 주석들을 제거하라  
- 그렇지 않으면 구절 주석(또는 빈줄)을 유지하라  

### 3.2 xUnit 테스트 프레임워크 살펴보기
모든 객체지향 언어에 단위 테스트 프레임워크가 있으며 모든 프레임워크는 매우 비슷하다.  
xUnit을 선호하는 까닭은 더 깨끗하고 간결하기 때문이다.  

### 3.3 테스트 간 테스트 픽스처 재사용
테스트에서 언제 어떻게 코드를 재사용하는지 아는 것이 중요하다.  
준비 구절에서 코드를 재사용하는 것이 테스트를 줄이면서 단순화하기 좋은 방법이다.  

**1. 테스트 생성자에서 초기화 코드 추출**
```java
 public class CustomerTests
 {
   private readonly Store _store;    // 공통 테스트 픽스처
   private readonly Customer _sut;
 
   public CustomerTests()  // 클래스 내 각 테스트 이전에 호출
   {
     _store = new Store();
     _store.AddInventory(Product.Shampoo, 10);
     _sut = new Customer();
   }
 
   [Fact]
   public void Purchase_succeeds_when_enough_inventory()
   {
     // ...
   }
 
   [Fact]
   public void Purchase_fails_when_not_enough_inventory()
   {
     // ...
   }
 }
 public class CustomerTests
{
    private readonly Store _store;    // 공통 테스트 픽스처
    private readonly Customer _sut;

    public CustomerTests()  // 클래스 내 각 테스트 이전에 호출
    {
        _store = new Store();
        _store.AddInventory(Product.Shampoo, 10);
        _sut = new Customer();
    }
 
   [Fact]
    public void Purchase_succeeds_when_enough_inventory()
    {
        // ...
    }
 
   [Fact]
    public void Purchase_fails_when_not_enough_inventory()
    {
        // ...
    }
}
```
준비 구절이 동일하므로 CustomerTests에 생성자를 호출하였다.  
이 방법으로 테스트 코드의 양을 줄일 수 있다.  

하지만 이는 테스트간의 결합도가 높아지고 가독성이 떨어진다.  

**테스트 간의 높은 결합도는 안티 패턴이다**  
테스트1 :
_store.AddInventory(Product.Shampoo, 10);

테스트2 :
_store.AddInventory(Product.Shampoo, 15);

상점의 초기 상테에 대한 가정을 무효화하므로 쓸데없이 테스트가 실패하게 된다.

**테스트 가독성을 떨어뜨리는 생성자 사용**  
준비 코드를 생성자로 추출할 때의 또 다른 단점은 테스트 가독성을 떨어뜨리는 것이다.  
테스트 메서드가 무엇을 하는지 이해하려면 클래스의 다른 부분도 봐야 한다.

**2. 더 나은 테스트 픽스처 재사용법 - 비공개 팩토리 메서드**
```java
 public class CustomerTests
 {
   [Fact]
   public void Purchase_succeeds_when_enough_inventory()
   {
     Store store = CreateStoreWIthInventory(Product.Shampoo, 10);
     // ...
   }
 
   [Fact]
   public void Purchase_fails_when_not_enough_inventory()
   {
     Store store = CreateStoreWIthInventory(Product.Shampoo, 10);
     // ...
   }
 
   private Store CreateStoreWIthInventory(Product product, int quantity)
   {
     Store store = new Store();
     store.AddInventory(product, quantity);
     return store;
   }
 }
```
공통 초기화 코드를 비공개 팩토리 메서드로 추출해 테스트 코드를 짧게 하면서,   
동시에 테스트 진행 상황에 대한 전체 맥락을 유지할 수 있다.  
또한 테스트가 서로 결합되지 않는다.

### 3.4 단위 테스트 명명법  
테스트에 표현력이 있는 이릅을 붙이는 것이 좋다.  
[테스트 대상 메서드] _ [시나리오] _ [예상결과]  
- 테스트 대상 메서드 : 테스트 중인 메서드의 이름  
- 시나리오 : 테스트 조건 
- 예상결과 : 테스트에서 기대하는 것

**단위 테스트 명명 지침**
- 엄격한 명명 정책을 따르지 않는다.
- 문제 도메인에 익숙한 비개발자들에게 시나리오를 설명하는 것처럼 테스트 이름을 짓자
- 단어를 _ 표시로 구분한다.

예) 과거 배송일이 유효하지 않다는 테스트  
- not bad  
public void Deliver_with_invalid_date_should_be_considered_invalid()  
- better  
public void Deliver_with_a_past_date_is_invalid()

### 3.5 매개변후화된 테스트 리팩터링 하기
보통 테스트 하나로는 동작 단위를 완전하게 설명하게 충분하지 않다.  
일반적으로 여러 구성 요소를 포함하며  
대부분 단위 테스트 프레임워크는 매개변수화된 테스트를 사용해 유사한 테스트를 묶을 수 있는 기능을 제공한다.  

매개변수화된 테스트를 사용하면 테스트 코드의 양을 크게 줄일 수 있지만, 비용이 발생한다.  
그 비용은 테스트 메서드가 나타내는 사실을 파악하기가 어려워진다. 매개변수가 많을수록 어렵다.  

### 3.6 검증문 라이브러리를 사용한 테스트 가독성 향상
ex) Fluent Assertions