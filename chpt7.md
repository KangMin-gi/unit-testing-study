# 가치 있는 단위 테스트를 위한 리팩터링

- 가치 있는 테스트를 알아보는 데 그치지 않고 작성하는 방법에 대해 알아보자

## 리팩터링할 코드 식별하기
- 기반 코드를 리팩터링하지 않고서는 테스트 코드를 크게 개선할 수 없다. 즉 테스트 코드 개선을 위해서는 production 코드 개선이 선행되어야 한다

### 코드의 네 가지 유형

#### 분류 기준 1) 복잡도 혹은 도메인 유의성
- 코드 복잡도
	- 코드 내 분기 지점 수
- 도메인 유의성
	- 코드가 프로젝트 문제 도메인에 대해 얼마나 의미 있는지
- 복잡한 코드, 도메인 유의성이 있는 코드가 단위 테스트할 때 가장 이롭다. 회귀 방지에 뛰어나기 때문이다.
	- 복잡한 코드 -> 테스트할 가치 있다.
	- 도메인 유의성 있는 코드 -> 테스트할 가치 있다.

#### 기준 2) 협력자 수
- 클래스 또는 메서드가 가진 협력자 수
- 협력자가 많은 코드는 테스트 비용이 많이 든다.
	- expection condition, assertion 위한 코드가 필요하기 때문

#### 코드 네 가지 유형
- 도메인 모델, 알고리즘
	- 단위테스트 시 가장 가성비가 좋다.
		- 회귀 방지 뛰어남
		- 협력자가 거의 없어서 테스트 유지비가 낮음
- 간단한 코드
	- 테스트할 필요 없다.
- 컨트롤러
	- 통합테스트의 일부로서 간단히 테스트하자 (이어지는 챕터에서 다룰 예정)
- 지나치게 복잡한 코드
	- 단위 테스트하기 어렵지만, 테스트 커버리지 없이 두기엔 위험한 코드
	- 알고리즘 + 컨트롤러로 나누는 것이 일반적
- 지나치게 복잡한 코드를 피하고 도메인 모델과 알고리즘만 단위테스트하는 것이 가치 있고 유지보수가 쉬운 테스트 코드를 작성하는 방법
- 프로젝트 가치를 높이는 테스트 코드만 남기고 나머지 테스트는 리팩터링하거나 제거하자. 테스트 스위트 크기를 부풀리지 말자


![](attachments/스크린샷%202023-04-24%20오전%206.51.12.png)

### 험블 객체 패턴을 사용해 지나치게 복잡한 코드 분할하기
- 지나치게 복잡한 코드를 쪼개려면 험블 객체 패턴을 써야 한다.
- 테스트 가능한 부분을 추출하여 코드 구성을 아래와 같이 바꾼다.
	- 테스트 하기 어려운 의존성 - 험블 객체 - 로직
- 육각형 아키텍처, 함수형 아키텍처 모두 이 패턴을 구현한다.

![](attachments/스크린샷%202023-04-24%20오전%206.54.34.png)

![](attachments/스크린샷%202023-04-24%20오전%206.55.50.png)

- 육각형 아키텍처는 비즈니스 로직 - 프로세스 외부 의존성 간 통신 분리
- 함수형 아키텍처는 더 나아가 모든 외부협력자와의 커뮤니케이션 - 비즈니스 로직 분리


#### 험블 객체 패턴과 단일 책임 원칙
- 각 클래스는 단일한 책임만을 가져야 한다.
- 비즈니스 로직과 오케스트레이션 분리
- e.g. MVC, MVP pattern
- e.g. Domain Driven Design - Aggregate pattern
- Testable design : testable + easy to maintain

## 가치 있는 단위 테스트를 위한 리팩터링하기
- 지나치게 복잡한 코드를 알고리즘 & 컨트롤러로 나누는 예제를 살펴보자

### 고객 관리 시스템
- 사용자 이메일을 변경하는 유스케이스

#### 초기 구현

