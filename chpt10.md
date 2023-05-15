# 데이터베이스 테스트

## 데이터베이스 테스트를 위한 전제 조건
- 형상 관리 시스템에 데이터베이스 유지
- 각각의 개발자를 위한 별도 데이터베이스 인스턴스 사용
- 데이터베이스 배포에 마이그레이션 기반 방식 적용

### 데이터베이스를 형상 관리 시스템에 유지
- 데이터베이스 스키마를 일반 코드로 취급
	- 데이터베이스 스키마를 Git 과 같은 형상관리 시스템에 저장하자
- 데이터베이스 스키마를 모델 데이터베이스로 관리하는 것은 안티패턴
	- 스키마 변경 history 를 알 수 없음. 즉, 스키마를 과거 특정 시점으로 되돌릴 수 없음
	- 복수 개의 source 정보. Git, 모델 데이터베이스 양쪽에 개발 상태를 표시하게 됨.
- 데이터베이스 스키마 업데이트를 Git 에 두면 데이터베이스 변경을 추적할 수 있고, source 정보를 하나로 관리 가능
- (참고) NoSQL 에서는 model 클래스가 곧 schema 역할을 하므로 데이터베이스 스키마가 자연스럽게 Git 에 저장될 듯?

### 참조 데이터도 데이터베이스 스키마다
- 데이터베이스 스키마는 SQL 스크립트 형태로 표현된다.
- 참조 데이터
	- 어플리케이션이 제대로 작동하기 위해 미리 채워야 하는 데이터
- 참조데이터 vs 일반데이터 구별 방법
	- 참조데이터
		- 어플리케이션이 데이터 수정 불가능
	- 일반데이터
		- 어플리케이션이 데이터 수정 가능
- 참조데이터 또한 SQL `INSERT` 문 형태로 GIT 에 저장하자

### 모든 개발자를 위한 별도의 데이터베이스 인스턴스
- 공유 DB 로 테스트하면 테스트간 간섭으로 개발 프로세스가 느려질 수 있음
- 테스트 실행 속도를 극대화하려면 개발자마다 각각의 데이터베이스 인스턴스를 사용하라


### 상태기반 DB 배포와 마이그레이션 기반 DB 배포
- 마이그레이션 기반 방식이 구현하기 어렵고 유지보수하기 힘들지만 장기적으로 보았을 때 훨씬 효과적이다.

#### 상태 기반 방식
- 개발 동안 유지보수하는 모델 데이터베이스가 있음
- DB 배포 시 비교 도구가 스크립트를 생성해서 운영 DB 를 모델 DB 와 동일하도록 최신 상태로 유지함
	- 비교 도구가 모델 데이터베이스와 동기화하는데 필요한 모든 작업을 수행
		- e.g. 불필요한 테이블 삭제, 새 테이블 생성 ...
#### 마이그레이션 기반 방식
- 데이터베이스를 어떤 버전에서 다른 버전으로 전환하는 명시적인 마이그레이션을 의미
- upgrade script 를 직접 작성

![](attachments/스크린샷%202023-05-14%20오후%202.41.50.png)

- 상태기반과 달리 DB 상태가 아니라 마이그레이션 자체가 Git 에 저장됨
	- 마이그레이션은 SQL 혹은 SQL 로 변환할 수 있는 DSL 을 사용해 표현

#### 상태 기반 방식 vs 마이그레이션 기반 방식 
- 상태 기반 방식
	- 데이터베이스 상태가 명시적으로 드러남
	- 마이그레이션 매커니즘은 감추어져 있음
	- 병합 충돌 처리 수월
- 마이그레이션 기반 방식 
	- 마이그레이션 매커니즘이 명시적으로 드러남
	- 데이터베이스 상태는 직접 볼 수 없고 마이그레이션으로 조합해야 함
	- 데이터 모션 문제 해결에 유리하다
		- 데이터 모션이란 새로운 데이터베이스 스키마를 준수하도록 기존 데이터의 형태를 변경하는 것
		- e.g. Name column 을 FirstName & LastName column 으로 나눈다
- 대부분 프로젝트에서는 데이터 모션이 병합 충돌보다 중요한 문제이므로 마이그레이션 기반 방식을 택하자

## 데이터베이스 트랜잭션 관리
### 제품 코드에서 DB 트랜잭션 관리

