# 7장. 가치 있는 단위 테스트를 위한 리팩터링

### 좋은 단위 테스트 스위트의 속성

- 개발 주기에 통합
- 코드베이스 중 가장 중요한 부분만을 대상
- 최소한의 유지비로 최대의 가치
    - 가치 있는 테스트 식별
        - 회귀 방지
        - 리팩터링 내성
        - 빠른 피드백
        - 유지 보수성
    - **가치 있는 테스트 작성**

# 1. 리팩터링할 코드 식별하기

테스트 코드와 제품 코드는 본질적으로 관련되어 있기 때문에 기반 코드를 리팩터링하지 않고서는 테스트 스위트를 크게 개선할 수 없다.

## 코드의 네 가지 유형

**분류 기준**

- 복잡도 또는 도메인 유의성
    - 복잡도
        - 코드 복잡도는 **코드 내 의사 결정 지점 수**로 정의하고, 이 숫자가 클수록 복잡도는 높아진다.
    - 도메인 유의성
        - 도메인 유의성은 코드가 프로젝트의 문제 도메인에 얼마나 의미 있는지 나타낸다.
        - 일반적으로 **도메인 계층의 모든 코드**는 최종 사용자의 목표와 직접적인 연관성이 있으므로 도메인 유의성이 높다.
        - 유틸리티 코드는 그런 연관성이 없다.
    - 복잡한 코드와 도메인 유의성을 갖는 코드의 단위 테스트는 **회귀 방지**에 뛰어나기 때문에 가장 이롭다.
    - 복잡도와 도메인 유의성은 서로 독립적
- 협력자 수
    - 클래스 또는 메서드가 가진 협력자 수
    - 협력자는 가변 의존성이거나 프로세스 외부 의존성
    - 테스트 크기에 따라 달라지는 유지 보수성 지표 때문에 협력자가 많은 코드는 테스트 비용이 많이 든다.
    - 협력자의 유형도 중요
        - 도메인 모델이라면 테스트에서 목 체계가 복잡하여 유지비가 더 많이 들기 때문에 프로세스 외부 협력자를 사용하면 안된다.
        - 리팩터링 내성을 잘 지키려면 어플리케이션 경계를 넘는 상호 작용을 검증하는 데만 사용해야 한다.
        - 프로세스 외부 의존성을 가진 모든 통신은 도메인 계층 외부의 클래스에 위임하는 것이 좋다.
        - 그러면 도메인 클래스는 프로세스 내부 의존성에서만 동작하게 된다.
    - 암시적 협력자와 명시적 협력자 모두 해당
    - 불변 의존성은 해당하지 않는다.

위 분류 기준으로 네 가지 코드 유형으로 나눌 수 있다.

<img width="679" alt="스크린샷 2023-04-21 오후 8 45 58" src="https://user-images.githubusercontent.com/7659412/233800273-c81dd5bb-5178-45da-af80-1c0eaa521036.png">

- 도메인 모델과 알고리즘
    - 보통 복잡한 코드는 도메인 모델이지만, 문제 도메인과 직접적으로 관련이 없는 복잡한 알고리즘이 있을 수 있어 100%는 아니다.
    - 단위 테스트를 작성하면 노력 대비 가장 가치있고 저렴하다.
      - 코드가 복잡하거나 중요한 로직을 수행해서 테스트의 회귀 방지가 향상되기 때문에 가치 있다.
      - 코드에 협력자가 거의 없어서 테스트 유지비를 낮추기 때문에 저렴하다.
- 간단한 코드
    - 매개변수가 없는 생성자 또는 한 줄 속성 등
    - 협력자가 있는 경우가 거의 없고 복잡도나 도메인 유의성도 거의 없다.
    - 가치가 0에 가까워 테스트할 필요가 전혀 없다.
- 컨트롤러
    - 복잡하거나 비즈니스에 중요한 작업을 하는 것이 아니라 도메인 클래스와 외부 애플리케이션 같은 다른 구성 요소의 작업을 조정
    - 포괄적인 통합 테스트의 일부로서 간단히 테스트 해야 한다.
- 지나치게 복잡한 코드
    - 덩치가 큰 컨트롤러 등
    - 협력자가 많으며 복잡하거나 중요하다.
    - 단위 테스트가 어렵지만 테스트 커버리지 없이 내버려두는 것은 너무 위험하다.
    - 많은 사람이 단위 테스트로 어려움을 겪는 주요 원인 중 하나
    - 실제로 구현이 까다로울 수 있지만, 지나치게 복잡한 코드를 알고리즘과 컨트롤러라는 두 부분으로 나누는 것이 일반적

지나치게 복잡한 코드를 피하고 도메인 모델과 알고리즘만 단위 테스트하는 것이 매우 가치 있고 유지 보수가 쉬운 테스트 스위트로 가는 길이다.

### 험블 객체 패턴을 사용해 지나치게 복잡한 코드 분할하기