```java
public class User {  
   private int userId;  
   private String email;  
   private UserType type;  
  
   public void changeEmail(int userId, String newEmail) {  
      Object[] data = db.getUserById(userId);  
      this.userId = userId;  
      this.email = (String)data[1];  
      this.type = (UserType)data[2];  
  
      if (this.email == newEmail) {  
         return;  
      }  
  
      Object[] companyData = db.getCompany();  
      String companyDomainName = (String)companyData[0];  
      int numberOfEmployees = (int)companyData[1];  
  
      String emailDomain = newEmail.split('@')[1];  
      boolean isEmailCorporate = emailDomain == companyDomainName;  
      UserType newType = isEmailCorporate ? UserType.EMPLOYEE : UserType.CUSTOMER;  
  
      if (this.type != newType) {  
         int delta = newType == UserType.EMPLOYEE ? 1 : -1;  
         int newNumber = numberOfEmployees + delta;  
         db.saveCompany(newNumber);  
      }  
  
      this.email = newEmail;  
      this.type = newType;  
  
      db.saveUser(this);  
      messageBus.sendEmailChangedMessage(this.userId, newEmail);  
   }  
  
   public enum UserType {  
      CUSTOMER, EMPLOYEE  
   }  
  
  
}
```

- 복잡도는 높지 않지만 도메인 유의성 측면에서 점수가 높다.
- 협력자 수 측면에서 점수가 높다.
	- Database, MessageBus -> 프로세스 외부 협력자
	- 도메인 유의성이 높은 코드에서 프로세스 외부 협력자를 사용해선 안 된다.
- 분석 결과, 지나치게 복잡한 코드로 분류된다.

- Active Record 패턴
	- 도메인 클래스가 스스로 DB 를 검색하고 다시 저장하는 방식
	- 비즈니스 로직과 프로세스 외부 의존성 통신 간 분리가 없음
		- 코드 베이스가 커지면 확장이 어려움

![](attachments/스크린샷%202023-04-24%20오전%207.07.49.png)


### 개선 1단계) 암시적 의존성을 명시적으로 만들기
- DB, MessageBus 에 대한 인터페이스를 두고 User 에 주입 & 테스트할 때는 인터페이스를 Mocking
- 어쨌든 도메인 모델이 프로세스 외부 협력자에게 의존하는 것이므로 충분한 해결책이 아님


### 개선 2단계) 어플리케이션 서비스 계층 도입
- 도메인 모델 - 외부 시스템 통신 책임을 humble controller(어플리케이션 서비스) 로 옮긴다.

```java
public class UserController {  
  
   // 문제점1 : 외부 의존성을 주입받지 않고 직접 인스턴스화  
   private Database db = new Datebase();  
   private MessageBus messageBus = new MessageBus();  
  
   public void changeEmail(int userId, String newEmail) {  
      Object[] data = db.getUserById(userId);  
      String email = (String)data[1];  
      User.UserType type = (User.UserType)data[2];  
      //문제점2 : db 에서 얻은 데이터를 user 인스턴스로 재구성하는 로직이 어플리케이션 서비스에 속해 있음  
      var user = new User(userId, email, type);  
  
      Object[] companyData = db.getCompany();  
      String companyDomainName = (String)companyData[0];  
      int numberOfEmployees = (int)companyData[1];  
  
      //문제점3 : 회사 직원수 - User 반환값과 관련 있는 것이 이상함. 책임 분리 적절치 않음  
      int newNumberOfEmployees = user.changeEmail(newEmail, companyDomainName, numberOfEmployees);  
  
      //문제점4 : 기존 이메일 - 새로운 이메일 달라진 점 있는지 확인하는 로직이 사라짐  
      db.saveCompany(newNumberOfEmployees);  
      db.saveUser(user);  
      messageBus.sendEmailChangedMessage(userId, newEmail);  
  
   }  
  
}
```

