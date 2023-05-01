# 통합 테스트를 하는 이유

## 통합 테스트는 무엇인가

### 통합 테스트의 역할
- 단위 테스트 조건
	- 단일 동작 단위 검증
	- 빠르게 수행
	- 다른 테스트와 별도로 처리
- 위 세가지 조건 중 하나라도 충족하지 못하면 통합 테스트에 해당
- 프로세스 외부 의존성과 통합해 어떻게 작동하는지 검증
- 컨트롤러 사분면에 속하는 코드를 다룸

### 테스트 피라미드
- 통합 테스트 비용
	- 프로세스 외부 의존성 운영 필요
	- 협력자가 많아서 테스트가 비대해짐
- 통합 테스트 효용
	- 회귀 방지 우수
	- 프로덕션 코드와 결합도가 낮아서 리팩터링 내성 우수
- 비용, 효용이 모두 존재하므로 단위 테스트 - 통합 테스트 적절 비율을 유지하는 것이 중요

#### 이상적인 단위테스트 - 통합테스트 비율
- 단위 테스트로 비즈니스 시나리오의 예외 상황을 확인
- 통합테스트로 주요 흐름(Happy path), 단위테스트가 다루지 못하는 예외 상황(edge case) 확인
	- 비즈니스 시나리오당 1~2개 권장
- 단, 이 비율은 프로젝트의 복잡도에 따라 달라질 수 있음
	- e.g. 단순한 프로젝트 -> 단위테스트 : 통합테스트 = 1:1


![](attachments/스크린샷%202023-04-28%20오후%2011.25.10.png)


### 통합테스트와 빠른 실패
- 통합 테스트로 주요 흐름 확인 지침
	- 프로세스 외부 의존성과의 모든 상호작용을 확인하기 위해 가장 긴 주요흐름을 선택하라
- 단위테스트로 다룰 수 없는 edge case 확인 지침
	- 예외가 발생해서 전체 어플리케이션이 즉시 실패된다면 해당 예외 상황은 테스트할 필요없다.
	- e.g. CRM 예시에서 `canChange()` 로 예외가 발생하는 케이스는 통합테스트로 검증할 필요 없음
		- 통합테스트로 검증하지 않더라도, controller 를 거치지 않고 `user.changeEmail()` 을 호출하면 assert 문에 의해 어플리케이션이 crash 되기 때문
		- Q. 잘 이해가 되지 않는다. 왜 검증하지 않아도 된다는 거지?
		- 이 예외케이스는 통합테스트 보다는 User 의 단위테스트로 검증하는 것이 적절 (Fast Fail Principle)

```java
// User
public void changeEmail(String newEmail, Company company) {  
   Assert.isNull(canChangeEmail(), "");

    //중략
}
```

```java
//Controller
public String changeEmail(int userId, String newEmail) {
    // 중략
  		String error = user.canChangeEmail();
		if (error != null) {
			return error;
		}

    //중략
}
```

#### Fast Fail principle
- 예기치 않은 오류가 발생하자마자 진행 중이던 연산을 중단하는 것
- 어플리케이션의 안정성을 높인다.
	- 피드백 루프 단축. 즉, 버그를 빨리 발견할 수 있음
	- 지속성 상태 보호. 손상된 상태가 확산되는 것을 막을 수 있음. e.g DB 로  버그가 침투되지 않게 함
- e.g. 예외 던지기. 프로그램 흐름을 중단하고 로그를 남긴 후 작업을 종료 혹은 재시작
- e.g. precondition (assert)
	- 설정파일 데이터 읽을 때
	- Q. 실무에서 자주 쓰시는지 궁금하다
		- 잘 안쓰시고 Throw 를 활용하시는 편

## 어떤 프로세스 외부 의존성을 직접 테스트 해야 하는가 (혹은 어떤 외부 의존성을 mocking 해도 되는가)

