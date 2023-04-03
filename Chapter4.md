# 4장.  좋은 단위 테스트의 4대 요소

## 좋은 단위 테스트 스위트의 특성

- 개발 주기에 통합
- 코드베이스의 가장 중요한 부분만을 대상
- 최소한의 유지비로 최대 가치
    - **가치 있는 테스트 식별**
    - 가치 있는 테스트 작성

## 좋은 단위 테스트의 네 가지 특성

- 회귀 방지
- 리팩토링 내성
- 빠른 피드백
- 유지 보수성

### 1. 회귀 방지 (회귀 = 소프트웨어 버그)

개발할 기능이 많을수록, 새로운 릴리즈에서 기능이 하나라도 고장 날 가능성이 높다.

코드베이스가 커질수록 잠재적인 버그에 더 많이 노출된다.

**회귀 방지 지표에 대한 테스트 점수**

- 테스트 중에 실행되는 코드의 양
    - 실행되는 코드의 양이 많을수록 테스트에서 회귀가 나타날 가능성이 높다.
- 코드 복잡도
    - 복잡한 비즈니스 로직을 나타내는 코드가 보일러플레이트 코드보다 훨씬 중요
- 코드의 도메인 유의성
    - 코드가 제품의 문제 도메인에 얼마나 의미 있는가?

**회귀 방지 지표를 극대화하려면 테스트가 가능한 한 많은 코드를 실행하는 것을 목표로 해야 한다.**

### 2. 리팩토링 내성

테스트를 ‘실패’로 바꾸지 않고 기본 어플리케이션 코드를 리팩터링 할 수 있는지에 대한 척도

**거짓 양성**

- 실제로 기능이 의도한 대로 작동하지만 테스트는 실패를 나타내는 결과
- 보통 코드를 리팩터링(구현을 수정하지만 식별할 수 있는 동작은 유지) 할 때 발생함
- 거짓 양성은 적을수록 좋다.

**리팩터링 내성 지표는 얼마나 많이 거짓 양성이 발생하는지 살펴봐야 한다.**

**거짓 양성이 중요한 이유**

- 테스트는 기존 기능이 고장 났을 때 조기 경고를 제공한다.
    - 타당한 이유 없이 실패하면, 코드 문제에 대응하는 능력과 의지가 희석되고 시간이 지나면 무뎌지게 된다.
- 테스트 덕분에 코드 변경이 회귀로 이어지지 않을 것이라고 확신한다.
    - 거짓 양성이 빈번하게 나타나면 테스트 스위트에 대한 신뢰가 서서히 떨어지고, 더 이상 믿을 만한 안전망으로 인식하지 않는다. 그러면, 회귀를 피하기 위해 코드 변경을 최소화 한다.

**거짓 양성의 원인**

- 테스트와 테스트 대상 시스템(SUT)의 구현 세부 사항이 많이 결합 할수록 허위 경보가 많이 생긴다.
- 거짓 양성이 생길 가능성을 줄이는 방법은 해당 구현 세부 사항에서 테스트를 분리하는 것이다.
- 테스트를 통해 SUT가 제공하는 최종 결과를 검증 하는지 확인해야 한다.

```java
public class Message {
	private String header;
	private String body;
	private String footer;
}

public interface IRenderer {
	String render(Message message);
}

public class MessageRenderer implements IRenderer {
	public List<IRenderer> subRenderers;

	public MessageRenderer() {
		subRenderers = new ArrayList<>();
		subRenderers.add(new HeaderRenderer());
		subRenderers.add(new BodyRenderer());
		subRenderers.add(new FooterRenderer());
	}

  @Override
	public String render(Message message) {
		return subRenderers.stream()
                .map(l -> l.render(message))
                .collect(Collectors.joining());
	}
}
```

