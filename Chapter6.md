# 6장. 단위 테스트 스타일

# 1. 단위 테스트의 세 가지 스타일

## 출력 기반 테스트

- 테스트 대상 시스템에 입력을 넣고 생성되는 출력을 점검하는 방식
- 전역 상태나 내부 상태를 변경하지 않는 코드에만 적용되므로 반환 값만 검증
- 사이드 이펙트 없는 코드 선호를 강조하는 프로그래밍 방식인 함수형 프로그래밍에 뿌리를 두고 있어 함수형이라고도 부름
    
    <img width="529" alt="스크린샷 2023-04-15 오후 6 08 35" src="https://user-images.githubusercontent.com/7659412/232326447-67478358-02c7-4b70-8fce-6806cf8b7608.png">

```java
public class PriceEngine {
	public int calculateDiscount(Product[] products[]) {
		int discount = products.length * 0.01;
		return Math.min(discount, 0.2);
	}
}

@Test
public void discount_of_two_products() {
	Product product1 = new Product("Hand wash");
	Product product2 = new Product("Shampoo");
	PriceEngin sut = new PriceEngine();

	int discount = sut.calculateDiscount(product1, product2);

	Assert.Equal(0.02, discount);
}
```

## 상태 기반 테스트

- 작업이 완료된 후 시스템 상태를 확인
- 상태라는 용어는 SUT나 협력자 중 하나, 또는 데이터베이스나 파일 시스템 등과 같은 프로세스 외부 의존성의 상태 등을 의미
    
    <img width="484" alt="스크린샷 2023-04-15 오후 6 13 48" src="https://user-images.githubusercontent.com/7659412/232326455-abf32825-bb08-413c-bc7f-cba09d376013.png">


```java
public class Order {
	private readonly List<Product> _products = new List<Product>();
	public IReadOnlyList<Product> products = _products.toList();

	public void addProduct(Product product) {
		_products.add(product);
	}
}

@Test
public void adding_a_product_to_an_order() {
	Product product = new Product("Hand wash');
	Order sut = new Order();

	sut.addProduct(product);

	Assert.Equal(1, sut.products.count);
	Assert.Equal(product, sut.products[0]);
}
```

## 통신 기반 테스트

- 목을 사용해 테스트 대상 시스템과 협력자 간의 통신을 검증
    
    <img width="690" alt="스크린샷 2023-04-15 오후 6 16 37" src="https://user-images.githubusercontent.com/7659412/232326463-58096b51-8259-42f8-9d25-f47c4512544d.png">


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

# 2. 단위 테스트 스타일 비교

### 좋은 단위 테스트의 4대 요소

- 회귀 방지
- 리팩터링 내성
- 빠른 피드백
- 유지 보수성

### 회귀 방지와 피드백 속도 지표로 스타일 비교

- 회귀 방지 지표
    - 회귀 방지 지표는 특정 스타일에 따라 달라지지 않는다.
    - 회귀 방지 지표는 세 가지 특성으로 결정
        - 테스트 중에 실행되는 코드의 양
            - 테스트 스타일과 관계 없이 보통 실행하는 코드가 많든 적든 원하는 대로 테스트 작성 가능
        - 코드 복잡도
        - 도메인 유의성
        - 통신 기반 스타일의 경우, 남용하면 작은 코드 조각을 검증하고 다른 것은 모두 목을 사용하는 등 피상적인 테스트가 될 수 있지만 기술을 남용하는 극단적인 사례임
- 피드백 속도
    - 테스트 스타일과 테스트 피드백 속도 사이에도 상관관계가 거의 없다.
    - 테스트가 프로세스 외부 의존성과 떨어져 단위 테스트 영역에 있는 한, 모든 스타일은 테스트 실행 속도가 거의 동일함
    - 목은 런타임에 지연 시간이 생기는 편이므로 통신 기반 테스트가 약간 나쁠 수 있지만, 테스트 개수가 수만 개 수준이 아니라면 별로 차이는 없다.

### 리팩터링 내성 지표로 스타일 비교

- 리팩터링 내성은 리팩터링 중에 발생하는 거짓 양성 수에 대한 척도
    - 식별할 수 있는 동작이 아니라 코드의 구현 세부 사항에 결합된 테스트의 결과