지나치게 복잡한 코드를 분할하려면 **험블 객체 패턴**을 사용한다.

- 코드가 비동기 또는 멀티 스레드 실행, 사용자 인터페이스, 프로세스 외부 의존성과의 통신 등 프레임워크 의존성에 결합되어 있으면 테스트가 어렵다.

- 테스트 대상 코드의 로직을 테스트 하려면 테스트가 가능한 부분을 추출해야 한다.

- 코드는 테스트 가능한 부분을 둘러싼 얇은 험블 래퍼가 된다.

- 험블 래퍼가 테스트하기 어려운 의존성과 새로 추출된 구성 요소를 붙이지만 자체적인 로직이 거의 없거나 전혀 없으므로 테스트할 필요가 없다.

<img width="653" alt="스크린샷 2023-04-21 오후 8 53 19" src="https://user-images.githubusercontent.com/7659412/233800277-11398307-ba64-4d0d-8d62-dabb0a1e6bc3.png">

육각형 아키텍처와 함수형 아키텍처가 이 패턴을 구현한다.

- 육각형 아키텍처는 비즈니스 로직과 프로세스 외부 의존성과의 통신을 분리
    - 도메인 계층과 어플리케이션 서비스 계층이 각각 담당
- 함수형 아키텍처는 더 나아가 프로세스 외부 의존성뿐만 아니라 모든 협력자와의 커뮤니케이션에서 비즈니스 로직을 분리
    - 함수형 코어에는 아무런 협력자도 없다.

<img width="586" alt="스크린샷 2023-04-21 오후 8 54 48" src="https://user-images.githubusercontent.com/7659412/233800281-30a86154-29d7-45dc-a510-07a3fad02c23.png">

험블 객체 패턴을 보는 또 다른 방법은 **단일 책임 원칙**을 지키는 것이다.

- 단일 책임 원칙은 각 클래스가 단일한 책임만 가져야 한다는 원칙이다.

- 험블 객체 패턴을 적용하면 비즈니스 로직을 거의 모든 것과 분리할 수 있다.

- MVC, MVP 패턴
    - 비즈니스 로직(모델)과 UI 관심사(뷰) 그리고 모델과 뷰 사이의 조정(프리젠터 또는 컨트롤러)을 분리하는데 도움이 된다.
    - 프리젠터와 컨트롤러의 구성 요소는 험블 객체로 뷰와 모델을 붙인다.
- 도메인 주도 설계의 집계 패턴
    - 클래스를 클러스터로 묶어서 클래스 간 연결을 줄인다.
    - 클래스는 해당 클러스터 내부에서 강결합돼 있지만, 클러스터 자체는 느슨하게 결합돼 있다.
    - 코드베이스의 총 통신 수를 줄여 연결이 줄어들고 테스트 용이성이 향상 된다.

- 비즈니스 로직과 오케스트레이션을 분리하면 테스트 용이성도 좋아지고 코드 복잡도도 해결할 수 있어 프로젝트 성장에 중요한 역할을 한다.

# 2. 가치 있는 단위 테스트를 위한 리팩터링하기

### 고객 관리 시스템 소개

- 사용자 등록을 처리하는 고객 관리 시스템
- 모든 사용자는 데이터베이스에 저장
- 현재 시스템은 사용자 이메일 변경이라는 단 하나의 유스케이스만 지원한다.
    - 사용자 이메일이 회사 도메인에 속한 경우 해당 사용자는 직원으로 표시. 그렇지 않으면 고객으로 간주
    - 시스템은 회사의 직원 수를 추적해야 한다. 사용자 유형이 직원에서 고객으로 또는 그 반대로 변경되면 이 숫자도 변경해야 한다.
    - 이메일이 변경되면 시스템은 메시지 버스로 메시지를 보내 외부 시스템에 알려야 한다.

```java
public class User {
	private int userId;
	private String email;
	private UserType type;

	public void changeEmail(int userId, String newEmail) {
		Object[] data = database.getUserById(userId);
		userId = userId;
		email = (String)data[1];
		type = (UserType)data[2];

		if(email == newEmail) {
			return;
		}

		Object[] companyData = database.getCompany();
		String companyDomainName = (String)companyData[0];
		int numberOfEmployees = (int)companyData[1];

		String emailDomain = newEmail.split('@')[1];
		boolean isEmailCorporate = emailDomain == companyDomainName;
		UserType newType = isEmailCorporate ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			int newNumber = numberOfEmployees + delta;
			database.saveCompany(newNumber);
		}

		email = newEmail;
		type = newType;

		database.saveUser(this);
		messageBus.sendEmailChangeMessage(userId, newEmail);
	}
}

public enum UserType {
	Customer, Employee
}
```

