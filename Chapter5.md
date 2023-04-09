# 5장. 목과 테스트 취약성

## 1. 목과 스텁 구분

- 목 : 테스트 대상 시스템과 그 협력자 사이의 상호 작용을 검사할 수 있는 테스트 대역
- 스텁 : 테스트 대역의 또 다른 유형

### 테스트 대역 유형

- 테스트 대역 : 모든 유형의 비운영용 가짜 의존성을 설명하는 포괄적인 용어
    - 테스트를 편리하게 하는 것
    - 목 : 목, 스파이
        - 외부로 나가는 상호 작용을 모방하고 검사하는데 도움
        - 상호 작용은 SUT가 상태를 변경하기 위한 의존성을 호출
    - 스텁 : 스텁, 더미, 페이크
        - 내부로 들어오는 상호 작용을 모방하는데 도움
        - SUT가 입력 데이터를 얻기 위한 의존성을 호출

<img width="453" alt="스크린샷 2023-04-06 오후 8 13 00" src="https://user-images.githubusercontent.com/7659412/230782955-710a278b-21f3-4b3c-8f93-2cecb1e91ce5.png">

- 이메일 발송은 SMTP 서버에 사이드 이펙트를 초래하는 상호 작용 - 목
- 데이터베이스에서 데이터를 검색하는 것은 내부로 들어오는 상호 작용 - 스텁

**테스트 대역 5가지의 차이점은 미미한 구현 세부 사항**

- 스파이 : 수동으로 작성하는 목
- 목 : 목 프레임워크의 도움을 받아 생성, 직접 작성한 목
- 더미 : 널 값이나 가짜 문자열 같이 단순하고 하드코딩 된 값
- 스텁 : 시나리오 마다 다른 값을 반환하게 끔 구성할 수 있도록 필요한 것을 다 갖춘 완전한 의존성
- 페이크 : 대다수의 목적에 부합하는 스텁, 보통 아직 존재하지 않는 의존성을 대체하고자 구현

### 도구로서의 목과 테스트 대역으로서의 목

```java
@Test
public void sending_a_greetings_email() {
	var mock = Mockito.mock(IEmailGateWay.class);
	var sut = new Controller(mock);

	sut.greetUser("user@gmail.com");

	Mockito.verify(mock, Mockito.times(1))
					.sendGreetingEmail("user@gmail.com");
}
```

- Mock 클래스는 도구로서의 목
- mock 인스턴스는 테스트 대역으로서의 목, 상호 작용을 모방하고 검증

```java
@Test
public void creating_a_report() {
	var stub = Mockito.mock(IDatabase.class);
	Mockito.when(stub.getNumberOfUsers()).thenReturn(10);
	var sut = new Controller(stub);

	Report report = sut.createReport();

	Assertions.assertEquals(10, report.numberOfUsers);
}
```

- 스텁은 내부로 들어오는 상호작용을 모방

- 목은 SUT에서 관련 의존성으로 나가는 상호 작용을 모방하고 검사하는 반면, 스텁은 내부로 들어오는 상호 작용만 모방하고 검사하지 않는다.
- 스텁은 SUT가 출력을 생성하도록 입력을 제공한다
- 스텁과의 상호 작용을 검증하는 것은 취약한 테스트를 야기하는 일반적인 안티 패턴
- 테스트에서 거짓 양성을 피하고 리팩터링 내성을 향상시키는 방법은 구현 세부 사항이 아니라 최종 결과를 검증하는 것

```java
@Test
public void createing_a_report() {
	var stub = Mockito.mock(IDatabase.class);
	Mockito.when(stub.getNumberOfUsers()).thenReturn(10);
	var sut = new Controller(stub);

	Report report = sut.createReport();

	Assertions.assertEquals(10, report.numberOfUsers);
	Mockito.verify(stub, Mockito.times(1))
					.getNumberOfUsers());
}
```

- 최종 결과가 아닌 사항을 검증하는 관행을 과잉 명세라고 부름

### 목과 스텁 함께 쓰기

```java
@Test
public void purchase_fails_when_not_enough_inventory() {
	var storeMock = Mockito.mock(IStore.class);
	Mockito.when(storeMock.hasEnoughInventory(Product.Shampoo, 5))
					.thenReturn(false);
	var sut = new Customer();

	boolean success = sut.purchase(storeMock, Product.Shampoo, 5);

	Assertions.assertFalse(success);
	Mockito.verify(storeMock, Mockito.never())
					.removeInventory(Product.Shampoo, 5);
}
```