- 출력 기반 테스트는 테스트 대상 메서드에만 결합되므로 거짓 양성 방지가 가장 우수
- 상태 기반 테스트는 일반적으로 거짓 양성이 되기 쉬움
    - 테스트가 테스트 대상 메서드 외에 클래스 상태와 함께 동작한다.
    - 테스트와 제품 코드 간의 결합도가 클수록 유출되는 구현 세부 사항에 테스트가 얽매일 가능성이 크다.
- 통신 기반 테스트는 허위 경보에 가장 취약함
    - 테스트 대역으로 상호 작용을 확인하는 테스트는 대부분 깨지기 쉽다.
- 캡슐화를 잘 지키고 테스트를 식별할 수 있는 동작에만 결합하면 거짓 양성을 최소로 줄일 수 있다.

### 유지 보수성 지표로 스타일 비교

- 단위 테스트 스타일 지표와 밀접한 관련이 있다. 그러나 완화할 수 있는 방법이 많지 않다.
- 유지 보수성은 단위 테스트의 유지비를 측정
    - 테스트를 이해하기 얼마나 어려운가(테스트 크기에 대한 함수)?
    - 테스트를 실행하기 얼마나 어려운가(테스트에 직접적으로 관련 있는 프로세스 외부 의존성 개수에 대한 함수)?
- 테스트가 크거나 하나 이상의 프로세스 외부 의존성과 직접 작동하는 테스트는 유지 보수가 어렵다.
- 출력 기반 테스트의 유지 보수성
    - 출력 기반 테스트가 가장 유지 보수하기 용이
    - 출력 기반 테스트는 거의 항상 짧고 간결하므로 유지 보수가 쉽다.
    - 전역 상태나 내부 상태를 변경할 리 없으므로 프로세스 외부 의존성을 다루지 않는다.
- 상태 기반 테스트의 유지보수성
    - 상태 검증은 종종 출력 검증보다 더 많은 공간을 차지하기 때문에 출력 기반 테스트보다 유지 보수가 쉽지 않다.
    
    ```java
    @Test
    public void adding_a_comment_to_an_article() {
    	Article sut = new Article();
    	String text = "Comment text";
    	String author = "John Doe";
    	LocalDateTime now = new LocalDateTime(2019, 4, 1);
    
    	sut.addComment(text, author, now);
    
    	Assert.Equal(1, sut.comments.count);
    	Assert.Equal(text, sut.comments[0].text);
    	Assert.Equal(author, sut.comments[0].author);
    	Assert.Equal(now, sut.comments[0].dateCreated);
    }
    ```
    
    - 상태 기반 테스트는 많은 데이터를 확인해야 하므로 크기가 커질 수 있다.
        - 헬퍼 메서드로 문제를 완화할 수 있지만, 이러한 메서드를 작성하고 유지하는 데 상당한 노력이 필요
        - 검증 대상 클래스의 동등 멤버를 정의해서 값 객체로 변환
            - 클래스가 값에 해당하고 값 객체로 변환할 수 있을때만 효과적
- 통신 기반 테스트의 유지보수성
    - 두 테스트에 비해 점수가 낮다.
    - 통신 기반 테스트에는 테스트 대역과 상호 작용 검증을 설정해야 되기 때문에 공간을 많이 차지
    - 목이 사슬 형태로 있을 때 테스트는 더 커지고 유지 보수하기 어려워진다.

### 정리

|  | 출력 기반 | 상태 기반 | 통신 기반 |
| --- | --- | --- | --- |
| 리팩터링 내성을 지키기 위해 필요한 노력 | 낮음 | 중간 | 중간 |
| 유지비 | 낮음 | 중간 | 높음 |
- 출력 기반 테스트가 가장 결과가 좋다.
    - 구현 세부 사항과 거의 결합되지 않으므로 리팩터링 내성을 적절히 유지하고자 주의를 많이 기울일 필요가 없다.
    - 간결하고 프로세스 외부 의존성이 없기 때문에 유지 보수도 쉽다.
    - 함수형으로 작성된 코드에만 적용할 수 있고, 대부분의 객체지향 프로그래밍 언어에는 해당하지 않기 때문에 적용하기 어렵다.
- 상태 기반 테스트, 통신 기반 테스트는 유출된 구현 세부 사항에 테스트가 결합할 가능성이 높고, 크기도 커서 유지비가 많이 든다.

# 3. 함수형 아키텍처 이해

### 함수형 프로그래밍

- 숨은 입출력이 없는 함수인 수학적 함수를 사용한 프로그래밍
- 수학적 함수의 모든 입출력은 메서드 이름, 인수, 반환 타입으로 구성된 메서드 시그니처에 명시
- 수학적 함수는 호출 횟수에 상관없이 주어진 입력에 대해 동일한 출력을 생성

