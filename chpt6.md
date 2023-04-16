# 단위 테스트 스타일

## 단위 테스트의 세 가지 스타일

### 출력 기반 테스트 (함수형)
- 테스트 대상 시스템에 입력을 넣고 생성되는 출력을 검증하는 방식

![](attachments/스크린샷%202023-04-16%20오전%2010.28.47.png)
![](attachments/스크린샷%202023-04-16%20오전%2010.29.01.png)

- 내부 컬렉션에 상품을 추가하거나 데이터베이스에 저장하지 않음
	- 즉, 사이드이펙트가 없음

### 상태 기반 테스트
- 작업이 완료된 후 시스템 상태를 확인
	- 시스템 상태 예시 : SUT, 협력자, DB, 파일시스템 등의 상태

![](attachments/스크린샷%202023-04-16%20오전%2010.31.58.png)

- `Products` collection 을 검증하는 모습

### 통신 기반 테스트
- 목을 사용해 데스트 대상 시스템과 협력자 간 통신 검증

![](attachments/스크린샷%202023-04-16%20오전%2010.35.01.png)

## 단위 테스트 스타일 비교
### 회귀 방지, 피드백 속도
- 단위 테스트 스타일과 상관관계 없음

### 리팩터링 내성 지표
- 출력 기반
	- best
	- 테스트가 테스트 대상 메서드에만 결합되므로 거짓 양성 방지 우수
- 상태 기반
	- bad
	- 거짓 양성이 되기 쉬움
	- 테스트 대상 메서드 외에 클래스 상태와 테스트 결합되어 있음
- 통신 기반
	- worst
	- 거짓 양성이 되기 가장 쉬움
		- 스텁과 상호 작용을 검증할 때 거짓 양성 발생
	- 통신 기반 테스트를 사용할 때는 항상 신중하자

### 유지보수성 지표
- 기준
	- 테스트를 이해하기 얼마나 쉬운가?
	- 테스트를 실행하는 게 얼마나 쉬운가?
- 출력 기반
	- best
	- 짧고 간결하므로 유지보수 쉬움
- 상태 기반
	- bad
	- 출력 검증보다 많은 space 차지
		- 대안
			- 헬퍼 메서드
			- 동등 멤버 정의
				- 코드 오염 발생 가능
					- 단위 테스트를 가능하게 하거나 단순하게 하기 위한 목적만으로 제품 코드베이스를 오염시키는 것

![](attachments/스크린샷%202023-04-16%20오전%2010.44.06.png)

![](attachments/스크린샷%202023-04-16%20오전%2010.47.39.png)

![](attachments/스크린샷%202023-04-16%20오전%2010.47.50.png)

- 통신 기반
	- worst
	- 테스트 대역과 상호 작용 검증 설정 -> 공간 많이 차지

### 단위 테스트 스타일 비교 결론
- 출력 기반 테스트를 지향하라
- 함수형으로 작성된 코드에만 적용 가능

## 함수형 아키텍처 이해
### 함수형 프로그래밍이란?
- 수학적 함수를 사용한 프로그래밍
	- 숨은 입출력이 없는 메서드
		- 모든 입출력은 메서드 시그니처에 명시되어 있음
	- 호출 횟수와 관계없이 주어진 입력에 대해 동일한 출력을 생성하는 메서드
- 숨은 입출력 유형
	- 사이드 이펙트
		- 메서드 시그니처에 표시되지 않은 출력
		- e.g. 클래스 인스턴스 상태 변경, DB 변경
	- 예외
	- 내외부 상태에 대한 참조
		- 메서드 시그니처에 표시되지 않은 입력
		- e.g. 데이터베이스에서 데이터 조회해오기, private mutable field 참조
- 메서드가 수학적 함수인지 판별하는 방법
	- 프로그램의 동작을 변경하지 않고 해당 메서드에 대한 호출을 반환 값으로 대체할 수 있는지 확인
	- 참조 투명성

```java
// 참조 투명성 있음
public int increment(int x){
	return x+1;
}
```

```java
// 참조투명성 없음
int x = 0;
public int increment(){
	x++;
	return x;
}
```

- 사이드 이펙트 발생 예시 코드
![](attachments/스크린샷%202023-04-16%20오전%2011.03.30.png)