```C#
public class Database
{
    private readonly string _connectionString;

    public Database(string connectionString)
    {
        _connectionString = connectionString;
    }

    public void SaveUser(User user)
    {
        bool isNewUser = user.UserId == 0;
        using (var connection =
            new SqlConnection(_connectionString))
        {
            /* Insert or update the user depending on isNewUser */
		} 
	}

    public void SaveCompany(Company company)
    {
        using (var connection =
            new SqlConnection(_connectionString))
        {
            /* Update only; there's only one company */
		} 
	}
}
```

```java
public String changeEmail(int userId, String newEmail) {  
   // 트랜잭션 1
   Object[] userData = db.getUserById(userId);  
   User user = UserFactory.create(userData);  
  
   String error = user.canChangeEmail();  
   if (error != null) {  
      return error;  
   }  
   // 트랜잭션 2
   Object[] companyData = db.getCompany();  
   Company company = CompanyFactory.create(companyData);  
  
   user.changeEmail(newEmail, company);  
   // 트랜잭션 3
   db.saveCompany(company);  
   // 트랜잭션 4
   db.saveUser(user);  
  
   eventDispatcher.dispatch(user.getDomainEvents());  
  
   return "OK";  
}
```

- read only 트랜잭션의 경우에는 동시에 여러 개의 트랜잭션을 열어도 무방
- 그러나 write 트랜잭션의 경우에는 데이터 모순을 피하기 위해 모든 update 들이 원자적이어야 함. 즉, 각 업데이트는 전체적으로 완료되거나 아무런 영향도 미치지 않아야 함

#### 데이터베이스 트랜잭션에서 데이터베이스 연결 분리하기
- 책임 분리
	- repository
		- 데이터베이스의 데이터에 대한 접근과 수정을 가능하게 하는 클래스
	- 트랜잭션
		- 데이터 업데이트를 commit or rollback 하는 클래스

![](attachments/스크린샷%202023-05-15%20오전%207.00.27.png)
- 트랜잭션이 컨트롤러 - 데이터베이스 간 상호작용을 조정한다. 데이터 수정은 커밋되거나 롤백된다.

```java
public class UserController {  
  
   private final Transaction transaction;  
   private final UserRepository userRepository;  
   private final CompanyRepository companyRepository;  
   private final EventDispatcher eventDispatcher;  
  
   public UserController(Transaction transaction, EventDispatcher eventDispatcher) {  
      this.transaction = transaction;  
      this.userRepository = new UserRepository(transaction);  
      this.companyRepository = new CompanyRepository(transaction);  
      this.eventDispatcher = eventDispatcher;  
   }  
  
   public String changeEmail(int userId, String newEmail) {  
      Object[] userData = userRepository.getUserById(userId);  
      User user = UserFactory.create(userData);  
  
      String error = user.canChangeEmail();  
      if (error != null) {  
         return error;  
      }  
  
      Object[] companyData = companyRepository.getCompany();  
      Company company = CompanyFactory.create(companyData);  
  
      user.changeEmail(newEmail, company);  
  
      companyRepository.saveCompany(company);  
      userRepository.saveUser(user);  
      eventDispatcher.dispatch(user.getDomainEvents());  
  
      //성공시 트랜잭션 커밋  
      transaction.commit();  
  
      return "OK";  
   }  
  
}
```

```C#
public class UserRepository
{
    private readonly Transaction _transaction;
    
// Injects a transaction into a repository
    public UserRepository(Transaction transaction)
    {
        _transaction = transaction;
    }

	/* ... */ 
}

public class Transaction : IDisposable
{
  // 트랜잭션을 성공으로 표시한다.
    public void Commit() { /* ... */ }
  // 트랜잭션을 종료한다. 
    public void Dispose() { /* ... */ }
}
```

- commit()
	- 트랜잭션을 성공으로 표시한다.
	- `changeEmail()` 끝에 위치한다. 메서드 수행 도중 오류가 발생하면 커밋되지 않도록 하기 위함.
- dispose()
	- 트랜잭션을 종료한다. 
	- 이전에 commit() 이 호출되었으면 데이터 업데이트를 DB 에 저장하고, 그렇지 않으면 롤백한다.
- userRepository 는 DB 를 직접 호출할 수 없음에 유의하자. 즉, Database 명시적 의존성이 없음