### 목과 스텁은 명령과 조회에 어떻게 관련돼 있는가?

**명령 조회 분리(CQS) 원칙**

- 모든 메서드는 명령이거나 조회여야 하며, 이 둘을 혼용해서는 안 된다.
- 명령 : 사이드 이펙트를 일으키고 어떤 값도 반환하지 않는 메서드
- 조회 : 사이드 이펙트가 없고 값을 반환하는 메서드
- ‘명령’과 ‘조회’를 분리 했기 때문에 성능 최적화 하기 쉽고 코드 가독성도 좋은 장점

항상 CQS 원칙을 따를 수 있는 것은 아니다. 그래도 가능한 CQS 원칙을 따르는 것이 좋다.

- 목 : 명령을 대체하는 테스트 대역
- 스텁 : 조회를 대체하는 테스트 대역

## 2. 식별할 수 있는 동작과 구현 세부 사항 정의

테스트에 거짓 양성이 있는 주요 이유는 코드의 구현 세부 사항과 결합돼 있기 때문이다.

강결합을 피하는 방법은 코드가 생성하는 최종 결과를 검증하고 구현 세부 사항과 테스트를 가능한 한 떨어뜨리는 것이다.

즉, 테스트는 ‘어떻게’ 가 아니라 ‘무엇’에 중점을 둬야 한다.

**모든 제품 코드는 2차원으로 분류**

- 공개 API 또는 비공개 API
- 식별할 수 있는 동작 또는 구현 세부 사항

각 차원의 범주는 겹치지 않는다.

코드베이스의 공개 API와 비공개 API를 구별할 수 있는 방법은 private, public 같은 접근제어자

식별할 수 있는 동작과 구현 세부 사항을 구별하는데 미묘한 차이가 있다.

**식별할 수 있는 동작**

- 클라이언트가 목표를 달성하는 데 도움이 되는 연산을 노출
- 클라이언트가 목표를 달성하는 데 도움이 되는 상태를 노출

**구현 세부 사항**은 이 두가지 중 아무것도 하지 않는다.

이상적으로 공개 API와 식별할 수 있는 동작이 일치, 비공개 API와 구현 세부 사항이 일치

잘못된 설계로 공개 API가 식별할 수 있는 동작의 범위를 넘어 구현 세부 사항을 노출할 수도 있음

```java
// 구현 세부 사항 노출(연산)
public class User
{
	private String name;

	public void setName(String name) {
		this.name = name;
	}

	public String normalizeName(String name) {
		String result = Objects.requireNonNullElse(name, "").trim();
		if(result.length() > 50) {
			return result.substring(0, 50);
		}
		return result;
	}
}

public class UserController
{
	public void renameUser(int userId, String newName) {
		User user = getUserFromDatabase(userId);

		String normalizeName = user.normalizeName(newName);
		user.setName(normalizeName);

		saveUserToDatabase(user);
	}
}
```

```java
// 구현 세부 사항 노출 개선(연산)
public class User
{
	private String name;

	public void setName(String name) {
		this.name = normalizeName(name);
	}

	private String normalizeName(String name) {
		String result = Objects.requireNonNullElse(name, "").trim();
		if(result.length() > 50) {
			return result.substring(0, 50);
		}
		return result;
	}
}

public class UserController
{
	public void renameUser(int userId, String newName) {
		User user = getUserFromDatabase(userId);
		user.setName(normalizeName);
		saveUserToDatabase(user);
	}
}
```

클래스가 구현 세부 사항을 유출 하는지 판단하는 데 도움이 되는 유용한 규칙

- 단일한 목표를 달성하고자 클래스에서 호출해야 하는 연산의 수가 1보다 크면 해당 클래스에서 구현 세부 사항을 유출할 가능성이 있다.

### 잘 설계된 API와 캡슐화

구현 세부 사항을 노출하면 불변성 위반을 가져옴.

캡슐화를 유지해야 소프트웨어 프로젝트의 지속적인 성장을 가져올 수 있다.