### 프로세스 외부 의존성의 두 가지 유형
#### (1) Managed 의존성
- 어플리케이션을 통해서만 접근 가능
- 외부 환경에서 보이지 않음
- e.g. 데이터베이스
- 이것과의 통신은 구현 세부 사항에 해당
- 실제 인스턴스를 사용해라
#### (2) Unmanaged 의존성
- 해당 의존성과의 상호 작용(사이드이펙트)을 외부에서 볼 수 있음
- e.g. SMTP 서버, 메시지 버스
- 이것과의 통신은 식별할 수 있는 동작
- Mocking 해라

![](attachments/스크린샷%202023-04-28%20오후%2011.44.58.png)


### Managed 이면서 Unmanaged 인 외부 의존성 다루기
- e.g. 다른 어플리케이션이 접근할 수 있는 DB
	- (참고) 시스템이 결합되고 추가 개발이 복잡해지므로 권장하지 않음. API, 메시지 통신 사용 권장
	- 공유되는 table -> unmanaged 의존성 -> mocking -> 상호작용 검증
	- 공유되지 않는 table -> managed 의존성 -> instance -> db 상태 직접 검증
	- Q. mocking 만 하고 상호작용 검증만 skip 하는 case 는 권장하지 않는 방식인지?
		- 단위테스트에서는 Ok, 통합테스트에서는 No

### 통합테스트에서 실제 DB 를 사용할 수 없으면 어떻게 할까?
- e.g. IT 보안 정책, 테스트용 DB 설정 및 유지 비용, 자동화 환경에 배포할 수 없는 레거시 DB
- Managed 의존성이므로 mock 으로 대체하기보다는 아예 통합테스트를 작성하지 말고 도메인 모델의 단위 테스트에만 집중하자

## 통합테스트 예제 with CRM 
### 어떤 시나리오를 테스트할까
- 가장 긴 주요 흐름
	- 기업 이메일에서 일반 이메일로 변경하는 유즈케이스. 사이드이펙트가 가장 많음
- 단위테스트로 테스트하지 않는 예외케이스
	- 이메일을 변경할 수 없는 시나리오 -> 컨트롤러에 이러한 확인이 없으면 어플리케이션이 '빨리 실패' -> 통합테스트 할 필요 없음

### 데이터베이스와 메시지버스 분류하기
- 직접 테스트할 대상과 Mocking 할 대상 분류하기
	- CRM 예제에서 DB 는 다른 시스템에서 접근할 수 없으므로 managed 의존성. -> 실제 인스턴스 사용
	- 메시지버스는 unmanaged 의존성 -> Mock 으로 대체


### 엔드 투 엔드 테스트
- 배포 후 작동하는 버전의 API 로 테스트. 어떤 외부 의존성도 Mock 으로 대체하지 않는다.


### 통합 테스트 : 첫 번째 시도
```java
@Test  
void changing_email_from_corporate_to_non_corporate() {  
   //given  
   var db = new Database(ConnectionString);  
   //DB 에 User, Company insert
   User user = createUser("user@mycorp.com", User.UserType.EMPLOYEE, db);  
   createCompany("mycorp.com", 1, db);  

   //MessageBus Mocking
   var messageBusMock = Mockito.mock(MessageBus.class);  
   var sut = new UserController(db, messageBusMock);  
  
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

   //Mock 상호작용 검증
   Mockito.verify(messageBusMock, Mockito.times(1)).sendEmailChangedMessage(user.getUserId(), "new@gmail.com");  
}
```

- 테스트가 DB 에 읽기/쓰기를 모두 수행하므로 회귀 방지를 최대화할 수 있음