### 함수형 아키텍처란?
- 함수형 프로그래밍의 목표
	- 사이드이펙트를 제거하는 것이 아니라 비즈니스 로직을 처리하는 코드와 사이드이펙트를 일으키는 코드를 분리하는 것
- 함수형 아키텍처
	- 사이드 이펙트를 다루는 코드를 최소화하면서 순수함수(불변) 방식으로 작성한 코드의 양을 극대화
- 코드 유형 분류
	- 결정을 내리는 코드
		- 사이드이펙트 없음
		- 수학적 함수 사용하여 작성
		- 함수형 코어(functional core)
		- 불변 코어(immutable core)
	- 결정에 따라 작용하는 코드
		- 수학적 함수에 의해 이뤄진 결정을 데이터베이스 변경과 같이 가시적인 부분으로 변환
		- 가변 셸(mutable shell)

![](attachments/스크린샷%202023-04-16%20오후%2012.05.36.png)

1. 가변 셸에서 모든 입력 수집
2. 함수형 코어는 decision 생성
3. 가변 셸에서 decision 을 사이드이펙트로 변환


### 함수형 아키텍처와 육각형 아키텍처 비교
- 공통점
	- 도메인 계층 - 어플리케이션서비스 계층 분리 / 결정 - 실행 분리 유사
	- 의존성 간의 단방향 흐름
		- 함수형 아키텍처의 불변 코어는 가변 셸에 의존하지 않음
- 차이점
	- 사이드이펙트 처리
		- 함수형 아키텍처
			- 모든 사이드이펙트를 가변 셸에서 처리
		- 육각형 아키텍처
			- 도메인 계층 내에서의 사이드이펙트는 허용
				- e.g. 클래스 인스턴스 상태 변경 가능

#### 함수형 아키텍처와 출력 기반 테스트로의 전환 예시

### 감사 시스템 예시

![](attachments/스크린샷%202023-04-16%20오후%201.05.30.png)

### 초안

```java
public class AuditManager {  
  
   private int maxEntriesPerFile;  
   private String directoryName;  
  
   public AuditManager(int maxEntriesPerFile, String directoryName) {  
      this.maxEntriesPerFile = maxEntriesPerFile;  
      this.directoryName = directoryName;  
   }  
  
   public void addRecord(String visitorName, Date timeOfVisit) {  
      Path directory = Paths.get(directoryName);  
      List<Path> files = Files.walk(directory).collect(Collectors.toList());  
      List<IndexFile> sorted = sortByIndex(files);  
  
      String newRecord = visitorName + ";" + timeOfVisit;  
  
      if (sorted.size() == 0) {  
         Path newFile = Paths.get(directoryName + "audit_1.txt");  
         Files.write(newFile, newRecord.getBytes());  
      }  
  
      IndexFile lastIndexFile = sorted.get(sorted.size() - 1);  
      File lastFile = lastIndexFile.getFile();  
      List<String> lines = Files.readAllLines(lastFile);  
  
      if (lines.size() < maxEntriesPerFile) {  
         lines.add(newRecord);  
         String newContent = String.join("\r\n", lines);  
         Files.write(lastFile, newContent);  
      } else {  
         int newIndex = lastIndexFile.index + 1;  
         String newName = "audit_" + newIndex + ".txt";  
         String newFile = Paths.get(directoryName + newName);  
         Files.write(newFile, newRecord.getBytes());  
      }  
   }  
  
   private List<IndexFile> sortByIndex(List<Path> files) {  
      return null;  
   }  
  
   public static class IndexFile{  
      private int index;  
      Path file;  
  
      public int getIndex() {  
         return index;  
      }  
  
      public Path getFile() {  
         return file;  
      }  
   }  
  
}

```

- 파일 시스템과 밀접하게 연결돼 있어 테스트하기 어렵다. (실제로 작업 디렉터리를 두고 테스트를 진행해야 함)
- 빠른 피드백 bad
- 유지 보수성 bad


