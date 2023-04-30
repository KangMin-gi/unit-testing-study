# 1. 통합 테스트는 무엇인가?

### 통합 테스트의 역할

- 단위 테스트의 세 가지 요구 사항
    - 단일 동작 단위를 검증한다.
    - 빠르게 수행된다.
    - 다른 테스트와 별도로 처리한다.
- 통합 테스트
    - 세 가지 요구 사항 중 하나라도 충족하지 못하는 테스트이다.
    - 시스템이 프로세스 외부 의존성과 통합해 어떻게 작동하는지를 검증한다.
    - 컨트롤러 사분면에 속하는 코드를 대상으로 테스트한다.

<img width="540" alt="스크린샷 2023-04-26 오후 9 41 21" src="https://user-images.githubusercontent.com/7659412/235341784-21659b12-19b5-4de1-a375-44b3dbb12926.png">

- 단위 테스트는 도메인 모델을 다루고 통합 테스트는 프로세스 외부 의존성과 도메인 모델을 연결하는 코드를 확인한다.
- 컨트롤러 사분면에 있는 코드를 다루는 테스트가 단위 테스트가 될 수도 있다.
    - 모든 프로세스 외부 의존성을 목으로 대체하면 테스트 간에 공유하는 의존성이 없어져 테스트 속도가 빨라지고 서로 격리될 수 있다.
    - 그러나 대부분의 어플리케이션은 목으로 대체할 수 없는 프로세스 외부 의존성이 있다.
        - 데이터베이스 등 다른 어플리케이션에서 볼 수 없는 의존성

### 다시 보는 테스트 피라미드

- 단위 테스트와 통합 테스트 간의 균형을 유지하는 것이 중요하다.
- 통합 테스트의 장점
    - 코드를 더 많이 거치기 때문에 회귀 방지가 단위 테스트보다 우수하다.
    - 제품 코드와의 결합도가 낮아서 리팩터링 내성이 우수하다.
- 통합 테스트의 단점
    - 프로세스 외부 의존성에 직접 작동하면 느려지고 테스트 유지비가 많이 든다.
        - 프로세스 외부 의존성 운영이 필요하다.
        - 관련된 협력자가 많아서 테스트가 비대해진다.
- 단위 테스트와 통합 테스트의 비율
    - 단위 테스트는 가능한 한 많이 비즈니스 시나리오의 예외 상황을 확인한다.
    - 통합 테스트는 주요 흐름과 단위 테스트가 다루지 못하는 기타 예외 상황을 다룬다.
        - 주요 흐름은 시나리오의 성공적인 실행
        - 예외 상황은 비즈니스 시나리오 수행 중 오류가 발생하는 경우
    - 대부분을 단위 테스트로 전환하면 유지비를 절감할 수 있다.
    - 통합 테스트가 비즈니스 시나리오 당 하나 또는 두 개 있으면 시스템 전체의 정확도를 보장할 수 있다.
    - 단위 테스트와 통합 테스트 사이의 피라미드 같은 비율을 만든다.
        - 테스트 피라미드는 프로젝트의 복잡도에 따라 모양이 다를 수 있다.
            - 단순한 애플리케이션은 도메인 모델과 알고리즘 사분면에 코드가 거의 없어 테스트 구성이 피라미드 대신 직사각형 모양이 된다.
            - 아주 단순한 경우, 어떠한 단위 테스트도 없다.
            - 통합 테스트는 단순한 애플리케이션에도 가치가 있다.
                - 다른 서브 시스템과 통합해 어떻게 작동하는지 확인하는 것이 더 중요하다.

<img width="561" alt="스크린샷 2023-04-26 오후 9 44 36" src="https://user-images.githubusercontent.com/7659412/235341791-374ed3e0-0e6d-4481-a76f-d51584bdf273.png">

### 통합 테스트와 빠른 실패

- 통합 테스트는 비즈니스 시나리오 당 하나의 주요 흐름과 단위 테스트로 처리할 수 없는 모든 예외 상황을 다룬다.
    - 프로세스 외부 의존성과의 상호 작용을 모두 확인하려면 가장 긴 주요 흐름을 선택한다.
    - 모든 상호 작용을 거치는 흐름이 없으면 외부 시스템과의 통신을 모두 확인하는 데 필요한 만큼 통합 테스트를 추가로 작성한다.
    - 어떠한 예외 상황에 잘못 실행돼 전체 애플리케이션이 즉시 실패하면 해당 예외 상황은 테스트할 필요가 없다.