- User 클래스는 사용자의 이메일을 변경한다.
- 사용자를 직원으로 식별할지 또는 고객으로 식별할지와 회사의 직원 수를 어떻게 업데이트할지 등 두 가지의 명시적 의사결정 지점만 포함되어 있어 코드 복잡도는 그리 높지 않다.
- 어플리케이션의 핵심 비즈니스 로직이므로 복잡도와 도메인 유의성은 높다.
- 네 개의 의존성이 있으며, 두 개는 명시적이고 나머지 두 개는 암시적
    - 명시적 의존성은 userId, newEmail 인수. 값이므로 클래스의 협력자 수에는 포함되지 않는다.
    - 암시적 의존성은 database, messageBus, 프로세스 외부 협력자이다.

<img width="641" alt="스크린샷 2023-04-21 오후 9 15 48" src="https://user-images.githubusercontent.com/7659412/233800286-feec6110-78e7-440c-a786-817d17d7ebb4.png">

- User 클래스는 복잡한 코드로 분류된다.
- 도메인 클래스가 스스로 데이터베이스를 검색하고 다시 저장하는 이리한 방식을 **활성 레코드 패턴**이라고 한다.
- 비즈니스 로직과 프로세스 외부 의존성과의 통신 사이에 분리가 없어서 단순하거나 단기 프로젝트에서는 잘 동작하지만 코드베이스가 커지면 확장하지 못하는 경우가 많다.

### 1단계 : 암시적 의존성을 명시적으로 만들기

- 테스트 용이성을 개선하는 일반적인 방법은 암시적 의존성을 명시적으로 만드는 것이다.
- 데이터베이스와 메시지 버스에 대한 인터페이스를 두고, 이 인터페이스를 User에 주입한 후 테스트에서 목으로 처리한다.
- 코드 유형 도표 관점에서 도메인 모델이 프로세스 외부 의존성을 직접 참조하든 인터페이스를 통해 참조하든 상관이 해당 의존성은 여전히 프로세스 외부에 있다.
- 테스트 하기 위해 목이 필요하고 테스트 유지비가 증가한다. 목을 데이터베이스 의존성에 사용하면 테스트 취약성을 야기할 수 있다.
- 도메인 모델은 직접적으로든 간접적으로든 프로세스 외부 협력자에게 의존하지 않는 것이 훨씬 깔끔하다.
- 도메인 모델은 외부 시스템과의 통신을 책임지지 않아야 한다.

### 2단계 : 어플리케이션 서비스 계층 도입

- 도메인 모델이 외부 시스템과 직접 통신하는 문제를 극복하려면 다른 클래스인 험블 컨트롤러로 책임을 옮겨야 한다.
- 일반적으로 도메인 클래스는 다른 도메인 클래스나 단순 값과 같은 프로세스 내부 의존성에만 의존해야 한다.

```java
public class UserController {
	private Database database = new Database();
	private MessageBus messageBus = new MessageBus();

	public void changeEmail(int userId, String newEmail) {
		Object[] data = database.getUserById(userId);
		String email = (String)data[1];
		UserType type = (UserType)data[2];
		User user = new User(userId, email, type);

		Object[] companyData = database.getCompany();
		String companyDomainName = (String)companyData[0];
		int numberOfEmployees = (int)companyData[1];

		int newNumberOfEmployees = user.changeEmail(newEmail, companyDomainName, numberOfEmployees);

		database.saveCompany(newNumberOfEmployees);
		database.saveUser(user);
		messageBus.sendEmailChangeMessage(userId, newEmail);
	}
}

public class User {
	private int userId;
	private String email;
	private UserType type;

	public int changeEmail(String newEmail, String companyDomainName, int numberOfEmployees) {
		if(email == newEmail) {
			return numberOfEmployees;
		}

		String emailDomain = newEmail.split('@')[1];
		boolean isEmailCorporate = emailDomain == companyDomainName;
		UserType newType = isEmailCorporate ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			int newNumber = numberOfEmployees + delta;
			numberOfEmployees = newNumber;
		}

		email = newEmail;
		type = newType;

		return numberOfEmployees;
	}
}

public enum UserType {
	Customer, Employee
}
```

User 클래스는 더 이상 프로세스 외부 의존성과 통신할 필요가 없으므로 테스트 하기가 매우 쉬워졌다.

문제점

- 프로세스 외부 의존성이 주입되지 않고 직접 인스턴스화 된다. 이 클래스를 위해 작성할 통합 테스트에서 문제가 된다.
- 컨트롤러는 데이터베이스에서 받은 원시 데이터를 User 인스턴스로 재구성한다.이는 복잡한 로직이므로 애플리케이션 서비스에 속하면 안된다. 애플리케이션 서비스의 역할은 복잡도나 도메인 유의성 로직이 아니라 오케스트레이션만 해당한다.
- User는 이제 업데이트된 직원 수를 반환하는데 회사 직원 수는 특정 사용자와 관련 없다.
- 컨트롤러는 새로운 이메일이 전과 다른지 여부와 관계없이 무조건 데이터를 수정해서 저장하고 메시지 버스에 알림을 보낸다.