- 구현 세부 사항을 숨기면 클라이언트의 사이에서 클래스 내부를 가릴 수 있기 때문에 내부를 손상시킬 위험이 적다.
- 데이터와 연산을 결합하면 해당 연산이 클래스의 불변성을 위반하지 않도록 할 수 있다.

좋은 단위 테스트와 잘 설계된 API 사이에는 본질적인 관계가 있다.

모든 구현 세부 사항을 비공개로 하면 테스트가 식별할 수 있는 동작을 검증하는 것 외에는 다른 선택지가 없어 이로 인해 리팩터링 내성도 자동으로 좋아진다.

연산과 상태를 최소한으로 노출해야 한다.

|  | 식별할 수 있는 동작 | 구현 세부 사항 |
| --- | --- | --- |
| 공개 | 좋음 | 나쁨 |
| 비공개 | 해당 없음 | 좋음 |

## 3. 목과 테스트 취약성 간의 관계 이해

### 육각형 아키텍처

<img width="343" alt="스크린샷 2023-04-06 오후 9 00 33" src="https://user-images.githubusercontent.com/7659412/230782971-d9a48f1f-ecf4-4aaa-89bd-67e676fa5afb.png">

- 도메인와 어플리케이션 서비스라는 두 계층으로 구성
- 도메인 계층 : 어플리케이션의 중심부, 비즈니스 로직 포함
- 어플리케이션 서비스 계층 : 도메인 위에 있으며 외부 환경과의 통신 조정, 도메인 클래스와 프로세스 외부 의존성 간의 작업을 조정

<img width="453" alt="스크린샷 2023-04-06 오후 9 02 10" src="https://user-images.githubusercontent.com/7659412/230782975-9e341984-0d40-49cb-a927-5280abba21f0.png">

다른 어플리케이션과 상호 작용

- 도메인 계층와 어플리케이션 서비스 계층 간의 관심사 분리
    - 도메인 계층은 어플리케이션의 가장 중요한 부분인 비즈니스 로직에 대해서만 책임
        - 애플리케이션의 도메인 지식 모음
    - 어플리케이션 서비스 계층은 어떤 비즈니스 로직도 있으면 안된다.
        - 요청이 들어오면 도메인 클래스의 연산으로 변환한 다음 결과를 저장하거나 호출자에게 다시 반환해서 도메인 계층으로 변환하는 책임
        - 일련의 비즈니스 유즈케이스
- 애플리케이션 내부 통신
    - 어플리케이션 서비스 계층에서 도메인 계층으로 흐르는 단방향 의존성 흐름
    - 도메인 계층 내부 클래스는 도메인 계층 내부 클래스끼리 서로 의존, 어플리케이션 서비스 계층에 의존하지 않음
    - 도메인 계층은 외부 환경에서 완전히 격리
- 어플리케이션 간의 통신
    - 외부 어플리케이션은 어플리케이션 서비스 계층에 있는 공통 인터페이스를 통해 해당 어플리케이션에 연결
    - 도메인 계층에 직접 접근할 수 없다.

어플리케이션의 각 계층은 식별할 수 있는 동작을 나타내며 해당 구현 세부 사항을 포함

잘 설계된 API의 원칙에는 프랙탈 특성이 있다.(자기유사성, 반복)

테스트의 달성하는 목표는 갖지만 서로 다른 수준에서 동작을 검증

- 어플리케이션 서비스를 다루는 테스트는 해당 서비스가 외부 클라이언트에게 매우 중요하고 큰 목표를 어떻게 이루는지 확인
- 도메인 클래스 테스트는 그 큰 목표의 하위 목표를 검증

식별할 수 있는 동작은 바깥 계층에서 안쪽으로 흐른다.

외부 클라이언트에게 중요한 목표는 개별 도메인 클래스에서 달성한 하위 목표로 변환된다.

### 내부 통신과 외부 통신의 차이점

시스템 내부 통신과 시스템 간 통신

- 시스템 내부 통신 : 애플리케이션 내 클래스 간의 통신, 구현 세부 사항
- 시스템 간 통신 : 애플리케이션이 다른 애플리케이션과 통신, 식별할 수 있는 동작

시스템 성장의 주요 원칙 중 하나로 하위 호환성을 지키는 것

시스템 간 통신을 테스트할 때 목을 사용하면 시스템과 외부 애플리케이션 간의 통신 패턴을 확인할 때 좋다.