```java
public class User {  
   private int userId;  
   private String email;  
   private UserType type;  
  
   public int changeEmail(String newEmail, String companyDomainName, int numberOfEmployees) {  
  
      if (this.email == newEmail) {  
         return numberOfEmployees;  
      }  
  
      String emailDomain = newEmail.split('@')[1];  
      boolean isEmailCorporate = emailDomain == companyDomainName;  
      UserType newType = isEmailCorporate ? UserType.EMPLOYEE : UserType.CUSTOMER;  
  
      if (this.type != newType) {  
         int delta = newType == UserType.EMPLOYEE ? 1 : -1;  
         int newNumber = numberOfEmployees + delta;  
         numberOfEmployees = newNumber;  
      }  
  
      this.email = newEmail;  
      this.type = newType;  
  
      return numberOfEmployees;  
   }  
  
   public enum UserType {  
      CUSTOMER, EMPLOYEE  
   }  
}
```

- User class
	- 프로세스 외부, 내부 협력자가 없어졌음
	- 프로세스 외부 의존성과 통신할 필요가 없어졌으므로 테스트 하기 쉬워졌다.
- User Controller
	- 주석에 명시한 문제점들이 존재
	- 컨트롤러임에도 로직이 꽤 복잡해서 지나치게 복잡한 코드 - 컨트롤러 경계에 위치

	![](attachments/스크린샷%202023-04-24%20오전%207.16.13.png)

### 3단계) 어플리케이션 서비스 복잡도 낮추기
- 재구성 로직을 어플리케이션 서비스에서 추출
	- c.f. ORM library
	- 본 예제에서는 팩토리 클래스를 별도로 작성

```java
public class UserFactory {  
  
   public static User create(Object[] data) {  
      Assert.isTrue(data.length >= 3, "data should have more than 2 elements");  
  
      int id = (int)data[0];  
      String email = (String)data[1];  
      User.UserType type = (User.UserType)data[2];  
  
      return new User(id, email, type);  
   }  
}

public class CompanyFactory {  
  
   public static Company create(Object[] data) {  
      String companyDomainName = (String)data[0];  
      int numberOfEmployees = (int)data[1];  
  
      return new Company(companyDomainName, numberOfEmployees);  
   }  
}
```

### 4단계) 새로운 도메인 클래스(Company) 도입
- User class 가 업데이트 된 직원 수를 반환하던 로직
	- 어색함. 책임이 잘못 분리되어 있다는 신호
	- 회사 관련 로직을 담당하는 새로운 도메인 클래스 Company 생성

```java
public class Company {  
   private String domainName;  
   private int numberOfEmployees;  
  
   public void changeNumberOfEmployees(int delta) {  
      Assert.isTrue(numberOfEmployees + delta >= 0, "");  
      numberOfEmployees += delta;  
   }  
  
   public boolean isEmailCorporate(String email) {  
      String emailDomain = email.split('@')[1];  
      return emailDomain == domainName;  
   }  
}
```

```java
public class UserController {  
  
   private Database db = new Datebase();  
   private MessageBus messageBus = new MessageBus();  
  
   public void changeEmail(int userId, String newEmail) {  
      Object[] userData = db.getUserById(userId);  
      var user = UserFactory.create(userData);  
  
      Object[] companyData = db.getCompany();  
      Company company = CompanyFactory.create(companyData);  
  
      user.changeEmail(newEmail, company);  
  
      db.saveCompany(newNumberOfEmployees);  
      db.saveUser(user);  
      messageBus.sendEmailChangedMessage(userId, newEmail);  
  
   }  
  
}
```

```java
public class User {  
   private int userId;  
   private String email;  
   private UserType type;  
  
   public void changeEmail(String newEmail, Company company) {  
  
      if (this.email == newEmail) {  
         return;  
      }  
  
      if (this.type != newType) {  
         int delta = newType == UserType.EMPLOYEE ? 1 : -1;  
         company.changeNumberOfEmployees(delta);  
      }  
  
      this.email = newEmail;  
      this.type = newType;  
   }  
  
   public enum UserType {  
      CUSTOMER, EMPLOYEE  
   }  
}
```

- 이제는 `User`  가 회사 데이터를 처리하지 않고 Company 인스턴스를 받아서 회사 이메일 여부 판단, 회사 직원 수 변경 책임을 해당 인스턴스에게 위임
- User 에게 협력자(Company) 가 생겨서 테스트가 약간 어려워지긴 했으나 큰 문제는 아님
- 팩토리 클래스 도입으로 UserController 는 확실히 컨트롤러 사분면에 위치하게 됨