```java
public int calculateDiscount(Product[] products[]) {
	int discount = products.length * 0.01;
	return Math.min(discount, 0.2);
}
```

- 입출력을 명시한 수학적 함수는 테스트가 짧고 간결하며 이해하고 유지 보수하기 쉬우므로 테스트하기가 매우 쉽다.
- 출력 기반 테스트를 적용할 수 있는 메서드 유형은 수학적 함수
- 유지 보수성이 뛰어나고 거짓 양성 빈도가 낮다.

### 숨은 입출력의 유형

- 사이드 이펙트
    - 메서드 시그니처에 표시되지 않은 출력이며 숨어있다.
    - 연산은 클래스 인스턴스의 상태를 변경하고 디스크의 파일을 업데이트 하는 등 사이드 이펙트를 발생 시킨다.
- 예외
    - 메서드가 예외를 던지면 프로그램 흐름에 메서드 시그니처에 설정된 계약을 우회하는 경로를 만든다.
    - 호출된 예외는 호출 스택의 어느 곳에서도 발생할 수 있으므로 메서드 시그니처가 전달하지 않는 출력을 추가한다.
- 내외부 상태에 대한 참조
    - 정적 속성을 사용하여 현재 날짜와 시간을 가져오는 메서드가 있을 수 있다.
    - 데이터베이스에서 데이터를 질의할 수 있고, 비공개 변경 가능 필드를 참조할 수 있다.
    - 메서드 시그니처에 없는 실행 흐름에 대한 입력이며 숨어있다.

- 메서드가 수학적 함수인지 판별하는 가장 좋은 방법은 프로그램의 동작을 변경하지 않고 해당 메서드에 대한 호출을 반환 값으로 대체할 수 있는지 확인 하는 것이다.

메서드 호출을 해당 값으로 바꾸는 것을 참조 투명성이라고 한다.

```java
public int increment(int x) {
	return x+1;
}

int y = increment(4);
int y = 5;
```

사이드 이펙트는 숨은 출력의 가장 일반적인 유형

```java
public Comment addComment(String text) {
	Comment comment = new Comment(text);
	**_comments.add(comment); // 사이드 이펙트**
	return comment;
}
```

### 함수형 아키텍처

- 함수형 프로그래밍의 목표는 사이드 이펙트를 완전히 제거하는 것이 아니라 비즈니스 로직을 처리하는 코드와 사이드 이펙트를 일으키는 코드를 분리
- 사이드 이펙트를 비즈니스 연산 끝으로 몰아서 비즈니스 로직을 사이드 이펙트와 분리
- 결정을 내리는 코드(함수형 코어) : 사이드 이펙트가 필요 없기 때문에 수학적 함수를 사용해 작성할 수 있다.
- 해당 결정에 따라 작용하는 코드(가변 셸) : 수학적 함수에 의해 이뤄진 모든 결정을 데이터베이스의 변경이나 메시지 버스로 전송된 메시지와 같이 가시적인 부분으로 변환한다.
    
    <img width="694" alt="스크린샷 2023-04-15 오후 8 33 08" src="https://user-images.githubusercontent.com/7659412/232326473-60d9de8e-745c-497e-b6e2-087659e411f4.png">

- 가변셸은 모든 입력을 수집한다.
- 함수형 코어는 결정을 생성한다.
- 가변셸은 결정을 사이드 이펙트로 변환한다.

목표는 출력 기반 테스트로 함수형 코어를 다루고 가변 셸은 적은 수의 통합 테스트에 맡긴다.

### 함수형 아키텍처와 육각형 아키텍처의 비교

- 공통점
    - 관심사 분리라는 아이디어를 기반
    - 육각형 아키텍처는 도메인 계층과 애플리케이션 서비스 계층을 구별
        - 도메인 계층 : 비즈니스 로직에 책임
        - 애플리케이션 서비스 계층 : 데이터베이스나 SMTP 서비스와 같이 외부 어플리케이션과의 통신에 책임
    - 의존성 간 단방향 흐름
- 차이점
    - 사이드 이펙트에 대한 처리
        - 함수형 아키텍처는 모든 사이드 이펙트를 불변 코어에서 비즈니스 연산 가장자리로 밀어내 가변 셸이 처리한다.
        - 육각형 아키텍처는 도메인 계층에 제한하는 한, 도메인 계층으로 인한 사이드 이펙트도 문제 없다.
            - 모든 수정 사항은 도메인 계층 내에 있어야 하며, 계층의 경계를 넘어서는 안된다.