```java
// User 클래스
public void changeEmail(String newEmail, Company company) {
	Precondition.requires(canChangeEmail() == null);
	/* 메서드의 나머지 부분 */
}

// UserController 클래스
public String changeEmail(int userId, String newEmail) {
	Object[] userData = _database.getUserById(userId);
	User user = UserFactory.create(userData);

	String error = user.canChangeEmail();
	if(error != null) { // 예외 상황
		return error;
	}

	/* 메서드의 나머지 부분 */
}
```

- UserController는 canChangeEmail()을 호출하고 해당 메서드가 오류를 반환하면 연산을 중단한다.
- UserController가 canChangeEmail()을 참조하지 않고 이메일을 변경하려고 하면 애플리케이션이 충돌한다.
- 처음 실행으로 버그가 드러나므로 쉽게 알아차리고 고칠 수 있고 데이터 손상으로 이어지지 않는다.
- UserController에서 canChangeEmail() 을 호출하는 것과 달리, User에서 사전 조건이 있는지를 테스트 해야 한다.
    - 단위 테스트가 더 낫고 통합 테스트는 필요 없다.
- 버그를 빨리 나타나게 하는 것을 빠른 실패 원칙 이라고 하며, 통합 테스트에서 할 수 있는 대안이다.
- 좋지 않은 테스트를 작성하는 것보다는 테스트를 작성하지 않는 것이 좋다.
- 빠른 실패 원칙
    - 예기치 않은 오류가 발생하자마자 현재 연산을 중단하는 것을 의미한다.
    - 어플리케이션의 안정성을 높이는데 도움이 된다.
        - 피드백 루프 단축
            - 버그를 빨리 발견할수록 더 쉽게 해결할 수 있다.
            - 이미 운영 환경으로 넘어온 버그는 개발 중에 발견된 버그보다 수정 비용이 훨씬 더 크다.
        - 지속성 상태 보호
            - 버그는 어플리케이션 상태를 손상시킨다.
            - 손상된 상태가 데이터베이스로 침투하면 고치기가 훨씬 어려워진다.
            - 빨리 실패하면 손상이 확산되는 것을 막을 수 있다.
    - 예외가 빠른 실패 원칙에 완벽히 부합되기 때문에 보통 예외를 던져서 현재 연산을 중지한다.
    - 빠른 실패 원칙의 예시
        - 전제 조건
            - 전제 조건의 실패는 어플리케이션 상태에 대해 가정이 잘못 되었다는 것을 의미하는데 이는 항상 버그다.
        - 설정 파일에서 데이터를 읽기
            - 설정 파일의 데이터가 불완전하거나 잘못된 경우 예외가 발생하도록 판독 로직을 구성할 수 있다.
            - 설정 파일에 문제가 있으면 애플리케이션이 시작하지 않도록 만들 수 있다.

# 2. 어떤 프로세스 외부 의존성을 직접 테스트해야 하는가?

- 통합 테스트는 시스템이 프로세스 외부 의존성과 어떻게 통합하는지를 검증한다.
    - 실제 프로세스 외부 의존성을 사용한다.
    - 해당 의존성을 목으로 대체한다.

### 프로세스 외부 의존성의 두 가지 유형

- 관리 의존성
    - 전체를 제어할 수 있는 프로세스 외부 의존성
    - 애플리케이션을 통해서만 접근할 수 있고 해당 의존성과 상호 작용은 외부 환경에서 볼 수 없다.
        - 관리 의존성과의 통신은 구현 세부 사항
    - 관리 의존성과 통신하는 것을 애플리케이션 뿐이기 때문에 하위 호환성을 유지할 필요가 없다.
    - 중요한 것은 시스템의 최종 상태
        - 통합 테스트에서 실제 인스턴스를 사용하면 외부 클라이언트 관점에서 최종 상태를 확인할 수 있다.
    - 데이터베이스 등이 있다.
    - 컬럼 이름을 변경하거나 데이터베이스를 이관하는 등 데이터베이스 리팩터링에도 도움이 된다.
    - 통합 테스트 작성 시 실제 인스턴스를 사용한다.
- 비관리 의존성
    - 전체를 제어할 수 없는 프로세스 외부 의존성
    - 해당 의존성과의 상호 작용을 외부에서 볼 수 있다.
        - 비관리 의존성과의 통신은 식별할 수 있는 동작
    - 하위 호환성을 지켜야 하기 때문에 비관리 의존성에 대한 통신 패턴을 유지해야 한다.
        - 목을 사용하면 모든 가능한 리팩터링을 고려해서 통신 패턴 영속성을 보장할 수 있다.
    - SMTP 서버와 메시지 버스 등이 있다.
    - 통합 테스트 작성시 목으로 대체한다.