- User 도메인 클래스가 사이드이펙트를 일으키긴 하지만 사이드이펙트를 끝까지 메모리 상에 들고 있고, 컨트롤러가 이를 DB 에 저장할 때만 사이드이펙트가 도메인의 경계를 넘게 됨
- 테스트가 외부 의존성을 검사할 필요가 없고 통신 기반 테스트를 사용하지 않아도 된다. 

## 최적의 단위 테스트 분석

- 예제 코드를 코드 유형별로 나눠보면 아래와 같다.

![](attachments/스크린샷%202023-04-24%20오전%207.33.31.png)

### 도메인 계층과 유틸리티 코드 테스트

```java
@Test  
void changing_Email_from_non_corporate_to_corporate() {  
   var company = new Company("mycorp.com", 1);  
   var sut = new User(1, "user@gmail.com", User.UserType.CUSTOMER);  
  
   sut.changeEmail("new@mycorp.com", company);  
  
   assertThat(company.getNumberOfEmployees()).isEqualTo(2);  
   assertThat(sut.getEmail()).isEqualTo("new@mycorp.com");  
   assertThat(sut.getType()).isEqualTo(User.UserType.EMPLOYEE);  
}

@Test  
@ParameterizedTest()  
@ValueSource("mycorp.com","email@mycorp.com", true) // 추후 학습  
void differentiates_a_corporate_email_from_non_corporate(String domain, String email, boolean expectedResult) {  
   var sut = new Company(domain, 0);  
  
   boolean isEmailCorporate = sut.isEmailCorporate(email);  
  
   assertThat(isEmailCorporate).isEqualTo(expectedResult);  
}
```

### 나머지 사분면 코드 테스트
- User, Company 의 생성자
	- 단순하고 회귀 방지 효과가 떨어지므로 작성하지 말자
- 컨트롤러
	- 다음 챕터에서 학습 예정

### 전제 조건을 테스트해야 하는가?
- 도메인 유의성이 있는 전제조건은 테스트 해라
	- e.g. Company class `Assert.isTrue(numberOfEmployees + delta >= 0); ` (O)
	- e.g. UserFactoryClass `Assert.isTrue(data.length >= 3);` (X)


## 컨트롤러에서 조건부 로직 처리
- 비즈니스 로직 - 오케스트레이션 분리는 비즈니스 연산이 3단계로 구분될 때 효과적
	- 저장소에서 데이터 검색
	- 비즈니스 로직 실행
	- 데이터를 다시 저장소에 저장

![](attachments/스크린샷%202023-04-24%20오후%2012.58.10.png)

- 그러나 6장에서 보았듯 비즈니스 로직에서 의사결정 결과에 따라 외부 의존성에서 데이터를 추가로 조회해야 하는 경우가 있다.

![](attachments/스크린샷%202023-04-24%20오후%2012.59.06.png)

#### 세 가지 특성
- 도메인 모델 testability
	- 도메인 클래스의 협력자 수, 유형
- 컨트롤러 단순성
	- 컨트롤러에 있는 의사결정(분기) 유무
- 성능
	- 프로세스 외부 의존성 호출 횟수

- 대안1) 무리를 해서라도 'read-decide-act' 구조를 유지한다.
	- 단점 : 필요 없는 경우에도 프로세스 외부 의존성 호출 -> 성능 저하
		- 권장 X
- 대안2) 도메인 모델에 프로세스 외부 의존성을 주입하고 비즈니스 로직이 해당 의존성을 호출할 시점을 직접 결정할 수 있게 한다.
	- 단점 : 도메인 모델의 testability 저하
		- 지나치게 복잡한 코드에 가까워짐. 권장 X
- 대안3) 의사결정 프로세스 단계를 더 세분화하고 각 단계별로 컨트롤러를 실행하도록 한다. 
	- 단점 : 컨트롤러가 복잡해짐. 컨트롤러에 의사 결정 지점이 늘어남
	- 이를 완화할 수 있는 해법이 있음

