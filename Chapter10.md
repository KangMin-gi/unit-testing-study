# 1. 데이터베이스 테스트를 위한 전제 조건

### 데이터베이스를 형상 관리 시스템에 유지

- 데이터베이스 스키마를 일반 코드와 같이 Git 같은 형상 관리 시스템에 저장하는 것이 좋다.
- 모델 데이터베이스를 기준점으로 잡아 모든 스키마 변경 사항을 해당 데이터베이스에 적용한다.
운영 배포할 때 전문 도구를 사용해서 모델 데이터베이스와 운영 데이터베이스를 비교하고 업그레이드하는 스크립트를 만들어 실행하는 경우가 있다.
이는 안티패턴이다.

    <img width="634" alt="스크린샷 2023-05-12 오후 7 51 47" src="https://github.com/KangMin-gi/unit-testing-study/assets/7659412/d5e308a4-89f8-40a8-b5db-63bb5d616e96">
    
- 모델 데이터베이스를 사용하는 것이 좋지 않은 이유
    - 변경 내역 부재
        - 데이터베이스 스키마를 과거의 특정 시점으로 되돌릴 수 없다.
    - 복수의 원천 정보
        - 모델 데이터베이스는 개발 상태에 대한 원천 정보를 둘러싸고 경합하게 된다.
        - Git, 모델 데이터베이스 두 가지로 기준을 두면 부담이 가중된다.
- 데이터베이스를 형상 관리 시스템에 유지 했을 때 장점
    - 원천 정보를 하나로 관리할 수 있다.
    - 일반 코드 변경과 함께 데이터베이스 변경을 추적할 수 있다.
        - 형상 관리 외부에서는 데이터베이스 구조를 수정하면 안된다.

### 참조 데이터도 데이터베이스 스키마다

- 데이터베이스 스키마
    - 테이블, 뷰, 인덱스, 저장 프로시저와 데이터베이스가 어떻게 구성되는 지에 대한 청사진을 형성하는 나머지 모든 것
    - SQL 스크립트 형태로 표현되어 언제든지 최신 데이터베이스 인스턴스를 만들 수 있어야 한다.
- 참조 데이터
    - 애플리케이션이 제대로 작동하도록 미리 채워야 하는 데이터
    - 애플리케이션이 데이터를 수정할 수 있으면 일반 데이터이고, 그렇지 않으면 참조 데이터이다.
    - 데이터베이스 스키마에 속한다.
    - 참조 데이터는 애플리케이션의 필수 사항이므로 데이터베이스 스키마와 함께 SQL INSERT 문 형태로 형상 관리 시스템에 저장한다.

### 모든 개발자를 위한 별도의 데이터베이스 인스턴스

- 공유 데이터베이스를 사용하면 개발 프로세스를 방해하고 테스트를 어렵게 한다.
    - 서로 다른 개발자가 실행한 테스트는 서로 간섭된다.
    - 하위 호환성이 없는 변경으로 다른 개발자의 작업을 막을 수 있다.
- 테스트 실행 속도를 극대화 하려면 개발자마다 별도로 데이터베이스 인스턴스를 사용한다.

### 상태 기반 데이터베이스 배포와 마이그레이션 기반 데이터베이스 배포

- 마이그레이션 기반 방식은 초기에는 구현하고 유지 보수하기가 어렵지만 장기적으로 상태 기반 방식보다 훨씬 효과적이다.
- 상태 기반 방식
    - 개발 내내 유지 보수하는 모델 데이터베이스가 있다.
    - 운영 배포 중에 비교 도구가 스크립트를 생성해서 운영 데이터베이스를 모델 데이터베이스와 비교해 최신 상태로 유지한다.
    - 물리적인 모델 데이터베이스는 원천 데이터가 아니다.
    해당 데이터베이스를 작성하는 데 사용할 수 있는 SQL 스크립트가 형상 관리 시스템에 저장 되어 있다.