### 관리 의존성이면서 비관리 의존성인 프로세스 외부 의존성 다루기

- 데이터베이스와 같이 다른 애플리케이션에서도 접근 가능한 프로세스 외부 의존성은 관리 의존성과 비관리 의존성 모두의 속성을 나타내는 프로세스 외부 의존성이다.
    - 전용 데이터베이스로 시작 했으나 다른 시스템이 같은 데이터베이스의 데이터를 요구하면서 쉽게 통합하기 위해 일부 테이블의 접근 권한을 공유한 경우
    - 시스템 간의 통합을 구현하는 데 데이터베이스를 사용하면 시스템이 서로 결합되고 추가 개발을 복잡하게 만들기 때문에 좋지 않다.
    - API(동기식 통신)이나 메시지 버스(비동기식 통신)을 사용하는 것이 더 낫다.
- 이미 공유 데이터베이스가 있으면 다른 애플리케이션이 볼 수 있는 테이블을 비관리 의존성으로 취급한다.
    - 테이블은 사실상 메시지 버스 역할을 하고 각 행이 메시지 역할을 한다.
    - 테이블을 이용한 통신 패턴이 바뀌지 않도록 하려면 목을 사용한다.
- 나머지 테이블을 관리 의존성으로 처리하고 상호 작용을 검증하지 말고 최종 상태를 확인한다.

<img width="519" alt="스크린샷 2023-04-27 오후 9 25 23" src="https://user-images.githubusercontent.com/7659412/235341797-0d20aa95-09d2-4507-a10f-6f2359b64ef3.png">

### 통합 테스트에서 실제 데이터베이스를 사용할 수 없으면 어떻게 할까?

- 통합 테스트에서 관리 의존성을 실제 버전으로 사용할 수 없는 경우도 있다.
    - 테스트 자동화 환경에 배포할 수 없는 레거시 데이터베이스
        - IT 보안 정책 때문이거나 테스트 데이터베이스 인스턴스를 설정하고 유지하는 비용이 큰 경우
- 관리 의존성을 목으로 대체하면 통합 테스트의 리팩터링 내성이 저하되고 회귀 방지도 떨어지기 때문에 좋지 않다.
    - 데이터베이스가 프로젝트에서 유일한 프로세스 외부 의존성이면 통합 테스트는 회귀 방지에 있어 단위 텟트랑 똑같다.
        - 통합 테스트가 하는 일은 컨트롤러가 어떤 리포지터리 메서드를 호출하는지 검증하는 것뿐이다.
- 데이터베이스를 그대로 테스트할 수 없으면 통합 테스트를 아예 작성하지 말고 도메인 모델의 단위 테스트에만 집중한다.
- 가치가 충분하지 않은 테스트는 테스트 스위트에 있어선 안된다.

# 3. 통합 테스트 : 예제

<img width="666" alt="스크린샷 2023-04-28 오후 8 52 08" src="https://user-images.githubusercontent.com/7659412/235341799-da16557f-6336-4301-80b6-46f26ef0ea3e.png">

### 어떤 시나리오를 테스트할까?

- 통합 테스트에 대한 일반적인 지침은 가장 긴 주요 흐름과 단위 테스트로는 수행할 수 없는 모든 예외 상황을 다루는 것이다.
    - 가장 긴 주요 흐름은 모든 프로세스 외부 의존성을 거치는 것
        - 예제에서 가장 긴 주요 흐름은 기업 이메일에서 일반 이메일로 변경하는 시나리오
            - 데이터베이스에서 사용자와 회사 모두 업데이트 된다.
            - 메시지 버스로 메시지를 보낸다.
    - 단위 테스트로 테스트 하지 않은 예외 상황이 있다.
        - 사용자가 이메일을 이미 확인 했으면 이메일을 변경하려고 할 때 변경할 수 없다고 에러를 내는 시나리오는 테스트 할 필요는 없다.
        - 컨트롤러에 이러한 확인이 없으면 어플리케이션이 빨리 실패하기 때문이다.

### 데이터베이스와 메시지 버스 분류하기

- 통합 테스트를 작성하기 전에 프로세스 외부 의존성을 두 가지로 분류해서 직접 테스트할 대상과 목으로 대체할 대상을 결정한다.
    - 데이터베이스에는 어떤 시스템도 접근할 수 없으므로 관리 의존성이다.
        - 실제 인스턴스를 사용한다.
        - 데이터베이스에 사용자와 회사를 삽입하고 해당 데이터베이스에서 이메일 변경 시나리오를 실행하고 데이터베이스 상태를 검증한다.
    - 메시지 버스는 비관리 의존성이다.
        - 목으로 대체한다.
        - 컨트롤러와 목 간의 상호 작용을 검증한다.