- 함수형 아키텍처는 육각형 아키텍처의 하위 집합이다.

# 4. 함수형 아키텍처와 출력 기반 테스트로의 전환

- 샘플 애플리케이션을 함수형 아키텍처로 리팩터링
    - 프로세스 외부 의존성에서 목으로 변경
    - 목에서 함수형 아키텍처로 변경

```java
public class AuditManager {
	private readonly int _maxEntriesPerFile;
	private readonly String _directoryName;

	public AuditManager(int maxEntriesPerFile, int directoryName) {
		_maxEntriesPerFile = maxEntriesPerFile
		_directoryName = directoryName;
	}

	public void addRecord(String visitorName, LocalDateTime timeOfVisit) {
		String[] filePaths = Directory.getFiles(_directoryName);
		(int index, String path)[] sorted = SortByIndex(filePaths);

		String newRecord = visitorName + ";" + timeOfVisit;

		if(sorted.length == 0) {
			String newFile = Path.combile(_directoryName, "audit_1.txt");
			File.writeAllText(newFile, newRecord);
			return;
		}

		(int currentFileIndex, String currentFilePath) = sorted.last();
		List<String> lines = File.ReadAllLines(currentFilePath).toList();

		if(lines.count < _maxEntriesPerFile) {
			lines.add(newRecord);
			String newContent = lines.join("\r\n");
			File.writeAllText(currentFilePath, newContent);
		} else {
			int nexIndex = currentFileIndex + 1;
			String newName = $"audit_{newIndex}.txt";
			String newFile = Path.combile(_directoryName, newName);
			File.writeAllText(newFile, newRecord);
		}
	}
}
```

- 작업 디렉터리에서 전체 파일 목록을 검색한다.
- 인덱스 별로 정렬한다.
- 아직 감사 파일이 없으면 단일 레코드로 첫 번째 파일을 생성한다.
- 감사 파일이 있으면 최신 파일을 가져와서 파일의 항목 수가 한계에 도달했는지에 따라 새 레코드를 추가하거나 새 파일을 생성한다.

|  | 초기 버전 |
| --- | --- |
| 회귀 방지 | 좋음 |
| 리팩터링 내성 | 좋음 |
| 빠른 피드백 | 나쁨 |
| 유지 보수성 | 나쁨 |

파일 시스템이 공유 의존성이기 때문에 그대로 테스트하기가 어렵고 테스트를 느리게 한다.

### 테스트를 파일 시스템에서 분리하기 위한 목 사용

- 테스트가 밀접하게 결합된 문제는 일반적으로 파일 시스템을 목으로 처리해서 해결

```java
public interface IFileSystem {
	string[] getFiles(String directoryName);
	void writeAllText(String filePath, String content);
	List<String> readAllLines(String filePath);
}

public class AuditManager {
	private readonly int _maxEntriesPerFile;
	private readonly String _directoryName;
	private readonly IFileSystem _fileSystem;

	public AuditManager(int maxEntriesPerFile, int directoryName, IFileSystem _fileSystem) {
		_maxEntriesPerFile = maxEntriesPerFile
		_directoryName = directoryName;
		_fileSystem = fileSystem;
	}

	public void addRecord(String visitorName, LocalDateTime timeOfVisit) {
		String[] filePaths = _fileSystem.getFiles(_directoryName);
		(int index, String path)[] sorted = SortByIndex(filePaths);
	
		String newRecord = visitorName + ";" + timeOfVisit;
	
		if(sorted.length == 0) {
			String newFile = Path.combile(_directoryName, "audit_1.txt");
			_fileSystem.writeAllText(newFile, newRecord);
			return;
		}
	
		(int currentFileIndex, String currentFilePath) = sorted.last();
		List<String> lines = _fileSystem.ReadAllLines(currentFilePath).toList();
	
		if(lines.count < _maxEntriesPerFile) {
			lines.add(newRecord);
			String newContent = lines.join("\r\n");
			_fileSystem.writeAllText(currentFilePath, newContent);
		} else {
			int nexIndex = currentFileIndex + 1;
			String newName = $"audit_{newIndex}.txt";
			String newFile = Path.combile(_directoryName, newName);
			_fileSystem.writeAllText(newFile, newRecord);
		}
	}
}
```