## 의존성 추상화를 위한 인터페이스 사용
### 인터페이스와 느슨한 결합
- 외부 의존성을 위해 인터페이스를 도입하는 경우가 많음
- 인터페이스에 구현이 하나만 있다면 인터페이스를 도입하지 마라.
	- 단일 구현을 위한 인터페이스는 추상화가 아니며 느슨한 결합도 제공하지 않음. 추상화는 발명하는 것이 아니라 발견되는 것
	- You Arent Gonna Need It (YAGNI) 원칙 위배. 즉, 현재 필요하지 않은 기능에 시간을 낭비하지 말라
- 좋지 않은 사례 예시

```java
public interface IMessageBus{

}
public class MessageBus implements IMessageBus{
}
```

```java
public interface IUserRepository{

}
public class UserRepository implements IUserRepository{
}
```

### 프로세스 외부 의존성에 인터페이스를 사용하는 이유
- 인터페이스에 구현이 하나만 있다고 가정할 때 프로세스 외부 의존성에 인터페이스를 사용하는 이유
	- Mocking 하기 위함. 인터페이스가 없으면 테스트 대역을 만들 수 없음
	- Q. 구체 클래스를 mocking 하면 안되는 이유가 뭘까?
		- 11장에서 다룬다.
	- 바꿔 말하면 Unmanaged 의존성에 대해서만 인터페이스를 써라. Managed 의존성은 구체 클래스로 사용해라
	- Mocking 이외의 이유로는 단일 구현을 위해 인터페이스를 도입하지 마라

```java
public class UserController {  
  
   private final Database db;  
   private final IMessageBus messageBus;  
  
   public UserController(Database db, IMessageBus messageBus) {  
      this.db = db;  
      this.messageBus = messageBus;  
   }  
  
   public String changeEmail(int userId, String newEmail) {
   }
}
```

### 프로세스 내부 의존성을 위한 인터페이스 사용
- 마찬가지로 Mocking 목적이 아니라면 단일 구현일 때 인터페이스를 도입하지 마라.
	- 내부 의존성은 외부 의존성과 달리 상호 작용을 검증해선 안 된다는 걸 명심해라

## 통합 테스트 Best practices
- 도메인 모델 경계 명시하기
- 어플리케이션 내 계층 수 줄이기
- 순환 의존성 제거하기

### 도메인 모델 경계 명시하기
- 도메인 모델
	- 프로젝트가 해결하고자 하는 문제에 대한 도메인 지식 모음
- 도메인 모델 코드를 명시적이고 잘 알려진 위치에 두자. 즉, 패키지 구분을 확실히 하자.
	- 단위테스트는 도메인 모델 & 알고리즘 대상
	- 통합테스트는 컨트롤러 대상

### 어플리케이션 내 계층 수 줄이기
- 어플리케이션 내 추상 계층이 많을 때 단점
	- 코드 이해 어려움
	- 컨트롤러 - 도메인 모델 사이에 명확한 경계 없음
		- 각 계층을 따로 검증하려는 경향으로 통합테스트 가치 떨어짐
		- 하위 계층을 Mocking 하려는 경향으로 단위테스트 가치 떨어짐
- 가능한 계층 수를 줄여라
- 이상적인 계층
	- 도메인 모델 (도메인 로직 포함)
	- 어플리케이션 서비스 계층(컨트롤러)
		- 외부 클라이언트에 대한 진입점 제공, 도메인 클래스와 외부 의존성 간 작업 조정
	- 인프라 계층
		- DB, ORM 매핑, SMTP 게이트웨이

![](attachments/스크린샷%202023-04-29%20오전%2011.17.34.png)
### 순환 의존성 제거하기
- 순환 의존성
	- 둘 이상의 클래스가 제대로 작동하고자 직간접적으로 서로 의존하는 것
	- e.g. 콜백

```java
public class CheckOutService {  
   public void checkOut(int orderId) {  
      var service = new ReportGenerationService();  
	  // ReportGenerationService 인스턴스 생성 후 자신을 인수로 넘겨줌
      service.generateReport(orderId, this);  
  
      //중략  
   }  
}

public class ReportGenerationService {  
   public void generateReport(int orderId, CheckOutService checkOutService) {  
      //생성이 완료되면 checkOutService 호출  
   }  
}

```