### 엔드 투 엔드 테스트는 어떤가?

- 엔드 투 엔드 테스트
    - API로 시나리오를 엔드 투 엔드 테스트하는 경우 모두 작동하는 버전의 API로 테스트하게 되고 어떤 프로세스 외부 의존성도 목으로 대체하지 않는다.
- 통합 테스트
    - 동일한 프로세스 내에서 애플리케이션을 호스팅하고 비관리 의존성을 목으로 대체한다.
- 엔드 투 엔드 테스트의 사용 여부는 각자의 판단에 맡긴다.
    - 대부분 통합 테스트 범주에 관리 의존성을 포함하고 비관리 의존성을 목으로 대체하면 보호 수준이 엔드 투 엔드 테스트와 비슷해지므로 엔드 투 엔드 테스트는 생략이 가능하다.
    - 배포 후 프로젝트의 상태 점검을 위해 한 개 또는 두 개 정도의 중요한 엔드 투 엔드 테스트는 작성 가능하다.
    - 테스트가 가장 긴 주요 흐름을 거치게 해서 모든 프로세스 외부 의존성과 올바르게 통신할 수 있게 해야 한다.
    - 외부 클라이언트의 동작을 모방하려면 메시지 버스는 직접 확인하고 데이터베이스 상태는 어플리케이션을 통해서 검증한다.

### 통합 테스트 : 첫 번째 시도

```java
@SpringBootTest
public void changing_email_from_corporate_to_non_corporate() {
	// 준비
	Database db = new Database(connectionString);
	User user = createUser("user@mycorp.com", UserType.Employee, db);
	createCompany("mycorp.com", 1, db);

	IMessageBus messageBusMock = Mockito.mock(IMessageBus.class);
	var sut = new UserController(db, messageBusMock);

	// 실행
	String result = sut.changeEmail(user.getUserId(), "new@gmail.com");

	// 검증
	Assertions.assertEqual("OK", result);

	Object[] userData = db.getUserById(user.getUserId());
	User userFromDb = UserFactory.create(userData);
	Assertions.assertEqual("new@gmail.com", userFromDb.getEmail());
	Assertions.assertEqual(UserType.Customer, userFromDb.getType());

	Object[] companyData = db.getCompany();
	Company companyFromDb = CompanyFactory.create(companyData);
	Assertions.assertEqual(0, companyFromDb.getNumberOfEmployees());

	Mockito.verify(messageBusMock, Mockito.times(1))
					.sendEmailChangedMessage(user.getUserId(), "new@gmail.com");
}
```

- 준비 구절에서 사용자와 회사를 데이터베이스에 삽입하지 않고, createUser, createCompany 헬퍼 메서드를 호출한다.
이러한 메서드는 통합 테스트에서 재사용할 수 있다.
- 입력 매개변수로 사용한 데이터와 별개로 데이터베이스 상태를 확인하는 것이 중요하다.
- 검증 구절에서 사용자와 회사 데이터를 각각 조회하고, 새로운 userFromDb, companyFromDb 인스턴스를 생성한 후에 해당 상태를 검증한다.
- 테스트가 데이터베이스에 대해 읽기와 쓰기를 모두 수행하므로 회귀 방지를 최대로 얻을 수 있다.
- 읽기는 컨트롤러에서 내부적으로 사용하는 동일한 코드를 써서 구현해야 한다.

# 4. 의존성 추상화를 위한 인터페이스 사용

### 인터페이스와 느슨한 결합

- 많은 개발자가 데이터베이스나 메시지 버스와 같은 프로세스 외부 의존성을 위해 인터페이스를 도입한다.
- 인터페이스를 사용하는 일반적인 이유
    - 프로세스 외부 의존성을 추상화해 느슨한 결합을 달성
    - 기존 코드를 변경하지 않고 새로운 기능을 추가해 공개 폐쇄 원칙(OCP)를 지킨다.
