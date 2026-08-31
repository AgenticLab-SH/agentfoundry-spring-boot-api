# AgentFoundry

[![CI](https://github.com/AgenticLab-SH/agentfoundry-spring-boot-api/actions/workflows/ci.yml/badge.svg)](https://github.com/AgenticLab-SH/agentfoundry-spring-boot-api/actions/workflows/ci.yml)

AI Agent 자산·구축 서비스·교육·프로젝트 협업을 Agent Card로 등록하고, 사용자의
목적·환경·리소스·예산과 맞는 항목을 설명 가능한 점수로 연결하는 Spring Boot REST
API 과제 프로젝트입니다.

실제 Agent나 외부 AI를 실행하지 않습니다. 규칙 기반 매칭, 참여 트랜잭션과 내부
크레딧으로 수업 범위 안에서 플랫폼의 핵심 흐름을 재현합니다.

## 실행

Java 21이 필요합니다.

```bash
./gradlew clean test
./gradlew bootRun
```

macOS에서 기본 Java가 17로 선택될 경우 Java 21을 명시합니다.

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew clean test
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew bootRun
```

- 웹 화면: <http://localhost:8080/>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/api-docs>
- H2 Console: <http://localhost:8080/h2-console>

H2 JDBC URL은 `jdbc:h2:mem:agentfoundrydb`, 사용자는 `sa`, 비밀번호는 없습니다.

## 검증

로컬과 GitHub Actions는 같은 Gradle Wrapper와 Java 21로 전체 테스트를 실행합니다.

```bash
./gradlew clean test --no-daemon
```

CI는 저장소 읽기 권한만 사용하고, 브랜치마다 이전 실행을 취소해 중복 실행을 줄입니다.
Gradle과 GitHub Actions 의존성은 Dependabot이 매주 갱신 후보를 제안하며, 실제 반영은
이 테스트를 통과한 변경만 검토합니다.

## 데모 계정

| ID | 비밀번호 | 용도 |
|---|---|---|
| `demo_requester` | `demo1234` | 요청 201의 매칭·참여·취소 |
| `agent_maker` | `demo1234` | Offering 제공자·완료 처리 |
| `low_credit` | `demo1234` | 크레딧 부족 롤백 |
| `project_provider` | `demo1234` | 프로젝트 협업 제공자 |
| `experienced_user` | `demo1234` | 활성·완료 참여와 추천 이력 |

모든 계정, Agent Card, 요청과 프로젝트는 과제 검증용 가상 데이터입니다. 비밀번호는
교육용 세션 로그인을 재현하기 위한 데모 값이며 API 응답과 로그에 포함하지 않습니다.
실제 서비스에서는 비밀번호 해시와 별도의 인증·인가 구성이 필요합니다.

## 핵심 기능

- AgentOffering·AgentRequest CRUD, 검색, 필터, 정렬과 페이징
- 유형·구성·도메인·환경·메모리·예산·신뢰도를 나눈 100점 매칭
- 참여 시 요청자 크레딧·슬롯·요청 상태·참여·원장 원자 처리
- 취소 시 환급·슬롯·요청 상태 복구
- 완료 시 제공자 보상·완료 건수·요청 상태 변경
- 완료한 요청자만 한 번 작성할 수 있는 추천
- DTO Validation, 소유자 검증과 `@RestControllerAdvice` 오류 계약
- H2 `data.sql`, Swagger와 같은 API를 사용하는 데스크톱 웹 화면
- Actuator `health/info`와 요청 본문을 기록하지 않는 AOP 처리시간 로그

내부 크레딧은 현금 가치가 없으며 실제 결제·환전·수익·투자·고용 기능을 제공하지
않습니다. 실제 Agent 실행, 외부 AI API, 크롤링과 파일 업로드도 포함하지 않습니다.