![](attachments/스크린샷%202023-04-24%20오후%201.10.07.png)

### CanExecute/Execute 패턴 사용

- 예제 코드에 다음 유스케이스를 추가해보자
	- 사용자는 자신이 confirm 한 email 은 변경할 수 없다.

#### 최초 구현

```java
public class User {  
   private int userId;  
   private String email;  
   private UserType type;  

   //새로 추가된 필드
   private boolean isEmailConfirmed;  
  
   public String changeEmail(String newEmail, Company company) {  
      if (isEmailConfirmed) {  
         return "Cannot change a confirmed email";  
      }  
  
      // 중략  
   }
}
```

```java
public class UserController {  
     
   private Database db = new Datebase();  
   private MessageBus messageBus = new MessageBus();  
  
   public String changeEmail(int userId, String newEmail) {  
      Object[] userData = db.getUserById(userId);  
      User user = UserFactory.create(userData);  
  
      Object[] companyData = db.getCompany();  
      Company company = CompanyFactory.create(companyData);  
  
      String error = user.changeEmail(newEmail, company);  
      if (error != null) {  
         return error;  
      }  
  
      db.saveCompany(newNumberOfEmployees);  
      db.saveUser(user);  
      messageBus.sendEmailChangedMessage(userId, newEmail);  
  
      return "OK";  
   }  
  
}
```

- 컨트롤러가 의사결정을 하진 않지만 성능 저하 발생
	- 이메일을 변경할 수 없는 경우에도 DB 에서 company instance 를 조회해야 함

#### 개선 시도) IsEmailConfirmed 확인을 컨트롤러에서 수행

```java
public class UserController {  
  
   private Database db = new Datebase();  
   private MessageBus messageBus = new MessageBus();  
  
   public String changeEmail(int userId, String newEmail) {  
      Object[] userData = db.getUserById(userId);  
      User user = UserFactory.create(userData);  
  
      //User 에서 이곳으로 옮긴 의사결정  
      if (user.isEmailConfirmed()) {  
         return "Cannot change a confirmed email";  
      }  
        
      Object[] companyData = db.getCompany();  
      Company company = CompanyFactory.create(companyData);  
      db.saveCompany(newNumberOfEmployees);  
      db.saveUser(user);  
      messageBus.sendEmailChangedMessage(userId, newEmail);  
  
      return "OK";  
   }  
  
}
```

- 성능 저하는 없지만 의사 결정 프로세스가 두 부분으로 나뉜다는 문제 발생. 즉, 캡슐화가 깨짐
	- 이메일 변경 진행 여부 -> 컨트롤러에서 판단
	- 변경 시 해야할 일 -> User 에서 수행

#### CanExecute/Execute 패턴 적용
```java
public class User {  
   private int userId;  
   private String email;  
   private UserType type;  
  
   private boolean isEmailConfirmed;  
  
   public String canChangeEmail() {  
      if (isEmailConfirmed) {  
         return "Cannot change a confirmed email";  
      }  
      return null;  
   }  
  
   public void changeEmail(String newEmail, Company company) {  
      Assert.isNull(canChangeEmail(), "");  
      
      // 중략  
   }
}
```

```java
public class UserController {  
  
   private Database db = new Datebase();  
   private MessageBus messageBus = new MessageBus();  
  
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
      messageBus.sendEmailChangedMessage(userId, newEmail);  
  
      return "OK";  
   }  
  
}
```

- 장점
	- 컨트롤러는 이메일 변경 프로세스에 대해 알 필요가 없다. `canChangeEmail()` 을 호출해서 연산을 수행할 수 있는지 확인만 하면 된다.
	- `changeEmail()` 에 전제조건이 추가 되더라도 confirmation 여부를 먼저 확인할 수 있음
	- Q. 최초 구현에 비해서 어떤 장점이 있는건지 잘 모르겠다...

### 도메인 이벤트를 사용해 도메인 모델 변경 사항 추적

