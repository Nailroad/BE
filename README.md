# Nailroad BE

## 서비스 소개

네일로드는 원하는 네일 디자인을 찾고, 네일샵에 견적을 요청해 시술을 예약하는 서비스입니다.

## 기술 스택

- Java 21
- Spring Boot 4.1.2 · Spring MVC
- Spring Data JPA · PostgreSQL
- Gradle Wrapper 9.8.2
- SpringDoc OpenAPI 3.2.0

## 로컬 실행

PostgreSQL 연결 정보를 환경변수로 지정한 뒤 `./gradlew bootRun`을 실행합니다.

| 환경변수 | 설명 |
| --- | --- |
| `DB_URL` | JDBC URL (예: `jdbc:postgresql://localhost:5432/nailroad`) |
| `DB_USERNAME` | PostgreSQL 사용자 이름 |
| `DB_PASSWORD` | PostgreSQL 비밀번호 |

`./gradlew test`도 위 환경변수로 PostgreSQL에 연결합니다. 테스트에서는 스키마 자동 변경을 끕니다.

## 팀원

| [구동현](https://github.com/9dongs) | [Seoyoung Park](https://github.com/Mymyseoyoung) |
| :---: | :---: |
| <a href="https://github.com/9dongs"><img src="https://avatars.githubusercontent.com/u/144160177?v=4" width="120" alt="구동현 GitHub 프로필" /></a> | <a href="https://github.com/Mymyseoyoung"><img src="https://avatars.githubusercontent.com/u/162325695?v=4" width="120" alt="Seoyoung Park GitHub 프로필" /></a> |
| Backend Developer | Backend Developer |
| [@9dongs](https://github.com/9dongs) | [@Mymyseoyoung](https://github.com/Mymyseoyoung) |