- 단일 구현을 위한 인터페이스는 추상화가 아니고, 해당 인터페이스를 구현하는 구체 클래스보다 결합도가 낮지 않으므로 인터페이스가 추상화되려면 구현이 적어도 두 가지는 있어야 한다.
- 특히 두 번째 이유는 더 기본적인 원칙인 YAGNI(You aren’t gonna need it)을 위반한다.
    - YAGNI(You aren’t gonna need it) : 현재 필요하지 않은 기능에 시간을 들이지 않는다.
    - 기회 비용
        - 현재 비즈니스 담당자에게 필요하지 않은 기능을 개발하는 것은 지금 당장 필요한 기능을 개발해야 하는 시간을 낭비하는 것이고, 추후에 담당자가 기능을 요구할 땐 이미 작성해놓은 코드를 수정해야 한다.
        - 실제 필요에 따라 기능을 구현하는 것이 유리하다.
    - 프로젝트 코드는 적을수록 좋다.
        - 요구 사항이 있는 것도 아닌데 만일을 위해 코드를 작성하면 코드베이스의 소유 비용이 불필요하게 증가한다.
    - 코드를 작성하는 것은 문제를 해결하는 값비싼 방법이므로 해결책에 필요한 코드가 적고 간단할수록 좋다.

### 프로세스 외부 의존성에 인터페이스를 사용하는 이유는 무엇인가?

- 목을 사용하기 위함이다.
- 인터페이스가 없으면 테스트 대역을 만들 수 없으므로 테스트 대상 시스템과 프로세스 외부 의존성 간의 상호 작용을 확인할 수 없다.
- 의존성을 목으로 처리할 필요가 없는 한, 프로세스 외부 의존성에 대한 인터페이스를 두면 안된다.
    - 비관리 의존성만 목으로 처리하므로 비관리 의존성에 대해서만 인터페이스를 사용한다.
    - 관리 의존성은 컨트롤러에 명시적으로 주입하고 해당 의존성을 구체 클래스로 작성한다.
- 구현이 둘 이상인 추상화는 목과 상관없이 인터페이스로 나타낼 수 있지만, 목 대체 이외의 이유로 단일 구현을 위해 인터페이스를 도입하는 것은 YAGNI 원칙 위배다.

```java
public class UserController {
	private final Database _database; // 관리 의존성
	private final IMessageBus _messageBus; // 비관리 의존성

	public UserController(Database database, IMessageBus messageBus) {
		_database = database;
		_messageBus = messageBus;
	}

	public String changeEmail(int userId, String newEmail) {
		/* _database와 _messageBus를 사용하는 메서드 */
	}
}
```

### 프로세스 내부 의존성을 위한 인터페이스 사용

- 프로세스 내부 의존성도 인터페이스 기반인 코드가 있다.
- IUser에 구현이 하나만 있으면 좋지 않은 신호다.
    - 프로세스 외부 의존성과 마찬가지로 도메인 클래스에 대해 단일 구현으로 인터페이스를 도입하는 이유는 목으로 처리하기 위함이다.
    - 도메인 클래스 간의 상호 작용을 확인하면 깨지기 쉬운 테스트가 되고 리팩터링 내성이 떨어진다.

```java
public interface IUser {
	int userId;
	String email;

	String canChangeEmail();
	void changeEmail(String newEmail, Company company);
}
```

# 5. 통합 테스트 모범 사례

- 통합 테스트를 최대한 활용하는 데 도움이 되는 일반적인 지침
    - 도메인 모델 경계 명시
    - 애플리케이션 내 계층 줄이기
    - 순환 의존성 제거

### 도메인 모델 경계 명시

- 도메인 모델은 프로젝트가 해결하고자 하는 문제에 대한 도메인 지식의 모음
- 항상 도메인 모델을 코드베이스에서 명시적이고 잘 알려진 위치에 둬야 한다.
- 도메인 모델에 명시적 경계를 지정하면 코드의 해당 부분을 더 잘보여주고 더 잘 설명할 수 있다.
- 도메인 클래스와 컨트롤러 사이의 명확한 경계로 단위 테스트와 통합 테스트의 차이점을 쉽게 구별할 수 있다.
- 이러한 경계는 별도의 어셈블리 또는 네임스페이스 형태로 나타낸다.

### 애플리케이션 내 계층 줄이기

- 대부분 간접 계층을 추가해서 코드를 추상화하고 일반화하려고 한다.
- 애플리케이션에 추상 계층이 너무 많으면 코드베이스를 탐색하기 어렵고 아주 간단한 연산이라 해도 숨은 로직을 이해하기가 너무 어려워진다.
- 추상화가 지나치게 많으면 단위 테스트와 통합 테스트에도 도움이 되지 않는다.
    - 간접 계층이 많은 코드베이스는 컨트롤러와 도메인 모델 사이에 명확한 경계가 없는 편이다.
    - 각 계층을 따로 검증하려는 경향이 훨씬 강하다.
    - 통합 테스트는 가치가 떨어지고, 각 테스트는 특정 계층의 코드만 실행하고 하위 계층은 목으로 처리한다.
    - 최종 결과는 항상 똑같이 낮은 리팩터링 내성과 불충분한 회귀 방지이다.
