# 9장. 목 처리에 대한 모범 사례

# 9.1 목의 가치를 극대화하기

```java
public class UserController {
	private final Database _database;
	private final EventDispatcher _eventDispatcher;

	public UserController(
		Database database,
		IMessageBus messageBus,
		IDomainLogger domainLogger) {
		_database = database;
		_eventDispatcher = new EventDispatcher(messageBus, domainLogger);
	}

	public String changeEmail(int userId, String newEmail) {
		Object[] userData = _database.getUserById(userId);
		User user = UserFactory.create(userData);
	
		String error = user.canChangeEmail();
		if(error != null) {
			return error;
		}

		Object[] companyData = _database.getCompany();
		Company company = CompanyFactory.create(companyData);

		user.changeEmail(newEmail, company);

		_database.saveCompany(company);
		_database.saveUser(user);
		_eventDispatcher.dispatch(user.getDomainEvents());

		return "OK";
	}
}
```

- 비관리 의존성에만 목을 사용하게끔 제한하는 것은 목의 가치를 극대화하기 위한 첫 번째 단계
    - 진단 로깅은 없고 지원 로깅(IDomainLogger)만 남아 있다.
    - 도메인 모델에서 생성된 도메인 이벤트를 비관리 의존성에 대한 호출로 변환하는 EventDispatcher 클래스를 도입했다.

```java
public class EventDispatcher {
	private final IMessageBus _messageBus;
	private final IDomainLogger _domainLogger;

	public EventDispatcher(
		IMessageBus messageBus,
		IDomainLogger domainLogger) {
		_messageBus = messageBus;
		_domainLogger = domainLogger;
	}

	public void dispatch(List<IDomainEvent> events) {
		for(IDomainEvent ev : events) {
			dispatch(ev);
		}
	}

	private void dispatch(IDomainEvent ev) {
	  switch (ev) {
	    case EmailChangedEvent emailChangedEvent ->
	      _messageBus.sendEmailChangedMessage(emailChangedEvent.getUserId(), emailChangedEvent.getNewEmail());
      case UserTypeChangedEvent userTypeChangedEvent ->
	      _domainLogger.userTypeHasChanged(userTypeChangedEvent.getUserId(), userTypeChangedEvent.getOldType(), userTypeChangedEvent.getNewType());
      default ->
				throw new IllegalArgumentException("지원하는 이벤트 타입이 아닙니다.");
    }
  }
}
```

```java
@Test
public void changing_email_from_corporate_to_non_corporate() {
	Database db = new Database(connectionString);
	User user = createUser("user@mycorp.com", UserType.Employee, db);
	createCompany("mycorp.com", 1, db);

	IMessageBus messageBusMock = Mockito.mock(IMessageBus.class);
  IDomainLogger loggerMock = Mockito.mock(IDomainLogger.class);
	UserController sut = new UserController(db, messageBusMock, loggerMock);

	String result = sut.changeEmail(user.getUserId(), "new@gmail.com");

	Assertions.assertEquals("OK", result);

	Object[] userData = db.getUserById(user.getUserId());
	User userFromDb = UserFactory.create(userData);
	Assertions.assertEquals("new@gmail.com", userFromDb.getEmail());
  Assertions.assertEquals(UserType.Customer, userFromDb.getType());

	Object[] companyData = db.getCompany();
	Company companyFromDb = CompanyFactory.create(companyData);
	Assertions.assertEquals(0, companyFromDb.getNumberOfEmployees());

	Mockito.verify(messageBusMock, Mockito.times(1)).sendEmailChangedMessage(user.getUserId(), "new@gmail.com");
  Mockito.verify(loggerMock, Mockito.times(1)).userTypeHasChanged(user.getUserId(), UserType.Employee, UserType.Customer);
}
```

- 통합 테스트는 모든 프로세스 외부 의존성을 거친다.
- 비관리 의존성인 IMessageBus와 IDomainLogger를 목으로 처리

### 시스템 끝에서 상호 작용 검증하기

```java
public interface IMessageBus {
	void sendEmailChangedMessage(int userId, String newEmail);
}

public class MessageBus : IMessageBus {
	private final IBus _bus;

	public void sendEmailChangedMessage(int userId, String newEmail) {
		_bus.send("Type : USER EMAIL CHANGED; Id : " + userId + "; NewEmail : " + newEmail);
	}
}

public interface IBus {
	void send(String message);
}
```

- 목을 사용할 때 항상 시스템 끝에서 비관리 의존성과의 상호 작용을 검증해야 한다.
- messageBusMock의 문제점은 IMessageBus 인터페이스가 시스템 끝에 있지 않다는 것이다.
    - IBus는 메시지 버스 SDK 라이브러리 위에 있는 래퍼
        - 연결 자격 증명과 같은 기술 세부 사항을 캡슐화하고 임의의 텍스트 메시지를 메시지 버스로 보내는 인터페이스
    - IMessageBus는 IBus 위에 있는 래퍼
        - 도메인과 관련된 메시지를 정의
        - 모든 메시지를 한 곳에 보관하고 애플리케이션에서 재사용
