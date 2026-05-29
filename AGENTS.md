# AGENTS.md

## 1. 프로젝트 개요 및 목적

해당 프로젝트는 DDD 기반 Spring Boot 프로젝트에서 **사용자가 중고 상품을 등록하고 경매 방식으로 판매할 수 있는 중고 상품 경매 서비스**입니다.
코드 에이전트가 반드시 지켜야 하는 **실행 규칙/금지 규칙/검증 절차**를 정의합니다.

- **목표**: 안정성(정합성/멱등성), 확장성(피크 트래픽), 변경 안전성(도메인 경계), 운영 가능성(관측/로그)을 해치지 않는 변경만 허용합니다.
- 이 문서는 "설명서"가 아니라 "규칙집"입니다. 규칙을 충족하지 못하면 작업을 중단합니다.

### 기술 스택
| 분류 | 기술 |
|------|------|
| Backend | Spring Boot, Spring WebFlux, Spring Batch, Spring Security |
| Database | MySQL, ElasticSearch |
| Cache | Redis (Sorted Set, Stream, Distributed Lock) |
| Infra | AWS, Docker, GitHub Actions |
| AI | OpenAI Embedding, OpenAI Vision, LLM Agent |
| 부하 테스트 | K6 |

---

## 2. 빌드 및 테스트 명령어

```bash
# 빌드
./gradlew build

# 테스트 전체 실행
./gradlew test

# 특정 테스트 실행
./gradlew test --tests "com.sh.Ram.*"

# 빌드 + 테스트 스킵
./gradlew build -x test

# 로컬 실행
./gradlew bootRun
```

---

## 3. 코드 스타일 가이드라인

### 패키지 구조
- 도메인 단위로 패키지를 분리합니다.
```
com.sh.Ram
├── auction       # 경매 도메인
├── bid           # 입찰 도메인
├── product       # 상품 도메인
├── member        # 회원 도메인
├── account       # 계좌 도메인
├── redis         # Redis 관련 (Stream, Lock)
├── elasticSearch # ES 관련
└── common        # 공통 예외, 응답 처리
```

### 네이밍 규칙
- 클래스명: 역할이 명확하게 드러나도록 작성합니다. (`BidService`, `AuctionRepository` 등)
- 메서드명: 동사로 시작하며 행위가 명확해야 합니다. (`submitBid`, `processAuction` 등)
- 상수: `static final`로 선언하며 대문자 스네이크 케이스를 사용합니다.
- 하드코딩 금지: 상수는 반드시 `static final` 또는 `Enum`으로 관리합니다.

### 레이어 규칙
- **Controller**: 요청/응답 처리만 담당, 비즈니스 로직 금지
- **Service**: 비즈니스 로직 담당
- **Repository**: DB 접근만 담당
- 엔티티를 Controller 레이어까지 노출 금지 → 반드시 DTO로 변환

### 예외 처리
- 반드시 커스텀 예외를 사용합니다.
```java
// 올바른 예시
throw new BidException("최소 입찰 금액 이상으로 입찰해주세요.");
throw new AuctionException("경매가 진행 중이 아닙니다.");

// 금지
throw new RuntimeException("에러 발생");
```

### 로깅 규칙
- `System.out.println()` 사용 금지 → 반드시 `log.info()`, `log.error()` 사용
- 로그에 민감 정보(비밀번호, 토큰 등) 출력 금지
- 핵심 비즈니스 로직 시작/종료 지점에 로그 추가
```java
log.info("[입찰 진행] 경매번호: {}, 입찰자: {}, 입찰가: {}", auctionId, memberId, bidPrice);
log.error("[입찰 실패] 원인: {}", e.getMessage());
```

---

## 4. 테스트 지침

### 단위 테스트
- 비즈니스 로직은 반드시 단위 테스트를 작성합니다.
- 테스트 메서드명은 한국어로 작성합니다.
```java
@Test
void 입찰_금액이_최소_입찰가보다_낮으면_예외가_발생한다() { ... }

@Test
void 이미_최고_입찰자는_재입찰할_수_없다() { ... }
```

### 동시성 테스트
- 동시성 이슈가 발생할 수 있는 구간은 반드시 동시성 테스트를 포함합니다.
- K6를 활용한 부하 테스트로 성능 검증을 진행합니다.
```javascript
// K6 동시성 테스트 예시
export let options = {
    vus: 100,
    iterations: 100,
};
```

### 성능 테스트 기준
| 항목 | 기준 |
|------|------|
| 평균 응답시간 | 3s 이하 |
| TPS | 20 req/s 이상 |
| 동시성 | 100명 동시 입찰 시 1명만 성공 |

---

## 5. 보안 고려 사항

