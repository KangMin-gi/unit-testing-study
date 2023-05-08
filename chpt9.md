# 목 처리에 대한 모범 사례
- (8장) 목은 unmanaged 의존성에만 적용하자
- 목의 가치를 극대화할 수 있는 방법을 알아보자

## 목의 가치를 극대화하기

```java
public class UserController {  
  
   private final Database db;  
   private final EventDispatcher eventDispatcher;  
  
   public UserController(Database db, IMessageBus messageBus, DomainLogger domainLogger) {  
      this.db = db;  
      this.eventDispatcher = new EventDispatch(messageBus, domainLogger);  
   }  
  
   public String changeEmail(int userId, String newEmail) {  
      Object[] userData = db.getUserById(userId);  
      User user = UserFactory.create(userData);  
  
      String error = user.canChangeEmail();  
      if (error != null) {  
         return error;  
      }  
  
      Object[] companyData = db.getCompany();  
      Company company = CompanyFactory.create(companyData);  
  
      user.changeEmail(newEmail, company);  
  
      db.saveCompany(company);  
      db.saveUser(user);  
      eventDispatcher.dispatch(user.getDomainEvents());  
  
      return "OK";  
   }  
  
}
```

```java
public class EventDispatcher {  
   private final MessageBus messageBus;  
   private final DomainLogger domainLogger;  
  
   public EventDispatcher(MessageBus messageBus, DomainLogger domainLogger) {  
      this.messageBus = messageBus;  
      this.domainLogger = domainLogger;  
   }  
  
   public void dispatch(List<DomainEvent> events) {  
      for (DomainEvent ev : events) {  
         dispatch(ev);  
      }  
   }  
  
   private void dispatch(DomainEvent event) {  
      switch (event) {  
         case EmailChangedEvent:  
            messageBus.sendEmailChangedMessage(event.getUserId, event.getNewEmail());  
            break;         case UserTypeChangedEvent:  
            domainLogger.userTypeHasChanged(event.getUserId, event.getOldType, event.getNewType);  
            break;      }  
   }  
}
```

```java
@Test  
void changing_email_from_corporate_to_non_corporate() {  
   //given  
   var db = new Database(ConnectionString);  
   com.example.crm.version6.User user = createUser("user@mycorp.com", User.UserType.EMPLOYEE, db);  
   createCompany("mycorp.com", 1, db);  
  
   var messageBusMock = Mockito.mock(MessageBus.class);  
   var loggerMock = Mockito.mock(DomainLogger.class);  
   var sut = new UserController(db, messageBusMock, loggerMock);  
  
   //when  
   String result = sut.changeEmail(user.getUserId(), "new@gmail.com");  
   //then  
   Assertions.assertThat(result).isEqualTo("OK");  
  
   //User 상태 검증  
   Object[] userData = db.getUserById(user.getUserId());  
   User userFromDb = UserFactory.create(userData);  
   assertThat(userFromDb.getEmail()).isEqualTo("new@gmail.com");  
   assertThat(userFromDb.getType()).isEqualTo(User.UserType.CUSTOMER);  
  
   //Company 상태 검증  
   Object[] companyData = db.getCompany();  
   Company companyFromDb = CompanyFactory.create(companyData);  
   assertThat(companyFromDb.getNumberOfEmployees()).isEqualTo(0);  
  
   //목 상호 작용 검증  
   Mockito.verify(messageBusMock, Mockito.times(1)).sendEmailChangedMessage(user.getUserId(), "new@gmail.com");  
   Mockito.verify(loggerMock, Mockito.times(1)).userTypeHasChanged(user.getUserId(), UserType.EMPLOYEE, UserType.CUSTOMER);  
}

```

- 위 통합테스트는 unmanaged, managed 의존성을 가진다.
- 문제점
	- MessageBus 인터페이스가 시스템 끝에 있지 않다.

### 시스템 끝에서 상호작용 검증하기
- 목을 사용할 때는 시스템 끝에서 Unmanaged 의존성과의 상호작용을 검증하자

```java
public interface MessageBusInterface {  
   void sendEmailChangedMessage(int userId, String newEmail);  
}

public class MessageBus implements MessageBusInterface {  
   private final Bus bus;  
  
   @Override  
   public void sendEmailChangedMessage(int userId, String newEmail) {  
      bus.send("Type: USER EMAIL CHANGED; " +  
         "ID: " + userId +  
         "NewEmail: " + newEmail);  
   }  
}

public interface BusInterface {  
   void send(String message);  
}
```

- BusInterface
	- message bus sdk library 위에 있는 래퍼
- MessageBusInterface
	- BusInterface 위에 있는 래퍼
	- domain 과 관련된 메시지 정의

![](attachments/스크린샷%202023-05-08%20오전%208.23.27.png)

- 육각형 아키텍처 관점에서 보면, BusInterface 는 컨트롤러 - 메시지버스 사이의 마지막 고리이고, MessageBusInterface 는 중간 고리이다. 
- MessageBusInterface 대신 BusInterface 를 Mocking 하면 회귀 방지를 극대화할 수 있다. (통합 테스트가 거치는 클래스의 수가 증가하기 때문)
- BusInterface 를 대상으로한 통합테스트