- 마이그레이션 기반 방식

    <img width="638" alt="스크린샷 2023-05-12 오후 8 13 21" src="https://github.com/KangMin-gi/unit-testing-study/assets/7659412/0dacd1ae-58d7-47e5-bc67-8611a8c8a0e4">
    
    - 데이터베이스를 어떤 버전에서 다른 버전으로 전환하는 명시적인 마이그레이션을 의미한다.
    - 운영 데이터베이스와 개발 데이터베이스를 자동으로 동기화하기 위한 도구를 쓸 수 없고 업그레이드 스크립트를 직접 작성해야 한다.
        - 운영 데이터베이스 스키마에서 문서화되지 않은 변경 사항을 발견할 때 데이터베이스 비교 도구가 아직 유용하다.
    - 형상 관리에 저장하는 산출물은 데이터베이스 상태가 아닌 마이그레이션이다.
    마이그레이션은 일반적으로 평이한 SQL 스크립트로 표시하지만, SQL로 변환할 수 있는 DSL 같은 언어를 사용해 작성할 수도 있다.

```csharp
[Migration(1)]
public class CreateUserTable : Migration {
	public override void Up() {
		Create.Table("Users");
	}

	public override void Down() {
		Delete.Table("Users");
	}
}
```

### 상태 기반 방식보다 마이그레이션 기반 방식을 선호하라

- 상태 기반 방식은 상태를 형상 관리에 저장함으로써 상태를 명시하고 비교 도구가 마이그레이션을 암묵적으로 제어할 수 있게 한다.
- 마이그레이션 기반 방식은 마이그레이션을 명시적으로 하지만 상태를 암묵적으로 둔다.
데이터베이스 상태를 직접 볼 수 없으며 마이그레이션으로 조합해야 한다.

|  | 데이터베이스 상태 | 마이그레이션 메커니즘 |
| --- | --- | --- |
| 상태 기반 방식 | 명시적 | 암묵적 |
| 마이그레이션 기반 방식 | 암묵적 | 명시적 |
- 데이터베이스 상태가 명확하면 병합 충돌을 처리하기가 수월하지만, 명시적 마이그레이션은 데이터 모션 문제를 해결하는 데 도움이 된다.
    - 데이터 모션은 새로운 데이터베이스 스키마를 준수하도록 기존 데이터의 형태를 변경하는 과정이다.
    - 대부분의 프로젝트에서 데이터 모션이 병합 충돌보다 중요하다.
- 마이그레이션 방식 사용시 주의 점
    - 마이그레이션을 통해 데이터베이스 스키마에 모든 수정 사항을 적용하고 형상 관리에 마이그레이션이 커밋된 후에는 수정하면 안된다.
    - 마이그레이션이 잘못된 경우 이전 마이그레이션을 수정하는 대신 새 마이그레이션을 생성해야 한다.

# 2. 데이터베이스 트랜잭션 관리

- 데이터베이스 트랜잭션 관리는 제품 코드와 테스트 코드 모두 중요하다.
    - 제품 코드에서는 트랜잭션 관리를 적절하게 하면 데이터 모순을 피할 수 있다.
    - 테스트 코드에서는 운영 환경에 근접한 설정으로 데이터베이스 통합을 검증하는데 도움이 된다.

### 제품 코드에서 데이터베이스 트랜잭션 관리하기

```java
public class Database {
	private final String _connectionString;

	public Database(String connectionString) {
		_connectionString = connectionString;
	}

	public void saveUser(User user) {
		boolean isNewUser = user.getUserId() == 0;
		try(Connection connection = new SqlConnection(_connectionString)) { // 트랜잭션 개방
			/* user 생성 또는 업데이트 */
		}
	}

	public void saveCompany(Company company) {
		try(Connection connection = new SqlConnection(_connectionString)) { // 트랜잭션 개방
			/* company 업데이트 */
		}
	}
}

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

	// 4개의 데이터베이스 트랜잭션 생성
	public String changeEmail(int userId, String newEmail) {
		Object[] userData = _database.getUserById(userId); // 트랜잭션 개방
		User user = UserFactory.create(userData);
	
		String error = user.canChangeEmail();
		if(error != null) {
			return error;
		}

		Object[] companyData = _database.getCompany(); // 트랜잭션 개방
		Company company = CompanyFactory.create(companyData);

		user.changeEmail(newEmail, company);

		_database.saveCompany(company); // 트랜잭션 개방
		_database.saveUser(user); // 트랜잭션 개방
		_eventDispatcher.dispatch(user.getDomainEvents());

		return "OK";
	}
}
```

- 읽기 전용 연산 중에는 여러 트랜잭션을 열어도 괜찮지만 데이터 변경이 포함된다면 모순을 피하기 위해 연산에 포함된 모든 업데이트는 원자적이어야 한다.