- JWT 토큰 검증은 반드시 Filter 레이어에서 처리합니다.
- 민감한 정보(비밀번호, 토큰 등)는 절대 로그에 출력하지 않습니다.
- .env 파일은 절대 git으로 관리하지 않습니다.
- 민감한 정보 외부 API키 , 토큰값, 비밀번호에 대한 값은 하드코딩하여 사용하지 않고, .env파일에 작성하여 해당 값들을 관리합니다.
- 외부 API 키는 환경변수로 관리하며 코드에 하드코딩하지 않습니다.
- SQL Injection 방지를 위해 반드시 JPA 또는 QueryDSL을 사용합니다.
- Redis 분산 락 해제 시 반드시 Lua 스크립트로 원자적으로 처리합니다.

---

## 6. 코드 리뷰 / PR 가이드라인

### 코드 리뷰 우선 순위
리뷰 시 아래 순서로 중요도를 판단합니다.

### P0

* 컴파일 실패
* 애플리케이션 기동 실패
* 데이터 손실 가능성
* 인증/인가 우회
* 동시성 정합성 붕괴

### P1

* Redis/DB 정합성 문제
* API 계약 불일치
* 트랜잭션 범위 문제
* N+1 및 성능 병목

### P2

* 유지보수성 저하
* 과도한 복잡도
* 테스트 부족

### P3

* 가독성
* 네이밍
* 스타일

### 리뷰 출력 형식

가능하면 아래 형식으로 리뷰합니다.

| 심각도 | 위치 | 문제 | 영향 | 개선안 |
| --- | -- | -- | -- | --- |

리뷰 마지막에는 다음 내용을 요약합니다.

* 병합 가능 여부
* 반드시 수정이 필요한 항목
* 추가 검증이 필요한 항목

### PR 규칙
- 반드시 코드 리뷰는 한국어로 작성합니다.
- 하나의 PR은 하나의 기능 단위로 작성합니다.
- PR 제목 형식: `[feat/fix/refactor/docs] 작업 내용 요약`

```
[feat] Redis Stream 기반 ES 비동기 색인 구현
[fix] 입찰 동시성 처리 Redis 분산락 적용
[refactor] Cursor 기반 페이지네이션으로 성능 개선
[docs] AGENTS.md 업데이트
```

### PR 본문 형식
```
## 작업 내용
- 변경 사항 요약

## 변경 이유
- 기존 문제점
- 개선 방향

## 테스트 방법
- 테스트 시나리오

## 관련 이슈
- #이슈번호
```

### 브랜치 전략
```
main         # 배포 브랜치
develop      # 개발 통합 브랜치
feat/*       # 기능 개발
fix/*        # 버그 수정
refactor/*   # 리팩토링
doccs/*      # 
```

---

## 7. 금지 규칙

### 트랜잭션
- `@Transactional` 범위 안에서 외부 API 호출 금지 (네트워크 지연으로 DB 커넥션 점유)
- `@Transactional` 범위 안에서 대용량 파일 처리 금지
- 불필요한 `@Transactional(readOnly = false)` 남발 금지

### 동시성
- 동시성 이슈가 발생할 수 있는 구간에 락 없이 DB 직접 접근 금지
- 입찰, 재고 차감 등 정합성이 중요한 로직은 반드시 Redis 분산락 적용

### 코드 품질
- 엔티티를 Controller 레이어까지 노출 금지
- `System.out.println()` 사용 금지
- 하드코딩 금지
- `@Async` 사용 시 트랜잭션 컨텍스트 공유 불가 주의 → Redis Stream 사용 권장

---

## 8. 도메인 규칙

### 경매 (Auction)
- 상태는 `PENDING → PROGRESS → CLOSED` 순서로만 변경 가능
- `CLOSED` 상태에서는 입찰 불가

### 입찰 (Bid)
- 현재가보다 낮은 금액으로 입찰 불가
- 최고 입찰자는 재입찰 불가
- 입찰 시 반드시 Redis 분산락으로 동시성 처리
- 입찰 성공 시 이전 최고 입찰자 예약금 즉시 해제

### 계좌 (Account)
- 가용잔액 = 계좌잔액 - 예약금
- 가용잔액 이하로 입찰 불가
- 입찰 시 예약금 선 확보 후 처리

### 상품 (Product)
- 상품 등록/수정/삭제 시 Redis Stream으로 ES 색인 이벤트 발행
- ES 색인 실패 시 재시도 3회 초과하면 ProductIndexFailLog에 저장

---

## 9. 인프라 / 배포 규칙

- Blue-Green 배포 전략을 사용합니다.
- 배포 전 반드시 테스트를 통과해야 합니다.
- 환경변수는 GitHub Secrets로 관리합니다.
- Docker 컨테이너 기반으로 배포합니다.