### 개선 : 파일 시스템 연산을 별도 클래스로 도출
```java
public class AuditManager2 {  
  
   private int maxEntriesPerFile;  
   private String directoryName;  
   private FileSystem fileSystem;  
  
   public AuditManager2(int maxEntriesPerFile, String directoryName, FileSystem fileSystem) {  
      this.maxEntriesPerFile = maxEntriesPerFile;  
      this.directoryName = directoryName;  
      this.fileSystem = fileSystem;  
   }  
  
   public void addRecord(String visitorName, Date timeOfVisit) {  
      List<IndexFile> files = (List<IndexFile>)fileSystem.getFiles(directoryName);  
      List<IndexFile> sorted = sortByIndex(files);  
      String newRecord = visitorName + ";" + timeOfVisit;  
  
      if (sorted.size() == 0) {  
         Path newFile = Paths.get(directoryName + "audit_1.txt");  
         fileSystem.writeAllText(newFile, newRecord);  
      }  
  
      IndexFile lastIndexFile = sorted.get(sorted.size() - 1);  
      File lastFile = lastIndexFile.getFile();  
      List<String> lines = Files.readAllLines(lastFile);  
  
      if (lines.size() < maxEntriesPerFile) {  
         lines.add(newRecord);  
         String newContent = String.join("\r\n", lines);  
         fileSystem.writeAllText(lastFile, newContent);  
      } else {  
         int newIndex = lastIndexFile.index + 1;  
         String newName = "audit_" + newIndex + ".txt";  
         String newFile = Paths.get(directoryName + newName);  
         fileSystem.writeAllText(newFile, newRecord);  
      }  
   }  
}  
  
interface FileSystem {  
   List<IndexFile> getFiles(String directoryName);  
  
   void writeAllText(String filePath, String content);  
  
   List<String> readAllLines(String filePath);  
}
```

```java
@Test  
void a_new_file_is_created_when_the_current_file_overflows() {  
   FileSystem fileSystemMock = mock(FileSystem.class);  
   when(fileSystemMock.getFiles("audits")).thenReturn(  
      List.of(new AuditManager2.IndexFile("audit_1.txt"), new AuditManager2.IndexFile("audit_2.txt")));  
   when(fileSystemMock.readAllLines("audit_2.txt")).thenReturn(List.of("Peter; 2023-04-06T16:30:00"));  
   AuditManager2 sut = new AuditManager2(3, "audits", fileSystemMock);  
  
   sut.addRecord("Alice", new Date());  
   verify(fileSystemMock, times(1)).writeAllText("audit_3.txt", "Alice;2023-04-06T17:30:00");  
}
```

- AuditManager 가 파일 시스템으로부터 분리되어 공유 의존성이 사라지고 테스트를 독립적으로 실행 가능
- 문제점
	- Mocking 위해 복잡한 설정 필요
	- 테스트 가독성 떨어짐

### 함수형 아키텍처로 리팩터링
```java
public class AuditManager3 {  
  
   private int maxEntriesPerFile;  
  
   public AuditManager3(int maxEntriesPerFile) {  
      this.maxEntriesPerFile = maxEntriesPerFile;  
   }  
  
   public FileUpdate addRecord(FileContent[] files, String visitorName, Date timeOfVisit) {  
      List<IndexFile> sorted = sortByIndex(files);  
  
      String newRecord = visitorName + ";" + timeOfVisit;  
  
      if (sorted.size() == 0) {  
         return new FileUpdate("audit_1.txt", newRecord);  
      }  
  
      IndexFile lastIndexFile = sorted.get(sorted.size() - 1);  
      File lastFile = lastIndexFile.getFile();  
      List<String> lines = Files.readAllLines(lastFile);  
  
      if (lines.size() < maxEntriesPerFile) {  
         lines.add(newRecord);  
         String newContent = String.join("\r\n", lines);  
         return new FileUpdate(lastFile, newContent);  
      } else {  
         int newIndex = lastIndexFile.index + 1;  
         String newName = "audit_" + newIndex + ".txt";  
         String newFile = Paths.get(directoryName + newName);  
         return new FileUpdate(newFile, newRecord);  
      }  
   }  
  
}  
  
class Persister{  
  
   public List<FileContent> readDirectory(String directoryName) {  
      return Files.walk(directoryName).map(path -> new FileContent(path.getFileName(), Files.readAllLines(path)));  
   }  
  
   public void applyUpdate(String directoryName, FileUpdate update) {  
      String filePath = Paths.get(directoryName + update.fileName);  
      Files.write(filePath, update.newContent);  
   }  
}  
  
class FileContent{  
   public String fileName;  
   public List<String> lines;  
  
   public FileContent(String fileName, List<String> lines) {  
      this.fileName = fileName;  
      this.lines = lines;  
   }  
}  
  
class FileUpdate{  
   public String fileName;  
   public String newContent;  
  
   public FileUpdate(String fileName, String newContent) {  
      this.fileName = fileName;  
      this.newContent = newContent;  
   }  
}  
  
interface FileSystem {  
   List<IndexFile> getFiles(String directoryName);  
  
   void writeAllText(String filePath, String content);  
  
   List<String> readAllLines(String filePath);  
}
```