<img width="534" alt="스크린샷 2023-04-21 오후 9 48 02" src="https://user-images.githubusercontent.com/7659412/233800293-78542d91-8d73-4417-897d-abb36351ffe4.png">

### 3단계 : 어플리케이션 서비스 복잡도 낮추기

- UserController의 복잡도를 낮추기 위해서는 User 인스턴스로 재구성하는 로직을 추출해야 한다.
- ORM 라이브러리를 사용해 데이터베이스를 도메인 모델에 매핑하면 재구성 로직을 옮기기에 적절한 위치가 될 수 있다.
- ORM을 사용하지 않거나 사용할 수 없으면 도메인 모델에 원시 데이터베이스 데이터로 도메인 클래스를 인스턴스화하는 팩토리 클래스를 작성한다.

```java
public class UserFactory {
	public static User create(Object[] data) {
		Assert.isTrue(data.length >= 3);

		int id = (int)data[0];
		String email = (String)data[1];
		UserType type = (UserType)data[2];

		return new User(id, email, type);
	}
}
```

- 협력자와 완전히 격리돼 있으므로 테스트가 쉬워졌다.
- 재구성 로직은 복잡하지만 도메인 유의성이 없다. 유틸리티 코드이다.

### 4단계 : 새 Company 클래스 소개

- User에서 업데이트된 직원 수를 반환하는 부분의 책임이 잘못 되었고 추상화가 없다.
- 회사 관련 로직과 데이터를 함께 묶는 또 다른 도메인 클래스인 Company 클래스를 만들어야 한다.

```java
public class Company {
	private String domainName;
	private int numberOfEmployees;

	public void changeNumberOfEmployees(int delta) {
		Assert.isTrue(numberOfEmployees + delta >= 0);
		numberOfEmployees += delta;
	}

	public boolean isEmailCorporate(String email) {
		String emailDomain = email.split('@')[1];
		return emailDomain == domainName;
	}
}
```

- ’묻지 말고 말하라’ 라는 원칙을 준수하는데 도움이 된다.
  - 데이터와 해당 데이터에 대한 작업을 묶는다.
  - User 인스턴스는 직원 수를 변경하거나 특정 이메일이 회사 이메일인지 여부를 파악하도록 회사에 말하며, 원시 데이터를 묻지 않고 모든 작업을 자체적으로 수행한다.

```java
public class UserController {
	private Database database = new Database();
	private MessageBus messageBus = new MessageBus();

	public void changeEmail(int userId, String newEmail) {
		Object[] userData = database.getUserById(userId);
		User user = UserFactory.crate(userData);

		Object[] companyData = database.getCompany();
		Company company = CompanyFactory.create(companyData);

		user.changeEmail(newEmail, company);

		database.saveCompany(company);
		database.saveUser(user);
		messageBus.sendEmailChangeMessage(userId, newEmail);
	}
}

public class User {
	private int userId;
	private String email;
	private UserType type;

	public void changeEmail(String newEmail, Company company) {
		if(email == newEmail) {
			return;
		}

		UserType newType = company.isEmailCorporate(newEmail) ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			company.changeNumberOfEmployees(delta);
		}

		email = newEmail;
		type = newType;
	}
}

public enum UserType {
	Customer, Employee
}
```

<img width="339" alt="스크린샷 2023-04-22 오후 3 19 41" src="https://user-images.githubusercontent.com/7659412/233800296-942fb071-a6af-40ff-8fed-037b5e25b524.png">

함수형 아키텍처와 비슷한 점

- 함수형 코어나 도메인 계층 모두 프로세스 외부 의존성과 통신하지 않는다.
- 애플리케이션 서비스 계층이 해당 통신을 담당한다.
- 파일 시스템이나 데이터베이스에서 원시 데이터를 가져온 다음, 해당 데이터를 상태가 없는 알고리즘이나 도메인 모델에 전달하고 결과를 다시 데이터 저장소에 저장한다.

함수형 아키텍처와 차이점

- 사이드 이펙트에 대한 처리
    - 함수형 코어는 어떠한 사이트 이펙트도 일으키지 않는다.
    - 도메인 모델은 사이드 이펙트를 일으키지만, 이러한 모든 사이드 이펙트는 도메인 모델 내부에 남아있다.
    - 컨트롤러가 User 객체와 Company 객체를 데이터베이스에 저장할 때만 사이드 이펙트가 도메인 모델의 경계를 넘는다.
    - 사이드 이펙트가 메모리에 남아있기 때문에 테스트 용이성이 크게 향상 된다.
    - 테스트가 프로세스 외부 의존성을 검사할 필요가 없고 통신 기반 테스트에 의존할 필요도 없다.
    - 메모리에 있는 객체의 출력 기반 테스트와 상태 기반 테스트로 모든 검증을 수행할 수 있다.

# 3. 최적의 단위 테스트 커버리지 분석

