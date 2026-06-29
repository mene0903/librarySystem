# 📚 도서 관리 플랫폼

> 대출 · 반납 · 추천이 가능한 도서관 관리 시스템  
> Spring Boot 기반 백엔드 개인 구현 프로젝트

---

## 🗂 목차

- [프로젝트 소개](#프로젝트-소개)
- [기술 스택](#기술-스택)
- [패키지 구조](#패키지-구조)
- [주요 기능](#주요-기능)
- [시스템 아키텍처](#시스템-아키텍처)
- [ERD](#erd)
- [핵심 구현 포인트](#핵심-구현-포인트)
- [실행 방법](#실행-방법)

---

## 프로젝트 소개

한국어 도서 DB 부재 문제를 외부 API 3개 조합으로 해결하고, 사용자 대출 이력 기반 추천 알고리즘을 구현한 도서관 관리 플랫폼입니다.

- **기간**: 2024년 (데이터베이스 수업 프로젝트)
- **인원**: 1인 개발 (팀 프로젝트로 진행, 전체 기능 단독 구현)
- **역할**: ERD 설계, 전체 백엔드 구현, 외부 API 연동 설계, 추천 알고리즘 설계

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java |
| Framework | Spring Boot |
| ORM | Spring Data JPA / Hibernate |
| Database | MySQL |
| 외부 API | 알라딘 API / 네이버 도서 API / 국립중앙도서관 API |
| 동시성 | CompletableFuture / ExecutorService |
| 빌드 | Gradle |
| View | Thymeleaf |

---

## 패키지 구조

```
src/main/java/com/group/library_system/
├── api/
│   ├── dto/                     # 외부 API 응답 DTO
│   ├── AladinApiConfig.java     # 알라딘 API 설정
│   ├── AladinBookApiService.java
│   ├── NaverApiConfig.java      # 네이버 API 설정
│   ├── NaverBookApiService.java
│   ├── NicApiConfig.java        # 국립중앙도서관 API 설정
│   └── NiciBookApiService.java
├── controller/
│   └── BookController.java
├── repository/
│   ├── Book.java
│   ├── BookRecommend.java
│   ├── BookRecommendRepository.java
│   ├── BookRepository.java
│   ├── Borrow.java
│   ├── BorrowRepository.java
│   ├── User.java
│   └── UserRepository.java
├── service/
│   ├── BookRecommendService.java  # 추천 알고리즘 (병렬 처리 포함)
│   ├── BookService.java           # 도서 검색 / 등록
│   ├── BorrowService.java         # 대출 / 반납 / 연장
│   └── UserService.java           # 사용자 관리
└── LibrarySystemApplication.java
```

> **설계 포인트**: 외부 API 호출 로직을 `api` 패키지로 분리하여 Service 레이어가 비즈니스 로직에만 집중할 수 있도록 구성했습니다.

---

## 주요 기능

### 📖 도서 등록
- ISBN 입력 → 알라딘 API 자동 조회 → DB 저장
- API 응답(JSON) → DTO → Entity 변환 파이프라인 구현
- 동일 ISBN 중복 등록 시 예외 처리

### 🔍 도서 검색
- 제목 / 저자 키워드 검색 → 네이버 도서 API 호출
- ISBN 검색 → 알라딘 API 상세 조회

### 📤 대출
- 중복 대출 시 예외 처리
- Borrow, Book 테이블 트랜잭션으로 동시 업데이트
- 대출 기간 5일 고정

### 📥 반납
- Borrow 기록 삭제 + Book 상태 업데이트
- 반납 시 사용자 통계(하루 평균 독서 페이지, 선호 카테고리) 자동 갱신

### 🔄 연장
- 연장 1회(+5일) 제한 비즈니스 로직 적용

### 🎯 도서 추천
- 신규 사용자 (대출 기록 없음) → 전체 베스트셀러 추천
- 기존 사용자 → 선호 카테고리 베스트셀러 + 하루 평균 독서 페이지 ± 50 범위 필터링
- 확장판 · 시리즈 중복 노출 방지 (제목 내 `-` 패턴 감지 후 제거)
- 알라딘 후보 200권을 병렬로 조회 후 국립중앙도서관 API로 페이지 필터링 → 최대 20권 추천

### 📊 사용자 통계
- 총 대출 권수, 하루 평균 독서 페이지, 카테고리 선호도 자동 집계
- `borrowCountMean = 누적 (페이지 / 대출일수) / 총 대출 횟수`

---

## 시스템 아키텍처

```
Client (Browser)
  └── BookController
        ├── BookService
        │     ├── NaverBookApiService   → 키워드 검색
        │     └── AladinBookApiService  → ISBN 상세 조회 / DB 저장
        ├── BorrowService
        │     └── BookRepository / BorrowRepository / UserRepository
        ├── UserService
        │     └── UserRepository
        └── BookRecommendService
              ├── AladinBookApiService  → 카테고리별 베스트셀러 200권 (병렬 4요청)
              ├── NiciBookApiService    → 페이지 수 조회 (병렬 200요청)
              └── BookRecommendRepository

MySQL DB
  ├── User          (사용자 정보 + 통계)
  ├── Book          (도서 정보)
  ├── Borrow        (대출 기록)
  └── BookRecommend (사용자별 선호 카테고리)

외부 API 역할 분리
  ├── 알라딘 API         → 베스트셀러, ISBN 상세 정보, 카테고리별 베스트셀러
  ├── 네이버 도서 API    → 제목 / 저자 텍스트 검색 (알라딘 미지원 기능)
  └── 국립중앙도서관 API → 페이지 수 조회 (알라딘 호출 한도 우회)
```

---

## ERD

| 테이블 | 주요 컬럼 |
|--------|----------|
| **User** | 회원번호, 이름, ID, 비밀번호, 생년월일, 핸드폰 번호, 가입일, 총 대출 권수, 누적 평균 페이지, 일 평균 페이지 |
| **Book** | ISBN, 제목, 저자, 카테고리 ID, 페이지 수, 리뷰 순위, 표지 이미지, 반납 예정일 (알라딘 API 자동 채움) |
| **Borrow** | 대출일, 반납 예정일, 연장 횟수, User FK, Book FK |
| **BookRecommend** | 사용자 선호 카테고리 ID, 카테고리 대출 횟수, User FK |

---

## 핵심 구현 포인트

### 1. 외부 API 3개를 역할별로 분리 설계

한국어 도서 DB가 없어 외부 API를 활용했으며, 각 API의 특성과 제약을 파악해 역할을 분리했습니다.

| API | 선택 이유 | 담당 역할 |
|-----|----------|----------|
| 알라딘 | ISBN 기반 상세 정보, 카테고리별 베스트셀러 제공 | 베스트셀러 조회, 대출 시 DB 저장, 카테고리 추천 |
| 네이버 도서 | 제목·저자 텍스트 검색 지원 (알라딘 미지원) | 도서 키워드 검색 |
| 국립중앙도서관 | 개별 도서 페이지 수 조회 가능 | 추천 알고리즘 페이지 필터링 |

알라딘 API의 개별 도서 호출 한도 문제를 국립중앙도서관 API로 분산해 해결했습니다.

### 2. 추천 알고리즘 — 병렬 처리로 성능 확보

200권의 후보 도서를 순차 조회하면 API 응답 시간이 누적되어 수십 초가 걸리는 문제가 있었습니다. `CompletableFuture` + `ExecutorService(30 threads)`로 알라딘 4개 요청(200권)과 국립중앙도서관 페이지 조회를 병렬 처리해 응답 시간을 단축했습니다.

```
알라딘 4요청 (50권씩) → 병렬 수행 → 200권 후보 합산
       ↓
국립중앙도서관 200요청 → 병렬 수행 → 페이지 범위 필터링
       ↓
최대 20권 추천 결과 반환
```

추천 기준은 두 가지입니다.
- **선호 카테고리**: 가장 많이 대출한 카테고리 ID → 알라딘 카테고리별 베스트셀러
- **하루 평균 독서 페이지**: `누적 (페이지 ÷ 대출일수) ÷ 총 대출 횟수` → 평균 ± 50 페이지 범위 필터링

### 3. 시리즈 · 확장판 중복 제거

추천 결과에 동일 시리즈가 중복 노출되는 문제가 있었습니다. 제목에서 `-` 이후 문자열과 말미 숫자를 제거한 `cleanTitle`과 첫 번째 저자명을 조합해 고유 키를 생성하고, 중복 키는 추천 리스트에서 제외했습니다. 1편을 추천하면 독자가 자연스럽게 다음 편으로 이어진다는 판단에서입니다.

### 4. API 호출 결과 인메모리 캐싱

국립중앙도서관 API의 동일 ISBN 중복 호출을 방지하기 위해 `ConcurrentHashMap`으로 페이지 수를 캐싱했습니다. 같은 ISBN이 여러 사용자에게 추천 후보로 등장할 경우 API 호출 없이 캐시에서 즉시 반환합니다.

> **개선 여지**: 현재는 JVM 메모리에 저장되어 서버 재시작 시 초기화됩니다. 추후 Redis 등 외부 캐시 도입을 고려할 수 있습니다.

### 5. 트랜잭션 관리
대출/반납 시 여러 테이블(Book, Borrow, User 통계, BookRecommend)이 동시에 업데이트되므로, `@Transactional`로 묶어 데이터 일관성을 보장했습니다.

---

## 실행 방법

```bash
# 1. 레포지토리 클론
git clone https://github.com/mene0903/librarySystem.git

# 2. application.properties에 DB 및 API 키 설정
spring.datasource.url=jdbc:mysql://localhost:3306/library
spring.datasource.username=your_username
spring.datasource.password=your_password
aladin.api.key=your_aladin_api_key
naver.client.id=your_naver_client_id
naver.client.secret=your_naver_client_secret
library.api.key=your_nationalibrary_api_key

# 3. 빌드 및 실행
./gradlew bootRun
```