```java
@Test  
void changing_email_from_corporate_to_non_corporate() {  
   //given  
   //중략
  
   var busMock = Mockito.mock(BusInterface.class);  
   var messageBusMock = new MessageBus(busMock); // 인터페이스 대신 구체 클래스 사용  
   var loggerMock = Mockito.mock(DomainLogger.class);  
   var sut = new UserController(db, messageBusMock, loggerMock);  
  
   //... 중략
  
   //목 상호 작용 검증  
   Mockito.verify(busMock, Mockito.times(1)).send("Type: USER EMAIL CHANGED; " +  
      "ID: " + user.getUserId() +  
      "NewEmail: " + "new@gmail.com");  
}
```

- 이전 테스트코드와 달리 `MessageBusInterface` 는 구현이 하나뿐인 인터페이스이면서 Mocking 하지 않기 때문에 이제는 이 인터페이스를 삭제하고 `MessageBus` 구체클래스로 대체할 수 있다.
	- Q. 테스트 코드 상에서만 MessageBusInterface 를 제거하라는 이야기인가?
- 이전 테스트코드와 달리 외부시스템에 전송되는 실제 텍스트를 검증
	- 실제 텍스트가 외부에서 식별 가능한 유일한 사이드 이펙트. 이전테스트에서 검증한 `sendEmailChangedMessage()` 는 구현 세부사항에 해당.
	- 시스템 끝에서 상호작용을 확인하면 회귀방지 뿐 아니라 리팩터링 내성도 향상된다!

### 목을 스파이로 대체하기 
- 스파이
	- 직접 작성
- 목
	- Mock 프레임워크 도움을 받아 생성
- 시스템 끝에 있는 클래스의 경우 스파이가 목보다 낫다.
	- 검증 단계에서 코드를 재사용하기 때문에 테스트 크기를 줄이고 가독성을 향상시킴

```java
class BusSpy implements Bus {  
   private List<String> sendMessages;  
  
   @Override  
   public void send(String message) {  
      //전송된 모든 메시지를 로컬에 저장  
      sendMessages.add(message);  
   }  
  
   public BusSpy shoudSendNumberOfMessages(int number) {  
      Assertions.assertThat(sendMessages.size()).isEqualTo(number);  
      return this;   }  
  
   public BusSpy withEmailChangedMessage(int userId, String newEmail) {  
      String message = "Type: USER EMAIL CHANGED; " +  
         "ID: " + userId +  
         "NewEmail: " + newEmail;  
      Assertions.assertThat(sendMessages).contains(message);  
      return this;   }  
}
```

```java
@Test  
void changing_email_from_corporate_to_non_corporate() {  
   //given  
   //...
  
   var busSpy = new BusSpy();  
   var messageBusMock = new MessageBus(busSpy);  
   var loggerMock = Mockito.mock(DomainLogger.class);  
   var sut = new UserController(db, messageBusMock, loggerMock);  
  
   // ...


   busSpy.shoudSendNumberOfMessages(1)  
      .withEmailChangedMessage(user.getUserId(), "new@gmail.com");  
}
```

- BusSpy 덕분에 메시지 버스와의 상호작용 검증이 간결해지고 표현력이 생김
- BusSpy 사용 - MessageBusInterface Mocking 차이점
	- 형태는 유사
	- MessageBusInterface Mocking 방식은 검증문을 작성할 때 제품코드를 신뢰할 수 밖에 없음
	- 반면 BusSpy 방식은 제품코드를 믿지 않고 한번 더 검증. 덕분에 메시지구조가 바뀐다면 BusSpy 방식에서는 이를 감지할 수 있음.

### DomainLoggerInterface
- 메시지버스와 달리 로거의 경우 로그메시지 구조를 유지하는 것이 그다지 중요하지 않음. 따라서 굳이 시스템 끝(LoggerInterface)을 Mocking 하기보다는 중간에 위치한 DomainLoggerInterface 를 Mocking 하는 것으로 충분

## 목 처리에 대한 기타 모범 사례
### 목은 통합 테스트만을 위한 것
- 목은 통합 테스트만을 위한 것이며 단위 테스트에서는 목을 사용하면 안 됨
- 도메인 모델 테스트 -> 단위테스트 -> Mock 사용 (X)
- 컨트롤러 테스트 -> 통합테스트 -> Mock 사용 (O)

### 테스트당 목이 하나일 필요는 없음
- 테스트의 '단위' 는 코드 단위가 아니라 동작 단위.
- 동작 단위를 검증하는데 필요한 Mock 수는 테스트 당 하나일 필요가 없음. unmanaged 의존성 숫자만큼 Mock 을 두면 됨

### 호출횟수 검증하기
- 예상하는 호출이 있는지 뿐만 아니라 예상치 못한 호출이 없는지 또한 검증해야 하므로 호출횟수를 정확히 검증하는 것이 좋다


### Only mock types that you own
- 서드파티 라이브러리를 사용할 때는 항상 어댑터를 작성하고, 해당 어댑터를 Mock 으로 처리하자
- 어댑터 정의했을 때 장점
	- 서드파티 코드가 변경되었을 때 파급효과를 어댑터 클래스로 제한할 수 있음(anti-corruption layer)
	- 라이브러리 복잡성 추상화
	- 라이브러리에서 필요한 기능만 노출 가능
	- 프로젝트 도메인 언어 활용 가능
- e.g. CRM 예제에서 BusInterface