#### 트랜잭션을 작업 단위<sup>A UNIT OF WORK</sup>로 업그레이드하기
- 작업 단위
	- 비즈니스 연산의 영향을 받는 객체 목록을 가짐. 작업이 완료되면 업데이트를 하나의 단위로 실행
- 장점
	- 업데이트 지연
		- 비즈니스 연산 종료 시점에 모든 업데이트를 실행
		- 데이터베이스 트랜잭션 기간 단축, 데이터 혼잡 줄임, 데이터베이스 호출 횟수 줄임
- 수정된 객체 목록을 관리하기 위한 SQL 스크립트를 작성하는 것은 쉽지 않은 일이지만 실무에서는 ORM library 가 이러한 작업 단위 패턴을 구현

![](attachments/스크린샷%202023-05-15%20오전%207.18.58.png)


```java
public class UserController {  
  
   private final CrmContext context;  
   private final UserRepository userRepository;  
   private final CompanyRepository companyRepository;  
   private final EventDispatcher eventDispatcher;  
  
   public UserController(CrmContext context, EventDispatcher eventDispatcher) {  
      this.context = context;  
      this.userRepository = new UserRepository(context);  
      this.companyRepository = new CompanyRepository(context);  
      this.eventDispatcher = eventDispatcher;  
   }  
  
   public String changeEmail(int userId, String newEmail) {  
      Object[] userData = userRepository.getUserById(userId);  
      User user = UserFactory.create(userData);  
  
      String error = user.canChangeEmail();  
      if (error != null) {  
         return error;  
      }  
  
      Object[] companyData = companyRepository.getCompany();  
      Company company = CompanyFactory.create(companyData);  
  
      user.changeEmail(newEmail, company);  
  
      companyRepository.saveCompany(company);  
      userRepository.saveUser(user);  
      eventDispatcher.dispatch(user.getDomainEvents());  
  
      // crmContext 가 트랜잭션을 대체함  
      context.saveChanges();  
      return "OK";  
   }  
}
```

- CrmContext
	- 데이터베이스와 도메인 모델 간 매핑을 포함
		- `UserFactory`, `CompanyFactory` 는 이제 필요하지 않음 


#### Data inconsistencies in non-relational database
- 고전적인 의미에서의 트랜잭션이 없음.
	- 원자적 업데이트는 단일 document 내에서만 보장됨. 따라서 비즈니스 연산이 여러 document 에 영향을 주는 경우 Data inconsistencies 가 생기기 쉬움
- 따라서 Data inconsistencies 해결을 위해서는 한 번에 둘 이상의 document 를 수정하는 비즈니스 연산이 없도록 document 를 설계해야 함


### 통합 테스트에서 데이터베이스 트랜잭션 관리하기
- 테스트 구절 간에 데이터베이스 트랜잭션이나 작업 단위를 재사용하지 말라

- 작업 단위 재사용하는 안티패턴 예시
```C#
[Fact]
public void Changing_email_from_corporate_to_non_corporate()
{

// 컨텍스트 생성
using (var context =
    new CrmContext(ConnectionString))

{  
// Arrange
// context 사용 (준비 구절)
    var userRepository =
        new UserRepository(context);

    var companyRepository =
        new CompanyRepository(context);

    var user = new User(0, "user@mycorp.com",
        UserType.Employee, false);

    userRepository.SaveUser(user);
    var company = new Company("mycorp.com", 1);
    companyRepository.SaveCompany(company);
    context.SaveChanges();

    var busSpy = new BusSpy();
    var messageBus = new MessageBus(busSpy);
    var loggerMock = new Mock<IDomainLogger>();
// context 사용 (실행 구절)
    var sut = new UserController(context,messageBus,loggerMock.Object);

// Act
string result = sut.ChangeEmail(user.UserId, "new@gmail.com");

// Assert
Assert.Equal("OK", result);

// context 사용 (검증 구절)
User userFromDb = userRepository
    .GetUserById(user.UserId);

Assert.Equal("new@gmail.com", userFromDb.Email);
Assert.Equal(UserType.Customer, userFromDb.Type);

Company companyFromDb = companyRepository
    .GetCompany();

Assert.Equal(0, companyFromDb.NumberOfEmployees);


busSpy.ShouldSendNumberOfMessages(1)
    .WithEmailChangedMessage(user.UserId, "new@gmail.com");

loggerMock.Verify(
    x => x.UserTypeHasChanged(

        user.UserId, UserType.Employee, UserType.Customer),
    Times.Once);
    }
}
```