|  | 협력자가 거의 없음 | 협력자가 많음 |
| --- | --- | --- |
| 복잡도와 도메인 유의성이 높음 | User의 changeEmail(newEmail, company)
Company의 changeNumberOfEmployees(delta)와 isEmailCorporate(email)
CompanyFactory의 create(data) |  |
| 복잡도와 도메인 유의성이 낮음 | User와 Company의 생성자 | UserController의 changeEmail(userId, newEmail) |

### 도메인 계층과 유틸리티 코드 테스트하기

- 좌측 상단 테스트 메서드는 비용 편익 측면에서 최상의 결과를 가져다준다.
- 코드의 복잡도나 도메인 유의성이 높으면 회귀 방지가 뛰어나고 협력자가 거의 없어 유지비도 가장 낮다.

```java
@Test
public void changing_email_from_non_corporate_to_corporate() {
	Company company = new Company("mycorp.com", 1);
	User sut = new User(1, "user@gmail.com", UserType.Customer);

	sut.changeEmail("new@mycorp.com", company);

	Assertion.assertEquals(2, company.numberOfEmployees);
	Assertion.assertEquals("new@mycorp.com", sut.getEmail());
	Assertion.assertEquals(UserType.Employee, sut.getType();
}
```

```java
@ParameterizedTest
@CsvSource({
      "mycorp.com,email@mycorp.com,true",
      "mycorp.com,email@gmail.com,false"
})
public void differentiates_a_corporate_email_from_non_corporate(String domain, String email, boolean expectedResult) {
	Company sut = new Company(domain, 0);

	boolean isEmailCorporate = sut.isEmailCorporate(email);

	Assertion.assertEquals(expectedResult, isEmailCorporate);
}
```

### 나머지 세 사분면에 대한 코드 테스트하기

- 복잡도가 낮고 협력자가 거의 없는 코드는 단순해서 노력을 들일 필요가 없고 테스트는 회귀 방지가 떨어진다.
- 복잡도가 높고 협력자가 많은 코드는 리팩터링으로 제거했으므로 테스트할 것이 없다.

### 전제 조건을 테스트해야 하는가?

- 일반적으로 권장하는 지침은 도메인 유의성이 있는 모든 전제 조건을 테스트한다.

```java
public void changeNumberOfEmployees(int delta) {
	Assert.isTrue(numberOfEmployees + delta >= 0);
	numberOfEmployees += delta;
}
```

- Company 클래스의 불변성에 해당한다.

# 4. 컨트롤러에서 조건부 로직 처리

- 조건부 로직을 처리 하면서 동시에 프로세스 외부 협력자 없이 도메인 계층을 유지 보수하는 것은 까다롭고 절충이 있기 마련이다.
- 비즈니스 로직과 오케스트레이션의 분리는 비즈니스 연산이 세 단계로 있을 때 가장 효과적이다.
    - 저장소에서 데이터 검색
    - 비즈니스 로직 실행하기
    - 데이터를 다시 저장소에 저장

<img width="342" alt="스크린샷 2023-04-22 오후 3 54 27" src="https://user-images.githubusercontent.com/7659412/233800303-31b9a338-db2b-472f-b64d-96523b127cd5.png">

문제점
  - 단계가 명확하지 않은 경우가 많다.
  - 의사 결정 프로세스의 중간 결과를 기반으로 프로세스 외부 의존성에서 추가 데이터를 조회해야 할 수 있다.
  - 프로세스 외부 의존성에 쓰기 작업도 그 결과에 따라 달라진다.

<img width="377" alt="스크린샷 2023-04-22 오후 3 55 41" src="https://user-images.githubusercontent.com/7659412/233800306-8bbaff98-3093-4446-925f-9eaebbe84b0d.png">

세 가지 방법

- 외부에 대한 모든 읽기와 쓰기를 가장자리로 밀어낸다. ‘읽고-결정하고-실행하기’ 구조를 유지하지만 성능이 저하된다. 필요 없는 경우에도 컨트롤러가 프로세스 외부 의존성을 호출한다.
- 도메인 모델에 프로세스 외부 의존성을 주입하고 비즈니스 로직이 해당 의존성을 호출할 시점을 직접 결정할 수 있게 한다.
- 의사 결정 프로세스 단계를 더 세분화하고, 각 단계별로 컨트롤러를 실행한다.

세 가지 특성의 균형을 맞추는 것이 중요하다.

- 도메인 모델 테스트 유의성 : 도메인 클래스의 협력자 수와 유형에 따른 함수
- 컨트롤러 단순성 : 의사 결정 지점이 있는지에 따라 다름
- 성능 : 프로세스 외부 의존성에 대한 호출 수로 정리

위에서 언급한 방법은 세 가지 특성 중 두가지 특성만 갖는다.

- 외부에 대한 모든 읽기와 쓰기를 비즈니스 연산 가장자리로 밀어내기
    - 컨트롤러를 계속 단순하게 하고 프로세스 외부 의존성과 도메인 모델을 분리하지만 성능이 저하된다.