### 데이터베이스 트랜잭션에서 데이터베이스 연결 분리하기

- 결정 유형을 두 가지로 분리
    - 업데이트할 데이터
    - 업데이트 유지 또는 롤백 여부
- Database 클래스를 리포지토리와 트랜잭션으로 나눠서 이러한 책임을 구분할 수 있다.
    - 리포지토리
        - 데이터베이스의 데이터에 대한 접근과 수정을 가능하게 하는 클래스
        - 데이터베이스 호출이 완료되는 즉시 리포지토리 폐기
        - 리포지토리는 항상 트랜잭션 위에서 작동
    - 트랜잭션
        - 데이터 업데이트를 완전히 커밋하거나 롤백하는 클래스
        - 전체 비즈니스 연산 동안 있으며 연산이 끝나면 폐기

```java
public class UserController {
	private final Transaction _transaction;
	private final UserRepository _userRepository;
	private final CompanyRepository _companyRepository;
	private final EventDispatcher _eventDispatcher;

	public UserController(
		Transaction transaction,
		MessageBus messageBus,
		IDomainLogger domainLogger) {
		_transaction = transaction;
		_userRepository = new UserRepository(transaction);
		_companyRepository = new CompanyRepository(transaction);
		_eventDispatcher = new EventDispatcher(messageBus, domainLogger);
	}

	public String changeEmail(int userId, String newEmail) {
		Object[] userData = _userRepository.getUserById(userId);
		User user = UserFactory.create(userData);
	
		String error = user.canChangeEmail();
		if(error != null) {
			return error;
		}

		Object[] companyData = _companyRepository.getCompany();
		Company company = CompanyFactory.create(companyData);

		user.changeEmail(newEmail, company);

		_userRepository.saveCompany(company);
		_companyRepository.saveUser(user);
		_eventDispatcher.dispatch(user.getDomainEvents());

		_transaction.commit();

		return "OK";
	}
}

public class UserRepository {
	private final Transaction _transaction;

	public UserRepository(Transaction transaction) {
		_transaction = transaction;
	}

	/* ... */
}

public class Transaction implements IDisposable {
	public void commit() {}
	public void dispose() {}
}
```

- commit()은 트랜잭션을 성공으로 표시한다.
비즈니스 연산이 성공하고 모든 데이터 수정을 저장할 준비가 된 경우에만 호출한다.
- dispose()는 트랜잭션을 종료한다.
비즈니스 연산이 끝날 때 항상 호출된다.
이전에 commit()이 호출된 경우 모든 업데이트를 저장하고 그렇지 않으면 롤백한다.
- 주요 흐름 동안에만 데이터베이스가 변경되도록 한다.

### 작업 단위로 트랜잭션 업그레이드하기

- Transaction 클래스를 작업 단위로 업그레이드 할 수 있다.
- 작업 단위에는 비즈니스 연산의 영향을 받는 객체 목록이 있다.
작업이 완료되면 작업 단위는 데이터베이스를 변경하기 위해 해야 하는 업데이트를 모두 파악하고 이러한 업데이트를 하나의 단위로 실행한다.
- 작업 단위의 장점은 업데이트 지연이다.
트랜잭션과 달리 작업 단위는 비즈니스 연산 종료 시점에 모든 업데이트를 실행하므로 데이터베이스 트랜잭션의 기간을 단축하고 데이터 혼잡을 줄인다.
- 데이터베이스 트랜잭션, ORM 라이브러리는 작업 단위 패턴도 구현한다.

```java
public class UserController {
	private final CrmContext _context;
	private final UserRepository _userRepository;
	private final CompanyRepository _companyRepository;
	private final EventDispatcher _eventDispatcher;

	public UserController(
		Context context,
		MessageBus messageBus,
		IDomainLogger domainLogger) {
		_context = context;
		_userRepository = new UserRepository(context);
		_companyRepository = new CompanyRepository(context);
		_eventDispatcher = new EventDispatcher(messageBus, domainLogger);
	}

	public String changeEmail(int userId, String newEmail) {
		User user = _userRepository.getUserById(userId);
	
		String error = user.canChangeEmail();
		if(error != null) {
			return error;
		}

		Company company = _companyRepository.getCompany();

		user.changeEmail(newEmail, company);

		_userRepository.saveCompany(company);
		_companyRepository.saveUser(user);
		_eventDispatcher.dispatch(user.getDomainEvents());

		_context.saveChanges(); // CrmContext가 트랜잭션을 대체함

		return "OK";
	}
}
```

