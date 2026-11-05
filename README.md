# Nailroad BE

Nailroad 백엔드입니다.


## 코드 컨벤션

### 네이밍

- 이름은 의미를 쉽게 이해할 수 있는 단어로 작성하고, 가능하면 단어를 줄여 쓰지 않습니다.
- 패키지명은 소문자로 작성합니다.
- 클래스명은 대문자로 시작하는 `UpperCamelCase`를 사용합니다. 예를 들어 `UserController`로 작성합니다.
- 변수명과 메서드명은 소문자로 시작하는 `lowerCamelCase`를 사용합니다. 예를 들어 `userAddress`, `findUser`로 작성합니다.
- 이름은 단수형을 기본으로 하며, 여러 값을 나타낼 때 복수형을 사용합니다.
- Java 소스 파일명은 클래스명과 동일하게 작성합니다.
- URL과 일반 파일명은 `kebab-case`를 사용합니다. 예를 들어 `/api/v1/user-email-page`로 작성합니다.
- Gradle 설정과 GitHub 템플릿 등 도구가 인식하는 파일명은 해당 도구의 규칙을 따릅니다.

### DB

- 테이블명과 컬럼명은 소문자 `snake_case`로 작성합니다.
- 테이블명은 단수형을 사용합니다.
- 단일 컬럼 기본 키의 이름은 `id`로 작성합니다.
- 컬럼명은 가능하면 단어를 줄여 쓰지 않습니다. 예를 들어 `user_address`, `user_name`으로 작성합니다.

### DTO와 엔티티

- 요청·응답 DTO는 `record`로 작성합니다.
- 엔티티의 `@Builder`는 생성에 필요한 필드만 받는 생성자에 붙입니다.
- JPA 엔티티는 `BaseEntity`를 통해 `createdAt`, `updatedAt`을 공통으로 관리합니다.
- `BaseEntity`에는 `@MappedSuperclass`와 `AuditingEntityListener`를 적용하고, JPA Auditing 설정을 함께 구성합니다.
- `createdAt`에는 `@CreatedDate`, `updatedAt`에는 `@LastModifiedDate`를 사용합니다.

### 패키지 구조

도메인별로 패키지를 구성하고, 여러 도메인에서 사용하는 코드는 `global`에서 관리합니다.

```text
nailroad
├── domain
│   └── user
│       ├── controller
│       ├── service
│       ├── repository
│       ├── entity
│       └── dto
│           ├── request
│           └── response
└── global
    ├── response       # 공통 API 응답
    ├── code           # 공통 성공·오류 코드
    ├── common         # BaseEntity 등 공통 기반 클래스
    ├── config         # 설정
    ├── exception      # 커스텀 예외
    └── handler        # 전역 예외 처리
```

위 구조를 기준으로 각 기능에서 필요한 패키지와 클래스를 추가합니다.

### 조회와 API 응답

- 단순 조회는 `findById`, `findByUserId` 등 메서드 이름 기반 쿼리를 사용합니다.
- 조인이나 고정된 조건의 복잡한 조회는 `@Query`로 JPQL을 작성합니다.
- API 경로는 `/api/v1`으로 시작합니다.
- 공통 API 응답은 `ApiResponse<T>`로 통일합니다.
- 응답 필드는 `isSuccess`, `code`, `message`, `result`, `error` 순서로 작성합니다.
- 성공 응답은 `isSuccess`를 `true`로 설정하고, 결과가 있으면 `result`에 담습니다.
- 실패 응답은 `isSuccess`를 `false`로 설정하고, 오류 코드와 메시지 및 상세 오류를 반환합니다.
- `result`와 `error`에 값이 없으면 `null`로 반환합니다.

## Git 컨벤션

### 브랜치 전략

```text
main        # 배포 가능한 안정 버전, PR을 통해서만 병합
develop     # 개발 통합 브랜치
feature/*   # 기능 개발
fix/*       # 버그 수정
refactor/*  # 기능 변화 없는 코드 개선
test/*      # 테스트 작성 및 수정
docs/*      # 문서 작성 및 수정
chore/*     # 설정 및 기타 작업
build/*     # Gradle 및 의존성 변경
```