- 위 코드(순환 의존성) 문제점
	- 코드를 읽고 이해하기 어려움
	- 동작 단위로 나눠서 테스트하려면 인터페이스에 의존해 Mocking 해야 하는데 이는 도메인 모델 테스트할 때 권장하지 않는 방식
- 순환 의존성이 없도록 개선한 코드

```java
public class CheckOutService {  
   public void checkOut(int orderId) {  
      var service = new ReportGenerationService();  
      Report report = service.generateReport(orderId);  
  
      //중략  
   }  
}

public class ReportGenerationService {  
  
   public Report generateReport(int orderId) {  
      // 이전과 달리 CheckOutService 를 호출하지 않고 작업 결과를 일반 값으로 리턴  
   }  
}

```

### 테스트에서 다중 실행 구절 사용
- 테스트에서 두 개 이상의 준비, 실행, 검증 구절을 두는 것은 code smell 에 해당한다
- 만약 위와 같은 코드가 있다면 테스트를 나누는 것이 좋다. 테스트가 단일 동작 단위에 초점을 맞추면 테스트를 이해하고 수정하기 쉽다.
- 단, 외부 의존성을 관리하기 어려운 경우라면 실행 구절을 여러 개 둘 수도 있다.
	- e.g. 은행에서 제공하는 샌드박스가 너무 느리거나 호출 횟수 제한이 있을 때

## 로깅 기능을 테스트하는 방법

### 로깅을 테스트 해야 하는가?
- 로깅이 어플리케이션의 식별할 수 있는 동작인지 구현 세부사항인지 여부에 따라 갈린다.
	- 로그를 개발자 이외의 다른 사람이 본다
		- 식별할 수 있는 동작
		- 테스트 O
		- 지원 로깅(support logging). 지원 담당자나 시스템 관리자가 추적할 수 있는 메시지 생성
	- 로그를 개발자만 본다
		- 구현 세부사항
		- 테스트 X
		- 진단 로깅(diagnostic logging). 개발자가 어플리케이션 내부 상황 파악할 수 있도록 도움

### 로깅을 어떻게 테스트해야 하는가?
- 로깅을 테스트한다면 식별할 수 있는 동작이므로 Mock 을 사용해서 상호작용을 검증해야 함.

#### Logger 인터페이스에 Wrapper 도입
- 그러나 지원 로깅은 비즈니스 요구사항이므로 logger 인터페이스를 wrapping 하여 DomainLogger 클래스를 만들고 비즈니스에 필요한 로깅을 모두 지원하는 것이 좋다.

```java
//User
public void changeEmail(String newEmail, Company company) {  
   logger.info("Changing email for user {userId} to {newEmail}"); //진단 로그  
   Assert.isNull(canChangeEmail(), "");  
  
   if (this.email == newEmail) {  
      return;  
   }  
  
   UserType newType =  company.isEmailCorporate(newEmail) ? UserType.EMPLOYEE : UserType.CUSTOMER;  
  
   if (this.type != newType) {  
      int delta = newType == UserType.EMPLOYEE ? 1 : -1;  
      company.changeNumberOfEmployees(delta);  
      domainLogger.userTypeHasChanged(userId, type, newType); //지원 로그  
   }  
  
   this.email = newEmail;  
   this.type = newType;  
   emailChangedEvents.add(new EmailChangedEvent(userId, newEmail));  
  
   logger.info("email is changed for user {userId}"); //진단 로그  
}

public class DomainLogger extends IDomainLogger{  
   private final Logger logger;  
  
   public DomainLogger(Logger logger) {  
      this.logger = logger;  
   }  
  
   public void userTypeHasChanged(int userId, User.UserType oldType, User.UserType newType) {  
      logger.info("User {userId} changed type " + "from {oldType} to {newType}");  
   }  
}
```