- 엔티티 프레임워크가 원시 데이터베이스 데이터와 도메인 개체 사이의 매퍼 역할을 하므로 UserFactory, CompanyFactory는 더 이상 필요하지 않다.

### 통합 테스트에서 데이터베이스 트랜잭션 관리하기

```java
@Test
public void changing_email_from_corporate_to_non_corporate() {
	try(CrmContext context = new CrmContext(connectionString) {
		// 준비 구절에서 컨텍스트 사용
		UserRepository userRepository = new UserRepository(context);
		CompanyRepository companyRepository = new CompanyRepository(context);
		User user = new User(0, "user@mycorp.com", UserType.Employee, false);
		userRepository.saveUser(user);
		Company company = new Company("mycorp.com", 1);
		companyRepository.saveCompany(company);
		context.saveChanges();

		BusSpy busSpy = new BusSpy();
		MessageBus messageBus = new MessageBus(busSpy);
	  IDomainLogger loggerMock = Mockito.mock(IDomainLogger.class);
		UserController sut = new UserController(context, messageBusMock, loggerMock);

		String result = sut.changeEmail(user.getUserId(), "new@gmail.com");

		Assertions.assertEquals("OK", result);

		User userFromDb = userRepository.getUserById(user.getUserId());
		Assertions.assertEquals("new@gmail.com", userFromDb.getEmail());
	  Assertions.assertEquals(UserType.Customer, userFromDb.getType());
	
		Company companyFromDb = companyRepository.getCompany();
		Assertions.assertEquals(0, companyFromDb.getNumberOfEmployees());
	
		busSpy.shouldSendNumberOfMessages(1).withEmailChangedMessage(user.getUserId(), "new@gmail.com");
		Mockito.verify(loggerMock, Mockito.times(1)).userTypeHasChanged(user.getUserId(), UserType.Employee, UserType.Customer);
	}
}
```

- 준비, 실행, 검증 세 구절에서 작업 단위를 재사용 하는 것은 운영 환경과 다른 환경을 만들기 때문에 문제가 된다.
    - 운영 환경에서는 각 비즈니스 연산에 CrmContext 전용 인스턴스가 있다. 
    컨트롤러 메서드 호출 직전에 생성되고 직후에 폐기된다.
    - 동작 모순에 빠지지 않기 위해 통합 테스트를 가능한 한 운영 환경에서와 비슷하게 해야 한다.
    - 통합 테스트에서 적어도 세 개의 트랜잭션 또는 작업 단위(준비, 실행, 검증)를 사용해야 한다.

# 3. 테스트 데이터 생명 주기

- 공유 데이터베이스를 사용하면 통합 테스트를 서로 분리할 수 없는 문제가 있다.
    - 통합 테스트를 순차적으로 실행해야 한다.
    - 테스트 실행 간에 남은 데이터를 제거해야 한다.
- 테스트는 데이터베이스 상태에 따라 달라지면 안된다. 테스트는 데이터베이스 상태를 원하는 조건으로 만들어야 한다.

### 병렬 테스트 실행과 순차적 테스트 실행

- 통합 테스트를 병렬로 실행하려면 모든 테스트 데이터가 고유한지 확인해야 한다.
    - 데이터베이스 제약 조건을 위반하지 않고 테스트가 다른 테스트 후에 입력 데이터를 잘못 수집하는 일이 생기지 않는다.
    - 남은 데이터를 정리하는 것도 어렵다.
- 성능 향상을 위해 시간을 허비하지 않고 순차적으로 통합 테스트를 실행하는 것이 더 실용적이다.
    - 단위 테스트는 테스트를 병렬로 처리하고 통합 테스트는 병렬 처리를 비활성화 한다.
- 컨테이너를 사용해 테스트를 병렬 처리할 수 있다.
    - 유지 보수 부담이 너무 커진다.
        - 도커 이미지를 유지 보수해야 한다.
        - 각 테스트마다 컨테이너 인스턴스가 있는지 확인해야 한다.
        - 통합 테스트를 일괄 처리한다.
        - 다 사용한 컨테이너는 폐기한다.
    - 통합 테스트의 실행 시간을 최소화해야 하는 경우가 아니라면 컨테이너를 사용하지 않는 것이 좋다.
    - 데이터베이스는 개발자당 하나의 인스턴스만 갖는 것이 더 실용적이다.