- 대부분 백엔드 시스템에서는 도메인 모델, 애플리케이션 서비스, 인프라 계층 세 가지만을 활용한다.
    - 인프라 계층은 보통 도메인 모델에 속하지 않는 알고리즘과 프로세스 외부 의존성에 접근할 수 있는 코드로 구성

### 순환 의존성 제거

- 순환 의존성은 둘 이상의 클래스가 제대로 작동하고자 직간접적으로 서로 의존하는 것이다.
- 순환 의존성의 대표적인 예는 콜백

```java
public class CheckOutService {
	public void checkOut(int orderId) {
		var service = new ReportGenerationService();
		service.generateReport(orderId, this);
		/* 기타 코드 */
	}
}

public class ReportGenerationService {
	public void generateReport(int orderId, CheckOutService checkOutService) {
		/* 생성이 완료되면 checkOutService 호출 */
	}
}
```

- 순환 의존성은 코드를 읽고 이해하려고 할 때 알아야 할 것이 많아서 큰 부담이 된다.
    - 해결책을 찾기 위한 출발점이 명확하지 않기 때문이다.
    - 하나의 클래스를 이해하려면 주변 클래스 그래프 전체를 한 번에 읽고 이해해야 하며, 소규모의 독립된 클래스 조차도 파악하기가 어렵다.
- 순환 의존성은 테스트를 방해한다.
    - 클래스 그래프를 나눠서 동작 단위를 하나 분리하려면 인터페이스에 의해 목으로 처리해야 하는 경우가 많으며, 도메인 모델을 테스트할 때 사용해서는 안된다.
    - 인터페이스를 사용해서 순환 의존성의 문제를 가려도 컴파일 타임에 순환 참조를 제거할 수 있지만 런타임에는 여전히 순환이 있다.
- 제일 좋은 방법은 순환 의존성을 제거하는 것이다.
    - 프로젝트에서 순환 의존성을 모두 제거하는 것은 거의 불가능하지만 서로 의존적인 클래스의 그래프를 가능한 적게 만들면 손상을 최소화 할 수 있다.

```java
public class CheckOutService {
	public void checkOut(int orderId) {
		var service = new ReportGenerationService();
		Report report = service.generateReport(orderId);
		/* 기타 코드 */
	}
}

public class ReportGenerationService {
	public Report generateReport(int orderId) {
		/* ... */
	}
}
```

### 테스트에서 다중 실행 구절 사용

- 테스트에서 두 개 이상의 준비나 실행 또는 검증 구절을 두는 것은 코드 악취이다.
    - 테스트가 여러 가지 동작 단위를 확인해서 테스트의 유지 보수성을 저해한다는 신호이다.
    - 사용자 등록와 사용자 삭제와 같이 두 가지 관련 유즈케이스를 하나의 통합 테스트에서 확인하려고 하면 테스트가 초점을 읽고 순식간에 커질 수 있다.
        - 준비 : 사용자 등록에 필요한 데이터 준비
        - 실행 : 등록 호출
        - 검증 : 등록이 성공 했는지 데이터베이스 조회
        - 실행 : 삭제 호출
        - 검증 : 삭제 성공 했는지 데이터베이스 조회
    - 각 실행을 고유의 테스트로 추출해 테스트를 나누는 것이 좋다.
    - 각 테스트가 단일 동작 단위에 초점을 맞추면 테스트를 더 쉽게 이해하고 필요할 때 수정할 수 있다.
- 예외로 원하는 상태로 만들기 어려운 프로세스 외부 의존성으로 작동하는 테스트가 있다.
    - 사용자를 등록하면 외부 은행 시스템에서 은행 계좌가 만들어진다. 은행에서 샌드박스를 제공하기에 엔드 투 엔드 테스트에서 이 샌드박스를 사용한다.
    - 샌드박스가 너무 느리거나 은행에서 해당 샌드박스에 대한 호출 수를 제한한다.
    - 여러 동작을 하나의 테스트로 묶어서 문제가 있는 프로세스 외부 의존성에 대한 상호 작용 횟수를 줄이는 것이 유리하다.
- 둘 이상의 실행 구절로 테스트를 작성하는 것이 타당한 이유를 생각해보면 프로세스 외부 의존성을 관리하기 어려운 경우이다.
- 단위 테스트는 프로세스 외부 의존성으로 작동하지 않기에 절대로 실행 구절이 여러 개 있어서는 안된다.
- 통합 테스트 조차도 실행 단계를 여러 개로 하는 경우가 드물다.
- 다단계 테스트는 거의 항상 엔드 투 엔드 테스트 범주에 속한다.