- 작업은 항상 Issue를 먼저 생성한 뒤 브랜치를 생성합니다.
- 작업 브랜치는 최신 `develop`에서 생성하고, 작업 완료 후 `develop`을 대상으로 PR을 생성합니다.
- 초기 세팅은 `main`에서 작업 브랜치를 생성하고, `main`을 대상으로 PR을 생성합니다. 병합 후 `develop`을 생성합니다.
- 배포할 때는 `develop`에서 `main`으로 PR을 생성합니다.
- `main`에는 직접 push하지 않습니다.

### 브랜치 네이밍

`타입/#이슈번호-간단한-설명` 형식을 사용하며, 설명은 영문 소문자와 하이픈(`-`)으로 작성합니다.

```text
feature/#12-schedule-generation
fix/#27-login-token-error
chore/#1-backend-setup
```

브랜치명에 `#`이 포함되므로 명령어에서는 브랜치명을 따옴표로 감싸 작성합니다.

```bash
git switch -c 'chore/#1-backend-setup'
```

### Issue 컨벤션

새로운 기능, 오류 수정, 리팩토링 등의 작업을 시작하기 전에 GitHub Issue를 생성합니다.

제목은 `[TYPE] 작업 내용` 형식으로 작성합니다.

| Type | 용도 |
| --- | --- |
| `FEAT` | 새로운 기능 구현 |
| `FIX` | 버그 수정 |
| `REFACTOR` | 기능 변화 없는 코드 개선 |
| `TEST` | 테스트 코드 작성 및 수정 |
| `DOCS` | 문서 작성 및 수정 |
| `CHORE` | 설정 및 기타 작업 |
| `BUILD` | Gradle 및 의존성 변경 |

```text
[FEAT] 예약 신청 API 구현
[FIX] 로그인 토큰 검증 오류 수정
[CHORE] 백엔드 초기 세팅
```

- Issue Type은 기능 추가에 `Feature`, 버그 수정에 `Bug`, 그 외 작업에 `Task`를 사용합니다.
- Issue 본문은 어떤 기능인가요, 상세 내용, 체크 리스트, 기타 순서로 작성합니다.
- 체크 리스트로 작업 범위와 진행 상황을 표시합니다.
- [Issue 템플릿](.github/ISSUE_TEMPLATE/issue-template.md)을 사용합니다.

### 커밋 컨벤션

`type: 작업 내용 (#이슈번호)` 형식으로 작성합니다.

| Type | 용도 |
| --- | --- |
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `refactor` | 기능 변화 없는 코드 개선 |
| `test` | 테스트 코드 작성 및 수정 |
| `docs` | 문서 작성 및 수정 |
| `chore` | 설정 및 기타 작업 |
| `build` | Gradle 및 의존성 변경 |

```text
feat: 초기 엔티티 설정 (#12)
fix: 로그인 토큰 검증 오류 수정 (#27)
docs: README 컨벤션 작성 (#1)
chore: Issue 및 PR 템플릿 추가 (#1)
```

- 작업 내용을 명확하고 간결하게 작성합니다.
- 하나의 커밋에는 하나의 목적을 담습니다.
- 커밋 메시지 끝에는 마침표를 붙이지 않습니다.

### PR 컨벤션

- [PR 템플릿](.github/pull_request_template.md)을 사용합니다.
- 제목은 Issue와 같은 `[TYPE] 작업 내용 (#이슈번호)` 형식으로 작성합니다.
- 작업 이유와 세부 내용, 관련 Issue 번호를 본문에 작성합니다.
- 화면이나 응답 예시가 필요한 경우 스크린샷을 첨부합니다.
- 로컬 테스트 결과, 병합 대상 브랜치, 관련 Label을 확인한 뒤 리뷰를 요청합니다.