```java
class ApplicationService{  
   private String directoryName;  
   private AuditManager3 auditManager;  
   private Persister persister;  
  
   public ApplicationService(String directoryName, AuditManager3 auditManager, Persister persister) {  
      this.directoryName = directoryName;  
      this.auditManager = auditManager;  
      this.persister = persister;  
   }  
  
   public void addRecord(String visitorName, Date timeOfVisit) {  
      List<FileContent> files = persister.readDirectory(directoryName);  
      FileUpdate update = auditManager.addRecord(files, visitorName, timeOfVisit);  
      persister.applyUpdate(directoryName, update);  
   }  
}
```



![](attachments/스크린샷%202023-04-16%20오후%202.11.30.png)

- Persister
	- 가변 셸
- AuditManager
	- 함수형 코어
	- 디렉토리 경로 대신 FileContent 를 입력받음
	- 파일 변경하지 않고 FileUpdate 명령 반환


```java
@Test  
void a_new_file_is_created_when_the_current_file_overflows() {  
   AuditManager3 sut = new AuditManager3(3);  
   List<FileContent> files = List.of(new FileContent("audit_1.txt", List.of("")),  
      new FileContent("audit_2.txt", List.of(  
         "Peter; 2020-04-06T16:30:00"  
      )));  
  
   FileUpdate update = sut.addRecord(files, "Alice", "2020-04-07T16:30:00");  
  
   Assertions.assertThat("audit_3.txt", update.fileName);  
   Assertions.assertThat("Alice;2020-04-07T16:30:00", update.newContent);  
}
```

- 빠른 피드백 개선
- 복잡한 목 설정이 필요 없고 단순한 입출력만 남음
	- 테스트 가독성 향상


## 함수형 아키텍처의 단점
### 함수형 아키텍처 적용 가능성
- 앞선 예제는 함수형 코어 이전에 입력 데이터를 모두 수집할 수 있어서 함수형 아키텍처가 잘 작동했음
- 그러나 실전에서는 함수형 코어 중간에 프로세스 외부 의존성에게 데이터를 추가로 질의해야 할 수도 있음
	- e.g. 방문 횟수가 임계치를 초과하면 방문자의 접근 레벨을 DB 로부터 조회해야 하는 경우 -> '접근레벨' 이라는 숨은 입력이 생겨남


- 대안1) 어플리케이션서비스에서 접근레벨 또한 함께 조회해서 넘겨줌
	- 단점
		- 접근레벨이 필요없는 경우에도 DB 를 조회하는 비용 발생
- 대안2) `AuditManager.isAccessLevelCheckRequired()` 메서드를 정의하고 어플리케이션서비스에서 사전에 이 메서드를 호출
	- 단점
		- DB를 호출할 것인지에 대한 결정이 어플리케이션서비스에게 넘어가버림

### 성능상 단점
- 함수형 아키텍처는 read-decide-act 방식 적용을 위해 작업 디렉토리에 있는 모든 파일의 내용까지 읽어야 했음.
	- 반면 초안과 Mock 버전에서는 모든 파일을 읽지 않고 마지막 파일 내용만 읽음
- 함수형 아키텍처와 전통적인 아키텍처 사이의 선택은 성능 or 유지보수성 간의 절충
- 성능이 그다지 중요하지 않은 시스템이라면 함수형 아키텍처를 사용해 유지 보수성을 향상 시키는 것을 권장

### 코드베이스 크기 증가
- 너무 단순한 코드 혹은 비즈니스 관점에서 그다지 중요하지 않은 코드라면 함수형 아키텍처를 적용하는 것은 권장 X
- 대부분 프로젝트에서는 모든 도메인 모델을 불변으로 만들 수는 없기 때문에 통신 기반, 상태 기반 스타일을 섞는 것도 괜찮다. 
- 모든 테스트를 출력기반으로 바꿀 순 없겠지만 가능한 많은 테스트를 출력기반으로 전환하자!