# 6. 로깅 기능을 테스트하는 방법

- 로깅은 회색 지대로 테스트에 관해서는 어떻게 해야 할지 분명하지 않다.

### 로깅을 테스트해야 하는가?

- 로깅은 횡단 기능으로 코드베이스 어느 부분에서나 필요로 할 수 있다.
- 로깅은 어플리케이션의 동작에 대해 중요한 정보를 생성하지만 보편적이므로 테스트 노력을 더 들일 가치가 있는지 분명하지 않다.
- 로깅은 텍스트 파일이나 데이터베이스와 같은 프로세스 외부 의존성에 사이드 이펙트를 초래한다.
    - 사이드 이펙트를 고객이나 애플리케이션의 클라이언트 또는 개발자 이외의 다른 사람이 보는 경우 로깅은 식별할 수 있는 동작이므로 반드시 테스트해야 한다.
    - 보는 사람이 개발자 뿐이라면 구현 세부 사항이므로 테스트하면 안된다.
- 지원 로깅 : 지원 담당자나 시스템 관리자가 추적할 수 있는 메시지를 생성
- 진단 로깅 : 개발자가 애플리케이션 내부 상황을 파악할 수 있도록 도움

```java
public class User {
	public void changeEmail(String newEmail, Company company) {
		// 진단 로깅
		_logger.info("Changing email for user " + userId + " to " + newEmail);

		Precondition.requires(canChangeEmail() == null);
		if(email == newEmail)
			return;

		UserType newType = company.isEmailCorporate(newEmail) ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			company.changeNumberOfEmployees(delta);
			// 지원 로깅
			_logger.info("User " + userId + " changed type from " + Type + " to " + newType);
		}

		email = newEmail;
		type = newType;
		emailChangedEvents.add(new EmailChangedEvent(userId, newEmail));

		// 진단 로깅
		_logger.info("Email is changed for user " + userId);
	}
}
```

### 로깅을 어떻게 테스트 해야 하는가?

- 프로세스 외부 의존성이 있기 때문에 테스트에 관한 한 프로세스 외부 의존성에 영향을 주는 다른 기능들과 동일한 규칙 적용한다.
- 애플리케이션과 로그 저장소 간의 상호 작용을 검증 하려면 목을 써야 한다.
    - 지원 로깅은 비즈니스 요구 사항이므로 해당 요구 사항을 코드베이스에 명시적으로 반영한다.
    - 비즈니스에 필요한 모든 지원 로깅을 명시적으로 나열하는 특별한 DomainLogger 클래스를 만들고 ILogger 대신 해당 클래스와의 상호 작용 확인
    - ILogger 인터페이스를 목으로 처리하면 안된다.

```java
public class User {
	public void changeEmail(String newEmail, Company company) {
		_logger.info("Changing email for user " + userId + " to " + newEmail); // 진단 로그

		Precondition.requires(canChangeEmail() == null);
		if(email == newEmail)
			return;

		UserType newType = company.isEmailCorporate(newEmail) ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			company.changeNumberOfEmployees(delta);
			_domainLogger.info("User " + userId + " changed type from " + Type + " to " + newType); // 지원 로그
		}

		email = newEmail;
		type = newType;
		emailChangedEvents.add(new EmailChangedEvent(userId, newEmail));

		_logger.info("Email is changed for user " + userId); // 진단 로그
	}
}

public class DomainLogger implements IDomainLogger {
	private final ILogger _logger;

	public DomainLogger(ILogger logger) {
		_logger = logger;
	}

	public void userTypeHasChanged(int userId, UserType oldType, UserType newType) {
		_logger.info("User " + userId + " changed type from " + oldType + " to " + newType;
	}
}
```

- 진단 로깅은 기존 로거를 사용하지만 지원 로깅은 domainLogger 사용한다.
- 도메인 언어를 사용해서 비즈니스에 필요한 특정 로그 항목을 선언하므로 지원 로깅을 더 쉽게 이해하고 유지 보수할 수 있따.
- 구조화된 로깅 개념과 매우 유사하므로 로그 파일의 후처리와 분석에서 유연성이 크게 향상된다.

- 구조화된 로깅 이해하기
    - 구조화된 로깅은 로그 데이터 캡처와 렌더링을 분리하는 로깅 기술이다.
    - 전통적인 로깅은 간단한 텍스트로 동작, 구조상 결과 로그 파일을 분석하기 어렵다.
    - 구조화된 로깅은 로그 저장소에 구조가 있다.
    - 메시지 템플릿의 해시를 계산하고 해당해시를 입력 매개변수와 결합해 캡처한 데이터 세트를 형성한다.
    - 그 다음에 데이터를 렌더링한다.