- 외부 라이브러리의 복잡성을 숨기는 것(IBus)과 모든 애플리케이션 메시지를 한 곳에 두는 책임(IMessageBus)은 분리하는 것이 좋다.
- IBus는 컨트롤러와 메시지 버스 사이의 마지막에 있고 IMessageBus는 중간에 있다.
- IBus를 목으로 처리하면 회귀 방지를 극대화할 수 있다.
    - 회귀 방지는 테스트 중에 실행되는 코드 양에 대한 함수
    - 비관리 의존성과 통신하는 마지막 타입을 목으로 처리하면 통합 테스트가 거치는 클래스의 수가 증가하므로 보호가 향상된다.

<img width="580" alt="스크린샷 2023-05-07 오후 2 31 33" src="https://user-images.githubusercontent.com/7659412/236663994-9471ff05-0e00-4bb4-af9f-1621aa0a2764.png">

```java
@Test
public void changing_email_from_corporate_to_non_corporate() {
	IBus busMock = Mockito.mock(IBus.class);
  IDomainLogger loggerMock = Mockito.mock(IDomainLogger.class);
  MessageBus messageBus = new MessageBus(busMock);
  UserController sut = new UserController(db, messageBus, loggerMock);

	/* ... */

	Mockito.verify(busMock, Mockito.times(1)).send("Type : USER EMAIL CHANGED; Id : " + user.getUserId() + "; NewEmail : " + userFromDb.getEmail());
}
```

- IMessageBus 인터페이스는 구현이 하나이고 목으로 처리하지 않기 때문에 인터페이스를 삭제하고 MessageBus 구체클래스로 대체한다.
- 사용자 정의 클래스에 대한 호출을 검증하는 것과 외부 시스템에 전송한 실제 텍스트 사이에는 큰 차이가 있다.
    - 외부 시스템은 애플리케이션으로부터 텍스트 메시지를 수신하고 MessageBus와 같은 클래스를 호출하지 않는다.
    - 텍스트 메시지는 외부에서 식별할 수 있는 유일한 사이드 이펙트이므로 이러한 메시지를 생성하는 데 참여하는 클래스는 구현 세부 사항이다.
    - 시스템 끝에서 상호 작용을 확인하면 회귀 방지가 좋아질 뿐만 아니라 리팩터링 내성도 향상된다.
        - 테스트는 거짓 양성에 노출될 가능성이 낮아져 리팩터링을 하더라도 메시지 구조를 유지하는 한 해당 테스트는 빨간색으로 바뀌지 않는다.
        - 단위 테스트에 비해 통합 테스트와 엔드 투 엔드 테스트가 리팩터링 내성이 우수한 것처럼 동일하다.
        - 코드베이스와의 결합도가 낮기 때문에 낮은 수준의 리팩터링에도 영향을 많이 받지 않는다.
- 비관리 의존성에 대한 호출은 애플리케이션을 떠나기 전에 몇 단계를 거치는데 마지막 단계를 선택해야 한다.
    - 외부 시스템과의 하위 호환성을 보장하는 가장 좋은 방법이며 하위 호환성은 목을 통해 달성할 수 있는 목표다.

### 목을 스파이로 대체하기

- 스파이는 목과 같은 목적을 수행하는 테스트 대역
- 스파이는 수동으로 목을 작성하는 반면에 목은 목 프레임워크의 도움을 받아 생성
- 시스템 끝에 있는 클래스의 경우 스파이가 목보다 낫다.
- 스파이는 검증 단계에서 코드를 재사용해 테스트 크기를 줄이고 가독성을 향상시킨다.

```java
public interface IBus {
	void send(String message);
}

public class BusSpy implements IBus {
	private List<String> _sentMessages = new List<String>();

	public void send(String message) {
		_sentMessage.add(message);
	}

	public BusSpy shouldSendNumberOfMessages(int number) {
		Assert.Equal(number, _sentMessages.size());
		return this;
	}

	public BusSpy withEmailChangedMessage(int userId, String newEmail) {
		String message = "Type : USER EMAIL CHANGED; ID: " + userId + "; NewEmail: " + newEmail;
		Assert.contains(_sentMessages, x => x == message);
		return this;
	}
}
```

```java
@SpringBootTest
public void changing_email_from_corporate_to_non_corporate() {
	BusSpy busSpy = new BusSpy();
  MessageBus messageBus = new MessageBus(busSpy);
  IDomainLogger loggerMock = Mockito.mock(IDomainLogger.class);
  UserController sut = new UserController(db, messageBus, loggerMock);

	/* ... */

	busSpy.shouldSendNumberOfMessages(1).withEmailChangedMessage(user.getUserId(), "new@gmail.com");
}
```

- 플루언트 인터페이스로 메시지 버스와의 상호 작용을 검증하는 것은 간결하고 표현력이 생긴다.
    - 여러가지 검증을 묶을 수 있으므로 응집도가 높고 쉬운 영어 문장을 형성할 수 있다.