```java
@Test
public void MessageRenderer_uses_correct_sub_renderers() {
	MessageRenderer sut = new MessageRenderer();

  List<IRenderer> renderers = sut.subRenderers;

  Assert.assertEquals(3, renderers.size());
  Assert.assertTrue(renderers.get(0) instanceof HeaderRenderer);
  Assert.assertTrue(renderers.get(1) instanceof BodyRenderer);
  Assert.assertTrue(renderers.get(2) instanceof FooterRenderer);
}
```

<img width="444" alt="스크린샷 2023-03-31 오후 8 18 05" src="https://user-images.githubusercontent.com/7659412/229492656-0b53a387-1a2e-4f69-8956-92255d9db792.png">

SUT 알고리즘과 테스트가 결합되어 있어 깨지기 쉽다.

- 회귀 발생 시 조기 경고를 제공하지 않는다. 대부분 기능에 문제가 없음에도 불구하고 경고를 발생시키므로 무시하게 된다.
- 리팩터링에 대한 능력과 의지를 방해한다.

리팩터링 내성을 높이기 위해서는 SUT의 구현 세부 사항과 테스트 간의 결합도를 낮춰야 한다.

```java
@Test
public void Rendering_a_message() {
	IRenderer sut = new MessageRenderer();
	Message message = new Message();
	message.setHeader("h");
	message.setBody("b");
	message.setFooter("f");

	String html = sut.render(message);

	Assert.Equal("<h1>h</h1><b>b</b><i>f</i>", html);
}
```

<img width="469" alt="스크린샷 2023-03-31 오후 8 20 57" src="https://user-images.githubusercontent.com/7659412/229492829-eb777a4e-d932-4bf5-a194-caf1b6ba1c1c.png">

### 첫 번째 특성과 두 번째 특성 간의 본질적인 관계

**테스트 정확도 극대화**

<img width="411" alt="스크린샷 2023-03-31 오후 8 25 25" src="https://user-images.githubusercontent.com/7659412/229492856-b29788c3-6625-4c3d-97f1-8c15afd88ad7.png">

회귀 방지가 훌륭한 테스트는 2종 오류인 거짓 음성의 수를 최소화

리팩터링 내성을 높이기 위해서는 1종 오류인 거짓 양성을 최소화

- 테스트가 버그 있음을 얼마나 잘 나타내는가? - 거짓 음성 제외
- 테스트가 버그 없음을 얼마나 잘 나타내는가? - 거짓 양성 제외

<img width="266" alt="스크린샷 2023-03-31 오후 8 29 29" src="https://user-images.githubusercontent.com/7659412/229492885-f786195f-9f41-4684-a3c9-89b5606f3733.png">

**거짓 양성과 거짓 음성의 중요성**

<img width="326" alt="스크린샷 2023-03-31 오후 8 31 39" src="https://user-images.githubusercontent.com/7659412/229492922-a67ad0be-cbcd-403e-9100-e5a435e8750a.png">

프로젝트 초기에는 리팩터링이 많이 필요 없어 거짓 양성이 그렇게 크게 중요하지 않다.

하지만, 프로젝트가 커지면서 리팩터링이 필요하게 되고 중 거짓 양성이 중요해진다.

## 세 번째 요소와 네 번째 요소 : 빠른 피드백과 유지 보수성

- 빠른 피드백은 단위 테스트의 필수 속성
- 테스트 속도가 빠를수록 테스트 스위트에서 더 많은 테스트를 수행할 수 있고 더 자주 실행할 수 있다.
- 유지 보수성 지표는 유지비를 평가한다.
    - 테스트가 얼마나 이해하기 어려운가?
        - 코드 라인이 적을수록 읽기 쉽다.
    - 테스트가 얼마나 실행하기 어려운가?
        - 테스트가 프로세스 외부 종속성으로 작동하면, 데이터베이스나 네트워크 등 의존성을 상시 운영하는 데 시간을 들여야 한다.

## 이상적인 테스트

좋은 단위 테스트의 4대 특성을 평가 했을 때 네 가지 특성의 가치 추정치를 곱하면 테스트의 가치가 결정 된다.