```java
@Test
public void a_new_file_is_created_when_the_current_file_overflows() {
	var fileSystemMock = new Mock<IFileSystem>();
	fileSystemMock
		.setup(x => x.getFiles("audits");
		.returns(new String[]{
			@"audits/audit_1.txt",
			@"audits/audit_2.txt"
		});
	fileSystemMock
		.setup(x => x.readAllLines(@"audits/audit_2.txt")
		.returns(new List<String>{
			"Peter;2019-04-06T16:30:00",
			"Peter;2019-04-06T16:30:00",
			"Peter;2019-04-06T16:30:00"
		});
	var sut = new AuditManager(3, "audits", fileSystemMock.Object);

	sut.addRecord("Alice", LocalDateTime.parse("2019-04-06T18:00:00"));

	fileSystemMock.verify(x => x.writeAllText(@"audits/audit_3.txt", "Alice;2019-04-06T18:00:00"));
}
```

|  | 초기 버전 | 목 사용 |
| --- | --- | --- |
| 회귀 방지 | 좋음 | 좋음 |
| 리팩터링 내성 | 좋음 | 좋음 |
| 빠른 피드백 | 나쁨 | 좋음 |
| 유지 보수성 | 나쁨 | 중간 |

테스트는 더 이상 파일 시스템에 접근하지 않기 때문에 더 빨리 실행된다.

테스트를 통과 시키려고 파일 시스템을 다룰 필요가 없기 때문에 유지비도 절감된다.

### 함수형 아키텍처로 리팩터링

```java
public interface IFileSystem {
	string[] getFiles(String directoryName);
	void writeAllText(String filePath, String content);
	List<String> readAllLines(String filePath);
}

public class AuditManager {
	private readonly int _maxEntriesPerFile;

	public AuditManager(int maxEntriesPerFile) {
		_maxEntriesPerFile = maxEntriesPerFile
	}

	public FileUpdate addRecord(FileContent[] files, String visitorName, LocalDateTime timeOfVisit) {
		(int index, FileContent file)[] sorted = SortByIndex(files);
	
		String newRecord = visitorName + ";" + timeOfVisit;
	
		if(sorted.length == 0) {
			return new FileUpdate("audit_1.txt", newRecord);
		}
	
		(int currentFileIndex, FileContent currentFile) = sorted.last();
		List<String> lines = currentFile.lines.toList();
	
		if(lines.count < _maxEntriesPerFile) {
			lines.add(newRecord);
			String newContent = lines.join("\r\n");
			return new FileUpdate(currentFile.fileName, newContent);
		} else {
			int nexIndex = currentFileIndex + 1;
			String newName = $"audit_{newIndex}.txt";
			return new FileUpdate(newName, newRecord);
		}
	}
}

public class FileContent {
	public readonly String fileName;
	public readonly String[] lines;

	public FileContent(String fileName, String[] lines) {
		fileName = fileName;
		lines = lines;
	}
}

public class FileUpdate {
	public readonly String fileName;
	public readonly String newContent;

	public FileUpdate(String fileName, String newContent) {
		fileName = fileName;
		newContent = newContent;
	}
}

public class Persister {
	public FileContent[] readDirectory(String directoryName) {
		return Directory.getFiles(directoryName)
										.select(x => new FileContent(Path.getFileName(x), File.readAllLines(x)))
										.toArray();
	}
}

public class ApplicationService {
	private readonly String _directoryName;
	private readonly AuditManager _auditManager;
	private readonly Persister _persister;

	public ApplicationService(String directoryName, int maxEntriesPerFile) {
		_directoryName = directoryName;
		_auditManager = new AduitManager(maxEntiresPerFile);
		_persister = new Persister();
	}

	public void addRecord(String visitorName, LocalDateTime timeOfVisit) {
		FileContent[] files = _persister.readDirectory(_directoryName);
		FileUpdate update = _auditManager.addRecord(files, visitorName, timeOfVisit);
		_persister.applyUpdate(_directoryName, update);
	}
}
```

Persister는 가변 셸, AuditManager는 함수형 코어

```java
@Test
public void a_new_file_is_created_when_the_current_file_overflows() {
	var sut = new AuditManager(3);
	var files = new FileContent[] {
		new FileContent("audit_1.txt", new String[0]),
		new FileContent("audit_2.txt", new String[] {
			"Peter;2019-04-06T16:30:00",
			"Jane;2019-04-06T16:40:00",
			"Jack;2019-04-06T17:00:00"
		})
	};

	FileUpdate update = sut.addRecord(files, "Alice", LocalDateTime.parse("2019-04-06T18:00:00"));

	Assert.Equal("audit_3.txt", update.fileName);
	Assert.Equal("Alice;2019-04-06T18:00:00", update.newContent);
}
```