- IMessageBus를 목으로 처리했던 버전과 매우 유사하지만 BusSpy는 테스트 코드, MessageBus는 제품 코드에 속한다.
    - 테스트에서 검증문을 작성할 때 제품 코드에 의존하면 안되기 때문에 이 차이는 중요하다.

### IDomainLogger는 어떤가?

- 로거와 메시지 버스는 비관리 의존성이므로 두 버전 모두 하위 호환성을 유지해야 하지만 호환성의 정확도가 같을 필요는 없다.
    - 메시지 버스를 사용하면 외부 시스템이 이러한 변경에 어떻게 반응하는지 알 수 없으므로 메시지 구조를 변경하지 않는 것이 중요하다.
    - 텍스트 로그의 정확한 구조는 대상 독자에게 그다지 중요하지 않다.
        - 중요한 것은 로그가 있다는 사실과 로그에 있는 정보다.
- IDomainLogger는 목으로 처리해도 보호 수준은 충분하다.

# 9.2 목 처리에 대한 모범 사례

- 모범 사례
    - 비관리 의존성에만 목 적용하기
    - 시스템 끝에 있는 의존성에 대해 상호 작용 검증하기
    - 통합 테스트에서만 목을 사용하고 단위 테스트에서는 사용하지 않기
    - 항상 목 호출 수 확인하기
    - 보유 타입만 목으로 처리하기

### 목은 통합 테스트만을 위한 것

- 비즈니스 로직과 오케스트레이션을 분리 하는 것이 기본 원칙이다.
- 코드는 복잡하거나 프로세스 외부 의존성과 통신할 수 있지만 둘 다는 아니다.
- 자연스럽게 도메인 모델과 컨트롤러라는 고유 계층 두 개로 만들어진다.
- 도메인 모델에 대한 테스트는 단위 테스트이고, 컨트롤러를 다루는 테스트는 통합 테스트이다.
- 목은 비관리 의존성에만 해당하며 컨트롤러만 이러한 의존성을 처리하기 때문에 통합 테스트에서 컨트롤러를 테스트할 때만 목을 적용해야 한다.

### 테스트당 목이 하나일 필요는 없음

- 동작 단위를 검증하는 데 필요한 목의 수는 관계 없다.
- 통합 테스트에 사용할 목의 수를 통제할 수 없다.
- 목의 수는 운영에 참여하는 비관리 의존성 수에만 의존한다.

### 호출 횟수 검증하기

- 비관리 의존성과의 통신에 관해서는 다음 두 가지를 모두 확인해야 한다.
    - 예상하는 호출이 있는가?
    - 예상치 못한 호출은 없는가?
- 비관리 의존성과 하위 호환성을 지켜야 하는 데서 비롯되고 호환성은 양방향이어야 한다.
- 애플리케이션이 외부 시스템이 예상하는 메시지를 생략해서는 안되고 예상치 못한 메시지도 생성하면 안 된다.
- 테스트 대상 시스템이 메시지를 전송하는 지 확인하는 것 뿐만 아니라 정확히 한 번만 전송 되는지 확인해야 한다.

```java
	Mockito.verify(messageBusMock, Mockito.times(1)).sendEmailChangedMessage(user.getUserId(), "new@gmail.com");
```

- 목에 다른 호출이 없는지도 명시적으로 확인할 수 있다.

```java
Mockito.verifyNoMoreInteractions(messageBusMock);
```

### 보유 타입만 목으로 처리하기

- 서드파티 라이브러리 위에 항상 어댑터를 작성하고 기본 타입 대신 해당 어댑터를 목으로 처리해야 한다.
    - 서드파티 코드의 작동 방식에 대해 깊이 이해하지 못하는 경우가 많다.
    - 해당 코드가 이미 내장 인터페이스를 제공하더라도 목으로 처리한 동작이 실제 외부 라이브러리와 일치하는지 확인해야 하므로 해당 인터페이스를 목으로 처리하는 것은 위험하다.
    - 서드파티 코드의 기술 세부 사항까지는 꼭 필요하지 않기에 어댑터는 이를 추상화하고 애플리케이션 관점에서 라이브러리와의 관계를 정의한다.
- 어댑터는 코드와 외부 환경 사이의 손상 방지 계층으로 작동한다.
    - 기본 라이브러리의 복잡성을 추상화
    - 라이브러리에서 필요한 기능만 노출
    - 프로젝트 도메인 언어를 사용해 수행
- IBus 인터페이스가 해당 역할을 수행한다.
    - 고유의 래퍼를 두면 라이브러리를 업그레이드할 때 서드파티 코드가 변경 되어 전체 코드베이스에 파급 효과가 나타나는 것을 하나의 클래스로 제한할 수 있다.
- 프로세스 내부 의존성에는 적용되지 않는다.
    - 인메모리 의존성이나 관리 의존성을 추상화할 필요는 없다.
    - 날짜와 시간 API 등
    - ORM이 외부 애플리케이션에서 볼 수 없는 데이터베이스를 접근하면 ORM을 추상화할 필요는 없다.