- 도메인 이벤트
	- domain experts(?) 에게 의미있는 이벤트. 주로 외부 application 에게 중요한 변화를 알려주고 싶을 때 활용된다.

- 기존 코드는 email 이 변경되지 않았더라도 messageBus 로 메시지를 보냄

```java
//User
public void changeEmail(String newEmail, Company company) {  
   Assert.isNull(canChangeEmail(), "");  
   if (this.email == newEmail) {  
      return;  
   }  
   // 중략  
}

//Controller
public String changeEmail(int userId, String newEmail) {  

   //중략
  
   user.changeEmail(newEmail, company);  
  
   db.saveCompany(newNumberOfEmployees);  
   db.saveUser(user);  
   messageBus.sendEmailChangedMessage(userId, newEmail);  
  
   return "OK";  
}
```

- domain event 를 새로 정의하여 문제 해결

```java
//User
public void changeEmail(String newEmail, Company company) {  
   Assert.isNull(canChangeEmail(), "");  
  
   if (this.email == newEmail) {  
      return;  
   }  
     
   UserType newType =  company.isEmailCorporate(newEmail) ? UserType.EMPLOYEE : UserType.CUSTOMER;  
  
   if (this.type != newType) {  
      int delta = newType == UserType.EMPLOYEE ? 1 : -1;  
      company.changeNumberOfEmployees(delta);  
   }  
  
   this.email = newEmail;  
   this.type = newType;  
   emailChangedEvents.add(new EmailChangedEvent(userId, newEmail));  
}

//Controller
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
  
   for (var event : user.getEmailChangedEvents()) {  
      messageBus.sendEmailChangedMessage(event.getUserId(), event.getNewEmail());  
   }  
     
   return "OK";  
}

```

```java
@Test  
void changing_email_from_corporate_to_non_corporate() {  
   Company company = new Company("mycorp.com", 1);  
   var sut = new User(1, "user@mycorp.com", User.UserType.EMPLOYEE, false);  
  
   sut.changeEmail("new@gmail.com", company);  
  
   assertThat(company.getNumberOfEmployees()).isEqualTo(0);  
   assertThat(sut.getEmail()).isEqualTo("new@gmail.com");  
   assertThat(sut.getType()).isEqualTo(User.UserType.CUSTOMER);  
   assertThat(sut.getEmailChangedEvents()).isEqualTo(new EmailChangedEvent(1, "new@gmail.com"));  
}
```

## 결론
- 외부 시스템에 대한 사이드이펙트를 추상화
	- e.g. emailChangedEvent
	- 덕분에 외부 의존성 없이 단순한 단위 테스트로 테스트할 수 있었음
- 도메인 이벤트와 canExecute/execute 패턴으로 도메인 모델에 모든 의사 결정을 담을 수 있었음
	- 그러나 현실적으로 비즈니스 로직 파편화가 생기는 상황이 있을 수 있음
	- e.g. 컨트롤러에서 User uniqueness 검증
		- 비즈니스 로직이 파편화되더라도 비즈니스 로직 - 오케스트레이션 분리하는 것은 의의가 있음. (단위 테스트 간소화)

### 식별할 수 있는 동작
- 클라이언트 목표 중 하나에 직접적인 연관이 있음
- 외부 어플리케이션에서 볼 수 있는 프로세스 외부 의존성에서 사이드 이펙트가 발생

![](attachments/스크린샷%202023-04-26%20오전%209.02.32.png)

- 컨트롤러를 테스트할 때는 `changeEmail()` , `sendEmailChanged()` 두 메서드를 호출하는지 검증이 필요하다. 그러나 User 를 호출하는 것은 구현 세부사항이므로 검증하면 안 된다.
- 한 단계 더 들어가서 User 를 테스트할 때는 컨트롤러가 클라이언트가 된다.  `changeEmail()` 은 클라이언트의 목표와 직접적인 연관이 있으므로 테스트한다. 하지만 User -> Company 로의 호출은 컨트롤러 관점에서 구현 세부사항에 해당한다. 따라서 Company 의 어떤 메소드를 호출하는지는 검증해서는 안된다.