- 모든 테스트 구절마다 context 를 재사용하고 있음
	- 실제 운영환경에서는 context 가 컨트롤러 메서드 호출 직전에 생성되고, 호출 직후 폐기된다. 
	- 운영환경 - 테스트환경 불일치 발생
- 위 문제 해결을 위해서는 실행 구절에서의 context 를 다른 곳에서 공유해선 안됨. 즉, 구절 당 하나의 context 를 갖는 것을 권장.
- (참고) chpt 8 입력 매개변수로 사용되는 데이터와는 별개로 데이터베이스의 상태를 확인하라

## 테스트데이터 생명 주기(life cycle)
- 공유 데이터베이스를 사용할 경우 통합테스트를 서로 분리할 수는 없다.
- 위 문제 해결을 위해
	- 통합테스트를 순차적으로 실행하라. 즉, 병렬 실행하지 마라
	- 테스트 실행 간 남은 데이터를 제거하라

### 병렬 테스트 실행과 순차적 테스트 실행
- 통합테스트를 병렬로 실행하려면 비용이 많이 든다. 따라서 순차적으로 통합 테스트를 실행하라.
- 두 가지 테스트군 (단위테스트 / 통합테스트) 를 만들고 통합 테스트군은 병렬 처리를 비활성화한다. 
- 컨테이너를 사용해 통합테스트를 병렬 처리할 수도 있지만 유지보수 부담이 커지므로 권장하지 않음
- Q. junit 단위테스트 병렬 실행?

### 테스트 실행 간 데이터 정리 
- (1안) 각 테스트 전 데이터베이스 백업 복원하기
	- 시간이 오래 걸리므로 권장 X
- (2안) 테스트 종료 시점에 데이터 정리하기
	- 데이터 정리 단계를 건너뛰기 쉬우므로 권장 X
	- e.g. 테스트가 도중에 종료되는 경우
- (3안) 트랜잭션이 커밋되지 않도록 래핑하기
	- 운영 환경과 다른 설정이 추가되는 것이므로 권장 X
- (4안) 테스트 시작 시점에 데이터 정리하기
	- best
- forgien key Contraint 준수 등 특정 순서에 따라 데이터를 제거해야할 수 있음.
	- 이때는 통합테스트의 base class 를 두고 이곳에서 삭제 스크립트를 수행하는 것도 방법

```C#
public abstract class IntegrationTests
{

    private const string ConnectionString = "...";
	protected IntegrationTests()
	{
		ClearDatabase();
	}
	
	private void ClearDatabase()
	{
	// 삭제 스크립트
		string query =
		"DELETE FROM dbo.[User];" +
		"DELETE FROM dbo.Company;";
		
		using (var connection = new SqlConnection(ConnectionString))
		{
			var command = new SqlCommand(query, connection)
			{
			
				CommandType = CommandType.Text
			};
			connection.Open();
			command.ExecuteNonQuery();
		}
	}
}
```

### 인메모리 데이터베이스 피하기
- 통합테스트 시 데이터베이스를 인메모리 데이터베이스로 교체
	- 장점
		- 테스트 데이터 제거 필요 X
		- 작업속도 향상
		- 테스트가 실행될 때마다 인스턴스화 가능
	- 단점
		- 운영 환경과 테스트환경 일치 X
			- 거짓 양성 혹은 거짓 음성이 발생하기 쉬움
	- 결론
		- 통합테스트시 데이터베이스를 인메모리 데이터베이스로 대체하지 마라


## 테스트 구절에서 코드 재사용하기
- 통합테스트를 간결하게 만들기 위해 가장 좋은 방법은 비즈니스와 관련 없는 기술적인 부분을 비공개 메서드 혹은 헬퍼 클래스로 추출하는 것

- 3개의 database context(작업단위) 를 사용하는 테스트코드