### 테스트 실행 간 데이터 정리

- 테스트 실행 간에 남은 데이터를 정리하는 네 가지 방법
    - 각 테스트 전에 데이터베이스 백업 복원하기
        - 데이터 정리 문제를 해결할 수 있지만 속도가 느리다.
        - 컨테이너를 사용하더라도 컨테이너 인스턴스를 제거하고 새 컨테이너를 생성하는 데 초 단위의 시간이 걸리기 때문에 테스트 스위트 실행 시간이 빠르게 늘어난다.
    - 테스트 종료 시점에 데이터 정리하기
        - 빠르지만 정리 단계를 건너뛰기 쉽다.
        - 테스트 도중에 빌드 서버가 중단하거나 디버거에서 테스트를 종료하면 입력 데이터는 데이터베이스에 남아있고 이후 테스트 실행에 영향을 준다.
    - 데이터베이스 트랜잭션에 각 테스트를 래핑하고 커밋하지 않기
        - 테스트와 SUT에서 변경한 모든 내용이 자동으로 롤백된다.
        - 작업 단위를 재사용할 때처럼 추가 트랜잭션으로 인해 운영 환경과 다른 설정이 생성된다.
    - **테스트 시작 시점에 데이터 정리하기**
        - 빠르게 작동하고 일관성이 없는 동작을 일으키지 않고 정리 단계를 실수로 건너뛰지 않는다.
        - 이 방법이 가장 좋다.
- 모든 통합 테스트의 기초 클래스를 두고, 기초 클래스에 삭제 스크립트를 작성한다.

```java
public abstract class IntegrationTests {
	private final String ConnectionString = "...";

	protected IntegrationTests() {
		clearDatabase();
	}

	private void clearDatabase() {
		String query = "DELETE FROM dbo.[User];" +
										"DELETE FROM dbo.Company;";

		try(Connection connection = new SqlConnection(ConnectionString)) {
			Command command = new SqlCommand(query, connection) {
				commandType = commandType.Text
			};

			connection.open();
			command.executeNonQuery();
		}
	}
}
```

- 삭제 스크립트는 일반 데이터를 모두 제거하지만 참조 데이터는 제거하면 안된다.

### 인메모리 데이터베이스 피하기

- 통합 테스트를 서로 분리하기 위해 데이터베이스를 SQLite 같은 인메모리 데이터베이스로 교체할 수 있다.
- 인메모리 데이터베이스의 장점
    - 테스트 데이터를 제거할 필요가 없음
    - 작업 속도 향상
    - 테스트가 실행될 때마다 인스턴스화 가능
- 인메모리 데이터베이스는 공유 의존성이 아니기 때문에 통합 테스트는 컨테이너 접근 방식과 유사한 단위 테스트가 된다.
- 일반 데이터베이스와 기능적으로 일관성이 없기 때문에 사용하지 않는 것이 좋다.
    - 운영 환경과 테스트 환경이 일치하지 않는다.
    - 일반 데이터베이스와 인메모리 데이터베이스의 차이로 인해 테스트에서 거짓 양성 또는 거짓 음성이 발생하기 쉽다.
    - 높은 보호 수준을 기대하기 어렵고, 수동으로 회귀 테스트를 많이 수행해야 한다.
- 테스트에서도 운영 환경과 똑같은 DBMS를 사용해야 한다.

# 4. 테스트 구절에서 코드 재사용하기

### 준비 구절에서 코드 재사용하기