시스템 내 클래스 간 통신을 검증하는 데 목을 사용하면 테스트가 구현 세부 사항과 결합 되며, 그에 따라 리팩터링 내성 지표가 미흡해진다.

### 시스템 내부 통신과 시스템 간 통신의 예

```
[ 비즈니스 유즈케이스 ]
고객이 상점에서 제품을 구매하려고 한다.
매장 내 제품 수량이 충분하면
- 재고가 상점에서 줄어든다.
- 고객에게 이메일로 영수증을 발송한다.
- 확인 내역을 반환한다.
```

```java
public class CustomerController {
	public boolean purchase(int customerId, int productId, int quantity) {
		Customer customer = customerRepository.findById(customerId);
		Product product = productRepository.findById(productId);

		boolean isSuccess = customer.purchase(mainStore, product, quantity);

		if(isSuccess) {
			emailGateway.sendReceipt(customer.email, product.name, quantity);
		}

		return isSuccess;
	}
}
```

CustomerController 클래스는 도메인 클래스(Customer, Product, Store)와 외부 어플리케이션(EmailGateway) 간의 작업을 조정하는 애플리케이션 서비스

- 시스템 간 통신 : CustomerController 어플리케이션 서비스와 서드파티 어플리케이션, 이메일 게이트웨이 간의 통신
- 시스템 내부 통신  Customer, Store 도메인 클래스 간의 통신

```java
// 시스템 간 통신에서 목 활용
@Test
public void successful_purchase() {
	var mock = Mockito.mock(IEmailGateWay.class);
	var sut = new CustomerController(mock);

	boolean isSuccess = sut.purchase(1, 2, 5);

	Assertions.assertTrue(isSuccess);
	Mockito.verify(mock, Mockito.times(1))
					.sendReceipt("customer@email.com", "Shampoo", 5);
}
```

```java
// 시스템 내부 통신에서 목 활용(취약한 테스트)
@Test
public void purchase_fails_when_not_enough_inventory() {
	var storeMock = Mockito.mock(IStore.class);
	Mockito.when(storeMock.hasEnoughInventory(Product.Shampoo, 5))
					.thenReturn(true);
	var sut = new Customer();

	boolean success = sut.purchase(storeMock, Product.Shampoo, 5);

	Assertions.assertTrue(success);
	Mockito.verify(storeMock, Mockito.times(1))
					.removeInventory(Product.Shampoo, 5);
}
```

# 4. 단위 테스트의 고전파와 런던파 재고

|  | 격리 주체 | 단위의 크기 | 테스트 대역 사용 대상 |
| --- | --- | --- | --- |
| 런던파 | 단위 | 단일 클래스 | 불변 의존성 외 모든 의존성 |
| 고전파 | 단위 테스트 | 단일 클래스 또는 클래스 세트 | 공유 의존성 |

런던파는 불변 의존성 외 모든 의존성에 목 사용을 권장, 시스템 내 통신과 시스템 간 통신을 구분하지 않는다.

목을 무분별하게 사용하면 종종 구현 세부 사항에 결합돼 테스트에 리팩터링 내성이 없어진다.

고전파는 테스트 간에 공유하는 의존성만 목으로 교체하자고 하므로 훨씬 유리하다.

그러나, 고전파도 시스템 간 통신에 대해서는 목 사용을 장려하기 때문에 이상적이지 않다.

### 모든 프로세스 외부 의존성을 목으로 해야 하는 것은 아니다.

- 공유 의존성 : 테스트 간에 공유하는 의존성(제품 코드가 아님)
- 프로세스 외부 의존성 : 프로그램의 실행 프로세스 외에 다른 프로세스를 점유하는 의존성
- 비공개 의존성 : 공유하지 않는 모든 의존성

프로세스 외부 의존성이 애플리케이션을 통해서만 접근할 수 있다면, 이러한 의존성과의 통신은 식별할 수 있는 동작이 아니다.

외부에서 관찰할 수 없는 프로세스 외부 의존성은 어플리케이션의 일부로 작용한다.

이런 프로세스 외부 의존성에 목을 사용하면 깨지기 쉬운 테스트로 이어진다.

### 목을 사용한 동작 검증

목은 어플리케이션의 경계를 넘나드는 상호 작용을 검증할 때와 이러한 상호 작용의 사이트 이펙트가 외부 환경에서 보일 때만 동작과 관련이 있다.