```C#
[Fact]
public void Changing_email_from_corporate_to_non_corporate()
{

	// Arrange
	User user;
	using (var context = new CrmContext(ConnectionString))
	{
		var userRepository = new UserRepository(context);
		var companyRepository = new CompanyRepository(context);
		user = new User(0, "user@mycorp.com",
	
			UserType.Employee, false);
		userRepository.SaveUser(user);
		var company = new Company("mycorp.com", 1);
		companyRepository.SaveCompany(company);
	
		context.SaveChanges();
	}

	var busSpy = new BusSpy();
	var messageBus = new MessageBus(busSpy);
	var loggerMock = new Mock<IDomainLogger>();

	string result;
	using (var context = new CrmContext(ConnectionString))
	{
	
		var sut = new UserController(context, messageBus, loggerMock.Object);
	
		// Act
		result = sut.ChangeEmail(user.UserId, "new@gmail.com");
	}
	
	// Assert
	Assert.Equal("OK", result);
	
	using (var context = new CrmContext(ConnectionString))
	{
	
	var userRepository = new UserRepository(context);
	var companyRepository = new CompanyRepository(context);
	
	User userFromDb = userRepository.GetUserById(user.UserId);
	Assert.Equal("new@gmail.com", userFromDb.Email);
	Assert.Equal(UserType.Customer, userFromDb.Type);
	
	Company companyFromDb = companyRepository.GetCompany();
	Assert.Equal(0, companyFromDb.NumberOfEmployees);
	
	busSpy.ShouldSendNumberOfMessages(1)
	    .WithEmailChangedMessage(user.UserId, "new@gmail.com");
	
	loggerMock.Verify(
	    x => x.UserTypeHasChanged(
	
	        user.UserId, UserType.Employee, UserType.Customer),
	    Times.Once);
	}
}
```

### 준비 구절에서 코드 재사용하기
- private factory method 도입

```C#
private User CreateUser(
		string email, UserType type, bool isEmailConfirmed)
{
	using (var context = new CrmContext(ConnectionString))
	{
		var user = new User(0, email, type, isEmailConfirmed);
		var repository = new UserRepository(context);
		repository.SaveUser(user);

		context.SaveChanges();

		return user;
	}
}

```

- 메서드 parameter 에 default value 를 지정하면 테스트 코드를 더욱 간결하게 만들 수 있다.
	- java 에서는 지원하지 않음..

```C#
private User CreateUser(  
string email = "user@mycorp.com", UserType type = UserType.Employee, bool isEmailConfirmed = false)
{  
	/* ... */
}
```

- (참고) 오브젝트 마더 vs 테스트 데이터 빌더
	- 오브젝트 마더
		- 테스트 픽스처를 만드는 데 도움이 되는 클래스 혹은 메서드
		- e.g. 위 예시 코드에서 팩토리 메서드
	- 테스트 데이터 빌더의 경우 가독성에 도움이 되긴 하지만 상용구가 많이 필요하므로 오브젝트 마더 사용을 권장
- 팩토리 메서드 위치
	- 우선 테스트 클래스 내에 두고, 코드 중복이 심해질 경우에 별도의 헬퍼 클래스로 이동시키자.
	- base 클래스에 두지는 말자. base 클래스는 데이터 정리와 같이 모든 테스트에서 실행해야 하는 코드를 위한 클래스

### 실행 구절에서 코드 재사용하기
- 기존 실행 구절

```C#
	string result;
	using (var context = new CrmContext(ConnectionString))
	{	
		var sut = new UserController(context, messageBus, loggerMock.Object);
	
		// Act
		result = sut.ChangeEmail(user.UserId, "new@gmail.com");
	}
```

- contoller function 을 parameter (delegate) 로 받는 메서드를 통해 위 구절을 간결하게 만들 수 있다.

```C#

private string Execute(
//Delegate defines a controller function.
    Func<UserController, string> func,
    MessageBus messageBus,
    IDomainLogger logger)
{
    using (var context = new CrmContext(ConnectionString))
    {
        var controller = new UserController(context, messageBus, logger);
		return func(controller);
	}
}
```

```C#
string result = Execute(
    x => x.ChangeEmail(user.UserId, "new@gmail.com"),
    messageBus, loggerMock.Object);
```

### 검증 구절에서 코드 재사용하기
- 준비 구절에서 private 팩토리 메서드를 구현한 것과 유사한 헬퍼 메서드를 만든다.

```C#
//헬퍼 메서드 사용
User userFromDb = QueryUser(user.UserId);
Assert.Equal("new@gmail.com", userFromDb.Email);
Assert.Equal(UserType.Customer, userFromDb.Type);

//헬퍼 메서드 사용
Company companyFromDb = QueryCompany();
Assert.Equal(0, companyFromDb.NumberOfEmployees);
```

- 추가로 chpt9 busSpy 와 같은 fluent 인터페이스를 만드는 것도 가독성 향상에 도움이 된다.