```java
@Test
public void changing_email_from_corporate_to_non_corporate() {
	// 준비
	User user;
	try(CrmContext context = new CrmContext(connectionString) {
		UserRepository userRepository = new UserRepository(context);
		CompanyRepository companyRepository = new CompanyRepository(context);
		User user = new User(0, "user@mycorp.com", UserType.Employee, false);
		userRepository.saveUser(user);
		Company company = new Company("mycorp.com", 1);
		companyRepository.saveCompany(company);
		context.saveChanges();
	}

	BusSpy busSpy = new BusSpy();
	MessageBus messageBus = new MessageBus(busSpy);
  IDomainLogger loggerMock = Mockito.mock(IDomainLogger.class);

	String result;
	try(CrmContext context = new CrmContext(connectionString) {
		UserController sut = new UserController(context, messageBusMock, loggerMock);
		// 실행
		result = sut.changeEmail(user.getUserId(), "new@gmail.com");
	}

	// 검증
	Assertions.assertEquals("OK", result);

	try(CrmContext context = new CrmContext(connectionString) {
		UserRepository userRepository = new UserRepository(context);
		CompanyRepository companyRepository = new CompanyRepository(context);
	
		User userFromDb = userRepository.getUserById(user.getUserId()); //
		Assertions.assertEquals("new@gmail.com", userFromDb.getEmail());
	  Assertions.assertEquals(UserType.Customer, userFromDb.getType());
	
		Company companyFromDb = companyRepository.getCompany(); //
		Assertions.assertEquals(0, companyFromDb.getNumberOfEmployees());
	
		busSpy.shouldSendNumberOfMessages(1).withEmailChangedMessage(user.getUserId(), "new@gmail.com");
		Mockito.verify(loggerMock, Mockito.times(1)).userTypeHasChanged(user.getUserId(), UserType.Employee, UserType.Customer);
	}
}
```

- 코드를 재사용하기 위해 비공개 팩토리 메서드를 도입한다.

```java
private User createUser(String email, UserType type, boolean isEmailConfirmed) {
	try(CrmContext context = new CrmContext(connectionString) {
		User user = new User(0, email, type, isEmailConfirmed);
		UserRepository userRepository = new UserRepository(context);
		userRepository.saveUser(user);

		context.saveChanges();

		return user;
	}
}
```

- 이런 패턴을 오브젝트 마더라고 한다.
    - 오브젝트 마더는 테스트 픽스처(테스트 실행 대상)을 만드는 데 도움이 되는 클래스 또는 메서드다.
- 준비 구절에서 코드를 재사용하는 목표를 달성하는 데 도움이 되는 패턴으로 테스트 데이터 빌더도 있다.
    - 오브젝트 마더와 유사하게 작동하지만 일반 메서드 대신 플루언트 인터페이스를 제공한다.
    - 테스트 가동성을 약간 향상시키지만 상용구가 너무 많이 필요하다.
- 기본적으로 팩토리 메서드는 동일한 클래스에 배치하고 코드 복제가 중요한 문제가 될 경우에만 별도의 헬퍼 클래스로 이동한다.

### 실행 구절에서 코드 재사용하기

- 모든 통합 테스트의 실행 구절에서는 데이터베이스 트랜잭션이나 작업 단위를 만든다.
- 어떤 컨트롤러의 기능을 호출해야 하는지에 대한 정보가 있는 대리자를 받는 메서드를 도입하면 실행 구절을 줄일 수 있다.

```java
// 데코레이터 메소드
private String execute(Function<UserController, String> func, MessageBus messageBus, IDomainLogger logger) {
	try(CrmContext context = new CrmContext(connectionString)) {
		UserController controller = new UserController(context, message, logger);
		return func(controller);
	}
}

String result = execute(x -> x.changeEmail(user.getUserId(), "new@gmail.com"), messageBus, loggerMock);
```

### 검증 구절에서 코드 재사용하기

```java
User userFromDb = QueryUser(user.getUserId()); // 새 헬퍼 메서드
Assertions.assertEquals("new@gmail.com", userFromDb.getEmail());
Assertions.assertEquals(UserType.Customer, userFromDb.getType());
	
Company companyFromDb = QueryCompany(); // 새 헬퍼 메서드
Assertions.assertEquals(0, companyFromDb.getNumberOfEmployees());
```

- 헬퍼 메서드를 사용해서 코드를 재사용 할 수 있다.

```java
public static class UserExtensions {
	public static User shouldExist(this User user) {
		Assertions.assertNotNull(user);
		return user;
	}

	public static User withEmail(this User user, String email) {
		Assertions.assertEquals(email, user.getEmail());
		return user;
	}
}

User userFromDb = QueryUser(user.getUserId());
userFromDb.shouldExist().withEmail("new@gmail.com").withType(UserType.Customer);
	
Company companyFromDb = QueryCompany();
companyFromDb.shouldExist().withNumberOfEmployees();
```