- 지원 로깅과 진단 로깅을 위한 테스트 작성
    - DomainLogger에는 프로세스 외부 의존성(로그 저장소)가 있다.
    - User가 해당 의존성과 상호작용 하므로, 비즈니스 로직과 프로세스 외부 의존성과의 통신 간에 분리해야 하는 원칙을 위반한다.

```java
public class User {
	public void changeEmail(String newEmail, Company company) {
		_logger.info("Changing email for user " + userId + " to " + newEmail); // 진단 로그

		Precondition.requires(canChangeEmail() == null);
		if(email == newEmail)
			return;

		UserType newType = company.isEmailCorporate(newEmail) ? UserType.Employee : UserType.Customer;

		if(type != newType) {
			int delta = newType == UserType.Employee ? 1 : -1;
			company.changeNumberOfEmployees(delta);
			addDomainEvent(newTypeChangedEvent(userId, type, newType);
		}

		email = newEmail;
		type = newType;
		addDomainEvent(new EmailChangedEvent(userId, newEmail));

		_logger.info("Email is changed for user " + userId); // 진단 로그
	}
}

public class UserController {
	public String changeEmail(int userId, String newEmail) {
		Object[] userData = _database.getUserById(userId);
		User user = UserFactory.create(userData);
	
		String error = user.canChangeEmail();
		if(error != null) {
			return error;
		}

		Object[] companyData = _datebase.getCompany();
		Company company = CompanyFactory.create(companyData);

		user.changeEmail(newEmail, company);

		_database.saveCompany(company);
		_database.saveUser(user);
		_eventDispatcher.dispatch(user.domainEvents);

		return "OK";
	}
}
```

EventDispatcher는 도메인 이벤트를 프로세스 외부 의존성에 대한 호출로 변환하는 새로운 클래스

- EmailChangedEvent는 _messageBus.sendEmailChangedMessage()로 변환
- UserTypeChangedEvent는 _domainLogger.userTypeHasChanged()로 변환

- 단위 테스트는 테스트 대상 User에 UserTypeChangedEvent 인스턴스를 확인해야 한다.
- 단일 통합 테스트는 목을 써서 DomainLogger와의 상호 작용이 올바른지 확인해야 한다.

진단 로깅은 개발자만을 위한 것이므로 테스트할 필요 없다.

### 로깅이 얼마나 많으면 충분한가?

- 지원 로깅은 비즈니스 요구 사항이므로 질문의 여지가 없지만 진단 로깅은 조절할 수 있다.
- 진단 로깅을 과도하게 사용하면 안된다.
    - 과도한 로깅은 코드를 혼란스럽게 한다.
        - 단위 테스트 관점에서는 좋을 지라도 User 클래스에서는 진단 로깅을 사용하지 않는 것이 좋다.
    - 로그의 신호 대비 잡음 비율이 중요하다.
        - 로그가 많을수록 관련 정보를 찾기 어려워진다.
        - 신호를 최대한으로 늘리고 잡음을 최소한으로 줄인다.
- 도메인 모델에서는 진단 로깅을 절대 사용하지 않는다.

### 로거 인스턴스를 어떻게 전달하는가?

- 정적 메서드 사용
    - 이러한 의존성 획득을 앰비언트 컨텍스트라고 부른다.
    - 안티패턴
    - 두 가지 단점
        - 의존성이 숨어있고 변경하기가 어렵다.
        - 테스트가 더 어려워진다.
    - 가장 큰 단점은 코드의 잠재적인 문제를 가린다.
    - 로거를 도메인 클래스에 명시적으로 주입하는 것이 너무 불편해서 앰비언트 컨텍스트에 의존해야 한다면 문제의 징후이다.
    - 로그를 너무 많이 남기거나 간접 계층으로 사용할 수 있다.

```java
public class User {
	private static final ILogger _logger = LogManager.GetLogger(User.class);

	public void changeEmail(String newEmail, Company company) {
		_logger.info("Change email for user " + userId + " to " + newEmail);
		/* ... */
		_logger.info("Email is changed for user " + userId);
	}
}
```

- 로거를 명시적으로 주입하는 한 가지 방법은 클래스 생성자

```java
public void changeEmail(String newEmail, Company company, ILogger logger) {
	logger.info("Change email for user " + userId + " to " + newEmail);
	/* ... */
	logger.info("Email is changed for user " + userId);
}
```