```C#
User userFromDb = QueryUser(user.UserId);
userFromDb
    .ShouldExist()
    .WithEmail("new@gmail.com")
	 .WithType(UserType.Customer);

	Company companyFromDb = QueryCompany();
	companyFromDb
		.ShouldExist()
		.WithNumberOfEmployees(0);
```


### 테스트가 데이터베이스 트랜잭션을 너무 많이 생성하는가?

- 개선한 통합테스트 코드

```C#
 public class UserControllerTests : IntegrationTests
{

Instantiates a new database context behind the scenes

	[Fact]
	public void Changing_email_from_corporate_to_non_corporate()
	{

	    // Arrange
	    // t1
	    User user = CreateUser(
	
	        email: "user@mycorp.com",
	
	        type: UserType.Employee);
	    // t2
	    CreateCompany("mycorp.com", 1);
	
	    var busSpy = new BusSpy();
	    var messageBus = new MessageBus(busSpy);
	    var loggerMock = new Mock<IDomainLogger>();
	
	    // Act
	    // t3
	    string result = Execute(
	
	        x => x.ChangeEmail(user.UserId, "new@gmail.com"),
	        messageBus, loggerMock.Object);
	
	    // Assert
	    Assert.Equal("OK", result);

		// t4
	    User userFromDb = QueryUser(user.UserId);
	    userFromDb
	
	        .ShouldExist()
	        .WithEmail("new@gmail.com")
	        .WithType(UserType.Customer);

		// t5
	    Company companyFromDb = QueryCompany();
	    companyFromDb
	
	        .ShouldExist()
	        .WithNumberOfEmployees(0);
	
	    busSpy.ShouldSendNumberOfMessages(1)
	        .WithEmailChangedMessage(user.UserId, "new@gmail.com");
	
	    loggerMock.Verify(
			x => x.UserTypeHasChanged(user.UserId, UserType.Employee, UserType.Customer), Times.Once);
	}
}

```

- 가독성, 유지보수성은 향상되었지만 데이터베이스 트랜잭션 횟수가 3회 -> 5회로 늘어났다. 
	- 이에 따라 테스트 수행 속도가 느려짐 (빠른 피드백 - 유지 보수성 tradeoff)
- 이 경우엔 성능저하가 크지 않으므로 유지보수성을 위해 성능을 양보하자

## 데이터베이스 테스트에 대한 일반적인 질문

### 읽기 테스트를 해야 하는가?
- 쓰기 테스트는 매우 중요하다.
	- 쓰기가 잘못 되었을 때 데이터가 손상되는 등 위험성이 높기 때문.
- 읽기 버그는 상대적으로 덜 해롭다.
	- 복잡하거나 중요한 읽기 작업만 테스트하고 나머지는 무시하라
- 읽기와 관련된 도메인 모델도 별도로 둘 필요는 없다.
	- 캡슐화는 변경 사항에 맞춰 데이터 일관성을 유지하기 위함인데, 읽기에는 데이터 변경이 없으므로 캡슐화 하는 것은 의미가 없다. 
	- Q. 읽기를 캡슐화한다는 것은 어떤 의미일까. readEvent?

### 레포지토리 테스트를 해야 하는가?
- 레포지토리 
	- 데이터베이스 위에서 유용한 추상화 제공
- 레포지토리 테스트는 DB - 도메인 객체 매핑을 검증할 때 도움이 될 수 있음
- but, 단점이 있음
	- 높은 유지비
		- 외부의존성인 데이터베이스가 있으므로 (컨트롤러 사분면에 위치) 유지비가 높음
	- 낮은 회귀 방지
		- 일반적으로 리포지토리는 그렇게 복잡하지 않으므로 회귀 방지에서 큰 효과가 없음.
- 레포지토리를 테스트하기 가장 좋은 방법
	- 리포지토리가 가지고 있는 복잡도를 별도 알고리즘으로 추출하고 해당 알고리즘 전용 테스트를 작성
		- e.g. UserFactory, CompanyFactory
- 레포지토리는 직접 테스트하지 말고 포괄적인 통합 테스트 스위트의 일부로 취급하자


## 결론
- 데이터베이스 테스트 작성시 이점
	- 데이터베이스를 리팩터링하거나 ORM 을 전환하거나 데이터베이스 공급업체를 변경할 때 도움이 된다.