- 플루언트 인터페이스를 확장 메서드로 구현할 수 있다.

### 테스트가 데이터베이스 트랜잭션을 너무 많이 생성하는가?

- 통합 테스트를 간결하게 하면 더 읽기 쉽고 유지 보수가 용이하다.

```java
public class UserControllerTests extends IntegrationTests {
	@Test
	public void changing_email_from_corporate_to_non_corporate() {
		// 준비
		User user = CreateUser("user@mycorp.com", UserType.Employee);
		CreateCompany("mycorp.com", 1);
	
		BusSpy busSpy = new BusSpy();
		MessageBus messageBus = new MessageBus(busSpy);
	  IDomainLogger loggerMock = Mockito.mock(IDomainLogger.class);
	
		String result = execute(x => x.changeEmail(user.getUserId(), "new@gmail.com"), messageBus, loggerMock);
	
		// 검증
		Assertions.assertEquals("OK", result);
	
		User userFromDb = QueryUser(user.getUserId());
		userFromDb.shouldExist().withEmail("new@gmail.com").withType(UserType.Customer);
	
		Company companyFromDb = QueryCompany();
		companyFromDb.shouldExist().withNumberOfEmployees();

		busSpy.shouldSendNumberOfMessages(1).withEmailChangedMessage(user.getUserId(), "new@gmail.com");
		Mockito.verify(loggerMock, Mockito.times(1)).userTypeHasChanged(user.getUserId(), UserType.Employee, UserType.Customer);
	}
}
```

- 데이터베이스 트랜잭션이 세 개에서 다섯 개로 늘어났다.
- 트랜잭션 개수가 늘어나서 테스트가 느려지지만, 이는 빠른 피드백과 유지 보수성 간의 절충이다.
- 유지 보수성을 위해 성능을 양보함으로써 절충하는 것이 좋다.

# 5. 데이터베이스 테스트에 대한 일반적인 질문

### 읽기 테스트를 해야 하는가?

- 쓰기 작업은 위험성이 높기 때문에 철저히 테스트하는 것은 매우 중요하다.
    - 쓰기가 잘못되면 데이터가 손상돼 데이터베이스뿐만 아니라 외부 애플리케이션에도 영향을 미칠 수 있다.
- 읽기 작업의 버그는 보통 해로운 문제가 없다.
- 읽기 테스트의 임계치는 쓰기 테스트의 임계치보다 높아야 한다.
- 읽기에는 추상화 계층이 거의 없기 때문에 단위 테스트는 아무 소용이 없다.
읽기를 테스트하기로 결정한 경우에는 실제 데이터베이스에서 통합 테스트를 한다.

<img width="597" alt="스크린샷 2023-05-12 오후 9 25 48" src="https://github.com/KangMin-gi/unit-testing-study/assets/7659412/abd08886-dfe0-4cde-8345-431f3e28014c">

### 리포지토리 테스트를 해야 하는가?

- 리포지토리는 데이터베이스 위에 유용한 추상화를 제공한다.
- 리포지토리가 도메인 객체를 어떻게 데이터베이스에 매핑하는지 테스트 하는 것은 유지비가 높고 회귀 방지가 떨어져 테스트 스위트에 손실이 된다.
- **높은 유지비**
    - 리포지토리는 복잡도가 거의 없고 프로세스 외부 의존성인 데이터베이스와 통신하므로 컨트롤러 사분면에 포함된다.
    - 프로세스 외부 의존성이 있으면 테스트의 유지비가 증가한다.
- **낮은 회귀 방지**
    - 리포지토리는 그렇게 복잡하지 않고 회귀 방지에서 일반적인 통합 테스트가 주는 이점과 겹친다.
    - 리포지토리를 테스트하기에 가장 좋은 방법은 리포지토리가 갖고 있는 약간의 복잡도를 별도의 알고리즘으로 추출하고 해당 알고리즘 전용 테스트를 작성하는 것이다.
    - ORM을 사용하면 데이터 매핑과 데이터베이스 상호작용 간의 분리는 불가능하다.
        - 리포지토리는 직접 테스트하지 말고 포괄적인 통합 테스트 스위트의 일부로 취급한다.
    - EventDispatcher도 별도로 테스트하지 않는다.
        - 목 체계가 복잡해서 유지비가 너무 많이 들고 회귀 방지의 이점은 너무 적다.