#### 구조화된 로깅 이해하기
- 데이터 캡처와 렌더링을 부리하는 로깅 기술
- `logger.info("user id is {userId}", 12);`
- 입력 매개변수와 결합해 Data set 을 형성. 텍스트파일이 아닌 json, csv 파일 등으로 렌더링하도록 로깅 라이브러리를 설정할 수 있음
- 위에서 구현한 DomainLogger 또한 로그를 Json, csv 파일로 작성하도록 쉽게 렌더링을 추가할 수 있음


#### 지원 로깅과 진단 로깅을 위한 테스트 작성
- DomainLogger 에는 외부 의존성이 있기 때문에 User(비즈니스 로직) - 외부 의존성 간 통신 발생. -> User 가 지나치게 복잡한 코드로 바뀌어 테스트 어려워짐
- 해결책
	- 별도의 domain event 도입

```java
//User
public void changeEmail(String newEmail, Company company) {  
   logger.info("Changing email for user {userId} to {newEmail}"); //진단 로그  
   Assert.isNull(canChangeEmail(), "");  
  
   if (this.email == newEmail) {  
      return;  
   }  
  
   UserType newType =  company.isEmailCorporate(newEmail) ? UserType.EMPLOYEE : UserType.CUSTOMER;  
  
   if (this.type != newType) {  
      int delta = newType == UserType.EMPLOYEE ? 1 : -1;  
      company.changeNumberOfEmployees(delta);  
      addDomainEvent(new UserTypeChangedEvent(userId, type, newType)); //domainLogger 대신 도메인 이벤트 사용  
   }  
  
   this.email = newEmail;  
   this.type = newType;  
   // emailChangedEvents.add(new EmailChangedEvent(userId, newEmail));  
   addDomainEvent(new EmailChangedEvent(userId, newEmail));  
  
   logger.info("email is changed for user {userId}"); //진단 로그  
}

//UserController
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
  
   db.saveCompany(newNumberOfEmployees);  
   db.saveUser(user);  
  
   eventDispatcher.dispatch(user.getDomainEvents());  
  
   return "OK";  
}
```

- eventDispatcher
	- EmailChangedEvent -> messageBus.sendEmailChangedMessage() 로 변환
	- userTypeChangedEvent -> domainLogger.userTypeHasChanged() 로 변환
- 단위테스트로 User 에서 UserTypeChangedEvent 인스턴스 확인
- 통합테스트로 Mock 을 써서 DomainLogger 와의 상호작용 확인

### 로깅이 얼마나 많으면 충분한가?
- 진단 로깅을 과도하게 사용하지 마라
	- 과도한 로깅은 코드를 혼란스럽게 한다.
	- 로그가 많을수록 필요한 정보를 찾기가 어려워진다.
- 도메인 모델에서는 진단 로깅을 절대 사용하지 마라. 대부분의 진단로깅은 컨트롤러로 옮겨라. 디버깅할 때만 일시적으로 진단 로깅을 사용하고 디버깅이 끝나면 제거해라

### 로거 인스턴스를 어떻게 전달할 것인가
- 정적 필드에 ILogger 를 저장하는 방식
	- ambient context. -> 안티패턴
		- 의존성이 숨어 있고 변경하기 어렵다
		- 테스트하기 어렵다.
	- Q. 다들 이렇게 하지 않나..?
	- 의존성이 숨어 있고 변경하기 어렵다.
	- 테스트하기 어렵다.
- method argument 혹은 생성자를 통해 logger 를 주입받아라.

## 결론
- 프로세스 외부 의존성과의 통신을 볼 때는 식별할 수 있는 동작인지 아니면 구현 세부사항인지 판단하자.
- 개발자가 아닌 사람이 로그를 본다면 로깅 기능을 Mocking 하고 그렇지 않다면  테스트하지 말라