- 도메인 모델에 프로세스 외부 의존성 주입하기
    - 성능을 유지하면서 컨트롤러를 단순하게 하지만, 도메인 모델의 테스트 유의성이 떨어진다.
- 의사 결정 프로세스 단계를 더 세분화하기
    - 성능과 도메인 모델 테스트 유의성에 도움을 주지만 컨트롤러가 단순하지 않다.
    - 세부 단계를 관리하려면 컨트롤러에 의사 결정 지점이 있어야 한다.

<img width="400" alt="스크린샷 2023-04-22 오후 3 59 52" src="https://user-images.githubusercontent.com/7659412/233800315-008c1e51-dd0a-4bf3-8a99-20e876d0f11a.png">

- 대부분의 소프트웨어 프로젝트에서 성능이 매우 중요하므로 첫 번째 방법은 고려할 필요가 없다.
- 두 번째 옵션도 대부분 코드를 지나치게 복잡한 사분면에 넣는다.
- 세 번째 방법은 컨트롤러를 더 복잡하게 만들기 때문에 지나치게 복잡한 사분면에 더 가까워진다.
    - 이 방법은 복잡도를 관리할 수 있는 방법이 있다.

### CanExecute/Execute 패턴 사용

- 컨트롤러의 복잡도가 커지는 것을 완화하는 방법은 비즈니스 로직이 도메인 모델에서 컨트롤러로 유출되는 것을 방지한다.
- 이메일은 사용자가 확인할 때까지만 변경할 수 있다고 하자. 사용자가 확인한 후에 이메일을 변경하려고 하면 오류 메시지가 표시돼야 한다.

```java
public class User {
	private int userId;
	private String email;
	private UserType type;
	private boolean isEmailConfirmed;

	public String changeEmail(String newEmail, Company company) {
		if(isEmailConfirmed) {
			return "Can't change a comfirmed email";
		}

		if(email == newEmail) {
			return;
		}

		UserType newType = isEmailCorporate ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			company.changeNumberOfEmployees(delta);
		}

		email = newEmail;
		type = newType;
	}
}

public class UserController {
	private Database database = new Database();
	private MessageBus messageBus = new MessageBus();

	public String changeEmail(int userId, String newEmail) {
		Object[] userData = database.getUserById(userId);
		User user = UserFactory.create(userData);

		Object[] companyData = database.getCompany();
		Company company = CompanyFactory.create(companyData);

		String error = user.changeEmail(newEmail, company);// 의사 결정
		if(error != null) {
			return error;
		}

		database.saveCompany(company);
		database.saveUser(user);
		messageBus.sendEmailChangeMessage(userId, newEmail);

		return "OK";
	}
}
```

- 컨트롤러가 의사 결정을 하지 않지만, 성능 저하를 감수해야 한다.
- 이메일을 확인해 변경할 수 없는 경우에도 무조건 데이터베이스에서 Company 인스턴스를 검색한다.
- 이는 모든 외부 읽기와 쓰기를 비즈니스 연산 끝으로 밀어내는 예다.

```java
public class User {
	private int userId;
	private String email;
	private UserType type;
	private boolean isEmailConfirmed;

	public void changeEmail(String newEmail, Company company) {
		if(email == newEmail) {
			return;
		}

		UserType newType = isEmailCorporate ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			company.changeNumberOfEmployees(delta);
		}

		email = newEmail;
		type = newType;
	}
}

public class UserController {
	private Database database = new Database();
	private MessageBus messageBus = new MessageBus();

	public String changeEmail(int userId, String newEmail) {
		Object[] userData = database.getUserById(userId);
		User user = UserFactory.create(userData);

		// 의사 결정
		if(user.isEmailConfirmed()) {
			return "Can't change a comfirmed email";
		}

		Object[] companyData = database.getCompany();
		Company company = CompanyFactory.create(companyData);

		user.changeEmail(newEmail, company);

		database.saveCompany(company);
		database.saveUser(user);
		messageBus.sendEmailChangeMessage(userId, newEmail);

		return "OK";
	}
}
```

- 성능은 그대로 유지된다. 그러나 의사 결정 프로세스는 두 부분으로 나뉜다.
    - 이메일 변경 진행 여부 - 컨트롤러에서 수행
    - 변경 시 해야할 일 - User에서 수행
- 도메인 모델의 캡슐화가 떨어진다. 파편화를 방지하기 위해 User에 새 메서드를 둬서, 이 메서드가 잘 실행되는 것을 이메일 변경의 전제 조건으로 한다.

```java
public class User {
	private int userId;
	private String email;
	private UserType type;
	private boolean isEmailConfirmed;

	public void changeEmail(String newEmail, Company company) {
		Assert.isNull(canChangeEmail());

		if(email == newEmail) {
			return;
		}

		UserType newType = isEmailCorporate ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			company.changeNumberOfEmployees(delta);
		}

		email = newEmail;
		type = newType;
	}

	public String canChangeEmail() {
		if(isEmailConfirmed) {
			return "Can't change a confirmed email";
		}
		return null;
	}
} 
```