- 회귀 방지
- 리팩터링 내성
- 빠른 피드백
- 유지 보수성

네 가지 특성 모두에서 최대 점수를 받는 테스트를 만드는 것은 불가능

- 회귀 방지, 리팩터링 내성, 빠른 피드백은 상호 배타적이기 때문이다.
- 셋 중에 하나는 희생해야 한다.

<img width="489" alt="스크린샷 2023-03-31 오후 8 36 01" src="https://user-images.githubusercontent.com/7659412/229492949-e3538950-83c1-4c4f-bd40-d70af8863077.png">

1. 엔드 투 엔드 테스트
    1. 최종 사용자의 관점에서 시스템을 테스트한다.
    2. 많은 코드를 테스트하기 때문에 회귀 방지를 훌륭하게 해낸다.
    3. 거짓 양성에 면역이 돼 리팩터링 내성도 우수하다.
    4. 그대신 속도가 느리다.
2. 간단한 테스트
    1. 간단한 테스트는 매우 빠르게 실행되고 빠른 피드백을 제공한다.
    2. 거짓 양성이 생길 가능성도 상당히 낮다.
    3. 그러나 회귀를 나타낼 수 없다.
3. 깨지기 쉬운 테스트
    1. 실행이 빠르고 회귀를 잡을 가능성이 높지만 거짓 양성이 많은 테스트를 작성하기가 매우 쉽다.
    2. 테스트가 SUT의 내부 구현 세부 사항에 결합 되어 있다.
4. 이상적인 테스트 : 결론
    1. 리팩터링 내성을 포기할 수는 없다. 테스트가 이 특성을 갖고 있는지 여부는 대부분 이진 선택이기 때문에
    2. 회귀 방지와 빠른 피드백 사이에서 자유롭게 움직인다.

<img width="360" alt="스크린샷 2023-03-31 오후 8 37 21" src="https://user-images.githubusercontent.com/7659412/229492996-846ea5d2-53ff-4325-8f3b-1d2276e5589e.png">       

## 대중적인 테스트 자동화 개념 살펴보기

- 테스트 피라미드

<img width="333" alt="스크린샷 2023-03-31 오후 8 36 59" src="https://user-images.githubusercontent.com/7659412/229493016-078830ea-b806-4b2b-8499-acad037438d6.png"> 

    - 테스트 스위트에서 테스트 유형 간의 일정한 비율
    - 각 층의 너비는 테스트 스위트에서 해당 테스트가 얼마나 보편적인지(넓을수록 해당 테스트는 많다.)
    - 층의 높이는 이러한 테스트가 최종 사용자의 동작을 얼마나 유사하게 흉내 내는지 나타내는 척도
    - 피라미드 상단의 테스트는 회귀 방지에 유리하고, 하단은 실행 속도를 강조한다.
    - 일반적으로 테스트 유형 간의 정확한 비율은 피라미트 형태를 유지

<img width="345" alt="스크린샷 2023-03-31 오후 8 38 07" src="https://user-images.githubusercontent.com/7659412/229493044-1a41e4b7-7fa1-4d29-9628-3314968f0abb.png">
        
- 화이트박스 테스트 대 블랙박스 테스트
    - 블랙박스 테스트
        - 시스템의 내부 구조를 몰라도 시스템의 기능을 검사할 수 있는 소프트웨어 테스트 방법
        - 일반적으로 명세와 요구 사항, 어플리케이션이 무엇을 해야 하는지를 중심으로 구축
    - 화이트박스 테스트
        - 어플리케이션의 내부 작업을 검증하는 테스트 방식
        - 테스트는 요구 사항이나 명세가 아닌 소스 코드에서 파생
    - 화이트박스 테스트는 철저하지만 테스트 대상 코드의 특정 구현과 결합돼 있어 깨지기 쉽다.(리팩터링 내성)
    - 리팩터링 내성을 포기할 수 없기 때문에 블랙박스 테스트를 기본으로 선택
    - 테스트를 분석할 때는 화이트박스 테스트