|  | 초기 버전 | 목 사용 | 출력 기반 |
| --- | --- | --- | --- |
| 회귀 방지 | 좋음 | 좋음 | 좋음 |
| 리팩터링 내성 | 좋음 | 좋음 | 좋음 |
| 빠른 피드백 | 나쁨 | 좋음 | 좋음 |
| 유지 보수성 | 나쁨 | 중간 | 좋음 |

빠른 피드백 개선 뿐 아니라 유지 보수성 지표도 향상

더 이상 복잡한 목 설정이 필요 없고, 단순한 입출력만 필요하므로 테스트 가독성을 크게 향상 시킨다.

### 예상되는 추가 개발

- 여러 파일에 영향을 줄 수 있으면, 새 메서드는 여러 개의 파일 명령을 반환하게 개발한다.

```java
public FileUpdate[] deleteAllMentions(FileContent[] files, String visitorName)
```

- 요구 사항에 파일 변경 뿐만 아니라 파일 삭제도 생기면 ActionType 필드를 추가해서 업데이트인지 삭제인지 나타낼 수 있다.
- 오류 처리가 필요하면 메서드 시그니처에 오류를 포함할 수 있다.

```java
public (FileUpdate update, Error error) addRecord(FileContent[] files, String visitorName, LocalDateTime timeOfVisit)
```

# 5. 함수형 아키텍처의 단점 이해하기

함수형 아키텍처라고 해도 코드베이스가 커지고 성능에 영향을 미치면서 유지 보수성의 이점이 상쇄된다.

### 함수형 아키텍처의 적용 가능성

의사 결정 절차의 중간 결과에 따라 프로세스 외부 의존성에서 추가 데이터 질의가 필요한 경우가 있다.

예를 들어, 지난 24시간 동안 방문 횟수가 임계치를 초과하면 감사 시스템이 방문자의 접근 레벨을 확인해야 한다고 해보자. 그리고 방문자의 접근 레벨이 모두 데이터베이스에 저장 되어 있다고 가정하자.

레코드를 저장할 때 접근 레벨을 체크하기 때문에 함수에 숨은 입력이 생긴다.

```java
public FileUpdate addRecord(FileContent[] files, String visitorName,
														DateTime timeOfVisit, **IDatabase database**)
```

**해결책**

- 어플리케이션 서비스에서 미리 디렉터리 내용과 방문자 접근 레벨을 수집할 수 있다.
    - 단점 : 성능이 저하된다. 접근 레벨이 필요 없는 경우에도 무조건 데이터베이스에 질의한다.
    - 그러나, 비즈니스 로직과 외부 시스템과의 통신을 계속 분리할 수 있다
- AuditManager에서는 isAccessLevelCheckRequired() 같은 새로운 메서드를 가져야 한다.
    
    어플리케이션 서비스에서 addRecord() 호출 전에 이 메서드를 호출하고 true를 반환하면 서비스는 데이터베이스에서 접근 레벨을 가져온 후 addRecord()에 전달한다.
    
    - 성능 향상을 위해 분리를 다소 완화했다.

### 성능 단점

- 시스템은 프로세스 외부 의존성을 더 많이 호출하기 때문에 그 결과로 성능이 떨어진다.
    - 읽고-결정하고-실행하기 방식을 따르도록 작업 디렉터리의 모든 파일을 읽었다.
- 함수형 아키텍처와 전통적인 아키텍처 사이의 선택은 성능과 코드 유지 보수성 간의 절충이다.

### 코드베이스 크기 증가

- 함수형 아키텍처에서는 함수형 코어와 가변 셸 사이를 명확하게 분리해야 한다.
- 코드 복잡도가 낮아지고 유지 보수성이 향상 되지만, 초기에 코딩이 더 필요하다.
- 항상 시스템의 복잡도와 중요성을 고려해 함수형 아키텍처를 전략적으로 적용해야 한다.
- 함수형 방식에서 순수성에 많은 비용이 든다면 순수성을 따르지 말라.
- 대부분의 경우 출력 기반 스타일과 상태 기반 스타일을 조합하게 되며, 통신 기반 스타일을 약간 섞어도 괜찮다.