- 두 가지 중요한 이점이 있다.
    - 컨트롤러는 더 이상 이메일 변경 프로세스를 알 필요가 없다.
    - changeEmail()의 전제 조건이 추가 돼도 먼저 확인하지 않으면 이메일을 변경할 수 없도록 보장한다.

- 이 패턴을 사용하면 도메인 계층의 모든 결정을 통합할 수 있다.
- 이제 컨트롤러에 이메일을 확인할 일이 없기 때문에 더 이상 의사 결정 지점이 없다.
- 컨트롤러에 canChangeEmail() 을 호출하는 if문이 있어도 if문을 테스트할 필요는 없다.
- User 클래스의 전제 조건을 단위 테스트하는 것으로 충분하다.

### 도메인 이벤트를 사용해 도메인 모델 변경 사항 추적

- 도메인 모델을 현재 상태로 만든 단계를 빼기 어려울 수 있다. 어플리케이션에서 정확히 무슨 일이 일어나는지 외부 시스템에 알려야 하기 때문에 이러한 단계를 아는 것이 중요할 수 있다. 컨트롤러에 이러한 책임이 있으면 더 복잡해진다.
- 도메인 모델에서 중요한 변경 사항을 추적하고 비즈니스 연산이 완료된 후 해당 변경 사항을 프로세스 외부 의존성 호출로 변환한다.
  - 도메인 이벤트로 이러한 추적을 구현할 수 있다.
  - 도메인 이벤트는 애플리케이션 내에서 도메인 전문가에게 중요한 이벤트를 말한다. 도메인 전문가에게는 무엇으로 도메인 이벤트와 일반 이벤트를 구별하는지가 중요하다. 도메인 이벤트는 종종 시스템에서 발생하는 중요한 변경 사항을 외부 애플리케이션에 알리는 데 사용된다.
- CRM에서 메시지 버스에 메시지를 보내서 외부 시스템에 변경된 사용자 이메일을 알려줘야 한다. 지금 예제는 이메일이 변경되지 않아도 메시지를 보낸다.
  - 이메일이 같은지 체크하는 부분을 컨트롤러로 옮겨서 해결할 수 있지만 비즈니스 로직이 파편화되는 문제가 있다.
  - 이를 해결하기 위해 도메인 이벤트를 사용한다
  - 구현 관점에서 도메인 이벤트는 외부 시스템에 통보하는 데 필요한 데이터가 포함된 클래스이다.

```java
public class EmailChangedEvent {
	private int userId;
	private String email;
}
```

- 도메인 이벤트는 이미 일어난 일들을 나타내기 때문에 항상 과거 시제로 명명한다.
- 도메인 이벤트는 값이다. 둘 다 불변이고 서로 바꿔서 쓸 수 있다.

```java
public void changeEmail(String newEmail, Company company) {
	Assert.isNull(canChangeEmail());

	if(email == newEmail) {
		return;
	}

	UserType newType = isEmailCorporate ? UserType.Employee : UserType.Customer;

	if(type != newType) {
		int delta = newType == UserType.Employee ? 1 : -1;
		company.changeNumberOfEmployees(delta);
	}

	email = newEmail;
	type = newType;
	emailChangedEvents.add(new EmailChangedEvent(userId, newEmail));
}
```

```java
public void changeEmail(int userId, String newEmail) {
	Object[] userData = database.getUserById(userId);
	User user = UserFactory.create(userData);

	String error = user.canChangeEmail();
	if(error != null)
		return error;

	Object[] companyData = database.getCompany();
	Company company = CompanyFactory.create(companyData);

	user.changeEmail(newEmail, company);

	database.saveCompany(company);
	database.saveUser(user);
	for(EmailChangedEvent ev in user.emailChangedEvents) {
		messageBus.sendEmailChangeMessage(ev.getUserId(), ev.getNewEmail());
	}

	return "OK";
}
```

- 저장 로직이 도메인 이벤트에 의존하지 않으므로 여전히 Company 인스턴스와 User 인스턴스는 무조건 데이터베이스에 저장된다. 데이터베이스의 변경 사항과 메시지 버스의 메시지가 다르기 때문이다.
- CRM을 제외한 어떤 어플리케이션도 데이터베이스에 대한 접근 권한이 없다면 데이터베이스와의 통신은 구현 세부 사항이다. 반면, 메시지 버스와의 통신은 애플리케이션의 식별할 수 있는 동작이다.
- 외부 시스템과의 계약을 지키려면 CRM은 이메일이 변경될 때만 메시지를 메시지 버스에 넣어야 한다.
- 도메인 이벤트로 해법을 일반화 할 수 있다.
- DomainEvent 기초 클래스를 추출해서 모든 클래스에 대해 이 기초 클래스를 참조하게 한다.
- 컨트롤러에서 도메인 이벤트를 수동으로 발송하는 대신, 별도의 이벤트 디스패처를 작성할 수도 있다.
- 대규모 프로젝트에서는 도메인 이벤트를 발송하기 전에 병합하는 메커니즘이 필요할 수 있다.
- 도메인 이벤트는 이벤트 컨트롤러에서 의사 결정 책임을 제거하고 해당 책임을 도메인 모델에 적용함으로써 외부 시스템과의 통신에 대한 단위 테스트를 간결하게 한다.
- 컨트롤러를 검증하고 프로세스 외부 의존성을 목으로 대체하는 대신, 단위 테스트에서 직접 도메인 이벤트 생성을 테스트할 수 있다.

