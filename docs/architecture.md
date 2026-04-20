# Architecture

## 서비스 책임
- Authz-server는 Gateway가 관리자 경로(`/admin/**`, `/v1/admin/**`) 요청을 처리하기 전에 최종 인가 판정을 제공하는 내부 서비스입니다.
- 에디터 서버(Block-server) 관리자 API 보호를 위한 capability 진실을 소유합니다.
- User-server의 인증 역할이나 프로필 공개 정책은 직접 신뢰하지 않고, Authz DB의 권한 역할만 인가 근거로 사용합니다.
- 실제 기능 집행은 소비자 서비스가 최종 강제하고, Authz-server는 허용/거부 판정을 반환합니다.

## Repository Context
- 구현 레포: `https://github.com/jho951/Authz-server`
- 로컬 개발 경로: `/Users/jhons/Downloads/BE/services/Authz-server`
- Gateway 연동 대상: `/Users/jhons/Downloads/BE/Api-gateway-server`
- Editor(Block) 연동 대상: `/Users/jhons/Downloads/BE/Block-server`

## Stack
- Java 17
- Spring Boot 3.5.13
- Spring Security
- Spring Data JPA
- Redis
- H2 local/runtime fallback
- Gradle 멀티모듈

## 모듈 구조
- `common`: Authz-server 내부 공통 기반 모듈입니다.
- `app`: Spring Boot 실행 애플리케이션입니다.

`common`에는 아래처럼 app 전역에서 반복되는 얇은 기반 타입만 둡니다.
- `BaseEntity`
- `BaseResponse`
- `ErrorResponse`
- `ErrorCode`
- `BaseException`
- `BadRequestException`
- `CommonExceptionHandler`

`app`에는 실행 정책과 업무 흐름을 둡니다.
- `config`: OpenAPI, governance audit sink, request logging, health/readiness endpoint
- `security`: platform-security filter chain, internal request authentication
- `domain.authorization`: 권한 검증 controller/dto/entity/model/repository/service/policy/cache
- `domain.audit`: permission audit logger와 governance policy plugin
- resource 설정
- test

## 주요 흐름
1. Gateway가 관리자 경로 요청을 수신합니다.
2. Gateway가 Authz-server의 `POST /permissions/internal/admin/verify`를 호출합니다.
3. Authz-server는 내부 caller proof, 필수 헤더, 원본 method/path 형식을 검증합니다.
4. `X-User-Id`로 Authz DB의 `user_roles`를 조회합니다.
5. `permission.route-policy.routes`로 원본 method/path를 permission code에 매핑합니다.
6. `role_permissions` 매핑에 필요한 permission code가 있는지 확인해 허용 여부를 판정합니다.
7. 판정 결과와 감사 로그를 남기고 `200` 또는 `403`을 반환합니다.

## 코드 경계
- `X-User-Role`은 권한 판단에 사용하지 않습니다.
- route policy에 명시되지 않은 경로는 기본 DENY입니다.
- 명시 허용 외 기본 DENY입니다.
- Redis 캐시는 장애 시 DB fallback을 허용하며, role-permission version을 cache key에 포함합니다.
- 운영 profile은 internal service JWT를 내부 caller proof로 사용합니다. legacy shared secret은 local/test 호환용입니다.

## 채택된 결정 요약
- Editor 관리자 경로용 Authz Service MVP를 채택합니다.
- Authz 권한 판단에서 `X-User-Role` 헤더를 제외합니다.
- `platform-security`와 `platform-governance` 최신 배포본을 소비합니다.
- Auth-server와 맞춘 `app`, `common` 멀티모듈 구조를 사용합니다.