```java
@Test
public void change_email_from_corporate_to_non_corporate() {
	Company company = new Company("mycorp.com", 1);
	User sut = new User(1, "user@mycorp.com", UserType.Employee, false);

	sut.changeEmail("new@gmail.com", company);

	company.numberOfEmployees.should().be(0);
	sut.email.should().be("new@gmail.com");
	sut.type.should().be(UserType.Customer);
	sut.emailChangedEvents.should().equal(new EmailChangedEvent(1, "new@gmail.com"));
}
```

# 5. 결론

- 외부 시스템에 대한 어플리케이션의 사이드 이펙트를 추상화 하는 것
- 비즈니스 연산이 끝날 때까지 이러한 사이드 이펙트를 메모리에 둬서 추상화하고, 프로세스 외부 의존성 없이 단순한 단위 테스트로 테스트할 수 있다.
- 도메인 이벤트는 메시지 버스에서 메시지에 기반한 추상화에 해당한다.
- 도메인 클래스의 변경 사항은 데이터베이스의 향후 수정 사항에 대한 추상화다.
- 추상화할 것을 테스트하기보다 추상화를 테스트하는 것이 더 쉽다.
- 도메인 이벤트와 canExecute/execute 패턴을 사용해 도메인 모델에 모든 의사 결정을 잘 담을 수 있었지만, 항상 그렇게 할 수는 없다.
    - 도메인 모델에 프로세스 외부 의존성을 두지 않고서는 컨트롤러 외부에서 이메일 고유성을 검증할 방법이 없다.
    - 비즈니스 연산 과정을 변경해야 하는 프로세스 외부 의존성의 실패다. 도메인 계층에서 프로세스 외부 의존성을 호출하지 않기 때무에 어디로 갈 것인지에 대한 결정은 도메인 계층에 있을 수 없다. 이 로직은 컨트롤러에 넣고 통합 테스트로 처리해야 한다. 잠재적인 파편화가 있더라도 비즈니스 로직을 오케스트레이션에서 분리하는 것은 많은 가치가 있다. 이렇게 분리하면 단위 테스트 프로세스가 크게 간소화 된다.
- 컨트롤러에 비즈니스 로직이 있는 것을 피할 수 없는 것처럼, 도메인 클래스에서 모든 협력자를 제거할 수 있는 경우는 거의 없다. 프로세스 외부 의존성을 참조하지 않으면 지나치게 복잡한 코드는 아니다.
- 그러나 이러한 협력자와의 상호 작용을 검증하려고 목을 사용하지는 말라. 이러한 상호 작용은 도메인 모델의 식별할 수 있는 동작과 아무런 관련이 없다. 컨트롤러에서 도메인 클래스로 가는 첫 번째 호출만 컨트롤러 목표에 직접적인 연관이 있다. 같은 연산 내에서 인접 도메인 클래스에 대해 수행하는 후속 호출은 모두 구현 세부 사항이다.

<img width="397" alt="스크린샷 2023-04-22 오후 5 03 15" src="https://user-images.githubusercontent.com/7659412/233800319-7018b9ea-22f4-4926-af14-6143dcb1dcca.png">

- 메서드가 클래스의 식별할 수 있는 동작인지 여부는 클라이언트가 누구인지와 클라이언트의 목표가 무엇인지에 달려 있다.
- 식별할 수 있는 동작이 되려면 메서드는 다음 두 가지 기준 중 하나를 충족해야 한다.
    - 클라이언트 목표 중 하나에 직접적인 연관이 있음
    - 외부 어플리케이션에서 볼 수 있는 프로세스 외부 의존성에서 사이드 이펙트가 발생함
- 식별할 수 있는 동작과 구현 세부 사항을 양파의 여러 겹으로 생각하라.
- 외부 계층의 관점에서 각 계층을 테스트하고, 해당 계층이 기저 계층과 어떻게 통신하는지는 무시해라
- 이러한 계층을 하나씩 내려가면서 관점을 바꾸게 된다. 이전에 구현 세부 사항이었던 것이 이제는 식별할 수 있는 동작이 되고, 이는 또 다른 테스트로 다루게 된다.
